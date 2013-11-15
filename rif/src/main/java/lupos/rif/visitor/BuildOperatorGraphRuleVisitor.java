/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.rif.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.EmptyIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.IndexJoinWithDuplicateElimination;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.minus.Minus;
import lupos.engine.operators.singleinput.Construct;
import lupos.engine.operators.singleinput.MakeBooleanResult;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.singleinput.modifiers.distinct.Distinct;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.util.ImmutableIterator;
import lupos.rif.IExpression;
import lupos.rif.RIFException;
import lupos.rif.builtin.RIFBuiltinFactory;
import lupos.rif.datatypes.Predicate;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.Uniterm;
import lupos.rif.operator.BooleanIndexScan;
import lupos.rif.operator.ConstructEquality;
import lupos.rif.operator.ConstructPredicate;
import lupos.rif.operator.EqualityFilter;
import lupos.rif.operator.InsertTripleIndexScan;
import lupos.rif.operator.IteratorIndexScan;
import lupos.rif.operator.PredicateIndexScan;
import lupos.rif.operator.PredicatePattern;
import lupos.rif.operator.RuleFilter;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class BuildOperatorGraphRuleVisitor extends BaseGraphBuilder {
	protected final String VARIABLE_PREDICATE = "?";

	protected final Map<KEY, Set<BasicOperator>> tripleProducer = new LinkedHashMap<KEY, Set<BasicOperator>>();
	protected final Map<KEY, Set<BasicOperator>> tripleConsumer = new LinkedHashMap<KEY, Set<BasicOperator>>();
	protected Multimap<IExpression, IExpression> equalityMap;
	protected boolean usesEqualities = false;

	public static abstract class KEY implements Iterable<KEY>{
		// used as unifying superclass for the keys of triple patterns, predicates and equality!
		protected abstract int getNumberOfPossibleMatchingKeys();

		@Override
		public Iterator<KEY> iterator(){
			// consider all possible combinations!
			return new ImmutableIterator<KEY>(){
				final int max = 1 << KEY.this.getNumberOfPossibleMatchingKeys();
				int current = 0;
				@Override
				public boolean hasNext() {
					return this.current < this.max;
				}
				@Override
				public KEY next() {
					if(this.hasNext()){
						final KEY result = KEY.this.getKey(this.current);
						this.current++;
						return result;
					} else {
						return null;
					}
				}
			};
		}

		protected abstract KEY getKey(int current);

		protected static Variable dummyVariable = new Variable("d");
	}

	public static class KeyEquality extends KEY {
		private final static int hashValue = 157639892; // just take arbitrary number for hash vale
		@Override
		public int hashCode(){
			return KeyEquality.hashValue;
		}
		@Override
		public boolean equals(final Object other){
			return (other instanceof KeyEquality);
		}
		@Override
		protected int getNumberOfPossibleMatchingKeys() {
			return 1;
		}
		@Override
		protected KEY getKey(final int current) {
			return this;
		}
	}

	private final static KeyEquality keyEquality = new KeyEquality();

	public static class KeyTriplePattern extends KEY {
		private final TriplePattern triplePattern;
		public KeyTriplePattern(final TriplePattern triplePattern){
			this.triplePattern = triplePattern;
		}
		@Override
		public int hashCode(){
			int result =0;
			for(final Item item: this.triplePattern){
				if(!item.isVariable()){
					result = (int)((long) result + item.hashCode()) % Integer.MAX_VALUE;
				}
			}
			return result;
		}
		@Override
		public boolean equals(final Object object){
			if(object instanceof KeyTriplePattern){
				final KeyTriplePattern other = (KeyTriplePattern) object;
				for(int i=0; i<3; i++){
					final Item thisItem = this.triplePattern.getPos(i);
					final Item otherItem = other.triplePattern.getPos(i);
					if(!(thisItem.isVariable() == otherItem.isVariable() && (thisItem.isVariable()
							&& otherItem.isVariable() || thisItem.equals(otherItem)))){
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}

		public boolean mayConsume(final KeyTriplePattern other){
			final Iterator<Item> thisIterator = this.triplePattern.iterator();
			final Iterator<Item> otherIterator = other.triplePattern.iterator();
			while(thisIterator.hasNext()){
				final Item thisItem = thisIterator.next();
				final Item otherItem = otherIterator.next();
				if(!(otherItem.isVariable() || thisItem.isVariable() || thisItem.equals(otherItem))){
					return false;
				}
			}
			return true;
		}

		@Override
		protected int getNumberOfPossibleMatchingKeys() {
			int number=0;
			for(final Item item: this.triplePattern){
				if(!item.isVariable()) {
					number++;
				}
			}
			return number;
		}
		@Override
		protected KEY getKey(final int current) {
			final Item[] items = new Item[3];
			int bitValue = 1;
			int index = 0;
			for(final Item item: this.triplePattern){
				if(item.isVariable()){
					items[index] = KEY.dummyVariable;
				} else {
					items[index] = ((current / bitValue) % 2 == 0)? KEY.dummyVariable : item;
					bitValue*=2;
				}
				index++;
			}
			return new KeyTriplePattern(new TriplePattern(items));
		}
	}

	public static class KeyPredicatePattern extends KEY {
		private final PredicatePattern predicatePattern;
		public KeyPredicatePattern(final PredicatePattern predicatePattern){
			this.predicatePattern = predicatePattern;
		}
		@Override
		public int hashCode(){
			int result =0;
			for(final Item item: this.predicatePattern){
				if(!item.isVariable()){
					result = (int)((long) result + item.hashCode()) % Integer.MAX_VALUE;
				}
			}
			return result;
		}
		@Override
		public boolean equals(final Object object){
			if(object instanceof KeyPredicatePattern){
				final KeyPredicatePattern other = (KeyPredicatePattern) object;
				final Iterator<Item> thisIterator = this.predicatePattern.iterator();
				final Iterator<Item> otherIterator = other.predicatePattern.iterator();
				while(thisIterator.hasNext()){
					final Item thisItem = thisIterator.next();
					final Item otherItem = otherIterator.next();
					if(!(thisItem.isVariable() == otherItem.isVariable() && (thisItem.isVariable()
							&& otherItem.isVariable() || thisItem.equals(otherItem)))){
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		@Override
		protected int getNumberOfPossibleMatchingKeys() {
			int number=-1;
			for(final Item item: this.predicatePattern){
				if(!item.isVariable()) {
					number++;
				}
			}
			return number;
		}

		@Override
		protected KEY getKey(final int current) {
			final Item[] items = new Item[this.predicatePattern.getPatternItems().size()];
			int bitValue = 1;
			int index = 0;
			for(final Item item: this.predicatePattern.getPatternItems()){
				if(item.isVariable()){
					items[index] = KEY.dummyVariable;
				} else {
					items[index] = ((current / bitValue) % 2 == 0)? KEY.dummyVariable : item;
					bitValue*=2;
				}
				index++;
			}
			return new KeyPredicatePattern(new PredicatePattern(this.predicatePattern.getPredicateName(), items));
		}
	}

	public BuildOperatorGraphRuleVisitor(final IndexScanCreatorInterface indexScanCreator) {
		super(indexScanCreator);
	}


	@Override
	public Object visit(final Document obj, final Object arg) throws RIFException {
		this.tripleProducer.clear();
		this.tripleConsumer.clear();
		this.equalityMap = HashMultimap.create();
		this.usesEqualities = false;
		// 1. Fakten muessen als erstes ausgewertet werden und dann in allen
		// Operatorbaeumen beruecksichtigt werden.
		this.predicateIndex = null;
		InsertTripleIndexScan insertTripleIndex = null;
		for (final IExpression fact : obj.getFacts()) {
			if (fact instanceof Equality) {
				final Equality eq = (Equality) fact;
				this.equalityMap.put(eq.leftExpr, eq.rightExpr);
				this.equalityMap.put(eq.rightExpr, eq.leftExpr);
				this.usesEqualities = true;
			} else {
				final Object item = ((RulePredicate) fact).toDataStructure();
				if (item instanceof Triple) {
					insertTripleIndex = insertTripleIndex == null ? new InsertTripleIndexScan(this.indexScanCreator) : insertTripleIndex;
					insertTripleIndex.addTripleFact((Triple) item);
				} else if (item instanceof Predicate) {
					this.predicateIndex = this.predicateIndex == null ? new PredicateIndexScan() : this.predicateIndex;
					this.predicateIndex.addPredicateFact((Predicate) item);
				}
			}
		}
		if (insertTripleIndex != null){
			this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(insertTripleIndex, 0));
			insertTripleIndex.addPrecedingOperator(this.indexScanCreator.getRoot());
		}
		if (this.predicateIndex != null){
			this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(this.predicateIndex, 1));
			this.predicateIndex.addPrecedingOperator(this.indexScanCreator.getRoot());
		}

		// 2. ueber alle Regeln gehen und mitschreiben, welche
		// Tripel-Praedikate
		// produziert werden, Externals koennen nicht vorkommen
		for (final Rule rule : obj.getRules()) {
			if (rule.isImplication()) {
				for (final IExpression expr : rule.getHeadExpressions()) {
					// Tripel mit variablen Praedikat wird generiert
					if (expr instanceof RulePredicate) {
						final RulePredicate pred = (RulePredicate) expr;
						final BasicOperator pattern = this.generatePattern(pred, arg);
						final KEY key = (pattern instanceof TriplePattern)?
								new KeyTriplePattern((TriplePattern)pattern):
								new KeyPredicatePattern((PredicatePattern)pattern);
						this.tripleProducer.put(key, new LinkedHashSet<BasicOperator>());
					} else if (expr instanceof Equality) {
						this.tripleProducer.put(BuildOperatorGraphRuleVisitor.keyEquality, new LinkedHashSet<BasicOperator>());
						this.usesEqualities = true;
					}
				}
			}
		}

		// 3. Operatorgraphen fuer einzelne Regeln berechnen
		final List<BasicOperator> subOperators = new ArrayList<BasicOperator>();
		for (final Rule rule : obj.getRules()) {
			if (rule.isImplication()) {
				final BasicOperator result = (BasicOperator) rule.accept(this, arg);
				subOperators.add(result);
			}
		}

		// 4. Rekursive Verbindungen aufloesen
		for (final Entry<KEY, Set<BasicOperator>> entry : this.tripleProducer.entrySet()) {
			boolean consumerExists = false;
			// find all matching consumers by just getting the previous determined tripleConsumers...
			final Set<BasicOperator> consumers = this.tripleConsumer.get(entry.getKey());
			if(consumers!=null){
				consumerExists = true;
				// Kreuzverbindungen zwischen Produzenten und Konsumenten
				// herstellen
				for (final BasicOperator producer : entry.getValue()) {
					for (final BasicOperator consumer : consumers) {
						producer.addSucceedingOperator(new OperatorIDTuple(consumer, producer.getSucceedingOperators().size()));
						consumer.addPrecedingOperator(producer);
						// Sonderfall: Falls PredicatePattern ->
						// Dann: PredicatePattern -> Distinct ->
						// -----> should now be unnecessary with our new joins with duplicate elimination integrated! <----
//						if (consumer instanceof PredicatePattern) {
//							boolean distinctFound = false;
//							for (OperatorIDTuple opid : consumer
//									.getSucceedingOperators())
//								if (opid.getOperator() instanceof Distinct)
//									distinctFound = true;
//							if (!distinctFound) {
//								final Distinct distinct = new Distinct();
//								for(OperatorIDTuple opID: consumer.getSucceedingOperators()){
//									distinct.getSucceedingOperators().add(new OperatorIDTuple(opID));
//								}
//								consumer.setSucceedingOperator(new OperatorIDTuple(distinct, 0));
//							}
//						}
					}
				}
			}
			// Wenn keine Konsumenten, dann Produzenten entfernen
			if (!consumerExists) {
				for (final BasicOperator producer : entry.getValue()) {
					if (producer instanceof Generate) {
						producer.removeFromOperatorGraph();
					}
				}
			}
		}

		// 5. Ergebniss aller Regeln in einem Result zusammenfuehren
		BasicOperator finalResult = null;
		if (obj.getConclusion() == null) {
			finalResult = new Result();
		} else {
			finalResult = this.patternFromConclusion(obj.getConclusion());
		}

		// Verbindungen zum Endergebniss herstellen
		for (final BasicOperator subOperator : subOperators) {
			// Result immer auf linker Seite, damit keine Linksrekursion
			// auftreten kann
			if (!subOperator.getSucceedingOperators().isEmpty()) {
				final OperatorIDTuple temp = subOperator.getSucceedingOperators().get(0);
				subOperator.getSucceedingOperators().set(0,new OperatorIDTuple(finalResult, 0));
				finalResult.addPrecedingOperator(subOperator);
				subOperator.addSucceedingOperator(temp);
			} else {
				subOperator.setSucceedingOperator(new OperatorIDTuple(finalResult, 0));
				finalResult.addPrecedingOperator(subOperator);
			}
		}
		if (subOperators.isEmpty()) {
			// Root verweist auf EmptyIndex und der direkt auf Result
			if (finalResult instanceof PredicatePattern
					|| finalResult instanceof TriplePattern) {
				finalResult.removeFromOperatorGraph();
				finalResult = null;
			}
			final EmptyIndexScan empty = new EmptyIndexScan(finalResult == null ? null
					: new OperatorIDTuple(finalResult, 0));
			this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(empty, this.indexScanCreator.getRoot().getSucceedingOperators().size()));
			empty.addPrecedingOperator(this.indexScanCreator.getRoot());
			if (finalResult == null) {
				finalResult = empty;
			}
		}

		if (this.booleanIndex != null
				&& this.booleanIndex.getSucceedingOperators().isEmpty()){
			this.indexScanCreator.getRoot().removeSucceedingOperator(this.booleanIndex);
			this.booleanIndex.removePrecedingOperator(this.indexScanCreator.getRoot());
		}

		// Falls Conclusion vorhanden, noch Result anhaengen, zum Sammeln der
		// Ergebnisse
		if (!(finalResult instanceof Result)) {
			if (obj.getConclusion() != null
					&& obj.getConclusion().getVariables().isEmpty()) {
				final BasicOperator mbr = new MakeBooleanResult();
				finalResult.addSucceedingOperator(mbr);
				mbr.addPrecedingOperator(finalResult);
				finalResult = mbr;
			}
			// DEBUG
			//finalResult.addSucceedingOperator(finalResult = new Distinct());
			final Result result = new Result();
			finalResult.addSucceedingOperator(result);
			result.addPrecedingOperator(finalResult);
			return result;
		} else {
			return finalResult;
		}
	}

	private BasicOperator patternFromConclusion(final IExpression conclusion) {
		// Annahme, Conclusion ist Praedikat
		final RulePredicate pred = (RulePredicate) conclusion;
		if (pred.isTriple()) {
			return this.unitermToTriplePattern(pred);
		} else {
			final URILiteral predName = (URILiteral) pred.termName.accept(this,
					null);
			final List<Item> predItems = new ArrayList<Item>();
			for (final IExpression expr : pred.termParams) {
				final Item item = (Item) expr.accept(this, null);
				predItems.add(item);
			}
			return new PredicatePattern(predName,
					predItems.toArray(new Item[] {}));
		}
	}

	@Override
	public Object visit(final Rule obj, final Object arg) throws RIFException {
		// Besimmen, ob Triple, Predicates oder Equalities erstellt werden sollen
		final List<BasicOperator> resultOps = new ArrayList<BasicOperator>();
		final List<Equality> equalities = new ArrayList<Equality>();
		boolean generateTriples = false;
		boolean generatePredicates = false;
		boolean generateEqualities = false;
		for (final IExpression expr : obj.getHeadExpressions()) {
			if (expr instanceof RulePredicate) {
				if (((RulePredicate) expr).isTriple()) {
					generateTriples = true;
				} else {
					generatePredicates = true;
				}
			} else if (expr instanceof Equality) {
				generateEqualities = true;
				equalities.add((Equality) expr);
			}
		}

		// 1. Unteroperatorgraph berechnen
		BasicOperator subOperator = (BasicOperator) obj.getBody().accept(this, null);
		for(final OperatorIDTuple opID: subOperator.getSucceedingOperators()){
			opID.getOperator().removePrecedingOperator(subOperator);
		}
		subOperator.getSucceedingOperators().clear();
		// is one of the previous operators a join, which also eliminates duplicates?
		boolean foundJoin = false;
		BasicOperator current = subOperator;
		do {
			if(subOperator instanceof IndexJoinWithDuplicateElimination){
				foundJoin = true;
			} else {
				if(current.getPrecedingOperators().size()!=1){
					// Distinct is necessary!
					break;
				}
				current = current.getPrecedingOperators().get(0);
			}
		} while(!foundJoin);

		if(!foundJoin){
			// add DISTINCT operator in order to avoid infinity loops
			final Distinct distinct = new Distinct();
			subOperator.addSucceedingOperator(distinct);
			distinct.addPrecedingOperator(subOperator);
			subOperator = distinct;
		}

		// add nots under subOperator
		for(final IExpression not: obj.getNots()){
			// TODO check for recursion in not expression and throw error in that case
			// (In the current implementation, negation in rules retrieves reasonable results only for non-recursive rules)
			// first determine operator graph for the not expression:
			final BasicOperator notOperator = (BasicOperator) not.accept(this, null);
			for(final OperatorIDTuple opID: notOperator.getSucceedingOperators()){
				opID.getOperator().removePrecedingOperator(notOperator);
			}
			notOperator.getSucceedingOperators().clear();
			// now add a not operator below subOperator and notOperator!
			// Luckily, the semantics of the not operator is exactly the same as of the minus operator!
			final Minus minus = new Minus(false);
			subOperator.addSucceedingOperator(minus, 0);
			notOperator.addSucceedingOperator(minus, 1);
			minus.addPrecedingOperator(subOperator);
			minus.addPrecedingOperator(notOperator);
			subOperator = minus;
		}

		// 1. Construct erstellen
		if (generateTriples) {
			final Construct construct = new Construct();
			subOperator.addSucceedingOperator(construct);
			construct.addPrecedingOperator(subOperator);
			final List<TriplePattern> patterns = new ArrayList<TriplePattern>();
			for (final Uniterm term : obj.getHead().getPredicates()) {
				if (((RulePredicate) term).isTriple()) {
					final TriplePattern pattern = this.unitermToTriplePattern(term);
					patterns.add(pattern);
				}
			}
			construct.setTemplates(patterns);
			// Fuer jedes Triplepattern in Construct ein Generate fuer Inferenz erstellen
			// wird, falls es keinen Consumer gibt, spaeter wieder entfernt
			for (final TriplePattern pattern : construct.getTemplates()) {
				final Generate generateTriplesOp = new Generate(pattern.getItems());
				generateTriplesOp.addPrecedingOperator(subOperator);
				subOperator.addSucceedingOperator(new OperatorIDTuple(generateTriplesOp, subOperator.getSucceedingOperators().size()));
				generateTriplesOp.addPrecedingOperator(subOperator);
				// TripleProduzenten registrieren
				this.add(this.tripleProducer, new KeyTriplePattern(pattern), generateTriplesOp);
			}
			resultOps.add(construct);
		}

		// 2. ConstructPredicate erstellen
		if (generatePredicates) {
			final ConstructPredicate generate = new ConstructPredicate();
			subOperator.addSucceedingOperator(generate);
			generate.addPrecedingOperator(subOperator);
			for (final Uniterm term : obj.getHead().getPredicates()) {
				if (!((RulePredicate) term).isTriple()) {
					final URILiteral name = (URILiteral) term.termName.accept(this, arg);
					final List<Item> params = new ArrayList<Item>();
					for (final IExpression expr : term.termParams) {
						final Item item = (Item) expr.accept(this, arg);
						params.add(item);
					}
					final Item[] paramsArray = params.toArray(new Item[] {});
					generate.addPattern(name, paramsArray);
					// Produzenten registrieren
					this.add(this.tripleProducer, new KeyPredicatePattern(new PredicatePattern(name, paramsArray)), generate);
				}
			}
			resultOps.add(generate);
		}

		// 3. ConstructEquality erstellen
		if (generateEqualities) {
			final ConstructEquality constructEq = new ConstructEquality(
					this.equalityMap, equalities.toArray(new Equality[] {}));
			subOperator.addSucceedingOperator(constructEq);
			constructEq.addPrecedingOperator(subOperator);
			resultOps.add(constructEq);
			this.add(this.tripleProducer, BuildOperatorGraphRuleVisitor.keyEquality, constructEq);
		}
		if (resultOps.size() == 1) {
			return resultOps.iterator().next();
		} else {
			final Union union = new Union();
			for (final BasicOperator op : resultOps){
				union.addSucceedingOperator(op);
				op.addPrecedingOperator(union);
			}
			return union;
		}
	}

	private void add(final Map<KEY, Set<BasicOperator>> map, final KEY key, final BasicOperator toAdd){
		Set<BasicOperator> set = map.get(key);
		if(set==null){
			set = new LinkedHashSet<BasicOperator>();
			map.put(key, set);
		}
		set.add(toAdd);
	}

	@Override
	public Object visit(final Conjunction obj, final Object arg) throws RIFException {
		// Vorgehensweise: erstmal alle Sub-Operatoren sammeln -> Danach:
		final Set<BasicOperator> operands = new LinkedHashSet<BasicOperator>();
		final Set<BasicIndexScan> indexes = new LinkedHashSet<BasicIndexScan>();
		final List<RuleFilter> predicates = new ArrayList<RuleFilter>();

		for (final IExpression expr : obj.exprs) {
			final BasicOperator op = (BasicOperator) expr.accept(this, arg);
			if (op instanceof RuleFilter) {
				predicates.add((RuleFilter) op);
				continue;
			}
			operands.add(op);
			if (op instanceof BasicIndexScan && !(op instanceof IteratorIndexScan)) {
				indexes.add((BasicIndexScan) op);
			}
		}
		// 1. Mergen von Indexen
		BasicIndexScan mainIndex = null;

		if (indexes.size() > 1) {
			final Iterator<BasicIndexScan> it = indexes.iterator();
			mainIndex = it.next();
			while (it.hasNext()) {
				final BasicIndexScan mergeIndex = it.next();
				mainIndex.getTriplePattern().addAll(mergeIndex.getTriplePattern());
				for(final OperatorIDTuple opID: mergeIndex.getSucceedingOperators()){
					opID.getOperator().removePrecedingOperator(mergeIndex);
				}
				mergeIndex.getSucceedingOperators().clear();
				mergeIndex.removeFromOperatorGraph();
				operands.remove(mergeIndex);
			}
			if(arg!=null){
				mainIndex.setSucceedingOperator((OperatorIDTuple) arg);
				((OperatorIDTuple) arg).getOperator().addPrecedingOperator(mainIndex);
			}
		}
		// 2. Joins erstellen
		BasicOperator headOperator = null;
		final Iterator<BasicOperator> opIt = operands.iterator();
		if (operands.size() > 1) {
			Join bottomJoin = new IndexJoinWithDuplicateElimination();
			final BasicOperator op1 = opIt.next();
			for(final OperatorIDTuple opID: op1.getSucceedingOperators()){
				opID.getOperator().removePrecedingOperator(op1);
			}
			op1.getSucceedingOperators().clear();
			op1.addSucceedingOperator(new OperatorIDTuple(bottomJoin, 0));
			bottomJoin.addPrecedingOperator(op1);
			final BasicOperator op2 = opIt.next();
			for(final OperatorIDTuple opID: op2.getSucceedingOperators()){
				opID.getOperator().removePrecedingOperator(op2);
			}
			op2.getSucceedingOperators().clear();
			op2.addSucceedingOperator(new OperatorIDTuple(bottomJoin, 1));
			bottomJoin.addPrecedingOperator(op2);
			if(arg!=null){
				bottomJoin.setSucceedingOperator((OperatorIDTuple) arg);
				((OperatorIDTuple) arg).getOperator().addPrecedingOperator(bottomJoin);
			}
			while (opIt.hasNext()) {
				final Join tempJoin = new IndexJoinWithDuplicateElimination();
				final BasicOperator operand = opIt.next();
				for(final OperatorIDTuple opID: operand.getSucceedingOperators()){
					opID.getOperator().removePrecedingOperator(operand);
				}
				operand.getSucceedingOperators().clear();
				operand.setSucceedingOperator(new OperatorIDTuple(tempJoin, 0));
				tempJoin.addPrecedingOperator(operand);
				for(final OperatorIDTuple opID: bottomJoin.getSucceedingOperators()){
					opID.getOperator().removePrecedingOperator(bottomJoin);
				}
				bottomJoin.getSucceedingOperators().clear();
				bottomJoin.setSucceedingOperator(new OperatorIDTuple(tempJoin, 1));
				tempJoin.addPrecedingOperator(bottomJoin);
				if(arg!=null){
					tempJoin.setSucceedingOperator((OperatorIDTuple) arg);
					((OperatorIDTuple) arg).getOperator().addPrecedingOperator(tempJoin);
				}
				bottomJoin = tempJoin;
			}
			headOperator = bottomJoin;
		} else if (operands.size() == 1) {
			headOperator = opIt.next();
		}

		// 3. Predicates davorschalten
		if (!predicates.isEmpty()) {
			// 3.1 Predicates sortieren, alle m�glichen Assignments nach vorn
			if (predicates.size() > 1) {
				int i = 0;
				final Set<RuleFilter> visited = new LinkedHashSet<RuleFilter>();
				while (i < predicates.size()) {
					if (!predicates.get(i).getExpression()
							.isPossibleAssignment()
							&& !visited.contains(predicates.get(i))) {
						final RuleFilter temp = predicates.get(i);
						predicates.remove(temp);
						predicates.add(temp);
						visited.add(temp);
						continue;
					}
					i++;
				}
			}
			// 3.2 Predicates in Baum einordnen
			// Sonderfall: Kein HeadOperator sondern nur RuleFilter
			if (headOperator == null) {
				headOperator = this.booleanIndex;
				for (final RuleFilter filter : predicates){
					headOperator.removeSucceedingOperator(filter);
					filter.removePrecedingOperator(headOperator);
				}
			} else {
				for(final OperatorIDTuple opID: headOperator.getSucceedingOperators()){
					opID.getOperator().removePrecedingOperator(headOperator);
				}
				headOperator.getSucceedingOperators().clear();
			}
			// Filter einbauen
			for (final BasicOperator pred : predicates) {
				headOperator.setSucceedingOperator(new OperatorIDTuple(pred, 0));
				pred.addPrecedingOperator(headOperator);
				if (headOperator != this.booleanIndex){
					this.booleanIndex.removeSucceedingOperator(pred);
					pred.removePrecedingOperator(this.booleanIndex);
				}
				headOperator = pred;
			}
			if(arg!=null){
				headOperator.setSucceedingOperator((OperatorIDTuple) arg);
				((OperatorIDTuple) arg).getOperator().addPrecedingOperator(headOperator);
			}
		}
		return headOperator;
	}

	@Override
	public Object visit(final Disjunction obj, final Object arg) throws RIFException {
		// Einf�hrung eines Union Operators, der alle Untergeordneten
		// Operatoren
		// zusammenf�hrt
		final Union union = new Union();
//		final Distinct distinct = new Distinct();
//		distinct.setSucceedingOperator((OperatorIDTuple) arg);
//		union.addSucceedingOperator(distinct);
		for (final IExpression expr : obj.exprs){
			expr.accept(this, new OperatorIDTuple(union, union.getSucceedingOperators().size()));
		}

//		return distinct;
		return union;
	}

	@Override
	public Object visit(final ExistExpression obj, final Object arg) throws RIFException {
		if (obj.getVariables().isEmpty()) {
			// keine Variablen zum joinen bzw. vereinen -> BooleanResult
			final MakeBooleanResult mbr = new MakeBooleanResult();
			if(arg!=null){
				mbr.setSucceedingOperator((OperatorIDTuple) arg);
				((OperatorIDTuple) arg).getOperator().addPrecedingOperator(mbr);
			}
			obj.expr.accept(this, new OperatorIDTuple(mbr, 0));
			return mbr;
		} else {
			// Variablen zum joinen vorhanden
			return obj.expr.accept(this, arg);
		}
	}

	public BasicOperator generatePattern(final RulePredicate obj, final Object arg){
		// Unterscheidung:
		// Wenn Pr�dikat, also kein Tripel
		if (!obj.isTriple()) {
			// PredicatePattern erstellen
			final URILiteral predName = (URILiteral) obj.termName.accept(this, arg);
			final List<Item> predItems = new ArrayList<Item>();
			for (final IExpression expr : obj.termParams) {
				final Item item = (Item) expr.accept(this, arg);
				predItems.add(item);
			}
			return new PredicatePattern(predName, predItems.toArray(new Item[] {}));
		}
		return this.unitermToTriplePattern(obj);
	}

	@Override
	public Object visit(final RulePredicate obj, final Object arg) throws RIFException {
		final BasicOperator zpattern = this.generatePattern(obj, arg);
		if (zpattern instanceof PredicatePattern) {
			final PredicatePattern predPat = (PredicatePattern) zpattern;
			if (this.predicateIndex == null) {
				this.predicateIndex = new PredicateIndexScan();
				this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(this.predicateIndex, this.indexScanCreator.getRoot().getSucceedingOperators().size()));
				this.predicateIndex.addPrecedingOperator(this.indexScanCreator.getRoot());
			}
			this.predicateIndex.addSucceedingOperator(new OperatorIDTuple(predPat, this.predicateIndex.getSucceedingOperators().size()));
			predPat.addPrecedingOperator(this.predicateIndex);
			if(arg!=null){
				predPat.setSucceedingOperator((OperatorIDTuple) arg);
				((OperatorIDTuple) arg).getOperator().addPrecedingOperator(predPat);
			}
			// Pr�dikatkonsumenten anmelden
			this.add(this.tripleConsumer, new KeyPredicatePattern(predPat), predPat);
			return predPat;
		}
		// Normale TripleBearbeitung
		// 1. TriplePattern erstellen
		final TriplePattern pattern = (TriplePattern) zpattern;

		// 2. Index erstellen, noch ohne succeding operator
		final BasicOperator index = this.indexScanCreator.createIndexScanAndConnectWithRoot(null, new ArrayList<TriplePattern>(Arrays.asList(pattern)), null);
		index.setPrecedingOperator(this.indexScanCreator.getRoot());

		// 3. Pr�fen ob Triple-Pr�dikat an anderer Stelle erzeugt wird
		final KeyTriplePattern keyPattern = new KeyTriplePattern(pattern);
		boolean flag = false;

		final HashSet<KeyTriplePattern> possibleMatchingKeysOfProducers = new LinkedHashSet<KeyTriplePattern>();

		for(final KEY mainkey: this.tripleProducer.keySet()){
			if(mainkey instanceof KeyTriplePattern){
				final KeyTriplePattern mainkeyTP = (KeyTriplePattern) mainkey;
				if(keyPattern.mayConsume(mainkeyTP)){
					possibleMatchingKeysOfProducers.add(mainkeyTP);
					flag = true;
				}
			}
		}
		if (flag) {
			// index -> (union -> distinct) <- triplepattern : return union
//			Distinct distinct = new Distinct();
			final Union union = new Union();
//			union.setSucceedingOperator(new OperatorIDTuple(distinct, 0));
			index.setSucceedingOperator(new OperatorIDTuple(union, 0));
			union.addPrecedingOperator(index);
//			distinct.setSucceedingOperator((OperatorIDTuple) arg);
			pattern.setSucceedingOperator(new OperatorIDTuple(union, 1));
			union.addPrecedingOperator(pattern);
			for(final KeyTriplePattern keyTP: possibleMatchingKeysOfProducers){
				this.add(this.tripleConsumer, keyTP, pattern);
			}
//			return distinct;
			if(arg!=null){
				union.setSucceedingOperator((OperatorIDTuple) arg);
				((OperatorIDTuple) arg).getOperator().addPrecedingOperator(union);
			}
			return union;
		} else {
			if(arg!=null){
				index.setSucceedingOperator((OperatorIDTuple) arg);
				((OperatorIDTuple) arg).getOperator().addPrecedingOperator(index);
			}
			return index;
		}
	}

	@Override
	public Object visit(final External obj, final Object arg) throws RIFException {
		// Wenn iterierbar, dann Index erstellen
		// TODO: pr�fen ob Variable nicht anderweitig gebunden, in Validate,
		// dann irgendwie verf�gbar machen das genau hier
		// ein IteratorIndex notwendig ist;
		final URILiteral name = (URILiteral) ((Constant) obj.termName)
				.getLiteral();
		if (RIFBuiltinFactory.isIterable(name)) {
			final BasicOperator root = this.indexScanCreator.getRoot();
			final IteratorIndexScan index = new IteratorIndexScan((root instanceof Root)? (Root) root : null, obj);
			this.indexScanCreator.getRoot().addSucceedingOperator(index);
			index.addPrecedingOperator(this.indexScanCreator.getRoot());
			return index;
		} else {
			return this.buildRuleFilter(obj, arg);
		}
	}

	@Override
	protected RuleFilter buildRuleFilter(final IExpression expr, final Object arg) {
		if (this.booleanIndex == null) {
			final BasicOperator root = this.indexScanCreator.getRoot();
			this.booleanIndex = new BooleanIndexScan((root instanceof Root)? (Root) root : null);
			this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(this.booleanIndex, 0));
			this.booleanIndex.addPrecedingOperator(this.indexScanCreator.getRoot());
		}
		RuleFilter filter = null;
		if (!this.usesEqualities || !(expr instanceof Equality)) {
			filter = new RuleFilter(expr, this.equalityMap);
		} else {
			filter = new EqualityFilter(expr, this.equalityMap);
			this.add(this.tripleConsumer, BuildOperatorGraphRuleVisitor.keyEquality, filter);
		}
		this.booleanIndex.addSucceedingOperator(filter);
		filter.addPrecedingOperator(this.booleanIndex);
		if(arg!=null){
			filter.setSucceedingOperator((OperatorIDTuple) arg);
			((OperatorIDTuple) arg).getOperator().addPrecedingOperator(filter);
		}
		return filter;
	}
}
