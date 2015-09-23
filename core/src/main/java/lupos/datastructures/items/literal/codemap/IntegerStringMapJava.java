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
package lupos.datastructures.items.literal.codemap;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StandardNodeDeSerializer;
import lupos.datastructures.smallerinmemorylargerondisk.MapImplementation;
public class IntegerStringMapJava implements IntegerStringMap {
	private Map<Integer, String> m;
	private Map<Integer, String> original;

	/**
	 * <p>Constructor for IntegerStringMapJava.</p>
	 *
	 * @param mapType a {@link lupos.datastructures.items.literal.LiteralFactory.MapType} object.
	 */
	public IntegerStringMapJava(final MapType mapType) {
		switch (mapType) {
		case HASHMAP:
			original = new HashMap<Integer, String>();
			break;
		case DBBPTREE:
			try {
				original = new lupos.datastructures.paged_dbbptree.DBBPTree<Integer, String>(
						300, 300,
						new StandardNodeDeSerializer<Integer, String>(
								Integer.class, String.class));
				((lupos.datastructures.paged_dbbptree.DBBPTree)original).setName("Dictionary: Integer->String");
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			break;
		default:
			original = new MapImplementation<Integer, String>();
			break;
		}
		if (original != null)
			m = Collections.synchronizedMap(original);
	}

	/**
	 * <p>Constructor for IntegerStringMapJava.</p>
	 *
	 * @param map a {@link java.util.Map} object.
	 */
	public IntegerStringMapJava(final Map<Integer, String> map) {
		original = map;
		m = Collections.synchronizedMap(map);
	}

	/** {@inheritDoc} */
	public String get(final int key) {
		return m.get(key);
	}

	/** {@inheritDoc} */
	public void put(final int key, final String s) {
		m.put(key, s);
	}

	/**
	 * <p>size.</p>
	 *
	 * @return a int.
	 */
	public int size() {
		return m.size();
	}

	/**
	 * <p>clear.</p>
	 */
	public void clear() {
		m.clear();
	}

	/**
	 * <p>getMap.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<Integer, String> getMap() {
		return m;
	}

	/** {@inheritDoc} */
	public void forEachValue(final TProcedureValue<String> arg0) {
		for (final String s : m.values()) {
			arg0.execute(s);
		}
	}

	/** {@inheritDoc} */
	public void forEachEntry(final TProcedureEntry<Integer, String> arg0) {
		for (final Entry<Integer, String> entry : m.entrySet()) {
			arg0.execute(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * <p>getOriginalMap.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<Integer, String> getOriginalMap() {
		return original;
	}
}
