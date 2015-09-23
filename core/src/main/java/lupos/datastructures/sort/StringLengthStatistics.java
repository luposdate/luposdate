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
package lupos.datastructures.sort;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.smallerinmemorylargerondisk.PagedCollection;
import lupos.datastructures.sort.helper.DataToBoundedBuffer;


public class StringLengthStatistics {

	// scale used for divisions with BigDecimal numbers...
	private final static int scale = 256;

	public static void main(final String[] args) throws Exception{
		System.out.println("Determining string length statistics of a large collection of Strings or RDF terms of large RDF data...");
		if(args.length<2){
			System.out.println(StringLengthStatistics.getHelpText());
			return;
		}
		final BoundedBuffer<String> buffer = new BoundedBuffer<String>();

		final PagedCollection<Integer> lengths = new PagedCollection<Integer>(Integer.class);
		final StringLengthAdder stringLengthAdder = new StringLengthAdder(buffer, lengths);

		stringLengthAdder.start();
		// read in and parse the data...
		DataToBoundedBuffer.dataToBoundedBuffer(new BufferedInputStream(new FileInputStream(args[0])), args[1], buffer);

		// signal that all the data is parsed (and nothing will be put into the buffer any more)
		buffer.endOfData();

		// wait for threads to finish generating string lengths...
		try {
			stringLengthAdder.join();
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}

		final long length = lengths.sizeAsLong();
		System.out.println("Number of strings           : "+length+"\n");
		// now compute statistics
		final BigDecimal[] terms = StringLengthStatistics.computeInnerTerm(lengths);
		System.out.println("Average string length sizes : "+terms[0]+"\n");
		System.out.println("Sample Standard Deviation   : "+computeSampleStandardDeviation(terms[1], length));
		System.out.println("StandardDeviationOfTheSample: "+computeStandardDeviationOfTheSample(terms[1], length)+"\n");

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (final Integer l : lengths) {
			if(l<min){
				min = l;
			}
			if(l>max){
				max = l;
			}
		}
		System.out.println("Minimum string length size  : "+min);
		System.out.println("Maximum string length size  : "+max+"\n");
		lengths.release();
	}

	public static double computeSampleStandardDeviation(final BigDecimal innerTerm, final long length){
		return Math.sqrt(innerTerm.divide(BigDecimal.valueOf(length-1), StringLengthStatistics.scale, BigDecimal.ROUND_HALF_UP).doubleValue());
	}

	public static double computeStandardDeviationOfTheSample(final BigDecimal innerTerm, final long length) {
		return Math.sqrt(innerTerm.divide(BigDecimal.valueOf(length), StringLengthStatistics.scale, BigDecimal.ROUND_HALF_UP).doubleValue());
	}

	public static BigDecimal[] computeInnerTerm(final PagedCollection<Integer> lengths) {
		BigInteger sum = BigInteger.ZERO;
		final Iterator<Integer> it = lengths.iterator();
		while(it.hasNext()) {
			sum = sum.add(BigInteger.valueOf(it.next()));
		}
		// System.out.println(sum);
		BigDecimal mean = new BigDecimal(sum);
		mean = mean.divide(BigDecimal.valueOf(lengths.sizeAsLong()), StringLengthStatistics.scale, BigDecimal.ROUND_HALF_UP);
		BigDecimal innerTerm = BigDecimal.ZERO;
		for (final Integer l : lengths) {
			final BigDecimal diff = BigDecimal.valueOf(l).subtract(mean);
			innerTerm = innerTerm.add(diff.multiply(diff));
		}
		return new BigDecimal[]{mean, innerTerm};
	}

	private static String getHelpText() {
		String result = "Call StringLengthStatistics in the following way:\n\njava lupos.datastructures.sort.StringLengthStatistics DATAFILE FORMAT\n\n";
		result += "DATAFILE contains the file with data (containing strings or RDF data)\n";
		result += "FORMAT can be STRING for a large collection of strings in one file, MULTIPLESTRING for a list of files containing strings to be read, BZIP2STRING and MULTIPLEBZIP2STRING for analogous, but BZIP2 compressed files, or an RDF format like N3\n";
		return result;
	}

	public static class StringLengthAdder extends Thread {
		private final BoundedBuffer<String> buffer;
		private final Collection<Integer> lengths;
		public StringLengthAdder(final BoundedBuffer<String> buffer, final Collection<Integer> lengths){
			this.buffer = buffer;
			this.lengths = lengths;
		}

		@Override
		public void run(){
			try {
				while(true){
					final String item = this.buffer.get();
					if(item==null){
						break;
					}
					this.lengths.add(item.length());
				}
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}
}
