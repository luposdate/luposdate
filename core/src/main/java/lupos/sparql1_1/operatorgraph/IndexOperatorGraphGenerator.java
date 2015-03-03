
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
package lupos.sparql1_1.operatorgraph;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.singleinput.Construct;
import lupos.engine.operators.singleinput.SeveralSucceedingOperators;
import lupos.engine.operators.singleinput.sparul.Clear;
import lupos.engine.operators.singleinput.sparul.Create;
import lupos.engine.operators.singleinput.sparul.Delete;
import lupos.engine.operators.singleinput.sparul.Drop;
import lupos.engine.operators.singleinput.sparul.Insert;
import lupos.engine.operators.singleinput.sparul.Load;
import lupos.engine.operators.singleinput.sparul.MultipleURIOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.sparql1_1.*;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreator_BasicIndex;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;
public class IndexOperatorGraphGenerator extends
		SPARQLCoreParserVisitorImplementation implements
		SPARQL1_1OperatorgraphGeneratorVisitor {

	/** Constant <code>operatorGraphGeneratorClass</code> */
	public static Class<? extends IndexOperatorGraphGenerator> operatorGraphGeneratorClass = IndexOperatorGraphGenerator.class;
	
	/**
	 * <p>Constructor for IndexOperatorGraphGenerator.</p>
	 */
	protected IndexOperatorGraphGenerator(){
		super();
	}
			
	/**
	 * <p>createOperatorGraphGenerator.</p>
	 *
	 * @param root a lupos$engine$operators$index$Root object.
	 * @param evaluator a {@link lupos.engine.evaluators.CommonCoreQueryEvaluator} object.
	 * @return a {@link lupos.sparql1_1.operatorgraph.IndexOperatorGraphGenerator} object.
	 * @throws java.lang.InstantiationException if any.
	 * @throws java.lang.IllegalAccessException if any.
	 */
	public static IndexOperatorGraphGenerator createOperatorGraphGenerator(Root root, CommonCoreQueryEvaluator<Node> evaluator) throws InstantiationException, IllegalAccessException{
		IndexOperatorGraphGenerator iogg = operatorGraphGeneratorClass.newInstance();
		iogg.setIndexScanGenerator(new IndexScanCreator_BasicIndex(root));
		iogg.evaluator = evaluator;
		return iogg;
	}
	
	/** {@inheritDoc} */
	public void visit(final ASTModify node, OperatorConnection connection) {
		URILiteral with = null;
		String withString = null;
		int insertIndex = -1;
		int deleteIndex = -1;
		boolean flagUSING = false;
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Node childi = node.jjtGetChild(i);
			if (childi instanceof ASTQuotedURIRef) {
				try {
					withString = childi.toString();
					with=LiteralFactory.createURILiteralWithoutLazyLiteral("<" + withString + ">");
				} catch (final Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}
			} else if(childi instanceof ASTDefaultGraph || childi instanceof ASTNamedGraph){
				childi.accept(this, connection);
				flagUSING = true;
			} else if(childi instanceof ASTInsert){
				insertIndex = i;
			} else if(childi instanceof ASTDelete){
				deleteIndex = i;
			}
		}
		if(insertIndex>=0 && deleteIndex>=0){
			final List<OperatorIDTuple> succeedingOperators = new LinkedList<OperatorIDTuple>();
			
			generateDeleteForModify((ASTConstructTemplate)node.jjtGetChild(deleteIndex).jjtGetChild(0), connection, with);
			succeedingOperators.add(connection.getOperatorIDTuple());
			
			generateInsertForModify((ASTConstructTemplate)node.jjtGetChild(insertIndex).jjtGetChild(0), connection, with);
			succeedingOperators.add(connection.getOperatorIDTuple());
			
			final SeveralSucceedingOperators sso = new SeveralSucceedingOperators();
			sso.setSucceedingOperators(succeedingOperators);
			
			connection.setOperatorConnection(sso);			
		} else {
			if(deleteIndex>=0){
				generateDeleteForModify((ASTConstructTemplate)node.jjtGetChild(deleteIndex).jjtGetChild(0), connection, with);
			} else {
				generateInsertForModify((ASTConstructTemplate)node.jjtGetChild(insertIndex).jjtGetChild(0), connection, with);
			}
		}
		if(with!=null && !flagUSING){
			this.indexScanCreator.addDefaultGraph(withString);
		}
		// deal with the where-clause!
		node.jjtGetChild(node.jjtGetNumChildren()-1).accept(this, connection, null);
	}
	
	/**
	 * <p>generateInsertForModify.</p>
	 *
	 * @param constructTemplate a {@link lupos.sparql1_1.ASTConstructTemplate} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 * @param with a {@link lupos.datastructures.items.literal.URILiteral} object.
	 */
	protected void generateInsertForModify(ASTConstructTemplate constructTemplate, OperatorConnection connection, URILiteral with){
		LinkedList<Tuple<Construct, Item>> graphConstraints = this.getGraphConstructsAndCheckForBNodes((ASTConstructTemplate)constructTemplate);
		LinkedList<MultipleURIOperator> muos = new LinkedList<MultipleURIOperator>(); 
		for(int i=0; i < graphConstraints.size(); i++){
			if(with!=null){
				Tuple<Construct, Item> entry = graphConstraints.get(i);
				if(entry.getSecond()==null)
					entry.setSecond(with);
			}
			MultipleURIOperator bo = new Insert(null, this.indexScanCreator.getDataset());
			connection.connect(bo);
			muos.add(bo);
		}		
		insertMultipleURIOperator(muos, graphConstraints, connection);
	}

	/**
	 * <p>generateDeleteForModify.</p>
	 *
	 * @param constructTemplate a {@link lupos.sparql1_1.ASTConstructTemplate} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 * @param with a {@link lupos.datastructures.items.literal.URILiteral} object.
	 */
	protected void generateDeleteForModify(ASTConstructTemplate constructTemplate, OperatorConnection connection, URILiteral with){
		LinkedList<Tuple<Construct, Item>> graphConstraints = this.getGraphConstructsAndCheckForBNodes((ASTConstructTemplate)constructTemplate);
		LinkedList<MultipleURIOperator> muos = new LinkedList<MultipleURIOperator>(); 
		for(int i=0; i < graphConstraints.size(); i++){
			if(with!=null){
				Tuple<Construct, Item> entry = graphConstraints.get(i);
				if(entry.getSecond()==null)
					entry.setSecond(with);
			}
			MultipleURIOperator bo = new Delete(null, this.indexScanCreator.getDataset()); 
			connection.connect(bo);
			muos.add(bo);
		}		
		insertMultipleURIOperator(muos, graphConstraints, connection);
	}

	/**
	 * <p>insertMultipleURIOperator.</p>
	 *
	 * @param ops a {@link java.util.Collection} object.
	 * @param graphConstraints a {@link java.util.LinkedList} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 */
	protected void insertMultipleURIOperator(Collection<MultipleURIOperator> ops, LinkedList<Tuple<Construct, Item>> graphConstraints, OperatorConnection connection){
		Iterator<MultipleURIOperator> it = ops.iterator();
		for(Tuple<Construct, Item> entry: graphConstraints){
			MultipleURIOperator muo = it.next();
			Collection<URILiteral> cu;
			Item item=entry.getSecond();
			if(item==null){
				cu = new LinkedList<URILiteral>();
			} else if(item.isVariable()){
				cu = this.indexScanCreator.getDataset().getNamedGraphs();
			} else {
				cu = new LinkedList<URILiteral>();
				cu.add((URILiteral)entry.getSecond());
			}
			muo.setURIs(cu);
			Construct construct=entry.getFirst();
			construct.addSucceedingOperator(muo);
			connection.connect(muo);
		}
		if(ops.size()>1){
			SeveralSucceedingOperators endOp = new SeveralSucceedingOperators();
			for(Tuple<Construct, Item> entry: graphConstraints){
				endOp.addSucceedingOperator(entry.getFirst());
			}
			connection.setOperatorConnection(endOp);
		} else {
			connection.setOperatorConnection(graphConstraints.get(0).getFirst());
		}
	}
	
	/**
	 * <p>getGraphConstructsAndCheckForBNodes.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTConstructTemplate} object.
	 * @return a {@link java.util.LinkedList} object.
	 */
	protected LinkedList<Tuple<Construct, Item>> getGraphConstructsAndCheckForBNodes(ASTConstructTemplate node){
		LinkedList<Tuple<Construct, Item>> graphConstraints = this.getGraphConstructs(node);
		for(Tuple<Construct, Item> entry: graphConstraints){
			for(TriplePattern tp: entry.getFirst().getTemplates()){
				for(Item item: tp){
					if(item instanceof LazyLiteral)
						item = ((LazyLiteral)item).getLiteral();
					if(item instanceof AnonymousLiteral)
						throw new Error("Blank nodes in INSERT or DELETE clauses are not allowed!");
				}
			}
		}		
		return graphConstraints;
	}
	
	/**
	 * <p>getGraphConstructsAndCheckForBNodesAndVariables.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTConstructTemplate} object.
	 * @return a {@link java.util.LinkedList} object.
	 */
	protected LinkedList<Tuple<Construct, Item>> getGraphConstructsAndCheckForBNodesAndVariables(ASTConstructTemplate node){
		LinkedList<Tuple<Construct, Item>> graphConstraints = this.getGraphConstructs(node);
		for(Tuple<Construct, Item> entry: graphConstraints){
			for(TriplePattern tp: entry.getFirst().getTemplates()){
				for(Item item: tp){
					if(item instanceof LazyLiteral)
						item = ((LazyLiteral)item).getLiteral();
					if(item instanceof AnonymousLiteral)
						throw new Error("Blank nodes in INSERT or DELETE clauses are not allowed!");
					if(item.isVariable())
						throw new Error("Variables in INSERT DATA or DELETE DATA clauses are not allowed!");
				}
			}
		}		
		return graphConstraints;
	}

	
	/** {@inheritDoc} */
	public void visit(final ASTDelete node, OperatorConnection connection) {
		LinkedList<Tuple<Construct, Item>> graphConstraints = this.getGraphConstructsAndCheckForBNodesAndVariables((ASTConstructTemplate)node.jjtGetChild(node.jjtGetNumChildren() - 1));
		LinkedList<MultipleURIOperator> muos = new LinkedList<MultipleURIOperator>(); 
		for(int i=0; i < graphConstraints.size(); i++){
			muos.add(new Delete(null, this.indexScanCreator.getDataset()));
		}
		insertMultipleURIOperator(muos,  graphConstraints,  node);
	}

	/** {@inheritDoc} */
	public void visit(final ASTInsert node, OperatorConnection connection) {
		LinkedList<Tuple<Construct, Item>> graphConstraints = this.getGraphConstructsAndCheckForBNodesAndVariables((ASTConstructTemplate)node.jjtGetChild(node.jjtGetNumChildren() - 1));
		LinkedList<MultipleURIOperator> muos = new LinkedList<MultipleURIOperator>(); 
		for(int i=0; i < graphConstraints.size(); i++){
			muos.add(new Insert(null, this.indexScanCreator.getDataset()));
		}
		insertMultipleURIOperator(muos,  graphConstraints,  node);
	}
	
	/**
	 * <p>insertMultipleURIOperator.</p>
	 *
	 * @param ops a {@link java.util.Collection} object.
	 * @param graphConstraints a {@link java.util.LinkedList} object.
	 * @param node a {@link lupos.sparql1_1.SimpleNode} object.
	 */
	protected void insertMultipleURIOperator(Collection<MultipleURIOperator> ops, LinkedList<Tuple<Construct, Item>> graphConstraints, SimpleNode node){
		Iterator<MultipleURIOperator> it = ops.iterator();
		for(Tuple<Construct, Item> entry: graphConstraints){
			MultipleURIOperator muo = it.next();
			Collection<URILiteral> cu;
			Item item=entry.getSecond();
			if(item==null){
				cu = new LinkedList<URILiteral>();
			} else if(item.isVariable()){
				cu = this.indexScanCreator.getDataset().getNamedGraphs();
			} else {
				cu = new LinkedList<URILiteral>();
				cu.add((URILiteral)entry.getSecond());
			}
			muo.setURIs(cu);
			muo.setSucceedingOperator(new OperatorIDTuple(result, 0));
			Construct construct = entry.getFirst();
			construct.setSucceedingOperator(new OperatorIDTuple(muo, 0));
			this.indexScanCreator.createEmptyIndexScanAndConnectWithRoot(new OperatorIDTuple(construct, 0));
		}
	}

	/**
	 * <p>InsertEmptyIndex.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.SimpleNode} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 */
	public void InsertEmptyIndex(final SimpleNode node, final OperatorConnection connection) {
		this.indexScanCreator.createEmptyIndexScanAndConnectWithRoot(connection.getOperatorIDTuple());
	}

	/** {@inheritDoc} */
	public void visit(final ASTLoad node, final OperatorConnection connection) {
		final Collection<URILiteral> cu = new LinkedList<URILiteral>();
		Node child0 = node.jjtGetChild(0);
		if (node.jjtGetChild(0) instanceof ASTQuotedURIRef) {
				try {
					cu.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<" + child0.toString() + ">"));
				} catch (final Exception e) {
					System.err.println(e);
				}

			}
		URILiteral into = null;
		if (node.jjtGetNumChildren()>1)
			try {
				into = LiteralFactory.createURILiteralWithoutLazyLiteral("<" + node.jjtGetChild(1).toString() + ">");
			} catch (final Exception e) {
				System.err.println(e);
			}
		connection.connectAndSetAsNewOperatorConnection(new Load(cu, into, this.indexScanCreator.getDataset(), node.isSilent()));
		this.indexScanCreator.createEmptyIndexScanAndConnectWithRoot(connection.getOperatorIDTuple());
	}
	
	/**
	 * <p>setURIs.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @param muo a {@link lupos.engine.operators.singleinput.sparul.MultipleURIOperator} object.
	 */
	public void setURIs(Node node, MultipleURIOperator muo){
		if(node.jjtGetNumChildren()>0){
			Node child = node.jjtGetChild(0);
			if(child instanceof ASTQuotedURIRef){
				try {
					muo.setURI(LiteralFactory.createURILiteralWithoutLazyLiteral("<" + child.toString() + ">"));
				} catch (final Exception e) {
					System.err.println(e);
				}	
			} else if(child instanceof ASTAll){
				Collection<URILiteral> cu=new LinkedList<URILiteral>();
				if(this.indexScanCreator.getDataset()!=null){
					Set<URILiteral> uris=this.indexScanCreator.getDataset().getDefaultGraphs();
					if(uris!=null)
						cu.addAll(uris);
					uris=this.indexScanCreator.getDataset().getNamedGraphs();
					if(uris!=null)
						cu.addAll(uris);
				}
				muo.setURIs(cu);
			} else if(child instanceof ASTDefault){
				Collection<URILiteral> cu=new LinkedList<URILiteral>();
				if(this.indexScanCreator.getDataset()!=null){
					Set<URILiteral> uris=this.indexScanCreator.getDataset().getDefaultGraphs();
					if(uris!=null)
						cu.addAll(uris);
				}
				muo.setURIs(cu);
			} else if(child instanceof ASTNamed){
				Collection<URILiteral> cu=new LinkedList<URILiteral>();
				if(this.indexScanCreator.getDataset()!=null){
					Set<URILiteral> uris=this.indexScanCreator.getDataset().getNamedGraphs();
					if(uris!=null)
						cu.addAll(uris);
				}
				muo.setURIs(cu);
			}
		}
	}

	/** {@inheritDoc} */
	public void visit(final ASTClear node, final OperatorConnection connection) {
		final Clear c = new Clear(this.indexScanCreator.getDataset(), node.isSilent());
		this.setURIs(node, c);
		connection.connectAndSetAsNewOperatorConnection(c);
		this.indexScanCreator.createEmptyIndexScanAndConnectWithRoot(connection.getOperatorIDTuple());
	}

	/** {@inheritDoc} */
	public void visit(final ASTCreate node, final OperatorConnection connection) {
		final Create c = new Create(this.indexScanCreator.getDataset(), node.isSilent());
		try {
			c.setURI(LiteralFactory.createURILiteralWithoutLazyLiteral("<" + node.jjtGetChild(0).toString() + ">"));
		} catch (final Exception e) {
			System.err.println(e);
		}
		connection.connectAndSetAsNewOperatorConnection(c);
		this.indexScanCreator.createEmptyIndexScanAndConnectWithRoot(connection.getOperatorIDTuple());
	}

	/** {@inheritDoc} */
	public void visit(final ASTDrop node, final OperatorConnection connection) {
		final Drop d = new Drop(this.indexScanCreator.getDataset(), node.isSilent());
		this.setURIs(node, d);
		connection.connectAndSetAsNewOperatorConnection(d);
		this.indexScanCreator.createEmptyIndexScanAndConnectWithRoot(connection.getOperatorIDTuple());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTDefaultGraph node, final OperatorConnection connection) {
		this.indexScanCreator.addDefaultGraph(((ASTQuotedURIRef) node.jjtGetChild(0)).getQRef());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTNamedGraph node, final OperatorConnection connection) {
		this.indexScanCreator.addNamedGraph(((ASTQuotedURIRef) node.jjtGetChild(0)).getQRef());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ASTGraphConstraint node, OperatorConnection connection) {
		final int numberChildren = node.jjtGetNumChildren();

		Item graphConstraint = null;
		final Node child = node.jjtGetChild(0);

		if (child instanceof ASTQuotedURIRef) {
			try {
				graphConstraint = LiteralFactory.createURILiteral(((ASTQuotedURIRef) child).toQueryString());
			} catch (final URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		} else if (child instanceof ASTVar) {
			graphConstraint = new Variable(((ASTVar) child).getName());
		}
		
		for (int i = 1; i < numberChildren; i++) {
			Node childi = node.jjtGetChild(i);
			if(childi instanceof ASTGroupConstraint)
				childi.accept(this, connection, graphConstraint);
			else throw new Error("Only ASTGroupConstraint expected, but got "+childi.getClass()+" of "+childi);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(ASTWindow node, OperatorConnection connection, Item graphConstraint){
		throw new UnsupportedOperationException("Index-based evaluators do not support stream processing!");
	}
}
