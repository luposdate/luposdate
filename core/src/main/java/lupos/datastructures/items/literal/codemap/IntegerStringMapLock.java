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
package lupos.datastructures.items.literal.codemap;

import java.util.concurrent.locks.ReentrantLock;
public class IntegerStringMapLock implements IntegerStringMap {

	private final ReentrantLock lock;
	private final IntegerStringMap map;

	/**
	 * <p>Constructor for IntegerStringMapLock.</p>
	 *
	 * @param lock a {@link java.util.concurrent.locks.ReentrantLock} object.
	 * @param map a {@link lupos.datastructures.items.literal.codemap.IntegerStringMap} object.
	 */
	public IntegerStringMapLock(final ReentrantLock lock, final IntegerStringMap map){
		this.lock=lock;
		this.map=map;
	}

	/** {@inheritDoc} */
	public String get(int key) {
		lock.lock();
		try{
			return map.get(key);
		}finally{
			lock.unlock();
		}
	}

	/** {@inheritDoc} */
	public void put(int key, String s) {
		lock.lock();
		try{
			map.put(key, s);
		}finally{
			lock.unlock();
		}
	}

	/**
	 * <p>clear.</p>
	 */
	public void clear() {
		lock.lock();
		try{
			map.clear();
		}finally{
			lock.unlock();
		}
	}

	/**
	 * <p>size.</p>
	 *
	 * @return a int.
	 */
	public int size() {
		lock.lock();
		try{
			return map.size();
		}finally{
			lock.unlock();
		}
	}

	/** {@inheritDoc} */
	public void forEachValue(TProcedureValue<String> arg0) {
		lock.lock();
		try{
			map.forEachValue(arg0);
		}finally{
			lock.unlock();
		}
	}

	/** {@inheritDoc} */
	public void forEachEntry(TProcedureEntry<Integer, String> arg0) {
		lock.lock();
		try{
			map.forEachEntry(arg0);
		}finally{
			lock.unlock();
		}
	}
}
