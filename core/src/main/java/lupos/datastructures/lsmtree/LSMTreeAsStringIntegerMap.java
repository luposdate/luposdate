package lupos.datastructures.lsmtree;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.items.literal.codemap.StringIntegerMap;
import lupos.datastructures.lsmtree.level.Container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LSMTreeAsStringIntegerMap implements StringIntegerMap {

	private final LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>> lsmtree;
	private int size;

	private static final Logger log = LoggerFactory.getLogger(LSMTreeAsStringIntegerMap.class);

	/**
	 * Constructor
	 *
	 * @param lsmtree the LSM-tree used as basis
	 * @param currentsize the current size of the LSM-tree (i.e., the contained String-Integer-mappings without removed entries)
	 */
	public LSMTreeAsStringIntegerMap(final LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>> lsmtree, final int currentsize){
		this.lsmtree = lsmtree;
		this.size = currentsize;
	}

	@Override
	public Integer get(final String s) {
		try {
			return this.lsmtree.get(s);
		} catch (ClassNotFoundException | IOException | URISyntaxException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public void put(final String s, final int value) {
		try {
			this.lsmtree.put(s, value);
			this.size++;
		} catch (ClassNotFoundException | IOException | URISyntaxException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void clear() {
		this.lsmtree.clear();
		this.size = 0;
	}

	@Override
	public int size() {
		return this.size;
	}

	/**
	 * Returns the underlying LSM tree
	 *
	 * @return the underlying LSM tree
	 */
	public LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>> getLSMTree(){
		return this.lsmtree;
	}
}
