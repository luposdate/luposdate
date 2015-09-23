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
package lupos.datastructures.dbmergesortedds.tosort;




public class LSDRadixSort extends InPlaceSort<String> {

	public LSDRadixSort(final int length) {
		super(length);
	}

	@Override
	public void sort() {
		this.sort(0, this.length - 1);
	}

	@Override
	public void sort(final int from, final int to) {
		// determine maximum
		int max = Integer.MIN_VALUE;
		for(int i=from; i<=to; i++){
			final String castedObject = (String) this.elements[i];
			if(castedObject.length() > max){
				max = castedObject.length();
			}
		}
		do {
			max--;
			this.elements = LSDRadixSort.countingsort(this.elements, from, to, new Digitizer(max));
		} while(max>0);
		// System.out.println(Arrays.toString(this.elements));
	}

	public static class Digitizer implements ComputeKey<String>{

		private final int index;

		public Digitizer(final int index){
			this.index = index;
		}

		@Override
		public int computeKey(final String t) {
			// a shorter string being a substring of a longer string precedes the longer string!
			return (t.length()>this.index)? t.charAt(this.index) : -1;
		}
	}

	@SuppressWarnings("unchecked")
	public static<T> Object[] countingsort(final Object[] input, final int from, final int to, final ComputeKey<T> ck) {
		// determine minimum and maximum
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for(int i=from; i<=to; i++){
			final int value = ck.computeKey((T)input[i]);
			if(value < min){
				min = value;
			}
			if(value > max){
				max = value;
			}
		}
		// initialize counting array:
		final int[] count = new int[max-min+1];
		count[0] = -1; // this is for correct address calculation: the first value should be put into address 0
		for(int i=1; i<count.length; i++) {
			count[i] = 0;
		}
		// now count
		for(int i=from; i<=to; i++){
			final int value = ck.computeKey((T)input[i]);
			count[value-min]++;
		}
		// calculate addresses
		for(int i=1; i<count.length; i++) {
			count[i] += count[i-1];
		}
		// now reorder input array:
		@SuppressWarnings("unchecked")
		final Object[] result = new Object[input.length]; // todo maybe shorter array according to from and to
		for(int i=to; i>=from; i--) {
			final Object value = input[i];
			final int index = (ck.computeKey((T)value) - min);
			result[count[index]] = value;
			count[index]--;
		}
		return result;
	}

	public static interface ComputeKey<T> {
		public int computeKey(T t);
	}
}
