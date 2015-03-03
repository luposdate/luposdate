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
package lupos.datastructures.dbmergesortedds.heap;

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.parallel.BoundedBuffer;
class InnerParallelHeap<E extends Comparable<E>> extends Thread {

	protected Lock lockFinish = new ReentrantLock();
	protected Condition finishCondition = this.lockFinish.newCondition();

	protected Lock lockEmpty = new ReentrantLock();
	protected Condition emptyCondition = this.lockEmpty.newCondition();
	protected Condition nonemptyCondition = this.lockEmpty.newCondition();

	protected int waitForEmpty = 0;

	/** Constant <code>maxInstructionBuffer=10</code> */
	protected static final int maxInstructionBuffer = 10;
	protected final SequentialHeap<E> sh;
	protected BoundedBuffer<InstructionContainer> bi = new BoundedBuffer<InstructionContainer>(
			maxInstructionBuffer);
	protected volatile boolean running = true;

	protected volatile int runningInstructionId = 0;
	protected volatile int finishedInstructionId = -1;

	public enum INSTRUCTION {
		BUBBLEDOWN, UPDATE, BUILDHEAP
	};

	/**
	 * <p>Constructor for InnerParallelHeap.</p>
	 *
	 * @param sh a {@link lupos.datastructures.dbmergesortedds.heap.SequentialHeap} object.
	 */
	public InnerParallelHeap(final SequentialHeap<E> sh) {
		super("InnerParallelHeap");
		this.sh = sh;
		String s = "";
		final Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
		Thread main = null;
		for (final Thread t : map.keySet()) {
			if (t.getName().compareTo("main") == 0) {
				main = t;
			}
		}
		if (main != null) {
			for (final StackTraceElement str : map.get(main)) {
				s += str.toString() + "\n";
			}
		}
		this.setName("InnerParallelHeap, StackTrace: " + s);
	}

	/**
	 * <p>getLastElement.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getLastElement() {
		this.lockEmpty.lock();
		try {
			this.waitForEmpty++;
			this.lockFinish.lock();
			try {
				try {
					while (this.finishedInstructionId + 1 < this.runningInstructionId) {
						this.finishCondition.await();
					}
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
				final E e = (E) this.sh.arr[--this.sh.length];
				this.sh.arr[this.sh.length] = null;
				return e;
			} finally {
				this.lockFinish.unlock();
			}
		} finally {
			this.waitForEmpty--;
			if (this.waitForEmpty == 0) {
				this.nonemptyCondition.signalAll();
			}
			this.lockEmpty.unlock();
		}
	}

	/**
	 * <p>peek.</p>
	 *
	 * @return a E object.
	 */
	public E peek() {
		this.lockEmpty.lock();
		try {
			this.waitForEmpty++;
			this.lockFinish.lock();
			try {
				try {
					while (this.finishedInstructionId + 1 < this.runningInstructionId) {
						this.finishCondition.await();
					}
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
				return this.sh.peek();
			} finally {
				this.lockFinish.unlock();
			}
		} finally {
			this.waitForEmpty--;
			if (this.waitForEmpty == 0) {
				this.nonemptyCondition.signalAll();
			}
			this.lockEmpty.unlock();
		}
	}

	/**
	 * <p>waitForEmptyInstructionQueue.</p>
	 */
	public void waitForEmptyInstructionQueue() {
		this.lockEmpty.lock();
		try {
			this.waitForEmpty++;
			this.lockFinish.lock();
			try {
				try {
					while (this.finishedInstructionId + 1 < this.runningInstructionId) {
						this.finishCondition.await();
					}
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
				return;
			} finally {
				this.lockFinish.unlock();
			}
		} finally {
			this.waitForEmpty--;
			if (this.waitForEmpty == 0) {
				this.nonemptyCondition.signalAll();
			}
			this.lockEmpty.unlock();
		}
	}

	/**
	 * <p>bubbledown.</p>
	 *
	 * @param data a E object.
	 */
	public void bubbledown(final E data) {
		this.lockEmpty.lock();
		try {
			while (this.waitForEmpty > 0) {
				this.nonemptyCondition.await();
			}
			this.bi.put(new InstructionContainer(INSTRUCTION.BUBBLEDOWN, data));
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		} finally {
			this.lockEmpty.unlock();
		}
	}

	/**
	 * <p>buildHeap.</p>
	 */
	public void buildHeap() {
		this.lockEmpty.lock();
		try {
			while (this.waitForEmpty > 0) {
				this.nonemptyCondition.await();
			}
			this.bi.put(new InstructionContainer(INSTRUCTION.BUILDHEAP));
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		} finally {
			this.lockEmpty.unlock();
		}
	}

	/**
	 * <p>addByUpdating.</p>
	 *
	 * @param data a E object.
	 */
	public void addByUpdating(final E data) {
		this.lockEmpty.lock();
		try {
			while (this.waitForEmpty > 0) {
				this.nonemptyCondition.await();
			}
			this.bi.put(new InstructionContainer(INSTRUCTION.UPDATE, data));
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		} finally {
			this.lockEmpty.unlock();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		try {
			while (this.running) {
				InstructionContainer ic;
				ic = this.bi.get();
				if (ic != null) {
					switch (ic.getInstruction()) {
					case BUBBLEDOWN:
						this.sh.arr[0] = ic.getData();
						if (this.sh.arr[0] == null) {
							this.sh.bubbleDown(0);
							this.sh.length--;
						} else {
							this.sh.bubbleDown(0);
						}
						break;
					case UPDATE:
						this.sh.addByUpdating(ic.getData());
						break;
					case BUILDHEAP:
						this.sh.buildHeap();
						break;
					}
					this.finishedInstructionId = ic.getInstructionId();
					this.lockFinish.lock();
					try {
						if (this.finishedInstructionId + 1 == this.runningInstructionId) {
							this.finishCondition.signalAll();
						}
					} finally {
						this.lockFinish.unlock();
					}
				}
			}
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void finalize() {
		this.stopIt();
	}

	/**
	 * <p>stopIt.</p>
	 */
	public void stopIt() {
		this.running = false;
		this.bi.stopIt();
	}

	private class InstructionContainer {
		private E data;
		private INSTRUCTION instruction;
		private int instructionId = InnerParallelHeap.this.runningInstructionId++;

		/**
		 * @param data
		 * @param instruction
		 */
		public InstructionContainer(final INSTRUCTION instruction, final E data) {
			this(instruction, data, null);
		}

		public InstructionContainer(final INSTRUCTION instruction) {
			this(instruction, null, null);
		}

		public InstructionContainer(final INSTRUCTION instruction,
				final E data, final Object dummy) {
			this.data = data;
			this.instruction = instruction;
		}

		public E getData() {
			return this.data;
		}

		public void setData(final E data) {
			this.data = data;
		}

		public INSTRUCTION getInstruction() {
			return this.instruction;
		}

		public void setInstruction(final INSTRUCTION instruction) {
			this.instruction = instruction;
		}

		public int getInstructionId() {
			return this.instructionId;
		}

		public void setInstructionId(final int instructionId) {
			this.instructionId = instructionId;
		}
	}
}
