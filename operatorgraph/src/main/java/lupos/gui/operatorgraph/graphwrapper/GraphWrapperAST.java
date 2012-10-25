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
package lupos.gui.operatorgraph.graphwrapper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Hashtable;
import java.util.LinkedList;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.DrawObject;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.viewer.ElementPanel;
import lupos.sparql1_1.*;
import xpref.datatypes.BooleanDatatype;

public class GraphWrapperAST extends GraphWrapper {
	
	public final static Class<?>[] queryHeads = new Class<?>[]{ASTSelectQuery.class, ASTAskQuery.class, ASTConstructQuery.class, ASTDescribeQuery.class, ASTQuery.class, ASTLoad.class, ASTClear.class, ASTDrop.class, ASTCreate.class, ASTAdd.class, ASTMove.class, ASTCopy.class, ASTInsert.class, ASTDelete.class, ASTModify.class };
	public final static Class<?>[] operatorNodes = new Class<?>[]{ASTAscOrder.class, ASTDescOrder.class, ASTOrNode.class, ASTAndNode.class, ASTEqualsNode.class, ASTNotEqualsNode.class, ASTLessThanNode.class, ASTLessThanEqualsNode.class, ASTGreaterThanNode.class, ASTGreaterThanEqualsNode.class, ASTInNode.class, ASTNotInNode.class, ASTAdditionNode.class, ASTSubtractionNode.class, ASTMultiplicationNode.class, ASTDivisionNode.class, ASTNotNode.class, ASTPlusNode.class, ASTMinusNode.class,ASTPathAlternative.class, ASTPathSequence.class, ASTInvers.class, ASTArbitraryOccurences.class, ASTOptionalOccurence.class, ASTArbitraryOccurencesNotZero.class, ASTDistinctPath.class, ASTNegatedPath.class, ASTNamed.class, ASTDefault.class, ASTAll.class }; 
	public final static Class<?>[] functionNodes = new Class<?>[]{ASTStrFuncNode.class, ASTLangFuncNode.class, ASTLangMatchesFuncNode.class, ASTDataTypeFuncNode.class, ASTBoundFuncNode.class, ASTIriFuncNode.class, ASTUriFuncNode.class, ASTBnodeFuncNode.class, ASTRandFuncNode.class, ASTABSFuncNode.class, ASTCeilFuncNode.class, ASTFloorFuncNode.class, ASTRoundFuncNode.class, ASTConcatFuncNode.class, ASTStrlenFuncNode.class, ASTUcaseFuncNode.class, ASTLcaseFuncNode.class, ASTEncodeForUriFuncNode.class, ASTContainsFuncNode.class, ASTStrstartsFuncNode.class, ASTStrEndsFuncNode.class, ASTStrBeforeFuncNode.class, ASTStrAfterFuncNode.class, ASTYearFuncNode.class, ASTMonthFuncNode.class, ASTDayFuncNode.class, ASTHoursFuncNode.class, ASTMinutesFuncNode.class, ASTSecondsFuncNode.class, ASTTimeZoneFuncNode.class, ASTTzFuncNode.class, ASTNowFuncNode.class, ASTUUIDFuncNode.class, ASTSTRUUIDFuncNode.class, ASTMD5FuncNode.class, ASTSHA1FuncNode.class, ASTSHA256FuncNode.class, ASTSHA384FuncNode.class, ASTSHA512FuncNode.class, ASTCoalesceFuncNode.class, ASTIfFuncNode.class, ASTStrLangFuncNode.class, ASTStrdtFuncNode.class, ASTSameTermFuncNode.class, ASTisIRIFuncNode.class, ASTisURIFuncNode.class, ASTisBlankFuncNode.class, ASTisLiteralFuncNode.class, ASTisNumericFuncNode.class, ASTRegexFuncNode.class, ASTSubstringFuncNode.class, ASTStrReplaceFuncNode.class, ASTFunctionCall.class, ASTArguments.class }; 
	public final static Class<?>[] highLevelOperators = new Class<?>[]{ASTBaseDecl.class, ASTPrefixDecl.class, ASTAs.class, ASTDefaultGraph.class, ASTNamedGraph.class, ASTHaving.class, ASTLimit.class, ASTOffset.class, ASTBindings.class, ASTExists.class, ASTNotExists.class, ASTAggregation.class, ASTService.class, ASTBind.class, ASTMinus.class, ASTUnionConstraint.class, ASTFilterConstraint.class, ASTConstructTemplate.class, ASTOptionalConstraint.class, ASTGraphConstraint.class }; 
	public final static Class<?>[] terminalNodes = new Class<?>[]{ASTAVerbType.class, ASTRDFLiteral.class, ASTDoubleCircumflex.class, ASTLangTag.class, ASTFloatingPoint.class, ASTBooleanLiteral.class, ASTStringLiteral.class, ASTQuotedURIRef.class, ASTQName.class, ASTBlankNode.class, ASTEmptyNode.class, ASTInteger.class, ASTVar.class, ASTNIL.class, ASTUndef.class }; 
	public final static Class<?>[] nonTerminalNodes = new Class<?>[]{ASTGroup.class, ASTOrderConditions.class, ASTBlankNodePropertyList.class, ASTCollection.class, ASTNodeSet.class, ASTObjectList.class, ASTGroupConstraint.class }; 
	
