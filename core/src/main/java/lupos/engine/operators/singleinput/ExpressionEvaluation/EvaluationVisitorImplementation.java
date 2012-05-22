package lupos.engine.operators.singleinput.ExpressionEvaluation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.VariableInInferenceRule;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.application.Application;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.NotBoundException;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.tripleoperator.TriplePattern;
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
import lupos.sparql1_1.ASTNIL;
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

public class EvaluationVisitorImplementation implements EvaluationVisitor<Map<Node, Object>, Object> {

	
	// for each ASTExists and ASTNotExists an IndexCollection and Result is
	// stored by the Filter
	// Additionally we hand over the evaluator in the filter
	// this is needed to process the actual subquery for each node
	private Map<SimpleNode, IndexCollection> collectionForExistNodes = new HashMap<SimpleNode, IndexCollection>();
	private Map<SimpleNode, Boolean> simpleExistNodes = new HashMap<SimpleNode, Boolean>();
	protected Map<SimpleNode, QueryResult> queryResultsForExistNodes = new HashMap<SimpleNode, QueryResult>();
	private CommonCoreQueryEvaluator<Node> evaluator;

	public void setCollectionForExistNodes(
			Map<SimpleNode, IndexCollection> collectionForExistNodes) {
		this.collectionForExistNodes = collectionForExistNodes;
	}

	public Map<SimpleNode, IndexCollection> getCollectionForExistNodes() {
		return collectionForExistNodes;
	}

	public CommonCoreQueryEvaluator<Node> getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(CommonCoreQueryEvaluator<Node> evaluator) {
		this.evaluator = evaluator;
	}
	
	@Override
	public void init() {
		EvaluationVisitorImplementation.resetNowDate();
		this.queryResultsForExistNodes.clear();		
	}
	
	@Override
	public void release() {
	}
	
	@Override
	public Object evaluate(ASTOrNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		NotBoundException _exceptionNB = null;
		TypeErrorException _exceptionTE = null;
		try {
			if (Helper.booleanEffectiveValue(node.jjtGetChild(0).accept(this, b, d)))
				return true;
		} catch (final NotBoundException nbe) {
			_exceptionNB = nbe;
		} catch (final TypeErrorException tee) {
			_exceptionTE = tee;
		}
		try {
			if (Helper.booleanEffectiveValue(node.jjtGetChild(1).accept(this, b, d)))
				return true;
		} catch (final NotBoundException nbe) {
			_exceptionNB = nbe;
		} catch (final TypeErrorException tee) {
			_exceptionTE = tee;
		}
		if (_exceptionNB != null) {
			throw _exceptionNB;
		} else if (_exceptionTE != null) {
			throw _exceptionTE;
		} else
			return false;
	}

	@Override
	public Object evaluate(ASTAndNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		try {
			return (Helper.booleanEffectiveValue(node.jjtGetChild(0).accept(this, b, d)) && 
					Helper.booleanEffectiveValue(node.jjtGetChild(1).accept(this, b, d)));
		} catch (final NotBoundException nbe) {
			throw (nbe);
		} catch (final TypeErrorException tee) {
			throw (tee);
		} catch (final Exception e) {
			System.out.println(e);
			return null;
		}
	}

