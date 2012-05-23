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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.TreeSet;

import lupos.misc.BitVector;

/**
 * This class implements a space-saving main memory search tree for strings. The
 * strings are stored in a tree where common prefixes are stored only once. The
 * strings are ordered, i.e. a sorted list of strings can be retrieved with a
 * left-order traversal of the search tree.
 */
public class Trie extends SuperTrie implements Iterable<String> {

	@Override
	public long sizeInMemory() {
		if (root == null)
			return 0;
		else
			return root.size;
	}

	@Override
	protected Node createNode(final String key) {
		return new StringSearchNode(key);
	}

	private class StringSearchNode extends Node {

		public StringSearchNode(final String firstEntry) {
			super(firstEntry);
		}

		public StringSearchNode(final String firstEntry,
				final Node childOfFirstEntry, final String secondEntry) {
			super(firstEntry, childOfFirstEntry, secondEntry);
		}

		public StringSearchNode(final String firstEntry,
				final String secondEntry, final Node childOfSecondEntry) {
			super(firstEntry, secondEntry, childOfSecondEntry);
		}

		public StringSearchNode(final String firstEntry,
				final String secondEntry) {
			super(firstEntry, secondEntry);
		}

		public StringSearchNode() {
		}

		@Override
		public String getContentOfIndex(final int index) {
			return new String((char[]) ((content)[index]));
		}

		@Override
		protected Node createNode(final String firstEntry,
				final String secondEntry) {
			return new StringSearchNode(firstEntry, secondEntry);
		}

		@Override
		protected Node createNode(final String firstEntry,
				final Node childOfFirstEntry, final String secondEntry) {
			return new StringSearchNode(firstEntry, childOfFirstEntry,
					secondEntry);
		}

		@Override
		protected Node createNode(final String firstEntry,
				final String secondEntry, final Node childOfSecondEntry) {
			return new StringSearchNode(firstEntry, secondEntry,
					childOfSecondEntry);
		}

		@Override
		protected Object getContentEntry(final char[] content) {
			return content;
		}

		@Override
		protected int getContentSize(final Object object) {
			return 2 * ((char[]) object).length;
		}
	}

	public void serialize(final OutputStream outputstream) throws IOException {
		serialize(outputstream, new Node[] { root });
	}

	private void serialize(final OutputStream outputstream,
			final Node[] levelnodes) throws IOException {
		int numberOfNodesInNextLevel = 0;
		for (final Node node : levelnodes) {
			if (node.children != null)
				for (final Node child : node.children)
					if (child != null)
						numberOfNodesInNextLevel++;
		}
		int index = 0;
		final Node[] nodesInNextLevel = new Node[numberOfNodesInNextLevel];
		for (final Node node : levelnodes) {
			if (node.content.length < 255)
				outputstream.write(node.content.length);
			else {
				// maximal 65535 children as chars are 2 bytes long
				// => 2 bytes for serializing number of contents!
				outputstream.write(255);
				outputstream.write(node.content.length % 256);
				outputstream.write(node.content.length / 256);
			}
			// bit mask for null values...
			final BitVector bitvector = new BitVector(node.content.length);
			if (node.children != null) {
				int i = 0;
				for (final Node child : node.children) {
					if (child != null) {
						nodesInNextLevel[index++] = child;
						bitvector.clear(i++);
					} else
						bitvector.set(i++);
				}
			} else {
				for (int i = 0; i < node.content.length; i++) {
					bitvector.set(i);
				}
			}
			bitvector.writeWithoutSize(outputstream);
			for (final Object content : node.content) {
				final char[] chararray = (char[]) content;
				final String contentString = new String(chararray);
				final byte[] bytes = contentString.getBytes();
				final boolean flag = contentString.compareTo(new String(bytes)) == 0;
				final int length = contentString.length();
				if ((flag && length <= 127) || (!flag && length <= (253 - 128))) {
					final int value = length + (flag ? 0 : 128);
					outputstream.write(value);
				} else {
					outputstream.write(flag ? 254 : 255);
					final int i1 = length % 256;
					final int i2 = (length / 256) % 256;
					final int i3 = (length / (256 * 256)) % 256;
					final int i4 = (length / (256 * 256 * 256)) % 256;
					outputstream.write(i1);
					outputstream.write(i2);
					outputstream.write(i3);
					outputstream.write(i4);
				}
				if (flag) {
					outputstream.write(bytes);
				} else {
					final ByteBuffer buf = ByteBuffer
							.allocate(chararray.length * 2);
					for (final char c : chararray) {
						buf.putChar(c);
					}
					outputstream.write(buf.array());
				}
			}
		}
		if (numberOfNodesInNextLevel > 0)
			serialize(outputstream, nodesInNextLevel);
	}

	public static Trie deserialize(final InputStream inputstream)
			throws IOException {
		final Trie trie = new Trie();
		trie.root = trie.new StringSearchNode();
		deserialize(inputstream, trie, new Node[] { trie.root });
		setNumberOfChildren(trie.root);
		trie.numberOfEntries = trie.root.numberOfEntries;
		return trie;
	}

	private static void setNumberOfChildren(final Node node) {
		int sumNumberOfEntries = 0;
		long sumSize = 8 * node.content.length;
		for (final Object oneContent : node.content)
			sumSize += node.getContentSize(oneContent);
		if (node.children == null)
			sumNumberOfEntries = node.content.length;
		else {
			for (final Node child : node.children) {
				if (child == null)
					sumNumberOfEntries += 1;
				else {
					setNumberOfChildren(child);
					sumNumberOfEntries += child.numberOfEntries;
					sumSize += child.size + 8;
				}
			}
		}
		node.numberOfEntries = sumNumberOfEntries;
		node.size = sumSize;
	}

