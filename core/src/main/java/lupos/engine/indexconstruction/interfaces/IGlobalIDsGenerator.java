package lupos.engine.indexconstruction.interfaces;

import lupos.datastructures.sorteddata.MapIteratorProvider;

public interface IGlobalIDsGenerator {
	public void generateGlobalIDs(final MapIteratorProvider<String, Integer> simap);
}
