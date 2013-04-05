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
package lupos.datastructures.queryresult;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.engine.operators.index.adaptedRDF3X.MergeIndicesTripleIterator;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class IdIteratorQueryResult extends IteratorQueryResult {

	private final MergeIndicesTripleIterator itt;
	private int idOfLastElement;

	public IdIteratorQueryResult(final MergeIndicesTripleIterator itt,
			final TriplePattern tp) {
		super(null);
		this.itt = itt;
		this.itb = new Iterator<Bindings>() {
			Bindings next = computeNext();
			int idOfLastElementIterator;

			public boolean hasNext() {
				return (next != null);
			}

			public Bindings next() {
				if (next == null)
					return null;
				final Bindings znext = next;
				idOfLastElement = idOfLastElementIterator;
				next = computeNext();
				return znext;
			}

			private Bindings computeNext() {
				while (itt.hasNext()) {
					// also consider inner joins in triple patterns like ?a ?a
					// ?b.
					final Bindings znext = tp.process(itt.next(), false, itt
							.getIdOfLastElement());
					if (znext != null) {
						idOfLastElementIterator = itt.getIdOfLastElement();
						return znext;
					}
				}
				return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	// return the id of the indices used in the case e.g. that there are
	// several default graphs...
	public int getIDOfLastBinding() {
		return idOfLastElement;
	}

	public int getMaxId() {
		return itt.getMaxId();
	}
}
