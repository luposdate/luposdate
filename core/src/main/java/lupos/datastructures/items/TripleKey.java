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
package lupos.datastructures.items;

import java.io.Serializable;

import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
public class TripleKey implements Comparable<TripleKey>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6205248978579168911L;

	protected Triple triple;
	protected TripleComparator comp;

	/**
	 * <p>Constructor for TripleKey.</p>
	 */
	public TripleKey() {
	}

	/**
	 * <p>Constructor for TripleKey.</p>
	 *
	 * @param triple a {@link lupos.datastructures.items.Triple} object.
	 * @param comp a {@link lupos.datastructures.items.TripleComparator} object.
	 */
	public TripleKey(final Triple triple, final TripleComparator comp) {
		this.triple = triple;
		this.comp = comp;
	}

	/**
	 * <p>Constructor for TripleKey.</p>
	 *
	 * @param triple a {@link lupos.datastructures.items.Triple} object.
	 * @param order a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public TripleKey(final Triple triple, final RDF3XIndexScan.CollationOrder order) {
		this.triple = triple;
		this.comp = new TripleComparator(order);
		this.comp.makeNoneForNull(triple);
	}

	/**
	 * <p>compareTo.</p>
	 *
	 * @param arg0 a {@link lupos.datastructures.items.TripleKey} object.
	 * @return a int.
	 */
	public int compareTo(final TripleKey arg0) {
		return comp.compare(triple, arg0.triple);
	}

	/**
	 * <p>compareTo.</p>
	 *
	 * @param arg0 a {@link lupos.datastructures.items.Triple} object.
	 * @return a int.
	 */
	public int compareTo(final Triple arg0) {
		return comp.compare(triple, arg0);
	}

	/**
	 * <p>Getter for the field <code>triple</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.Triple} object.
	 */
	public Triple getTriple() {
		return triple;
	}

	/**
	 * <p>Setter for the field <code>triple</code>.</p>
	 *
	 * @param triple a {@link lupos.datastructures.items.Triple} object.
	 */
	public void setTriple(final Triple triple) {
		this.triple = triple;
	}

	/**
	 * <p>getTripleComparator.</p>
	 *
	 * @return a {@link lupos.datastructures.items.TripleComparator} object.
	 */
	public TripleComparator getTripleComparator() {
		return comp;
	}

	/**
	 * <p>setTripleComparator.</p>
	 *
	 * @param comp a {@link lupos.datastructures.items.TripleComparator} object.
	 */
	public void setTripleComparator(final TripleComparator comp) {
		this.comp = comp;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "TripleKey of " + triple.toString() + ", " + comp.toString();
	}
}
