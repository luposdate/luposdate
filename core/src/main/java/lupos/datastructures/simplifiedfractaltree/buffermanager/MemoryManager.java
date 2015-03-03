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
package lupos.datastructures.simplifiedfractaltree.buffermanager;

import java.io.Serializable;

import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.buffermanager.PageManager;
import lupos.datastructures.simplifiedfractaltree.FractalTreeEntry;

/**
 * A manager that controls the memory usage a paged file.
 *
 * @author Denis Fäcke
 * @see BufferManager
 * @see PageManager
 * @version $Id: $Id
 */
public interface MemoryManager<K extends Comparable<K> & Serializable, V extends Serializable> {
	/**
	 * Acquires the specified amount of memory.
	 *
	 * @param amount The amount of memory needed
	 * @return The position in the memory
	 * @param pageCount a int.
	 */
	Pointer acquire(int pageCount, int amount);

	/**
	 * Releases the specified memory area.
	 *
	 * @param leftPage The left page
	 * @param leftBound The left bound
	 * @param size The amount of memory to release
	 */
	void release(int leftPage, int leftBound, int size);

	/**
	 * Defragments the memory and removes unused areas inside the managed file.
	 *
	 * @param bufferedList a {@link lupos.datastructures.simplifiedfractaltree.buffermanager.BufferedList_LuposSerialization} object.
	 */
	void defragment(BufferedList_LuposSerialization<FractalTreeEntry<K, V>> bufferedList);
}
