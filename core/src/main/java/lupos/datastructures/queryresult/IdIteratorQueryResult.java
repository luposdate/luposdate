
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
package lupos.datastructures.queryresult;

import lupos.datastructures.bindings.Bindings;
import lupos.engine.operators.index.adaptedRDF3X.MergeIndicesTripleIterator;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.util.ImmutableIterator;
public class IdIteratorQueryResult extends IteratorQueryResult {

	private final MergeIndicesTripleIterator itt;
	private int idOfLastElement;

	/**
	 * <p>Constructor for IdIteratorQueryResult.</p>
	 *
	 * @param itt a {@link lupos.engine.operators.index.adaptedRDF3X.MergeIndicesTripleIterator} object.
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 */
	public IdIteratorQueryResult(final MergeIndicesTripleIterator itt,
			final TriplePattern tp) {
		super(null);
		this.itt = itt;
		this.itb = new ImmutableIterator<Bindings>() {
			Bindings next = this.computeNext();
			int idOfLastElementIterator;

			@Override
			public boolean hasNext() {
				return (this.next != null);
			}

			@Override
			public Bindings next() {
				if (this.next == null) {
					return null;
				}
				final Bindings znext = this.next;
				IdIteratorQueryResult.this.idOfLastElement = this.idOfLastElementIterator;
				this.next = this.computeNext();
				return znext;
			}

			private Bindings computeNext() {
				while (itt.hasNext()) {
					// also consider inner joins in triple patterns like ?a ?a
					// ?b.
					final Bindings znext = tp.process(itt.next(), false, itt
							.getIdOfLastElement());
					if (znext != null) {
						this.idOfLastElementIterator = itt.getIdOfLastElement();
						return znext;
					}
				}
				return null;
			}
		};
	}

	// return the id of the indices used in the case e.g. that there are
	// several default graphs...
	/**
	 * <p>getIDOfLastBinding.</p>
	 *
	 * @return a int.
	 */
	public int getIDOfLastBinding() {
		return this.idOfLastElement;
	}

	/**
	 * <p>getMaxId.</p>
	 *
	 * @return a int.
	 */
	public int getMaxId() {
		return this.itt.getMaxId();
	}
}
