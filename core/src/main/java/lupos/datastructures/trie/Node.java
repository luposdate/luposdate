/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.datastructures.trie;

import java.util.Iterator;

public abstract class Node {

	public Object[] content = null;
	public Node[] children = null;
	public int numberOfEntries = 0;

	// size of size, numberOfEntries and
	// the pointers children and content
	// (64 Bit pointer size assumed)!
	public long size = 8 + 4 + 2 * 8;

	protected Node() {
	}

	public Node(final String firstEntry) {
		numberOfEntries = 1;
		content = new Object[1];
		content[0] = getContentEntry(firstEntry.toCharArray());
		size += 8 + getContentSize(content[0]);
	}

	public Node(final String firstEntry, final Node childOfFirstEntry,
			final String secondEntry) {
		numberOfEntries = 1 + childOfFirstEntry.numberOfEntries;
		content = new Object[2];
		content[0] = getContentEntry(firstEntry.toCharArray());
		content[1] = getContentEntry(secondEntry.toCharArray());
		children = new Node[2];
		children[0] = childOfFirstEntry;
		children[1] = null;
		size += 2 * 8 + getContentSize(content[0]) + getContentSize(content[1])
				+ 2 * 8 + childOfFirstEntry.size;
	}

	public Node(final String firstEntry, final String secondEntry,
			final Node childOfSecondEntry) {
		numberOfEntries = 1 + childOfSecondEntry.numberOfEntries;
		content = new Object[2];
		content[0] = getContentEntry(firstEntry.toCharArray());
		content[1] = getContentEntry(secondEntry.toCharArray());
		children = new Node[2];
		children[0] = null;
		children[1] = childOfSecondEntry;
		size += 2 * 8 + getContentSize(content[0]) + getContentSize(content[1])
				+ 2 * 8 + childOfSecondEntry.size;
	}

	public Node(final String firstEntry, final String secondEntry) {
		numberOfEntries = 2;
		content = new Object[2];
		content[0] = getContentEntry(firstEntry.toCharArray());
		content[1] = getContentEntry(secondEntry.toCharArray());
		size += 2 * 8 + getContentSize(content[0]) + getContentSize(content[1]);
	}

	public String get(final int index) {
		int currentStartIndex = 0;

		for (int i = 0; i < getContentLength(); i++) {
			if (children == null || children[i] == null) {
				if (currentStartIndex == index)
					return getContentOfIndex(i);
				currentStartIndex++;
			} else {
				if (index >= currentStartIndex
						&& index < (currentStartIndex + children[i].numberOfEntries))
					return getContentOfIndex(i)
							+ children[i].get(index - currentStartIndex);
				currentStartIndex += children[i].numberOfEntries;
			}
		}
		return null;
	}

	public int getIndex(final String key, final int startIndex) {
		int currentStartIndex = startIndex;

		for (int i = 0; i < getContentLength(); i++) {
			final String prefixKey = getContentOfIndex(i);
			final int prefixKeyLength = prefixKey.length();
			if (prefixKeyLength <= key.length()) {
				if (key.substring(0, prefixKeyLength).compareTo(prefixKey) == 0) {
					if (children == null || children[i] == null) {
						if (prefixKeyLength == key.length())
							return currentStartIndex;
					} else {
						if (prefixKeyLength == 0) {
							final int index = children[i].getIndex(
									key.substring(prefixKey.length()),
									currentStartIndex);
							if (index > -1)
								return index;
						} else
							return children[i].getIndex(
									key.substring(prefixKey.length()),
									currentStartIndex);
					}
				}
			}
			if (children == null || children[i] == null)
				currentStartIndex++;
			else
				currentStartIndex += children[i].numberOfEntries;
		}
		return -1;
	}

	public boolean remove(final String key) {
		for (int i = 0; i < content.length; i++) {
			final String prefixKey = getContentOfIndex(i);
			if (prefixKey.length() <= key.length()) {
				if (key.substring(0, prefixKey.length()).compareTo(prefixKey) == 0) {
					if (children == null || children[i] == null) {
						if (prefixKey.length() == key.length()) {
							numberOfEntries--;
							size -= getContentSize(content[i]) + 8;
							final Object[] zcontent = new Object[content.length - 1];
							System.arraycopy(content, 0, zcontent, 0, i);
							System.arraycopy(content, i + 1, zcontent, i,
									content.length - i - 1);
							content = zcontent;

							if (children != null) {
								final Node[] zchildren = new Node[children.length - 1];
								size -= 8;
								System.arraycopy(children, 0, zchildren, 0, i);
								System.arraycopy(children, i + 1, zchildren, i,
										children.length - i - 1);
								children = zchildren;
							}
							return true;
						}
					} else {
						final boolean removed = children[i].remove(key
								.substring(prefixKey.length()));
						if (removed) {
							numberOfEntries--;
							if (children[i].getContentLength() == 1) {
								// child node can be "merged":
								size -= 8 + 4 + 2 * 8; // memory size of
								// child
								// to be removed!
								content[i] = getContentEntry((prefixKey + children[i]
										.getContentOfIndex(0)).toCharArray());
								if (children[i].children != null) {
									size -= 8;
									if (children[i].children[0] != null) {
										size -= 8;
										children[i] = children[i].children[0];
									} else
										children[i] = null;
								} else
									children[i] = null;
							}
						}
						return removed;
					}
				}
			}
		}
		return false;
	}

