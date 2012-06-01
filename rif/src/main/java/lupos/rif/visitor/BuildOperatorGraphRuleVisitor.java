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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
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
import lupos.misc.Tuple;
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
import lupos.rif.model.RuleVariable;
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
	protected final Map<String, Tuple<Set<BasicOperator>, Set<BasicOperator>>> tripleProducerConsumer = new HashMap<String, Tuple<Set<BasicOperator>, Set<BasicOperator>>>();
	protected Multimap<IExpression, IExpression> equalityMap;
	protected boolean usesEqualities = false;

	public BuildOperatorGraphRuleVisitor(final IndexScanCreatorInterface indexScanCreator) {
		super(indexScanCreator);
	}

	/**
	 * 
	 * Is this Document generating Triples with calculated predicate, like (?x
	 * ?y "5")
	 * 
	 * @return
	 */
	protected boolean isVarPred() {
		return this.tripleProducerConsumer.containsKey(this.VARIABLE_PREDICATE);
	}

	@Override
	public Object visit(Document obj, Object arg) throws RIFException {
		this.tripleProducerConsumer.clear();
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
						if (pred.termName instanceof RuleVariable) {
							if (!isVarPred())
								this.tripleProducerConsumer
										.put(this.VARIABLE_PREDICATE,
												new Tuple<Set<BasicOperator>, Set<BasicOperator>>(
														new HashSet<BasicOperator>(),
														new HashSet<BasicOperator>()));
						} else if (!this.tripleProducerConsumer
								.containsKey(pred.termName.toString()))
							this.tripleProducerConsumer
									.put(pred.termName.toString(),
											new Tuple<Set<BasicOperator>, Set<BasicOperator>>(
													new HashSet<BasicOperator>(),
													new HashSet<BasicOperator>()));

					} else if (expr instanceof Equality) {
						this.tripleProducerConsumer
								.put("=",
										new Tuple<Set<BasicOperator>, Set<BasicOperator>>(
												new HashSet<BasicOperator>(),
												new HashSet<BasicOperator>()));
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
		for (Entry<String, Tuple<Set<BasicOperator>, Set<BasicOperator>>> entry : this.tripleProducerConsumer
				.entrySet()) {
			// Wenn keine Konsumenten, dann Produzenten entfernen
			if (entry.getValue().getSecond().isEmpty()) {
				for (BasicOperator producer : entry.getValue().getFirst())
					if (producer instanceof Generate)
						producer.removeFromOperatorGraph();
			} else {
				// Kreuzverbindungen zwischen Produzenten und Konsumenten
				// herstellen
				for (BasicOperator producer : entry.getValue().getFirst())
					for (BasicOperator consumer : entry.getValue().getSecond()) {
						producer.addSucceedingOperator(new OperatorIDTuple(
								consumer, producer.getSucceedingOperators()
										.size()));
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
								distinct.getSucceedingOperators().addAll(
										consumer.getSucceedingOperators());
								consumer.setSucceedingOperator(new OperatorIDTuple(
										distinct, 0));
							}
						}
					}
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
				OperatorIDTuple temp = subOperator.getSucceedingOperators()
						.get(0);
				subOperator.getSucceedingOperators().set(0,
						new OperatorIDTuple(finalResult, 0));
				subOperator.addSucceedingOperator(temp);
			} else
				subOperator.setSucceedingOperator(new OperatorIDTuple(
						finalResult, 0));
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
			this.indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(empty,
					this.indexScanCreator.getRoot().getSucceedingOperators().size()));
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
				subOperator.addSucceedingOperator(new OperatorIDTuple(
						generateTriplesOp, subOperator.getSucceedingOperators()
								.size()));
				// TripleProduzenten registrieren
				this.tripleProducerConsumer
						.get(pattern.getPos(1).isVariable() ? this.VARIABLE_PREDICATE
								: pattern.getPos(1).toString()).getFirst()
						.add(generateTriplesOp);
			}
			resultOps.add(construct);
		}

		// 2. ConstructPredicate erstellen
		if (generatePredicates) {
			final ConstructPredicate generate = new ConstructPredicate();
			subOperator.addSucceedingOperator(generate);
			for (Uniterm term : obj.getHead().getPredicates()) {
				if (!((RulePredicate) term).isTriple()) {
					final URILiteral name = (URILiteral) term.termName.accept(
							this, arg);
					final List<Item> params = new ArrayList<Item>();
					for (IExpression expr : term.termParams) {
						final Item item = (Item) expr.accept(this, arg);
						params.add(item);
					}
					generate.addPattern(name, params.toArray(new Item[] {}));
					// Produzenten registrieren
					this.tripleProducerConsumer.get(name.toString()).getFirst()
							.add(generate);
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
			this.tripleProducerConsumer.get("=").getFirst().add(constructEq);
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

	@Override
	public Object visit(Conjunction obj, Object arg) throws RIFException {
		// Vorgehensweise: erstmal alle Sub-Operatoren sammeln -> Danach:
		Set<BasicOperator> operands = new HashSet<BasicOperator>();
		Set<BasicIndex> indexes = new HashSet<BasicIndex>();
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
				mainIndex.getTriplePattern().addAll(
						mergeIndex.getTriplePattern());
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
				bottomJoin.setSucceedingOperator(new OperatorIDTuple(tempJoin,
						1));
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
				Set<RuleFilter> visited = new HashSet<RuleFilter>();
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

	@Override
	public Object visit(RulePredicate obj, Object arg) throws RIFException {
		// Unterscheidung:
		// Wenn Pr�dikat, also kein Tripel
		if (!obj.isTriple()) {
			// PredicatePattern erstellen
			final URILiteral predName = (URILiteral) obj.termName.accept(this,
					arg);
			final List<Item> predItems = new ArrayList<Item>();
			for (IExpression expr : obj.termParams) {
				final Item item = (Item) expr.accept(this, arg);
				predItems.add(item);
			}
			final PredicatePattern predPat = new PredicatePattern(predName,
					predItems.toArray(new Item[] {}));
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
			if (this.tripleProducerConsumer.containsKey(predName.toString()))
				this.tripleProducerConsumer.get(predName.toString()).getSecond()
						.add(predPat);
			return predPat;
		}
		// Normale TripleBearbeitung
		// 1. TriplePattern erstellen
		TriplePattern pattern = unitermToTriplePattern(obj);

		// 2. Index erstellen, noch ohne succeding operator		
		BasicOperator index = this.indexScanCreator.createIndexScanAndConnectWithRoot(null, new ArrayList<TriplePattern>(Arrays.asList(pattern)), null);
			index.setPrecedingOperator(this.indexScanCreator.getRoot());

		// 3. Pr�fen ob Triple-Pr�dikat an anderer Stelle erzeugt wird
		if (this.tripleProducerConsumer
				.containsKey(pattern.getPos(1).isVariable() ? this.VARIABLE_PREDICATE
						: pattern.getPos(1).toString())) {
			// index -> (union -> distinct) <- triplepattern : return union
			Distinct distinct = new Distinct();
			Union union = new Union();
			union.setSucceedingOperator(new OperatorIDTuple(distinct, 0));
			index.setSucceedingOperator(new OperatorIDTuple(union, 0));
			distinct.setSucceedingOperator((OperatorIDTuple) arg);
			pattern.setSucceedingOperator(new OperatorIDTuple(union, 1));
			this.tripleProducerConsumer
					.get(pattern.getPos(1).isVariable() ? this.VARIABLE_PREDICATE
							: pattern.getPos(1).toString()).getSecond()
					.add(pattern);
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
			this.tripleProducerConsumer.get("=").getSecond().add(filter);
		}
		this.booleanIndex.addSucceedingOperator(filter);
		filter.setSucceedingOperator((OperatorIDTuple) arg);
		return filter;
	}
}
