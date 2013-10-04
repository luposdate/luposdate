/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.engine.operators.singleinput.filter.expressionevaluation;

import java.io.UnsupportedEncodingException;
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
import java.util.UUID;

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
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
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
import lupos.sparql1_1.ASTLangTag;
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

public class EvaluationVisitorImplementation implements EvaluationVisitor<Map<Node, Object>, Object> {

	protected static HashMap<URILiteral, ExternalFunction> externalFunctions = new HashMap<URILiteral, ExternalFunction>();

	/**
	 * call this function to register an external function
	 * @param name name of the external function
	 * @param externalFunction the external function
	 */
	public static void registerExternalFunction(final URILiteral name, final ExternalFunction externalFunction){
		EvaluationVisitorImplementation.externalFunctions.put(name, externalFunction);
	}

	// for each ASTExists and ASTNotExists an Root and Result is
	// stored by the Filter
	// Additionally we hand over the evaluator in the filter
	// this is needed to process the actual subquery for each node
	private Map<SimpleNode, Root> collectionForExistNodes = new HashMap<SimpleNode, Root>();
	private final Map<SimpleNode, Boolean> simpleExistNodes = new HashMap<SimpleNode, Boolean>();
	protected Map<SimpleNode, QueryResult> queryResultsForExistNodes = new HashMap<SimpleNode, QueryResult>();
	private CommonCoreQueryEvaluator<Node> evaluator;

	@Override
	public void setCollectionForExistNodes(
			final Map<SimpleNode, Root> collectionForExistNodes) {
		this.collectionForExistNodes = collectionForExistNodes;
	}

	@Override
	public Map<SimpleNode, Root> getCollectionForExistNodes() {
		return this.collectionForExistNodes;
	}

	@Override
	public CommonCoreQueryEvaluator<Node> getEvaluator() {
		return this.evaluator;
	}

