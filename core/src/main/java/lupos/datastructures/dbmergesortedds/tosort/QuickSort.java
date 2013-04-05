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
package lupos.datastructures.dbmergesortedds.tosort;

import java.util.Comparator;

import lupos.datastructures.dbmergesortedds.StandardComparator;

public class QuickSort<E extends Comparable<E>> extends InPlaceSort<E> {

	// this is a quicksort variant, which uses the pivot element as
	// median-of-three
	// and insertion sort for small sequences...

	private static int M = 25;
	private final Comparator<E> comp;

	public QuickSort(final int length) {
		this(length, new StandardComparator<E>());
		// System.out
		// .println("Initialize Quicksort with buffer of size "
		// + length
		// +
		// " for generating the initial runs for the external merge sort algorithm..."
		// );
	}

	public QuickSort(final int length, final Comparator<E> comp) {
		super(length);
		this.comp = comp;
		// System.out
		// .println("Initialize Quicksort with buffer of size "
		// + length
		// +
		// " for generating the initial runs for the external merge sort algorithm..."
		// );
	}

	@Override
	public void sort() {
		sort(0, size() - 1);
	}

	@Override
	public void sort(final int unten, final int oben) {
		Object tmp;
		if (oben - unten > M) { // use Quicksort
			int i = unten - 1;
			int j = oben;
			// pivot element as median of three
			final int m = (unten + oben) / 2;
			if (comp.compare((E) elements[unten], (E) elements[m]) > 0) {
				tmp = elements[unten];
				elements[unten] = elements[m];
				elements[m] = tmp;
			}
			if (comp.compare((E) elements[unten], (E) elements[oben]) > 0) {
				tmp = elements[unten];
				elements[unten] = elements[oben];
				elements[oben] = tmp;
			} else if (comp.compare((E) elements[oben], (E) elements[m]) > 0) {
				tmp = elements[oben];
				elements[oben] = elements[m];
				elements[m] = tmp;
			}
			// x is the median!
			final E x = (E) elements[oben];

			while (true) {
				while (comp.compare((E) elements[++i], x) < 0)
					;
				while (comp.compare((E) elements[--j], x) > 0 && j > i)
					;
				if (i >= j)
					break;
				tmp = elements[i]; // Hilfsspeicher
				elements[i] = elements[j]; // a[i] und
				elements[j] = tmp; // a[j] werden getauscht
			}
			// swap median to the middle
			elements[oben] = elements[i];
			elements[i] = x;
			// alle Elemente der linken Haelfte sind kleiner
			// als alle Elemente der rechten Haelfte
			// median ist genau dazwischen und schon an der richtigen Stelle in
			// der sortierten Folge!

			sort(unten, i - 1); // sortiere linke Haelfte
			sort(i + 1, oben); // sortiere rechte Haelfte
		} else { // use insertion sort for small sequences
			for (int i = unten + 1; i <= oben; ++i) {
				final E tmp2 = (E) elements[i];
				int j;
				for (j = i - 1; j >= unten
						&& comp.compare(tmp2, (E) elements[j]) < 0; --j)
					elements[j + 1] = elements[j];
				elements[j + 1] = tmp2;
			}
		}
	}
}
