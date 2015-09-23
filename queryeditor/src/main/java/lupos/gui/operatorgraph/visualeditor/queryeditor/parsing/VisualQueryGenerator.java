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
package lupos.gui.operatorgraph.visualeditor.queryeditor.parsing;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashSet;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Graph;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Join;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Optional;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.QueryRDFTerm;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.TripleContainer;
import lupos.misc.util.OperatorIDTuple;
import lupos.sparql1_1.ASTABSFuncNode;
import lupos.sparql1_1.ASTAVerbType;
import lupos.sparql1_1.ASTAdd;
import lupos.sparql1_1.ASTAggregation;
import lupos.sparql1_1.ASTAll;
import lupos.sparql1_1.ASTArbitraryOccurences;
import lupos.sparql1_1.ASTArbitraryOccurencesNotZero;
import lupos.sparql1_1.ASTBind;
import lupos.sparql1_1.ASTBindings;
import lupos.sparql1_1.ASTBlankNodePropertyList;
import lupos.sparql1_1.ASTCeilFuncNode;
import lupos.sparql1_1.ASTClear;
import lupos.sparql1_1.ASTCoalesceFuncNode;
import lupos.sparql1_1.ASTCollection;
import lupos.sparql1_1.ASTConcatFuncNode;
import lupos.sparql1_1.ASTContainsFuncNode;
import lupos.sparql1_1.ASTCopy;
import lupos.sparql1_1.ASTCreate;
import lupos.sparql1_1.ASTDayFuncNode;
import lupos.sparql1_1.ASTDefault;
import lupos.sparql1_1.ASTDefaultGraph;
import lupos.sparql1_1.ASTDelete;
import lupos.sparql1_1.ASTDrop;
import lupos.sparql1_1.ASTEncodeForUriFuncNode;
import lupos.sparql1_1.ASTExists;
import lupos.sparql1_1.ASTExpressionList;
import lupos.sparql1_1.ASTFilterConstraint;
import lupos.sparql1_1.ASTFloorFuncNode;
import lupos.sparql1_1.ASTGraphConstraint;
import lupos.sparql1_1.ASTGroup;
import lupos.sparql1_1.ASTGroupConstraint;
import lupos.sparql1_1.ASTHaving;
import lupos.sparql1_1.ASTHoursFuncNode;
import lupos.sparql1_1.ASTIfFuncNode;
import lupos.sparql1_1.ASTInNode;
import lupos.sparql1_1.ASTInsert;
import lupos.sparql1_1.ASTInvers;
import lupos.sparql1_1.ASTLcaseFuncNode;
import lupos.sparql1_1.ASTLoad;
import lupos.sparql1_1.ASTMD5FuncNode;
import lupos.sparql1_1.ASTMinus;
import lupos.sparql1_1.ASTMinutesFuncNode;
import lupos.sparql1_1.ASTModify;
import lupos.sparql1_1.ASTMonthFuncNode;
import lupos.sparql1_1.ASTMove;
import lupos.sparql1_1.ASTNIL;
import lupos.sparql1_1.ASTNamed;
import lupos.sparql1_1.ASTNamedGraph;
import lupos.sparql1_1.ASTNegatedPath;
import lupos.sparql1_1.ASTNodeSet;
import lupos.sparql1_1.ASTNotExists;
import lupos.sparql1_1.ASTNotInNode;
import lupos.sparql1_1.ASTNowFuncNode;
import lupos.sparql1_1.ASTObjectList;
import lupos.sparql1_1.ASTOptionalConstraint;
import lupos.sparql1_1.ASTOptionalOccurence;
import lupos.sparql1_1.ASTPathAlternative;
import lupos.sparql1_1.ASTPathSequence;
import lupos.sparql1_1.ASTQName;
import lupos.sparql1_1.ASTQuery;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTRandFuncNode;
import lupos.sparql1_1.ASTRoundFuncNode;
import lupos.sparql1_1.ASTSHA1FuncNode;
import lupos.sparql1_1.ASTSHA256FuncNode;
import lupos.sparql1_1.ASTSHA384FuncNode;
import lupos.sparql1_1.ASTSHA512FuncNode;
import lupos.sparql1_1.ASTSTRUUIDFuncNode;
import lupos.sparql1_1.ASTSecondsFuncNode;
import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.ASTStrAfterFuncNode;
import lupos.sparql1_1.ASTStrBeforeFuncNode;
import lupos.sparql1_1.ASTStrEndsFuncNode;
import lupos.sparql1_1.ASTStrLangFuncNode;
import lupos.sparql1_1.ASTStrReplaceFuncNode;
import lupos.sparql1_1.ASTStrdtFuncNode;
import lupos.sparql1_1.ASTStrlenFuncNode;
import lupos.sparql1_1.ASTStrstartsFuncNode;
import lupos.sparql1_1.ASTSubstringFuncNode;
import lupos.sparql1_1.ASTTimeZoneFuncNode;
import lupos.sparql1_1.ASTTripleSet;
import lupos.sparql1_1.ASTTzFuncNode;
import lupos.sparql1_1.ASTUUIDFuncNode;
import lupos.sparql1_1.ASTUcaseFuncNode;
import lupos.sparql1_1.ASTUndef;
import lupos.sparql1_1.ASTUnionConstraint;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.ASTYearFuncNode;
import lupos.sparql1_1.ASTisBlankFuncNode;
import lupos.sparql1_1.ASTisIRIFuncNode;
import lupos.sparql1_1.ASTisLiteralFuncNode;
import lupos.sparql1_1.ASTisNumericFuncNode;
import lupos.sparql1_1.ASTisURIFuncNode;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SPARQL1_1ParserVisitor;
public class VisualQueryGenerator extends SPARQLCoreParserVisitorImplementation
implements SPARQL1_1ParserVisitor {

	/**
	 * <p>Constructor for VisualQueryGenerator.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 */
	public VisualQueryGenerator(final Prefix prefix) {
		super();

		this.prefix = prefix;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTQuery node, final Object data) {
		final int numberChildren = node.jjtGetNumChildren();
		final String graphConstraint = null;

		Operator testOP = null;

		for (int i = 0; i < numberChildren; i++){
			final Operator testOP2 = (Operator) node.jjtGetChild(i).jjtAccept(
					this, graphConstraint);
			if(testOP==null) {
				testOP = testOP2;
			}
		}

		return testOP;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTGroupConstraint node, final Object data) {
		try {
			int numberUnionOrGraphConstraints = 0;

			final HashMap<Item, QueryRDFTerm> rdfHash = new HashMap<Item, QueryRDFTerm>();
			final LinkedHashSet<Operator> rdfTermToJoin = new LinkedHashSet<Operator>();

			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);

				if (n instanceof ASTTripleSet) {
					final QueryRDFTerm rdft = (QueryRDFTerm) n.jjtAccept(
							this, rdfHash);

					rdfTermToJoin.add(rdft);

				} else if (n instanceof ASTUnionConstraint
						|| n instanceof ASTGraphConstraint) {
					numberUnionOrGraphConstraints++;
				}
			}

			int numberJoinPartner = numberUnionOrGraphConstraints;

			if (rdfTermToJoin.size() > 0) {
				numberJoinPartner++;
			}

			Operator testOP = null;

			if (numberJoinPartner > 1) {
				final Join joinOp = new Join();

				int j = 0;

				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					final Node n = node.jjtGetChild(i);

					if (n instanceof ASTUnionConstraint
							|| n instanceof ASTGraphConstraint) {
						testOP = (Operator) n.jjtAccept(
								this, data);

						if (testOP != null) {
							joinOp.addSucceedingOperator(new OperatorIDTuple<Operator>(
									testOP, j));
						}

						j++;
					}
				}

				if (rdfTermToJoin.size() > 0) {
					final TripleContainer opContainer = new TripleContainer(
							rdfTermToJoin);

					joinOp.addSucceedingOperator(new OperatorIDTuple<Operator>(
							opContainer, j));
				}

				testOP = joinOp;
			} else { // There should be only triple patterns or one
				// ASTUnionConstraint / ASTGraphConstraint
				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					final Node n = node.jjtGetChild(i);

					if (n instanceof ASTTripleSet) {
						final TripleContainer opContainer = new TripleContainer(
								rdfTermToJoin);

						testOP = opContainer;

						break;
					} else if (n instanceof ASTUnionConstraint
							|| n instanceof ASTGraphConstraint) {
						testOP = (Operator) n.jjtAccept(
								this, data);

						break;
					}
				}
			}

			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);

				if (n instanceof ASTOptionalConstraint) {
					final Optional optional = new Optional();

					if (testOP != null) {
						optional.addSucceedingOperator(new OperatorIDTuple<Operator>(
								testOP, 0));
					}

					testOP = (Operator) n.jjtAccept(
							this, null);

					if (testOP != null) {
						optional.addSucceedingOperator(new OperatorIDTuple<Operator>(
								testOP, 1));
					}

					testOP = optional;
				}
			}

			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);

				Operator filterOp = null;

				if (n instanceof ASTFilterConstraint) {
					filterOp = (Operator) n.jjtAccept(
							this, data);

					if (filterOp != null) {
						filterOp.addSucceedingOperator(new OperatorIDTuple<Operator>(
								testOP, 0));

						testOP = filterOp;
					}
				}
			}

			return testOP;
		} catch (final Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTModify node, final Object data) {
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDelete node, final Object data) {
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTInsert node, final Object data) {
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLoad node, final Object data) {
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTClear node, final Object data) {
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCreate node, final Object data) {
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDrop node, final Object data) {
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDefaultGraph node, final Object data) {
		return ((ASTQuotedURIRef) node.jjtGetChild(0)).toQueryString();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNamedGraph node, final Object data) {
		return ((ASTQuotedURIRef) node.jjtGetChild(0)).toQueryString();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTGraphConstraint node, final Object data) {
		final int numberChildren = node.jjtGetNumChildren();
		final String graphConstraint = null;

		Item item=null;

		final Node child = node.jjtGetChild(0);

		if (child instanceof ASTQuotedURIRef) {
			try {
				item = LiteralFactory.createURILiteral(((ASTQuotedURIRef) child).getQRef());
			} catch (final URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		if (child instanceof ASTVar) {
			item = new Variable(((ASTVar) child).getName());
		}


		final Graph g = new Graph(this.prefix, item);

		for (int i = 1; i < numberChildren; i++) {
			final Operator op = (Operator) node.jjtGetChild(i).jjtAccept(
					this, data);

			g.addSucceedingOperator(new OperatorIDTuple<Operator>(op, i - 1));
		}

		return g;
	}


	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAVerbType node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTGroup node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTHaving node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBindings node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNIL node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTUndef node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAdd node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMove node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCopy node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNamed node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDefault node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAll node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTService node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBind node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMinus node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNodeSet node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTObjectList node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTPathAlternative node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTPathSequence node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTInvers node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTArbitraryOccurences node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTOptionalOccurence node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTArbitraryOccurencesNotZero node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNegatedPath node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBlankNodePropertyList node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCollection node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTInNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNotInNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTRandFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTABSFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCeilFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTFloorFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTRoundFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTConcatFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrlenFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTUcaseFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLcaseFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTEncodeForUriFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTContainsFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrstartsFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrEndsFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrBeforeFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrAfterFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTYearFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMonthFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDayFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTHoursFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMinutesFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSecondsFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTTimeZoneFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTTzFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNowFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTUUIDFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSTRUUIDFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMD5FuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSHA1FuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSHA256FuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSHA384FuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSHA512FuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCoalesceFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTIfFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrLangFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrdtFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisIRIFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisURIFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisBlankFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisLiteralFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisNumericFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSubstringFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrReplaceFuncNode node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTExists node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNotExists node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAggregation node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTQName node, final Object data) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTExpressionList node, final Object data) {
		return null;
	}
}
