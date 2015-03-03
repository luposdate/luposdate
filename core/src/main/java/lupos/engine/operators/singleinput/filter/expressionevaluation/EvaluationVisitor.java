
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
package lupos.engine.operators.singleinput.filter.expressionevaluation;

import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.singleinput.NotBoundException;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.sparql1_1.ASTABSFuncNode;
import lupos.sparql1_1.ASTAdditionNode;
import lupos.sparql1_1.ASTAggregation;
import lupos.sparql1_1.ASTAndNode;
import lupos.sparql1_1.ASTBnodeFuncNode;
import lupos.sparql1_1.ASTBooleanLiteral;
import lupos.sparql1_1.ASTBoundFuncNode;
import lupos.sparql1_1.ASTCeilFuncNode;
import lupos.sparql1_1.ASTCoalesceFuncNode;
import lupos.sparql1_1.ASTConcatFuncNode;
import lupos.sparql1_1.ASTContainsFuncNode;
import lupos.sparql1_1.ASTDataTypeFuncNode;
import lupos.sparql1_1.ASTDayFuncNode;
import lupos.sparql1_1.ASTDivisionNode;
import lupos.sparql1_1.ASTDoubleCircumflex;
import lupos.sparql1_1.ASTEncodeForUriFuncNode;
import lupos.sparql1_1.ASTEqualsNode;
import lupos.sparql1_1.ASTExists;
import lupos.sparql1_1.ASTFilterConstraint;
import lupos.sparql1_1.ASTFloatingPoint;
import lupos.sparql1_1.ASTFloorFuncNode;
import lupos.sparql1_1.ASTFunctionCall;
import lupos.sparql1_1.ASTGreaterThanEqualsNode;
import lupos.sparql1_1.ASTGreaterThanNode;
import lupos.sparql1_1.ASTHoursFuncNode;
import lupos.sparql1_1.ASTIfFuncNode;
import lupos.sparql1_1.ASTInNode;
import lupos.sparql1_1.ASTInteger;
import lupos.sparql1_1.ASTIriFuncNode;
import lupos.sparql1_1.ASTLangFuncNode;
import lupos.sparql1_1.ASTLangMatchesFuncNode;
import lupos.sparql1_1.ASTLangTag;
import lupos.sparql1_1.ASTLcaseFuncNode;
import lupos.sparql1_1.ASTLessThanEqualsNode;
import lupos.sparql1_1.ASTLessThanNode;
import lupos.sparql1_1.ASTMD5FuncNode;
import lupos.sparql1_1.ASTMinusNode;
import lupos.sparql1_1.ASTMinutesFuncNode;
import lupos.sparql1_1.ASTMonthFuncNode;
import lupos.sparql1_1.ASTMultiplicationNode;
import lupos.sparql1_1.ASTNotEqualsNode;
import lupos.sparql1_1.ASTNotExists;
import lupos.sparql1_1.ASTNotInNode;
import lupos.sparql1_1.ASTNotNode;
import lupos.sparql1_1.ASTNowFuncNode;
import lupos.sparql1_1.ASTOrNode;
import lupos.sparql1_1.ASTPlusNode;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTRDFLiteral;
import lupos.sparql1_1.ASTRandFuncNode;
import lupos.sparql1_1.ASTRegexFuncNode;
import lupos.sparql1_1.ASTRoundFuncNode;
import lupos.sparql1_1.ASTSHA1FuncNode;
import lupos.sparql1_1.ASTSHA256FuncNode;
import lupos.sparql1_1.ASTSHA384FuncNode;
import lupos.sparql1_1.ASTSHA512FuncNode;
import lupos.sparql1_1.ASTSTRUUIDFuncNode;
import lupos.sparql1_1.ASTSameTermFuncNode;
import lupos.sparql1_1.ASTSecondsFuncNode;
import lupos.sparql1_1.ASTStrAfterFuncNode;
import lupos.sparql1_1.ASTStrBeforeFuncNode;
import lupos.sparql1_1.ASTStrEndsFuncNode;
import lupos.sparql1_1.ASTStrFuncNode;
import lupos.sparql1_1.ASTStrLangFuncNode;
import lupos.sparql1_1.ASTStrReplaceFuncNode;
import lupos.sparql1_1.ASTStrdtFuncNode;
import lupos.sparql1_1.ASTStringLiteral;
import lupos.sparql1_1.ASTStrlenFuncNode;
import lupos.sparql1_1.ASTStrstartsFuncNode;
import lupos.sparql1_1.ASTSubstringFuncNode;
import lupos.sparql1_1.ASTSubtractionNode;
import lupos.sparql1_1.ASTTimeZoneFuncNode;
import lupos.sparql1_1.ASTTzFuncNode;
import lupos.sparql1_1.ASTUUIDFuncNode;
import lupos.sparql1_1.ASTUcaseFuncNode;
import lupos.sparql1_1.ASTUriFuncNode;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.ASTYearFuncNode;
import lupos.sparql1_1.ASTisBlankFuncNode;
import lupos.sparql1_1.ASTisIRIFuncNode;
import lupos.sparql1_1.ASTisLiteralFuncNode;
import lupos.sparql1_1.ASTisNumericFuncNode;
import lupos.sparql1_1.ASTisURIFuncNode;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SimpleNode;
public interface EvaluationVisitor<D, R> {
	
	/**
	 * <p>init.</p>
	 */
	public void init();
	