	public static boolean isContained(Class<?> nodeClass, Class<?>[] classes){
		for(Class<?> classToCheck: classes){
			if(classToCheck.equals(nodeClass))
				return true;
		}
		return false;
	}
		
	public static boolean isQueryHead(Class<?> nodeClass){
		return isContained(nodeClass, queryHeads);
	}
	public static boolean isOperatorNode(Class<?> nodeClass){
		return isContained(nodeClass, operatorNodes);
	}
	public static boolean isFunctionNode(Class<?> nodeClass){
		return isContained(nodeClass, functionNodes);
	}
	public static boolean isHighLevelOperator(Class<?> nodeClass){
		return isContained(nodeClass, highLevelOperators);
	}
	public static boolean isTerminalNode(Class<?> nodeClass){
		return isContained(nodeClass, terminalNodes);
	}
	public static boolean isNonTerminalNode(Class<?> nodeClass){
		return isContained(nodeClass, nonTerminalNodes);
	}
	
	public GraphWrapperAST(final Node element) {
		super(element);
	}

	@Override
	public AbstractSuperGuiComponent createObject(final OperatorGraph parent) {
		return new ElementPanel(parent, this);
	}

	@Override
	public void drawAnnotationsBackground(final Graphics2D g2d,
			final Dimension size) {
	}

	@Override
	public void drawBackground(final Graphics2D g2d, final Dimension size) {
		try {
			if (!BooleanDatatype.getValues("ast_useStyledBoxes").get(0)
					.booleanValue()) {
				DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width - 1,
						size.height - 1, Color.WHITE, Color.BLACK);
			} else {
				DrawObject drawObject = null;

				if (isQueryHead(this.element.getClass())) {
					drawObject = this
							.getOperatorStyle("ast_style_queryheadnode");
				} else if (isOperatorNode(this.element.getClass())) {
					drawObject = this
							.getOperatorStyle("ast_style_operatornode");
				} else if (this.element instanceof ASTTripleSet) {
					drawObject = this
							.getOperatorStyle("ast_style_triplepatternnode");
				} else if (isFunctionNode(this.element.getClass())) {
					drawObject = this
							.getOperatorStyle("ast_style_functionnode");
				} else if (isHighLevelOperator(this.element.getClass())) {
					drawObject = this
							.getOperatorStyle("ast_style_highleveloperatornode");
				} else if (isTerminalNode(this.element.getClass())) {
					drawObject = this
							.getOperatorStyle("ast_style_terminalnode");
				} else if (isNonTerminalNode(this.element.getClass())) {
					drawObject = this
							.getOperatorStyle("ast_style_nonterminalnode");
				}

				if (drawObject != null) {
					drawObject.draw(g2d, 0, 0, size.width, size.height);
				} else {
					DrawObject.drawGradientPaintRoundBox(g2d, 0, 0, size.width,
							size.height, Color.LIGHT_GRAY, Color.WHITE);
				}
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	@Override
	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawLineAnnotations(
			final OperatorGraph parent) {
		return new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();
	}

	@Override
	public LinkedList<GraphWrapper> getContainerElements() {
		return new LinkedList<GraphWrapper>();
	}

	@Override
	public LinkedList<GraphWrapper> getPrecedingElements() {
		final LinkedList<GraphWrapper> precedingElements = new LinkedList<GraphWrapper>();

		if (((Node) this.element).jjtGetParent() != null) {
			precedingElements.add(new GraphWrapperAST(((Node) this.element)
					.jjtGetParent()));
		}

		return precedingElements;
	}

	@Override
	public LinkedList<GraphWrapperIDTuple> getSucceedingElements() {
		final Node[] children = ((Node) this.element).getChildren();

		final LinkedList<GraphWrapperIDTuple> succedingElements = new LinkedList<GraphWrapperIDTuple>();

		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				succedingElements.add(new GraphWrapperIDTuple(
						new GraphWrapperAST(children[i]), i));
			}
		}

		return succedingElements;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public StringBuffer serializeObjectAndTree() {
		return new StringBuffer();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		return this.toString();
	}

	@Override
	public Node getElement() {
		return (Node) this.element;
	}

	@Override
	public boolean usePrefixesActive() {
		return false;
	}

	@Override
	public String getWantedPreferencesID() {
		return "ast_useStyledBoxes";
	}
}