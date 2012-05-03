package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.*;

import lupos.datastructures.bindings.Bindings;

public class HashBlockingDistinct extends BlockingDistinct {
	
	public HashBlockingDistinct(){
		super(new HashSet<Bindings>());
	}
}