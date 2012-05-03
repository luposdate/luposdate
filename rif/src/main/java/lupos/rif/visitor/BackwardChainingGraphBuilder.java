package lupos.rif.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.MakeBooleanResult;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rif.IExpression;
import lupos.rif.RIFException;
import lupos.rif.datatypes.Predicate;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RulePredicate;
import lupos.rif.operator.BindableIndex;
import lupos.rif.operator.BindablePredicateIndex;
import lupos.rif.operator.BindableTripleIndex;
import lupos.rif.operator.InitializeDatasetIndex;
import lupos.rif.operator.InsertTripleIndex;
import lupos.rif.operator.PredicateIndex;
import lupos.rif.operator.PredicatePattern;
import lupos.rif.operator.RuleFilter;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;

public class BackwardChainingGraphBuilder extends BaseGraphBuilder {
	private InitializeDatasetIndex datasetIndex;
	
	private final IndexCollection indexCollection;

	public BackwardChainingGraphBuilder(final IndexScanCreatorInterface indexScanCreator, IndexCollection indexCollection) {
		super(indexScanCreator);
		predicateIndex = new PredicateIndex();
		this.indexCollection = indexCollection;
	}

	public Object visit(Document obj, Object arg) throws RIFException {
		// Initialisierungen
		InsertTripleIndex insertTripleIndex = null;
		for (Rule fact : obj.getRules())
			if (!fact.isImplication() && fact.getDeclaredVariables().isEmpty()) {
				final Object item = ((RulePredicate) fact.getHead())
				.toDataStructure();
				if (item instanceof Triple) {
					insertTripleIndex = insertTripleIndex == null ? new InsertTripleIndex(indexScanCreator)
					: insertTripleIndex;
					insertTripleIndex.addTripleFact((Triple) item);
				} else if (item instanceof Predicate) {
					predicateIndex.addPredicateFact((Predicate) item);
				}
			}
		if (insertTripleIndex != null)
			indexCollection.addSucceedingOperator(new OperatorIDTuple(
					insertTripleIndex, 0));
		datasetIndex = new InitializeDatasetIndex(indexCollection);
		indexCollection.addSucceedingOperator(datasetIndex);

		// Conclusion auswerten
		// TODO: erstmal nur ohne Equality, conjunction, disjunction und exists
		PredicateIndex conclusionIndex = null;
		if (obj.getConclusion() instanceof RulePredicate) {
			final RulePredicate predicate = (RulePredicate) obj.getConclusion();
			conclusionIndex = new PredicateIndex();
			indexCollection.addSucceedingOperator(conclusionIndex);
			Predicate toAdd = null;
			if (predicate.isTriple()) {
				final Triple triple = (Triple) predicate.toDataStructure();
				toAdd = new Predicate();
				toAdd.setName(triple.getPredicate());
				toAdd.getParameters().addAll(
						Arrays.asList(triple.getSubject(), triple.getObject()));
			} else
				toAdd = (Predicate) predicate.toDataStructure();
			conclusionIndex.addPredicateFact(toAdd);
		}

		// TODO: nur eine Regel enthalten, welche direkt auf die conclusion
		// passt,
		// sp�ter auswahl der richtigen Regel
		final Result result = new Result();

		BasicOperator subOperator = null;
		for (final Rule rule : obj.getRules())
			if (rule.isImplication()) {
				subOperator = (BasicOperator) obj.getRules().iterator().next().accept(this,
						conclusionIndex);
				break;
			}

		// Keine Regel vorhanden, also m�ssen alle Fakten auf vorhandensein vom
		// Pattern �berpr�ft werden
		if (subOperator == null) {
			subOperator = null;
		}

		subOperator.addSucceedingOperator(result);

		if (datasetIndex.isEmpty())
			indexCollection.removeSucceedingOperator(datasetIndex);

		return result;
	}

	public Object visit(Rule obj, Object arg) throws RIFException {
		BasicOperator headOperator = (BasicOperator) obj.getHead()
		.accept(this, null);
		((BasicOperator) arg).addSucceedingOperator(headOperator);

		// regelk�rper auswerten und unter head h�ngen
		BasicOperator subOperator = (BasicOperator) obj.getBody().accept(this,
				headOperator);

		final MakeBooleanResult mbr = new MakeBooleanResult();
		subOperator.addSucceedingOperator(mbr);

		return mbr;
	}

	public Object visit(Conjunction obj, Object arg) throws RIFException {
		// Vorgehensweise: erstmal alle Sub-Operatoren sammeln -> Danach:
		Set<BasicOperator> operands = new HashSet<BasicOperator>();
		List<RuleFilter> predicates = new ArrayList<RuleFilter>();
		// erst Rulefilter berechnen
		for (IExpression expr : obj.exprs) {
			if (expr instanceof Equality || expr instanceof External) {
				BasicOperator op = (BasicOperator) expr.accept(this, arg);
				predicates.add((RuleFilter) op);
				continue;
			}
		}
		// 3. Predicates davorschalten
		BasicOperator argument = (BasicOperator) arg;
		BasicOperator headOperator = (BasicOperator) arg;
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
			// Filter einbauen
			for (BasicOperator pred : predicates) {
				argument.removeSucceedingOperator(pred);
				headOperator.addSucceedingOperator(pred);
				headOperator = pred;
			}
		}
		// restliche Operatoren berechnen
		for (IExpression expr : obj.exprs) {
			if (!(expr instanceof Equality) && !(expr instanceof External)) {
				BasicOperator op = (BasicOperator) expr.accept(this,
						headOperator);
				operands.add(op);
			}
		}