	private static void deserialize(final InputStream inputstream,
			final Trie trie, final Node[] nodes) throws IOException {
		final BitVector[] bitvectors = new BitVector[nodes.length];
		int numberOfNodesInNextLevel = 0;
		int index = 0;
		// read in every node of this level
		for (final Node node : nodes) {
			// first read the number of content elements
			int length = inputstream.read();
			if (length == -1)
				throw new EOFException();
			if (length == 255) {
				length = inputstream.read() + (inputstream.read() * 256);
			}
			// now read the bit vector reflecting which children are null
			final BitVector bitvector = new BitVector(0);
			bitvector.readWithoutSize(inputstream, length);
			bitvectors[index] = bitvector;

			// calculate the number of nodes in the next level to be read in...
			for (int j = 0; j < length; j++) {
				if (!bitvector.get(j))
					numberOfNodesInNextLevel++;
			}

			// read in the content elements...
			node.content = new Object[length];
			for (int i = 0; i < length; i++) {
				final int value = inputstream.read();
				int contentlength;
				boolean flag;
				if (value >= 254) {
					contentlength = inputstream.read()
							+ 256
							* (inputstream.read() + 256 * (inputstream.read() + 256 * inputstream
									.read()));
					flag = (value == 254);
				} else if (value <= 127) {
					contentlength = value;
					flag = true;
				} else {
					contentlength = value - 127;
					flag = false;
				}
				final byte[] ba = new byte[flag ? contentlength
						: contentlength * 2];
				inputstream.read(ba);
				if (flag) {
					node.content[i] = (new String(ba)).toCharArray();
				} else {
					final ByteBuffer buf = ByteBuffer.wrap(ba);
					final StringBuffer sb = new StringBuffer();
					for (int j = 0; j < contentlength; j++) {
						sb.append(buf.getChar());
					}
					node.content[i] = sb.toString().toCharArray();
				}
			}
			index++;
		}
		if (numberOfNodesInNextLevel > 0) {
			final Node[] nodesInNextLevel = new Node[numberOfNodesInNextLevel];
			for (int j = 0; j < numberOfNodesInNextLevel; j++)
				nodesInNextLevel[j] = trie.new StringSearchNode();
			deserialize(inputstream, trie, nodesInNextLevel);

			// at last set the children correctly!
			int indexChildren = 0;
			int i = 0;
			for (final Node node : nodes) {
				final BitVector bitvector = bitvectors[i];
				if (bitvector.count() < node.content.length) {
					// there is at least one child!
					node.children = new Node[node.content.length];
					for (int j = 0; j < node.content.length; j++) {
						if (bitvector.get(j)) {
							node.children[j] = null;
						} else {
							node.children[j] = nodesInNextLevel[indexChildren++];
						}
					}
				} else {
					node.children = null;
				}
				i++;
			}
		}
	}

	public static void main(final String[] args) {
		final TreeSet<String> treeSet = new TreeSet<String>();
		final Trie searchTree = new Trie();

		final String[] test = new String[] { "hallo", "hello", "aaaaabbbb",
				"aaaaabbbbdddf", "aaaaabbbbdddg", "aakdl", "aaaaabbbbddddd",
				"aaaaabbbbddddd8", "aaaaabbbbddddd9", "aaaaabbbbddddd10",
				"aaaaabbbbddddd11", "aaaaabbbbddddd3", "aaaaabbbbddddd4",
				"aaaaabbbbddddd5", "aaaaabbbbddddd6", "aaaaabbbbddddd7",
				"zzzzhsks", "zzhksfdhj" };

		long sum = 0;
		for (final String s : test) {
			treeSet.add(s);
			searchTree.add(s);
			sum += s.getBytes().length + 8;
			System.out.println("Total size of strings:" + sum
					+ " <-> size of search tree:" + searchTree.sizeInMemory());
		}

		System.out.println(searchTree.toString());

		treeSet.remove("aaaaabbbbddddd11");
		searchTree.remove("aaaaabbbbddddd11");
		treeSet.remove("aaaaabbbbddddd110");
		searchTree.remove("aaaaabbbbddddd110");
		treeSet.remove("aaaaabbbbddddd4");
		searchTree.remove("aaaaabbbbddddd4");

		System.out.println(searchTree.toString());

		final Iterator<String> iterator1 = treeSet.iterator();
		final Iterator<String> iterator2 = searchTree.iterator();
		while (iterator1.hasNext()) {
			if (iterator2.hasNext()) {
				final String exp = iterator1.next();
				final String found = iterator2.next();
				if (exp.compareTo(found) != 0)
					System.err.println("Expected:" + exp + "\nBut found:"
							+ found);
			} else {
				System.err.println("Some elements are missing:");
				while (iterator1.hasNext()) {
					System.err.println(iterator1.next());
				}
			}
		}
		if (iterator2.hasNext()) {
			System.err.println("Some elements are too much:");
			while (iterator2.hasNext()) {
				System.err.println(iterator2.next());
			}
		}

		final Iterator<String> iterator3 = searchTree.iterator();
		while (iterator3.hasNext()) {
			final String s = iterator3.next();
			System.out.println(s + " -> " + searchTree.getIndex(s));
		}

		for (int i = 0; i < searchTree.size(); i++)
			System.out.println(i + " <- " + searchTree.get(i));

		System.out.println("aaaaabbbbddddd10 -> "
				+ searchTree.removeAndGetNextLargerOne("aaaaabbbbddddd10"));
		System.out.println("aaaaabbbbdddg -> "
				+ searchTree.removeAndGetNextLargerOne("aaaaabbbbdddg"));

		System.out.println("End of test!");
	}
}
