
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
package lupos.engine.operators.singleinput.modifiers;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings;
import lupos.misc.util.ImmutableIterator;
public class SortLimit extends SingleInputOperator {

	private ComparatorBindings comparator;
	private Bindings[] smallestBindings;
	private Bindings max = null;
	private int posMax = -1;
	private int pos = 0;

	/**
	 * <p>Constructor for SortLimit.</p>
	 *
	 * @param comparator a {@link lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings} object.
	 * @param limit a int.
	 */
	public SortLimit(final ComparatorBindings comparator, final int limit) {
		this.setComparator(comparator);
		this.setLimit(limit);
	}

	/**
	 * <p>Constructor for SortLimit.</p>
	 */
	public SortLimit() {
		this.comparator=null;
		this.smallestBindings=null;
	}


	/**
	 * <p>Setter for the field <code>comparator</code>.</p>
	 *
	 * @param comp a {@link lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings} object.
	 */
	public void setComparator(final ComparatorBindings comp){
		this.comparator=comp;
	}

	/**
	 * <p>setLimit.</p>
	 *
	 * @param limit a int.
	 */
	public void setLimit(final int limit){
		this.smallestBindings = new Bindings[limit];
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (this.pos < this.smallestBindings.length && itb.hasNext()) {
			final Bindings b = itb.next();
			if (this.max == null || this.comparator.compare(b, this.max) > 0) {
				this.posMax = this.pos;
				this.max = b;
			}
			this.smallestBindings[this.pos++] = b;
		}
		if (itb.hasNext()) {
			while (itb.hasNext()) {
				final Bindings b = itb.next();
				if (this.comparator.compare(b, this.max) < 0) {
					this.smallestBindings[this.posMax] = b;
					this.max = b;
					// find new maximum
					for (int i = 0; i < this.smallestBindings.length; i++) {
						if (this.comparator.compare(this.smallestBindings[i], this.max) > 0) {
							this.max = this.smallestBindings[i];
							this.posMax = i;
						}
					}
				}
			}
		}
		if (itb instanceof ParallelIterator) {
			((ParallelIterator<Bindings>) itb).close();
		}
		return QueryResult.createInstance(new ImmutableIterator<Bindings>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return this.i < SortLimit.this.pos;
			}

			@Override
			public Bindings next() {
				if (this.hasNext()) {
					return SortLimit.this.smallestBindings[this.i++];
				} else {
					return null;
				}
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString()+" " + this.smallestBindings.length;
	}
}
