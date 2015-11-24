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
package lupos.engine.operators.singleinput.filter;

import lupos.sparql1_1.*;

/**
 * This class checks whether or not materialization of lazy literals is needed when applying the (sub-) query...
 *
 * @author groppe
 * @version $Id: $Id
 */
public class MaterializationNeededVisitor implements SPARQL1_1ParserVisitor {

	/**
	 * Checks the children of a given node
	 * @param node the node of which the children are checked
	 * @param data if data is null and variables are used, then materilization is needed, otherwise materilization is not needed (e.g. computations with only constants)
	 * @return true if materialization of lazy literals is needed, otherwise false
	 */
	private boolean checkChildren(final SimpleNode node, final Object data){
		boolean flag = false;
		for(final Node child: node.getChildren()){
			flag |= (Boolean) child.jjtAccept(this, data);
		}
		return flag;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final SimpleNode node, final Object data) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTGroupConstraint node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTTripleSet node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAVerbType node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTQuery node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBaseDecl node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTPrefixDecl node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSelectQuery node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAs node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTConstructQuery node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDescribeQuery node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAskQuery node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDefaultGraph node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNamedGraph node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTGroup node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTHaving node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTOrderConditions node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAscOrder node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDescOrder node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLimit node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTOffset node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBindings node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTPlusNode node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTUndef node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLoad node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTClear node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDrop node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCreate node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAdd node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMove node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCopy node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTInsert node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDelete node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTModify node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDefault node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNamed node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAll node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTConstructTemplate node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTGraphConstraint node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTOptionalConstraint node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTService node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBind node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMinus node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTUnionConstraint node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTFilterConstraint node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTFunctionCall node, final Object data) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTArguments node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTExpressionList node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNodeSet node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTObjectList node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTPathAlternative node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTPathSequence node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTInvers node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTArbitraryOccurences node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTOptionalOccurence node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTArbitraryOccurencesNotZero node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNegatedPath node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTInteger node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBlankNodePropertyList node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCollection node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTVar node, final Object data) {
		if(data!=null){
			return true;
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNIL node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTOrNode node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAndNode node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTEqualsNode node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNotEqualsNode node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLessThanNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTGreaterThanNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLessThanEqualsNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTGreaterThanEqualsNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTInNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNotInNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAdditionNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSubtractionNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMultiplicationNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDivisionNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNotNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMinusNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLangFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLangMatchesFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDataTypeFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBoundFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTIriFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTUriFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBnodeFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTRandFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTABSFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCeilFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTFloorFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTRoundFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTConcatFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrlenFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTUcaseFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLcaseFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTEncodeForUriFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTContainsFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrstartsFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrEndsFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrBeforeFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrAfterFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTYearFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMonthFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDayFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTHoursFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMinutesFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSecondsFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTTimeZoneFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTTzFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNowFuncNode node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTUUIDFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSTRUUIDFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTMD5FuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSHA1FuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSHA256FuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSHA384FuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSHA512FuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTCoalesceFuncNode node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTIfFuncNode node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrLangFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrdtFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSameTermFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisIRIFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisURIFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisBlankFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisLiteralFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTisNumericFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTRegexFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTSubstringFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStrReplaceFuncNode node, final Object data) {
		return this.checkChildren(node, true);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTExists node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTNotExists node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTAggregation node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTRDFLiteral node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTDoubleCircumflex node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTLangTag node, final Object data) {
		return this.checkChildren(node, data);
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTFloatingPoint node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBooleanLiteral node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTStringLiteral node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTQuotedURIRef node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTQName node, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTBlankNode node, final Object data) {
		if(data!=null){
			return true;
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(final ASTEmptyNode node, final Object data) {
		if(data!=null){
			return true;
		} else {
			return false;
		}
	}
}