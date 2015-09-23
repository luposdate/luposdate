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
package lupos.sparql1_1.operatorgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.minus.Minus;
import lupos.engine.operators.multiinput.minus.SortedMinus;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.engine.operators.singleinput.AddComputedBinding;
import lupos.engine.operators.singleinput.Bind;
import lupos.engine.operators.singleinput.ComputeBindings;
import lupos.engine.operators.singleinput.Construct;
import lupos.engine.operators.singleinput.Group;
import lupos.engine.operators.singleinput.GroupByAddComputedBinding;
import lupos.engine.operators.singleinput.Having;
import lupos.engine.operators.singleinput.MakeBooleanResult;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.filter.Filter;
import lupos.engine.operators.singleinput.modifiers.Limit;
import lupos.engine.operators.singleinput.modifiers.Offset;
import lupos.engine.operators.singleinput.modifiers.distinct.Distinct;
import lupos.engine.operators.singleinput.modifiers.distinct.InMemoryDistinct;
import lupos.engine.operators.singleinput.path.Closure;
import lupos.engine.operators.singleinput.path.PathLengthZero;
import lupos.engine.operators.singleinput.sort.Sort;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.generated.CorrectOperatorgraphRulePackage;
import lupos.sparql1_1.ASTAdditionNode;
import lupos.sparql1_1.ASTAggregation;
import lupos.sparql1_1.ASTArbitraryOccurences;
import lupos.sparql1_1.ASTArbitraryOccurencesNotZero;
import lupos.sparql1_1.ASTAs;
import lupos.sparql1_1.ASTAskQuery;
import lupos.sparql1_1.ASTBind;
import lupos.sparql1_1.ASTBindings;
import lupos.sparql1_1.ASTConstructQuery;
import lupos.sparql1_1.ASTConstructTemplate;
import lupos.sparql1_1.ASTDefaultGraph;
import lupos.sparql1_1.ASTDivisionNode;
import lupos.sparql1_1.ASTExists;
import lupos.sparql1_1.ASTFilterConstraint;
import lupos.sparql1_1.ASTGraphConstraint;
import lupos.sparql1_1.ASTGroup;
import lupos.sparql1_1.ASTGroupConstraint;
import lupos.sparql1_1.ASTHaving;
import lupos.sparql1_1.ASTInvers;
import lupos.sparql1_1.ASTLimit;
import lupos.sparql1_1.ASTMinus;
import lupos.sparql1_1.ASTMultiplicationNode;
import lupos.sparql1_1.ASTNamedGraph;
import lupos.sparql1_1.ASTNegatedPath;
import lupos.sparql1_1.ASTNotExists;
import lupos.sparql1_1.ASTObjectList;
import lupos.sparql1_1.ASTOffset;
import lupos.sparql1_1.ASTOptionalConstraint;
import lupos.sparql1_1.ASTOptionalOccurence;
import lupos.sparql1_1.ASTOrderConditions;
import lupos.sparql1_1.ASTPathAlternative;
import lupos.sparql1_1.ASTPathSequence;
import lupos.sparql1_1.ASTPlusNode;
import lupos.sparql1_1.ASTQuery;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTSelectQuery;
import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.ASTStream;
import lupos.sparql1_1.ASTSubtractionNode;
import lupos.sparql1_1.ASTTripleSet;
import lupos.sparql1_1.ASTUndef;
import lupos.sparql1_1.ASTUnionConstraint;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.ASTWindow;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.SimpleNode;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreator_BasicIndex;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;
public abstract class SPARQLCoreParserVisitorImplementation implements
		SPARQL1_1OperatorgraphGeneratorVisitor {

	protected Result result;
	protected int var = 0;
	/** Constant <code>useSortedMinus=true</code> */
	protected static boolean useSortedMinus = true;
	protected CommonCoreQueryEvaluator<Node> evaluator;
	protected IndexScanCreatorInterface indexScanCreator;

	/** Constant <code>serviceGeneratorClass</code> */
	public static Class<? extends ServiceGenerator> serviceGeneratorClass = ServiceGenerator.class;
	protected ServiceGenerator serviceGenerator;

	/**
	 * specifies whether or not property paths (...)* and (...)+ are expressed by using the Closure operator, (...)? and (...)* by using the PathLengthZero operator!
	 */
	public static boolean USE_CLOSURE_AND_PATHLENGTHZERO_OPERATORS = true;

	/**
	 * <p>getVariable.</p>
	 *
	 * @param subject a {@link java.lang.String} object.
	 * @param object a {@link java.lang.String} object.
	 * @param variable a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.Variable} object.
	 */
	protected Variable getVariable(String subject, String object, String variable){
		while(subject.startsWith("?") || subject.startsWith("$")) {
			subject=subject.substring(1);
		}
		while(object.startsWith("?") || object.startsWith("$")) {
			object=object.substring(1);
		}
		while(variable.startsWith("?") || variable.startsWith("$")) {
			variable=variable.substring(1);
		}
		String newVariable=variable+this.var;
		while(newVariable.equals(subject) || newVariable.equals(object) ){
			this.var++;
			newVariable=variable+this.var;
		}
		return new Variable(newVariable);
	}

	/**
	 * <p>Getter for the field <code>result</code>.</p>
	 *
	 * @return a {@link lupos.engine.operators.singleinput.Result} object.
	 */
	public Result getResult() {
		return this.result;
	}

	/**
	 * <p>getOperatorgraphRoot.</p>
	 *
	 * @return a {@link lupos.engine.operators.BasicOperator} object.
	 */
	public BasicOperator getOperatorgraphRoot(){
		return this.indexScanCreator.getRoot();
	}

	/**
	 * <p>Constructor for SPARQLCoreParserVisitorImplementation.</p>
	 */
	public SPARQLCoreParserVisitorImplementation() {
		this.result = new Result();
		try {
			this.serviceGenerator = serviceGeneratorClass.newInstance();
		} catch (final InstantiationException e) {
			this.serviceGenerator = new ServiceGenerator();
			System.err.println();
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			this.serviceGenerator = new ServiceGenerator();
			System.err.println();
			e.printStackTrace();
		}
	}

	/**
	 * <p>setIndexScanGenerator.</p>
	 *
	 * @param indexScanCreator a {@link lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface} object.
	 */
	public void setIndexScanGenerator(final IndexScanCreatorInterface indexScanCreator){
		this.indexScanCreator = indexScanCreator;
	}

	/**
	 * <p>getIndexScanGenerator.</p>
	 *
	 * @return a {@link lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface} object.
	 */
	public IndexScanCreatorInterface getIndexScanGenerator(){
		return this.indexScanCreator;
	}

	/**
	 * <p>visitChildrenWithoutStringConcatenation.</p>
	 *
	 * @param n a {@link lupos.sparql1_1.Node} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 */
	public void visitChildrenWithoutStringConcatenation(final Node n, final OperatorConnection connection) {
		for (int i = 0; i < n.jjtGetNumChildren(); i++) {
			n.jjtGetChild(i).accept(this, connection);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTConstructQuery node, final OperatorConnection connection) {
		// Dealing with the STREAM clause
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (node.jjtGetChild(i) instanceof ASTStream) {
				this.visit((ASTStream)node.jjtGetChild(i));
			}
		}

		int index;
		final Node child0 = node.jjtGetChild(0);
		if(child0 instanceof ASTConstructTemplate){
			this.setGraphConstraints(child0, connection);
			index=1;
		} else {
			index=0;
			for (int i = index; i < node.jjtGetNumChildren(); i++) {
				final Node childi = node.jjtGetChild(i);
				if(childi instanceof ASTGroupConstraint) {
					this.setGraphConstraints(childi, connection);
				}
			}
		}
		for (int i = index; i < node.jjtGetNumChildren(); i++) {
			final Node childi = node.jjtGetChild(i);
			if(childi instanceof ASTGroupConstraint) {
				childi.accept(this, connection, null);
			} else {
				if(!(childi instanceof ASTStream)){
					childi.accept(this, connection);
				}
			}
		}
	}

	/**
	 * <p>setGraphConstraints.</p>
	 *
	 * @param constructTemplate a {@link lupos.sparql1_1.Node} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 */
	protected void setGraphConstraints(final Node constructTemplate,final OperatorConnection connection){
		final LinkedList<Tuple<Construct, Item>> graphConstraints = this.getGraphConstructs(constructTemplate);
		// there should be only one graphConstraint (for "default graph")!
		for(final Tuple<Construct, Item> entry: graphConstraints){
			connection.connectAndSetAsNewOperatorConnection(entry.getFirst());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTAskQuery node, final OperatorConnection connection) {
		connection.connectAndSetAsNewOperatorConnection(new MakeBooleanResult());

		this.visitQueryNode(node, connection, null);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTOrderConditions node, final OperatorConnection connection) {
		connection.connectAndSetAsNewOperatorConnection(new Sort(node));
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTLimit node, final OperatorConnection connection) {
		connection.connectAndSetAsNewOperatorConnection(new Limit(node.getLimit()));
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTOffset node, final OperatorConnection connection) {
		connection.connectAndSetAsNewOperatorConnection(new Offset(node.getOffset()));
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTOptionalConstraint node, final OperatorConnection connection, final Item graphConstraint) {
		node.jjtGetChild(0).accept(this, connection, graphConstraint);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTUnionConstraint node, final OperatorConnection connection, final Item graphConstraint) {
		final Union union = new Union();
		connection.connectAndSetAsNewOperatorConnection(union);
		node.jjtGetChild(0).accept(this, connection, graphConstraint);
		connection.setOperatorConnection(union,1);
		node.jjtGetChild(1).accept(this, connection, graphConstraint);
	}

	/**
	 * <p>getGraphConstructs.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.Node} object.
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<Tuple<Construct, Item>> getGraphConstructs(final Node node) {
		final LinkedList<Tuple<Construct, Item>> graphConstructs = new LinkedList<Tuple<Construct, Item>>();
		final Collection<TriplePattern> operators = this.collectTriplePatternOfChildren(node);
		if(operators.size()>0){
			final Construct c = new Construct();
			c.setTemplates(operators);
			graphConstructs.add(new Tuple<Construct, Item>(c, null));
		}
		for(int i=0; i<node.jjtGetNumChildren();i++){
			final Node child = node.jjtGetChild(i);
			if(child instanceof ASTGraphConstraint){
				final Collection<TriplePattern> otp = this.collectTriplePatternOfChildren(child);
				if(otp.size()>0){
					final Construct c2 = new Construct();
					c2.setTemplates(otp);
					final Node childchild=child.jjtGetChild(0);
					Item item=null;
					if (childchild instanceof ASTQuotedURIRef) {
						try {
							item=LiteralFactory.createURILiteralWithoutLazyLiteral("<"+ childchild.toString() + ">");
						} catch (final Exception e) {
							System.err.println(e);
						}
					} else if(childchild instanceof ASTVar){
						item = new Variable(((ASTVar)childchild).getName());
					}
					graphConstructs.add(new Tuple<Construct, Item>(c2, item));
				}
			}
		}
		return graphConstructs;
	}

	/**
	 * <p>collectTriplePatternOfChildren.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.Node} object.
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<TriplePattern> collectTriplePatternOfChildren(final Node node){
		final Collection<TriplePattern> resultantTriplePatterns = new LinkedList<TriplePattern>();
		for(int i=0; i<node.jjtGetNumChildren(); i++){
			final Node child = node.jjtGetChild(i);
			if(child instanceof ASTTripleSet){
				resultantTriplePatterns.add(this.getTriplePattern((ASTTripleSet)child));
			}
		}
		return resultantTriplePatterns;
	}

	/**
	 * <p>getItem.</p>
	 *
	 * @param n a {@link lupos.sparql1_1.Node} object.
	 * @return a {@link lupos.datastructures.items.Item} object.
	 */
	public static Item getItem(final Node n) {
		if(n instanceof ASTObjectList) {
			return getItem(n.jjtGetChild(0));
		}
		if (n instanceof ASTVar) {
			final ASTVar var = (ASTVar) n;
			final String name = var.getName();
			return new Variable(name);
		} else {
			return LazyLiteral.getLiteral(n, true);
		}
	}

	/**
	 * <p>getTriplePattern.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTTripleSet} object.
	 * @return a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 */
	public TriplePattern getTriplePattern(final ASTTripleSet node) {
		final Item[] item = { null, null, null };

		for (int i = 0; i < 3; i++) {
			final Node n = node.jjtGetChild(i);
			if(n instanceof ASTObjectList){
				item[i] = getItem(n.jjtGetChild(0));
			} else {
				item[i] = getItem(n);
			}
		}
		final TriplePattern tpe = new TriplePattern(item[0], item[1], item[2]);
		return tpe;
	}

	/**
	 * <p>isHigherConstructToJoin.</p>
	 *
	 * @param n a {@link lupos.sparql1_1.Node} object.
	 * @return a boolean.
	 */
	protected boolean isHigherConstructToJoin(final Node n){
		return (n instanceof ASTUnionConstraint
				|| n instanceof ASTGraphConstraint
				|| n instanceof ASTService
				|| n instanceof ASTGroupConstraint
				|| n instanceof ASTWindow
				|| n instanceof ASTSelectQuery
				|| n instanceof ASTBindings);
	}

	/**
	 * <p>handleHigherConstructToJoin.</p>
	 *
	 * @param n a {@link lupos.sparql1_1.Node} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 * @param graphConstraint a {@link lupos.datastructures.items.Item} object.
	 * @return a boolean.
	 */
	protected boolean handleHigherConstructToJoin(final Node n, final OperatorConnection connection, final Item graphConstraint){
		if (this.isHigherConstructToJoin(n)) {
			if(n instanceof ASTService){
				final ASTService service = (ASTService) n;
				if(this.serviceGenerator.countsAsJoinPartner(service)){
					this.serviceGenerator.insertIndependantFederatedQueryOperator(service, connection, this.indexScanCreator);
					return true;
				} else {
					return false;
				}
			} else if(n instanceof ASTGraphConstraint){
				n.accept(this, connection);
			} else if(n instanceof ASTBindings){
				final ComputeBindings computeBindings = new ComputeBindings(this.getQueryResultFromValuesClause((ASTBindings)n));

				this.indexScanCreator.createEmptyIndexScanAndConnectWithRoot(new OperatorIDTuple(computeBindings, 0));

				connection.connect(computeBindings);
			} else {
				n.accept(this, connection, graphConstraint);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>createMultipleOccurence.</p>
	 *
	 * @param tripleSet a {@link lupos.sparql1_1.ASTTripleSet} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 * @param graphConstraint a {@link lupos.datastructures.items.Item} object.
	 */
	protected void createMultipleOccurence(final ASTTripleSet tripleSet, final OperatorConnection connection, final Item graphConstraint) {
		if(USE_CLOSURE_AND_PATHLENGTHZERO_OPERATORS){
			try{
				Variable subject;
				Variable object;
				Item realSubject = null;
				Item realObject = null;
				boolean subjectIsALiteral = false;
				boolean objectIsALiteral = false;
				Item itm = getItem(tripleSet.jjtGetChild(0));
				if (!itm.isVariable()){
					subject = this.getVariable(getItem(tripleSet.jjtGetChild(0)).toString(), getItem(tripleSet.jjtGetChild(2)).toString(), "interimSubject");
					realSubject = itm;
					subjectIsALiteral = true;
				} else {
					subject = (Variable) itm;
				}
				final Node subjectNode = tripleSet.jjtGetChild(0);

				itm = getItem(tripleSet.jjtGetChild(2));
				if (!itm.isVariable()){
					object = this.getVariable(getItem(tripleSet.jjtGetChild(0)).toString(), getItem(tripleSet.jjtGetChild(2)).toString(), "interimObject");
					realObject = itm;
					objectIsALiteral = true;
				} else {
					object = (Variable) itm;
				}
				final Node objectNode = tripleSet.jjtGetChild(2);


				final Node predicateNode = tripleSet.jjtGetChild(1);
				final BasicOperator startingOperator = predicateNode.accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);

				if(!subjectIsALiteral && !objectIsALiteral){
					startingOperator.addSucceedingOperator(connection.getOperatorIDTuple());
				}
				else
					if(subjectIsALiteral && !objectIsALiteral){
						final Filter filter = new Filter("(" + subject + " = " + realSubject +")");
						final Projection projection = new Projection();
						projection.addProjectionElement(object);
						if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
							projection.addProjectionElement((Variable)graphConstraint);
						}

						filter.addSucceedingOperator(new OperatorIDTuple(projection,0));
						projection.addSucceedingOperator(connection.getOperatorIDTuple());
						startingOperator.addSucceedingOperator(new OperatorIDTuple(filter,0));

					}
					else
						if(!subjectIsALiteral && objectIsALiteral){
							final Filter filter = new Filter("(" + object + " = " + realObject + ")");
							final Projection projection = new Projection();
							projection.addProjectionElement(subject);
							if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
								projection.addProjectionElement((Variable)graphConstraint);
							}

							filter.addSucceedingOperator(new OperatorIDTuple(projection,0));
							projection.addSucceedingOperator(connection.getOperatorIDTuple());
							startingOperator.addSucceedingOperator(new OperatorIDTuple(filter,0));

						}
						else
							if(subjectIsALiteral && objectIsALiteral){
								final Filter firstFilter = new Filter("(" + object + " = " + realObject + ")");
								final Filter secondFilter = new Filter("(" + subject + " = " + realSubject + ")");
								final Projection firstProjection = new Projection();
								firstProjection.addProjectionElement(subject);
								if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
									firstProjection.addProjectionElement((Variable)graphConstraint);
								}
								final Projection secondProjection = new Projection();
								secondProjection.addProjectionElement(object);
								if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
									secondProjection.addProjectionElement((Variable)graphConstraint);
								}

								firstFilter.addSucceedingOperator(new OperatorIDTuple(firstProjection,0));
								firstProjection.addSucceedingOperator(new OperatorIDTuple(secondFilter, 0));
								secondFilter.addSucceedingOperator(new OperatorIDTuple(secondProjection,0));
								secondProjection.addSucceedingOperator(connection.getOperatorIDTuple());
								startingOperator.addSucceedingOperator(new OperatorIDTuple(firstFilter,0));
							}
			}
			catch( final Exception e){
				e.printStackTrace();
				System.out.println(e);
			}
		} else {
			// alternative way to evaluate (...)?, (...)* and (...)+ without using the Closure and PathLengthZero operators!
			try{
				Variable subject;
				Variable object;
				Item realSubject = null;
				Item realObject = null;
				boolean subjectIsALiteral = false;
				boolean objectIsALiteral = false;
				Item itm = getItem(tripleSet.jjtGetChild(0));
				if (!itm.isVariable()){
					subject = this.getVariable(getItem(tripleSet.jjtGetChild(0)).toString(), getItem(tripleSet.jjtGetChild(2)).toString(), "interimSubject");
					realSubject = itm;
					subjectIsALiteral = true;
				} else {
					subject = (Variable) itm;
				}
				final Node subjectNode = tripleSet.jjtGetChild(0);
				itm = getItem(tripleSet.jjtGetChild(2));
				if (!itm.isVariable()){
					object = this.getVariable(getItem(tripleSet.jjtGetChild(0)).toString(), getItem(tripleSet.jjtGetChild(2)).toString(), "interimObject");
					realObject = itm;
					objectIsALiteral = true;
				} else {
					object = (Variable) itm;
				}
				final Node objectNode = tripleSet.jjtGetChild(2);
				final ReplaceVar replaceVar = new ReplaceVar();
				replaceVar.addSubstitution(object, subject);
				final Variable variable = this.getVariable(subject.toString(), object.toString(), "interimVariable");
				replaceVar.addSubstitution(variable, object);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
					replaceVar.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
				}
				final ReplaceVar replaceVari = new ReplaceVar();
				replaceVari.addSubstitution(subject, subject);
				replaceVari.addSubstitution(object, variable);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
					replaceVari.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
				}

				final BasicOperator startingOperator =tripleSet.jjtGetChild(1).jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);

				final InMemoryDistinct memoryDistinct = new InMemoryDistinct();
				final Filter filter = new Filter("(" + subject + " != " + object + ")");

				startingOperator.addSucceedingOperator(new OperatorIDTuple(filter,0));
				startingOperator.addSucceedingOperator(connection.getOperatorIDTuple());
				final Join intermediateJoinOperator = new Join();
				replaceVar.addSucceedingOperator(new OperatorIDTuple(memoryDistinct,0));
				memoryDistinct.addSucceedingOperator(new OperatorIDTuple(intermediateJoinOperator,1));
				filter.addSucceedingOperator(new OperatorIDTuple(intermediateJoinOperator,0));
				filter.addSucceedingOperator(new OperatorIDTuple(replaceVar,0));
				intermediateJoinOperator.addSucceedingOperator(new OperatorIDTuple(replaceVari,0));
				replaceVari.addSucceedingOperator(new OperatorIDTuple(replaceVar,0));
				replaceVari.addSucceedingOperator(connection.getOperatorIDTuple());

				if(subjectIsALiteral && !objectIsALiteral){
					final Filter firstFilter = new Filter("(" + subject + " = " + realSubject +")");
					final Filter secondFilter = new Filter("(" + subject + " = " + realSubject +")");
					final Projection firstProjection = new Projection();
					firstProjection.addProjectionElement(object);
					if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
						firstProjection.addProjectionElement((Variable)graphConstraint);
					}
					final Projection secondProjection = new Projection();
					secondProjection.addProjectionElement(object);
					if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
						secondProjection.addProjectionElement((Variable)graphConstraint);
					}

					firstFilter.addSucceedingOperator(new OperatorIDTuple(firstProjection,0));
					secondFilter.addSucceedingOperator(new OperatorIDTuple(secondProjection,0));

					firstProjection.addSucceedingOperator(connection.getOperatorIDTuple());
					secondProjection.addSucceedingOperator(connection.getOperatorIDTuple());

					replaceVari.addSucceedingOperator(new OperatorIDTuple(secondFilter,0));
					replaceVari.removeSucceedingOperator(connection.getOperatorIDTuple().getOperator());

					startingOperator.addSucceedingOperator(new OperatorIDTuple(firstFilter,0));
					startingOperator.removeSucceedingOperator(connection.getOperatorIDTuple().getOperator());

				}

				if(!subjectIsALiteral && objectIsALiteral){
					final Filter firstFilter = new Filter("(" + object + " = " + realObject + ")");
					final Filter secondFilter = new Filter("(" + object + " = " + realObject + ")");
					final Projection firstProjection = new Projection();
					firstProjection.addProjectionElement(subject);
					if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
						firstProjection.addProjectionElement((Variable)graphConstraint);
					}
					final Projection secondProjection = new Projection();
					secondProjection.addProjectionElement(subject);
					if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
						secondProjection.addProjectionElement((Variable)graphConstraint);
					}

					firstFilter.addSucceedingOperator(new OperatorIDTuple(firstProjection,0));
					secondFilter.addSucceedingOperator(new OperatorIDTuple(secondProjection,0));

					firstProjection.addSucceedingOperator(connection.getOperatorIDTuple());
					secondProjection.addSucceedingOperator(connection.getOperatorIDTuple());

					replaceVari.addSucceedingOperator(new OperatorIDTuple(secondFilter,0));
					replaceVari.removeSucceedingOperator(connection.getOperatorIDTuple().getOperator());

					startingOperator.addSucceedingOperator(new OperatorIDTuple(firstFilter,0));
					startingOperator.removeSucceedingOperator(connection.getOperatorIDTuple().getOperator());

				}
				if(subjectIsALiteral && objectIsALiteral){
					final Filter firstFilter = new Filter("(" + object + " = " + realObject + ")");
					final Filter secondFilter = new Filter("(" + subject + " = " + realSubject + ")");
					final Filter thirdFilter = new Filter("(" + object + " = " + realObject + ")");
					final Filter fourthFilter = new Filter("(" + subject + " = " + realSubject + ")");
					final Projection firstProjection = new Projection();
					firstProjection.addProjectionElement(subject);
					if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
						firstProjection.addProjectionElement((Variable)graphConstraint);
					}
					final Projection secondProjection = new Projection();
					secondProjection.addProjectionElement(object);
					if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
						secondProjection.addProjectionElement((Variable)graphConstraint);
					}
					final Projection thirdProjection = new Projection();
					thirdProjection.addProjectionElement(subject);
					if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
						thirdProjection.addProjectionElement((Variable)graphConstraint);
					}
					final Projection fourthProjection = new Projection();
					fourthProjection.addProjectionElement(object);
					if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
						fourthProjection.addProjectionElement((Variable)graphConstraint);
					}

					firstFilter.addSucceedingOperator(new OperatorIDTuple(firstProjection,0));
					secondFilter.addSucceedingOperator(new OperatorIDTuple(secondProjection,0));
					thirdFilter.addSucceedingOperator(new OperatorIDTuple(thirdProjection,0));
					fourthFilter.addSucceedingOperator(new OperatorIDTuple(fourthProjection,0));

					firstProjection.addSucceedingOperator(new OperatorIDTuple(secondFilter, 0));
					secondProjection.addSucceedingOperator(connection.getOperatorIDTuple());
					thirdProjection.addSucceedingOperator(new OperatorIDTuple(fourthFilter, 0));
					fourthProjection.addSucceedingOperator(connection.getOperatorIDTuple());

					replaceVari.addSucceedingOperator(new OperatorIDTuple(thirdFilter,0));
					replaceVari.removeSucceedingOperator(connection.getOperatorIDTuple().getOperator());

					startingOperator.addSucceedingOperator(new OperatorIDTuple(firstFilter,0));
					startingOperator.removeSucceedingOperator(connection.getOperatorIDTuple().getOperator());
				}
			}
			catch( final Exception e){
				e.printStackTrace();
				System.out.println(e);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator visit(final ASTOptionalOccurence node,
			final OperatorConnection connection, final Item graphConstraint,
			final Variable subject, final Variable object, final Node subjectNode, final Node objectNode) {
		if(USE_CLOSURE_AND_PATHLENGTHZERO_OPERATORS){
			Node predicateNode = node.jjtGetChild(0);
			while (predicateNode instanceof ASTOptionalOccurence){
				predicateNode = predicateNode.jjtGetChild(0);
			}
			final BasicOperator startingOperator = predicateNode.accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);

			final Item[] items = {subject,this.getVariable(subject.toString(),object.toString(),"predicate"),object};
			final TriplePattern tp = new TriplePattern(items);
			final LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
			temp.add(tp);
			final BasicOperator memoryScan = this.indexScanCreator.createIndexScanAndConnectWithRoot(null, temp, graphConstraint);

			Set<Literal> allowedObjects = null;
			Set<Literal> allowedSubjects = null;

			if(node.jjtGetParent() instanceof ASTTripleSet){
				// If there is a fixed subject or object, then we should consider it during evaluation!
				allowedSubjects = this.getSetOfLiterals(subjectNode);
				allowedObjects = this.getSetOfLiterals(objectNode);
			}

			final PathLengthZero zeroOperator = new PathLengthZero(subject, object, allowedSubjects, allowedObjects);
			memoryScan.addSucceedingOperator(new OperatorIDTuple(zeroOperator,0));

			final Union union = new Union();
			zeroOperator.addSucceedingOperator(new OperatorIDTuple(union,0));
			startingOperator.addSucceedingOperator(new OperatorIDTuple(union,1));

			final InMemoryDistinct distinct = new InMemoryDistinct();
			union.addSucceedingOperator(new OperatorIDTuple(distinct,0));

			return distinct;
		} else {
			// alternative way to evaluate (...)? without using the PathLengthZero operator!

			final BasicOperator startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
			final Union union = new Union();

			final BasicOperator leftSide = this.zeroPath(node, graphConstraint, subject, object, subjectNode, objectNode);
			leftSide.addSucceedingOperator(new OperatorIDTuple(union,0));

			final Projection projection = new Projection();
			projection.addProjectionElement(subject);
			projection.addProjectionElement(object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
				projection.addProjectionElement((Variable)graphConstraint);
			}

			startingOperator.addSucceedingOperator(new OperatorIDTuple(union,1));

			union.addSucceedingOperator(new OperatorIDTuple(projection,0));
			return projection;
		}
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator visit(final ASTArbitraryOccurences node,
			final OperatorConnection connection, final Item graphConstraint,
			final Variable subject, final Variable object, final Node subjectNode, final Node objectNode) {
		if(USE_CLOSURE_AND_PATHLENGTHZERO_OPERATORS){
			Node predicateNode = node.jjtGetChild(0);
			while (predicateNode instanceof ASTArbitraryOccurences ||
					predicateNode instanceof ASTArbitraryOccurencesNotZero){
				predicateNode = predicateNode.jjtGetChild(0);
			}
			final BasicOperator startingOperator = predicateNode.accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
			// System.out.println(startingOperator);

			Set<Literal> allowedObjects = null;
			Set<Literal> allowedSubjects = null;

			if(node.jjtGetParent() instanceof ASTTripleSet){
				// If there is a fixed subject or object, then we should consider it during evaluation!
				allowedSubjects = this.getSetOfLiterals(subjectNode);
				allowedObjects = this.getSetOfLiterals(objectNode);
			}

			final Closure closure = new Closure(subject, object, allowedSubjects, allowedObjects);
			startingOperator.addSucceedingOperator(new OperatorIDTuple(closure,0));

			final Item[] items = {subject,this.getVariable(subject.toString(),object.toString(),"predicate"),object};
			final TriplePattern tp = new TriplePattern(items);
			final LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
			temp.add(tp);
			final BasicOperator memoryScan = this.indexScanCreator.createIndexScanAndConnectWithRoot(null, temp, graphConstraint);

			final PathLengthZero zeroOperator = new PathLengthZero(subject, object, allowedSubjects, allowedObjects);
			memoryScan.addSucceedingOperator(new OperatorIDTuple(zeroOperator,0));

			final Union union = new Union();
			zeroOperator.addSucceedingOperator(new OperatorIDTuple(union,0));
			startingOperator.removeSucceedingOperator(closure);
			startingOperator.addSucceedingOperator(new OperatorIDTuple(union,1));
			union.addSucceedingOperator(new OperatorIDTuple(closure,0));

			return closure;
		} else {
			// alternative way to evaluate (...)* without using the Closure and PathLengthZero operators!

			// Plus Operator
			final BasicOperator startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);

			final Projection projection = new Projection();
			projection.addProjectionElement(subject);
			projection.addProjectionElement(object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
				projection.addProjectionElement((Variable)graphConstraint);
			}

			final Union union = new Union();
			final InMemoryDistinct memoryDistinct = new InMemoryDistinct();
			try {
				final Filter filter = new Filter("(" + subject + " != " + object + ")");

				final ReplaceVar replaceVar = new ReplaceVar();
				replaceVar.addSubstitution(object, subject);
				final Variable variable = this.getVariable(subject.toString(), object.toString(), "interimVariable");
				replaceVar.addSubstitution(variable, object);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
					replaceVar.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
				}
				final ReplaceVar replaceVari = new ReplaceVar();
				replaceVari.addSubstitution(subject, subject);
				replaceVari.addSubstitution(object, variable);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
					replaceVari.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
				}

				startingOperator.addSucceedingOperator(new OperatorIDTuple(filter,0));
				startingOperator.addSucceedingOperator(new OperatorIDTuple(union,1));
				final Join intermediateJoinOperator = new Join();
				replaceVar.addSucceedingOperator(new OperatorIDTuple(memoryDistinct,0));
				memoryDistinct.addSucceedingOperator(new OperatorIDTuple(intermediateJoinOperator,1));
				filter.addSucceedingOperator(new OperatorIDTuple(intermediateJoinOperator,0));
				filter.addSucceedingOperator(new OperatorIDTuple(replaceVar,0));
				intermediateJoinOperator.addSucceedingOperator(new OperatorIDTuple(replaceVari,0));
				replaceVari.addSucceedingOperator(new OperatorIDTuple(replaceVar,0));
				replaceVari.addSucceedingOperator(new OperatorIDTuple(union,1));
				union.addSucceedingOperator(new OperatorIDTuple(projection,0));

				//Zero Operator
				final BasicOperator startingPoint = this.zeroPath(node, graphConstraint, subject, object, subjectNode, objectNode);

				startingPoint.addSucceedingOperator(new OperatorIDTuple(union,0));

			} catch (final ParseException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			return projection;
		}
	}

	private BasicOperator zeroPath(final Node node, final Item graphConstraint, final Variable subject, final Variable object, final Node subjectNode, final Node objectNode) {
		if (!getItem(subjectNode).isVariable() && !getItem(objectNode).isVariable()){
			final Projection projection = new Projection();
			projection.addProjectionElement(subject);
			projection.addProjectionElement(object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
				projection.addProjectionElement((Variable)graphConstraint);
			}
			// TODO consider graphConstraint!
			this.indexScanCreator.createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(new OperatorIDTuple(projection,0), graphConstraint);
			return projection;
		} else if (!getItem(subjectNode).isVariable()){
			final Bind firstBind = new Bind(object);
			firstBind.addProjectionElement(object, subjectNode);
			// TODO consider graphConstraint!
			this.indexScanCreator.createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(new OperatorIDTuple(firstBind,0), graphConstraint);
			return firstBind;
		} else if(!getItem(objectNode).isVariable()){
			final LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
			final Item[] items = {subject, getItem(node.jjtGetChild(0)), object};
			temp.add(new TriplePattern(items));
			final Bind firstBind = new Bind(subject);
			firstBind.addProjectionElement(subject, objectNode);
			// TODO consider graphConstraint!
			this.indexScanCreator.createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(new OperatorIDTuple(firstBind,0), graphConstraint);
			return firstBind;
		} else {
			final Union union = new Union();
			final Variable intermediatePredicate = this.getVariable(subject.toString(),object.toString(),"intermediatePredicate");
			final Variable intermediateObject = this.getVariable(subject.toString(),object.toString(),"intermediateObject");
			final Item[] items = {subject, intermediatePredicate, intermediateObject};
			LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
			TriplePattern tp = new TriplePattern(items);
			temp.add(tp);
			this.indexScanCreator.createIndexScanAndConnectWithRoot(new OperatorIDTuple(union,0), temp, graphConstraint);

			items[0] = intermediateObject;
			items[1] = intermediatePredicate;
			items[2] = subject;
			temp = new LinkedList<TriplePattern>();
			tp = new TriplePattern(items.clone());
			temp.add(tp);
			this.indexScanCreator.createIndexScanAndConnectWithRoot(new OperatorIDTuple(union,1), temp, graphConstraint);

			final Projection projection = new Projection();
			projection.addProjectionElement(subject);
			projection.addProjectionElement(object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
				projection.addProjectionElement((Variable)graphConstraint);
			}
			final ASTVar n = new ASTVar(100);
			n.setName(subject.toString().substring(1));
			final Bind bind = new Bind(subject);
			bind.addProjectionElement(object,n);
			union.addSucceedingOperator(new OperatorIDTuple(bind,0));
			bind.addSucceedingOperator(new OperatorIDTuple(projection,0));
			final InMemoryDistinct memoryDistinct = new InMemoryDistinct();
			projection.addSucceedingOperator(new OperatorIDTuple(memoryDistinct,0));
			return memoryDistinct;
		}
	}

	private Set<Literal> getSetOfLiterals(final Node node){
		Set<Literal> allowedLiterals = null;
		final Item item = getItem(node);
		if(!item.isVariable()){
			allowedLiterals = new HashSet<Literal>();
			allowedLiterals.add((Literal) item);
		}
		return allowedLiterals;
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator visit(final ASTArbitraryOccurencesNotZero node,
			final OperatorConnection connection, final Item graphConstraint,
			final Variable subject, final Variable object, final Node subjectNode, final Node objectNode) {
		if(USE_CLOSURE_AND_PATHLENGTHZERO_OPERATORS){
			Node predicateNode = node.jjtGetChild(0);
			while (predicateNode instanceof ASTArbitraryOccurences ||
					predicateNode instanceof ASTArbitraryOccurencesNotZero){
				if(predicateNode instanceof ASTArbitraryOccurences){
					return predicateNode.accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
				}
				predicateNode = predicateNode.jjtGetChild(0);
			}
			final BasicOperator startingOperator = predicateNode.accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);

			Set<Literal> allowedObjects = null;
			Set<Literal> allowedSubjects = null;

			if(node.jjtGetParent() instanceof ASTTripleSet){
				// If there is a fixed subject or object, then we should consider it during evaluation!
				allowedSubjects = this.getSetOfLiterals(subjectNode);
				allowedObjects = this.getSetOfLiterals(objectNode);
			}

			final Closure closure = new Closure(subject, object, allowedSubjects, allowedObjects);
			startingOperator.addSucceedingOperator(new OperatorIDTuple(closure,0));

			return closure;
		} else {
			// alternative way to evaluate (...)+ without using the Closure operator!
			final ReplaceVar replaceVar = new ReplaceVar();
			final ReplaceVar replaceVari = new ReplaceVar();
			final BasicOperator startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);

			final Projection projection = new Projection();
			projection.addProjectionElement(subject);
			projection.addProjectionElement(object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
				projection.addProjectionElement((Variable)graphConstraint);
			}

			final InMemoryDistinct memoryDistinct = new InMemoryDistinct();
			try {
				final Filter filter = new Filter("(" + subject + " != " + object + ")");

				replaceVar.addSubstitution(object, subject);
				final Variable variable = this.getVariable(subject.toString(), object.toString(), "interimVariable");
				replaceVar.addSubstitution(variable, object);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
					replaceVar.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
				}
				replaceVari.addSubstitution(subject, subject);
				replaceVari.addSubstitution(object, variable);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
					replaceVari.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
				}

				startingOperator.addSucceedingOperator(new OperatorIDTuple(filter,0));
				startingOperator.addSucceedingOperator(new OperatorIDTuple(projection,0));
				final Join intermediateJoinOperator = new Join();
				replaceVar.addSucceedingOperator(new OperatorIDTuple(memoryDistinct,0));
				memoryDistinct.addSucceedingOperator(new OperatorIDTuple(intermediateJoinOperator,1));
				filter.addSucceedingOperator(new OperatorIDTuple(intermediateJoinOperator,0));
				filter.addSucceedingOperator(new OperatorIDTuple(replaceVar,0));
				intermediateJoinOperator.addSucceedingOperator(new OperatorIDTuple(replaceVari,0));
				replaceVari.addSucceedingOperator(new OperatorIDTuple(replaceVar,0));
				replaceVari.addSucceedingOperator(new OperatorIDTuple(projection,0));

			} catch (final ParseException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			return projection;
		}
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator visit(final ASTInvers node,
			final OperatorConnection connection, final Item graphConstraint,
			final Variable subject, final Variable object, final Node subjectNode, final Node objectNode) {
		return node.jjtGetChild(0).accept(this, connection, graphConstraint, object, subject, subjectNode, objectNode);
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator visit(final ASTNegatedPath node,
			final OperatorConnection connection, final Item graphConstraint,
			final Variable subject, final Variable object, final Node subjectNode, final Node objectNode) {

		final Minus minus = new Minus();
		final Union union = new Union();

		final Item[] items = {subject, this.getVariable(subject.toString(), object.toString(), "b"), object};
		final TriplePattern tp = new TriplePattern(items);
		final LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
		temp.add(tp);
		this.indexScanCreator.createIndexScanAndConnectWithRoot(new OperatorIDTuple(minus,0), temp, graphConstraint);
		for (int i = 0; i < node.jjtGetNumChildren(); i++){
			final Item[] items2 = new Item[3];
			if(node.jjtGetChild(i) instanceof ASTInvers){
				items2[0] = object;
				items2[1] = getItem(node.jjtGetChild(i).jjtGetChild(0));
				items2[2] = subject;
			} else {
				items2[0] = subject;
				items2[1] = getItem(node.jjtGetChild(i));
				items2[2] = object;
			}
			final TriplePattern tp2 = new TriplePattern(items2);
			final LinkedList<TriplePattern> temp2 = new LinkedList<TriplePattern>();
			temp2.add(tp2);
			this.indexScanCreator.createIndexScanAndConnectWithRoot(new OperatorIDTuple(union,i), temp2, graphConstraint);
		}

		union.addSucceedingOperator(new OperatorIDTuple(minus,1));

		final Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
			projection.addProjectionElement((Variable)graphConstraint);
		}
		minus.addSucceedingOperator(new OperatorIDTuple(projection,0));

		return projection;
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator visit(final ASTQuotedURIRef node,
			final OperatorConnection connection, final Item graphConstraint,
			final Variable subject, final Variable object, final Node subjectNode, final Node objectNode) {
		final Item[] items = {subject, getItem(node), object};
		final TriplePattern tp = new TriplePattern(items);
		final LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
		temp.add(tp);
		return this.indexScanCreator.createIndexScanAndConnectWithRoot(null, temp, graphConstraint);
	}


	/** {@inheritDoc} */
	@Override
	public BasicOperator visit(final ASTPathAlternative node,
			final OperatorConnection connection, final Item graphConstraint,
			final Variable subject, final Variable object, final Node subjectNode, final Node objectNode) {
		final BasicOperator leftSide=node.jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
		final BasicOperator rightSide = node.jjtGetChild(1).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
		final Union union = new Union();
		leftSide.addSucceedingOperator(new OperatorIDTuple(union,0));
		rightSide.addSucceedingOperator(new OperatorIDTuple(union,1));
		final Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
			projection.addProjectionElement((Variable)graphConstraint);
		}
		union.addSucceedingOperator(new OperatorIDTuple(projection,0));
		return projection;
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator visit(final ASTPathSequence node, final OperatorConnection connection,
			final Item graphConstraint, final Variable subject, final Variable object, final Node subjectNode, final Node objectNode) {
		final Join join = new Join();
		final Variable v = this.getVariable(subject.toString(), object.toString(), "b");
//		final Node n0 = node.jjtGetChild(0);
//		if(n0 instanceof ASTQuotedURIRef){
//			final Item[] items = {subject, getItem(node.jjtGetChild(0)), v};
//			final TriplePattern tp = new TriplePattern(items);
//			final LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
//			temp.add(tp);
//			this.indexScanCreator.createIndexScanAndConnectWithRoot(new OperatorIDTuple(join, 0), temp, graphConstraint);
//		} else {
			final BasicOperator startingOperator2 = node.jjtGetChild(0).accept(this, connection, graphConstraint, subject, v, subjectNode, objectNode);
			startingOperator2.addSucceedingOperator(new OperatorIDTuple(join, 0));
//		}
		final BasicOperator startingOperator = node.jjtGetChild(1).accept(this, connection, graphConstraint, v, object, subjectNode, objectNode);
		startingOperator.addSucceedingOperator(new OperatorIDTuple(join,1));
		final Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode))) {
			projection.addProjectionElement((Variable)graphConstraint);
		}
		join.addSucceedingOperator(new OperatorIDTuple(projection,0));
		return projection;
	}


	/** {@inheritDoc} */
	@Override
	public void visit(final ASTSelectQuery node, final OperatorConnection connection) {
		this.visit(node, connection, null);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTMinus node, final OperatorConnection connection, final Item graphConstraint) {
		node.jjtGetChild(0).accept(this, connection, graphConstraint);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTSelectQuery node, final OperatorConnection connection, final Item graphConstraint) {
		this.visitQueryNode(node, connection, graphConstraint);
	}

	/**
	 * <p>visitQueryNode.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.Node} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 * @param graphConstraint a {@link lupos.datastructures.items.Item} object.
	 */
	public void visitQueryNode(final Node node, final OperatorConnection connection, Item graphConstraint) {

		// the graph variable is not bound if it is not selected in the subquery
		if (graphConstraint != null && graphConstraint.isVariable()
				&& !(node instanceof ASTSelectQuery && ((ASTSelectQuery)node).isSelectAll())) {

			boolean graphVariableIsSelected = false;
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);
				if (n instanceof ASTVar) {
					final ASTVar variable = (ASTVar) n;
					if (variable.getName().equals(graphConstraint.getName())) {
						graphVariableIsSelected = true;
					}
				}
			}
			// as a workaround we rename the graphvariable in the subquery
			if (!graphVariableIsSelected) {
				int index=0;
				do {
					graphConstraint = new Variable(graphConstraint.getName() + index);
					index++;
				} while (this.hasThisVariable(node, graphConstraint));
			}
		}
		final int numberChildren = node.jjtGetNumChildren();

		boolean onlyAggregations = true;

		// insert limit operator
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTLimit) {
				node.jjtGetChild(i).accept(this, connection);
			}
		}
		// insert offset operator
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTOffset) {
				node.jjtGetChild(i).accept(this, connection);
			}
		}

		if (node instanceof ASTSelectQuery && ((ASTSelectQuery)node).isDistinct()) {

			// or insert a DISTINCT operator into the operator graph:
			connection.connectAndSetAsNewOperatorConnection(new Distinct());
		}

		LinkedList<AddComputedBinding> listOACB = new LinkedList<AddComputedBinding>();
		boolean group = false;

		for (int i = 0; i < numberChildren; i++) {
			final Node childi = node.jjtGetChild(i);
			if (childi instanceof ASTGroup) {
				group = true;
			}
		}

		// insert projection operator

		if (!(node instanceof ASTSelectQuery && ((ASTSelectQuery)node).isSelectAll())) {
			final Projection p = new Projection();
			final LinkedList<AddComputedBinding> listOfAddComputedBindings = new LinkedList<AddComputedBinding>();
			for (int i = 0; i < numberChildren; i++) {
				if (node.jjtGetChild(i) instanceof ASTVar) {
					final ASTVar variable = (ASTVar) node.jjtGetChild(i);
					p.addProjectionElement(new Variable(variable.getName()));
					onlyAggregations = false;
				} else if (node.jjtGetChild(i) instanceof ASTAs) {
					final ASTVar variable = (ASTVar) node.jjtGetChild(i)
							.jjtGetChild(1);
					final lupos.sparql1_1.Node constraint = node.jjtGetChild(i)
							.jjtGetChild(0);
					/*
					 * Detecting Errors in SelectQuery if aggregations are used
					 * and additional variables are not bound by a GROUP BY
					 * statement
					 */
					// this.prooveBoundedGroup(node.jjtGetChild(i));

					if (!(constraint instanceof ASTAggregation)) {
						onlyAggregations = false;
					}
					final Variable var2 = new Variable(variable.getName());
					p.addProjectionElement(var2);
					final AddComputedBinding acb = group ? new GroupByAddComputedBinding()
							: new AddComputedBinding();
					acb.addProjectionElement(var2, constraint);
					listOfAddComputedBindings.add(acb);
				}
			}
			// deleting of values if there is only an aggregation statement
			if (onlyAggregations || group) {
				connection.connectAndSetAsNewOperatorConnection(new Distinct());
			}
			listOACB = this.topologicalSorting(listOfAddComputedBindings);
			connection.connectAndSetAsNewOperatorConnection(p);
		}

		// insert sort operator
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTOrderConditions) {
				node.jjtGetChild(i).accept(this, connection);
			}
		}

		for (final AddComputedBinding acb : listOACB) {
			connection.connectAndSetAsNewOperatorConnection(acb);
		}

		// Dealing with the HAVING clause
		for (int i = 0; i < numberChildren; i++) {
			final Node childi = node.jjtGetChild(i);
			if (childi instanceof ASTHaving) {
				for (int k = 0; k < childi.jjtGetNumChildren(); k++) {
					if (childi.jjtGetChild(k) instanceof ASTFilterConstraint) {
						final Having filter = new Having((ASTFilterConstraint) childi
								.jjtGetChild(k));
						this.processExistChildren(node, graphConstraint, filter);
						filter.setEvaluator(this.evaluator);
						connection.connectAndSetAsNewOperatorConnection(filter);
					}
				}

			}
		}

		// Dealing with the GROUP clause
		for (int j = 0; j < numberChildren; j++) {
			final Projection p = new Projection();
			final LinkedList<AddComputedBinding> listOfAddComputedBindings = new LinkedList<AddComputedBinding>();
			ASTVar variable = null;
			final Node childi = node.jjtGetChild(j);
			onlyAggregations = true;
			if (childi instanceof ASTGroup) {
				for (int i = 0; i < childi.jjtGetNumChildren(); i++) {
					if (childi.jjtGetChild(i) instanceof ASTAdditionNode
							|| childi.jjtGetChild(i) instanceof ASTSubtractionNode
							|| childi.jjtGetChild(i) instanceof ASTMultiplicationNode
							|| childi.jjtGetChild(i) instanceof ASTDivisionNode) {
						throw new Error(
								"Error in GROUP BY statement: AS not found");

					} else if (childi.jjtGetChild(i) instanceof ASTAs) {

						variable = (ASTVar) childi.jjtGetChild(i).jjtGetChild(1);
						final lupos.sparql1_1.Node constraint = childi.jjtGetChild(i).jjtGetChild(0);

						/*
						 * Detecting Errors in SelectQuery if aggregations are
						 * used and additional variables are not bound by a
						 * GROUP BY statement
						 */
						this.prooveBoundedGroup(constraint);

						if (!(constraint instanceof ASTAggregation)) {
							onlyAggregations = false;
						}
						final Variable var2 = new Variable(variable.getName());
						p.addProjectionElement(var2);

						final AddComputedBinding acb = new GroupByAddComputedBinding();
						acb.addProjectionElement(var2, constraint);
						listOfAddComputedBindings.add(acb);

						// deleting of values if there is only an aggregation
						// statement
						if (onlyAggregations) {
							connection.connectAndSetAsNewOperatorConnection(new Distinct());
						}

						for (final AddComputedBinding acbd : listOfAddComputedBindings) {
							connection.connectAndSetAsNewOperatorConnection(acbd);
						}

					}
				}

				// TODO This is according to the SPARQL 1.1 specification, but often the restrictions are nonsense...
				// this.groupNegativeSyntaxTest(childi, node);
				final Group g = new Group(childi);
				connection.connectAndSetAsNewOperatorConnection(g, 0);

				final Sort sort = new Sort(childi);
				connection.connectAndSetAsNewOperatorConnection(sort, 0);
			}
		}

		// Dealing with the FROM (NAMED) clauses
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTDefaultGraph
					|| node.jjtGetChild(i) instanceof ASTNamedGraph) {
				node.jjtGetChild(i).accept(this, connection);
			}
		}

		// Dealing with the STREAM clause
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTStream) {
				this.visit((ASTStream)node.jjtGetChild(i));
			}
		}

		// Dealing with the WHERE clause
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTGroupConstraint) {
				this.visit((ASTGroupConstraint) node.jjtGetChild(i),
						connection, graphConstraint);
			}
		}

	}

	private boolean hasThisVariable(final Node node, final Item variable) {
		boolean resultOfMethod = false;
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			final Node n = node.jjtGetChild(i);
			if (n instanceof ASTVar
					&& ((ASTVar) n).getName().equals(variable.getName())) {
				return true;
			}
			resultOfMethod |= this.hasThisVariable(n, variable);
		}
		return resultOfMethod;
	}

	private LinkedList<AddComputedBinding> topologicalSorting(
			final LinkedList<AddComputedBinding> listOfAddComputedBindings) {
		final LinkedList<AddComputedBinding> listOACB = new LinkedList<AddComputedBinding>();

		// sorting the nodes in case of dependencies
		final LinkedList<Variable> variableList = new LinkedList<Variable>();
		// saves the predecessors
		final LinkedList<LinkedList<Integer>> dependenciesList = new LinkedList<LinkedList<Integer>>();
		// Filling list with all 'result'-variables
		for (final AddComputedBinding acb : listOfAddComputedBindings) {
			final Iterator<Entry<Variable, Filter>> i = acb.getProjections()
					.entrySet().iterator();
			while (i.hasNext()) {
				final Map.Entry<Variable, Filter> mapEntry = i.next();
				variableList.add(mapEntry.getKey());
			}
		}

		// Filling List with all dependencies for each 'result'-variable
		for (final AddComputedBinding acb : listOfAddComputedBindings) {
			final Iterator<Entry<Variable, Filter>> i = acb.getProjections()
					.entrySet().iterator();
			while (i.hasNext()) {
				final Map.Entry<Variable, Filter> mapEntry = i.next();
				final Filter filter = mapEntry.getValue();
				final Set<Variable> set = filter.getUsedVariables();
				final Object[] valueArray = set.toArray();

				final LinkedList<Integer> varList = new LinkedList<Integer>();
				for (int k = 0; k < variableList.size(); k++) {
					int counter = 0;
					for (final Object variable : valueArray) {
						if (variable.equals(variableList.get(k))) {
							varList.add(k);
						}
						counter++;
					}
				}
				dependenciesList.add(varList);
			}
		}

		// begin of modified topological sorting: creating list with counter
		// for
		// dependencies of each element
		final int[] dependenciesCounter = new int[dependenciesList.size()];
		for (int i = 0; i < dependenciesList.size(); i++) {
			dependenciesCounter[i] = dependenciesList.get(i).size();
		}
		// searching 'result' variables without dependencies and deleting
		// them by marking with '-1' and adding the related ACB to the
		// listOACB
		for (int i = 0; i < dependenciesCounter.length; i++) {
			boolean dependenciesAlreadyCleared = false;
			// if dependencies counter is 0, there is no dependency, so the
			// element can be simply used
			if (dependenciesCounter[i] == 0) {
				dependenciesCounter[i] = -1;
				listOACB.addFirst(listOfAddComputedBindings.get(i));
				i = -1;
			}
			// if there are dependencies, it must be clarified, that all of
			// them are already added to the listOACB, then
			// dependenciesAlreadyCleared will be true at the end of this
			// loop
			else if (!(dependenciesCounter[i] == -1)) {
				for (int c = 0; c < dependenciesList.get(i).size(); c++) {
					if (dependenciesCounter[dependenciesList.get(i).get(c)] == -1) {
						dependenciesAlreadyCleared = true;

					} else {
						dependenciesAlreadyCleared = false;
						break;
					}
				}
			}
			if (dependenciesAlreadyCleared == true) {

				listOACB.addFirst(listOfAddComputedBindings.get(i));
				dependenciesCounter[i] = -1;
				i = -1;
			}

		}

		// Error if there are cycles
		for (int i = 0; i < dependenciesCounter.length; i++) {
			if (dependenciesCounter[i] != -1) {
				throw new Error(
						"Erroneous Query: Cycles detected in SELECT-Expression");
			}
		}

		return listOACB;

	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTFilterConstraint node,
			final OperatorConnection connection, final Item graphConstraint) {
		final Filter filter = new Filter(node);

		this.processExistChildren(node, graphConstraint, filter);

		filter.setEvaluator(this.evaluator);
		connection.connectAndSetAsNewOperatorConnection(filter);
	}

	/**
	 * Checks recursively for ASTExists and ASTNotExists nodes under node and
	 * sets up a new {@link Root} and {@link Result} for each
	 * occurrence. This is needed, because we perform new queries for these
	 * types of nodes.
	 *
	 * @param node
	 *            the node
	 * @param graphConstraint
	 *            a graphConstraint to hand over
	 * @param filter
	 *            the {@link Root}s and {@link Result}s are passed to
	 *            this {@link Filter}
	 */
	private void processExistChildren(final Node node, final Item graphConstraint,
			final Filter filter) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			final Node n = node.jjtGetChild(i);
			if (n instanceof ASTExists || n instanceof ASTNotExists) {
				// TODO support also stream-based evaluators!
				if(this.indexScanCreator instanceof IndexScanCreator_BasicIndex){
					final IndexScanCreator_BasicIndex isc = (IndexScanCreator_BasicIndex) this.indexScanCreator;

					final Root collectionClone = (Root) isc.getRoot().clone();
					collectionClone.setSucceedingOperators(new LinkedList<OperatorIDTuple>());

					this.indexScanCreator = new IndexScanCreator_BasicIndex(collectionClone);

					final Result newResult = new Result();
					final OperatorConnection connection = new OperatorConnection(newResult);
					this.visit((ASTGroupConstraint) n.jjtGetChild(0), connection, graphConstraint);

					collectionClone.deleteParents();
					collectionClone.setParents();
					collectionClone.detectCycles();
					collectionClone.sendMessage(new BoundVariablesMessage());
					final CorrectOperatorgraphRulePackage recog = new CorrectOperatorgraphRulePackage();
					recog.applyRules(collectionClone);

					filter.getCollectionForExistNodes().put((SimpleNode) n, collectionClone);

					this.indexScanCreator = isc;
				} else {
					throw new Error("FILTER (NOT) EXISTS is currently only supported by index-based evaluation (e.g. RDF3X and MemoryIndex)!");
				}
			} else {
				this.processExistChildren(n, graphConstraint, filter);
			}
		}
	}

	/**
	 * Checks if there is an error in the group statement syntax
	 *
	 * @param childi
	 *            ASTGroup-Node
	 * @param node
	 *            ASTSelectQuery-Node
	 */
	private void groupNegativeSyntaxTest(final lupos.sparql1_1.Node childi, final lupos.sparql1_1.Node node) {
		final LinkedList<Variable> groupVariables = new LinkedList<Variable>();
		for (int k = 0; k < childi.jjtGetNumChildren(); k++) {
			if (childi.jjtGetChild(k) instanceof ASTVar) {
				final ASTVar variable = (ASTVar) childi.jjtGetChild(k);
				groupVariables.add(new Variable(variable.getName()));
			} else if (childi.jjtGetChild(k) instanceof ASTAs) {
				final ASTVar variable = (ASTVar) childi.jjtGetChild(k).jjtGetChild(1);
				groupVariables.add(new Variable(variable.getName()));
			}
		}
		final LinkedList<Variable> selectVariables = new LinkedList<Variable>();
		for (int k = 0; k < node.jjtGetNumChildren(); k++) {
			if (node.jjtGetChild(k) instanceof ASTVar) {
				final ASTVar variable = (ASTVar) node.jjtGetChild(k);
				selectVariables.add(new Variable(variable.getName()));
			}
		}
		for (int k = 0; k < groupVariables.size(); k++) {
			boolean allVariablesFound = false;
			for (int l = 0; l < selectVariables.size(); l++) {
				if (groupVariables.get(k).equals(selectVariables.get(l))) {
					allVariablesFound = true;
					break;
				} else {
					allVariablesFound = false;
				}
			}
			if (!allVariablesFound) {
				throw new Error("Erroneous Query: Variable in GroupStatement not bound");
			}
		}

		for (int k = 0; k < selectVariables.size(); k++) {
			boolean allVariablesFound = false;
			for (int l = 0; l < groupVariables.size(); l++) {
				if (selectVariables.get(k).equals(groupVariables.get(l))) {
					allVariablesFound = true;
					break;
				} else {
					allVariablesFound = false;
				}
			}
			if (!allVariablesFound) {
				throw new Error("Erroneous Query: Variable in GroupStatement not bound");
			}
		}
	}

	/**
	 * <p>prooveBoundedGroup.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 */
	public void prooveBoundedGroup(final lupos.sparql1_1.Node node) {
		if (node.jjtGetChild(0) instanceof ASTAggregation) {
			for (int index = 0; index < node.jjtGetNumChildren(); index++) {
				if (node.jjtGetParent().jjtGetChild(index) instanceof ASTVar) {
					ASTVar varNode = (ASTVar) node.jjtGetParent().jjtGetChild(index);
					final Variable selectVar = new Variable(varNode.getName());
					boolean varChecked = false;
					for (int g = 0; g < node.jjtGetParent().jjtGetNumChildren(); g++) {
						if (node.jjtGetParent().jjtGetChild(g) instanceof ASTGroup) {
							if (node.jjtGetParent().jjtGetChild(g).jjtGetChild(0) instanceof ASTAs) {
								varNode = (ASTVar) node.jjtGetParent().jjtGetChild(g).jjtGetChild(0).jjtGetChild(1);
							} else {
								varNode = (ASTVar) node.jjtGetParent().jjtGetChild(g).jjtGetChild(0);
							}
							final Variable groupVar = new Variable(varNode.getName());
							if (groupVar.equals(selectVar)) {
								varChecked = true;
							}
						}
					}
					if (!varChecked) {
						throw new Error("Error in Select Query: "
								+ selectVar.getName()
								+ " is not bound by a GROUP BY statement");
					}
				}
			}
		}
	}
	/**
	 * computes a queryresult from a VALUES clause.
	 * For example, the queryresult {(?s="s1", ?o="o1"), (?o="o2")} is computed from the clause VALUES (?s ?o) { ("s1" "o1") (UNDEF "o2")}
	 *
	 * @param node the ASTBindings node in the abstract syntax tree
	 * @return the computed queryresult
	 */
	private QueryResult getQueryResultFromValuesClause(final ASTBindings node){
		final QueryResult bindingsQR = QueryResult.createInstance();
		Bindings binding = new BindingsMap();

		// Getting the variables which are used in the BINDINGS clause
		final LinkedList<Variable> varList = new LinkedList<Variable>();

		for (int k = 0; k < node.jjtGetNumChildren(); k++) {
			if (node.jjtGetChild(k) instanceof ASTVar) {
				final ASTVar var2 = (ASTVar) node.jjtGetChild(k);
				final Variable variable = new Variable(var2.getName());
				varList.add(variable);
			}
		}

		// Creating the bindings with the variables and the literals
		for (int j = 0; j < node.jjtGetNumChildren(); j++) {
			if (node.jjtGetChild(j) instanceof ASTPlusNode) {
				binding = new BindingsMap();
				for (int m = 0; m < node.jjtGetChild(j)
						.jjtGetNumChildren(); m++) {
					final Node litNode = node.jjtGetChild(j)
							.jjtGetChild(m);
					if (!(litNode instanceof ASTUndef)) {
						final Literal lit = LazyLiteral.getLiteral(litNode,
								true);
						binding.add(varList.get(m), lit);
					}
				}
				bindingsQR.add(binding);
			}
		}

		return bindingsQR;
	}


	/**
	 * Extended this method to check if there is an ASTBindings
	 *
	 * @param node
	 *            ASTQuery-Node
	 */
	public void visit(final ASTQuery node) {
		BasicOperator lastOperator = this.result;

		// dealing with the BINDINGS clause
		final int numberChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTBindings) {

				// Inserting the join operation
				final ComputeBindings computeBindings = new ComputeBindings(this.getQueryResultFromValuesClause((ASTBindings)node.jjtGetChild(i)));
				final Join join = new Join();
				join.addSucceedingOperator(this.result);

				computeBindings.addSucceedingOperator(new OperatorIDTuple(join,1));

				this.indexScanCreator.createEmptyIndexScanAndConnectWithRoot(new OperatorIDTuple(computeBindings, 0));
				lastOperator = join;
			}
		}

		for (int i = 0; i < numberChildren; i++) {
			if (!(node.jjtGetChild(i) instanceof ASTBindings)) {
				node.jjtGetChild(i).accept(this,
						new OperatorConnection(lastOperator, 0));
			}
		}

		final BasicOperator root = this.indexScanCreator.getRoot();
		root.setParents();
		root.detectCycles();
		root.sendMessage(new BoundVariablesMessage());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTQuery node, final OperatorConnection connection) {
		this.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTService node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("Service currently not supported (but add-ons on LUPOSDATE Core support Service)!");
	}

	/** {@inheritDoc} */
	public void visit(final ASTGroupConstraint node, final OperatorConnection connection){
		this.visit(node, connection, null);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTGroupConstraint node, final OperatorConnection connection, final Item graphConstraint) {
		try {
			// ------------------------------------------------------------------
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);
				if (n instanceof ASTFilterConstraint) {
					n.accept(this, connection, graphConstraint);
				}
			}
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);
				if (n instanceof ASTBind) {
					final ASTVar variable = (ASTVar) n.jjtGetChild(1);
					final Variable variable2 = new Variable(variable.getName());
					final Bind b = new Bind(variable2);
					b.addProjectionElement(variable2, n.jjtGetChild(0));
					connection.connectAndSetAsNewOperatorConnection(b, 0);
				}
			}
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);
				if (n instanceof ASTOptionalConstraint) {
					final Optional opt = new Optional();
					connection.connectAndSetAsNewOperatorConnection(opt, 1);
					n.accept(this, connection, graphConstraint);
					connection.setOperatorConnection(opt, 0);
				} else if (n instanceof ASTMinus) {
					Minus minus = null;
					if (useSortedMinus) {
						// insert sort operator to preprocess for SortedMinus
						final Sort sortLeft = new Sort();
						final Sort sortRight = new Sort();
						minus = new SortedMinus(sortLeft, sortRight);

						connection.connectAndSetAsNewOperatorConnection(minus, 1);
						connection.connectAndSetAsNewOperatorConnection(sortRight);
						n.accept(this, connection, graphConstraint);
						connection.setOperatorConnection(minus, 0);
						connection.connectAndSetAsNewOperatorConnection(sortLeft);
					} else {
						minus = new Minus();

						connection.connectAndSetAsNewOperatorConnection(minus, 1);
						n.accept(this, connection, graphConstraint);
						connection.setOperatorConnection(minus, 0);
					}
				}
			}

			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);
				if (n instanceof ASTService) {
					this.serviceGenerator.insertFederatedQueryOperator((ASTService)n, connection);
				}
			}

			int numberUnionOrGraphConstraints = 0;
			final LinkedList<TriplePattern> triplePatternToJoin = new LinkedList<TriplePattern>();
			final LinkedList<ASTTripleSet> multipleOccurencesToJoin = new LinkedList<ASTTripleSet>();
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);
				if (n instanceof ASTTripleSet) {
					final Node predicate = n.jjtGetChild(1);
					if (predicate instanceof ASTArbitraryOccurencesNotZero || predicate instanceof ASTArbitraryOccurences || predicate instanceof ASTOptionalOccurence) {
						multipleOccurencesToJoin.add((ASTTripleSet) n);
					} else {
						final TriplePattern tp = this.getTriplePattern((ASTTripleSet) n);
						triplePatternToJoin.add(tp);
					}
				} else if (this.isHigherConstructToJoin(n)) {
					if(!(n instanceof ASTService) ||  this.serviceGenerator.countsAsJoinPartner((ASTService)n)){
						numberUnionOrGraphConstraints++;
					}
				}
			}
			int numberJoinPartner = numberUnionOrGraphConstraints;
			if (triplePatternToJoin.size() > 0) {
				numberJoinPartner++;
			}
			numberJoinPartner+=multipleOccurencesToJoin.size();

			if (numberJoinPartner > 1) {
				final Join joinOperator = new Join();
				connection.connect(joinOperator);
				int j = 0;
				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					final Node n = node.jjtGetChild(i);
					connection.setOperatorConnection(joinOperator, j);
					if(this.handleHigherConstructToJoin(n, connection, graphConstraint)){
						j++;
					}
				}
				if (triplePatternToJoin.size() > 0) {
					connection.setOperatorConnection(joinOperator, j);
					this.indexScanCreator.createIndexScanAndConnectWithRoot(connection.getOperatorIDTuple(), triplePatternToJoin, graphConstraint);
					j++;
				}
				for(int i = 0; i < multipleOccurencesToJoin.size(); i++){
					connection.setOperatorConnection(joinOperator, j);
					this.createMultipleOccurence(multipleOccurencesToJoin.get(i), connection, graphConstraint);
					j++;
				}
			} else if (numberJoinPartner == 0) {
				this.indexScanCreator.createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(connection.getOperatorIDTuple(), graphConstraint);
			} else { // There should be only triple patterns or one
				// higher construct to join
				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					final Node n = node.jjtGetChild(i);
					if (n instanceof ASTTripleSet) {
						if (multipleOccurencesToJoin.size() == 1){
							this.createMultipleOccurence(multipleOccurencesToJoin.get(i), connection, graphConstraint);
							break;
						}
						else{
							this.indexScanCreator.createIndexScanAndConnectWithRoot(connection.getOperatorIDTuple(), triplePatternToJoin, graphConstraint);
							break;
						}
					} else {
						this.handleHigherConstructToJoin(n, connection, graphConstraint);
					}
				}
			}
		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTGraphConstraint node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("Named graphs are not supported by this evaluator!");
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTStream} object.
	 */
	@SuppressWarnings("unused")
	public void visit(final ASTStream node) {
		throw new UnsupportedOperationException("Streams are not supported by this evaluator!");
	}
}
