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
import lupos.sparql1_1.ASTDistinctPath;
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

	public VisualQueryGenerator(final Prefix prefix) {
		super();

		this.prefix = prefix;
	}

	@Override
	public Object visit(final ASTQuery node, final Object data) {
		final int numberChildren = node.jjtGetNumChildren();
		final String graphConstraint = null;

		Operator testOP = null;

		for (int i = 0; i < numberChildren; i++){
			Operator testOP2 = (Operator) node.jjtGetChild(i).jjtAccept(
					(SPARQL1_1ParserVisitor) this, graphConstraint);
			if(testOP==null)
				testOP = testOP2;
		}

		return testOP;
	}

	public Object visit(final ASTGroupConstraint node, final Object data) {
		try {
			int numberUnionOrGraphConstraints = 0;

			final HashMap<Item, QueryRDFTerm> rdfHash = new HashMap<Item, QueryRDFTerm>();
			final LinkedHashSet<Operator> rdfTermToJoin = new LinkedHashSet<Operator>();

			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);

				if (n instanceof ASTTripleSet) {
					final QueryRDFTerm rdft = (QueryRDFTerm) n.jjtAccept(
							(SPARQL1_1ParserVisitor) this, rdfHash);

					rdfTermToJoin.add(rdft);

				} else if (n instanceof ASTUnionConstraint
						|| n instanceof ASTGraphConstraint)
					numberUnionOrGraphConstraints++;
			}

			int numberJoinPartner = numberUnionOrGraphConstraints;

			if (rdfTermToJoin.size() > 0)
				numberJoinPartner++;

			Operator testOP = null;

			if (numberJoinPartner > 1) {
				final Join joinOp = new Join();

				int j = 0;

				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					final Node n = node.jjtGetChild(i);

					if (n instanceof ASTUnionConstraint
							|| n instanceof ASTGraphConstraint) {
						testOP = (Operator) n.jjtAccept(
								(SPARQL1_1ParserVisitor) this, data);

						if (testOP != null)
							joinOp.addSucceedingOperator(new OperatorIDTuple<Operator>(
									testOP, j));

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
								(SPARQL1_1ParserVisitor) this, data);

						break;
					}
				}
			}

			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);

				if (n instanceof ASTOptionalConstraint) {
					final Optional optional = new Optional();

					if (testOP != null)
						optional.addSucceedingOperator(new OperatorIDTuple<Operator>(
								testOP, 0));

					testOP = (Operator) n.jjtAccept(
							(SPARQL1_1ParserVisitor) this, null);

					if (testOP != null)
						optional.addSucceedingOperator(new OperatorIDTuple<Operator>(
								testOP, 1));

					testOP = optional;
				}
			}

			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);

				Operator filterOp = null;

				if (n instanceof ASTFilterConstraint) {
					filterOp = (Operator) n.jjtAccept(
							(SPARQL1_1ParserVisitor) this, data);

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

	public Object visit(final ASTModify node, final Object data) {
		// endOperandID = 0;
		// endOperator = q;
		//
		// Collection<URILiteral> cu = new LinkedList<URILiteral>();
		//
		// for(int i = 0; i < node.jjtGetNumChildren(); i++) {
		// if(node.jjtGetChild(i) instanceof ASTQuotedURIRef) {
		// try {
		// cu.add(LiteralFactory.createURILiteral("<" + (String)
		// (node.jjtGetChild(i)).jjtAccept((SPARQLULCoreParserVisitor) this,
		// data) + ">"));
		// } catch (Exception e) {
		// log.error(e);
		// }
		// }
		// }
		//
		// Modify m = new Modify(cu);
		// m.setSucceedingOperator(new OperatorIDTuple(endOperator,
		// endOperandID));
		//
		// endOperator = m;
		// endOperandID = 0;
		//
		// data = node.jjtGetChild(node.jjtGetNumChildren() -
		// 2).jjtAccept((SPARQLULCoreParserVisitor) this, data);
		//
		// List<OperatorIDTuple> succeedingOperators = new
		// LinkedList<OperatorIDTuple>();
		// succeedingOperators.add(new OperatorIDTuple(endOperator,
		// endOperandID));
		//
		// endOperator = m;
		// endOperandID = 1;
		//
		// data = node.jjtGetChild(node.jjtGetNumChildren() -
		// 1).jjtAccept((SPARQLULCoreParserVisitor) this, data);
		//
		// succeedingOperators.add(new OperatorIDTuple(endOperator,
		// endOperandID));
		//
		// SeveralSucceedingOperators sso = new SeveralSucceedingOperators();
		// sso.setSucceedingOperators(succeedingOperators);
		//
		// endOperator = sso;
		// endOperandID = 0;
		//
		// InsertEmptyIndex(node);

		return data;
	}

	public Object visit(final ASTDelete node, final Object data) {
		// endOperandID = 0;
		// endOperator = q;
		//
		// Collection<URILiteral> cu = new LinkedList<URILiteral>();
		//
		// for(int i = 0; i < node.jjtGetNumChildren(); i++) {
		// if(node.jjtGetChild(i) instanceof ASTQuotedURIRef) {
		// try {
		// cu.add(LiteralFactory.createURILiteral("<" + (String)
		// (node.jjtGetChild(i)).jjtAccept((SPARQLULCoreParserVisitor) this,
		// data) + ">"));
		// } catch (Exception e) {
		// log.error(e);
		// }
		// }
		// }
		//
		// Delete d = new Delete(cu);
		// d.setSucceedingOperator(new OperatorIDTuple(endOperator,
		// endOperandID));
		//
		// endOperator = d;
		// endOperandID = 0;
		//
		// data = node.jjtGetChild(node.jjtGetNumChildren() -
		// 1).jjtAccept((SPARQLULCoreParserVisitor) this, data);
		//
		// InsertEmptyIndex(node);

		return data;
	}

	public Object visit(final ASTInsert node, final Object data) {
		// endOperandID = 0;
		// endOperator = q;
		//
		// Collection<URILiteral> cu = new LinkedList<URILiteral>();
		//
		// for(int i = 0; i < node.jjtGetNumChildren(); i++) {
		// if(node.jjtGetChild(i) instanceof ASTQuotedURIRef) {
		// try {
		// cu.add(LiteralFactory.createURILiteral("<" + (String)
		// (node.jjtGetChild(i)).jjtAccept((SPARQLULCoreParserVisitor) this,
		// data) + ">"));
		// } catch (Exception e) {
		// log.error(e);
		// }
		// }
		// }
		//
		// Insert insert = new Insert(cu);
		// insert.setSucceedingOperator(new OperatorIDTuple(endOperator,
		// endOperandID));
		//
		// endOperator = insert;
		// endOperandID = 0;
		//
		// data = node.jjtGetChild(node.jjtGetNumChildren() -
		// 1).jjtAccept((SPARQLULCoreParserVisitor) this, data);
		//
		// InsertEmptyIndex(node);

		return data;
	}

	// public void InsertEmptyIndex(SimpleNode node) {
	// Node parent = node.jjtGetParent();
	// int i;
	//
	// for(i = 0; i < parent.jjtGetNumChildren(); i++)
	// if(node.equals(parent.jjtGetChild(i)))
	// break;
	//
	// // if(i >= parent.jjtGetNumChildren() - 1)
	// // root.addSucceedingOperator(new OperatorIDTuple(new
	// EmptyIndex(new OperatorIDTuple(endOperator, endOperandID), null), 0));
	// }

	public Object visit(final ASTLoad node, final Object data) {
		// endOperandID = 0;
		// endOperator = q;
		//
		// Collection<URILiteral> cu = new LinkedList<URILiteral>();
		//
		// for(int i = 0; i < node.jjtGetNumChildren(); i++) {
		// if(node.jjtGetChild(i) instanceof ASTQuotedURIRef) {
		// try {
		// cu.add(LiteralFactory.createURILiteral("<" + (String)
		// (node.jjtGetChild(i)).jjtAccept((SPARQLULCoreParserVisitor) this,
		// data) + ">"));
		// } catch (Exception e) {
		// log.error(e);
		// }
		// }
		// }
		//
		// URILiteral into = null;
		//
		// if(node.jjtGetChild(node.jjtGetNumChildren() - 1) instanceof ASTINTO)
		// {
		// try {
		// into = LiteralFactory.createURILiteral("<" + (String)
		// node.jjtGetChild(node.jjtGetNumChildren() -
		// 1).jjtAccept((SPARQLULCoreParserVisitor) this, data) + ">");
		// } catch (Exception e) {
		// log.error(e);
		// }
		// }
		//
		// Load load = new Load(cu, into);
		// load.setSucceedingOperator(new OperatorIDTuple(endOperator,
		// endOperandID));
		//
		// endOperator = load;
		// endOperandID = 0;
		//
		// InsertEmptyIndex(node);

		return data;
	}

	public Object visit(final ASTClear node, final Object data) {
		// endOperandID = 0;
		// endOperator = q;
		//
		// Clear c = new Clear();
		// if(node.jjtGetNumChildren() > 0) {
		// try {
		// c.setURI(LiteralFactory.createURILiteral("<" + (String)
		// node.jjtGetChild(0).jjtAccept((SPARQLULCoreParserVisitor) this, data)
		// + ">"));
		// } catch (Exception e) {
		// log.error(e);
		// }
		// }
		// else
		// c.setURI(null);
		//
		// c.setSucceedingOperator(new OperatorIDTuple(endOperator,
		// endOperandID));
		//
		// endOperator = c;
		// endOperandID = 0;
		//
		// InsertEmptyIndex(node);

		return data;
	}

	public Object visit(final ASTCreate node, final Object data) {
		// endOperandID = 0;
		// endOperator = q;
		//
		// Create c = new Create();
		//
		// try {
		// c.setURI(LiteralFactory.createURILiteral("<" + (String)
		// node.jjtGetChild(0).jjtAccept((SPARQLULCoreParserVisitor) this, data)
		// + ">"));
		// } catch (Exception e) {
		// log.error(e);
		// }
		//
		// c.setSilent(node.getSilent());
		// c.setSucceedingOperator(new OperatorIDTuple(endOperator,
		// endOperandID));
		//
		// endOperator = c;
		// endOperandID = 0;
		//
		// InsertEmptyIndex(node);

		return data;
	}

	public Object visit(final ASTDrop node, final Object data) {
		// endOperandID = 0;
		// endOperator = q;
		//
		// Drop d = new Drop();
		//
		// try {
		// d.setURI(LiteralFactory.createURILiteral("<" + (String)
		// node.jjtGetChild(0).jjtAccept((SPARQLULCoreParserVisitor) this, data)
		// + ">"));
		// } catch (Exception e) {
		// log.error(e);
		// }
		//
		// d.setSilent(node.getSilent());
		// d.setSucceedingOperator(new OperatorIDTuple(endOperator,
		// endOperandID));
		//
		// endOperator = d;
		// endOperandID = 0;
		//
		// InsertEmptyIndex(node);

		return data;
	}

	public Object visit(final ASTDefaultGraph node, final Object data) {
		return ((ASTQuotedURIRef) node.jjtGetChild(0)).toQueryString();
	}

	public Object visit(final ASTNamedGraph node, final Object data) {
		return ((ASTQuotedURIRef) node.jjtGetChild(0)).toQueryString();
	}

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
					(SPARQL1_1ParserVisitor) this, data);

			g.addSucceedingOperator(new OperatorIDTuple<Operator>(op, i - 1));
		}

		return g;
	}


	@Override
	public Object visit(ASTAVerbType node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTGroup node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTHaving node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTBindings node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTNIL node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTUndef node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTAdd node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTMove node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTCopy node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTNamed node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTDefault node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTAll node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTService node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTBind node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTMinus node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTNodeSet node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTObjectList node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTPathAlternative node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTPathSequence node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTInvers node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTArbitraryOccurences node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTOptionalOccurence node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTArbitraryOccurencesNotZero node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTDistinctPath node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTNegatedPath node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTBlankNodePropertyList node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTCollection node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTInNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTNotInNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTRandFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTABSFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTCeilFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTFloorFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTRoundFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTConcatFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTStrlenFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTUcaseFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTLcaseFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTEncodeForUriFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTContainsFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTStrstartsFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTStrEndsFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTStrBeforeFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTStrAfterFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTYearFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTMonthFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTDayFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTHoursFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTMinutesFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTSecondsFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTTimeZoneFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTTzFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTNowFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTUUIDFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTSTRUUIDFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTMD5FuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTSHA1FuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTSHA256FuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTSHA384FuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTSHA512FuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTCoalesceFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTIfFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTStrLangFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTStrdtFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTisIRIFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTisURIFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTisBlankFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTisLiteralFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTisNumericFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTSubstringFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTStrReplaceFuncNode node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTExists node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTNotExists node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTAggregation node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTQName node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ASTExpressionList node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}
}