	@Override
	public void setEvaluator(final CommonCoreQueryEvaluator<Node> evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	public void init() {
		EvaluationVisitorImplementation.resetNowDate();
		this.queryResultsForExistNodes.clear();
	}

	@Override
	public void release() {
		// nothing to release...
	}

	@Override
	public Object evaluate(final ASTOrNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		NotBoundException _exceptionNB = null;
		TypeErrorException _exceptionTE = null;
		try {
			if (Helper.booleanEffectiveValue(node.jjtGetChild(0).accept(this, b, d))) {
				return true;
			}
		} catch (final NotBoundException nbe) {
			_exceptionNB = nbe;
		} catch (final TypeErrorException tee) {
			_exceptionTE = tee;
		}
		try {
			if (Helper.booleanEffectiveValue(node.jjtGetChild(1).accept(this, b, d))) {
				return true;
			}
		} catch (final NotBoundException nbe) {
			_exceptionNB = nbe;
		} catch (final TypeErrorException tee) {
			_exceptionTE = tee;
		}
		if (_exceptionNB != null) {
			throw _exceptionNB;
		} else if (_exceptionTE != null) {
			throw _exceptionTE;
		} else {
			return false;
		}
	}

	@Override
	public Object evaluate(final ASTAndNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
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
	public Object evaluate(final ASTEqualsNode node, final Bindings b, final Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.equals(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(final ASTNotEqualsNode node, final Bindings b,
			final Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.NOTequals(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(final ASTLessThanNode node, final Bindings b, final Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.less(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(final ASTLessThanEqualsNode node, final Bindings b, final Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.le(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(final ASTGreaterThanNode node, final Bindings b,
			final Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.greater(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(final ASTGreaterThanEqualsNode node, final Bindings b,
			final Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return Helper.ge(node.jjtGetChild(0).accept(this, b, d), node.jjtGetChild(1).accept(this, b, d));
	}

	@Override
	public Object evaluate(final ASTDivisionNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.divideNumericValues(this.resultOfChildZero(node, b, d), this.resultOfChildOne(node, b, d));
	}

	@Override
	public Object evaluate(final ASTAdditionNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.addNumericValues(this.resultOfChildZero(node, b, d), this.resultOfChildOne(node, b, d));
	}

	protected Object resultOfChildZero(final Node node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.jjtGetChild(0).accept(this, b, d);
	}

	protected Object resultOfChildOne(final Node node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.jjtGetChild(1).accept(this, b, d);
	}

	protected Object resultOfChildTwo(final Node node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.jjtGetChild(2).accept(this, b, d);
	}

	@Override
	public Object evaluate(final ASTRDFLiteral node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return this.resultOfChildZero(node, b, d);
	}

	protected Object[] getOperand(final Node node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object[] result = new Object[2];
		result[0] = this.resultOfChildZero(node, b, d);
		result[1] = Helper.getType(result[0]);
		return result;
	}

	@Override
	public Object evaluate(final ASTMinusNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object[] operand = this.getOperand(node, b, d);
		if (operand[1] == BigInteger.class) {
			return Helper.getInteger(operand[0]).negate();
		} else if (operand[1] == Float.class) {
			return -1 * Helper.getFloat(operand[0]);
		} else if (operand[1] == Double.class) {
			return -1 * Helper.getDouble(operand[0]);
		} else if (operand[1] == BigDecimal.class) {
			return Helper.getBigDecimal(operand[0]).negate();
		}
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(final ASTMultiplicationNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.multiplyNumericValues(this.resultOfChildZero(node, b, d), this.resultOfChildOne(node, b, d));
	}


	@Override
	public Object evaluate(final ASTNotNode node, final Bindings b, final Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		return !Helper.booleanEffectiveValue(this.resultOfChildZero(node, b, d));
	}

	@Override
	public Object evaluate(final ASTPlusNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return this.resultOfChildZero(node, b, d);
	}

	@Override
	public Object evaluate(final ASTRegexFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if (o instanceof URILiteral || o instanceof AnonymousLiteral) {
			return false;
		}
		final String cmp = Helper.getString(o);
		String pattern = Helper.getString(Helper.unlazy(this.resultOfChildOne(node, b, d)));
		String oldPattern;
		do {
			oldPattern = pattern;
			pattern = pattern.replace("\\\\", "\\");
		} while (oldPattern.compareTo(pattern) != 0);
		String flags = "";
		if (node.jjtGetNumChildren() > 2){
			flags = Helper.getString(Helper.unlazy(this.resultOfChildTwo(node, b, d)));
		}
		// return match(cmp,pattern,flags); // does not support flag x!!!
		return Helper.matchXerces(cmp, pattern, flags);
	}

	@Override
	public Object evaluate(final ASTStringLiteral node, final Bindings b, final Map<Node, Object> d) {
		return node.getLiteral(true);
	}

	@Override
	public Object evaluate(final ASTBooleanLiteral node, final Bindings b,
			final Map<Node, Object> d) {
		return node.getState();
	}

	@Override
	public Object evaluate(final ASTSubtractionNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.subtractNumericValues(this.resultOfChildZero(node, b, d), this.resultOfChildOne(node, b, d));
	}

	@Override
	public Object evaluate(final ASTVar node, final Bindings b, final Map<Node, Object> d) throws NotBoundException {
		Literal l = b.get(new Variable(node.getName()));
		if (l == null) {
			l = b.get(new VariableInInferenceRule(node.getName()));
		}
		if (l == null) {
			throw new NotBoundException("Variable "
					+ node.getName()
					+ " is not bound!");
		} else {
			return l;
		}
	}

	@Override
	public Object evaluate(final ASTBoundFuncNode node, final Bindings b, final Map<Node, Object> d) throws TypeErrorException {
		try {
			this.resultOfChildZero(node, b, d);
			return true;
		} catch (final NotBoundException nbe) {
			return false;
		}
	}

	@Override
	public Object evaluate(final ASTLangFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if (o instanceof LanguageTaggedLiteral) {
			return "\"" + ((LanguageTaggedLiteral) o).getOriginalLanguage() + "\"";
		} else if (o instanceof TypedLiteral || o instanceof CodeMapLiteral || o instanceof StringLiteral) {
			return "\"\"";
		} else {
			throw new TypeErrorException();
		}
	}

	@Override
	public Object evaluate(final ASTLangMatchesFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o1 = this.resultOfChildZero(node, b, d);
		final Object o2 = this.resultOfChildOne(node, b, d);
		final String s1 = o1.toString().toUpperCase();
		final String s2 = o2.toString().toUpperCase();
		if (s2.compareTo("\"*\"") == 0) {
			if (s1.compareTo("\"\"") == 0) {
				return false;
			} else {
				return true;
			}
		}
		if (s2.length() < s1.length()) {
			if (s2.compareTo("\"\"") == 0) {
				return false;
			} else {
				return s1.substring(1, s1.length() - 1).startsWith(
						s2.substring(1, s2.length() - 1));
			}
		}
		return (Helper.unquote(s1).compareTo(Helper.unquote(s2)) == 0);
	}

	@Override
	public Object evaluate(final ASTDataTypeFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if (o instanceof TypedLiteral) {
			return ((TypedLiteral) o).getTypeLiteral();
		} else {
			try {
				if (o instanceof BigInteger) {
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#integer>");
				} else if (o instanceof Float) {
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#float>");
				} else if (o instanceof Double) {
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#double>");
				} else if (o instanceof BigDecimal) {
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#decimal>");
				} else if (o instanceof String) {
					return LiteralFactory
					.createURILiteral("<http://www.w3.org/2001/XMLSchema#string>");
				}
			} catch (final URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		if (o instanceof CodeMapLiteral || o instanceof StringLiteral) {
			try {
				return LiteralFactory.createURILiteral("<http://www.w3.org/2001/XMLSchema#string>");
			} catch (final URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
			throw new TypeErrorException();
	}

	@Override
	public Object evaluate(final ASTSameTermFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final String a = Helper.getOriginalValueString(this.resultOfChildZero(node, b, d));
		final String bs = Helper.getOriginalValueString(this.resultOfChildOne(node, b, d));
		return a.compareTo(bs) == 0;
	}

	public Object createURI_IRI(final Node node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if(o instanceof URILiteral){
			return o;
		}
		if(o instanceof TypedLiteral){
			final TypedLiteral tl = (TypedLiteral) o;
			if(tl.getType().compareTo("<http://www.w3.org/2001/XMLSchema#string>")==0) {
				o = tl.getContent();
			} else {
				throw new TypeErrorException();
			}
		}
		if(o instanceof String || o instanceof StringLiteral || o instanceof CodeMapLiteral){
			try {
				String s = Helper.trim(o.toString());
				if(s.startsWith("'") || s.startsWith("\"")) {
					s=s.substring(1, s.length()-1);
				}
				return LiteralFactory.createURILiteral("<"+s+">");
			} catch (final URISyntaxException e) {
				throw new TypeErrorException();
			}
		}
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(final ASTUriFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return this.createURI_IRI(node, b, d);
	}

	@Override
	public Object evaluate(final ASTIriFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return this.createURI_IRI(node, b, d);
	}

	protected static final String prefixInternalBlankNodes = "_:internal!";
	protected int id = 0;
	protected final HashMap<Object, Integer> mapForBlankNodeGeneration = new HashMap<Object, Integer>();

	@Override
	public Object evaluate(final ASTBnodeFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		Integer id_for_o;
		if(node.jjtGetNumChildren()>0){
			final Object o = this.resultOfChildZero(node, b, d);
			id_for_o = this.mapForBlankNodeGeneration.get(o);
			if(id_for_o == null){
				id_for_o = this.id++;
				this.mapForBlankNodeGeneration.put(o, id_for_o);
			}
		} else {
			id_for_o = this.id++;
		}
		return LiteralFactory.createAnonymousLiteral(prefixInternalBlankNodes+(id_for_o));
	}

	@Override
	public Object evaluate(final ASTisLiteralFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if (!(o instanceof AnonymousLiteral || o instanceof URILiteral)) {
			if (o instanceof Literal){
				return true;
			}
		}
		return false;
	}

	@Override
	public Object evaluate(final ASTFunctionCall node, final Bindings b, final Map<Node, Object> d) throws TypeErrorException, NotBoundException {
		final Literal name = LazyLiteral.getLiteral(node.jjtGetChild(0));
		final ExternalFunction externalFunction = EvaluationVisitorImplementation.externalFunctions.get(name);
		if(externalFunction!=null){
			final Node child1 = node.jjtGetChild(1);
			final int number = child1.jjtGetNumChildren();
			final Object[] args = new Object[number];
			for(int i=0; i<number; i++){
				args[i] = child1.jjtGetChild(i).accept(this, b, d);
			}
			return externalFunction.evaluate(args);
		}
		if (name.toString().startsWith("<http://www.w3.org/2001/XMLSchema#")) {
			return Helper.cast(name.toString(), this.resultOfChildZero(node.jjtGetChild(1), b, d));
		}
		System.err.println("Filter Error: unknown function "+ name.toString());
		return false;
	}

	@Override
	public Object evaluate(final ASTQuotedURIRef node, final Bindings b, final Map<Node, Object> d) {
		return node.getLiteral(true);
	}

	@Override
	public Object evaluate(final ASTDoubleCircumflex node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.getLiteral(true);
	}

	@Override
	public Object evaluate(final ASTFloatingPoint node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.getLiteral(true);
	}

	@Override
	public Object evaluate(final ASTisBlankFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object parameter = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if(parameter instanceof AnonymousLiteral) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean isURI_IRI(final Node node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if (o instanceof URILiteral) {
			return true;
		}
		final String text = o.toString();
		if (URILiteral.isURI(text)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Object evaluate(final ASTisURIFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return this.isURI_IRI(node, b, d);
	}

	@Override
	public Object evaluate(final ASTisIRIFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return this.isURI_IRI(node, b, d);
	}

	@Override
	public Object evaluate(final ASTInteger node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.getLiteral(true);
	}

	@Override
	public Object evaluate(final ASTLangTag node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.getLiteral(true);
	}

	@Override
	public Object evaluate(final ASTFilterConstraint node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return this.resultOfChildZero(node, b, d);
	}


	@Override
	public Object evaluate(final ASTisNumericFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		// TODO check the domain!!!
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if(o instanceof BigInteger || o instanceof Float || o instanceof Double || o instanceof BigDecimal) {
			return true;
		}
		if(o instanceof TypedLiteral){
			return Helper.isNumeric(((TypedLiteral)o).getType());
		}
		return false;
	}

	@Override
	public Object evaluate(final ASTAggregation node, final Bindings b, final Map<Node, Object> d) {
		// the result of the aggregation function has already been previously computed...
		return d.get(node);
	}

	@Override
	public Object applyAggregationCOUNT(final Iterator<Object> values) {
		long l=0;
		while(values.hasNext()){
			values.next();
			l++;
		}
		return BigInteger.valueOf(l);
	}

	@Override
	public Object applyAggregationSUM(final Iterator<Object> values) {
		Object result=BigInteger.ZERO;
		while(values.hasNext()){
			final Object next = values.next();
			try {
				result = Helper.addNumericValues(result, next);
			} catch (final TypeErrorException e) {
				// ignore...
			}
		}
		return result;
	}

	@Override
	public Object applyAggregationMIN(final Iterator<Object> values) {
		Object result = null;
		while(values.hasNext()){
			final Object next = values.next();
			try {
				if(result == null || Helper.less(next, result)) {
					result = next;
				}
			} catch (final TypeErrorException e) {
				// ignore...
			}
		}
		return result;
	}

	@Override
	public Object applyAggregationMAX(final Iterator<Object> values) {
		Object result = null;
		while(values.hasNext()){
			final Object next = values.next();
			try {
				if(result == null || Helper.greater(next, result)) {
					result = next;
				}
			} catch (final TypeErrorException e) {
				// ignore...
			}
		}
		return result;
	}


	@Override
	public Object evaluate(final ASTExists node, final Bindings bindings,
			final Map<Node, Object> d) {
		return this.processSubquery(node, bindings, d, this.collectionForExistNodes.get(node), this.evaluator);

	}

	@Override
	public Object evaluate(final ASTNotExists node, final Bindings bindings,
			final Map<Node, Object> d) {
		return !this.processSubquery(node, bindings, d, this.collectionForExistNodes.get(node), this.evaluator);

	}

	/**
	 * Checks whether the subquery represented by the {@link Root}
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
	 *            the {@link Root} for the subquery
	 * @param evaluator_param
	 *            the {@link CommonCoreQueryEvaluator} which should be used to
	 *            process the subquery
	 * @param result
	 *            the {@link Result} for the subquery
	 */
	public boolean processSubquery(final SimpleNode node, final Bindings bindings,
			final Map<Node, Object> d, final Root collection,
			final CommonCoreQueryEvaluator<Node> evaluator_param) {
		Boolean simple = this.simpleExistNodes.get(node);
		if(simple==null){
			collection.visit(new SimpleOperatorGraphVisitor() {

				@Override
				public Object visit(final BasicOperator basicOperator) {
					// exclude more complicated cases, which might lead to errors...
					// maybe too strict, must be checked again to allow more...
					if (!(basicOperator instanceof BasicIndexScan ||
							basicOperator instanceof Join ||
							basicOperator instanceof Result)){
						EvaluationVisitorImplementation.this.simpleExistNodes.put(node, false);
					}
					return null;
				}
			});
			simple = this.simpleExistNodes.get(node);
			if(simple==null){
				this.simpleExistNodes.put(node, true);
				simple = true;
			}
		}
		if(simple) {
			return processSimpleSubquery(node, bindings, d, collection, evaluator_param);
		} else {
			return this.processSubqueryAndGetWholeResult(node, bindings, d, collection, evaluator_param);
		}
	}

	/**
	 * Checks whether the subquery represented by the {@link Root}
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
	 *            the {@link Root} for the subquery
	 * @param evaluator_param
	 *            the {@link CommonCoreQueryEvaluator} which should be used to
	 *            process the subquery
	 * @param result
	 *            the {@link Result} for the subquery
	 */
	public boolean processSubqueryAndGetWholeResult(final SimpleNode node, final Bindings bindings,
			final Map<Node, Object> d, final Root collection,
			final CommonCoreQueryEvaluator<Node> evaluator_param) {

		if (!this.queryResultsForExistNodes.containsKey(node)) {
			this.performSubQueryAndGetWholeResult(node, collection, evaluator_param);
		}

		final Iterator<Bindings> bindingsSet = this.queryResultsForExistNodes.get(node)
				.iterator();

		while (bindingsSet.hasNext()) {
			final Bindings bindings2 = bindingsSet.next();
			final Set<Variable> vars = bindings.getVariableSet();
			vars.retainAll(bindings2.getVariableSet());

			boolean isEqual = true;
			for (final Variable variable : vars) {
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
			return this.result;
		}

		@Override
		public Object visit(final BasicOperator basicOperator) {
			if(basicOperator instanceof Result) {
				this.result = (Result) basicOperator;
			}
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	public static void setMaxVariables(final BasicOperator root){
		// save all variables of the subquery in the bindingsarray
		final Set<Variable> maxVariables = new TreeSet<Variable>();
		root.visit(new SimpleOperatorGraphVisitor() {
			@Override
			public Object visit(final BasicOperator basicOperator) {
				if (basicOperator.getUnionVariables() != null) {
					maxVariables.addAll(basicOperator.getUnionVariables());
				}
				return null;
			}

		});

		BindingsArray.forceVariables(maxVariables);
	}

	public static Result setupEvaluator(final CommonCoreQueryEvaluator<Node> evaluator, final Root collection){
		evaluator.setRootNode(collection);
		collection.deleteParents();
		collection.setParents();
		collection.detectCycles();
		collection.sendMessage(new BoundVariablesMessage());

		setMaxVariables(collection);

		final GetResult getResult = new GetResult();
		collection.visit(getResult);
		final Result result = getResult.getResult();

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
	 *            the {@link Root} for the subquery
	 * @param evaluator_param
	 *            the {@link CommonCoreQueryEvaluator} which should be used to
	 *            process the subquery
	 * @param result
	 *            the {@link Result} for the subquery
	 */
	@SuppressWarnings("deprecation")
	protected void performSubQueryAndGetWholeResult(final SimpleNode node,
			final Root collection,
			final CommonCoreQueryEvaluator<Node> evaluator_param) {
		final BasicOperator oldRoot = evaluator_param.getRootNode();
		final Result oldResult = evaluator_param.getResultOperator();

		// the static bindingsarray is saved and restored after the subquery
		final Map<Variable, Integer> oldVarsTmp = BindingsArray.getPosVariables();

		final Result result = setupEvaluator(evaluator_param, (Root) collection.deepClone());

		final CollectResult cr = new CollectResult(false);
		result.addApplication(cr);

		evaluator_param.logicalOptimization();
		evaluator_param.physicalOptimization();
		try {
			evaluator_param.evaluateQuery();
		} catch (final Exception e1) {
			e1.printStackTrace();
		}
		final QueryResult queryResult = cr.getResult();
		this.queryResultsForExistNodes.put(node, this.transformQueryResult(queryResult));
		BindingsArray.forceVariables(oldVarsTmp);
		evaluator_param.setRootNode(oldRoot);
		evaluator_param.setResult(oldResult);
	}

	protected QueryResult transformQueryResult(final QueryResult queryResult) {
		final QueryResult result = QueryResult.createInstance();
		if (queryResult == null) {
			return result; // empty result
		} else {

			final Iterator<Bindings> it = queryResult.oneTimeIterator();
			while (it.hasNext()) {
				final Bindings b = new BindingsMap();
				final Bindings bindings = it.next();
				b.addAll(bindings);
				result.add(b);
			}
			return result;
		}
	}

	@SuppressWarnings({ "unused", "deprecation" })
	public static boolean processSimpleSubquery(final SimpleNode node, final Bindings bindings,
			final Map<Node, Object> d, final Root collection,
			final CommonCoreQueryEvaluator<Node> evaluator) {

		final BasicOperator oldRoot = evaluator.getRootNode();
		final Result oldResult = evaluator.getResultOperator();
		// the static bindingsarray is saved and restored after the subquery
		final Map<Variable, Integer> oldVarsTmp = BindingsArray.getPosVariables();

		final Root collectionClone = (Root) collection.deepClone();
		collectionClone.visit(new SimpleOperatorGraphVisitor() {

			@Override
			public Object visit(final BasicOperator basicOperator) {

				if (basicOperator instanceof BasicIndexScan) {
					final BasicIndexScan basicIndex = (BasicIndexScan) basicOperator;
					final Collection<TriplePattern> triplePatterns = basicIndex
							.getTriplePattern();
					final Collection<TriplePattern> newTriplePatterns = new LinkedList<TriplePattern>();
					for (final TriplePattern t : triplePatterns) {
						final Item[] itemArray = t.getItems().clone();
						for (int i = 0; i < 3; i++) {
							if (itemArray[i].isVariable()) {
								final Literal literal = bindings
										.get((Variable) itemArray[i]);
								if (literal != null) {
									itemArray[i] = literal;
								}
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

		final Result result = setupEvaluator(evaluator, collectionClone);

		result.clearApplications();
		final NonEmptyApplication application = new NonEmptyApplication();
		result.addApplication(application);

		evaluator.logicalOptimization();
		evaluator.physicalOptimization();
		try {
			evaluator.evaluateQuery();
		} catch (final Exception e1) {
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
		public void call(final QueryResult res) {
			if (res.oneTimeIterator().hasNext()) {
				this.resultIsEmpty = false;
			}
		}

		@Override
		public void deleteResult() {
			// not used...
		}

		@Override
		public void deleteResult(final QueryResult res) {
			// not used...
		}

		@Override
		public void start(final Type type) {
			// not used...
		}

		@Override
		public void stop() {
			// not used...
		}
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
	public Object applyAggregationSAMPLE(final Iterator<Object> values) {
		final double r = Math.random();
		final LinkedList<Object> valueList = new LinkedList<Object>();
		while (values.hasNext()) {
			valueList.add(values.next());
		}
		final double randomValue = (valueList.size() - 1) * r;
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
	public Object applyAggregationGROUP_CONCAT(final Iterator<Object> values,
			final String separator) {
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
	public Object applyAggregationAVG(final Iterator<Object> values) {
		long count = 0;
		Object result = BigInteger.ZERO;
		while (values.hasNext()) {
			final Object next = values.next();
			try {
				result = Helper.addNumericValues(result, next);
				count++;
			} catch (final TypeErrorException e) {
				return null;
			}
		}
		try {
			return Helper.divideNumericValues(result, BigInteger.valueOf(count));
		} catch (final TypeErrorException e) {
			return null;
		}
	}

	@Override
	public Object evaluate(final ASTStrdtFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object arg0 = Helper.unlazy(this.resultOfChildZero(node, b, d));
		final Object arg1 = Helper.unlazy(this.resultOfChildOne(node, b, d));
		try {
			return LiteralFactory.createTypedLiteral("\""
					+ Helper.getSimpleString(arg0) + "\"", Helper.getString(arg1));
		} catch (final URISyntaxException e) {
			throw new TypeErrorException();
		}
	}

	@Override
	public Object evaluate(final ASTStrFuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if (o instanceof TypedLiteral) {
			return ((TypedLiteral) o).getOriginalContent();
		} else if (o instanceof LanguageTaggedLiteral) {
			return ((LanguageTaggedLiteral) o).getContentLiteral();
		}
		return o.toString();
	}

	@Override
	public Object evaluate(final ASTStrLangFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final String content = Helper.quote(Helper.getSimpleString(Helper.unlazy(this.resultOfChildZero(
				node, b, d))));
		final String language = Helper.getSimpleString(Helper.unlazy(this.resultOfChildOne(node,
				b, d)));
		return LiteralFactory.createLanguageTaggedLiteral(content, language);
	}

	@Override
	public Object evaluate(final ASTABSFuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		final Object type = Helper.getType(o);
		if (type == BigInteger.class) {
			return Helper.getInteger(o).abs();
		}
		if (type == Double.class) {
			return Math.abs(Helper.getDouble(o));
		}
		if (type == Float.class) {
			return Math.abs(Helper.getFloat(o));
		}
		if (type == BigDecimal.class) {
			return Helper.getBigDecimal(o).abs();
		}
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(final ASTCeilFuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		final Object type = Helper.getType(o);
		if (type == BigInteger.class) {
			return Helper.getInteger(o);
		}
		if (type == Double.class) {
			return Math.ceil(Helper.getDouble(o));
		}
		if (type == Float.class) {
			return Math.ceil(Helper.getFloat(o));
		}
		if (type == BigDecimal.class) {
			final BigDecimal bd = Helper.getBigDecimal(o);
			return bd.setScale(0, RoundingMode.CEILING);
		}
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(final ASTFloorFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		final Object type = Helper.getType(o);
		if (type == BigInteger.class) {
			return Helper.getInteger(o);
		}
		if (type == Double.class) {
			return Math.floor(Helper.getDouble(o));
		}
		if (type == Float.class) {
			return Math.floor(Helper.getFloat(o));
		}
		if (type == BigDecimal.class) {
			final BigDecimal bd = Helper.getBigDecimal(o);
			return bd.setScale(0, RoundingMode.FLOOR);
		}
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(final ASTRoundFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		final Object type = Helper.getType(o);
		if (type == BigInteger.class) {
			return Helper.getInteger(o);
		}
		if (type == Double.class) {
			return Math.round(Helper.getDouble(o));
		}
		if (type == Float.class) {
			return Math.round(Helper.getFloat(o));
		}
		if (type == BigDecimal.class) {
			final BigDecimal bd = Helper.getBigDecimal(o);
			return bd.setScale(0, RoundingMode.HALF_UP);
		}
		throw new TypeErrorException();
	}

	@Override
	public Object evaluate(final ASTConcatFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		if (node.jjtGetNumChildren() == 0) {
			return "";
		}
		// jump over ASTExpressionList!
		final Node child0 = node.jjtGetChild(0);
		if (child0 instanceof ASTNIL || child0.jjtGetNumChildren() == 0) {
			return "";
		}
		Object result = Helper.unlazy(child0.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(result)) {
			return null;
		}
		for (int i = 1; i < child0.jjtGetNumChildren(); i++) {

			final Object child = Helper.unlazy(child0.jjtGetChild(i).accept(this, b, d));

			if (Helper.isNumeric(child)) {
				return null;
			}

			final String concatenatedContent = "\"" + Helper.unquote(Helper.getContent(result))
					+ Helper.unquote(Helper.getContent(child)) + "\"";

			if (result instanceof TypedLiteral
					&& child instanceof TypedLiteral
					&& ((TypedLiteral) result).getTypeLiteral().equals(
							((TypedLiteral) child).getTypeLiteral())) {
				try {
					result = LiteralFactory.createTypedLiteral(
							concatenatedContent, ((TypedLiteral) result)
									.getTypeLiteral());
				} catch (final URISyntaxException e) {
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
	public Object evaluate(final ASTSubstringFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		final int start = Helper.getInteger(
				Helper.unlazy(node.jjtGetChild(1).accept(this, b, d))).intValue() - 1;
		final String content = Helper.unquote(Helper.getContent(wholeString));
		final String resultantContent;
		if (node.jjtGetNumChildren() == 2) {
			resultantContent = content.substring(start);
		} else {
			final int end = Helper.getInteger(
					Helper.unlazy(node.jjtGetChild(2).accept(this, b, d))).intValue();
			resultantContent = content.substring(start, start + end);
		}
		return Helper.createWithSameType(resultantContent, wholeString);
	}

	@Override
	public Object evaluate(final ASTStrlenFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		final String content = Helper.unquote(Helper.getContent(wholeString));
		return BigInteger.valueOf(content.length());
	}

	@Override
	public Object evaluate(final ASTUcaseFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		return Helper.createWithSameType(Helper.unquote(Helper.getContent(wholeString))
				.toUpperCase(), wholeString);
	}

	@Override
	public Object evaluate(final ASTLcaseFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		return Helper.createWithSameType(Helper.unquote(Helper.getContent(wholeString))
				.toLowerCase(), wholeString);
	}

	// @SuppressWarnings("deprecation")
	@Override
	public Object evaluate(final ASTEncodeForUriFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		try {
			return Helper.quote(URLEncoder.encode(Helper.unquote(Helper.getContent(wholeString)), "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			throw new TypeErrorException(e.getMessage());
		}
	}

	@Override
	public Object evaluate(final ASTContainsFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		final Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString)) {
			return null;
		}
		return (Helper.unquote(Helper.getContent(wholeString))
				.contains(Helper.unquote(Helper.getContent(containsString))));
	}

	@Override
	public Object evaluate(final ASTStrstartsFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		final Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString)) {
			return null;
		}
		return (Helper.unquote(Helper.getContent(wholeString))
				.startsWith(Helper.unquote(Helper.getContent(containsString))));
	}

	@Override
	public Object evaluate(final ASTStrEndsFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		final Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString)) {
			return null;
		}
		return (Helper.unquote(Helper.getContent(wholeString))
				.endsWith(Helper.unquote(Helper.getContent(containsString))));
	}

	@Override
	public Object evaluate(final ASTStrBeforeFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		final Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString)) {
			return null;
		}
		final String stringOfWholeString = Helper.unquote(Helper.getContent(wholeString));
		final int index = stringOfWholeString
				.indexOf(Helper.unquote(Helper.getContent(containsString)));
		if (index > 0) {
			return Helper.createWithSameType(stringOfWholeString.substring(0, index),
					wholeString);
		} else {
			return Helper.createWithSameType("", wholeString);
		}
	}

	@Override
	public Object evaluate(final ASTStrAfterFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object wholeString = Helper.unlazy(node.jjtGetChild(0).accept(this, b, d));
		if (Helper.isNumeric(wholeString)) {
			return null;
		}
		final Object containsString = Helper.unlazy(node.jjtGetChild(1).accept(this, b, d));
		if (Helper.isNumeric(containsString)) {
			return null;
		}
		final String stringOfWholeString = Helper.unquote(Helper.getContent(wholeString));
		final int index = stringOfWholeString
				.indexOf(Helper.unquote(Helper.getContent(containsString)));
		if (index > 0) {
			return Helper.createWithSameType(stringOfWholeString.substring(index),
					wholeString);
		} else {
			return Helper.createWithSameType("", wholeString);
		}
	}

	@Override
	public Object evaluate(final ASTMD5FuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("MD5", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(final ASTSHA1FuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("SHA1", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(final ASTSHA256FuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("SHA-256", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(final ASTSHA384FuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("SHA-384", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(final ASTSHA512FuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.applyHashFunction("SHA-512", Helper.unquote(Helper.getContent(Helper.unlazy(node
				.jjtGetChild(0).accept(this, b, d)))));
	}

	@Override
	public Object evaluate(final ASTYearFuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getYear() + 1900;
	}

	@Override
	public Object evaluate(final ASTMonthFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getMonth() + 1;
	}

	@Override
	public Object evaluate(final ASTDayFuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getDate();
	}

	@Override
	public Object evaluate(final ASTHoursFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getHours();
	}

	@Override
	public Object evaluate(final ASTMinutesFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getMinutes();
	}

	@Override
	public Object evaluate(final ASTSecondsFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return BigDecimal.valueOf(Helper.getDateAndTypeCheck(
				Helper.unlazy(node.jjtGetChild(0).accept(this, b, d))).getSeconds());
	}

	@Override
	public Object evaluate(final ASTTimeZoneFuncNode node, final Bindings b,
			final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		try {
			return LiteralFactory.createTypedLiteral(Helper
					.getTimezoneAndTypeCheck(Helper.unlazy(node.jjtGetChild(0).accept(
							this, b, d))),
					"<http://www.w3.org/2001/XMLSchema#dayTimeDuration>");
		} catch (final URISyntaxException e) {
			throw new TypeErrorException();
		}
	}

	@Override
	public Object evaluate(final ASTTzFuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return Helper.getTzAndTypeCheck(Helper.unlazy(node.jjtGetChild(0).accept(this,
				b, d)));
	}

	private static Date now = new Date();

	public static void resetNowDate(){
		now = new Date();
	}

	@Override
	public Object evaluate(final ASTNowFuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return now;
	}

	@Override
	public Object evaluate(final ASTUUIDFuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		try {
			return LiteralFactory.createURILiteral("<urn:uuid:"+ UUID.randomUUID().toString() + ">");
		} catch (final URISyntaxException e) {
			throw new TypeErrorException(e.getMessage());
		}
	}

	@Override
	public Object evaluate(final ASTSTRUUIDFuncNode node, final Bindings b, final Map<Node, Object> d)
			throws NotBoundException, TypeErrorException {
		return "\"" + UUID.randomUUID().toString() + "\"";
	}

	@Override
	public Object evaluate(final ASTInNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
	    return this.handleInAndNotIn(node, b, d);
	}

	@Override
	public Object evaluate(final ASTNotInNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
	    return !this.handleInAndNotIn(node, b, d);
	}

	protected boolean handleInAndNotIn(final Node node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object arg1 = Helper.unlazy(this.resultOfChildZero(node, b, d));
		final Node child1 = node.jjtGetChild(1);
		for(int i=0; i<child1.jjtGetNumChildren(); i++) {
			final Object arg2 = Helper.unlazy(child1.jjtGetChild(i).accept(this, b, d));
			if(Helper.equals(arg1, arg2)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object evaluate(final ASTRandFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
	    return new Double(Math.random());
	}

	@Override
	public Object evaluate(final ASTIfFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object arg0 = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if(Helper.booleanEffectiveValue(arg0)){
			return this.resultOfChildOne(node, b, d);
		} else {
			return this.resultOfChildTwo(node, b, d);
		}
	}

	@Override
	public Object evaluate(final ASTCoalesceFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Node child0 = node.jjtGetChild(0);
		for(int i=0; i<child0.jjtGetNumChildren(); i++){
			try {
				return Helper.unlazy(child0.jjtGetChild(i).accept(this, b, d));
			} catch(final Error e){
				// ignore...
			} catch(final Exception e){
				// ignore...
			}
		}
		return null;
	}

	@Override
	public Object evaluate(final ASTStrReplaceFuncNode node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		final Object o = Helper.unlazy(this.resultOfChildZero(node, b, d));
		if (o instanceof URILiteral || o instanceof AnonymousLiteral || Helper.isNumeric(o)) {
			throw new TypeErrorException();
		}
		final String cmp = Helper.unquote(Helper.getContent(o));
		String pattern = Helper.getString(this.resultOfChildOne(node, b, d));
		final String replacement = Helper.getString(this.resultOfChildTwo(node, b, d));
		String oldPattern;
		do {
			oldPattern = pattern;
			pattern = pattern.replace("\\\\", "\\");
		} while (oldPattern.compareTo(pattern) != 0);
		String flags = "";
		if (node.jjtGetNumChildren() > 3) {
			flags = Helper.getString(this.resultOfChildThree(node, b, d));
		}
		// return match(cmp,pattern,flags); // does not support flag x!!!
		return Helper.createWithSameType(Helper.replaceXerces(cmp, pattern, flags, replacement), o);
	}

	protected Object resultOfChildThree(final Node node, final Bindings b, final Map<Node, Object> d) throws NotBoundException, TypeErrorException {
		return node.jjtGetChild(3).accept(this, b, d);
	}
}