/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.datastructures.paged_dbbptree.node.nodedeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import lupos.datastructures.paged_dbbptree.node.DBBPTreeEntry;
import lupos.io.Registration;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.Tuple;
public class StandardNodeDeSerializer<K, V> implements NodeDeSerializer<K, V> {

	/** Constant <code>MOREENTRIES=0</code> */
	protected final static byte MOREENTRIES = 0;
	/** Constant <code>FILENAMEOFNEXTLEAFNODE=1</code> */
	protected final static byte FILENAMEOFNEXTLEAFNODE = 1;

	protected final Class<? extends K> keyClass;
	protected final Class<? extends V> valueClass;

	/**
	 * <p>Constructor for StandardNodeDeSerializer.</p>
	 *
	 * @param keyClass a {@link java.lang.Class} object.
	 * @param valueClass a {@link java.lang.Class} object.
	 */
	public StandardNodeDeSerializer(final Class<? extends K> keyClass,
			final Class<? extends V> valueClass) {
		this.keyClass = keyClass;
		this.valueClass = valueClass;
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<K, Integer> getNextInnerNodeEntry(final K lastKey2, final InputStream in2) {
		int nextFilename;
		try {
			nextFilename = InputHelper.readLuposInt(in2);
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
			nextKey = Registration.deserializeWithoutId(this.keyClass, lastKey2, in2);
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

	/** {@inheritDoc} */
	@Override
	public DBBPTreeEntry<K, V> getNextLeafEntry(final InputStream in, final K lastKey, final V lastValue) {
		try {
			try {
				final byte type = InputHelper.readLuposByte(in);
				if (type < 0) {
					return null;
				}
				if (type == FILENAMEOFNEXTLEAFNODE) {
					final int filenameOfNextLeafNode = InputHelper.readLuposInt(in);
					return new DBBPTreeEntry<K, V>(filenameOfNextLeafNode);
				}
				final int filenameOfNextLeafNode = -1;
				final V nextValue = Registration.deserializeWithoutId(this.valueClass, lastValue, in);
				if (nextValue == null) {
					in.close();
					return null;
				}
				final K nextKey = Registration.deserializeWithoutId(this.keyClass, lastKey, in);
				return new DBBPTreeEntry<K, V>(nextKey, nextValue, filenameOfNextLeafNode);

			} catch (final java.io.EOFException e) {
				in.close();
				return null;
			} catch (final ClassNotFoundException e) {
				System.err.println(e);
				e.printStackTrace();
			} catch (final URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void writeInnerNodeEntry(final int fileName, final OutputStream out) throws IOException {
		OutHelper.writeLuposInt(fileName, out);
	}

	/** {@inheritDoc} */
	@Override
	public void writeInnerNodeEntry(final int fileName, final K key, final OutputStream out, final K lastKey) throws IOException {
		OutHelper.writeLuposInt(fileName, out);
		Registration.serializeWithoutId(key, lastKey, out);
	}

	/** {@inheritDoc} */
	@Override
	public void writeLeafEntryNextFileName(final int filename, final OutputStream out) throws IOException {
		OutHelper.writeLuposByte(FILENAMEOFNEXTLEAFNODE, out);
		OutHelper.writeLuposInt(filename, out);
	}

	/** {@inheritDoc} */
	@Override
	public void writeLeafEntry(final K k, final V v, final OutputStream out, final K lastKey, final V lastValue) throws IOException {
		OutHelper.writeLuposByte(MOREENTRIES, out);
		Registration.serializeWithoutId(v, lastValue, out);
		Registration.serializeWithoutId(k, lastKey, out);
	}

	/**
	 * <p>Getter for the field <code>keyClass</code>.</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<? extends K> getKeyClass(){
		return this.keyClass;
	}

	/**
	 * <p>Getter for the field <code>valueClass</code>.</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<? extends V> getValueClass(){
		return this.valueClass;
	}
}
