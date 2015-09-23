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
package lupos.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DBMergeSortedMap;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.dbmergesortedds.Entry;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.smallerinmemorylargerondisk.SetImplementation;
import lupos.io.helper.InputHelper;
public class LuposObjectInputStream<E> extends ObjectInputStream {

	/** Constant <code>UTF8="UTF-8"</code> */
	public static final String UTF8 = "UTF-8";

	/** Constant <code>LITERAL=0</code> */
	public static final int LITERAL = 0;
	/** Constant <code>URILITERAL=1</code> */
	public static final int URILITERAL = 1;
	/** Constant <code>LANGUAGETAGGEDLITERAL=2</code> */
	public static final int LANGUAGETAGGEDLITERAL = 2;
	/** Constant <code>TYPEDLITERAL=3</code> */
	public static final int TYPEDLITERAL = 3;
	/** Constant <code>ANONYMOUSLITERAL=4</code> */
	public static final int ANONYMOUSLITERAL = 4;
	/** Constant <code>LAZYLITERAL=5</code> */
	public static final int LAZYLITERAL = 5;
	/** Constant <code>LAZYLITERALMATERIALIZED=6</code> */
	public static final int LAZYLITERALMATERIALIZED = 6;
	/** Constant <code>LAZYLITERALORIGINALCONTENT=7</code> */
	public static final int LAZYLITERALORIGINALCONTENT = 7;
	/** Constant <code>LAZYLITERALORIGINALCONTENTMATERIALIZED=8</code> */
	public static final int LAZYLITERALORIGINALCONTENTMATERIALIZED = 8;
	/** Constant <code>PLAINSTRINGLITERAL=9</code> */
	public static final int PLAINSTRINGLITERAL = 9;

	public InputStream is;
	protected Class<? extends E> classOfElements;

	private final static int memoryLimit = 10000;

	/**
	 * <p>Constructor for LuposObjectInputStream.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public LuposObjectInputStream() throws IOException {
	}

	/**
	 * <p>Constructor for LuposObjectInputStream.</p>
	 *
	 * @param arg0 a {@link java.io.InputStream} object.
	 * @param classOfElements a {@link java.lang.Class} object.
	 * @throws java.io.IOException if any.
	 * @throws java.io.EOFException if any.
	 */
	public LuposObjectInputStream(final InputStream arg0, final Class<? extends E> classOfElements) throws IOException, EOFException {
		super(arg0);
		this.is = arg0;
		this.classOfElements = classOfElements;
	}

	/**
	 * <p>readLuposObject.</p>
	 *
	 * @return a E object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 * @throws java$net$URISyntaxException if any.
	 */
	public E readLuposObject() throws IOException, ClassNotFoundException, URISyntaxException {
		return Registration.deserializeWithoutId(this.classOfElements, this);
	}

	/**
	 * <p>readLuposObject.</p>
	 *
	 * @param classOfElements a {@link java.lang.Class} object.
	 * @param <TT> a TT object.
	 * @return a TT object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 * @throws java$net$URISyntaxException if any.
	 */
	public <TT> TT readLuposObject(final Class classOfElements) throws IOException, ClassNotFoundException, URISyntaxException {
		return (TT) Registration.deserializeWithoutId(classOfElements, this);
	}

	private Bindings previousBindings = null;

	private Triple lastTriple = null;

	// the last stored string as byte array...
	protected byte[] lastString = null;

