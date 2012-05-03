package lupos.datastructures.items.literal.codemap;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;
import lupos.datastructures.smallerinmemorylargerondisk.MapImplementation;

public class StringIntegerMapJava implements StringIntegerMap {
	private Map<String, Integer> m;
	private Map<String, Integer> original;

	public StringIntegerMapJava(final MapType mapType) {
		switch (mapType) {
		case HASHMAP:
			original = new HashMap<String, Integer>();
			break;
		case DBBPTREE:
			try {
				original = new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(
						300, 300,
						new StandardNodeDeSerializer<String, Integer>(
								String.class, Integer.class));
				((lupos.datastructures.paged_dbbptree.DBBPTree)original).setName("Dictionary: String->Integer");
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			break;
		default:
			original = new MapImplementation<String, Integer>();
			break;
		}
		if (original != null)
			m = Collections.synchronizedMap(original);
	}

	public StringIntegerMapJava(final Map<String, Integer> map) {
		original = map;
		m = Collections.synchronizedMap(map);
	}

	public Integer get(final String s) {
		return m.get(s);
	}

	public void put(final String s, final int value) {
		m.put(s, value);
	}

	public int size() {
		return m.size();
	}

	public void clear() {
		m.clear();
	}

	public Map<String, Integer> getMap() {
		return m;
	}

	public Map<String, Integer> getOriginalMap() {
		return original;
	}
}