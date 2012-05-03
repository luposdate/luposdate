package lupos.datastructures.items.literal.codemap;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;
import lupos.datastructures.smallerinmemorylargerondisk.MapImplementation;

public class IntegerStringMapJava implements IntegerStringMap {
	private Map<Integer, String> m;
	private Map<Integer, String> original;

	public IntegerStringMapJava(final MapType mapType) {
		switch (mapType) {
		case HASHMAP:
			original = new HashMap<Integer, String>();
			break;
		case DBBPTREE:
			try {
				original = new lupos.datastructures.paged_dbbptree.DBBPTree<Integer, String>(
						300, 300,
						new StandardNodeDeSerializer<Integer, String>(
								Integer.class, String.class));
				((lupos.datastructures.paged_dbbptree.DBBPTree)original).setName("Dictionary: Integer->String");
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			break;
		default:
			original = new MapImplementation<Integer, String>();
			break;
		}
		if (original != null)
			m = Collections.synchronizedMap(original);
	}

	public IntegerStringMapJava(final Map<Integer, String> map) {
		original = map;
		m = Collections.synchronizedMap(map);
	}

	public String get(final int key) {
		return m.get(key);
	}

	public void put(final int key, final String s) {
		m.put(key, s);
	}

	public int size() {
		return m.size();
	}

	public void clear() {
		m.clear();
	}

	public Map<Integer, String> getMap() {
		return m;
	}

	public void forEachValue(final TProcedureValue<String> arg0) {
		for (final String s : m.values()) {
			arg0.execute(s);
		}
	}

	public void forEachEntry(final TProcedureEntry<Integer, String> arg0) {
		for (final Entry<Integer, String> entry : m.entrySet()) {
			arg0.execute(entry.getKey(), entry.getValue());
		}
	}

	public Map<Integer, String> getOriginalMap() {
		return original;
	}
}