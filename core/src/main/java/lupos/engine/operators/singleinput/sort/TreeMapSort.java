package lupos.engine.operators.singleinput.sort;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.sorteddata.ElementCounter;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;

public class TreeMapSort extends SortedBagSort {

	public TreeMapSort(){
		super();
	}
	
	public TreeMapSort(lupos.sparql1_1.Node node){
		super(new lupos.datastructures.sorteddata.SortedBagImplementation<Bindings>(new java.util.TreeMap<Bindings,ElementCounter<Bindings>>(new ComparatorAST(node))), node);
	}
	
	public void cloneFrom(BasicOperator op) {
		super.cloneFrom(op);
		this.sswd= new lupos.datastructures.sorteddata.SortedBagImplementation<Bindings>(new java.util.TreeMap<Bindings,ElementCounter<Bindings>>(new DifferentFromComparator<Bindings>(((Sort)op).getComparator())));		
	}
	
}
