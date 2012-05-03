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
