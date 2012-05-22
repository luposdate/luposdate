package lupos.datastructures.parallel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer<E> {
	final Lock lock = new ReentrantLock();
	final Condition notFull = this.lock.newCondition();
	final Condition notEmpty = this.lock.newCondition();

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
		this.lock.lock();
		try {
			while (this.count == this.maxBufferLocal && !this.stop) {
				this.notFull.await();
			}
			if (this.count == this.maxBufferLocal && this.stop)
				return;
			this.buffer[this.putptr] = bindings;
			if (++this.putptr == this.maxBufferLocal)
				this.putptr = 0;
			++this.count;
			this.notEmpty.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	public void put(final E bindings, final int freeSpace)
			throws InterruptedException {
		this.lock.lock();
		try {
			while (this.count >= this.maxBufferLocal - freeSpace && !this.stop) {
				this.notFull.await();
			}
			if (this.count == this.maxBufferLocal && this.stop)
				return;
			this.buffer[this.putptr] = bindings;
			if (++this.putptr == this.maxBufferLocal)
				this.putptr = 0;
			++this.count;
			this.notEmpty.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	public void putFirst(final E bindings) throws InterruptedException {
		this.lock.lock();
		try {
			while (this.count == this.maxBufferLocal && !this.stop) {
				this.notFull.await();
			}
			if (this.count == this.maxBufferLocal && this.stop)
				return;
			if (--this.takeptr < 0)
				this.takeptr = this.maxBufferLocal - 1;
			this.buffer[this.takeptr] = bindings;
			++this.count;
			this.notEmpty.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	public E get() throws InterruptedException {
		this.lock.lock();
		try {
			while (this.count == 0 && !this.endOfData && !this.stop) {
				this.notEmpty.await();
			}
			if ((this.count == 0 && this.endOfData) || this.stop)
				return null;
			final E bindings = getUnsynchronized();
			this.notFull.signalAll();
			return bindings;
		} finally {
			this.lock.unlock();
		}
	}

	public Object[] get(final int min, final int max)
			throws InterruptedException {
		this.lock.lock();
		try {
			while (this.count < min && !this.endOfData && !this.stop) {
				this.notEmpty.await();
			}
			if ((this.count == 0 && this.endOfData) || this.stop)
				return null;
			final int take = Math.min(max, this.count);
			final Object[] ea = new Object[take];
			for (int i = 0; i < take; i++)
				ea[i] = getUnsynchronized();
			this.notFull.signalAll();
			return ea;
		} finally {
			this.lock.unlock();
		}
	}

	private E getUnsynchronized() {
		final E bindings = (E) this.buffer[this.takeptr];
		if (++this.takeptr == this.maxBufferLocal)
			this.takeptr = 0;
		--this.count;
		return bindings;
	}

	public boolean hasNext() throws InterruptedException {
		this.lock.lock();
		try {
			if (this.count > 0)
				return true;
			while (this.count == 0 && !this.endOfData && !this.stop) {
				this.notEmpty.await();
			}
			if ((this.count == 0 && this.endOfData) || this.stop){
				return false;
			} else {
				return true;
			}
		} finally {
			this.lock.unlock();
		}
	}

	public int size() {
		this.lock.lock();
		try {
			return this.count;
		} finally {
			this.lock.unlock();
		}
	}

	public void endOfData() {
		this.lock.lock();
		try {
			this.endOfData = true;
			this.notEmpty.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	public void stopIt() {
		this.lock.lock();
		try {
			this.stop = true;
			this.notFull.signalAll();
			this.notEmpty.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	public static int getMaxBuffer() {
		return maxBuffer;
	}

	public static void setMaxBuffer(final int maxBuffer) {
		BoundedBuffer.maxBuffer = maxBuffer;
	}

	public boolean isEmpty() {
		this.lock.lock();
		try {
			if (this.count == 0 && this.endOfData)
				return true;
			else
				return false;
		} finally {
			this.lock.unlock();
		}
	}

	public boolean isCurrentlyEmpty() {
		this.lock.lock();
		try {
			if (this.count == 0)
				return true;
			else
				return false;
		} finally {
			this.lock.unlock();
		}
	}

	public boolean isCurrentlyFull() {
		this.lock.lock();
		try {
			return this.count == this.maxBufferLocal;
		} finally {
			this.lock.unlock();
		}
	}
}
