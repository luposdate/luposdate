/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;
import lupos.datastructures.smallerinmemorylargerondisk.MapImplementation;

public class StringIntegerMapJava implements StringIntegerMap {
	private Map<String, Integer> m;
	private Map<String, Integer> original;

	@SuppressWarnings("rawtypes")
	public StringIntegerMapJava(final MapType mapType) {
		switch (mapType) {
		case HASHMAP:
			this.original = new HashMap<String, Integer>();
			break;
		case DBBPTREE:
			try {
				this.original = new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(
						300, 300,
						new StandardNodeDeSerializer<String, Integer>(
								String.class, Integer.class));
				((lupos.datastructures.paged_dbbptree.DBBPTree)this.original).setName("Dictionary: String->Integer");
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			break;
		default:
			this.original = new MapImplementation<String, Integer>();
			break;
		}
		if (this.original != null)
			this.m = Collections.synchronizedMap(this.original);
	}

	public StringIntegerMapJava(final Map<String, Integer> map) {
		this.original = map;
		this.m = Collections.synchronizedMap(map);
	}

	@Override
	public Integer get(final String s) {
		return this.m.get(s);
	}

	@Override
	public void put(final String s, final int value) {
		this.m.put(s, value);
	}

	@Override
	public int size() {
		return this.m.size();
	}

	@Override
	public void clear() {
		this.m.clear();
	}

	public Map<String, Integer> getMap() {
		return this.m;
	}

	public Map<String, Integer> getOriginalMap() {
		return this.original;
	}
}