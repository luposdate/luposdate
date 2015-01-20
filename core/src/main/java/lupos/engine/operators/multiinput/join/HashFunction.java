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
package lupos.engine.operators.multiinput.join;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;

public class HashFunction {
	private final static int MAXCOMPONENTS = 100;

	private static final long prim = 16785407; // chosen prime number

	private final long[] functionElements;

	public HashFunction(final long[] initValues) {
		this.functionElements = initValues;
	}

	public HashFunction() {
		this.functionElements = new long[MAXCOMPONENTS];
		for (int i = 0; i < this.functionElements.length; i++) {
			this.functionElements[i] = (long) (Math.random() * prim);
		}
	}

	public long[] getInitValues() {
		return this.functionElements;
	}

	public long[] getInitValues(final Collection<Variable> intersectionVariables) {
		final long[] la = new long[Math.min(intersectionVariables.size(),
				this.functionElements.length)];
		System.arraycopy(this.functionElements, 0, la, 0, la.length);
		return la;
	}

	public long hash(final String s) {
		long hash = 0;
		for (int i = 0; i < Math.min(s.length(), this.functionElements.length); i++) {
			hash = (hash + ((s.charAt(i) * this.functionElements[i]) % prim) % prim);
		}
		return hash;
	}

	/**
	 * the key is always greater then zero
	 */
	public long hash(final Collection<Literal> key) {
		long hash = 0;
		int i = 0;
		for (final Literal lit : key) {
			if (i >= this.functionElements.length)
				break;
			hash = (hash + ((lit.hashCode() * this.functionElements[i]) % prim)
					% prim);
			if (hash < 0)
				hash *= -1;
			i++;
		}
		return hash;
	}

	public long hash(final Literal lit) {
		long hash = (lit.hashCode() * this.functionElements[0]) % prim;
		if (hash < 0)
			hash *= -1;
		return hash;
	}

	public static Collection<Literal> getKey(final Bindings b,
			final Collection<Variable> intersectionVariables) {
		final Collection<Literal> key = new LinkedList<Literal>();
		for (final Variable v : intersectionVariables) {
			if (b.get(v) == null)
				return null;
			else
				key.add(b.get(v));
		}
		return key;
	}

	public long hash(final Bindings b,
			final Collection<Variable> intersectionVariables) {
		return hash(getKey(b, intersectionVariables));
	}

}
