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
package lupos.optimizations.logical.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.VariableInInferenceRule;
import lupos.datastructures.items.literal.LanguageTaggedLiteralOriginalLanguage;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.TypedLiteralOriginalContent;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.AddBinding;
import lupos.engine.operators.singleinput.filter.Filter;
import lupos.engine.operators.singleinput.filter.expressionevaluation.Helper;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;

/**
 * This class implements following rule to replace constant values in Index
 * operations:
 *
 * index(...?X...)          index(...constant...)
 *   |                        |
 * filter(?X=constant)  =&gt;  addBinding(?X=constant)
 *   |                        |
 *
 * Preconditions: - the filter should contain an expression like ?X=constant
 *
 * @author groppe
 * @version $Id: $Id
 */
public class RuleReplaceConstantOfFilterInTriplePattern extends Rule {

	private Variable var;
	private Variable varInference;
	private Literal constant;
	private TriplePattern triplePattern;

	/**
	 * <p>Constructor for RuleReplaceConstantOfFilterInTriplePattern.</p>
	 */
	public RuleReplaceConstantOfFilterInTriplePattern() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	protected void init() {
		// Define left side of rule
		final Filter a = new Filter();

		this.subGraphMap = new HashMap<BasicOperator, String>();
		this.subGraphMap.put(a, "filter");

		this.startNode = a;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Filter filter = (Filter) mso.get("filter");
		BasicOperator searchTriplePattern = filter;
		while (searchTriplePattern != null
				&& !(searchTriplePattern instanceof TriplePattern)) {
			if (searchTriplePattern.getPrecedingOperators().size() > 1) {
				return false;
			}
			searchTriplePattern = searchTriplePattern.getPrecedingOperators()
			.get(0);
		}
		if (searchTriplePattern == null
				|| !(searchTriplePattern instanceof TriplePattern)) {
			return false;
		}
		if (searchTriplePattern.getSucceedingOperators().size() > 1) {
			return false;
		}
		this.triplePattern = (TriplePattern) searchTriplePattern;
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

					if (!this.triplePattern.getVariables().contains(this.var)
							&& !this.triplePattern.getVariables().contains(
									this.varInference)) {
						// TODO
						// delete triple pattern as it will never have a result!
						System.err
						.println("Can be optimized by extending RuleReplaceConstantOfFilterInTriplePattern: delete triple pattern with succeeding unsatisfiable filter expression!");
						return false;
					}

					if (right instanceof lupos.sparql1_1.ASTQName
							|| right instanceof lupos.sparql1_1.ASTQuotedURIRef
							|| right instanceof lupos.sparql1_1.ASTFloatingPoint
							|| right instanceof lupos.sparql1_1.ASTInteger
							|| right instanceof lupos.sparql1_1.ASTStringLiteral
							|| right instanceof lupos.sparql1_1.ASTDoubleCircumflex) {
						this.constant = LazyLiteral.getLiteral(right);
						// Is it possible to loose the information of the
						// original string representation?
						if (this.constant instanceof TypedLiteralOriginalContent
								|| this.constant instanceof LanguageTaggedLiteralOriginalLanguage) {
							return false;
						} else if (this.constant instanceof TypedLiteral) {
							if (Helper.isNumeric(((TypedLiteral) this.constant)
									.getType())) {
								return false;
							} else {
								return true;
							}
						} else {
							return true;
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
		final BasicOperator aboveFilter = filter.getPrecedingOperators().get(0);

		aboveFilter.setSucceedingOperators(filter.getSucceedingOperators());

		this.triplePattern.replace(this.var, this.constant);
		this.triplePattern.replace(this.varInference, this.constant);

		final AddBinding addBinding = new AddBinding(this.var, this.constant);
		added.add(addBinding);

		addBinding.setSucceedingOperators(this.triplePattern
				.getSucceedingOperators());

		this.triplePattern.setSucceedingOperator(new OperatorIDTuple(addBinding, 0));

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		deleted.add(filter);
		if (deleted.size() > 0 || added.size() > 0) {
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		} else {
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "Constant Propagation";
	}
}
