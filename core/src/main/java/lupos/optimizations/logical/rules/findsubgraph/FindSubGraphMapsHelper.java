package lupos.optimizations.logical.rules.findsubgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.SimpleOperatorGraphVisitor;

public class FindSubGraphMapsHelper implements SimpleOperatorGraphVisitor{
	private Collection<Map<String,BasicOperator>> foundSubGraphs = new HashSet<Map<String,BasicOperator>>();
	private BasicOperator startNode;
	private Map<BasicOperator,String> subGraphMap;
	
	public FindSubGraphMapsHelper(BasicOperator startNode, Map<BasicOperator,String> subGraphMap){
		this.startNode=startNode;
		this.subGraphMap=subGraphMap;
	}
	
	public Object visit(BasicOperator basicOperator) {
		Map<String,BasicOperator> mso=FindSubGraph.checkSubGraph(basicOperator, subGraphMap, startNode);
		if(mso!=null) foundSubGraphs.add(mso);
		return null;
	}
	
	public Collection<Map<String,BasicOperator>> getFoundSubGraphs(){
		return foundSubGraphs;
	}		
}
