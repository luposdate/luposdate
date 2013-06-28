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
package lupos.sparql1_1.operatorgraph;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.stream.StreamDuration;
import lupos.engine.operators.stream.StreamTriples;
import lupos.engine.operators.stream.Window;
import lupos.engine.operators.stream.WindowDuration;
import lupos.engine.operators.stream.WindowInstancesDuration;
import lupos.engine.operators.stream.WindowInstancesNumber;
import lupos.engine.operators.stream.WindowTriples;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.sparql1_1.ASTClear;
import lupos.sparql1_1.ASTCreate;
import lupos.sparql1_1.ASTDefaultGraph;
import lupos.sparql1_1.ASTDelete;
import lupos.sparql1_1.ASTDrop;
import lupos.sparql1_1.ASTInsert;
import lupos.sparql1_1.ASTLoad;
import lupos.sparql1_1.ASTModify;
import lupos.sparql1_1.ASTNamedGraph;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTType;
import lupos.sparql1_1.ASTWindow;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreator_Stream;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public class StreamOperatorGraphGenerator extends
	SPARQLCoreParserVisitorImplementation implements
	SPARQL1_1OperatorgraphGeneratorVisitor {

	public static Class<? extends StreamOperatorGraphGenerator> operatorGraphGeneratorClass = StreamOperatorGraphGenerator.class;

	public static StreamOperatorGraphGenerator createOperatorGraphGenerator(final CommonCoreQueryEvaluator<Node> evaluator) throws InstantiationException, IllegalAccessException{
		final StreamOperatorGraphGenerator sogg = operatorGraphGeneratorClass.newInstance();
		sogg.setIndexScanGenerator_Stream(new IndexScanCreator_Stream());
		sogg.evaluator = evaluator;
		return sogg;
	}

	private IndexScanCreator_Stream indexScanCreator_Stream;

	protected StreamOperatorGraphGenerator(){
		super();
	}

	private void setIndexScanGenerator_Stream(
			final IndexScanCreator_Stream indexScanCreator_Stream) {
		this.indexScanCreator_Stream = indexScanCreator_Stream;
		this.setIndexScanGenerator(this.indexScanCreator_Stream);
	}

	@Override
	public void visit(final lupos.sparql1_1.ASTStream node) {
		final CollectResult cr = new CollectResult(false);
		this.result.addApplication(cr);
		if (node.isTriples()) {
			this.indexScanCreator_Stream.setStream(new StreamTriples(cr, node.getValue()));
		} else {
			this.indexScanCreator_Stream.setStream(new StreamDuration(cr, node.getValue()));
		}
	}

	@Override
	public void visit(final ASTWindow node, final OperatorConnection connection, final Item graphConstraint) {
		ASTType.TYPE dot = ASTType.TYPE.TRIPLES;
		int slidingNumber = 10;
		Literal iri = null;
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			final Node n = node.jjtGetChild(i);
			if (n instanceof ASTType) {
				dot = ((ASTType) n).getType();
				slidingNumber = ((ASTType) n).getValue();
				if(dot == ASTType.TYPE.INSTANCESDURATION || dot == ASTType.TYPE.INSTANCESNUMBER){
					iri = ((ASTQuotedURIRef)n.jjtGetChild(0)).getLiteral(true);
				}
			}
		}
		final Window window;
		switch(dot){
			default:
			case TRIPLES:
				window = new WindowTriples(slidingNumber);
				break;
			case DURATION:
				window = new WindowDuration(slidingNumber);
				break;
			case INSTANCESNUMBER:
				window = new WindowInstancesNumber(slidingNumber, iri);
				break;
			case INSTANCESDURATION:
				window = new WindowInstancesDuration(slidingNumber, iri);
				break;
		}
		if (this.indexScanCreator_Stream.getStream() == null) {
			System.out.println("Query uses Window operations, but did not define a stream, asssuming STREAM INTERMEDIATERESULT TRIPLES 1");
			final CollectResult cr = new CollectResult(false);
			this.result.addApplication(cr);
			this.indexScanCreator_Stream.setStream(new StreamTriples(cr, 1));
		}
		this.indexScanCreator_Stream.getStream().addSucceedingOperator(new OperatorIDTuple(window, 0));
		final PatternMatcher currentPatternMatcher = new PatternMatcher();
		window.addSucceedingOperator(new OperatorIDTuple(currentPatternMatcher, 0));
		final PatternMatcher oldPatternMatcher = this.indexScanCreator_Stream.getCurrentPatternMatcher();
		this.indexScanCreator_Stream.setCurrentPatternMatcher(currentPatternMatcher);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			final Node n = node.jjtGetChild(i);
			if (!(n instanceof ASTType)) {
				n.accept(this, connection, graphConstraint);
			}
		}
		this.indexScanCreator_Stream.setCurrentPatternMatcher(oldPatternMatcher);
	}

	@Override
	public void visit(final ASTLoad node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(final ASTClear node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(final ASTDrop node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(final ASTCreate node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(final ASTInsert node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(final ASTDelete node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(final ASTModify node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(final ASTDefaultGraph node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support several graphs!");
	}

	@Override
	public void visit(final ASTNamedGraph node, final OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support named graphs!");
	}
}