	@Override
	public Object evaluate(ASTEqualsNode node, Bindings b, Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.equals(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(ASTNotEqualsNode node, Bindings b,
			Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.NOTequals(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(ASTLessThanNode node, Bindings b, Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.less(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(ASTLessThanEqualsNode node, Bindings b, Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.le(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(ASTGreaterThanNode node, Bindings b,
			Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.greater(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(ASTGreaterThanEqualsNode node, Bindings b,
			Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.ge(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}
	
	@Override
	public Object evaluate(ASTDivisionNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.divideNumericValues(this.resultOfChildZero(node, b, d), this.resultOfChildOne(node, b, d));
	}

	@Override
	public Object evaluate(ASTAdditionNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.addNumericValues(this.resultOfChildZero(node, b, d), this.resultOfChildOne(node, b, d));
	}
	
	protected Object resultOfChildZero(Node node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.jjtGetChild(0).accept(this, b, d);
	}

	protected Object resultOfChildOne(Node node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.jjtGetChild(1).accept(this, b, d);
	}

	protected Object resultOfChildTwo(Node node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.jjtGetChild(2).accept(this, b, d);
	}

	@Override
	public Object evaluate(ASTRDFLiteral node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return resultOfChildZero(node, b, d);
	}
	
	protected Object[] getOperand(Node node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object[] result = new Object[2];
		result[0] = resultOfChildZero(node, b, d);
		result[1] = Helper.getType(result[0]);
		return result;
	}

	@Override
	public Object evaluate(ASTMinusNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object[] operand = getOperand(node, b, d);
		if (operand[1] == BigInteger.class)
			return Helper.getInteger(operand[0]).negate();
		else if (operand[1] == Float.class)
			return -1 * Helper.getFloat(operand[0]);
		else if (operand[1] == Double.class)
			return -1 * Helper.getDouble(operand[0]);
		else if (operand[1] == BigDecimal.class)
			return Helper.getBigDecimal(operand[0]).negate();
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(ASTMultiplicationNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.multiplyNumericValues(this.resultOfChildZero(node, b, d), this.resultOfChildOne(node, b, d));
	}


	@Override
	public Object evaluate(ASTNotNode node, Bindings b, Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return !Helper.booleanEffectiveValue(resultOfChildZero(node, b, d));
	}

	@Override
	public Object evaluate(ASTPlusNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return resultOfChildZero(node, b, d);
	}

	@Override
	public Object evaluate(ASTRegexFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = resultOfChildZero(node, b, d);
		if (o instanceof URILiteral || o instanceof AnonymousLiteral)
			return false;
		final String cmp = Helper.getString(o);
		String pattern = Helper.getString(resultOfChildOne(node, b, d));
		String oldPattern;
		do {
			oldPattern = pattern;
			pattern = pattern.replace("\\\\", "\\");
		} while (oldPattern.compareTo(pattern) != 0);
		String flags = "";
		if (node.jjtGetNumChildren() > 2)
			flags = Helper.getString(resultOfChildTwo(node, b, d));
		// return match(cmp,pattern,flags); // does not support flag x!!!
		return Helper.matchXerces(cmp, pattern, flags);
	}

	@Override
	public Object evaluate(ASTStringLiteral node, Bindings b, Map<Node, Object> d) {
		return "\""+ Helper.trim(node.getStringLiteral()) + "\"";	}

	@Override
	public Object evaluate(ASTBooleanLiteral node, Bindings b,
			Map<Node, Object> d) {
		return node.getState();
	}

	@Override
	public Object evaluate(ASTSubtractionNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.subtractNumericValues(this.resultOfChildZero(node, b, d), this.resultOfChildOne(node, b, d));
	}

	@Override
	public Object evaluate(ASTVar node, Bindings b, Map<Node, Object> d) throws NotBoundException {
		Literal l = b.get(new Variable(node.getName()));
		if (l == null)
			l = b.get(new VariableInInferenceRule(node.getName()));
		if (l == null)
			throw new NotBoundException("Variable "
					+ node.getName()
					+ " is not bound!");
		else
			return l;
	}

	@Override
	public Object evaluate(ASTBoundFuncNode node, Bindings b, Map<Node, Object> d) throws TypeErrorException {
		try {
			resultOfChildZero(node, b, d);
			return true;
		} catch (final NotBoundException nbe) {
			return false;
		}
	}

	@Override
	public Object evaluate(ASTLangFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = resultOfChildZero(node, b, d);
		if (o instanceof LanguageTaggedLiteral) {
			return "\"" + ((LanguageTaggedLiteral) o).getOriginalLanguage() + "\"";
		} else if (o instanceof TypedLiteral || o instanceof CodeMapLiteral || o instanceof StringLiteral) {
			return "\"\"";
		} else
			throw new TypeErrorException();
	}

	@Override
	public Object evaluate(ASTLangMatchesFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o1 = resultOfChildZero(node, b, d);
		final Object o2 = resultOfChildOne(node, b, d);
		final String s1 = o1.toString().toUpperCase();
		final String s2 = o2.toString().toUpperCase();
		if (s2.compareTo("\"*\"") == 0) {
			if (s1.compareTo("\"\"") == 0)
				return false;
			else
				return true;
		}
		if (s2.length() < s1.length()) {
			if (s2.compareTo("\"\"") == 0)
				return false;
			else
				return s1.substring(1, s1.length() - 1).startsWith(
						s2.substring(1, s2.length() - 1));
		}
		return (s1.compareTo(s2) == 0);
	}

	@Override
	public Object evaluate(ASTDataTypeFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = resultOfChildZero(node, b, d);
		if (o instanceof TypedLiteral) {
			return ((TypedLiteral) o).getTypeLiteral();
		} else {
			try {
				if (o instanceof BigInteger)
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#integer>");
				else if (o instanceof Float)
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#float>");
				else if (o instanceof Double)
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#double>");
				else if (o instanceof BigDecimal)
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#decimal>");
				else if (o instanceof String)
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#string>");
			} catch (final URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		if (o instanceof CodeMapLiteral || o instanceof StringLiteral)
			try {
				return LiteralFactory.createURILiteral("<http://www.w3.org/2001/XMLSchema#string>");
			} catch (final URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			throw new TypeErrorException();
	}

	@Override
	public Object evaluate(ASTSameTermFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final String a = Helper.getOriginalValueString(resultOfChildZero(node, b, d));
		final String bs = Helper.getOriginalValueString(resultOfChildOne(node, b, d));
		return a.compareTo(bs) == 0;
	}
	
	public Object createURI_IRI(Node node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object o = resultOfChildZero(node, b, d);
		if(o instanceof LazyLiteral)
			o=((LazyLiteral)o).getLiteral();
		if(o instanceof URILiteral)
			return o;
		if(o instanceof TypedLiteral){
			TypedLiteral tl = (TypedLiteral) o;
			if(tl.getType().compareTo("<http://www.w3.org/2001/XMLSchema#string>")==0)
				o = tl.getContent();
			else throw new TypeErrorException();
		}
		if(o instanceof String || o instanceof StringLiteral || o instanceof CodeMapLiteral){
			try {
				String s = Helper.trim(o.toString());
				if(s.startsWith("'") || s.startsWith("\""))
					s=s.substring(1, s.length()-1);
				return LiteralFactory.createURILiteral("<"+s+">");
			} catch (URISyntaxException e) {
				throw new TypeErrorException();
			}
		}
		throw new TypeErrorException();
	}
	
	@Override
	public Object evaluate(ASTUriFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return createURI_IRI(node, b, d);
	}

	@Override
	public Object evaluate(ASTIriFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return createURI_IRI(node, b, d);
	}
	
	protected static final String prefixInternalBlankNodes = "_:internal!";
	protected int id = 0;
	protected final HashMap<Object, Integer> mapForBlankNodeGeneration = new HashMap<Object, Integer>();

	@Override
	public Object evaluate(ASTBnodeFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Integer id_for_o;
		if(node.jjtGetNumChildren()>0){
			Object o = resultOfChildZero(node, b, d);
			id_for_o = mapForBlankNodeGeneration.get(o);
			if(id_for_o == null){
				id_for_o = id++;
				mapForBlankNodeGeneration.put(o, id_for_o);
			}
		} else id_for_o = id++;
		return LiteralFactory.createAnonymousLiteral(prefixInternalBlankNodes+(id_for_o));
	}

	@Override
	public Object evaluate(ASTisLiteralFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = resultOfChildZero(node, b, d);
		if (!(o instanceof AnonymousLiteral || o instanceof URILiteral)) {
			if (o instanceof Literal)
				return true;
		}
		return false;
	}

	@Override
	public Object evaluate(ASTFunctionCall node, Bindings b, Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		final Literal name = LazyLiteral.getLiteral(node.jjtGetChild(0));
		if (name.toString().startsWith("<http://www.w3.org/2001/XMLSchema#")) {
			return Helper.cast(name.toString(), resultOfChildZero(node.jjtGetChild(1), b, d));
		}
		System.err.println("Filter Error: unknown function "+ name.toString());
		return false;
	}

	@Override
	public Object evaluate(ASTQuotedURIRef node, Bindings b, Map<Node, Object> d) {
		return LazyLiteral.getLiteral(node);
	}

	public Object evaluate(ASTDoubleCircumflex node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return LazyLiteral.getLiteral(node);
	}

	public Object evaluate(ASTFloatingPoint node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return LazyLiteral.getLiteral(node);
	}
	
	@Override
	public Object evaluate(ASTisBlankFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object parameter = resultOfChildZero(node, b, d);
		if(parameter instanceof LazyLiteral){
			parameter=((LazyLiteral)parameter).getLiteral();
		}
		if(parameter instanceof AnonymousLiteral) return true;
		else return false;
	}

	protected boolean isURI_IRI(Node node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = resultOfChildZero(node, b, d);
		if (o instanceof URILiteral) {
			return true;
		}
		final String text = o.toString();
		if (URILiteral.isURI(text))
			return true;
		else
			return false;	
	}	
	
	@Override
	public Object evaluate(ASTisURIFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return isURI_IRI(node, b, d);
	}

	@Override
	public Object evaluate(ASTisIRIFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return isURI_IRI(node, b, d);
	}

	@Override
	public Object evaluate(ASTInteger node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return new BigInteger(node.getValue().toString());
	}

	@Override
	public Object evaluate(ASTFilterConstraint node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return resultOfChildZero(node, b, d);
	}


	@Override
	public Object evaluate(ASTisNumericFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		// TODO check the domain!!!
		Object o = resultOfChildZero(node, b, d);
		if(o instanceof BigInteger || o instanceof Float || o instanceof Double || o instanceof BigDecimal)
			return true;
		if(o instanceof TypedLiteral){
			return Helper.isNumeric(((TypedLiteral)o).getType());
		}
		return false;
	}

	@Override
	public Object evaluate(ASTAggregation node, Bindings b, Map<Node, Object> d) {
		// the result of the aggregation function has already been previously computed...
		return d.get(node);
	}

	@Override
	public Object applyAggregationCOUNT(Iterator<Object> values) {
		long l=0;
		while(values.hasNext()){
			values.next();
			l++;
		}
		return BigInteger.valueOf(l);
	}

	@Override
	public Object applyAggregationSUM(Iterator<Object> values) {
		Object result=BigInteger.ZERO;
		while(values.hasNext()){
			Object next = values.next();
			try {
				result = Helper.addNumericValues(result, next);
			} catch (TypeErrorException e) {
			}
		}
		return result;
	}

	@Override
	public Object applyAggregationMIN(Iterator<Object> values) {
		Object result = null;
		while(values.hasNext()){
			Object next = values.next();
			try {
				if(result == null || Helper.less(next, result))
					result = next;
			} catch (TypeErrorException e) {
			}
		}
		return result;
	}

	@Override
	public Object applyAggregationMAX(Iterator<Object> values) {
		Object result = null;
		while(values.hasNext()){
			Object next = values.next();
			try {
				if(result == null || Helper.greater(next, result))
					result = next;
			} catch (TypeErrorException e) {
			}
		}
		return result;
	}

	
	@Override
	public Object evaluate(ASTExists node, final Bindings bindings,
			Map<Node, Object> d) {
		return processSubquery(node, bindings, d, collectionForExistNodes
				.get(node), evaluator);

	}

	@Override
	public Object evaluate(ASTNotExists node, final Bindings bindings,
			Map<Node, Object> d) {
		return !processSubquery(node, bindings, d, collectionForExistNodes
				.get(node), evaluator);

	}

	/**
	 * Checks whether the subquery represented by the {@link IndexCollection}
	 * and {@link Result} has a non-empty result. Used for processing Exists and
	 * Not Exists.
	 * 
	 * @param node
	 *            the node associated with this query, usually an instance of
	 *            {@link ASTExists} or {@link ASTNotExists}
	 * @param bindings
	 *            a {@link Bindings}, {@link Variable}s, which are bounded in
	 *            this {@link Bindings}, are replaced by the {@link Literal}s in
	 *            the subquery.
	 * @param d
	 * @param collection
	 *            the {@link IndexCollection} for the subquery
	 * @param evaluator
	 *            the {@link CommonCoreQueryEvaluator} which should be used to
	 *            process the subquery
	 * @param result
	 *            the {@link Result} for the subquery
	 */
	public boolean processSubquery(final SimpleNode node, final Bindings bindings,
			Map<Node, Object> d, IndexCollection collection,
			CommonCoreQueryEvaluator<Node> evaluator) {
		Boolean simple = simpleExistNodes.get(node);
		if(simple==null){
			collection.visit(new SimpleOperatorGraphVisitor() {

				@Override
				public Object visit(BasicOperator basicOperator) {
					// exclude more complicated cases, which might lead to errors...
					// maybe too strict, must be checked again to allow more...
					if (!(basicOperator instanceof BasicIndex || 
							basicOperator instanceof Join || 
							basicOperator instanceof Result)){
						simpleExistNodes.put(node, false);
					}						
					return null;
				}
			});
			simple = simpleExistNodes.get(node);
			if(simple==null){
				simpleExistNodes.put(node, true);
				simple = true;
			}
		}
		if(simple) {
			return processSimpleSubquery(node, bindings, d, collection, evaluator);			
		} else {
			return processSubqueryAndGetWholeResult(node, bindings, d, collection, evaluator);
		}
	}
	
	/**
	 * Checks whether the subquery represented by the {@link IndexCollection}
	 * and {@link Result} has a non-empty result. Used for processing Exists and
	 * Not Exists.<br>
	 * Implementation note: Contrary to {@link EvaluationVisitorImplementation}
	 * the subquery is processed once per {@link ASTExists}/{@link ASTNotExists}
	 * node and the evaluation is done by iterating over the result set.
	 * 
	 * @param node
	 *            the node associated with this query, usually an instance of
	 *            {@link ASTExists} or {@link ASTNotExists}
	 * @param bindings
	 *            a {@link Bindings}, {@link Variable}s, which are bounded in
	 *            this {@link Bindings}, are replaced by the {@link Literal}s in
	 *            the subquery.
	 * @param d
	 * @param collection
	 *            the {@link IndexCollection} for the subquery
	 * @param evaluator
	 *            the {@link CommonCoreQueryEvaluator} which should be used to
	 *            process the subquery
	 * @param result
	 *            the {@link Result} for the subquery
	 */
	public boolean processSubqueryAndGetWholeResult(SimpleNode node, Bindings bindings,
			Map<Node, Object> d, IndexCollection collection,
			CommonCoreQueryEvaluator<Node> evaluator) {

		if (!queryResultsForExistNodes.containsKey(node)) {
			performSubQueryAndGetWholeResult(node, collection, evaluator);
		}

		Iterator<Bindings> bindingsSet = queryResultsForExistNodes.get(node)
				.iterator();

		while (bindingsSet.hasNext()) {
			Bindings bindings2 = bindingsSet.next();
			Set<Variable> vars = bindings.getVariableSet();
			vars.retainAll(bindings2.getVariableSet());

			boolean isEqual = true;
			for (Variable variable : vars) {
				if (bindings.get(variable)
						.compareToNotNecessarilySPARQLSpecificationConform(
								bindings2.get(variable)) != 0) {
					isEqual = false;
				}
			}

			if (isEqual || vars.isEmpty()) {
				return true;
			}
		}

		return false;
	}
	
	public static class GetResult implements SimpleOperatorGraphVisitor {
		
		protected Result result = null;
		
		public Result getResult() {
			return result;
		}

		@Override
		public Object visit(BasicOperator basicOperator) {
			if(basicOperator instanceof Result)
				this.result = (Result) basicOperator;
			return null;
		}		
	}
	
	public static void setMaxVariables(BasicOperator root){
		// save all variables of the subquery in the bindingsarray
		final Set<Variable> maxVariables = new TreeSet<Variable>();
		root.visit(new SimpleOperatorGraphVisitor() {
			public Object visit(final BasicOperator basicOperator) {
				if (basicOperator.getUnionVariables() != null)
					maxVariables.addAll(basicOperator.getUnionVariables());
				return null;
			}

		});
		
		BindingsArray.forceVariables(maxVariables);
	}
	
	public static Result setupEvaluator(CommonCoreQueryEvaluator<Node> evaluator, IndexCollection collection){
		evaluator.setRootNode(collection);
		collection.deleteParents();
		collection.setParents();
		collection.detectCycles();
		collection.sendMessage(new BoundVariablesMessage());

		setMaxVariables(collection);
		
		GetResult getResult = new GetResult();
		collection.visit(getResult);
		Result result = getResult.getResult();

		evaluator.setResult(result);
		result.clearApplications();
		
		return result;
	}

	/**
	 * Computes the {@link QueryResult} for the subquery with the given node and
	 * stores it in a {@link HashMap} in this class, so it can be used for
	 * further processing.
	 * 
	 * @param node
	 *            the node associated with this query, usually an instance of
	 *            {@link ASTExists} or {@link ASTNotExists}
	 * @param collection
	 *            the {@link IndexCollection} for the subquery
	 * @param evaluator
	 *            the {@link CommonCoreQueryEvaluator} which should be used to
	 *            process the subquery
	 * @param result
	 *            the {@link Result} for the subquery
	 */
	protected void performSubQueryAndGetWholeResult(final SimpleNode node,
			IndexCollection collection,
			CommonCoreQueryEvaluator<Node> evaluator) {
		BasicOperator oldRoot = evaluator.getRootNode();
		Result oldResult = evaluator.getResultOperator();

		// the static bindingsarray is saved and restored after the subquery
		Map<Variable, Integer> oldVarsTmp = BindingsArray.getPosVariables();

		Result result = setupEvaluator(evaluator, (IndexCollection) collection.deepClone());
		
		CollectResult cr = new CollectResult(false);
		result.addApplication(cr);

		evaluator.logicalOptimization();
		evaluator.physicalOptimization();
		try {
			evaluator.evaluateQuery();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		QueryResult queryResult = cr.getResult();
		queryResultsForExistNodes.put(node, transformQueryResult(queryResult));
		BindingsArray.forceVariables(oldVarsTmp);
		evaluator.setRootNode(oldRoot);
		evaluator.setResult(oldResult);
	}

	protected QueryResult transformQueryResult(QueryResult queryResult) {
		QueryResult result = QueryResult.createInstance();
		if (queryResult == null) {
			return result; // empty result
		} else {

			Iterator<Bindings> it = queryResult.oneTimeIterator();
			while (it.hasNext()) {
				Bindings b = new BindingsMap();
				Bindings bindings = it.next();
				b.addAll(bindings);
				result.add(b);
			}
			return result;
		}
	}
	
	public static boolean processSimpleSubquery(SimpleNode node, final Bindings bindings,
			Map<Node, Object> d, IndexCollection collection,
			CommonCoreQueryEvaluator<Node> evaluator) {

		BasicOperator oldRoot = evaluator.getRootNode();
		Result oldResult = evaluator.getResultOperator();
		// the static bindingsarray is saved and restored after the subquery
		Map<Variable, Integer> oldVarsTmp = BindingsArray.getPosVariables();

		IndexCollection collectionClone = (IndexCollection) collection.deepClone();
		collectionClone.visit(new SimpleOperatorGraphVisitor() {

			@Override
			public Object visit(BasicOperator basicOperator) {

				if (basicOperator instanceof BasicIndex) {
					BasicIndex basicIndex = (BasicIndex) basicOperator;
					Collection<TriplePattern> triplePatterns = basicIndex
							.getTriplePattern();
					Collection<TriplePattern> newTriplePatterns = new LinkedList<TriplePattern>();
					for (TriplePattern t : triplePatterns) {
						Item[] itemArray = t.getItems().clone();
						for (int i = 0; i < 3; i++) {
							if (itemArray[i].isVariable()) {
								Literal literal = bindings
										.get((Variable) itemArray[i]);
								if (literal != null)
									itemArray[i] = literal;
							}
						}
						newTriplePatterns.add(new TriplePattern(itemArray));
					}
					basicIndex.setTriplePatterns(newTriplePatterns);
				} else {
					// TODO Filter
					// node.clone(false)
				}
				return null;
			}
		});

		Result result = setupEvaluator(evaluator, collectionClone);

		result.clearApplications();
		NonEmptyApplication application = new NonEmptyApplication();
		result.addApplication(application);
		
		evaluator.logicalOptimization();
		evaluator.physicalOptimization();
		try {
			evaluator.evaluateQuery();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		BindingsArray.forceVariables(oldVarsTmp);
		evaluator.setRootNode(oldRoot);
		evaluator.setResult(oldResult);
		return !application.getResultIsEmpty();
	}
	
	public static class NonEmptyApplication implements Application {
		
		protected boolean resultIsEmpty=true;
		
		public boolean getResultIsEmpty(){
			return this.resultIsEmpty;
		}
		
		@Override
		public void call(QueryResult res) {
			if (res.oneTimeIterator().hasNext()) {
				this.resultIsEmpty = false;
			}
		}

		@Override
		public void deleteResult() {}

		@Override
		public void deleteResult(QueryResult res) {}

		@Override
		public void start(Type type) {}

		@Override
		public void stop() {}
	}

	/**
	 * Method for computing the SAMPLE-statement by returning a random value of
	 * the given values
	 * 
	 * @param Iterator
	 *            <Object>
	 * @return Object
	 */
	@Override
	public Object applyAggregationSAMPLE(Iterator<Object> values) {
		double r = Math.random();
		LinkedList<Object> valueList = new LinkedList<Object>();
		while (values.hasNext()) {
			valueList.add(values.next());
		}
		double randomValue = (valueList.size() - 1) * r;
		return valueList.get((int) Math.round(randomValue));
	}

	/**
	 * Concatenates the values of a group and inserts between the values a separator
	 * if given
	 * 
	 * @param Iterator
	 *            <Object> values
	 * @param String
	 *            separator
	 */
	@Override
	public Object applyAggregationGROUP_CONCAT(Iterator<Object> values,
			String separator) {
		boolean firstTime = true;
		String concat = "";
		while (values.hasNext()) {
			if (firstTime) {
				concat = concat + Helper.getString(values.next());
				firstTime = false;
			} else {
				concat = concat + separator + Helper.getString(values.next());
			}

		}
		return Helper.quote(concat);
	}

	@Override
	public Object applyAggregationAVG(Iterator<Object> values) {
		long count = 0;
		Object result = BigInteger.ZERO;
		while (values.hasNext()) {
			Object next = values.next();
			try {
				result = Helper.addNumericValues(result, next);
				count++;
			} catch (TypeErrorException e) {
				return null;
			}
		}
		try {
			return Helper.divideNumericValues(result, BigInteger.valueOf(count));
		} catch (TypeErrorException e) {
			return null;
		}
	}

	@Override
	public Object evaluate(ASTStrdtFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object arg0 = Helper.unlazy(this.resultOfChildZero(node, b, d));
		Object arg1 = Helper.unlazy(this.resultOfChildOne(node, b, d));
		try {
			return LiteralFactory.createTypedLiteral("\""
					+ Helper.getSimpleString(arg0) + "\"", Helper.getString(arg1));
		} catch (URISyntaxException e) {
			throw new TypeErrorException();
		}
	}

	@Override
	public Object evaluate(ASTStrFuncNode node, Bindings b, Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(resultOfChildZero(node, b, d));
		if (o instanceof TypedLiteral) {
			return ((TypedLiteral) o).getOriginalContent();
		} else if (o instanceof LanguageTaggedLiteral) {
			return ((LanguageTaggedLiteral) o).getContentLiteral();
		}
		return o.toString();
	}

	@Override
	public Object evaluate(ASTStrLangFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final String content = Helper.quote(Helper.getSimpleString(Helper.unlazy(resultOfChildZero(
				node, b, d))));
		final String language = Helper.getSimpleString(Helper.unlazy(resultOfChildOne(node,
				b, d)));
		return LiteralFactory.createLanguageTaggedLiteral(content, language);
	}

	@Override
	public Object evaluate(ASTABSFuncNode node, Bindings b, Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(resultOfChildZero(node, b, d));
		Object type = Helper.getType(o);
		if (type == BigInteger.class)
			return Helper.getInteger(o).abs();
		if (type == Double.class)
			return Math.abs(Helper.getDouble(o));
		if (type == Float.class)
			return Math.abs(Helper.getFloat(o));
		if (type == BigDecimal.class)
			return Helper.getBigDecimal(o).abs();
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(ASTCeilFuncNode node, Bindings b, Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(resultOfChildZero(node, b, d));
		Object type = Helper.getType(o);
		if (type == BigInteger.class)
			return Helper.getInteger(o);
		if (type == Double.class)
			return Math.ceil(Helper.getDouble(o));
		if (type == Float.class)
			return Math.ceil(Helper.getFloat(o));
		if (type == BigDecimal.class) {
			BigDecimal bd = Helper.getBigDecimal(o);
			return bd.setScale(0, RoundingMode.CEILING);
		}
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(ASTFloorFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(resultOfChildZero(node, b, d));
		Object type = Helper.getType(o);
		if (type == BigInteger.class)
			return Helper.getInteger(o);
		if (type == Double.class)
			return Math.floor(Helper.getDouble(o));
		if (type == Float.class)
			return Math.floor(Helper.getFloat(o));
		if (type == BigDecimal.class) {
			BigDecimal bd = Helper.getBigDecimal(o);
			return bd.setScale(0, RoundingMode.FLOOR);
		}
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(ASTRoundFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(resultOfChildZero(node, b, d));
		Object type = Helper.getType(o);
		if (type == BigInteger.class)
			return Helper.getInteger(o);
		if (type == Double.class)
			return Math.round(Helper.getDouble(o));
		if (type == Float.class)
			return Math.round(Helper.getFloat(o));
		if (type == BigDecimal.class) {
			BigDecimal bd = Helper.getBigDecimal(o);
			return bd.setScale(0, RoundingMode.HALF_UP);
		}
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(ASTConcatFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		if (node.jjtGetNumChildren() == 0)
			return "";
		// jump over ASTExpressionList!
		Node child0 = node.jjtGetChild(0);
		if (child0 instanceof ASTNIL || child0.jjtGetNumChildren() == 0) {
			return "";
		}
		Object result = Helper.unlazy(child0.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(result))
			return null;
		for (int i = 1; i < child0.jjtGetNumChildren(); i++) {

			Object child = Helper.unlazy(child0.jjtGetChild(i).accept(this, b, d));

			if (Helper.isNumeric(child))
				return null;

			String concatenatedContent = "\"" + Helper.unquote(Helper.getContent(result))
					+ Helper.unquote(Helper.getContent(child)) + "\"";

			if (result instanceof TypedLiteral
					&& child instanceof TypedLiteral
					&& ((TypedLiteral) result).getTypeLiteral().equals(
							((TypedLiteral) child).getTypeLiteral())) {
				try {
					result = LiteralFactory.createTypedLiteral(
							concatenatedContent, ((TypedLiteral) result)
									.getTypeLiteral());
				} catch (URISyntaxException e) {
					throw new TypeErrorException();
				}
			} else if (result instanceof LanguageTaggedLiteral
					&& child instanceof LanguageTaggedLiteral
					&& ((LanguageTaggedLiteral) result).getLanguage()
							.compareTo(
									((LanguageTaggedLiteral) child)
											.getLanguage()) == 0) {
				result = LiteralFactory.createLanguageTaggedLiteral(
						concatenatedContent, ((LanguageTaggedLiteral) result)
								.getLanguage());
			} else {
				result = concatenatedContent;
			}
		}
		return result;
	}

	@Override
	public Object evaluate(ASTSubstringFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		final int start = Helper.getInteger(
				Helper.unlazy(node.jjtGetChild(1).accept(this, b, d))).intValue() - 1;
		final String content = Helper.unquote(Helper.getContent(wholeString));
		final String resultantContent;
		if (node.jjtGetNumChildren() == 2) {
			resultantContent = content.substring(start);
		} else {
			int end = Helper.getInteger(
					Helper.unlazy(node.jjtGetChild(2).accept(this, b, d))).intValue();
			resultantContent = content.substring(start, start + end);
		}
		return Helper.createWithSameType(resultantContent, wholeString);
	}

	@Override
	public Object evaluate(ASTStrlenFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		final String content = Helper.unquote(Helper.getContent(wholeString));
		return BigInteger.valueOf(content.length());
	}

	@Override
	public Object evaluate(ASTUcaseFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		return Helper.createWithSameType(Helper.unquote(Helper.getContent(wholeString))
				.toUpperCase(), wholeString);
	}

	@Override
	public Object evaluate(ASTLcaseFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		return Helper.createWithSameType(Helper.unquote(Helper.getContent(wholeString))
				.toLowerCase(), wholeString);
	}

	@Override
	public Object evaluate(ASTEncodeForUriFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		return Helper.quote(URLEncoder.encode(Helper.unquote(Helper.getContent(wholeString))));
	}

	@Override
	public Object evaluate(ASTContainsFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString))
			return null;
		return (Helper.unquote(Helper.getContent(wholeString))
				.contains(Helper.unquote(Helper.getContent(containsString))));
	}

	@Override
	public Object evaluate(ASTStrstartsFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString))
			return null;
		return (Helper.unquote(Helper.getContent(wholeString))
				.startsWith(Helper.unquote(Helper.getContent(containsString))));
	}

	@Override
	public Object evaluate(ASTStrEndsFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString))
			return null;
		return (Helper.unquote(Helper.getContent(wholeString))
				.endsWith(Helper.unquote(Helper.getContent(containsString))));
	}

	@Override
	public Object evaluate(ASTStrBeforeFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString))
			return null;
		String stringOfWholeString = Helper.unquote(Helper.getContent(wholeString));
		int index = stringOfWholeString
				.indexOf(Helper.unquote(Helper.getContent(containsString)));
		if (index > 0) {
			return Helper.createWithSameType(stringOfWholeString.substring(0, index),
					wholeString);
		} else {
			return Helper.createWithSameType("", wholeString);
		}
	}

	@Override
	public Object evaluate(ASTStrAfterFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString))
			return null;
		Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString))
			return null;
		String stringOfWholeString = Helper.unquote(Helper.getContent(wholeString));
		int index = stringOfWholeString
				.indexOf(Helper.unquote(Helper.getContent(containsString)));
		if (index > 0) {
			return Helper.createWithSameType(stringOfWholeString.substring(index),
					wholeString);
		} else {
			return Helper.createWithSameType("", wholeString);
		}
	}

	@Override
	public Object evaluate(ASTMD5FuncNode node, Bindings b, Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("MD5", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(ASTSHA1FuncNode node, Bindings b, Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("SHA1", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(ASTSHA256FuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("SHA-256", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(ASTSHA384FuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("SHA-384", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(ASTSHA512FuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("SHA-512", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(ASTYearFuncNode node, Bindings b, Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getYear() + 1900;
	}

	@Override
	public Object evaluate(ASTMonthFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getMonth() + 1;
	}

	@Override
	public Object evaluate(ASTDayFuncNode node, Bindings b, Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getDate();
	}

	@Override
	public Object evaluate(ASTHoursFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getHours();
	}

	@Override
	public Object evaluate(ASTMinutesFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getMinutes();
	}

	@Override
	public Object evaluate(ASTSecondsFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return BigDecimal.valueOf(Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getSeconds());
	}

	@Override
	public Object evaluate(ASTTimeZoneFuncNode node, Bindings b,
			Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		try {
			return LiteralFactory.createTypedLiteral(Helper
					.getTimezoneAndTypeCheck(Helper.unlazy(node.jjtGetChild(0).accept(
							this, b, d))),
					"<http://www.w3.org/2001/XMLSchema#dayTimeDuration>");
		} catch (URISyntaxException e) {
			throw new TypeErrorException();
		}
	}

	@Override
	public Object evaluate(ASTTzFuncNode node, Bindings b, Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.getTzAndTypeCheck(Helper.unlazy(node.jjtGetChild(0).accept(this,
				b, d)));
	}

	private static Date now = new Date();
	
	public static void resetNowDate(){
		now = new Date();
	}

	@Override
	public Object evaluate(ASTNowFuncNode node, Bindings b, Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return now;
	}
	
	@Override
	public Object evaluate(ASTInNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
	    return this.handleInAndNotIn(node, b, d);
	}

	@Override
	public Object evaluate(ASTNotInNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
	    return !this.handleInAndNotIn(node, b, d);
	}

	protected boolean handleInAndNotIn(Node node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object arg1 = Helper.unlazy(this.resultOfChildZero(node, b, d));
		Node child1 = node.jjtGetChild(1);
		for(int i=0; i<child1.jjtGetNumChildren(); i++) {
			Object arg2 = Helper.unlazy(child1.jjtGetChild(i).accept(this, b, d));
			if(Helper.equals(arg1, arg2))
				return true;
		}
		return false;
	}
	
	@Override
	public Object evaluate(ASTRandFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
	    return new Double(Math.random());
	}

	@Override
	public Object evaluate(ASTIfFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object arg0 = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if(Helper.booleanEffectiveValue(arg0)){
			return this.resultOfChildOne(node, b, d);
		} else {
			return this.resultOfChildTwo(node, b, d);
		}
	}

	@Override
	public Object evaluate(ASTCoalesceFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Node child0 = node.jjtGetChild(0);
		for(int i=0; i<child0.jjtGetNumChildren(); i++){
			try {
				return Helper.unlazy(child0.jjtGetChild(i).accept(this, b, d));
			} catch(Error e){
			} catch(Exception e){				
			}
		}
		return null;
	}
	
	@Override
	public Object evaluate(ASTStrReplaceFuncNode node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(resultOfChildZero(node, b, d));
		if (o instanceof URILiteral || o instanceof AnonymousLiteral || Helper.isNumeric(o))
			throw new TypeErrorException();
		final String cmp = Helper.unquote(Helper.getContent(o));
		String pattern = Helper.getString(resultOfChildOne(node, b, d));
		String replacement = Helper.getString(resultOfChildTwo(node, b, d));
		String oldPattern;
		do {
			oldPattern = pattern;
			pattern = pattern.replace("\\\\", "\\");
		} while (oldPattern.compareTo(pattern) != 0);
		String flags = "";
		if (node.jjtGetNumChildren() > 3)
			flags = Helper.getString(resultOfChildThree(node, b, d));
		// return match(cmp,pattern,flags); // does not support flag x!!!
		return Helper.createWithSameType(Helper.replaceXerces(cmp, pattern, flags, replacement), o);
	}

	protected Object resultOfChildThree(Node node, Bindings b, Map<Node, Object> d) throws NotBoundException, TypeErrorException {		
		return node.jjtGetChild(3).accept(this, b, d);
	}
}