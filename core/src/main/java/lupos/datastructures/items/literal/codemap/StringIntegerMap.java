package lupos.datastructures.items.literal.codemap;

public interface StringIntegerMap {
	public Integer get(String s);

	public void put(String s, int value);

	public void clear();

	public int size();
}
