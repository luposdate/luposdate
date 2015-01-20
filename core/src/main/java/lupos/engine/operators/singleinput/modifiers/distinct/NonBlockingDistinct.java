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
package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.misc.util.ImmutableIterator;

public abstract class NonBlockingDistinct extends Distinct {

	private static final long serialVersionUID = -5670129779878953225L;

	final protected Set<Bindings> bindings;

	public NonBlockingDistinct(final Set<Bindings> setOfBindings){
		this.bindings = setOfBindings;
	}

	@Override
	public QueryResult process(final QueryResult _bindings, final int operandID) {
		final Iterator<Bindings> itb = _bindings.oneTimeIterator();
		if (!itb.hasNext()) {
			return null;
		} else {
			return QueryResult.createInstance(new ImmutableIterator<Bindings>() {
				Bindings next = null;

				@Override
				public boolean hasNext() {
					if (this.next != null) {
						return true;
					}
					if (itb.hasNext()) {
						this.next = this.next();
						if (this.next != null) {
							return true;
						}
					}
					return false;
				}

				@Override
				public Bindings next() {
					if (this.next != null) {
						final Bindings znext = this.next;
						this.next = null;
						return znext;
					}
					while (itb.hasNext()) {
						final Bindings b = itb.next();
						if (!NonBlockingDistinct.this.bindings.contains(b)) {
							NonBlockingDistinct.this.bindings.add(b);
							return b;
						}
					}
					return null;
				}
			});
		}
	}

	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult, final int operandID) {
		// problem: it does not count the number of occurrences of a binding
		// i.e. { ?a=<a> }, { ?a=<a> } and delete { ?a=<a> } will result in
		// {} instead of { ?a=<a> }!!!!!!
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext()){
			this.bindings.remove(itb.next());
		}
		return null;
	}

	@Override
	public void deleteQueryResult(final int operandID) {
		this.bindings.clear();
	}

	@Override
	protected boolean isPipelineBreaker() {
		return false;
	}
}