/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.util.NoSuchElementException;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.parallel.BoundedBuffer;

public class ParallelIteratorQueryResult extends IteratorQueryResult {

	ParallelIterator<Bindings> parallelitb;

	public ParallelIteratorQueryResult(final ParallelIterator<Bindings> itb) {
		super(itb);
		this.parallelitb = itb;
	}

	public ParallelIteratorQueryResult(
			final BoundedBuffer<Bindings> queueParameter) {
		this(new ParallelIterator<Bindings>() {
			private final BoundedBuffer<Bindings> queue = queueParameter;

			public boolean hasNext() {
				try {
					return queue.hasNext();
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
					return false;
				}
			}

			public Bindings next() throws NoSuchElementException {
				try {
					return queue.get();
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
					return null;
				}
			}

			/**
			 * There's no reason to delete an element.
			 * 
			 * @throws UnsupportedOperationException
			 */
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException();
			}

			/**
			 * Since we're done after this object is collected, this also aborts
			 * the helper thread.
			 */
			@Override
			protected void finalize() throws Throwable {
				queue.stopIt();
			}

			public void close() {
				queue.stopIt();
			}
		});
	}

	@Override
	public void release() {
		if (parallelitb != null) {
			parallelitb.close();
			parallelitb = null;
		}
		super.release();
	}
}
