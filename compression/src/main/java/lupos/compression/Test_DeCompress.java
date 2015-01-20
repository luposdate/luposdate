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
package lupos.compression;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is for testing some standard compressors...
 */
public class Test_DeCompress {

	/**
	 * called if wrong command line options are given...
	 */
	private static void error() {
		System.out.println("Error in command line options, use:");
		System.out
				.println("java lupos.compression.Test_DeCompress <COMPRESS|UNCOMPRESS> <NONE|GZIP|HUFFMAN|BZIP2|LZMA|PPM> <Inputfile> <Outputfile>");
		System.exit(0);
	}

	/**
	 * Method to get the type of compressor from command line option.
	 *
	 * @param type
	 *            the compressor type as string
	 * @return the compressor as enum
	 */
	private static Compression getCompressor(final String type) {
		final String upperCaseType = type.toUpperCase();
		try {
			return Compression.valueOf(upperCaseType);
		} catch (final Exception e) {
			error();
			return null;
		}
	}

	/**
	 * The method to compress a file
	 *
	 * @param compressor
	 *            the type of used compressor as enum
	 * @param inputFile
	 *            the input file the content of which is to compress
	 * @param outputFile
	 *            the file into which the output will be written
	 */
	private static void compress(final Compression compressor,
			final String inputFile, final String outputFile) {
		try {
			final InputStream input = new BufferedInputStream(
					new FileInputStream(inputFile));
			final OutputStream output = compressor
					.createOutputStream(new BufferedOutputStream(
							new FileOutputStream(outputFile)));

			pipe(input, output);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * The method to uncompress a file
	 *
	 * @param compressor
	 *            the type of used compressor as enum
	 * @param inputFile
	 *            the input file the content of which is to uncompress
	 * @param outputFile
	 *            the file into which the output will be written
	 */
	private static void uncompress(final Compression compressor,
			final String inputFile, final String outputFile) {
		try {
			final InputStream input = compressor
					.createInputStream(new BufferedInputStream(
							new FileInputStream(inputFile)));
			final OutputStream output = new BufferedOutputStream(
					new FileOutputStream(outputFile));

			pipe(input, output);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * This method realizes a pipe transferring the read bytes from an
	 * inputstream into an outputstream
	 *
	 * @param input
	 *            the inputstream from the bytes are read
	 * @param output
	 *            the outputstream to which the read bytes are written
	 * @throws IOException
	 */
	private static void pipe(final InputStream input, final OutputStream output)
			throws IOException {
		int nextByte;

		while ((nextByte = input.read()) >= 0) {
			output.write(nextByte);
		}

		input.close();
		output.close();
	}

	/**
	 * @param args
	 *            command line options: java misc.Test_DeCompress
	 *            <COMPRESS|UNCOMPRESS> <NONE|GZIP|HUFFMAN|BZIP2|LZMA|PPM> <Inputfile> <Outputfile>
	 */
	public static void main(final String[] args) {
		System.out.println("De-/Compressor");
		if (args.length != 4) {
			error();
		} else {
			final String firstArg = args[0].toUpperCase();
			if (firstArg.compareTo("COMPRESS") == 0
					|| firstArg.compareTo("C") == 0) {
				compress(getCompressor(args[1]), args[2], args[3]);
			} else if (firstArg.compareTo("UNCOMPRESS") == 0
					|| firstArg.compareTo("U") == 0) {
				uncompress(getCompressor(args[1]), args[2], args[3]);
			} else {
				error();
			}
		}
	}

}