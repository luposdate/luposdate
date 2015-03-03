
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.optimizations.sparql2core_sparql;

import lupos.sparql1_1.*;
public interface SPARQL1_1ParserVisitorStringGenerator
{
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.SimpleNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(SimpleNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTGroupConstraint} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTGroupConstraint node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTTripleSet} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTTripleSet node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAVerbType} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAVerbType node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTQuery} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTQuery node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTBaseDecl} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTBaseDecl node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTPrefixDecl} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTPrefixDecl node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSelectQuery} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSelectQuery node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAs} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAs node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTConstructQuery} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTConstructQuery node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDescribeQuery} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDescribeQuery node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAskQuery} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAskQuery node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDefaultGraph} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDefaultGraph node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNamedGraph} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNamedGraph node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTGroup} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTGroup node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTHaving} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTHaving node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTOrderConditions} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTOrderConditions node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAscOrder} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAscOrder node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDescOrder} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDescOrder node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTLimit} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTLimit node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTOffset} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTOffset node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTBindings} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTBindings node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNIL} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNIL node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTPlusNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTPlusNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTUndef} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTUndef node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTLoad} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTLoad node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTClear} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTClear node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDrop} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDrop node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTCreate} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTCreate node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAdd} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAdd node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTMove} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTMove node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTCopy} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTCopy node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTInsert} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTInsert node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDelete} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDelete node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTModify} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTModify node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNamed} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNamed node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDefault} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDefault node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAll} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAll node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTConstructTemplate} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTConstructTemplate node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTGraphConstraint} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTGraphConstraint node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTOptionalConstraint} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTOptionalConstraint node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTService} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTService node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTBind} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTBind node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTMinus} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTMinus node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTUnionConstraint} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTUnionConstraint node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTFilterConstraint} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTFilterConstraint node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTFunctionCall} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTFunctionCall node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTArguments} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTArguments node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTExpressionList} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTExpressionList node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNodeSet} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNodeSet node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTObjectList} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTObjectList node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTPathAlternative} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTPathAlternative node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTPathSequence} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTPathSequence node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTInvers} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTInvers node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTArbitraryOccurences} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTArbitraryOccurences node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTOptionalOccurence} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTOptionalOccurence node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTArbitraryOccurencesNotZero} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTArbitraryOccurencesNotZero node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNegatedPath} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNegatedPath node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTInteger} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTInteger node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTBlankNodePropertyList} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTBlankNodePropertyList node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTCollection} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTCollection node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTVar} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTVar node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTOrNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTOrNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAndNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAndNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTEqualsNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTEqualsNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNotEqualsNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNotEqualsNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTLessThanNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTLessThanNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTLessThanEqualsNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTLessThanEqualsNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTGreaterThanNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTGreaterThanNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTGreaterThanEqualsNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTGreaterThanEqualsNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTInNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTInNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNotInNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNotInNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAdditionNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAdditionNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSubtractionNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSubtractionNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTMultiplicationNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTMultiplicationNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDivisionNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDivisionNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNotNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNotNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTMinusNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTMinusNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStrFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStrFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTLangFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTLangFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTLangMatchesFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTLangMatchesFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDataTypeFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDataTypeFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTBoundFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTBoundFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTIriFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTIriFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTUriFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTUriFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTBnodeFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTBnodeFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTRandFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTRandFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTABSFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTABSFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTCeilFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTCeilFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTFloorFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTFloorFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTRoundFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTRoundFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTConcatFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTConcatFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStrlenFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStrlenFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTUcaseFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTUcaseFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTLcaseFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTLcaseFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTEncodeForUriFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTEncodeForUriFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTContainsFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTContainsFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStrstartsFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStrstartsFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStrEndsFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStrEndsFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStrBeforeFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStrBeforeFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStrAfterFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStrAfterFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTYearFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTYearFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTMonthFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTMonthFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDayFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDayFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTHoursFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTHoursFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTMinutesFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTMinutesFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSecondsFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSecondsFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTTimeZoneFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTTimeZoneFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTTzFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTTzFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNowFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNowFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTUUIDFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTUUIDFuncNode node);  
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSTRUUIDFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSTRUUIDFuncNode node);  
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTMD5FuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTMD5FuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSHA1FuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSHA1FuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSHA256FuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSHA256FuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSHA384FuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSHA384FuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSHA512FuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSHA512FuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTCoalesceFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTCoalesceFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTIfFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTIfFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStrLangFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStrLangFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStrdtFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStrdtFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSameTermFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSameTermFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTisIRIFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTisIRIFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTisURIFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTisURIFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTisBlankFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTisBlankFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTisLiteralFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTisLiteralFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTisNumericFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTisNumericFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTRegexFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTRegexFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTSubstringFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTSubstringFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStrReplaceFuncNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStrReplaceFuncNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTExists} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTExists node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTNotExists} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTNotExists node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTAggregation} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTAggregation node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTRDFLiteral} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTRDFLiteral node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTDoubleCircumflex} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTDoubleCircumflex node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTLangTag} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTLangTag node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTFloatingPoint} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTFloatingPoint node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTBooleanLiteral} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTBooleanLiteral node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStringLiteral} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStringLiteral node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTQuotedURIRef} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTQuotedURIRef node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTQName} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTQName node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTBlankNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTBlankNode node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTEmptyNode} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTEmptyNode node);
  
  // for stream evaluator:
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStream} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStream node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTWindow} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTWindow node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTStart} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTStart node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTEnd} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTEnd node);
  /**
   * <p>visit.</p>
   *
   * @param node a {@link lupos.sparql1_1.ASTType} object.
   * @return a {@link java.lang.String} object.
   */
  public String visit(ASTType node);
}
