package lupos.engine.operators.singleinput.ExpressionEvaluation;

import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.index.IndexCollection;
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
	
	public void init();
	
	public void release();
	
	public R evaluate(ASTOrNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTAndNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTEqualsNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTNotEqualsNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTLessThanNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTLessThanEqualsNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTGreaterThanNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTGreaterThanEqualsNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTDivisionNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTAdditionNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTRDFLiteral node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTMinusNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTMultiplicationNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTNotNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTPlusNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTRegexFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStringLiteral node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTBooleanLiteral node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTSubtractionNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTVar node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTBoundFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStrFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTLangFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTLangMatchesFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTDataTypeFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTSameTermFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTUriFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTIriFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTBnodeFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTisLiteralFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTFunctionCall node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTQuotedURIRef node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTDoubleCircumflex node, Bindings b, D d) throws NotBoundException, TypeErrorException;

	public R evaluate(ASTFloatingPoint node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTisBlankFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTisURIFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTisIRIFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTInteger node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTFilterConstraint node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTRandFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTABSFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTCeilFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTFloorFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTRoundFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTConcatFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStrlenFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTUcaseFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTLcaseFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTEncodeForUriFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTContainsFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStrstartsFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStrEndsFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStrBeforeFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStrAfterFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTYearFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTMonthFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTDayFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTHoursFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTMinutesFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTSecondsFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTTimeZoneFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTTzFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTNowFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTMD5FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTSHA1FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTSHA256FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTSHA384FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTSHA512FuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTCoalesceFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTIfFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStrLangFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStrdtFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTisNumericFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTSubstringFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTStrReplaceFuncNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTExists node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTNotExists node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTInNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTNotInNode node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R evaluate(ASTAggregation node, Bindings b, D d) throws NotBoundException, TypeErrorException;
	
	public R applyAggregationCOUNT(Iterator<R> values);
	
	public R applyAggregationSUM(Iterator<R> values);
	
	public R applyAggregationMIN(Iterator<R> values);
	
	public R applyAggregationMAX(Iterator<R> values);
	
	public R applyAggregationAVG(Iterator<R> values);
	
	public R applyAggregationSAMPLE(Iterator<R> values);
	
	public R applyAggregationGROUP_CONCAT(Iterator<R> values, String SEPARATOR);

	public void setEvaluator(CommonCoreQueryEvaluator<Node> evaluator);

	public CommonCoreQueryEvaluator<Node> getEvaluator();

	public void setCollectionForExistNodes(Map<SimpleNode, IndexCollection> collectionForExistNodes);

	public Map<SimpleNode, IndexCollection> getCollectionForExistNodes();
}