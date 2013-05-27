/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.datastructures.paged_dbbptree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.Tuple;

public class StandardNodeDeSerializer<K, V> implements NodeDeSerializer<K, V> {

	protected final static byte MOREENTRIES = 0;
	protected final static byte FILENAMEOFNEXTLEAFNODE = 1;

	protected final Class<? super K> keyClass;
	protected final Class<? super V> valueClass;

	public StandardNodeDeSerializer(final Class<? super K> keyClass,
			final Class<? super V> valueClass) {
		this.keyClass = keyClass;
		this.valueClass = valueClass;
	}

	@Override
	public Tuple<K, Integer> getNextInnerNodeEntry(final K lastKey2,
			final LuposObjectInputStream<V> in2) {
		int nextFilename;
		try {
			nextFilename = InputHelper.readLuposInt(in2.is);
		} catch (final java.io.EOFException e) {
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			System.out.println(e);
			return null;
		}
		if (nextFilename < 0) {
			return null;
		}
		K nextKey;
		try {
			nextKey = (K) in2.readLuposObject(this.keyClass);
			return new Tuple<K, Integer>(nextKey, nextFilename);
		} catch (final java.io.EOFException e) {
			return new Tuple<K, Integer>(null, nextFilename);
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println(e);
			return null;
		} catch (final URISyntaxException e) {
			e.printStackTrace();
			System.err.println(e);
			return null;
		}
	}

