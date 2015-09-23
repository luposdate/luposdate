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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.dbmergesortedds.Entry;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.smallerinmemorylargerondisk.SetImplementation;
import lupos.io.helper.OutHelper;
public class LuposObjectOutputStream extends ObjectOutputStream {

	public OutputStream os;

	/**
	 * <p>Constructor for LuposObjectOutputStream.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public LuposObjectOutputStream() throws IOException {
	}

	/**
	 * <p>Constructor for LuposObjectOutputStream.</p>
	 *
	 * @param arg0 a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public LuposObjectOutputStream(final OutputStream arg0) throws IOException {
		super(arg0);
		this.os = arg0;
	}

	protected Triple lastTriple = null;

	protected byte[] lastString = null;

	protected Bindings previousBindings = null;

	/**
	 * <p>writeLuposObject.</p>
	 *
	 * @param arg0 a {@link java.lang.Object} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeLuposObject(final Object arg0) throws IOException {
		Registration.serializeWithoutId(arg0, this);
	}

	/**
	 * <p>writeLuposTriple.</p>
	 *
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeLuposTriple(final Triple t) throws IOException {
		OutHelper.writeLuposTriple(t, this.lastTriple, this.os);
		this.lastTriple = t;
	}

	/**
	 * <p>writeLuposTripleKey.</p>
	 *
	 * @param tk a {@link lupos.datastructures.items.TripleKey} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeLuposTripleKey(final TripleKey tk) throws IOException {
		OutHelper.writeLuposTripleKey(tk, this.lastTriple, this.os);
	}

	/**
	 * <p>writeLuposDifferenceString.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeLuposDifferenceString(final String s) throws IOException {
		this.lastString = OutHelper.writeLuposDifferenceString(s, this.lastString, this.os);
	}

	/**
	 * <p>writeLuposBindings.</p>
	 *
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeLuposBindings(final Bindings bindings) throws IOException {
		OutHelper.writeLuposBindings(bindings, this.previousBindings, this.os);
		this.previousBindings = bindings;
	}

	/**
	 * <p>writeLuposDiskCollection.</p>
	 *
	 * @param dc a {@link lupos.datastructures.dbmergesortedds.DiskCollection} object.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings("rawtypes")
	public void writeLuposDiskCollection(final DiskCollection dc) throws IOException {
		dc.writeLuposObject(this);
	}

	/**
	 * <p>writeLuposCollection.</p>
	 *
	 * @param t a {@link java.util.Collection} object.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void writeLuposCollection(final Collection t) throws IOException {
		if (t.size() > 200) {
			this.os.write(255);
			final DiskCollection dc;
			if (t.size() > 0) {
				dc = new DiskCollection(t.iterator().next().getClass());
			} else {
				dc = new DiskCollection(Object.class);
			}
			dc.addAll(t);
			dc.writeLuposObject(this);
		} else {
			this.os.write(t.size());
			if (t.size() > 0) {
				Registration.serializeId(t.iterator().next(), this);
				for (final Object o : t) {
					Registration.serializeWithoutId(o, this);
				}
			}
		}
	}

	/**
	 * <p>writeLuposTreeSet.</p>
	 *
	 * @param t a {@link java.util.TreeSet} object.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings("rawtypes")
	public void writeLuposTreeSet(final TreeSet t) throws IOException {
		this.writeObject(t.comparator());
		OutHelper.writeLuposInt(t.size(), this.os);
		if (t.size() > 0) {
			Registration.serializeId(t.iterator().next(), this);
		}
		for (final Object o : t) {
			Registration.serializeWithoutId(o, this);
		}
	}

	/**
	 * <p>writeLuposSortedSet.</p>
	 *
	 * @param t a {@link java.util.SortedSet} object.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings("rawtypes")
	public void writeLuposSortedSet(final SortedSet t) throws IOException {
		this.writeObject(t.comparator());
		OutHelper.writeLuposInt(t.size(), this.os);
		if (t.size() > 0) {
			Registration.serializeId(t.iterator().next(), this);
		}
		for (final Object o : t) {
			Registration.serializeWithoutId(o, this);
		}

	}

	/**
	 * <p>writeLuposSetImplementation.</p>
	 *
	 * @param t a {@link lupos.datastructures.smallerinmemorylargerondisk.SetImplementation} object.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings("rawtypes")
	public void writeLuposSetImplementation(final SetImplementation t) throws IOException {
		OutHelper.writeLuposInt(t.size(), this.os);
		if (t.size() > 0){
			Registration.serializeId(t.iterator().next(), this);
		}
		for (final Object o : t) {
			Registration.serializeWithoutId(o, this);
		}
	}

	/**
	 * <p>writeLuposSortedMap.</p>
	 *
	 * @param t a {@link java.util.SortedMap} object.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void writeLuposSortedMap(final SortedMap t) throws IOException {
		OutHelper.writeLuposByte((byte) 1, this.os);
		this.writeObject(t.comparator());
		OutHelper.writeLuposInt(t.size(), this.os);
		if (t.size() == 0) {
			return;
		}
		boolean flag = true;
		for (final Object mapentry : t.entrySet()) {
			final Object value = ((Map.Entry<Object, Object>) mapentry).getValue();
			final Object key = ((Map.Entry<Object, Object>) mapentry).getKey();

			if (flag) {
				flag = false;
				Registration.serializeId(key, this);
				Registration.serializeId(value, this);
			}

			Registration.serializeWithoutId(value, this);
			Registration.serializeWithoutId(key, this);
		}
	}

	/**
	 * <p>writeLuposEntry.</p>
	 *
	 * @param e a {@link lupos.datastructures.dbmergesortedds.Entry} object.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void writeLuposEntry(final Entry e) throws IOException {
		OutHelper.writeLuposEntry(e, this);
	}
}
