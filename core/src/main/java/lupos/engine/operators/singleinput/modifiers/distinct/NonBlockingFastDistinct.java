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

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.paged_set.PagedHashMultiSet;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.misc.BitVector;
import lupos.misc.Tuple;

public class NonBlockingFastDistinct extends Distinct {

	private static final long serialVersionUID = -5670129779878953225L;

	protected final Set<Bindings> bindings;

	public static int BITVECTORSIZE = 64 * 1024;

	public NonBlockingFastDistinct(final Set<Bindings> setOfBindings){
		this.bindings = setOfBindings;
	}

	public NonBlockingFastDistinct() {
		this(new PagedHashMultiSet<Bindings>(Bindings.class));
	}

	@Override
	public QueryResult process(final QueryResult _bindings, final int operandID) {
		if(_bindings.isEmpty()){
			return null;
		}

		final BoundedBuffer<Bindings> distinctElements = new BoundedBuffer<Bindings>();
		final BoundedBuffer<Bindings> toBeChecked = new BoundedBuffer<Bindings>();
		final BoundedBuffer<Tuple<Bindings, Boolean>> toBeAddedToSet = new BoundedBuffer<Tuple<Bindings, Boolean>>();

		final Feeder feeder = new Feeder(_bindings, toBeChecked);
		final FastDecider fastDecider = new FastDecider( distinctElements, toBeChecked, toBeAddedToSet, this.bindings);
		final DetailedDecider detailedDecider = new DetailedDecider(distinctElements, toBeAddedToSet, this.bindings);

		feeder.start();
		fastDecider.start();
		detailedDecider.start();


			return QueryResult.createInstance(new ParallelIterator<Bindings>() {
				@Override
				public boolean hasNext() {
					try {
						return distinctElements.hasNext();
					} catch (final InterruptedException e) {
						System.err.println(e);
						e.printStackTrace();
					}
					return false;
				}

				@Override
				public Bindings next() {
					if(this.hasNext()){
						try {
							return distinctElements.get();
						} catch (final InterruptedException e) {
							System.err.println(e);
							e.printStackTrace();
						}
					}
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void close() {
					feeder.stopIt();
					distinctElements.stopIt();
					toBeChecked.stopIt();
					toBeAddedToSet.stopIt();
				}

				@Override
				public void finalize(){
					this.close();
				}
			});

	}

	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult, final int operandID) {
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

	@Override
	public void finalize(){
		if(this.bindings instanceof PagedHashMultiSet){
			try {
				((PagedHashMultiSet<Bindings>)this.bindings).release();
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

	public static class Feeder extends Thread {

		protected final QueryResult queryResult;
		protected final BoundedBuffer<Bindings> toBeChecked;
		protected volatile boolean stopped = false;

		public Feeder(final QueryResult queryResult, final BoundedBuffer<Bindings> toBeChecked){
			this.queryResult = queryResult;
			this.toBeChecked = toBeChecked;
		}

		@Override
		public void run(){
			try {
				final Iterator<Bindings> itb = this.queryResult.oneTimeIterator();
				while (!this.stopped && itb.hasNext()){
					this.toBeChecked.put(itb.next());
				}
				this.toBeChecked.endOfData();
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		public void stopIt(){
			this.stopped = true;
		}
	}

	public static class FastDecider extends Thread {

		protected final BoundedBuffer<Bindings> distinctElements;
		protected final BoundedBuffer<Bindings> toBeChecked;
		protected final BoundedBuffer<Tuple<Bindings, Boolean>> toBeAddedToSet;

		protected final BitVector bitVector = new BitVector(NonBlockingFastDistinct.BITVECTORSIZE);

		public FastDecider(final BoundedBuffer<Bindings> distinctElements, final BoundedBuffer<Bindings> toBeChecked, final BoundedBuffer<Tuple<Bindings, Boolean>> toBeAddedToSet, final Set<Bindings> bindings){
			this.distinctElements = distinctElements;
			this.toBeChecked = toBeChecked;
			this.toBeAddedToSet = toBeAddedToSet;
			// first just for initialization purposes set up bit vector with existing elements of our set of bindings
			for(final Bindings b: bindings){
				final int hashCode = Math.abs(b.hashCode() % NonBlockingFastDistinct.BITVECTORSIZE);
				this.bitVector.set(hashCode);
			}
		}

		@Override
		public void run() {
			try {
				while(this.toBeChecked.hasNext()){
					final Bindings next = this.toBeChecked.get();
					if(next==null){
						break;
					}
					final int hashCode = Math.abs(next.hashCode() % NonBlockingFastDistinct.BITVECTORSIZE);
					if(this.bitVector.get(hashCode)){
						// Collision found, check if it is really a duplicate.
						// This is done in parallel by another thread.
						this.toBeAddedToSet.put(new Tuple<Bindings, Boolean>(next, true));
					} else {
						// surely no duplicate
						this.bitVector.set(hashCode);
						this.distinctElements.put(next);
						this.toBeAddedToSet.put(new Tuple<Bindings, Boolean>(next, false));
					}
				}
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			// all has been fast checked!
			this.toBeAddedToSet.endOfData();
		}
	}

	public static class DetailedDecider extends Thread {

		protected final BoundedBuffer<Bindings> distinctElements;
		protected final BoundedBuffer<Tuple<Bindings, Boolean>> toBeAddedToSet;
		protected final Set<Bindings> bindings;

		public DetailedDecider(final BoundedBuffer<Bindings> distinctElements, final BoundedBuffer<Tuple<Bindings, Boolean>> toBeAddedToSet, final Set<Bindings> bindings){
			this.distinctElements = distinctElements;
			this.toBeAddedToSet = toBeAddedToSet;
			this.bindings = bindings;
		}

		@Override
		public void run() {
			try {
				while(this.toBeAddedToSet.hasNext()){
					final Tuple<Bindings, Boolean> next = this.toBeAddedToSet.get();
					if(next==null){
						break;
					}
					final boolean result = this.bindings.add(next.getFirst());
					if(result && next.getSecond()){
						this.distinctElements.put(next.getFirst());
					}
				}
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			// all has been detailed checked!
			this.distinctElements.endOfData();
		}
	}
}