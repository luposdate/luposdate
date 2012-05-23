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
package lupos.datastructures.items.literal.codemap;

import java.util.Map.Entry;

import lupos.datastructures.trie.TrieMap;

public class IntegerStringMapTrieSlave implements IntegerStringMap {

	protected final TrieMap<Integer> trieMap;
	protected int[] indexMap;

	public IntegerStringMapTrieSlave(final TrieMap<Integer> trieMap){
		this.trieMap=trieMap;
		updateIndexMap();
	}

	private void updateIndexMap(){
		indexMap=new int[trieMap.size()];
		Integer[] objectArray=trieMap.getObjectArray();
		for(int index=0;index<objectArray.length;index++){
			indexMap[objectArray[index]-1]=index;
		}
	}

	public String get(int key) {
		return trieMap.getTrie().get(indexMap[key-1]);
	}

	public void put(int key, String s) {
		// this is the slave
		// => we assume that trieMap has been updated before correspondingly
		// => only update our indexMap according to trieMap!
		updateIndexMap();
	}

	public void clear() {
		trieMap.clear();
		updateIndexMap();
	}

	public int size() {
		return trieMap.size();
	}

	public void forEachValue(TProcedureValue<String> arg0) {
		for(String value: trieMap.getTrie())
			arg0.execute(value);
	}

	public void forEachEntry(TProcedureEntry<Integer, String> arg0) {
		for (final Entry<String,Integer> entry : trieMap.entrySet()) {
			arg0.execute(entry.getValue(), entry.getKey());
		}
	}
}
