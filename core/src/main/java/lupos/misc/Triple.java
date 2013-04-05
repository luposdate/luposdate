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
package lupos.misc;

public class Triple<T1, T2, T3> {
	protected T1 t1;
	protected T2 t2;
	protected T3 t3;

	public Triple(T1 t1, T2 t2, T3 t3) {
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
	}

	public T1 getFirst() {
		return this.t1;
	}

	public void setFirst(T1 t1) {
		this.t1 = t1;
	}

	public T2 getSecond() {
		return this.t2;
	}

	public void setSecond(T2 t2) {
		this.t2 = t2;
	}

	public T3 getThird() {
		return this.t3;
	}

	public void setThird(T3 t3) {
		this.t3 = t3;
	}

	public boolean equals(Object object) {
		if(object instanceof Triple) {
			Triple triple = (Triple) object;

			if(!this.t1.equals(triple.t1)) {
				return false;
			}

			if(!this.t2.equals(triple.t2)) {
				return false;
			}

			return this.t3.equals(triple.t3);
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		return (int) (((long) this.t1.hashCode() + this.t2.hashCode() + this.t3.hashCode()) % Integer.MAX_VALUE);
	}
	
	public String toString(){
		return "(" + t1.toString()+", "+t2.toString()+", "+t3.toString()+")";
	}
}
