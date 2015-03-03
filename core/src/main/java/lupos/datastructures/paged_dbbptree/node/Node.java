
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.datastructures.paged_dbbptree.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public abstract class Node<K extends Comparable<K> & Serializable, V extends Serializable> {
	public InputStream in;
	public int filename;
	public List<K> readKeys;

	protected Class<? super K> keyClass;
	protected Class<? super V> valueClass;

	/**
	 * <p>Constructor for Node.</p>
	 *
	 * @param keyClass a {@link java.lang.Class} object.
	 * @param valueClass a {@link java.lang.Class} object.
	 * @param size a int.
	 */
	protected Node(final Class<? super K> keyClass,
			final Class<? super V> valueClass, final int size) {
		this.keyClass = keyClass;
		this.valueClass = valueClass;
		this.readKeys = new ArrayList<K>(size);
	}

	/**
	 * <p>Getter for the field <code>in</code>.</p>
	 *
	 * @return a {@link java.io.InputStream} object.
	 */
	public InputStream getIn() {
		return this.in;
	}

	/**
	 * <p>Setter for the field <code>in</code>.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 */
	public void setIn(final InputStream in) {
		this.in = in;
	}

	/**
	 * <p>Getter for the field <code>filename</code>.</p>
	 *
	 * @return a int.
	 */
	public int getFilename() {
		return this.filename;
	}

	/**
	 * <p>Setter for the field <code>filename</code>.</p>
	 *
	 * @param filename a int.
	 */
	public void setFilename(final int filename) {
		this.filename = filename;
	}

	/**
	 * <p>getKeys.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<K> getKeys() {
		return this.readKeys;
	}

	/**
	 * <p>setKeys.</p>
	 *
	 * @param readKeys a {@link java.util.List} object.
	 */
	public void setKeys(final List<K> readKeys) {
		this.readKeys = readKeys;
	}

	/** {@inheritDoc} */
	@Override
	public void finalize() {
		try {
			if(this.in!=null){
				this.in.close();
			}
		} catch (final IOException e1) {
		}
	}
}