	@Override
	public DBBPTreeEntry<K, V> getNextLeafEntry(
			final LuposObjectInputStream<V> in, final K lastKey,
			final V lastValue) {
		try {
			try {
				final byte type = InputHelper.readLuposByte(in.is);
				if (type < 0) {
					return null;
				}
				if (type == FILENAMEOFNEXTLEAFNODE) {
					final int filenameOfNextLeafNode = InputHelper.readLuposInt(in.is);
					return new DBBPTreeEntry<K, V>(null, null,
							filenameOfNextLeafNode);
				}
				int filenameOfNextLeafNode = -1;
				V nextValue = null;
				K nextKey = null;
				try {
					final Object o = (this.valueClass == String.class) ? InputHelper.readLuposString(
							(String) lastValue, in.is)
							: in.readLuposObject(this.valueClass);
					if (o == null) {
						in.close();
						return null;
					}
					try {
						nextValue = (V) o;
					} catch (final ClassCastException e) {
						filenameOfNextLeafNode = Integer.parseInt((String) o);
					}
				} catch (final java.io.EOFException e) {
					in.close();
					return null;
				}
				if (nextValue instanceof Triple && this.keyClass == String.class) {
					final Literal lastSubject = ((Triple) nextValue)
							.getSubject();
					final Literal lastPredicate = ((Triple) nextValue)
							.getPredicate();
					final Literal lastObject = ((Triple) nextValue).getObject();
					final byte compressed = InputHelper.readLuposByte(in.is);
					switch (compressed) {
					case 1:
						nextKey = (K) new String(lastSubject.toString()
								+ lastPredicate.toString()
								+ lastObject.toString());
						break;
					case 2:
						nextKey = (K) new String(lastSubject.toString()
								+ lastObject.toString()
								+ lastPredicate.toString());
						break;
					case 3:
						nextKey = (K) new String(lastPredicate.toString()
								+ lastSubject.toString()
								+ lastObject.toString());
						break;
					case 4:
						nextKey = (K) new String(lastPredicate.toString()
								+ lastObject.toString()
								+ lastSubject.toString());
						break;
					case 5:
						nextKey = (K) new String(lastObject.toString()
								+ lastSubject.toString()
								+ lastPredicate.toString());
						break;
					case 6:
						nextKey = (K) new String(lastObject.toString()
								+ lastPredicate.toString()
								+ lastSubject.toString());
						break;
					}
				}
				if (nextKey == null) {
					try {
						nextKey = (K) ((this.keyClass == String.class) ? InputHelper.readLuposString(
								(String) lastKey, in.is)
								: in.readLuposObject(this.keyClass));
					} catch (final java.io.EOFException e) {
						in.close();
						if (nextValue != null) {
							filenameOfNextLeafNode = Integer
									.parseInt((String) nextValue);
							nextValue = null;
						}
					}
					if (nextKey == null) {
						in.close();
						if (nextValue != null) {
							filenameOfNextLeafNode = Integer
									.parseInt((String) nextValue);
							nextValue = null;
						}
					}
				}
				return new DBBPTreeEntry<K, V>(nextKey, nextValue,
						filenameOfNextLeafNode);
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
				in.close();
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
		} catch (final URISyntaxException e) {
			e.printStackTrace();
			System.err.println(e);
			return null;
		}
		return null;
	}

	@Override
	public void writeInnerNodeEntry(final int fileName,
			final LuposObjectOutputStream out) throws IOException {
		OutHelper.writeLuposInt(fileName, out.os);
	}

	@Override
	public void writeInnerNodeEntry(final int fileName, final K key,
			final LuposObjectOutputStream out, final K lastKey)
			throws IOException {
		OutHelper.writeLuposInt(fileName, out.os);
		out.writeLuposObject(key);
	}

	@Override
	public void writeLeafEntryNextFileName(final int filename,
			final LuposObjectOutputStream out) throws IOException {
		OutHelper.writeLuposByte(FILENAMEOFNEXTLEAFNODE, out.os);
		OutHelper.writeLuposInt(filename, out.os);
	}

	@Override
	public void writeLeafEntry(final K k, final V v,
			final LuposObjectOutputStream out, final K lastKey,
			final V lastValue) throws IOException {
		OutHelper.writeLuposByte(MOREENTRIES, out.os);
		if (v instanceof String) {
			OutHelper.writeLuposString((String) v, (String) lastValue, out.os);
		} else {
			out.writeLuposObject(v);
		}
		if (v instanceof Triple && k instanceof String) {
			final String s = (String) k;
			final Literal lastSubject = ((Triple) v).getSubject();
			final Literal lastPredicate = ((Triple) v).getPredicate();
			final Literal lastObject = ((Triple) v).getObject();
			if (s.startsWith(lastSubject.toString())) {
				if (s.compareTo(lastSubject.toString()
						+ lastPredicate.toString() + lastObject.toString()) == 0) {
					OutHelper.writeLuposByte((byte) 1, out.os);
					return;
				} else if (s.compareTo(lastSubject.toString()
						+ lastObject.toString() + lastPredicate.toString()) == 0) {
					OutHelper.writeLuposByte((byte) 2, out.os);
					return;
				}
			} else if (s.startsWith(lastPredicate.toString())) {
				if (s.compareTo(lastPredicate.toString()
						+ lastSubject.toString() + lastObject.toString()) == 0) {
					OutHelper.writeLuposByte((byte) 3, out.os);
					return;
				} else if (s.compareTo(lastPredicate.toString()
						+ lastObject.toString() + lastSubject.toString()) == 0) {
					OutHelper.writeLuposByte((byte) 4, out.os);
					return;
				}
			} else if (s.startsWith(lastObject.toString())) {
				if (s.compareTo(lastObject.toString() + lastSubject.toString()
						+ lastPredicate.toString()) == 0) {
					OutHelper.writeLuposByte((byte) 5, out.os);
					return;
				} else if (s.compareTo(lastObject.toString()
						+ lastPredicate.toString() + lastSubject.toString()) == 0) {
					OutHelper.writeLuposByte((byte) 6, out.os);
					return;
				}
			}
			OutHelper.writeLuposByte((byte) 0, out.os);
			OutHelper.writeLuposString(s, out.os);
		} else if (k instanceof String && lastKey != null) {
			OutHelper.writeLuposString((String) k, (String) lastKey, out.os);
		} else {
			out.writeLuposObject(k);
		}
	}
}
