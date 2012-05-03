package lupos.engine.operators.singleinput.sort;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.sorteddata.ElementCounter;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;

public class HybridSortedBagSort extends SortedBagSort {

	public HybridSortedBagSort(){
		super();
	}
	
	public HybridSortedBagSort(final lupos.sparql1_1.Node node){
		super(new lupos.datastructures.smallerinmemorylargerondisk.SortedBagImplementation<Bindings>(new lupos.datastructures.sorteddata.SortedBagImplementation<Bindings>(new java.util.TreeMap<Bindings,ElementCounter<Bindings>>(new ComparatorAST(node)))), node);
	}
	
	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
		this.sswd= new lupos.datastructures.smallerinmemorylargerondisk.SortedBagImplementation<Bindings>(new lupos.datastructures.sorteddata.SortedBagImplementation<Bindings>(new java.util.TreeMap<Bindings,ElementCounter<Bindings>>(new DifferentFromComparator<Bindings>(((Sort)op).getComparator()))));		
	}
	
}