	/**
	 * <p>release.</p>
	 */
	public void release();
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTOrNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTOrNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTAndNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTAndNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTEqualsNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTEqualsNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNotEqualsNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTNotEqualsNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLessThanNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTLessThanNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLessThanEqualsNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTLessThanEqualsNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTGreaterThanNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTGreaterThanNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTGreaterThanEqualsNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTGreaterThanEqualsNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDivisionNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTDivisionNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTAdditionNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTAdditionNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTRDFLiteral} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTRDFLiteral node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTMinusNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTMinusNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTMultiplicationNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTMultiplicationNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNotNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTNotNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTPlusNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTPlusNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTRegexFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTRegexFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStringLiteral} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStringLiteral node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTBooleanLiteral} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTBooleanLiteral node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSubtractionNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTSubtractionNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTVar} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTVar node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTBoundFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTBoundFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStrFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLangFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTLangFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLangMatchesFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTLangMatchesFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDataTypeFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTDataTypeFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSameTermFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTSameTermFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTUriFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTUriFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTIriFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTIriFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTBnodeFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTBnodeFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTisLiteralFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTisLiteralFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTFunctionCall} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTFunctionCall node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTQuotedURIRef} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTQuotedURIRef node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDoubleCircumflex} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTDoubleCircumflex node, Bindings b, D d) throws NotBoundException, TypeErrorException;

	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTFloatingPoint} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTFloatingPoint node, Bindings b, D d) throws NotBoundException, TypeErrorException;

	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLangTag} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTLangTag node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTisBlankFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTisBlankFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTisURIFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTisURIFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTisIRIFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTisIRIFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTInteger} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTInteger node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTFilterConstraint} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTFilterConstraint node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTRandFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTRandFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTABSFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTABSFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTCeilFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTCeilFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTFloorFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTFloorFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTRoundFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTRoundFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTConcatFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTConcatFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrlenFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStrlenFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTUcaseFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTUcaseFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTLcaseFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTLcaseFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTEncodeForUriFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTEncodeForUriFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTContainsFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTContainsFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrstartsFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStrstartsFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrEndsFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStrEndsFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrBeforeFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStrBeforeFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrAfterFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStrAfterFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTYearFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTYearFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTMonthFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTMonthFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTDayFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTDayFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTHoursFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTHoursFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTMinutesFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTMinutesFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSecondsFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTSecondsFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTTimeZoneFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTTimeZoneFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTTzFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTTzFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNowFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTNowFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTUUIDFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTUUIDFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSTRUUIDFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTSTRUUIDFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTMD5FuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTMD5FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSHA1FuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTSHA1FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSHA256FuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTSHA256FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSHA384FuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTSHA384FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSHA512FuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTSHA512FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTCoalesceFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTCoalesceFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTIfFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTIfFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrLangFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStrLangFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrdtFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStrdtFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTisNumericFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTisNumericFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTSubstringFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTSubstringFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStrReplaceFuncNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTStrReplaceFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTExists} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTExists node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNotExists} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTNotExists node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTInNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTInNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTNotInNode} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTNotInNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>evaluate.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTAggregation} object.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param d a D object.
	 * @return a R object.
	 * @throws lupos.engine.operators.singleinput.NotBoundException if any.
	 * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
	 */
	public R evaluate(ASTAggregation node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	/**
	 * <p>applyAggregationCOUNT.</p>
	 *
	 * @param values a {@link java.util.Iterator} object.
	 * @return a R object.
	 */
	public R applyAggregationCOUNT(Iterator<R> values);
	
	/**
	 * <p>applyAggregationSUM.</p>
	 *
	 * @param values a {@link java.util.Iterator} object.
	 * @return a R object.
	 */
	public R applyAggregationSUM(Iterator<R> values);
	
	/**
	 * <p>applyAggregationMIN.</p>
	 *
	 * @param values a {@link java.util.Iterator} object.
	 * @return a R object.
	 */
	public R applyAggregationMIN(Iterator<R> values);
	
	/**
	 * <p>applyAggregationMAX.</p>
	 *
	 * @param values a {@link java.util.Iterator} object.
	 * @return a R object.
	 */
	public R applyAggregationMAX(Iterator<R> values);
	
	/**
	 * <p>applyAggregationAVG.</p>
	 *
	 * @param values a {@link java.util.Iterator} object.
	 * @return a R object.
	 */
	public R applyAggregationAVG(Iterator<R> values);
	
	/**
	 * <p>applyAggregationSAMPLE.</p>
	 *
	 * @param values a {@link java.util.Iterator} object.
	 * @return a R object.
	 */
	public R applyAggregationSAMPLE(Iterator<R> values);
	
	/**
	 * <p>applyAggregationGROUP_CONCAT.</p>
	 *
	 * @param values a {@link java.util.Iterator} object.
	 * @param SEPARATOR a {@link java.lang.String} object.
	 * @return a R object.
	 */
	public R applyAggregationGROUP_CONCAT(Iterator<R> values, String SEPARATOR);

	/**
	 * <p>setEvaluator.</p>
	 *
	 * @param evaluator a {@link lupos.engine.evaluators.CommonCoreQueryEvaluator} object.
	 */
	public void setEvaluator(CommonCoreQueryEvaluator<Node> evaluator);

	/**
	 * <p>getEvaluator.</p>
	 *
	 * @return a {@link lupos.engine.evaluators.CommonCoreQueryEvaluator} object.
	 */
	public CommonCoreQueryEvaluator<Node> getEvaluator();

	/**
	 * <p>setCollectionForExistNodes.</p>
	 *
	 * @param collectionForExistNodes a {@link java.util.Map} object.
	 */
	public void setCollectionForExistNodes(Map<SimpleNode, Root> collectionForExistNodes);

	/**
	 * <p>getCollectionForExistNodes.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<SimpleNode, Root> getCollectionForExistNodes();
}