	/**
	 * <p>readLuposTriple.</p>
	 *
	 * @return a {@link lupos.datastructures.items.Triple} object.
	 * @throws java.io.IOException if any.
	 * @throws java$net$URISyntaxException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public Triple readLuposTriple() throws IOException, URISyntaxException, ClassNotFoundException {
		this.lastTriple = InputHelper.readLuposTriple(this.lastTriple, this.is);
		return this.lastTriple;
	}
	/**
	 * <p>readLuposTripleKey.</p>
	 *
	 * @return a {@link lupos.datastructures.items.TripleKey} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public TripleKey readLuposTripleKey() throws IOException, ClassNotFoundException {
		// it is expected that the triple key contains the key computed from the
		// last read triple
		return InputHelper.readLuposTripleKey(this.lastTriple, this.is);
	}

	/**
	 * <p>readLuposDifferenceString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public String readLuposDifferenceString() throws IOException {
		this.lastString = InputHelper.readLuposDifferenceString(this.lastString, this.is);
		if(this.lastString==null){
			return null;
		} else {
			return new String(this.lastString, LuposObjectInputStream.UTF8);
		}
	}

	/**
	 * <p>readLuposBindings.</p>
	 *
	 * @return a {@link lupos.datastructures.bindings.Bindings} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public Bindings readLuposBindings() throws IOException, ClassNotFoundException {
		this.previousBindings = InputHelper.readLuposBindings(this.previousBindings, this.is);
		return this.previousBindings;
	}

	/**
	 * <p>readLuposDiskCollection.</p>
	 *
	 * @return a {@link lupos.datastructures.dbmergesortedds.DiskCollection} object.
	 * @throws java.lang.ClassNotFoundException if any.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings("rawtypes")
	public DiskCollection readLuposDiskCollection() throws ClassNotFoundException, IOException {
		return DiskCollection.readAndCreateLuposObject(this);
	}

	/**
	 * <p>readLuposCollection.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection readLuposCollection() throws IOException, ClassNotFoundException {
		final int size = this.is.read();
		if (size == 255) {
			return DiskCollection.readAndCreateLuposObject(this);
		} else {
			final LinkedList ll = new LinkedList();
			final Class type = Registration.deserializeId(this)[0];
			for (int i = 0; i < size; i++) {
				try {
					ll.add(Registration.deserializeWithoutId(type, this));
				} catch (final URISyntaxException e) {
					e.printStackTrace();
					throw new IOException(e.getMessage());
				}
			}
			return ll;
		}
	}

	/**
	 * <p>readLuposTreeSet.</p>
	 *
	 * @return a E object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public E readLuposTreeSet() throws IOException, ClassNotFoundException {
		final Comparator comparator = (Comparator) this.readObject();
		final int size = InputHelper.readLuposInt(this.is);
		if (size == 0) {
			return (E) new TreeSet(comparator);
		}
		final Class c = Registration.deserializeId(this)[0];
		final TreeSet ts = new TreeSet(comparator);
		for (int i = 0; i < size; i++) {
			try {
				ts.add(Registration.deserializeWithoutId(c, this));
			} catch (final URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
		}
		return (E) ts;
	}

	/**
	 * <p>readLuposSortedSet.</p>
	 *
	 * @return a E object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public E readLuposSortedSet() throws IOException, ClassNotFoundException {
		final Comparator comparator = (Comparator) this.readObject();
		final int size = InputHelper.readLuposInt(this.is);
		if (size < 0) {
			return null;
		}
		if (size == 0){
			final SortConfiguration sortConfiguration = new SortConfiguration();
			sortConfiguration.useReplacementSelection(2, 2);
			return (E) new DBMergeSortedSet(sortConfiguration, comparator, null);
		}
		final Class type = Registration.deserializeId(this)[0];
		final SortedSet ms;
		if (size < memoryLimit) {
			if (type == Triple.class) {
				ms = new TreeSet<Triple>(comparator);
			} else {
				ms = new TreeSet(comparator);
			}
		} else {
			final SortConfiguration sortConfiguration = new SortConfiguration();
			sortConfiguration.useReplacementSelection(2, 2);

			if (type == Triple.class) {
				ms = new DBMergeSortedSet(sortConfiguration, comparator, Triple.class);
			} else {
				ms = new DBMergeSortedSet(sortConfiguration, comparator, null);
			}
		}
		for (int i = 0; i < size; i++) {
			try {
				ms.add(Registration.deserializeWithoutId(type, this));
			} catch (final URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
		}
		return (E) ms;
	}

	/**
	 * <p>readLuposSetImplementation.</p>
	 *
	 * @return a {@link lupos.datastructures.smallerinmemorylargerondisk.SetImplementation} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SetImplementation readLuposSetImplementation() throws IOException, ClassNotFoundException {
		final int size = InputHelper.readLuposInt(this.is);
		if (size < 0){
			return null;
		}
		final SetImplementation set = new SetImplementation();
		if(size==0){
			return set;
		}
		final Class type = Registration.deserializeId(this)[0];
		for (int i = 0; i < size; i++) {
			try {
				set.add((Serializable)Registration.deserializeWithoutId(type, this));
			} catch (final URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
		}
		return set;
	}

	/**
	 * <p>readLuposSortedMap.</p>
	 *
	 * @return a E object.
	 * @throws java.io.IOException if any.
	 * @throws java$net$URISyntaxException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public E readLuposSortedMap() throws IOException, URISyntaxException, ClassNotFoundException {
		final int type = InputHelper.readLuposByte(this.is);
		if (type == 1) {
			return this.readLuposDBMergeSortedMapBasic();
		} else {
			System.err.println("LuposObjectInputStream: Not supported!");
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private E readLuposDBMergeSortedMapBasic() throws IOException, ClassNotFoundException {
		final Comparator comparator = (Comparator) this.readObject();
		final int size = InputHelper.readLuposInt(this.is);
		if (size < 0) {
			return null;
		}

		final SortConfiguration sortConfiguration = new SortConfiguration();
		sortConfiguration.useReplacementSelection(2, 2);

		final DBMergeSortedMap ms = new DBMergeSortedMap(sortConfiguration, comparator, null);
		if (size == 0) {
			return (E) ms;
		}
		final Class typeKey = Registration.deserializeId(this)[0];
		final Class typeValue = Registration.deserializeId(this)[0];
		for (int i = 0; i < size; i++) {
			Serializable value;
			Serializable key;
			try {
				value = (Serializable) Registration.deserializeWithoutId(typeValue, this);
				key = (Serializable) Registration.deserializeWithoutId(typeKey, this);
				ms.put(key, value);
			} catch (final URISyntaxException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		return (E) ms;
	}

	/**
	 * <p>readLuposEntry.</p>
	 *
	 * @return a {@link lupos.datastructures.dbmergesortedds.Entry} object.
	 * @throws java.lang.ClassNotFoundException if any.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings("rawtypes")
	public Entry readLuposEntry() throws ClassNotFoundException, IOException {
		return InputHelper.readLuposEntry(this);
	}
}
