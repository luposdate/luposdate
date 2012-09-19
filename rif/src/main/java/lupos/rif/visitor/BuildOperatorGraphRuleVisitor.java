/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
import java.util.HashMap;
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
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.EmptyIndex;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Construct;
import lupos.engine.operators.singleinput.MakeBooleanResult;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.singleinput.modifiers.distinct.Distinct;
import lupos.engine.operators.tripleoperator.TriplePattern;
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
import lupos.rif.operator.BooleanIndex;
import lupos.rif.operator.ConstructEquality;
import lupos.rif.operator.ConstructPredicate;
import lupos.rif.operator.EqualityFilter;
import lupos.rif.operator.InsertTripleIndex;
import lupos.rif.operator.IteratorIndex;
import lupos.rif.operator.PredicateIndex;
import lupos.rif.operator.PredicatePattern;
import lupos.rif.operator.RuleFilter;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class BuildOperatorGraphRuleVisitor extends BaseGraphBuilder {
	protected final String VARIABLE_PREDICATE = "?";
	/**
	 * String -> Pr�dikatbezeichung bsp. cdp:example(...)
	 */
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
			return new Iterator<KEY>(){
				final int max = 1 << KEY.this.getNumberOfPossibleMatchingKeys();
				int current = 0;
				@Override
				public boolean hasNext() {
					return this.current < this.max;
				}
				@Override
				public KEY next() {
					if(this.hasNext()){
						KEY result = KEY.this.getKey(this.current);
						this.current++;
						return result;
					} else {
						return null;
					}
				}
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
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
		public boolean equals(Object other){
			return (other instanceof KeyEquality);
		}
		@Override
		protected int getNumberOfPossibleMatchingKeys() {
			return 1;
		}
		@Override
		protected KEY getKey(int current) {
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
			for(Item item: this.triplePattern){
				if(!item.isVariable()){
					result = (int)((long) result + item.hashCode()) % Integer.MAX_VALUE;
				}
			}
			return result;
		}
		@Override
		public boolean equals(Object object){
			if(object instanceof KeyTriplePattern){
				KeyTriplePattern other = (KeyTriplePattern) object;
				for(int i=0; i<3; i++){
					Item thisItem = this.triplePattern.getPos(i);
					Item otherItem = other.triplePattern.getPos(i);  
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
		
		public boolean mayConsume(KeyTriplePattern other){
			Iterator<Item> thisIterator = this.triplePattern.iterator();
			Iterator<Item> otherIterator = other.triplePattern.iterator();
			while(thisIterator.hasNext()){
				Item thisItem = thisIterator.next();					
				Item otherItem = otherIterator.next();  
				if(!(otherItem.isVariable() || thisItem.isVariable() || thisItem.equals(otherItem))){
					return false;
				}
			}
			return true;
		}

		@Override
		protected int getNumberOfPossibleMatchingKeys() {
			int number=0;
			for(Item item: this.triplePattern){
				if(!item.isVariable())
					number++;
			}
			return number;
		}
		@Override
		protected KEY getKey(int current) {
			Item[] items = new Item[3];
			int bitValue = 1;
			int index = 0;
			for(Item item: this.triplePattern){
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
			for(Item item: this.predicatePattern){
				if(!item.isVariable()){
					result = (int)((long) result + item.hashCode()) % Integer.MAX_VALUE;
				}
			}
			return result;
		}
		@Override
		public boolean equals(Object object){
			if(object instanceof KeyPredicatePattern){
				KeyPredicatePattern other = (KeyPredicatePattern) object;
				Iterator<Item> thisIterator = this.predicatePattern.iterator();
				Iterator<Item> otherIterator = other.predicatePattern.iterator();
				while(thisIterator.hasNext()){
					Item thisItem = thisIterator.next();					
					Item otherItem = otherIterator.next();  
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
			for(Item item: this.predicatePattern){
				if(!item.isVariable())
					number++;
			}
			return number;
		}
		
		@Override
		protected KEY getKey(int current) {
			Item[] items = new Item[this.predicatePattern.getPatternItems().size()];
			int bitValue = 1;
			int index = 0;
			for(Item item: this.predicatePattern.getPatternItems()){
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
	public Object visit(Document obj, Object arg) throws RIFException {
		this.tripleProducer.clear();
		this.tripleConsumer.clear();
		this.equalityMap = HashMultimap.create();
		this.usesEqualities = false;
		// 1. Fakten m�ssen als erstes ausgewertet werden und dann in allen
		// Operatorb�umen ber�cksichtigt werden.
		this.predicateIndex = null;
		InsertTripleIndex insertTripleIndex = null;
		for (IExpression fact : obj.getFacts())
			if (fact instanceof Equality) {
				final Equality eq = (Equality) fact;
				this.equalityMap.put(eq.leftExpr, eq.rightExpr);
				this.equalityMap.put(eq.rightExpr, eq.leftExpr);
				this.usesEqualities = true;
			} else {
				final Object item = ((RulePredicate) fact).toDataStructure();
				if (item instanceof Triple) {
					insertTripleIndex = insertTripleIndex == null ? new InsertTripleIndex(this.indexScanCreator)
							: insertTripleIndex;
					insertTripleIndex.addTripleFact((Triple) item);
				} else if (item instanceof Predicate) {
					this.predicateIndex = this.predicateIndex == null ? new PredicateIndex()
							: this.predicateIndex;
					this.predicateIndex.addPredicateFact((Predicate) item);
				}
			}
		if (insertTripleIndex != null){
			this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(insertTripleIndex, 0));
		}
		if (this.predicateIndex != null)
			this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(this.predicateIndex, 1));

		// 2. �ber alle Regeln gehen und mitschreiben, welche
		// Tripel-Pr�dikate
		// produziert werden, Externals k�nnen nicht vorkommen
		for (Rule rule : obj.getRules())
			if (rule.isImplication()) {
				for (IExpression expr : rule.getHeadExpressions())
					// Tripel mit variablen Pr�dikat wird generiert
					if (expr instanceof RulePredicate) {
						final RulePredicate pred = (RulePredicate) expr;
						BasicOperator pattern = this.generatePattern(pred, arg);
						KEY key = (pattern instanceof TriplePattern)?
								new KeyTriplePattern((TriplePattern)pattern):
								new KeyPredicatePattern((PredicatePattern)pattern);
						this.tripleProducer.put(key, new LinkedHashSet<BasicOperator>());
					} else if (expr instanceof Equality) {
						this.tripleProducer.put(BuildOperatorGraphRuleVisitor.keyEquality, new LinkedHashSet<BasicOperator>());
						this.usesEqualities = true;
					}
			}

		// 3. Operatorgraphen f�r einzelne Regeln berechnen
		List<BasicOperator> subOperators = new ArrayList<BasicOperator>();
		for (Rule rule : obj.getRules())
			if (rule.isImplication()) {
				BasicOperator result = (BasicOperator) rule.accept(this, arg);
				subOperators.add(result);
			}

		// 4. Rekursive Verbindungen aufl�sen
		for (Entry<KEY, Set<BasicOperator>> entry : this.tripleProducer.entrySet()) {
			boolean consumerExists = false;
			// find all matching consumers by just getting the previous determined tripleConsumers...
			Set<BasicOperator> consumers = this.tripleConsumer.get(entry.getKey());
			if(consumers!=null){
				consumerExists = true;
				// Kreuzverbindungen zwischen Produzenten und Konsumenten
				// herstellen
				for (BasicOperator producer : entry.getValue())
					for (BasicOperator consumer : consumers) {
						producer.addSucceedingOperator(new OperatorIDTuple(consumer, producer.getSucceedingOperators().size()));
						// Sonderfall: Falls PredicatePattern ->
						// Dann: PredicatePattern -> Distinct ->
						if (consumer instanceof PredicatePattern) {
							boolean distinctFound = false;
							for (OperatorIDTuple opid : consumer
									.getSucceedingOperators())
								if (opid.getOperator() instanceof Distinct)
									distinctFound = true;
							if (!distinctFound) {
								final Distinct distinct = new Distinct();
								for(OperatorIDTuple opID: consumer.getSucceedingOperators()){										
									distinct.getSucceedingOperators().add(new OperatorIDTuple(opID));
								}
								consumer.setSucceedingOperator(new OperatorIDTuple(distinct, 0));
							}
						}
					}
			}
			// Wenn keine Konsumenten, dann Produzenten entfernen
			if (!consumerExists) {
				for (BasicOperator producer : entry.getValue())
					if (producer instanceof Generate)
						producer.removeFromOperatorGraph();
			}
		}

		// 5. Ergebniss aller Regeln in einem Result zusammenf�hren
		BasicOperator finalResult = null;
		if (obj.getConclusion() == null) {
			finalResult = new Result();
		} else
			finalResult = patternFromConclusion(obj.getConclusion());

		// Verbindungen zum Endergebniss herstellen
		for (BasicOperator subOperator : subOperators) {
			// Result immer auf linker Seite, damit keine Linksrekursion
			// auftreten kann
			if (!subOperator.getSucceedingOperators().isEmpty()) {
				OperatorIDTuple temp = subOperator.getSucceedingOperators().get(0);
				subOperator.getSucceedingOperators().set(0,new OperatorIDTuple(finalResult, 0));
				subOperator.addSucceedingOperator(temp);
			} else
				subOperator.setSucceedingOperator(new OperatorIDTuple(finalResult, 0));
		}
		if (subOperators.isEmpty()) {
			// IndexCollection verzweist auf EmptyIndex und der direkt auf
			// Result
			if (finalResult instanceof PredicatePattern
					|| finalResult instanceof TriplePattern) {
				finalResult.removeFromOperatorGraph();
				finalResult = null;
			}
			final EmptyIndex empty = new EmptyIndex(finalResult == null ? null
					: new OperatorIDTuple(finalResult, 0),
					new ArrayList<TriplePattern>(), null);
			this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(empty, this.indexScanCreator.getRoot().getSucceedingOperators().size()));
			if (finalResult == null)
				finalResult = empty;
		}

		if (this.booleanIndex != null
				&& this.booleanIndex.getSucceedingOperators().isEmpty())
			this.indexScanCreator.getRoot().removeSucceedingOperator(this.booleanIndex);

		// Falls Conclusion vorhanden, noch Result anh�ngen, zum Sammeln der
		// Ergebnisse
		if (!(finalResult instanceof Result)) {
			if (obj.getConclusion() != null
					&& obj.getConclusion().getVariables().isEmpty()) {
				final BasicOperator mbr = new MakeBooleanResult();
				finalResult.addSucceedingOperator(mbr);
				finalResult = mbr;
			}
			// DEBUG
			//finalResult.addSucceedingOperator(finalResult = new Distinct());
			final Result result = new Result();
			finalResult.addSucceedingOperator(result);
			return result;
		} else
			return finalResult;
	}

	private BasicOperator patternFromConclusion(IExpression conclusion) {
		// Annahme, Conclusion ist Pr�dikat
		final RulePredicate pred = (RulePredicate) conclusion;
		if (pred.isTriple())
			return unitermToTriplePattern(pred);
		else {
			final URILiteral predName = (URILiteral) pred.termName.accept(this,
					null);
			final List<Item> predItems = new ArrayList<Item>();
			for (IExpression expr : pred.termParams) {
				final Item item = (Item) expr.accept(this, null);
				predItems.add(item);
			}
			return new PredicatePattern(predName,
					predItems.toArray(new Item[] {}));
		}
	}

	@Override
	public Object visit(Rule obj, Object arg) throws RIFException {
		// Besimmen, ob Triple, Predicates oder Equalities erstellt werden
		// sollen
		final List<BasicOperator> resultOps = new ArrayList<BasicOperator>();
		final List<Equality> equalities = new ArrayList<Equality>();
		boolean generateTriples = false;
		boolean generatePredicates = false;
		boolean generateEqualities = false;
		for (IExpression expr : obj.getHeadExpressions())
			if (expr instanceof RulePredicate)
				if (((RulePredicate) expr).isTriple())
					generateTriples = true;
				else
					generatePredicates = true;
			else if (expr instanceof Equality) {
				generateEqualities = true;
				equalities.add((Equality) expr);
			}

		// 1. Unteroperatorgraph berechnen
		BasicOperator subOperator = (BasicOperator) obj.getBody().accept(this,
				null);
		subOperator.getSucceedingOperators().clear();

		// 1. Construct erstellen
		if (generateTriples) {
			final Construct construct = new Construct();
			subOperator.addSucceedingOperator(construct);
			List<TriplePattern> patterns = new ArrayList<TriplePattern>();
			for (Uniterm term : obj.getHead().getPredicates()) {
				if (((RulePredicate) term).isTriple()) {
					TriplePattern pattern = unitermToTriplePattern(term);
					patterns.add(pattern);
				}
			}
			construct.setTemplates(patterns);
			// F�r jedes Triplepattern in Construct ein Generate f�r
			// Inferenz
			// erstellen
			// wird, falls es keinen Consumer gibt, sp�ter wieder entfernt
			for (TriplePattern pattern : construct.getTemplates()) {
				Generate generateTriplesOp = new Generate(pattern.getItems());
				generateTriplesOp.addPrecedingOperator(subOperator);
				subOperator.addSucceedingOperator(new OperatorIDTuple(generateTriplesOp, subOperator.getSucceedingOperators().size()));
				// TripleProduzenten registrieren
				add(this.tripleProducer, new KeyTriplePattern(pattern), generateTriplesOp);
			}
			resultOps.add(construct);
		}

		// 2. ConstructPredicate erstellen
		if (generatePredicates) {
			final ConstructPredicate generate = new ConstructPredicate();
			subOperator.addSucceedingOperator(generate);
			for (Uniterm term : obj.getHead().getPredicates()) {
				if (!((RulePredicate) term).isTriple()) {
					final URILiteral name = (URILiteral) term.termName.accept(this, arg);
					final List<Item> params = new ArrayList<Item>();
					for (IExpression expr : term.termParams) {
						final Item item = (Item) expr.accept(this, arg);
						params.add(item);
					}
					Item[] paramsArray = params.toArray(new Item[] {});
					generate.addPattern(name, paramsArray);
					// Produzenten registrieren
					add(this.tripleProducer, new KeyPredicatePattern(new PredicatePattern(name, paramsArray)), generate);
				}
			}
			resultOps.add(generate);
		}

		// 3. ConstructEquality erstellen
		if (generateEqualities) {
			final ConstructEquality constructEq = new ConstructEquality(
					this.equalityMap, equalities.toArray(new Equality[] {}));
			subOperator.addSucceedingOperator(constructEq);
			resultOps.add(constructEq);
			add(this.tripleProducer, BuildOperatorGraphRuleVisitor.keyEquality, constructEq);
		}
		if (resultOps.size() == 1)
			return resultOps.iterator().next();
		else {
			final Union union = new Union();
			for (final BasicOperator op : resultOps)
				union.addSucceedingOperator(op);
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
	public Object visit(Conjunction obj, Object arg) throws RIFException {
		// Vorgehensweise: erstmal alle Sub-Operatoren sammeln -> Danach:
		Set<BasicOperator> operands = new LinkedHashSet<BasicOperator>();
		Set<BasicIndex> indexes = new LinkedHashSet<BasicIndex>();
		List<RuleFilter> predicates = new ArrayList<RuleFilter>();

		for (IExpression expr : obj.exprs) {
			BasicOperator op = (BasicOperator) expr.accept(this, arg);
			if (op instanceof RuleFilter) {
				predicates.add((RuleFilter) op);
				continue;
			}
			operands.add(op);
			if (op instanceof BasicIndex && !(op instanceof IteratorIndex))
				indexes.add((BasicIndex) op);
		}
		// 1. Mergen von Indexen
		BasicIndex mainIndex = null;

		if (indexes.size() > 1) {
			Iterator<BasicIndex> it = indexes.iterator();
			mainIndex = it.next();
			while (it.hasNext()) {
				BasicIndex mergeIndex = it.next();
				mainIndex.getTriplePattern().addAll(mergeIndex.getTriplePattern());
				mergeIndex.getSucceedingOperators().clear();
				mergeIndex.removeFromOperatorGraph();
				operands.remove(mergeIndex);
			}
			mainIndex.setSucceedingOperator((OperatorIDTuple) arg);
		}
		// 2. Joins erstellen
		BasicOperator headOperator = null;
		Iterator<BasicOperator> opIt = operands.iterator();
		if (operands.size() > 1) {
			Join bottomJoin = new Join();
			BasicOperator op1 = opIt.next();
			op1.getSucceedingOperators().clear();
			op1.addSucceedingOperator(new OperatorIDTuple(bottomJoin, 0));
			BasicOperator op2 = opIt.next();
			op2.getSucceedingOperators().clear();
			op2.addSucceedingOperator(new OperatorIDTuple(bottomJoin, 1));
			bottomJoin.setSucceedingOperator((OperatorIDTuple) arg);
			while (opIt.hasNext()) {
				Join tempJoin = new Join();
				BasicOperator operand = opIt.next();
				operand.getSucceedingOperators().clear();
				operand.setSucceedingOperator(new OperatorIDTuple(tempJoin, 0));
				bottomJoin.getSucceedingOperators().clear();
				bottomJoin.setSucceedingOperator(new OperatorIDTuple(tempJoin, 1));
				tempJoin.setSucceedingOperator((OperatorIDTuple) arg);
				bottomJoin = tempJoin;
			}
			headOperator = bottomJoin;
		} else if (operands.size() == 1)
			headOperator = opIt.next();

		// 3. Predicates davorschalten
		if (!predicates.isEmpty()) {
			// 3.1 Predicates sortieren, alle m�glichen Assignments nach vorn
			if (predicates.size() > 1) {
				int i = 0;
				Set<RuleFilter> visited = new LinkedHashSet<RuleFilter>();
				while (i < predicates.size()) {
					if (!predicates.get(i).getExpression()
							.isPossibleAssignment()
							&& !visited.contains(predicates.get(i))) {
						RuleFilter temp = predicates.get(i);
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
				for (RuleFilter filter : predicates)
					headOperator.removeSucceedingOperator(filter);
			} else
				headOperator.getSucceedingOperators().clear();
			// Filter einbauen
			for (BasicOperator pred : predicates) {
				headOperator
						.setSucceedingOperator(new OperatorIDTuple(pred, 0));
				if (headOperator != this.booleanIndex)
					this.booleanIndex.removeSucceedingOperator(pred);
				headOperator = pred;
			}
			headOperator.setSucceedingOperator((OperatorIDTuple) arg);
		}
		return headOperator;
	}

	@Override
	public Object visit(Disjunction obj, Object arg) throws RIFException {
		// Einf�hrung eines Union Operators, der alle Untergeordneten
		// Operatoren
		// zusammenf�hrt
		final Union union = new Union();
		final Distinct distinct = new Distinct();
		distinct.setSucceedingOperator((OperatorIDTuple) arg);
		union.addSucceedingOperator(distinct);
		for (IExpression expr : obj.exprs)
			expr.accept(this, new OperatorIDTuple(union, union
					.getSucceedingOperators().size()));

		return distinct;
	}

	@Override
	public Object visit(ExistExpression obj, Object arg) throws RIFException {
		if (obj.getVariables().isEmpty()) {
			// keine Variablen zum joinen bzw. vereinen -> BooleanResult
			MakeBooleanResult mbr = new MakeBooleanResult();
			mbr.setSucceedingOperator((OperatorIDTuple) arg);
			obj.expr.accept(this, new OperatorIDTuple(mbr, 0));
			return mbr;
		} else {
			// Variablen zum joinen vorhanden
			return obj.expr.accept(this, arg);
		}
	}
	
	public BasicOperator generatePattern(RulePredicate obj, Object arg){
		// Unterscheidung:
		// Wenn Pr�dikat, also kein Tripel
		if (!obj.isTriple()) {
			// PredicatePattern erstellen
			final URILiteral predName = (URILiteral) obj.termName.accept(this, arg);
			final List<Item> predItems = new ArrayList<Item>();
			for (IExpression expr : obj.termParams) {
				final Item item = (Item) expr.accept(this, arg);
				predItems.add(item);
			}
			return new PredicatePattern(predName, predItems.toArray(new Item[] {}));
		}
		return unitermToTriplePattern(obj);
	}

	@Override
	public Object visit(RulePredicate obj, Object arg) throws RIFException {
		BasicOperator zpattern = generatePattern(obj, arg);
		if (zpattern instanceof PredicatePattern) {
			final PredicatePattern predPat = (PredicatePattern) zpattern;
			if (this.predicateIndex == null) {
				this.predicateIndex = new PredicateIndex();
				this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(
						this.predicateIndex, this.indexScanCreator.getRoot()
								.getSucceedingOperators().size()));
			}
			this.predicateIndex.addSucceedingOperator(new OperatorIDTuple(predPat,
					this.predicateIndex.getSucceedingOperators().size()));
			predPat.setSucceedingOperator((OperatorIDTuple) arg);
			// Pr�dikatkonsumenten anmelden
			add(this.tripleConsumer, new KeyPredicatePattern(predPat), predPat);
			return predPat;
		}
		// Normale TripleBearbeitung
		// 1. TriplePattern erstellen
		TriplePattern pattern = (TriplePattern) zpattern;

		// 2. Index erstellen, noch ohne succeding operator		
		BasicOperator index = this.indexScanCreator.createIndexScanAndConnectWithRoot(null, new ArrayList<TriplePattern>(Arrays.asList(pattern)), null);
			index.setPrecedingOperator(this.indexScanCreator.getRoot());

		// 3. Pr�fen ob Triple-Pr�dikat an anderer Stelle erzeugt wird
		KeyTriplePattern keyPattern = new KeyTriplePattern(pattern);
		boolean flag = false;
		
		HashSet<KeyTriplePattern> possibleMatchingKeysOfProducers = new LinkedHashSet<KeyTriplePattern>(); 
		
		for(KEY mainkey: this.tripleProducer.keySet()){
			if(mainkey instanceof KeyTriplePattern){
				KeyTriplePattern mainkeyTP = (KeyTriplePattern) mainkey;
				if(keyPattern.mayConsume(mainkeyTP)){
					possibleMatchingKeysOfProducers.add(mainkeyTP);
					flag = true;
				}
			}
		}
		if (flag) {
			// index -> (union -> distinct) <- triplepattern : return union
			Distinct distinct = new Distinct();
			Union union = new Union();
			union.setSucceedingOperator(new OperatorIDTuple(distinct, 0));
			index.setSucceedingOperator(new OperatorIDTuple(union, 0));
			distinct.setSucceedingOperator((OperatorIDTuple) arg);
			pattern.setSucceedingOperator(new OperatorIDTuple(union, 1));
			for(KeyTriplePattern keyTP: possibleMatchingKeysOfProducers){
				add(this.tripleConsumer, keyTP, pattern);
			}
			return distinct;
		} else {
			index.setSucceedingOperator((OperatorIDTuple) arg);
			return index;
		}
	}
	
	@Override
	public Object visit(External obj, Object arg) throws RIFException {
		// Wenn iterierbar, dann Index erstellen
		// TODO: pr�fen ob Variable nicht anderweitig gebunden, in Validate,
		// dann irgendwie verf�gbar machen das genau hier
		// ein IteratorIndex notwendig ist;
		final URILiteral name = (URILiteral) ((Constant) obj.termName)
				.getLiteral();
		if (RIFBuiltinFactory.isIterable(name)) {
			final IteratorIndex index = new IteratorIndex(obj);
			this.indexScanCreator.getRoot().addSucceedingOperator(index);
			return index;
		} else
			return buildRuleFilter(obj, arg);
	}

	@Override
	protected RuleFilter buildRuleFilter(IExpression expr, Object arg) {
		if (this.booleanIndex == null) {
			this.booleanIndex = new BooleanIndex();			
			this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(this.booleanIndex, 0));
		}
		RuleFilter filter = null;
		if (!this.usesEqualities || !(expr instanceof Equality))
			filter = new RuleFilter(expr, this.equalityMap);
		else {
			filter = new EqualityFilter(expr, this.equalityMap);
			add(this.tripleConsumer, BuildOperatorGraphRuleVisitor.keyEquality, filter);
		}
		this.booleanIndex.addSucceedingOperator(filter);
		filter.setSucceedingOperator((OperatorIDTuple) arg);
		return filter;
	}
}
