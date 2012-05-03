package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.HashSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.smallerinmemorylargerondisk.SetImplementation;

public class HybridBlockingDistinct extends BlockingDistinct {
	
	public HybridBlockingDistinct(){
		super(new SetImplementation<Bindings>(new HashSet<Bindings>()));
	}
}