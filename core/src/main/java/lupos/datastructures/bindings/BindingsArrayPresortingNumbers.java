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
package lupos.datastructures.bindings;

import java.util.HashMap;
import java.util.Map.Entry;

import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rdf.Prefix;
public class BindingsArrayPresortingNumbers extends BindingsArray {

	/**
	 * <p>Constructor for BindingsArrayPresortingNumbers.</p>
	 *
	 * @param bindingsFactory a {@link lupos.datastructures.bindings.BindingsFactory} object.
	 */
	public BindingsArrayPresortingNumbers(final BindingsFactory bindingsFactory) {
		super(bindingsFactory);
	}

	protected HashMap<TriplePattern, HashMap<Object, Container>> presortingnumbers = null;

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString() + " presorting numbers:" + this.presortingnumbers + "\n";
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final Prefix prefix) {
		return super.toString(prefix) + " presorting numbers:" + this.presortingnumbers + "\n";
	}


	/** {@inheritDoc} */
	@Override
	public void init() {
		super.init();
	}

	/** {@inheritDoc} */
	@Override
	public void addPresortingNumber(final TriplePattern tp,
			final Object orderPattern, final int pos, final int max,
			final int id) {
		if (this.presortingnumbers == null) {
			this.presortingnumbers = new HashMap<TriplePattern, HashMap<Object, Container>>();
		}
		HashMap<Object, Container> hm = this.presortingnumbers.get(tp);
		if (hm == null) {
			hm = new HashMap<Object, Container>();
		}
		hm.put(orderPattern, new Container(pos, max, id));
		this.presortingnumbers.put(tp, hm);
	}

	/**
	 * <p>getPos.</p>
	 *
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param orderPattern a {@link java.lang.Object} object.
	 * @return a int.
	 */
	public int getPos(final TriplePattern tp, final Object orderPattern) {
		if (this.presortingnumbers == null) {
			return -1;
		}
		final HashMap<Object, Container> hm = this.presortingnumbers.get(tp);
		if (hm == null) {
			// System.out
			// .println(
			// "Error: Asked presorting number is not available in Binding:"
			// + tp + "," + orderPattern);
		} else {
			final Container c = hm.get(orderPattern);
			if (c == null) {
				// System.out
				// .println(
				// "Error: Asked presorting number is not available in Binding:"
				// + tp + "," + orderPattern);
			} else {
				return c.pos;
			}
		}
		return -1;
	}

	/**
	 * <p>getMax.</p>
	 *
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param orderPattern a {@link java.lang.Object} object.
	 * @return a int.
	 */
	public int getMax(final TriplePattern tp, final Object orderPattern) {
		if (this.presortingnumbers == null) {
			return -1;
		}
		final HashMap<Object, Container> hm = this.presortingnumbers.get(tp);
		if (hm == null) {
			// System.out
			// .println(
			// "Error: Asked presorting number is not available in Binding:"
			// + tp + "," + orderPattern);
		} else {
			final Container c = hm.get(orderPattern);
			if (c == null) {
				// System.out
				// .println(
				// "Error: Asked presorting number is not available in Binding:"
				// + tp + "," + orderPattern);
			} else {
				return c.max;
			}
		}
		return -1;
	}

	/**
	 * <p>getId.</p>
	 *
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param orderPattern a {@link java.lang.Object} object.
	 * @return a int.
	 */
	public int getId(final TriplePattern tp, final Object orderPattern) {
		if (this.presortingnumbers == null) {
			return -1;
		}
		final HashMap<Object, Container> hm = this.presortingnumbers.get(tp);
		if (hm == null) {
			// System.out
			// .println(
			// "Error: Asked presorting number is not available in Binding:"
			// + tp + "," + orderPattern);
		} else {
			final Container c = hm.get(orderPattern);
			if (c == null) {
				// System.out
				// .println(
				// "Error: Asked presorting number is not available in Binding:"
				// + tp + "," + orderPattern);
			} else {
				return c.id;
			}
		}
		return -1;
	}

	private class Container {
		public int pos;
		public int max;
		public int id;

		public Container(final int pos, final int max, final int id) {
			this.pos = pos;
			this.max = max;
			this.id = id;
		}

		@Override
		public String toString() {
			return "(pos:" + this.pos + ", max:" + this.max + ", id:" + this.id + ")";
		}
	}

	/** {@inheritDoc} */
	@Override
	public BindingsArrayPresortingNumbers clone() {
		final BindingsArrayPresortingNumbers other = new BindingsArrayPresortingNumbers(this.bindingsFactory);
		// System.arraycopy(this.literals, 0, other.literals, 0,
		// this.literals.length);
		other.cloneLiterals(this.getLiterals());

		if (this.presortingnumbers != null) {
			for (final Entry<TriplePattern, HashMap<Object, Container>> entry : this.presortingnumbers
					.entrySet()) {
				for (final Entry<Object, Container> innerEntry : entry
						.getValue().entrySet()) {
					other.addPresortingNumber(entry.getKey(), innerEntry
							.getKey(), innerEntry.getValue().pos, innerEntry
							.getValue().max, innerEntry.getValue().id);
				}
			}
		}

		return other;
	}

	/** {@inheritDoc} */
	@Override
	public void addAllPresortingNumbers(final Bindings bindings) {
		if (!(bindings instanceof BindingsArrayPresortingNumbers)) {
			return;
		}
		if (((BindingsArrayPresortingNumbers) bindings).presortingnumbers == null) {
			return;
		}
		for (final Entry<TriplePattern, HashMap<Object, Container>> entry : ((BindingsArrayPresortingNumbers) bindings).presortingnumbers
				.entrySet()) {
			for (final Entry<Object, Container> innerEntry : entry.getValue()
					.entrySet()) {
				this.addPresortingNumber(entry.getKey(), innerEntry.getKey(),
						innerEntry.getValue().pos, innerEntry.getValue().max,
						innerEntry.getValue().id);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public BindingsArrayPresortingNumbers createInstance(){
		return new BindingsArrayPresortingNumbers(this.bindingsFactory);
	}
}
