package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.rdfs.index.RDFSPutIntoIndices;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
import lupos.optimizations.logical.rules.RuleEngine;

public class RuleDeleteNotConnectedToResultOperator extends Rule {

	@Override
	protected void init() {
		final BasicOperator bo = new TriplePattern();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(bo, "bo");

		startNode = bo;
	}

	public Rule[] getRulesToApply(final RuleEngine ruleEngine) {
		return new Rule[] {};
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final BasicOperator bo = mso.get("bo");
		final FindResultOperatorVisitor frov = new FindResultOperatorVisitor();
		final Object o = bo.visit(frov);
		return !frov.found();
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final BasicOperator bo = mso.get("bo");
		for (final BasicOperator pred : bo.getPrecedingOperators()) {
			pred.removeSucceedingOperator(bo);
		}
		bo.visit(new SimpleOperatorGraphVisitor() {
			public Object visit(final BasicOperator basicOperator) {
				deleted.add(basicOperator);
				return null;
			}
		});
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	private class FindResultOperatorVisitor implements
			SimpleOperatorGraphVisitor {

		private boolean found = false;

		public Object visit(final BasicOperator basicOperator) {
			if (basicOperator instanceof Result
					|| basicOperator instanceof RDFSPutIntoIndices)
				found = true;
			return null;
		}

		public boolean found() {
			return found;
		}

	}
}
