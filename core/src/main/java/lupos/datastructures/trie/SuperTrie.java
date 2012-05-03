package lupos.datastructures.trie;

import java.util.Iterator;

/**
 * This class implements a space-saving main memory search tree for strings. The
 * strings are stored in a tree where common prefixes are stored only once. The
 * strings are ordered, i.e. a sorted list of strings can be retrieved with a
 * left-order traversal of the search tree.
 */
public abstract class SuperTrie implements Iterable<String> {

	public enum TRIETYPE {
		NORMAL
	};

	public static TRIETYPE TYPE = TRIETYPE.NORMAL;

	public static SuperTrie createInstance() {
		switch (TYPE) {
		default:
		case NORMAL:
			return new Trie();
		}
	}

	public static long MEMORYLIMIT = 1024 * 1024 * 1024; // 1 Gigabyte memory
	// limit per
	// default!

	protected Node root = null;
	protected int numberOfEntries = 0;

	public int getIndex(final String key) {
		if (root == null)
			return -1;
		else
			return root.getIndex(key, 0);
	}

	public String get(final int index) {
		if (root == null)
			return null;
		else
			return root.get(index);
	}

	public boolean remove(final String key) {
		if (root == null)
			return false;
		else if (root.remove(key)) {
			numberOfEntries--;
			return true;
		} else
			return false;
	}

	public String removeAndGetNextLargerOne(final String key) {
		if (root == null)
			return null;
		else
			try {
				final String nextLargerOne = root.removeAndGetNextLargerOne(
						key, "");
				numberOfEntries--;
				return nextLargerOne;
			} catch (final Exception e) {
				return null;
			}
	}

	protected abstract Node createNode(String key);

	public boolean add(final String key) {
		if (root == null) {
			root = createNode(key);
			numberOfEntries = 1;
			return true;
		}
		if (root.add(key)) {
			numberOfEntries++;
			return true;
		} else
			return false;
	}

	public Iterator<String> iterator() {
		if (root == null)
			return new Iterator<String>() {
				public boolean hasNext() {
					return false;
				}

				public String next() {
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		else
			return root.iterator("");
	}

	public int size() {
		return numberOfEntries;
	}

	public abstract long sizeInMemory();

	public boolean isFull() {
		return sizeInMemory() > MEMORYLIMIT;
	}

	public void clear() {
		root = null;
		numberOfEntries = 0;
	}

	@Override
	public String toString() {
		if (root == null)
			return "";
		else
			return root.toString("");
	}

}