		// 1. Mergen von Indexen
		// BasicIndex mainIndex = null;
		//
		// if (indexes.size() > 1) {
		// Iterator<BasicIndex> it = indexes.iterator();
		// mainIndex = it.next();
		// while (it.hasNext()) {
		// BasicIndex mergeIndex = it.next();
		// mainIndex.getTriplePattern().addAll(
		// mergeIndex.getTriplePattern());
		// mergeIndex.getSucceedingOperators().clear();
		// mergeIndex.removeFromOperatorGraph();
		// operands.remove(mergeIndex);
		// }
		// mainIndex.setSucceedingOperator((OperatorIDTuple) arg);
		// }

		// 2. Joins erstellen
		Iterator<BasicOperator> opIt = operands.iterator();
		if (operands.size() > 1) {
			Join bottomJoin = new Join();
			BasicOperator op1 = opIt.next();
			// op1.removeFromOperatorGraph();
			// op1.getPrecedingOperators().clear();
			// headOperator.addSucceedingOperator(op1);
			op1.addSucceedingOperator(new OperatorIDTuple(bottomJoin, 0));
			BasicOperator op2 = opIt.next();
			// op2.removeFromOperatorGraph();
			// op2.getPrecedingOperators().clear();
			// headOperator.addSucceedingOperator(op2);
			op2.addSucceedingOperator(new OperatorIDTuple(bottomJoin, 1));
			while (opIt.hasNext()) {
				Join tempJoin = new Join();
				BasicOperator operand = opIt.next();
				// operand.removeFromOperatorGraph();
				// operand.getPrecedingOperators().clear();
				// headOperator.addSucceedingOperator(operand);
				operand.setSucceedingOperator(new OperatorIDTuple(tempJoin, 0));
				bottomJoin.setSucceedingOperator(new OperatorIDTuple(tempJoin,
						1));
				bottomJoin = tempJoin;
			}
			return bottomJoin;
		} else if (operands.size() == 1) {
			final BasicOperator operand = opIt.next();
			// operand.removeFromOperatorGraph();
			// headOperator.addSucceedingOperator(operand);
			return operand;
		} else
			return headOperator;
	}

	public Object visit(ExistExpression obj, Object arg) throws RIFException {
		if (obj.getVariables().isEmpty()) {
			// keine Variablen zum joinen bzw. vereinen -> BooleanResult
			MakeBooleanResult mbr = new MakeBooleanResult();
			BasicOperator subOperator = (BasicOperator) obj.expr.accept(this,
					arg);
			subOperator.addSucceedingOperator(mbr);
			return mbr;
		} else {
			// Variablen zum joinen vorhanden
			return obj.expr.accept(this, arg);
		}
	}

	public Object visit(Disjunction obj, Object arg) throws RIFException {
		// jeden einzelnen zweig mit Union zusammenf�hren
		final Union union = new Union();
		for (final IExpression expr : obj.exprs) {
			final BasicOperator subOperator = (BasicOperator) expr.accept(this,
					arg);
			subOperator.addSucceedingOperator(union);
		}
		return union;
	}

	public Object visit(RulePredicate obj, Object arg) throws RIFException {
		if (obj.isTriple() && arg instanceof BasicOperator) {

			final TriplePattern pattern = unitermToTriplePattern(obj);
			final BasicIndex index = indexCollection.newIndex(null,
					new ArrayList<TriplePattern>(Arrays.asList(pattern)), null);
			final BindableIndex bindIndex = new BindableTripleIndex(index);
			datasetIndex.addBindableIndex(bindIndex);
			((BasicOperator) arg).addSucceedingOperator(bindIndex);
			return bindIndex;

		}
		// Pr�dikat
		final URILiteral name = (URILiteral) obj.termName.accept(this, null);
		final List<Item> items = new ArrayList<Item>();
		for (final IExpression expr : obj.termParams)
			items.add((Item) expr.accept(this, null));
		final PredicatePattern pattern = new PredicatePattern(name,
				items.toArray(new Item[] {}));
		if (arg instanceof BasicOperator) {
			// Regelk�rper, IndexScan erstellen
			final BindableIndex bindIndex = new BindablePredicateIndex(
					predicateIndex, pattern);
			((BasicOperator) arg).addSucceedingOperator(bindIndex);
			return bindIndex;
		} else
			return pattern;
	}

	@Override
	protected RuleFilter buildRuleFilter(IExpression expr, Object arg) {
		final RuleFilter filter = new RuleFilter(expr, null);
		((BasicOperator) arg).addSucceedingOperator(filter);
		return filter;
	}
}
