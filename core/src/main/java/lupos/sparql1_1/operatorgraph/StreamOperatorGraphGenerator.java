package lupos.sparql1_1.operatorgraph;

import lupos.datastructures.items.Item;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.stream.StreamDuration;
import lupos.engine.operators.stream.StreamTriples;
import lupos.engine.operators.stream.Window;
import lupos.engine.operators.stream.WindowDuration;
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
import lupos.sparql1_1.Node;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreator_Stream;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;
import lupos.sparql1_1.ASTType;
import lupos.sparql1_1.ASTWindow;

public class StreamOperatorGraphGenerator extends
	SPARQLCoreParserVisitorImplementation implements
	SPARQL1_1OperatorgraphGeneratorVisitor {

	public static Class<? extends StreamOperatorGraphGenerator> operatorGraphGeneratorClass = StreamOperatorGraphGenerator.class;
	
	public static StreamOperatorGraphGenerator createOperatorGraphGenerator(CommonCoreQueryEvaluator<Node> evaluator) throws InstantiationException, IllegalAccessException{
		StreamOperatorGraphGenerator sogg = operatorGraphGeneratorClass.newInstance();
		sogg.setIndexScanGenerator_Stream(new IndexScanCreator_Stream());
		sogg.evaluator = evaluator;
		return sogg;
	}
	
	private IndexScanCreator_Stream indexScanCreator_Stream;
	
	protected StreamOperatorGraphGenerator(){
		super();
	}
			
	private void setIndexScanGenerator_Stream(
			IndexScanCreator_Stream indexScanCreator_Stream) {
		this.indexScanCreator_Stream = indexScanCreator_Stream;
		this.setIndexScanGenerator(this.indexScanCreator_Stream);
	}

	public void visit(final lupos.sparql1_1.ASTStream node) {
		final CollectResult cr = new CollectResult();
		this.result.addApplication(cr);
		if (node.isTriples()) {
			indexScanCreator_Stream.setStream(new StreamTriples(cr, node.getValue()));
		} else {
			indexScanCreator_Stream.setStream(new StreamDuration(cr, node.getValue()));
		}
	}

	public void visit(final ASTWindow node, OperatorConnection connection, Item graphConstraint) {
		ASTType.TYPE dot = ASTType.TYPE.TRIPLES;
		int slidingNumber = 10;
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			final Node n = node.jjtGetChild(i);
			if (n instanceof ASTType) {
				dot = ((ASTType) n).getType();
				slidingNumber = ((ASTType) n).getValue();
			} 
		}
		final Window window = (dot == ASTType.TYPE.TRIPLES) ? new WindowTriples(slidingNumber) : new WindowDuration(slidingNumber);						
		if (indexScanCreator_Stream.getStream() == null) {
			System.out.println("Query uses Window operations, but did not define a stream, asssuming STREAM INTERMEDIATERESULT TRIPLES 1");
			final CollectResult cr = new CollectResult();
			this.result.addApplication(cr);
			indexScanCreator_Stream.setStream(new StreamTriples(cr, 1));
		}
		indexScanCreator_Stream.getStream().addSucceedingOperator(new OperatorIDTuple(window, 0));		
		final PatternMatcher currentPatternMatcher = new PatternMatcher();
		window.addSucceedingOperator(new OperatorIDTuple(currentPatternMatcher, 0));
		PatternMatcher oldPatternMatcher = indexScanCreator_Stream.getCurrentPatternMatcher(); 
		indexScanCreator_Stream.setCurrentPatternMatcher(currentPatternMatcher);
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			final Node n = node.jjtGetChild(i);
			if (!(n instanceof ASTType)) {
				n.accept(this, connection, graphConstraint);
			}
		}		
		indexScanCreator_Stream.setCurrentPatternMatcher(oldPatternMatcher);
	}

	@Override
	public void visit(ASTLoad node, OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(ASTClear node, OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(ASTDrop node, OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(ASTCreate node, OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(ASTInsert node, OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(ASTDelete node, OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(ASTModify node, OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support Updates!");
	}

	@Override
	public void visit(ASTDefaultGraph node, OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support several graphs!");
	}

	@Override
	public void visit(ASTNamedGraph node, OperatorConnection connection) {
		throw new UnsupportedOperationException("This evaluator does not support named graphs!");
	}
}
