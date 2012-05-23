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
package lupos.gui.debug;

import lupos.engine.operators.BasicOperator;

/**
 * The class RingBuffer represents a simple mechanism to store an amount of
 * values in a cyclic order within a limited space
 */
public class RingBuffer {

	// maximum length of internal array
	private final int MAX_LENGTH = 1000000;

	// internal variables to define the ringbuffer
	// end points to the element after the current one
	// start points to the first one and is initialized with end
	private volatile int start, end = 0;
	private volatile int current = -1;

	// the ringbuffer itself is an array of StepContainers
	private final StepContainer[] array = new StepContainer[MAX_LENGTH];

	/**
	 * This method is only called, when a StepContainer should be added
	 * 
	 * @param stepContainer
	 * @return the current stepContainer
	 */
	public synchronized StepContainer next(final StepContainer stepContainer) {
		if (current == -1) {
			current++;
			array[current] = stepContainer;
		} else if (current == end) {
			current++;
			current %= MAX_LENGTH;
			end++;
			end %= MAX_LENGTH;
			if (end == start) {
				start++;
				start %= MAX_LENGTH;
			}
			array[current] = stepContainer;
		} else {
			current++;
			current %= MAX_LENGTH;
			array[current] = stepContainer;
		}
		return array[current];
	}

	/**
	 * Returns the next StepContainer inside the RingBuffer
	 * 
	 * @return the next StepContainer
	 */
	public synchronized StepContainer next() {
		current++;
		current %= MAX_LENGTH;
		return array[current];
	}

	/**
	 * Returns the previous StepContainer inside the RingBuffer
	 * 
	 * @return the current StepContainer
	 */
	public synchronized StepContainer previous() {
		current--;
		if (current == -1) {
			current = MAX_LENGTH - 1;
		}
		return array[current];
	}

	/**
	 * Returns the current StepContainer if existing
	 * 
	 * @return the current StepContainer
	 */
	public synchronized StepContainer getCurrentStepContainer() {
		StepContainer result = null;
		if (current != -1) {
			result = array[current];
		}
		return result;
	}

	/**
	 * Check whether there is a previous element
	 * 
	 * @return true, if there is an element preceeding the current one
	 */
	public synchronized boolean hasPrevious() {
		return ((current != -1) && (current != start));
	}

	/**
	 * Check whether there is a next element
	 * 
	 * @return true, if there is an element after the current one
	 */
	public synchronized boolean hasNext() {
		return (current != end && current != -1);
	}

	/**
	 * goBackTo returns the element preceeding the parameter StepContainer
	 */
	public synchronized StepContainer goBackTo(final BasicOperator from) {
		if (current != -1 && start != current) {
			int i = current;
			while (i != start) {
				i--;
				if (i < 0)
					i = MAX_LENGTH - 1;
				if (array[i].getFrom().equals(from)) {
					current = i;
					return array[i];
				}

			}
		}
		return null;
	}

	/**
	 * goBackToStart sets current to start, thus returning the first element
	 * 
	 * @return the first element from the ringbuffer
	 */
	public synchronized StepContainer goBackToStart() {
		current = start;
		return array[current];
	}
}