	public String removeAndGetNextLargerOne(final String key,
			final String prefix) throws Exception {
		for (int i = 0; i < content.length; i++) {
			final String prefixKey = getContentOfIndex(i);
			if (prefixKey.length() <= key.length()) {
				if (key.substring(0, prefixKey.length()).compareTo(prefixKey) == 0) {
					if (children == null || children[i] == null) {
						if (prefixKey.length() == key.length()) {
							numberOfEntries--;
							size -= getContentSize(content[i]) + 8;
							final Object[] zcontent = new Object[content.length - 1];
							System.arraycopy(content, 0, zcontent, 0, i);
							System.arraycopy(content, i + 1, zcontent, i,
									content.length - i - 1);
							content = zcontent;

							if (children != null) {
								final Node[] zchildren = new Node[children.length - 1];
								size -= 8;
								System.arraycopy(children, 0, zchildren, 0, i);
								System.arraycopy(children, i + 1, zchildren, i,
										children.length - i - 1);
								children = zchildren;
							}
							if (i >= content.length)
								return null;
							if (children != null && children[i] != null)
								return prefix + getContentOfIndex(i)
										+ children[i].get(0);
							else
								return prefix + getContentOfIndex(i);
						}
					} else {
						final String nextLargerOne = children[i]
								.removeAndGetNextLargerOne(
										key.substring(prefixKey.length()),
										prefix + prefixKey);
						numberOfEntries--;
						if (children[i].getContentLength() == 1) {
							// child node can be "merged":
							size -= 8 + 4 + 2 * 8; // memory size of
							// child
							// to be removed!
							content[i] = getContentEntry((prefixKey + children[i]
									.getContentOfIndex(0)).toCharArray());
							if (children[i].children != null) {
								size -= 8;
								if (children[i].children[0] != null) {
									size -= 8;
									children[i] = children[i].children[0];
								} else
									children[i] = null;
							} else
								children[i] = null;
						}
						if (nextLargerOne != null)
							return nextLargerOne;
						if (i + 1 >= content.length)
							return null;
						if (children != null && children[i + 1] != null)
							return prefix + getContentOfIndex(i + 1)
									+ children[i + 1].get(0);
						else
							return prefix + getContentOfIndex(i + 1);
					}
				}
			}
		}
		throw new Exception();
	}

	public boolean add(final String key) {

		for (int i = 0; i < content.length; i++) {
			final String prefixKey = getContentOfIndex(i);
			if (prefixKey.length() <= key.length()
					&& key.substring(0, prefixKey.length())
							.compareTo(prefixKey) == 0) {
				if (children == null || children[i] == null) {
					if (prefixKey.length() == key.length())
						return false;
				} else {
					final long oldsize = children[i].size;
					if (children[i].add(key.substring(prefixKey.length()))) {
						size += children[i].size - oldsize;
						numberOfEntries++;
						return true;
					} else
						return false;
				}
			}
			if (prefixKey.length() == 0
					&& (children == null || children[i] == null))
				// maybe the next one fits!
				continue;
			if (prefixKey.length() > 0 && key.length() > 0) {
				final int compare = prefixKey.substring(0, 1).compareTo(
						key.substring(0, 1));
				if (compare == 0) {
					// common prefix, but not for all characters => new
					// node with two entries!
					numberOfEntries++;
					for (int j = 1; j < Math.min(prefixKey.length(),
							key.length()); j++) {
						final int compare2 = prefixKey.substring(j, j + 1)
								.compareTo(key.substring(j, j + 1));
						if (compare2 != 0) {
							final String prefix = prefixKey.substring(0, j);
							final String oldKey = prefixKey.substring(j);
							final String newKey = key.substring(j);
							size -= getContentSize(content[i]);
							content[i] = getContentEntry(prefix.toCharArray());
							size += getContentSize(content[i]);
							if (children == null) {
								children = new Node[content.length];
								size += content.length * 8; // assume 64 Bit
								// pointers!
							}
							if (children[i] != null)
								size -= children[i].size;
							if (compare2 < 0) {
								children[i] = (children[i] == null) ? createNode(
										oldKey, newKey) : createNode(oldKey,
										children[i], newKey);
							} else {
								children[i] = (children[i] == null) ? createNode(
										newKey, oldKey) : createNode(newKey,
										oldKey, children[i]);
							}
							size += children[i].size;
							return true;
						}
					}
					oneIsSmaller(prefixKey, key, i);
					return true;
				} else if (compare > 0) {
					// must be inserted as leaf before current node!
					insertAsLeafBefore(i, key, null);
					return true;
				}
				// compare<0 => must be inserted somewhere right from this
				// position! => continue loop
			} else {
				// the empty string "" must be inserted as leaf before current
				// node!
				if (prefixKey.length() == 0) {
					// the node contains the empty string
					// => replace this entry with the one
					// to be inserted and afterwards
					// insert empty string (a little bit dirty programming)
					content[i] = key.toCharArray();
					if (children != null) {
						final Node child = children[i];
						children[i] = null;
						insertAsLeafBefore(i, "", child);
					} else
						insertAsLeafBefore(i, "", null);
				} else
					insertAsLeafBefore(i, key, null);
				return true;
			}
		}
		// key must be inserted as most right entry...
		numberOfEntries++;
		final Object[] zcontent = new Object[content.length + 1];
		size += 8;
		System.arraycopy(content, 0, zcontent, 0, content.length);
		zcontent[content.length] = getContentEntry(key.toCharArray());
		size += getContentSize(zcontent[content.length]);
		content = zcontent;

		if (children != null) {
			final Node[] zchildren = new Node[children.length + 1];
			size += 8;
			System.arraycopy(children, 0, zchildren, 0, children.length);
			zchildren[children.length] = null;
			children = zchildren;
		}
		return true;
	}

