/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.optimizations.logical.rules.externalontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.VariableInInferenceRule;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.AddBinding;
import lupos.engine.operators.singleinput.filter.Filter;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

/**
 * This class implements following rule to replace constant values in Index
 * operations:
 *
 * index(...?X...) index(...constant...) | | filter(?X=constant) =&gt;
 * addBinding(?X=constant) | |
 *
 * Preconditions: - the filter should contain an expression like ?X=constant
 *
 * @author groppe
 * @version $Id: $Id
 */
public class RuleEliminateUnsatisfiableAddFilterSequence extends Rule {

	private Variable var;
	private Variable varInference;
	private Literal constant;

	/**
	 * <p>Constructor for RuleEliminateUnsatisfiableAddFilterSequence.</p>
	 */
	public RuleEliminateUnsatisfiableAddFilterSequence() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	protected void init() {
		// Define left side of rule
		final AddBinding add = new AddBinding(null, null);
		final Filter filter = new Filter();

		add.setSucceedingOperator(new OperatorIDTuple(filter, -1));
		filter.setPrecedingOperator(add);

		this.subGraphMap = new HashMap<BasicOperator, String>();
		this.subGraphMap.put(filter, "filter");
		this.subGraphMap.put(add, "add");

		this.startNode = filter;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Filter filter = (Filter) mso.get("filter");
		if (filter.getPrecedingOperators().size() > 1) {
			return false;
		}
		lupos.sparql1_1.Node n = filter.getNodePointer();
		if (n.jjtGetNumChildren() > 0) {
			n = n.jjtGetChild(0);
			if (n instanceof lupos.sparql1_1.ASTEqualsNode) {
				lupos.sparql1_1.Node left = n.jjtGetChild(0);
				lupos.sparql1_1.Node right = n.jjtGetChild(1);
				if (right instanceof lupos.sparql1_1.ASTVar) {
					final lupos.sparql1_1.Node tmp = left;
					left = right;
					right = tmp;
				}
				if (left instanceof lupos.sparql1_1.ASTVar) {
					final String varname = ((lupos.sparql1_1.ASTVar) left)
							.getName();
					this.var = new Variable(varname);
					this.varInference = new VariableInInferenceRule(varname);

					if (right instanceof lupos.sparql1_1.ASTQName
							|| right instanceof lupos.sparql1_1.ASTQuotedURIRef
							|| right instanceof lupos.sparql1_1.ASTFloatingPoint
							|| right instanceof lupos.sparql1_1.ASTInteger
							|| right instanceof lupos.sparql1_1.ASTStringLiteral
							|| right instanceof lupos.sparql1_1.ASTDoubleCircumflex) {
						this.constant = LazyLiteral.getLiteral(right);

						final AddBinding add = (AddBinding) mso.get("add");
						if (add.getVar().equals(this.var)
								|| add.getVar().equals(this.varInference)) {
							if (add.getLiteral().equals(this.constant)) {
								return false;
							} else {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Filter filter = (Filter) mso.get("filter");

		for (final BasicOperator parent : filter.getPrecedingOperators()) {
			this.deleteAllAbove(filter, parent, deleted);
		}
		final OperatorIDTuple[] opIDTuples = filter.getSucceedingOperators()
				.toArray(new OperatorIDTuple[0]);
		for (final OperatorIDTuple opIDTuple : opIDTuples) {
			this.deleteAllBelow(filter, opIDTuple.getOperator(), deleted);
		}
		if (!deleted.isEmpty() || !added.isEmpty()) {
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		} else {
			return null;
		}
	}

	/**
	 * <p>deleteAllAbove.</p>
	 *
	 * @param parent a {@link lupos.engine.operators.BasicOperator} object.
	 * @param child a {@link lupos.engine.operators.BasicOperator} object.
	 * @param deleted a {@link java.util.Collection} object.
	 */
	public void deleteAllAbove(final BasicOperator parent,
			final BasicOperator child, final Collection<BasicOperator> deleted) {
		parent.removeSucceedingOperator(child);
		if (!(parent instanceof PatternMatcher)) {

			// log parent as deleted operator
			deleted.add(parent);

			// in addition, log all operators (other than child) below parent, that became unrechable
			if (!parent.getSucceedingOperators().isEmpty()) {
				final OperatorIDTuple[] opIDTuples =
					parent.getSucceedingOperators().toArray(new OperatorIDTuple[0]);
				for (final OperatorIDTuple opIDTuple : opIDTuples) {
					this.logDeletedOperatorsBelow(opIDTuple.getOperator(), deleted);
				}
			}

			for (final BasicOperator parentparent : parent
					.getPrecedingOperators()) {
				this.deleteAllAbove(parentparent, parent, deleted);
			}
		}
	}

	/**
	 * <p>logDeletedOperatorsBelow.</p>
	 *
	 * @param parent a {@link lupos.engine.operators.BasicOperator} object.
	 * @param deleted a {@link java.util.Collection} object.
	 */
	public void logDeletedOperatorsBelow(final BasicOperator parent,
			final Collection<BasicOperator> deleted) {
		if (parent.getPrecedingOperators().size() < 2) {
			deleted.add(parent);
			final OperatorIDTuple[] opIDTuples =
				parent.getSucceedingOperators().toArray(new OperatorIDTuple[0]);
			for (final OperatorIDTuple opIDTuple : opIDTuples) {
				this.logDeletedOperatorsBelow(opIDTuple.getOperator(), deleted);
			}
		}
	}

	/**
	 * <p>deleteAllBelow.</p>
	 *
	 * @param parent a {@link lupos.engine.operators.BasicOperator} object.
	 * @param child a {@link lupos.engine.operators.BasicOperator} object.
	 * @param deleted a {@link java.util.Collection} object.
	 */
	public void deleteAllBelow(final BasicOperator parent,
			final BasicOperator child, final Collection<BasicOperator> deleted) {
		if (child.getPrecedingOperators().size() > 1) {
			parent.removeSucceedingOperator(child);
			child.removePrecedingOperator(parent);
		} else {
			deleted.add(parent);
			final OperatorIDTuple[] opIDTuples = child.getSucceedingOperators()
					.toArray(new OperatorIDTuple[0]);
			for (final OperatorIDTuple opIDTuple : opIDTuples) {
				this.deleteAllBelow(child, opIDTuple.getOperator(), deleted);
			}
		}
	}
}
