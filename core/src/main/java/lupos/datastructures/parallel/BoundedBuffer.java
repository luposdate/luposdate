package lupos.datastructures.parallel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer<E> {
	final Lock lock = new ReentrantLock();
	final Condition notFull = lock.newCondition();
	final Condition notEmpty = lock.newCondition();

	private static int maxBuffer = 1000;
	private final int maxBufferLocal;
	private final Object[] buffer;
	private int count, takeptr, putptr;
	private boolean endOfData = false;
	private boolean stop = false;

	public BoundedBuffer() {
		this.buffer = new Object[maxBuffer];
		this.maxBufferLocal = maxBuffer;
	}

	public BoundedBuffer(final int maxBufferLocal) {
		this.buffer = new Object[maxBufferLocal];
		this.maxBufferLocal = maxBufferLocal;
	}

	public void put(final E bindings) throws InterruptedException {
		lock.lock();
		try {
			while (count == maxBufferLocal && !stop) {
				notFull.await();
			}
			if (count == maxBufferLocal && stop)
				return;
			buffer[putptr] = bindings;
			if (++putptr == maxBufferLocal)
				putptr = 0;
			++count;
			notEmpty.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public void put(final E bindings, final int freeSpace)
			throws InterruptedException {
		lock.lock();
		try {
			while (count >= maxBufferLocal - freeSpace && !stop) {
				notFull.await();
			}
			if (count == maxBufferLocal && stop)
				return;
			buffer[putptr] = bindings;
			if (++putptr == maxBufferLocal)
				putptr = 0;
			++count;
			notEmpty.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public void putFirst(final E bindings) throws InterruptedException {
		lock.lock();
		try {
			while (count == maxBufferLocal && !stop) {
				notFull.await();
			}
			if (count == maxBufferLocal && stop)
				return;
			if (--takeptr < 0)
				takeptr = maxBufferLocal - 1;
			buffer[takeptr] = bindings;
			++count;
			notEmpty.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public E get() throws InterruptedException {
		lock.lock();
		try {
			while (count == 0 && !endOfData && !stop) {
				notEmpty.await();
			}
			if ((count == 0 && endOfData) || stop)
				return null;
			final E bindings = getUnsynchronized();
			notFull.signalAll();
			return bindings;
		} finally {
			lock.unlock();
		}
	}

	public Object[] get(final int min, final int max)
			throws InterruptedException {
		lock.lock();
		try {
			while (count < min && !endOfData && !stop) {
				notEmpty.await();
			}
			if ((count == 0 && endOfData) || stop)
				return null;
			final int take = Math.min(max, count);
			final Object[] ea = new Object[take];
			for (int i = 0; i < take; i++)
				ea[i] = getUnsynchronized();
			notFull.signalAll();
			return ea;
		} finally {
			lock.unlock();
		}
	}

	private E getUnsynchronized() {
		final E bindings = (E) buffer[takeptr];
		if (++takeptr == maxBufferLocal)
			takeptr = 0;
		--count;
		return bindings;
	}

	public boolean hasNext() throws InterruptedException {
		lock.lock();
		try {
			if (count > 0)
				return true;
			while (count == 0 && !endOfData && !stop) {
				notEmpty.await();
			}
			if ((count == 0 && endOfData) || stop)
				return false;
			else
				return true;
		} finally {
			lock.unlock();
		}
	}

	public int size() {
		lock.lock();
		try {
			return count;
		} finally {
			lock.unlock();
		}
	}

	public void endOfData() {
		lock.lock();
		try {
			endOfData = true;
			notEmpty.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public void stopIt() {
		lock.lock();
		try {
			stop = true;
			notFull.signalAll();
			notEmpty.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public static int getMaxBuffer() {
		return maxBuffer;
	}

	public static void setMaxBuffer(final int maxBuffer) {
		BoundedBuffer.maxBuffer = maxBuffer;
	}

	public boolean isEmpty() {
		lock.lock();
		try {
			if (count == 0 && endOfData)
				return true;
			else
				return false;
		} finally {
			lock.unlock();
		}
	}

	public boolean isCurrentlyEmpty() {
		lock.lock();
		try {
			if (count == 0)
				return true;
			else
				return false;
		} finally {
			lock.unlock();
		}
	}

	public boolean isCurrentlyFull() {
		lock.lock();
		try {
			return count == maxBufferLocal;
		} finally {
			lock.unlock();
		}
	}
}
