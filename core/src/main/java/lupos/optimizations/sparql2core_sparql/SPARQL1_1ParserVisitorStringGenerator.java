/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.optimizations.sparql2core_sparql;

import lupos.sparql1_1.*;

public interface SPARQL1_1ParserVisitorStringGenerator
{
  public String visit(SimpleNode node);
  public String visit(ASTGroupConstraint node);
  public String visit(ASTTripleSet node);
  public String visit(ASTAVerbType node);
  public String visit(ASTQuery node);
  public String visit(ASTBaseDecl node);
  public String visit(ASTPrefixDecl node);
  public String visit(ASTSelectQuery node);
  public String visit(ASTAs node);
  public String visit(ASTConstructQuery node);
  public String visit(ASTDescribeQuery node);
  public String visit(ASTAskQuery node);
  public String visit(ASTDefaultGraph node);
  public String visit(ASTNamedGraph node);
  public String visit(ASTGroup node);
  public String visit(ASTHaving node);
  public String visit(ASTOrderConditions node);
  public String visit(ASTAscOrder node);
  public String visit(ASTDescOrder node);
  public String visit(ASTLimit node);
  public String visit(ASTOffset node);
  public String visit(ASTBindings node);
  public String visit(ASTNIL node);
  public String visit(ASTPlusNode node);
  public String visit(ASTUndef node);
  public String visit(ASTLoad node);
  public String visit(ASTClear node);
  public String visit(ASTDrop node);
  public String visit(ASTCreate node);
  public String visit(ASTAdd node);
  public String visit(ASTMove node);
  public String visit(ASTCopy node);
  public String visit(ASTInsert node);
  public String visit(ASTDelete node);
  public String visit(ASTModify node);
  public String visit(ASTNamed node);
  public String visit(ASTDefault node);
  public String visit(ASTAll node);
  public String visit(ASTConstructTemplate node);
  public String visit(ASTGraphConstraint node);
  public String visit(ASTOptionalConstraint node);
  public String visit(ASTService node);
  public String visit(ASTBind node);
  public String visit(ASTMinus node);
  public String visit(ASTUnionConstraint node);
  public String visit(ASTFilterConstraint node);
  public String visit(ASTFunctionCall node);
  public String visit(ASTArguments node);
  public String visit(ASTExpressionList node);
  public String visit(ASTNodeSet node);
  public String visit(ASTObjectList node);
  public String visit(ASTPathAlternative node);
  public String visit(ASTPathSequence node);
  public String visit(ASTInvers node);
  public String visit(ASTArbitraryOccurences node);
  public String visit(ASTOptionalOccurence node);
  public String visit(ASTArbitraryOccurencesNotZero node);
  public String visit(ASTNegatedPath node);
  public String visit(ASTInteger node);
  public String visit(ASTBlankNodePropertyList node);
  public String visit(ASTCollection node);
  public String visit(ASTVar node);
  public String visit(ASTOrNode node);
  public String visit(ASTAndNode node);
  public String visit(ASTEqualsNode node);
  public String visit(ASTNotEqualsNode node);
  public String visit(ASTLessThanNode node);
  public String visit(ASTLessThanEqualsNode node);
  public String visit(ASTGreaterThanNode node);
  public String visit(ASTGreaterThanEqualsNode node);
  public String visit(ASTInNode node);
  public String visit(ASTNotInNode node);
  public String visit(ASTAdditionNode node);
  public String visit(ASTSubtractionNode node);
  public String visit(ASTMultiplicationNode node);
  public String visit(ASTDivisionNode node);
  public String visit(ASTNotNode node);
  public String visit(ASTMinusNode node);
  public String visit(ASTStrFuncNode node);
  public String visit(ASTLangFuncNode node);
  public String visit(ASTLangMatchesFuncNode node);
  public String visit(ASTDataTypeFuncNode node);
  public String visit(ASTBoundFuncNode node);
  public String visit(ASTIriFuncNode node);
  public String visit(ASTUriFuncNode node);
  public String visit(ASTBnodeFuncNode node);
  public String visit(ASTRandFuncNode node);
  public String visit(ASTABSFuncNode node);
  public String visit(ASTCeilFuncNode node);
  public String visit(ASTFloorFuncNode node);
  public String visit(ASTRoundFuncNode node);
  public String visit(ASTConcatFuncNode node);
  public String visit(ASTStrlenFuncNode node);
  public String visit(ASTUcaseFuncNode node);
  public String visit(ASTLcaseFuncNode node);
  public String visit(ASTEncodeForUriFuncNode node);
  public String visit(ASTContainsFuncNode node);
  public String visit(ASTStrstartsFuncNode node);
  public String visit(ASTStrEndsFuncNode node);
  public String visit(ASTStrBeforeFuncNode node);
  public String visit(ASTStrAfterFuncNode node);
  public String visit(ASTYearFuncNode node);
  public String visit(ASTMonthFuncNode node);
  public String visit(ASTDayFuncNode node);
  public String visit(ASTHoursFuncNode node);
  public String visit(ASTMinutesFuncNode node);
  public String visit(ASTSecondsFuncNode node);
  public String visit(ASTTimeZoneFuncNode node);
  public String visit(ASTTzFuncNode node);
  public String visit(ASTNowFuncNode node);
  public String visit(ASTUUIDFuncNode node);  
  public String visit(ASTSTRUUIDFuncNode node);  
  public String visit(ASTMD5FuncNode node);
  public String visit(ASTSHA1FuncNode node);
  public String visit(ASTSHA256FuncNode node);
  public String visit(ASTSHA384FuncNode node);
  public String visit(ASTSHA512FuncNode node);
  public String visit(ASTCoalesceFuncNode node);
  public String visit(ASTIfFuncNode node);
  public String visit(ASTStrLangFuncNode node);
  public String visit(ASTStrdtFuncNode node);
  public String visit(ASTSameTermFuncNode node);
  public String visit(ASTisIRIFuncNode node);
  public String visit(ASTisURIFuncNode node);
  public String visit(ASTisBlankFuncNode node);
  public String visit(ASTisLiteralFuncNode node);
  public String visit(ASTisNumericFuncNode node);
  public String visit(ASTRegexFuncNode node);
  public String visit(ASTSubstringFuncNode node);
  public String visit(ASTStrReplaceFuncNode node);
  public String visit(ASTExists node);
  public String visit(ASTNotExists node);
  public String visit(ASTAggregation node);
  public String visit(ASTRDFLiteral node);
  public String visit(ASTDoubleCircumflex node);
  public String visit(ASTLangTag node);
  public String visit(ASTFloatingPoint node);
  public String visit(ASTBooleanLiteral node);
  public String visit(ASTStringLiteral node);
  public String visit(ASTQuotedURIRef node);
  public String visit(ASTQName node);
  public String visit(ASTBlankNode node);
  public String visit(ASTEmptyNode node);
  
  // for stream evaluator:
  public String visit(ASTStream node);
  public String visit(ASTWindow node);
  public String visit(ASTStart node);
  public String visit(ASTEnd node);
  public String visit(ASTType node);
}