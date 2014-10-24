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
package lupos.io.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import lupos.datastructures.simplifiedfractaltree.FractalTreeEntry;
import lupos.datastructures.simplifiedfractaltree.StringKey;
import lupos.io.Registration;
import lupos.io.Registration.DeSerializerConsideringSubClasses;

/**
 * This class is a DeSerializer for <tt>FractalTreeEntry</tt> for the <tt>Luposdate</tt> de/serialization. In order to work it must be registered in
 * the <tt>Registration</tt> class. It falls back to default java serialization if the key and value type is unsupported.
 *
 * @author Denis Fäcke
 * @see DeSerializerConsideringSubClasses
 * @see Registration
 */
@SuppressWarnings("rawtypes")
public class FRACTALTREEENTRY extends DeSerializerConsideringSubClasses<FractalTreeEntry> {
	/**
	 * Reads a <tt>FractalTreeEntry</tt> from the <tt>InputStream</tt>.
	 *
	 * @param in A <tt>InputStream</tt>
	 * @see InputStream
	 */
	@Override
	public FractalTreeEntry deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
		return readLuposFractalTreeEntry(in);
	}

	/**
	 * Returns the registered classes.
	 *
	 * @see Registration
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends FractalTreeEntry>[] getRegisteredClasses() {
		return new Class[] { lupos.datastructures.simplifiedfractaltree.FractalTreeEntry.class };
	}

	@Override
	public int length(final FractalTreeEntry arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Writes the <tt>FractalTreeEntry</tt> to the <tt>OutputStream</tt>.
	 *
	 * @param entry A <tt>FractalTreeEntry</tt>
	 * @param outputStream A <tt>OutputStream</tt>
	 * @see OutputStream
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void serialize(final FractalTreeEntry entry, final OutputStream outputStream) throws IOException {
		writeLuposFractalTreeEntry(entry, outputStream);
	}

	/**
	 * Tests if the specified <tt>Object</tt> is a instance of <tt>FractalTreeEntry</tt>.
	 *
	 * @param instance A <tt>Object</tt>
	 * @return True if <tt>instance</tt> is a instance of <tt>FractalTreeEntry</tt>, else false
	 */
	@Override
	public boolean instanceofTest(final Object o) {
		return o instanceof lupos.datastructures.simplifiedfractaltree.FractalTreeEntry;
	}

	/**
	 * Writes a <tt>FractalTreeEntry</tt> to the <tt>OutputStream</tt>.
	 *
	 * @param t A <tt>FractalTreeEntry</tt>
	 * @param os A <tt>OutputStream</tt>
	 * @throws IOException
	 * @see FractalTreeEntry
	 * @see OutputStream
	 * @throws IOException
	 */
	public final static <K extends Comparable<K>, V> void writeLuposFractalTreeEntry(final lupos.datastructures.simplifiedfractaltree.FractalTreeEntry<K, V> t,
			final OutputStream os) throws IOException {
		// writes the type
		Registration.serializeId(t, os);
		if (t.key != null && t.value != null) {
			if (t.key instanceof StringKey && t.value instanceof Integer) {
				// writes the key
				Registration.serializeWithoutId(((StringKey)t.key).string, os);
				// writes the value
				Registration.serializeWithoutId(t.value, os);
				// writes the pointer
				Registration.serializeWithoutId(new Integer(t.pointer), os);
				//writes the flag
				Registration.serializeWithoutId(new Boolean(t.flag), os);
			} else {
				System.err.println("Unsupported key/value type! Falling back " + "to default java serialization!");
				final ObjectOutputStream oos = new ObjectOutputStream(os);
				oos.writeObject(t);
				oos.flush();
			}
		}
	}

	/**
	 * Reads a <tt>FractalTreeEntry</tt> from the <tt>InputStream</tt>.
	 *
	 * @param in A <tt>InputStream</tt>
	 * @return A <tt>FractalTreeEntry</tt>
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @see FractalTreeEntry
	 * @see InputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public final static lupos.datastructures.simplifiedfractaltree.FractalTreeEntry readLuposFractalTreeEntry(final InputStream in) throws IOException,
			ClassNotFoundException {
		final Class type1 = Registration.deserializeId(in)[0]; // reads the type
		if (type1 == null) {
			System.err.println("Unsupported key/value type! Falling back to default java deserialization!");
			final ObjectInputStream ois = new ObjectInputStream(in);
			return (FractalTreeEntry) ois.readObject();
		}
		final StringKey key;
		final Object value, pointer, flag;

		try {
			// reads the key
			key = new StringKey(Registration.deserializeWithoutId(String.class, in));
			if (key.string == null) {
				return new FractalTreeEntry<>();
			}
			// reads the value
			value = Registration.deserializeWithoutId(Integer.class, in);
			// reads the pointer
			pointer = Registration.deserializeWithoutId(Integer.class, in);
			flag = Registration.deserializeWithoutId(Boolean.class, in);
			if (key != null && value != null) {
				return new FractalTreeEntry<>(key, value, (int) pointer, (boolean)flag);
			}
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
}