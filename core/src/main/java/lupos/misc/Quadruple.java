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

public class Quadruple<T1, T2, T3, T4> extends Triple<T1, T2, T3> {
	protected T4 t4;

	public Quadruple(final T1 t1, final T2 t2, final T3 t3, final T4 t4) {
		super(t1, t2, t3);
		this.t4 = t4;
	}

	public T4 getFourth() {
		return this.t4;
	}

	public void setFourth(final T4 t4) {
		this.t4 = t4;
	}

	@Override
	public boolean equals(final Object object) {
		if(object instanceof Quadruple) {
			if(!super.equals(object)) {
				return false;
			}

			@SuppressWarnings("unchecked")
			final Quadruple<T1, T2, T3, T4> quadruple = (Quadruple<T1, T2, T3, T4>) object;

			return this.t4.equals(quadruple.t4);
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int) (((long) this.t1.hashCode() + this.t2.hashCode() + this.t3.hashCode() + this.t4.hashCode() ) % Integer.MAX_VALUE);
	}

	@Override
	public String toString(){
		return "(" + this.t1.toString()+", "+this.t2.toString()+", "+this.t3.toString()+", "+this.t4.toString()+")";
	}
}
