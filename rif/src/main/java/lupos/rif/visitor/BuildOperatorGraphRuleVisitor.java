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
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.EmptyIndex;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Construct;
import lupos.engine.operators.singleinput.MakeBooleanResult;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.singleinput.modifiers.distinct.Distinct;
import lupos.engine.operators.singleinput.modifiers.distinct.InMemoryDistinct;
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
import com.google.common.collect.Sets;

public class BuildOperatorGraphRuleVisitor extends BaseGraphBuilder {
	protected final String VARIABLE_PREDICATE = "?";
	/**
	 * String -> Prï¿½dikatbezeichung bsp. cdp:example(...)
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
		return tripleProducerConsumer.containsKey(VARIABLE_PREDICATE);
	}

	public Object visit(Document obj, Object arg) throws RIFException {
		tripleProducerConsumer.clear();
		equalityMap = HashMultimap.create();
		usesEqualities = false;
		// 1. Fakten mï¿½ssen als erstes ausgewertet werden und dann in allen
		// Operatorbï¿½umen berï¿½cksichtigt werden.
		predicateIndex = null;
		InsertTripleIndex insertTripleIndex = null;
		for (IExpression fact : obj.getFacts())
			if (fact instanceof Equality) {
				final Equality eq = (Equality) fact;
				equalityMap.put(eq.leftExpr, eq.rightExpr);
				equalityMap.put(eq.rightExpr, eq.leftExpr);
				usesEqualities = true;
			} else {
				final Object item = ((RulePredicate) fact).toDataStructure();
				if (item instanceof Triple) {
					insertTripleIndex = insertTripleIndex == null ? new InsertTripleIndex(indexScanCreator)
							: insertTripleIndex;
					insertTripleIndex.addTripleFact((Triple) item);
				} else if (item instanceof Predicate) {
					predicateIndex = predicateIndex == null ? new PredicateIndex()
							: predicateIndex;
					predicateIndex.addPredicateFact((Predicate) item);
				}
			}
		if (insertTripleIndex != null){
			indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(insertTripleIndex, 0));
		}
		if (predicateIndex != null)
			indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(predicateIndex, 1));

		// 2. ï¿½ber alle Regeln gehen und mitschreiben, welche
		// Tripel-Prï¿½dikate
		// produziert werden, Externals kï¿½nnen nicht vorkommen
		for (Rule rule : obj.getRules())
			if (rule.isImplication()) {
				for (IExpression expr : rule.getHeadExpressions())
					// Tripel mit variablen Prï¿½dikat wird generiert
					if (expr instanceof RulePredicate) {
						final RulePredicate pred = (RulePredicate) expr;
						if (pred.termName instanceof RuleVariable) {
							if (!isVarPred())
								tripleProducerConsumer
										.put(VARIABLE_PREDICATE,
												new Tuple<Set<BasicOperator>, Set<BasicOperator>>(
														new HashSet<BasicOperator>(),
														new HashSet<BasicOperator>()));
						} else if (!tripleProducerConsumer
								.containsKey(pred.termName.toString()))
							tripleProducerConsumer
									.put(pred.termName.toString(),
											new Tuple<Set<BasicOperator>, Set<BasicOperator>>(
													new HashSet<BasicOperator>(),
													new HashSet<BasicOperator>()));

					} else if (expr instanceof Equality) {
						tripleProducerConsumer
								.put("=",
										new Tuple<Set<BasicOperator>, Set<BasicOperator>>(
												new HashSet<BasicOperator>(),
												new HashSet<BasicOperator>()));
						usesEqualities = true;
					}
			}

		// 3. Operatorgraphen fï¿½r einzelne Regeln berechnen
		List<BasicOperator> subOperators = new ArrayList<BasicOperator>();
		for (Rule rule : obj.getRules())
			if (rule.isImplication()) {
				BasicOperator result = (BasicOperator) rule.accept(this, arg);
				subOperators.add(result);
			}

		// 4. Rekursive Verbindungen auflï¿½sen
		for (Entry<String, Tuple<Set<BasicOperator>, Set<BasicOperator>>> entry : tripleProducerConsumer
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

		// 5. Ergebniss aller Regeln in einem Result zusammenfï¿½hren
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
			indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(empty,
					indexScanCreator.getRoot().getSucceedingOperators().size()));
			if (finalResult == null)
				finalResult = empty;
		}

		if (booleanIndex != null
				&& booleanIndex.getSucceedingOperators().isEmpty())
			indexScanCreator.getRoot().removeSucceedingOperator(booleanIndex);

		// Falls Conclusion vorhanden, noch Result anhŠngen, zum Sammeln der
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
		// Annahme, Conclusion ist PrŠdikat
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
			// Fï¿½r jedes Triplepattern in Construct ein Generate fï¿½r
			// Inferenz
			// erstellen
			// wird, falls es keinen Consumer gibt, spï¿½ter wieder entfernt
			for (TriplePattern pattern : construct.getTemplates()) {
				Generate generateTriplesOp = new Generate(pattern.getItems());
				generateTriplesOp.addPrecedingOperator(subOperator);
				subOperator.addSucceedingOperator(new OperatorIDTuple(
						generateTriplesOp, subOperator.getSucceedingOperators()
								.size()));
				// TripleProduzenten registrieren
				tripleProducerConsumer
						.get(pattern.getPos(1).isVariable() ? VARIABLE_PREDICATE
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
					tripleProducerConsumer.get(name.toString()).getFirst()
							.add(generate);
				}
			}
			resultOps.add(generate);
		}

		// 3. ConstructEquality erstellen
		if (generateEqualities) {
			final ConstructEquality constructEq = new ConstructEquality(
					equalityMap, equalities.toArray(new Equality[] {}));
			subOperator.addSucceedingOperator(constructEq);
			resultOps.add(constructEq);
			tripleProducerConsumer.get("=").getFirst().add(constructEq);
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
			// 3.1 Predicates sortieren, alle mï¿½glichen Assignments nach vorn
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
				headOperator = booleanIndex;
				for (RuleFilter filter : predicates)
					headOperator.removeSucceedingOperator(filter);
			} else
				headOperator.getSucceedingOperators().clear();
			// Filter einbauen
			for (BasicOperator pred : predicates) {
				headOperator
						.setSucceedingOperator(new OperatorIDTuple(pred, 0));
				if (headOperator != booleanIndex)
					booleanIndex.removeSucceedingOperator(pred);
				headOperator = pred;
			}
			headOperator.setSucceedingOperator((OperatorIDTuple) arg);
		}
		return headOperator;
	}

	public Object visit(Disjunction obj, Object arg) throws RIFException {
		// Einfï¿½hrung eines Union Operators, der alle Untergeordneten
		// Operatoren
		// zusammenfï¿½hrt
		final Union union = new Union();
		final Distinct distinct = new Distinct();
		distinct.setSucceedingOperator((OperatorIDTuple) arg);
		union.addSucceedingOperator(distinct);
		for (IExpression expr : obj.exprs)
			expr.accept(this, new OperatorIDTuple(union, union
					.getSucceedingOperators().size()));

		return distinct;
	}

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

	public Object visit(RulePredicate obj, Object arg) throws RIFException {
		// Unterscheidung:
		// Wenn Prï¿½dikat, also kein Tripel
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
			if (predicateIndex == null) {
				predicateIndex = new PredicateIndex();
				indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(
						predicateIndex, indexScanCreator.getRoot()
								.getSucceedingOperators().size()));
			}
			predicateIndex.addSucceedingOperator(new OperatorIDTuple(predPat,
					predicateIndex.getSucceedingOperators().size()));
			predPat.setSucceedingOperator((OperatorIDTuple) arg);
			// Prï¿½dikatkonsumenten anmelden
			if (tripleProducerConsumer.containsKey(predName.toString()))
				tripleProducerConsumer.get(predName.toString()).getSecond()
						.add(predPat);
			return predPat;
		}
		// Normale TripleBearbeitung
		// 1. TriplePattern erstellen
		TriplePattern pattern = unitermToTriplePattern(obj);

		// 2. Index erstellen, noch ohne succeding operator		
		BasicOperator index = indexScanCreator.createIndexScanAndConnectWithRoot(null, new ArrayList<TriplePattern>(Arrays.asList(pattern)), null);
			index.setPrecedingOperator(indexScanCreator.getRoot());

		// 3. Prï¿½fen ob Triple-Prï¿½dikat an anderer Stelle erzeugt wird
		if (tripleProducerConsumer
				.containsKey(pattern.getPos(1).isVariable() ? VARIABLE_PREDICATE
						: pattern.getPos(1).toString())) {
			// index -> (union -> distinct) <- triplepattern : return union
			Distinct distinct = new Distinct();
			Union union = new Union();
			union.setSucceedingOperator(new OperatorIDTuple(distinct, 0));
			index.setSucceedingOperator(new OperatorIDTuple(union, 0));
			distinct.setSucceedingOperator((OperatorIDTuple) arg);
			pattern.setSucceedingOperator(new OperatorIDTuple(union, 1));
			tripleProducerConsumer
					.get(pattern.getPos(1).isVariable() ? VARIABLE_PREDICATE
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
		// TODO: prï¿½fen ob Variable nicht anderweitig gebunden, in Validate,
		// dann irgendwie verfï¿½gbar machen das genau hier
		// ein IteratorIndex notwendig ist;
		final URILiteral name = (URILiteral) ((Constant) obj.termName)
				.getLiteral();
		if (RIFBuiltinFactory.isIterable(name)) {
			final IteratorIndex index = new IteratorIndex(obj);
			indexScanCreator.getRoot().addSucceedingOperator(index);
			return index;
		} else
			return buildRuleFilter(obj, arg);
	}

	protected RuleFilter buildRuleFilter(IExpression expr, Object arg) {
		if (booleanIndex == null) {
			booleanIndex = new BooleanIndex();			
			indexScanCreator.getRoot().addSucceedingOperator(new OperatorIDTuple(booleanIndex, 0));
		}
		RuleFilter filter = null;
		if (!usesEqualities || !(expr instanceof Equality))
			filter = new RuleFilter(expr, equalityMap);
		else {
			filter = new EqualityFilter(expr, equalityMap);
			tripleProducerConsumer.get("=").getSecond().add(filter);
		}
		booleanIndex.addSucceedingOperator(filter);
		filter.setSucceedingOperator((OperatorIDTuple) arg);
		return filter;
	}
}
