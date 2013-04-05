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
import java.util.List;
import java.util.Set;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
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
import lupos.rif.operator.BindableIndexScan;
import lupos.rif.operator.BindablePredicateIndexScan;
import lupos.rif.operator.BindableTripleIndexScan;
import lupos.rif.operator.InitializeDatasetIndexScan;
import lupos.rif.operator.InsertTripleIndexScan;
import lupos.rif.operator.PredicateIndexScan;
import lupos.rif.operator.PredicatePattern;
import lupos.rif.operator.RuleFilter;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;

public class BackwardChainingGraphBuilder extends BaseGraphBuilder {
	private InitializeDatasetIndexScan datasetIndex;
	
	private final Root root;

	public BackwardChainingGraphBuilder(final IndexScanCreatorInterface indexScanCreator, Root root) {
		super(indexScanCreator);
		predicateIndex = new PredicateIndexScan();
		this.root = root;
	}

	public Object visit(Document obj, Object arg) throws RIFException {
		// Initialisierungen
		InsertTripleIndexScan insertTripleIndex = null;
		for (Rule fact : obj.getRules())
			if (!fact.isImplication() && fact.getDeclaredVariables().isEmpty()) {
				final Object item = ((RulePredicate) fact.getHead())
				.toDataStructure();
				if (item instanceof Triple) {
					insertTripleIndex = insertTripleIndex == null ? new InsertTripleIndexScan(indexScanCreator)
					: insertTripleIndex;
					insertTripleIndex.addTripleFact((Triple) item);
				} else if (item instanceof Predicate) {
					predicateIndex.addPredicateFact((Predicate) item);
				}
			}
		if (insertTripleIndex != null)
			root.addSucceedingOperator(new OperatorIDTuple(
					insertTripleIndex, 0));
		datasetIndex = new InitializeDatasetIndexScan(root);
		root.addSucceedingOperator(datasetIndex);

		// Conclusion auswerten
		// TODO: erstmal nur ohne Equality, conjunction, disjunction und exists
		PredicateIndexScan conclusionIndex = null;
		if (obj.getConclusion() instanceof RulePredicate) {
			final RulePredicate predicate = (RulePredicate) obj.getConclusion();
			conclusionIndex = new PredicateIndexScan();
			root.addSucceedingOperator(conclusionIndex);
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
			root.removeSucceedingOperator(datasetIndex);

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
			final BasicIndexScan index = root.newIndexScan(null,
					new ArrayList<TriplePattern>(Arrays.asList(pattern)), null);
			final BindableIndexScan bindIndex = new BindableTripleIndexScan(index);
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
			final BindableIndexScan bindIndex = new BindablePredicateIndexScan(
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
