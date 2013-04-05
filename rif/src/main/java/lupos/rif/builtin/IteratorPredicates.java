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
package lupos.rif.builtin;

import java.util.Iterator;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;

public class IteratorPredicates {

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-less-than")
	public static Iterator<Literal> numeric_less_than(final Literal start) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		return new Iterator<Literal>() {
			int value = startValue;

			public boolean hasNext() {
				return true;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(--value, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-less-than-or-equal")
	public static Iterator<Literal> numeric_less_than_or_equal(
			final Literal start) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		return new Iterator<Literal>() {
			int value = startValue;

			public boolean hasNext() {
				return true;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(value--, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-greater-than")
	public static Iterator<Literal> numeric_greater_than(final Literal start) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		return new Iterator<Literal>() {
			int value = startValue;

			public boolean hasNext() {
				return true;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(++value, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-greater-than-or-equal")
	public static Iterator<Literal> numeric_greater_than_or_equal(
			final Literal start) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		return new Iterator<Literal>() {
			int value = startValue;

			public boolean hasNext() {
				return true;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(value++, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-between")
	public static Iterator<Literal> numeric_between(final Literal start,
			final Literal stop) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		final int endValue = BuiltinHelper.getInteger((TypedLiteral) stop);
		return new Iterator<Literal>() {
			int value = startValue;
			final boolean direction = endValue > startValue;

			public boolean hasNext() {
				return direction ? value < (endValue - 1)
						: endValue < (value - 1);
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(direction ? ++value
						: --value, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-between-enclosing")
	public static Iterator<Literal> numeric_between_enclosing(
			final Literal start, final Literal stop) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		final int endValue = BuiltinHelper.getInteger((TypedLiteral) stop);
		return new Iterator<Literal>() {
			int value = startValue;
			final boolean direction = endValue > startValue;

			public boolean hasNext() {
				return direction ? value <= endValue : value >= endValue;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(direction ? value++
						: value--, "integer");
			}

			public void remove() {
			}
		};
	}
}
