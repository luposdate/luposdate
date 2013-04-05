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
package lupos.datastructures.patriciatrie.disk.nodemanager;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * HashBuffer that uses Integer as key. Whenever the buffer is full, an overflow
 * handler is called.
 * 
 * @param <T>
 *            Object type for the values in the LinkedHashMap.
 */
public class HashBuffer<T> extends LinkedHashMap<Integer, T> {
	
	/** serialVersionUID */
	private static final long serialVersionUID = -7477765811314650525L;

	/**
	 * @param <T>
	 *            Object type for the values in the LinkedHashMap, that will be
	 *            handled by this OverflowHandler
	 */
	public interface OverflowHandler<T> {

		/**
		 * Will be called, when the buffer is full.
		 * 
		 * @param obj
		 */
		public void onOverflow(final T obj);
	}

	/** Size of the buffer */
	private final int bufferSize;
	
	/** Overflow handler for this buffer */
	private OverflowHandler<T> overflowHandler;
	
	/**
	 * @param bufferSize
	 *            Size of the buffer
	 */
	public HashBuffer(final int bufferSize) {
		super(bufferSize);
		this.bufferSize = bufferSize;
		this.overflowHandler = null;
	}

	/**
	 * Sets the overflow handler
	 * 
	 * @param overflowHandler
	 */
	public void setOverflowHandler(final OverflowHandler<T> overflowHandler) {
		this.overflowHandler = overflowHandler;
	}
	
	/**
	 * Adds an object to the buffer with idx as key. If the buffer is full, the
	 * overflow handler will be called and the eldest entry will be removed.
	 * 
	 * @param idx
	 *            Key for the buffer
	 * @param obj
	 *            Actual object to store in the buffer
	 */
	public void add(final int idx, final T obj) {
		if (!this.containsKey(idx)) {
			if (this.size() >= this.bufferSize && this.bufferSize > 0) {
				// Buffer is full, remove the eldest entry
				final Entry<Integer, T> entryToRemove = this.entrySet().iterator().next();
				
				// Call the overflow handler if any is defined
				if (this.overflowHandler != null)
					this.overflowHandler.onOverflow(entryToRemove.getValue());

				this.remove(entryToRemove.getKey());	
			}
		}
		else {
			// Entry is already in the buffer. Remove and reinsert it, to make sure, it is the latest entry.
			this.remove(idx);
		}
		
		this.put(idx, obj);
	}	
}
