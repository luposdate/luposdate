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
package lupos.datastructures.items.literal.codemap;

public class IntegerStringMapArray implements IntegerStringMap {
	
	protected String[] values = new String[0]; 

	@Override
	public String get(int key) {
		if(key >= this.values.length){
			return null;
		} else {
			return this.values[key];
		}
	}

	@Override
	public void put(int key, String s) {
		if(key >= this.values.length){
			String[] newValues = new String[key+1];
			System.arraycopy(this.values, 0, newValues, 0, this.values.length);
			this.values = newValues;
		}
		this.values[key] = s;
	}

	@Override
	public void clear() {
		this.values = new String[0];
	}

	@Override
	public int size() {
		return this.values.length;
	}

	@Override
	public void forEachValue(TProcedureValue<String> arg0) {
		for (final String value: this.values) {
			arg0.execute(value);
		}
	}

	@Override
	public void forEachEntry(TProcedureEntry<Integer, String> arg0) {
		int i=0;
		for (final String value: this.values) {
			arg0.execute(i, value);
			i++;
		}
	}

}
