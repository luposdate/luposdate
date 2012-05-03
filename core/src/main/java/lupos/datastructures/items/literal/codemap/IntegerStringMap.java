package lupos.datastructures.items.literal.codemap;

public interface IntegerStringMap {
	public String get(int key);

	public void put(int key, String s);

	public void clear();

	public int size();

	public void forEachValue(final TProcedureValue<String> arg0);

	public void forEachEntry(final TProcedureEntry<Integer, String> arg0);
}
