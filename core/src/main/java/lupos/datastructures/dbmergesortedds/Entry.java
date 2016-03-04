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
package lupos.datastructures.dbmergesortedds;

import java.io.Serializable;
import java.util.Comparator;
public class Entry<E> implements Comparable<Entry<E>>, Serializable {
	private static final long serialVersionUID = -5186882148047627193L;

	public final E e;
	public int run;
	public int n;
	transient protected Comparator<? super E> comp;

	/**
	 * <p>Constructor for Entry.</p>
	 *
	 * @param e a E object.
	 * @param comp a {@link java.util.Comparator} object.
	 * @param n a int.
	 */
	public Entry(final E e, final Comparator<? super E> comp, final int n) {
		this.n = n;
		run = 1;
		this.e = e;
		this.comp = comp;
	}

	/**
	 * <p>Constructor for Entry.</p>
	 *
	 * @param e a E object.
	 * @param n a int.
	 */
	public Entry(final E e, final int n) {
		this.n = n;
		this.e = e;
		this.comp = null;
	}

	/**
	 * <p>Constructor for Entry.</p>
	 *
	 * @param e a E object.
	 */
	public Entry(final E e) {
		this.e = e;
		this.comp = null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object other) {
		if (this.getClass() != other.getClass())
			return false;
		return comp.compare(e, ((Entry<E>) other).e) == 0;
	}

	public boolean runMatters = true;

	/**
	 * <p>compareTo.</p>
	 *
	 * @param other a {@link lupos.datastructures.dbmergesortedds.Entry} object.
	 * @return a int.
	 */
	public int compareTo(final Entry<E> other) {
		if (other == null) {
			return -1;
		}
		if (run == other.run) {
			final int compResult = comp.compare(e, other.e);
			if (compResult == 0) {
				if (n > other.n)
					return 1;
				else if (n == other.n)
					return 0;
				else
					return -1;
			} else {
				return compResult;
			}
		} else if (!runMatters) {
			final int compResult = comp.compare(e, other.e);
			if (compResult == 0) {
				if (run > other.run)
					return 1;
				else if (run < other.run)
					return -1;
				if (n > other.n)
					return 1;
				else if (n == other.n)
					return 0;
				else
					return -1;
			} else {
				return compResult;
			}
		} else {
			return run > other.run ? 1 : -1;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return run + " - " + e.toString();
	}
}