	protected void insertAsLeafBefore(final int i, final String key,
			final Node child) {
		numberOfEntries++;
		final Object[] zcontent = new Object[content.length + 1];
		size += 8;
		System.arraycopy(content, 0, zcontent, 0, i);
		System.arraycopy(content, i, zcontent, i + 1, content.length - i);
		zcontent[i] = getContentEntry(key.toCharArray());
		size += getContentSize(zcontent[i]);
		content = zcontent;

		if (children != null || child != null) {
			final Node[] zchildren = new Node[children.length + 1];
			if (children != null) {
				size += 8;
				System.arraycopy(children, 0, zchildren, 0, i);
				System.arraycopy(children, i, zchildren, i + 1, children.length
						- i);
			} else
				size += 8 * zchildren.length;
			zchildren[i] = child;
			children = zchildren;
		}
	}

	protected void oneIsSmaller(final String prefixKey, final String key,
			final int i) {
		final int minLength = Math.min(prefixKey.length(), key.length());
		final String prefix = prefixKey.substring(0, minLength);
		size -= getContentSize(content[i]);
		content[i] = getContentEntry(prefix.toCharArray());
		size += getContentSize(content[i]);
		final String oldKey = prefixKey.substring(minLength);
		final String newKey = key.substring(minLength);
		if (children == null) {
			children = new Node[content.length];
			size += content.length * 8; // assume 64 Bit pointers...
		}
		if (children[i] != null)
			size -= children[i].size;
		if (prefixKey.length() < key.length()) {
			children[i] = (children[i] == null) ? createNode(oldKey, newKey)
					: createNode(oldKey, children[i], newKey);
		} else {
			children[i] = (children[i] == null) ? createNode(newKey, oldKey)
					: createNode(newKey, oldKey, children[i]);
		}
		size += children[i].size;
	}

	protected abstract Node createNode(String firstEntry, String secondEntry);

	protected abstract Node createNode(String firstEntry,
			Node childOfFirstEntry, String secondEntry);

	protected abstract Node createNode(String firstEntry, String secondEntry,
			Node childOfSecondEntry);

	protected abstract Object getContentEntry(char[] content);

	protected abstract int getContentSize(Object object);

	public Iterator<String> iterator(final String prefix) {
		return new Iterator<String>() {
			int index = 0;
			Iterator<String> currentChildrenIterator = null;
			String next = null;

			public boolean hasNext() {
				if (next == null)
					next = next();
				return (next != null);
			}

			public String next() {
				if (next != null) {
					final String znext = next;
					next = null;
					return znext;
				}
				do {
					if (currentChildrenIterator != null
							&& currentChildrenIterator.hasNext())
						return currentChildrenIterator.next();
					if (index >= getContentLength())
						return null;
					if (currentChildrenIterator == null
							|| !currentChildrenIterator.hasNext()) {
						if (children != null && children[index] != null) {
							currentChildrenIterator = children[index]
									.iterator(prefix + getContentOfIndex(index));
							index++;
						} else {
							return prefix + getContentOfIndex(index++);
						}
					}
				} while (true);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public String toString(final String indent) {
		String s = indent + "[";
		for (int i = 0; i < getContentLength(); i++) {
			if (i > 0)
				s += ",\n " + indent;
			s += "\"" + getContentOfIndex(i) + "\"";
			if (children != null && children[i] != null)
				s += " ->\n" + children[i].toString(indent + "  ");
		}
		return s + "]";
	}

	public int getContentLength() {
		return content.length;
	}

	public abstract String getContentOfIndex(int index);

	@Override
	public String toString() {
		return toString("");
	}
}