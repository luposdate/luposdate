package lupos.sparql1_1.operatorgraph;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import lupos.engine.operators.index.IndexCollection;
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
import lupos.engine.operators.singleinput.Filter;
import lupos.engine.operators.singleinput.Group;
import lupos.engine.operators.singleinput.GroupByAddComputedBinding;
import lupos.engine.operators.singleinput.Having;
import lupos.engine.operators.singleinput.MakeBooleanResult;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.modifiers.Limit;
import lupos.engine.operators.singleinput.modifiers.Offset;
import lupos.engine.operators.singleinput.modifiers.distinct.Distinct;
import lupos.engine.operators.singleinput.modifiers.distinct.InMemoryDistinct;
import lupos.engine.operators.singleinput.sort.Sort;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.generated.CorrectOperatorgraphRulePackage;
import lupos.sparql1_1.*;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreator_BasicIndex;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public abstract class SPARQLCoreParserVisitorImplementation implements
		SPARQL1_1OperatorgraphGeneratorVisitor {

	protected Result result;
	protected int var = 0;
	protected static boolean useSortedMinus = true;	
	protected CommonCoreQueryEvaluator<Node> evaluator;
	protected IndexScanCreatorInterface indexScanCreator;
		
	protected Variable getVariable(String subject, String object, String variable){
		while(subject.startsWith("?") || subject.startsWith("$"))
			subject=subject.substring(1);
		while(object.startsWith("?") || object.startsWith("$"))
			object=object.substring(1);
		while(variable.startsWith("?") || variable.startsWith("$"))
			variable=variable.substring(1);
		String newVariable=variable+var;
		while(newVariable.equals(subject) || newVariable.equals(object) ){
			var++;			
			newVariable=variable+var;			
		}
		return new Variable(newVariable);
	}

	public Result getResult() {
		return result;
	}
	
	public BasicOperator getOperatorgraphRoot(){
		return this.indexScanCreator.getRoot();
	}

	public SPARQLCoreParserVisitorImplementation() {
		result = new Result();
	}
	
	public void setIndexScanGenerator(IndexScanCreatorInterface indexScanCreator){
		this.indexScanCreator = indexScanCreator; 
	}

	public void visitChildrenWithoutStringConcatenation(final Node n, final OperatorConnection connection) {
		for (int i = 0; i < n.jjtGetNumChildren(); i++) {
			n.jjtGetChild(i).accept(this, connection);
		}
	}
	
	public void visit(final ASTConstructQuery node, final OperatorConnection connection) {
		// Dealing with the STREAM clause
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (node.jjtGetChild(i) instanceof ASTStream) {
				this.visit((ASTStream)node.jjtGetChild(i));
			}
		}
		
		int index;
		Node child0 = node.jjtGetChild(0);
		if(child0 instanceof ASTConstructTemplate){
			setGraphConstraints(child0, connection);
			index=1;
		} else {
			index=0;
			for (int i = index; i < node.jjtGetNumChildren(); i++) {
				Node childi = node.jjtGetChild(i);
				if(childi instanceof ASTGroupConstraint)
					setGraphConstraints(childi, connection);
			}			
		}
		for (int i = index; i < node.jjtGetNumChildren(); i++) {
			Node childi = node.jjtGetChild(i);
			if(childi instanceof ASTGroupConstraint)
				childi.accept(this, connection, null);
			else childi.accept(this, connection);
		}
	}
	
	protected void setGraphConstraints(Node constructTemplate,OperatorConnection connection){
		LinkedList<Tuple<Construct, Item>> graphConstraints = this.getGraphConstructs(constructTemplate);
		// there should be only one graphConstraint (for "default graph")!
		for(Tuple<Construct, Item> entry: graphConstraints){
			connection.connectAndSetAsNewOperatorConnection(entry.getFirst());
		}
	}

	public void visit(final ASTAskQuery node, final OperatorConnection connection) {
		final int numberChildren = node.jjtGetNumChildren();

		connection.connectAndSetAsNewOperatorConnection(new MakeBooleanResult());
		// Dealing with the STREAM clause
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTStream) {
				this.visit((ASTStream)node.jjtGetChild(i));
			}
		}

		// phase 2: Dealing with the WHERE clause
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTGroupConstraint) {
				node.jjtGetChild(i).accept(this, connection, null);
			}

		}
	}

	public void visit(final ASTOrderConditions node, final OperatorConnection connection) {
		connection.connectAndSetAsNewOperatorConnection(new Sort(node));
	}

	public void visit(final ASTLimit node, final OperatorConnection connection) {
		connection.connectAndSetAsNewOperatorConnection(new Limit(node.getLimit()));
	}

	public void visit(final ASTOffset node, final OperatorConnection connection) {
		connection.connectAndSetAsNewOperatorConnection(new Offset(node.getOffset()));
	}

	public void visit(final ASTOptionalConstraint node, final OperatorConnection connection, Item graphConstraint) {
		node.jjtGetChild(0).accept(this, connection, graphConstraint);
	}

	public void visit(final ASTUnionConstraint node, final OperatorConnection connection, Item graphConstraint) {
		final Union union = new Union();
		connection.connectAndSetAsNewOperatorConnection(union);
		node.jjtGetChild(0).accept(this, connection, graphConstraint);
		connection.setOperatorConnection(union,1);
		node.jjtGetChild(1).accept(this, connection, graphConstraint);
	}

	public LinkedList<Tuple<Construct, Item>> getGraphConstructs(final Node node) {
		final LinkedList<Tuple<Construct, Item>> graphConstructs = new LinkedList<Tuple<Construct, Item>>();
		final Collection<TriplePattern> operators = collectTriplePatternOfChildren(node);
		if(operators.size()>0){
			final Construct c = new Construct();
			c.setTemplates(operators);
			graphConstructs.add(new Tuple<Construct, Item>(c, null));
		}
		for(int i=0; i<node.jjtGetNumChildren();i++){
			Node child = node.jjtGetChild(i);
			if(child instanceof ASTGraphConstraint){
				final Collection<TriplePattern> otp = collectTriplePatternOfChildren(child);
				if(otp.size()>0){
					final Construct c2 = new Construct();
					c2.setTemplates(otp);
					Node childchild=child.jjtGetChild(0);
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
	
	public Collection<TriplePattern> collectTriplePatternOfChildren(final Node node){
		Collection<TriplePattern> result = new LinkedList<TriplePattern>();
		for(int i=0; i<node.jjtGetNumChildren(); i++){
			Node child = node.jjtGetChild(i);
			if(child instanceof ASTTripleSet){
				result.add(this.getTriplePattern((ASTTripleSet)child));
			}
		}
		return result;
	}

	public static Item getItem(final Node n) {
		if(n instanceof ASTObjectList)
			return getItem(n.jjtGetChild(0));
		if (n instanceof ASTVar) {
			final ASTVar var = (ASTVar) n;
			final String name = var.getName();
			return new Variable(name);
		} else
			return LazyLiteral.getLiteral(n, true);
	}

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
	
	protected boolean isHigherConstructToJoin(Node n){
		return (n instanceof ASTUnionConstraint
				|| n instanceof ASTGraphConstraint
				|| n instanceof ASTService
				|| n instanceof ASTGroupConstraint
				|| n instanceof ASTWindow
				|| n instanceof ASTSelectQuery);
	}
	
	protected boolean handleHigherConstructToJoin(Node n, OperatorConnection connection, Item graphConstraint){
		if (isHigherConstructToJoin(n)) {
			if(n instanceof ASTService || n instanceof ASTGraphConstraint){
				n.accept(this, connection);
			} else {
				n.accept(this, connection, graphConstraint);
			}
			return true;
		} else return false;
	}
	
	protected void createMultipleOccurence(int i, LinkedList<ASTTripleSet> multipleOccurencesToJoin, OperatorConnection connection, Item graphConstraint) throws ParseException{
		try{
			Variable subject;
			Variable object;
			Item realSubject = null;
			Item realObject = null;
			boolean subjectIsALiteral = false;
			boolean objectIsALiteral = false;
			Item itm = getItem(multipleOccurencesToJoin.get(i).jjtGetChild(0));
			if (!itm.isVariable()){
				subject = getVariable(getItem(multipleOccurencesToJoin.get(i).jjtGetChild(0)).toString(), getItem(multipleOccurencesToJoin.get(i).jjtGetChild(2)).toString(), "interimSubject");
				realSubject = itm;
				subjectIsALiteral = true;
			} else {
				subject = (Variable) itm;
			}
			Node subjectNode = multipleOccurencesToJoin.get(i).jjtGetChild(0);
			itm = getItem(multipleOccurencesToJoin.get(i).jjtGetChild(2));
			if (!itm.isVariable()){
				object = getVariable(getItem(multipleOccurencesToJoin.get(i).jjtGetChild(0)).toString(), getItem(multipleOccurencesToJoin.get(i).jjtGetChild(2)).toString(), "interimObject"); 
				realObject = itm;
				objectIsALiteral = true;
			} else {
				object = (Variable) itm;
			}
			Node objectNode = multipleOccurencesToJoin.get(i).jjtGetChild(2);
			ReplaceVar replaceVar = new ReplaceVar();
			replaceVar.addSubstitution(object, subject);
			Variable variable = getVariable(subject.toString(), object.toString(), "interimVariable");
			replaceVar.addSubstitution(variable, object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
				replaceVar.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
			ReplaceVar replaceVari = new ReplaceVar();
			replaceVari.addSubstitution(subject, subject);
			replaceVari.addSubstitution(object, variable);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
				replaceVari.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);

			BasicOperator startingOperator = multipleOccurencesToJoin.get(i).jjtGetChild(1).jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);

			InMemoryDistinct memoryDistinct = new InMemoryDistinct();
			Filter filter = new Filter("(" + subject + " != " + object + ")");

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
				Filter firstFilter = new Filter("(" + subject + " = " + realSubject +")");
				Filter secondFilter = new Filter("(" + subject + " = " + realSubject +")");
				Projection firstProjection = new Projection();
				firstProjection.addProjectionElement(object);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
					firstProjection.addProjectionElement((Variable)graphConstraint);
				Projection secondProjection = new Projection();
				secondProjection.addProjectionElement(object);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
					secondProjection.addProjectionElement((Variable)graphConstraint);

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
				Filter firstFilter = new Filter("(" + object + " = " + realObject + ")");
				Filter secondFilter = new Filter("(" + object + " = " + realObject + ")");
				Projection firstProjection = new Projection();
				firstProjection.addProjectionElement(subject);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
					firstProjection.addProjectionElement((Variable)graphConstraint);
				Projection secondProjection = new Projection();
				secondProjection.addProjectionElement(subject);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
					secondProjection.addProjectionElement((Variable)graphConstraint);

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
				Filter firstFilter = new Filter("(" + object + " = " + realObject + ")");
				Filter secondFilter = new Filter("(" + subject + " = " + realSubject + ")");
				Filter thirdFilter = new Filter("(" + object + " = " + realObject + ")");
				Filter fourthFilter = new Filter("(" + subject + " = " + realSubject + ")");
				Projection firstProjection = new Projection();
				firstProjection.addProjectionElement(subject);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
					firstProjection.addProjectionElement((Variable)graphConstraint);
				Projection secondProjection = new Projection();
				secondProjection.addProjectionElement(object);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
					secondProjection.addProjectionElement((Variable)graphConstraint);
				Projection thirdProjection = new Projection();
				thirdProjection.addProjectionElement(subject);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
					thirdProjection.addProjectionElement((Variable)graphConstraint);
				Projection fourthProjection = new Projection();
				fourthProjection.addProjectionElement(object);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
					fourthProjection.addProjectionElement((Variable)graphConstraint);

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
		catch( Exception e){
			e.printStackTrace();
			System.out.println(e);
		}
	}
	
	public BasicOperator visit(ASTGivenOccurences node,
			OperatorConnection connection, Item graphConstraint,
			Variable subject, Variable object, Node subjectNode, Node objectNode) {
		ReplaceVar replaceVar = new ReplaceVar();
		ReplaceVar replaceVari = new ReplaceVar();
		BasicOperator startingOperator;
		
		int unionPartner = 0;
		Union union = new Union();
		
		int minimalDepth = node.getLowerLimit();
		int maximalDepth = node.getUpperLimit();
		
		if(minimalDepth == 0){
			BasicOperator leftSide = zeroPath(node, connection, graphConstraint, subject, object, subjectNode, objectNode);
			leftSide.addSucceedingOperator(new OperatorIDTuple(union,unionPartner));
			unionPartner++;
			minimalDepth++;
		}
		
		if(maximalDepth == ASTGivenOccurences.INFINITY){ //Handle as plus
			int joinPartner = 0;
			Join joinOperator = new Join();
			Variable currentObject = subject;
			for (int i = 1; i < minimalDepth; i++){
				Variable currentSubject = currentObject;
				currentObject = getVariable(subject.toString(), object.toString(), "interimObject");
				startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, currentSubject, currentObject, subjectNode, objectNode);				
				startingOperator.addSucceedingOperator(new OperatorIDTuple(joinOperator,joinPartner));
				joinPartner++;
			}
			
			startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, currentObject, object, subjectNode, objectNode);
			startingOperator.addSucceedingOperator(new OperatorIDTuple(joinOperator,joinPartner));
			
			InMemoryDistinct memoryDistinct = new InMemoryDistinct();
			try {
				Filter filter = new Filter("(" + subject + " != " + object + ")");
			
				replaceVar.addSubstitution(object, subject);
				Variable variable = getVariable(subject.toString(), object.toString(), "interimVariable");
				replaceVar.addSubstitution(variable, object);
				replaceVari.addSubstitution(subject, subject);
				replaceVari.addSubstitution(object, variable);
				if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
					replaceVari.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
				
				joinOperator.addSucceedingOperator(new OperatorIDTuple(filter,0));
				joinOperator.addSucceedingOperator(new OperatorIDTuple(union,1));
				final Join intermediateJoinOperator = new Join();
				replaceVar.addSucceedingOperator(new OperatorIDTuple(memoryDistinct,0));
				memoryDistinct.addSucceedingOperator(new OperatorIDTuple(intermediateJoinOperator,1));
				filter.addSucceedingOperator(new OperatorIDTuple(intermediateJoinOperator,0));
				filter.addSucceedingOperator(new OperatorIDTuple(replaceVar,0));
				intermediateJoinOperator.addSucceedingOperator(new OperatorIDTuple(replaceVari,0));
				replaceVari.addSucceedingOperator(new OperatorIDTuple(replaceVar,0));
				replaceVari.addSucceedingOperator(new OperatorIDTuple(union,unionPartner));
				unionPartner++;
			} catch (ParseException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
		} else { //Looping Loui-Part
			for(int i = minimalDepth; i <= maximalDepth; i++){
				Join join = new Join();
				Variable currentObject = subject;
				int j;
				for (j = 0; j < i-1; j++){
					Variable currentSubject = currentObject;
					currentObject = getVariable(subject.toString(),object.toString(), "interimObject");
					startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, currentSubject, currentObject, subjectNode, objectNode); 
					startingOperator.addSucceedingOperator(new OperatorIDTuple(join,j));
				}
				
				startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, currentObject, object, subjectNode, objectNode);
				startingOperator.addSucceedingOperator(new OperatorIDTuple(join,j));
				
				join.addSucceedingOperator(new OperatorIDTuple(union,unionPartner));
				unionPartner++;
			}
		}
		
		Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
			projection.addProjectionElement((Variable)graphConstraint);
		union.addSucceedingOperator(new OperatorIDTuple(projection,0));
		
		return projection;
	}

	public BasicOperator visit(ASTOptionalOccurence node,
			OperatorConnection connection, Item graphConstraint,
			Variable subject, Variable object, Node subjectNode, Node objectNode) {
		BasicOperator startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
		Union union = new Union();
		
		BasicOperator leftSide = zeroPath(node, connection, graphConstraint, subject, object, subjectNode, objectNode);
		leftSide.addSucceedingOperator(new OperatorIDTuple(union,0));
		
		Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
			projection.addProjectionElement((Variable)graphConstraint);
		
		startingOperator.addSucceedingOperator(new OperatorIDTuple(union,1));
		
		union.addSucceedingOperator(new OperatorIDTuple(projection,0));
		return projection;
	}

	public BasicOperator visit(ASTArbitraryOccurences node,
			OperatorConnection connection, Item graphConstraint,
			Variable subject, Variable object, Node subjectNode, Node objectNode) {
		// Plus Operator
		BasicOperator startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
		 
		Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
			projection.addProjectionElement((Variable)graphConstraint);
		
		Union union = new Union();
		InMemoryDistinct memoryDistinct = new InMemoryDistinct();
		try {
			Filter filter = new Filter("(" + subject + " != " + object + ")");
		
			ReplaceVar replaceVar = new ReplaceVar();
			replaceVar.addSubstitution(object, subject);
			Variable variable = getVariable(subject.toString(), object.toString(), "interimVariable");
			replaceVar.addSubstitution(variable, object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
				replaceVar.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
			ReplaceVar replaceVari = new ReplaceVar();
			replaceVari.addSubstitution(subject, subject);
			replaceVari.addSubstitution(object, variable);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
				replaceVari.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
			
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
			BasicOperator startingPoint = zeroPath(node, connection, graphConstraint, subject, object, subjectNode, objectNode);
			
			startingPoint.addSucceedingOperator(new OperatorIDTuple(union,0));
			
		} catch (ParseException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return projection;
	}

	private BasicOperator zeroPath(Node node, OperatorConnection connection, Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode) {		
		if (!getItem(subjectNode).isVariable() && !getItem(objectNode).isVariable()){
			Projection projection = new Projection();
			projection.addProjectionElement(subject);
			projection.addProjectionElement(object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
				projection.addProjectionElement((Variable)graphConstraint);
			// TODO consider graphConstraint!
			this.indexScanCreator.createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(new OperatorIDTuple(projection,0), graphConstraint);
			return projection;
		} else if (!getItem(subjectNode).isVariable()){
			Bind firstBind = new Bind(object);
			firstBind.addProjectionElement(object, subjectNode);
			// TODO consider graphConstraint!
			this.indexScanCreator.createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(new OperatorIDTuple(firstBind,0), graphConstraint);
			return firstBind;
		} else if(!getItem(objectNode).isVariable()){
			LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
			Item[] items = {subject, getItem(node.jjtGetChild(0)), object};
			temp.add(new TriplePattern(items));
			Bind firstBind = new Bind(subject);
			firstBind.addProjectionElement(subject, objectNode);
			// TODO consider graphConstraint!
			this.indexScanCreator.createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(new OperatorIDTuple(firstBind,0), graphConstraint);
			return firstBind;
		} else {		
			Union union = new Union();
			Variable intermediatePredicate = getVariable(subject.toString(),object.toString(),"intermediatePredicate");
			Variable intermediateObject = getVariable(subject.toString(),object.toString(),"intermediateObject");
			Item[] items = {subject, intermediatePredicate, intermediateObject};
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
			
			Projection projection = new Projection();
			projection.addProjectionElement(subject);
			projection.addProjectionElement(object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
				projection.addProjectionElement((Variable)graphConstraint);
			ASTVar n = new ASTVar(100);
			n.setName(subject.toString().substring(1));
			Bind bind = new Bind(subject);
			bind.addProjectionElement(object,n);
			union.addSucceedingOperator(new OperatorIDTuple(bind,0));
			bind.addSucceedingOperator(new OperatorIDTuple(projection,0));
			InMemoryDistinct memoryDistinct = new InMemoryDistinct();
			projection.addSucceedingOperator(new OperatorIDTuple(memoryDistinct,0));
			return memoryDistinct;
		}
	}

	public BasicOperator visit(ASTArbitraryOccurencesNotZero node,
			OperatorConnection connection, Item graphConstraint,
			Variable subject, Variable object, Node subjectNode, Node objectNode) {
		ReplaceVar replaceVar = new ReplaceVar();
		ReplaceVar replaceVari = new ReplaceVar();
		BasicOperator startingOperator = node.jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
		
		Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
			projection.addProjectionElement((Variable)graphConstraint);
		
		InMemoryDistinct memoryDistinct = new InMemoryDistinct();
		try {
			Filter filter = new Filter("(" + subject + " != " + object + ")");
		
			replaceVar.addSubstitution(object, subject);
			Variable variable = getVariable(subject.toString(), object.toString(), "interimVariable");
			replaceVar.addSubstitution(variable, object);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
				replaceVar.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
			replaceVari.addSubstitution(subject, subject);
			replaceVari.addSubstitution(object, variable);
			if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
				replaceVari.addSubstitution((Variable)graphConstraint, (Variable)graphConstraint);
			
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
		
		} catch (ParseException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return projection;
		
	}

	public BasicOperator visit(ASTInvers node,
			OperatorConnection connection, Item graphConstraint,
			Variable subject, Variable object, Node subjectNode, Node objectNode) {
		return node.jjtGetChild(0).accept(this, connection, graphConstraint, object, subject, subjectNode, objectNode);
	}

	public BasicOperator visit(ASTNegatedPath node,
			OperatorConnection connection, Item graphConstraint,
			Variable subject, Variable object, Node subjectNode, Node objectNode) {

		Minus minus = new Minus();
		Union union = new Union();

		Item[] items = {subject, getVariable(subject.toString(), object.toString(), "b"), object};
		TriplePattern tp = new TriplePattern(items);
		LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
		temp.add(tp);
		this.indexScanCreator.createIndexScanAndConnectWithRoot(new OperatorIDTuple(minus,0), temp, graphConstraint);
		for (int i = 0; i < node.jjtGetNumChildren(); i++){
			Item[] items2 = new Item[3];
			if(node.jjtGetChild(i) instanceof ASTInvers){
				items2[0] = object;
				items2[1] = getItem(node.jjtGetChild(i).jjtGetChild(0));
				items2[2] = subject;
			} else {
				items2[0] = subject;
				items2[1] = getItem(node.jjtGetChild(i));
				items2[2] = object;
			}
			TriplePattern tp2 = new TriplePattern(items2);
			LinkedList<TriplePattern> temp2 = new LinkedList<TriplePattern>();
			temp2.add(tp2);
			this.indexScanCreator.createIndexScanAndConnectWithRoot(new OperatorIDTuple(union,i), temp2, graphConstraint);
		}

		union.addSucceedingOperator(new OperatorIDTuple(minus,1));

		Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
			projection.addProjectionElement((Variable)graphConstraint);
		minus.addSucceedingOperator(new OperatorIDTuple(projection,0));

		return projection;
	}
	
	public BasicOperator visit(ASTQuotedURIRef node,
			OperatorConnection connection, Item graphConstraint,
			Variable subject, Variable object, Node subjectNode, Node objectNode) {
		Item[] items = {subject, getItem(node), object};
		TriplePattern tp = new TriplePattern(items);
		LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
		temp.add(tp);
		return this.indexScanCreator.createIndexScanAndConnectWithRoot(null, temp, graphConstraint);
	}


	public BasicOperator visit(ASTPathAlternative node,
			OperatorConnection connection, Item graphConstraint,
			Variable subject, Variable object, Node subjectNode, Node objectNode) {		
		BasicOperator leftSide=node.jjtGetChild(0).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);
		BasicOperator rightSide = node.jjtGetChild(1).accept(this, connection, graphConstraint, subject, object, subjectNode, objectNode);				
		Union union = new Union();
		leftSide.addSucceedingOperator(new OperatorIDTuple(union,0));
		rightSide.addSucceedingOperator(new OperatorIDTuple(union,1));
		Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
			projection.addProjectionElement((Variable)graphConstraint);
		union.addSucceedingOperator(new OperatorIDTuple(projection,0));		
		return projection; 
	}

	public BasicOperator visit(ASTPathSequence node, OperatorConnection connection,
			Item graphConstraint, Variable subject, Variable object, Node subjectNode, Node objectNode) {
		Item[] items = {subject, getItem(node.jjtGetChild(0)), (Item) getVariable(subject.toString(),object.toString(), "b")};
		TriplePattern tp = new TriplePattern(items);
		LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
		temp.add(tp);
		Join join = new Join();
		this.indexScanCreator.createIndexScanAndConnectWithRoot(new OperatorIDTuple(join, 0), temp, graphConstraint);
		BasicOperator startingOperator = node.jjtGetChild(1).accept(this, connection, graphConstraint, (Variable)items[2], object, subjectNode, objectNode); 
		startingOperator.addSucceedingOperator(new OperatorIDTuple(join,1));
		Projection projection = new Projection();
		projection.addProjectionElement(subject);
		projection.addProjectionElement(object);
		if(graphConstraint!=null && graphConstraint.isVariable() && !graphConstraint.equals(getItem(subjectNode)) && !graphConstraint.equals(getItem(objectNode)))
			projection.addProjectionElement((Variable)graphConstraint);
		join.addSucceedingOperator(new OperatorIDTuple(projection,0));
		return projection; 
	}


	public void visit(final ASTSelectQuery node, final OperatorConnection connection) {
		visit(node, connection, null);
	}

	@Override
	public void visit(ASTMinus node, OperatorConnection connection, Item graphConstraint) {
		node.jjtGetChild(0).accept(this, connection, graphConstraint);
	}
	
	public void visit(final ASTSelectQuery node,
			final OperatorConnection connection, Item graphConstraint) {

		// the graph variable is not bound if it is not selected in the subquery
		if (graphConstraint != null && graphConstraint.isVariable()
				&& !node.isSelectAll()) {

			boolean graphVariableIsSelected = false;
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Node n = node.jjtGetChild(i);
				if (n instanceof ASTVar) {
					ASTVar var = (ASTVar) n;
					if (var.getName().equals(graphConstraint.getName())) {
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
				} while (hasThisVariable(node, graphConstraint));
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

		if (node.isDistinct()) {

			// or insert a DISTINCT operator into the operator graph:
			connection.connectAndSetAsNewOperatorConnection(new Distinct());
		}

		LinkedList<AddComputedBinding> listOACB = new LinkedList<AddComputedBinding>();
		boolean group = false;

		for (int i = 0; i < numberChildren; i++) {
			Node childi = node.jjtGetChild(i);
			if (childi instanceof ASTGroup) {
				group = true;
			}
		}

		// insert projection operator

		if (!node.isSelectAll()) {
			final Projection p = new Projection();
			LinkedList<AddComputedBinding> listOfAddComputedBindings = new LinkedList<AddComputedBinding>();
			for (int i = 0; i < numberChildren; i++) {
				if (node.jjtGetChild(i) instanceof ASTVar) {
					final ASTVar var = (ASTVar) node.jjtGetChild(i);
					p.addProjectionElement(new Variable(var.getName()));
					onlyAggregations = false;
				} else if (node.jjtGetChild(i) instanceof ASTAs) {
					final ASTVar var = (ASTVar) node.jjtGetChild(i)
							.jjtGetChild(1);
					final lupos.sparql1_1.Node constraint = node.jjtGetChild(i)
							.jjtGetChild(0);
					/*
					 * Detecting Errors in SelectQuery if aggregations are used
					 * and additional variables are not bound by a GROUP BY
					 * statement
					 */
					prooveBoundedGroup(node.jjtGetChild(i));

					if (!(constraint instanceof ASTAggregation)) {
						onlyAggregations = false;
					}
					final Variable var2 = new Variable(var.getName());
					p.addProjectionElement(var2);
					AddComputedBinding acb = group ? new GroupByAddComputedBinding()
							: new AddComputedBinding();
					acb.addProjectionElement(var2, constraint);
					listOfAddComputedBindings.add(acb);
				}
			}
			// deleting of values if there is only an aggregation statement
			if (onlyAggregations || group) {
				connection.connectAndSetAsNewOperatorConnection(new Distinct());
			}
			listOACB = topologicalSorting(listOfAddComputedBindings);
			connection.connectAndSetAsNewOperatorConnection(p);
		}

		// insert sort operator
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTOrderConditions) {
				node.jjtGetChild(i).accept(this, connection);
			}
		}

		for (AddComputedBinding acb : listOACB) {
			connection.connectAndSetAsNewOperatorConnection(acb);
		}

		// Dealing with the HAVING clause
		for (int i = 0; i < numberChildren; i++) {
			Node childi = node.jjtGetChild(i);
			if (childi instanceof ASTHaving) {
				for (int k = 0; k < childi.jjtGetNumChildren(); k++) {
					if (childi.jjtGetChild(k) instanceof ASTFilterConstraint) {
						Having filter = new Having((ASTFilterConstraint) childi
								.jjtGetChild(k));
						processExistChildren(node, graphConstraint, filter);
						filter.setEvaluator(this.evaluator);
						connection.connectAndSetAsNewOperatorConnection(filter);
					}
				}

			}
		}

		// Dealing with the GROUP clause
		for (int j = 0; j < numberChildren; j++) {
			final Projection p = new Projection();
			LinkedList<AddComputedBinding> listOfAddComputedBindings = new LinkedList<AddComputedBinding>();
			ASTVar var = null;
			Node childi = node.jjtGetChild(j);
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
						
						var = (ASTVar) childi.jjtGetChild(i).jjtGetChild(1);
						final lupos.sparql1_1.Node constraint = childi
								.jjtGetChild(i).jjtGetChild(0);

						/*
						 * Detecting Errors in SelectQuery if aggregations are
						 * used and additional variables are not bound by a
						 * GROUP BY statement
						 */
						prooveBoundedGroup(constraint);

						if (!(constraint instanceof ASTAggregation)) {
							onlyAggregations = false;
						}
						final Variable var2 = new Variable(var.getName());
						p.addProjectionElement(var2);

						AddComputedBinding acb = new GroupByAddComputedBinding();
						acb.addProjectionElement(var2, constraint);
						listOfAddComputedBindings.add(acb);

						// deleting of values if there is only an aggregation
						// statement
						if (onlyAggregations) {
							connection
									.connectAndSetAsNewOperatorConnection(new Distinct());
						}

						for (AddComputedBinding acbd : listOfAddComputedBindings) {
							connection
									.connectAndSetAsNewOperatorConnection(acbd);
						}

					}
				}

				groupNegativeSyntaxTest(childi, node);
				Group g = new Group(childi);
				connection.connectAndSetAsNewOperatorConnection(g, 0);

				Sort sort = new Sort(childi);
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

	private boolean hasThisVariable(Node node, Item var) {
		boolean result = false;
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Node n = node.jjtGetChild(i);
			if (n instanceof ASTVar
					&& ((ASTVar) n).getName().equals(var.getName())) {
				return true;
			}
			result |= hasThisVariable(n, var);
		}
		return result;
	}

	private LinkedList<AddComputedBinding> topologicalSorting(
			LinkedList<AddComputedBinding> listOfAddComputedBindings) {
		LinkedList<AddComputedBinding> listOACB = new LinkedList<AddComputedBinding>();

		// sorting the nodes in case of dependencies
		LinkedList<Variable> variableList = new LinkedList<Variable>();
		// saves the predecessors
		LinkedList<LinkedList<Integer>> dependenciesList = new LinkedList<LinkedList<Integer>>();
		// Filling list with all 'result'-variables
		for (AddComputedBinding acb : listOfAddComputedBindings) {
			Iterator<Entry<Variable, Filter>> i = acb.getProjections()
					.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<Variable, Filter> mapEntry = (Map.Entry<Variable, Filter>) i
						.next();
				variableList.add(mapEntry.getKey());
			}
		}

		// Filling List with all dependencies for each 'result'-variable
		for (AddComputedBinding acb : listOfAddComputedBindings) {
			Iterator<Entry<Variable, Filter>> i = acb.getProjections()
					.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<Variable, Filter> mapEntry = (Map.Entry<Variable, Filter>) i
						.next();
				Filter filter = mapEntry.getValue();
				Set<Variable> set = filter.getUsedVariables();
				Object[] valueArray = set.toArray();

				LinkedList<Integer> varList = new LinkedList<Integer>();
				for (int k = 0; k < variableList.size(); k++) {
					int counter = 0;
					for (Object var : valueArray) {
						if (var.equals(variableList.get(k))) {
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
		int[] dependenciesCounter = new int[dependenciesList.size()];
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

	public void visit(final ASTFilterConstraint node,
			final OperatorConnection connection, Item graphConstraint) {
		Filter filter = new Filter(node);

		processExistChildren(node, graphConstraint, filter);

		filter.setEvaluator(this.evaluator);
		connection.connectAndSetAsNewOperatorConnection(filter);
	}

	/**
	 * Checks recursively for ASTExists and ASTNotExists nodes under node and
	 * sets up a new {@link IndexCollection} and {@link Result} for each
	 * occurrence. This is needed, because we perform new queries for these
	 * types of nodes. Used by
	 * {@link #visit(ASTFilterConstraint, OperatorConnection, Item))}.
	 * 
	 * @param node
	 *            the node
	 * @param graphConstraint
	 *            a graphConstraint to hand over
	 * @param filter
	 *            the {@link IndexCollection}s and {@link Result}s are passed to
	 *            this {@link Filter}
	 */
	private void processExistChildren(final Node node, Item graphConstraint,
			Filter filter) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Node n = node.jjtGetChild(i);
			if (n instanceof ASTExists || n instanceof ASTNotExists) {
				// TODO support also stream-based evaluators!				
				if(this.indexScanCreator instanceof IndexScanCreator_BasicIndex){
					IndexScanCreator_BasicIndex isc = (IndexScanCreator_BasicIndex) this.indexScanCreator;
					
					IndexCollection collectionClone = (IndexCollection) isc.getIndexCollection().clone();
					collectionClone.setSucceedingOperators(new LinkedList<OperatorIDTuple>());
					
					this.indexScanCreator = new IndexScanCreator_BasicIndex(collectionClone);
	
					Result newResult = new Result();
					OperatorConnection connection = new OperatorConnection(newResult);
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
				processExistChildren(n, graphConstraint, filter);
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
	private void groupNegativeSyntaxTest(lupos.sparql1_1.Node childi,
			lupos.sparql1_1.Node node) {
		boolean allVariablesFound = true;
		LinkedList<Variable> groupVariables = new LinkedList<Variable>();
		for (int k = 0; k < childi.jjtGetNumChildren(); k++) {
			if (childi.jjtGetChild(k) instanceof ASTVar) {
				ASTVar var = (ASTVar) childi.jjtGetChild(k);
				groupVariables.add(new Variable(var.getName()));
			} else if (childi.jjtGetChild(k) instanceof ASTAs) {
				ASTVar var = (ASTVar) childi.jjtGetChild(k).jjtGetChild(1);
				groupVariables.add(new Variable(var.getName()));
			}
		}
		LinkedList<Variable> selectVariables = new LinkedList<Variable>();
		for (int k = 0; k < node.jjtGetNumChildren(); k++) {
			if (node.jjtGetChild(k) instanceof ASTVar) {
				ASTVar var = (ASTVar) node.jjtGetChild(k);
				selectVariables.add(new Variable(var.getName()));
			}
		}
		for (int k = 0; k < groupVariables.size(); k++) {
			allVariablesFound = false;
			for (int l = 0; l < selectVariables.size(); l++) {
				if (groupVariables.get(k).equals(selectVariables.get(l))) {
					allVariablesFound = true;
					break;
				} else {
					allVariablesFound = false;
				}
			}
			if (!allVariablesFound) {
				throw new Error(
						"Erroneous Query: Variable in GroupStatement not bound");
			}
		}

		for (int k = 0; k < selectVariables.size(); k++) {
			allVariablesFound = false;
			for (int l = 0; l < groupVariables.size(); l++) {
				if (selectVariables.get(k).equals(groupVariables.get(l))) {
					allVariablesFound = true;
					break;
				} else {
					allVariablesFound = false;
				}
			}
			if (!allVariablesFound) {
				throw new Error(
						"Erroneous Query: Variable in GroupStatement not bound");
			}
		}
	}

	public void prooveBoundedGroup(lupos.sparql1_1.Node node) {
		if (node.jjtGetChild(0) instanceof ASTAggregation) {
			for (int index = 0; index < node.jjtGetNumChildren(); index++) {
				if (node.jjtGetParent().jjtGetChild(index) instanceof ASTVar) {
					ASTVar varNode = (ASTVar) node.jjtGetParent().jjtGetChild(
							index);
					Variable selectVar = new Variable(varNode.getName());
					boolean varChecked = false;
					for (int g = 0; g < node.jjtGetParent().jjtGetNumChildren(); g++) {
						if (node.jjtGetParent().jjtGetChild(g) instanceof ASTGroup) {
							if (node.jjtGetParent().jjtGetChild(g).jjtGetChild(
									0) instanceof ASTAs) {
								varNode = (ASTVar) node.jjtGetParent()
										.jjtGetChild(g).jjtGetChild(0)
										.jjtGetChild(1);
							} else {
								varNode = (ASTVar) node.jjtGetParent()
										.jjtGetChild(g).jjtGetChild(0);
							}
							Variable groupVar = new Variable(varNode.getName());
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
	 * Extended this method to check if there is an ASTBindings
	 * 
	 * @param ASTQuery
	 *            ASTQuery-Node
	 */
	public void visit(final ASTQuery node) {
		BasicOperator lastOperator = result;

		// dealing with the BINDINGS clause
		final int numberChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numberChildren; i++) {
			if (node.jjtGetChild(i) instanceof ASTBindings) {
				QueryResult bindingsQR = QueryResult.createInstance();
				Bindings binding = new BindingsMap();
				ASTVar var2;
				Variable var;

				// Getting the variables which are used in the BINDINGS clause
				LinkedList<Variable> varList = new LinkedList<Variable>();

				for (int k = 0; k < node.jjtGetChild(i).jjtGetNumChildren(); k++) {
					if (node.jjtGetChild(i).jjtGetChild(k) instanceof ASTVar) {

						var2 = (ASTVar) node.jjtGetChild(i).jjtGetChild(k);
						var = new Variable(var2.getName());
						varList.add(var);

					}
				}

				// Creating the bindings with the variables and the literals
				for (int j = 0; j < node.jjtGetChild(i).jjtGetNumChildren(); j++) {
					if (node.jjtGetChild(i).jjtGetChild(j) instanceof ASTPlusNode) {
						binding = new BindingsMap();
						for (int m = 0; m < node.jjtGetChild(i).jjtGetChild(j)
								.jjtGetNumChildren(); m++) {
							Node litNode = node.jjtGetChild(i).jjtGetChild(j)
									.jjtGetChild(m);
							if (!(litNode instanceof ASTUndef)) {
								Literal lit = LazyLiteral.getLiteral(litNode,
										true);
								binding.add(varList.get(m), lit);
							}
						}
						bindingsQR.add(binding);
					}

				}

				// Inserting the join operation
				ComputeBindings computeBindings = new ComputeBindings(
						bindingsQR);
				Join join = new Join();
				join.addSucceedingOperator(result);
				
				computeBindings.addSucceedingOperator(new OperatorIDTuple(join,1));
				
				this.indexScanCreator.createEmptyIndexScanAndConnectWithRoot(new OperatorIDTuple(computeBindings, 0));
				lastOperator = join;
			}
		}

		for (int i = 0; i < numberChildren; i++) {
			if (!(node.jjtGetChild(i) instanceof ASTBindings))
				node.jjtGetChild(i).accept(this,
						new OperatorConnection(lastOperator, 0));
		}

		BasicOperator root = this.indexScanCreator.getRoot();
		root.setParents();
		root.detectCycles();
		root.sendMessage(new BoundVariablesMessage());
	}
	
	@Override
	public void visit(final ASTQuery node, final OperatorConnection connection) {
		this.visit(node);
	}

	@Override
	public void visit(ASTService node, OperatorConnection connection) {
		throw new UnsupportedOperationException("Service currently not supported (but add-ons on LUPOSDATE Core support Service)!");
	}

	public void visit(final ASTGroupConstraint node, OperatorConnection connection){
		this.visit(node, connection, null);
	}
	
	@Override
	public void visit(final ASTGroupConstraint node, OperatorConnection connection, Item graphConstraint) {
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
					ASTVar var = (ASTVar) n.jjtGetChild(1);
					final Variable var2 = new Variable(var.getName());
					Bind b = new Bind(var2);
					b.addProjectionElement(var2, n.jjtGetChild(0));
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
						Sort sortLeft = new Sort();
						Sort sortRight = new Sort();
						minus = new SortedMinus(sortLeft, sortRight);

						connection.connectAndSetAsNewOperatorConnection(minus,
								1);
						connection
								.connectAndSetAsNewOperatorConnection(sortRight);
						n.accept(this, connection, graphConstraint);
						connection.setOperatorConnection(minus, 0);
						connection
								.connectAndSetAsNewOperatorConnection(sortLeft);

					} else {
						minus = new Minus();

						connection.connectAndSetAsNewOperatorConnection(minus,
								1);
						n.accept(this, connection, graphConstraint);
						connection.setOperatorConnection(minus, 0);
					}

				}
			}
			int numberUnionOrGraphConstraints = 0;
			final LinkedList<TriplePattern> triplePatternToJoin = new LinkedList<TriplePattern>();
			final LinkedList<ASTTripleSet> multipleOccurencesToJoin = new LinkedList<ASTTripleSet>();
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node n = node.jjtGetChild(i);
				if (n instanceof ASTTripleSet) {

					if (n.jjtGetChild(1) instanceof ASTArbitraryOccurencesNotZero) {
						multipleOccurencesToJoin.add((ASTTripleSet) n);
					} else {
						final TriplePattern tp = this.getTriplePattern((ASTTripleSet) n);
						triplePatternToJoin.add(tp);
					}
				} else if (isHigherConstructToJoin(n)) {
					numberUnionOrGraphConstraints++;
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
					if(handleHigherConstructToJoin(n, connection, graphConstraint)){
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
					createMultipleOccurence(i, multipleOccurencesToJoin, connection, graphConstraint);
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
							createMultipleOccurence(i, multipleOccurencesToJoin, connection, graphConstraint);
							break;
						}
						else{
							this.indexScanCreator.createIndexScanAndConnectWithRoot(connection.getOperatorIDTuple(), triplePatternToJoin, graphConstraint);
							break;
						}
					} else {
						handleHigherConstructToJoin(n, connection, graphConstraint);
					}
				}
			}
		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(final ASTGraphConstraint node, OperatorConnection connection) {
		throw new UnsupportedOperationException("Named graphs are not supported by this evaluator!");
	}
	
	public void visit(final ASTStream node) {
		throw new UnsupportedOperationException("Streams are not supported by this evaluator!");
	}
}
