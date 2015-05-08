package lupos.datastructures.sort;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

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
		for (final Integer l : lengths) {
			sum = sum.add(BigInteger.valueOf(l));
		}
		System.out.println(sum);
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
