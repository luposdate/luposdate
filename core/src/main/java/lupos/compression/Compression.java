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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
//the following classes are contained in jars (lzma-4.63-jio-0.95.jar, colloquial_arithcode-1_1.jar), which are not yet available in maven repositories... 
//import net.contrapunctus.lzma.LzmaInputStream;
//import net.contrapunctus.lzma.LzmaOutputStream;

//import com.colloquial.arithcode.ArithCodeInputStream;
//import com.colloquial.arithcode.ArithCodeOutputStream;
//import com.colloquial.arithcode.PPMModel;


/**
 * This enumeration contains some standard compression algorithms.
 * 
 * This enum can especially be used to create input and output streams for (un-) compressing content.
 * 
 * Note that some compression algorithms are lazily bound. They only work if other modules are also loaded.
 * (Background: the goal of the core-module is to support a light-weight evaluator, which is not dependent on third-party jars. The lazy binding of some extended functionality is a compromise.)
 */
public enum Compression {
	/**
	 * Dummy strategy, not changing the inferior streams.
	 */
	NONE {

		@Override
		public InputStream createInputStream(final InputStream inferior) {
			return inferior;
		}

		@Override
		public OutputStream createOutputStream(final OutputStream inferior) {
			return inferior;
		}

	},
	
	/**
	 * Compression strategy to apply blockwise huffman encoding.
	 * Background: Many of the used data structures and streams already avoid repetitions (at least common prefixes), such that zipping with the additional phase for detecting repetitions does not compress so much any more, but has high overhead.
	 * 
	 * Note that this strategy is lazy bound by using the reflection api: The module luposdate.compression should be loaded otherwise using this strategy leads to errors... 
	 */
	HUFFMAN {

		@SuppressWarnings("rawtypes")
		@Override
		public InputStream createInputStream(final InputStream inferior)
				throws IOException {
			// use reflection api allowing a later binding of the jar with the compression algorithms and letting the core module free of third-party jars!				
			try {
				Class clas = Class.forName("lupos.compression.huffman.HuffmanInputStream");
				Class[] types = new Class[] { InputStream.class };
				@SuppressWarnings("unchecked")
				Constructor<? extends InputStream> cons = clas.getConstructor(types);
				Object[] args = new Object[] { inferior };
				return cons.newInstance(args);
			} catch (Exception ex) {
				System.err.println("WARNING: Tried to use Huffman compression, but the corresponding jar is not included in the class path => do not use any compression!");
				System.err.println(ex);
				ex.printStackTrace();
				return inferior;
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		public OutputStream createOutputStream(final OutputStream inferior)
				throws IOException {
			// use reflection api allowing a later binding of the jar with the compression algorithms and letting the core module free of third-party jars!				
			try {
				Class clas = Class.forName("lupos.compression.huffman.HuffmanOutputStream");
				Class[] types = new Class[] { OutputStream.class };
				@SuppressWarnings("unchecked")
				Constructor<? extends OutputStream> cons = clas.getConstructor(types);
				Object[] args = new Object[] { inferior };
				return cons.newInstance(args);
			} catch (Exception ex) {
				System.err.println("WARNING: Tried to use Huffman compression, but the corresponding jar is not included in the class path => do not use any compression!");
				System.err.println(ex);
				ex.printStackTrace();
				return inferior;
			}
		}		
	},
	
	/**
	 * Build-in Java compression strategy.
	 */
	GZIP {

		@Override
		public InputStream createInputStream(final InputStream inferior) throws IOException {
			return new GZIPInputStream(inferior);
		}

		@Override
		public OutputStream createOutputStream(final OutputStream inferior)
				throws IOException {
			return new GZIPOutputStream(inferior);
		}

	},

	/**
	 * Compression strategy used by the Apache web server. Available from
	 * http://www.kohsuke.org/bzip2//
	 * 
	 * Note that this strategy is lazy bound by using the reflection api: The module luposdate.integrationBZIP2 should be loaded otherwise using this strategy leads to errors... 
	 */
	BZIP2 {

		@SuppressWarnings("rawtypes")
		@Override
		public InputStream createInputStream(final InputStream inferior)
				throws IOException {
			// use reflection api allowing a later binding of the jar with the compression algorithms and letting the core module free of third-party jars!				
			try {
				Class clas = Class.forName("org.apache.commons.compress.bzip2.CBZip2InputStream");
				Class[] types = new Class[] { InputStream.class };
				@SuppressWarnings("unchecked")
				Constructor<? extends InputStream> cons = clas.getConstructor(types);
				Object[] args = new Object[] { inferior };
				return cons.newInstance(args);
			} catch (Exception ex) {
				System.err.println("WARNING: Tried to use BZIP2 compression, but the corresponding jar is not included in the class path => do not use any compression!");
				System.err.println(ex);
				ex.printStackTrace();
				return inferior;
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		public OutputStream createOutputStream(final OutputStream inferior)
				throws IOException {
			// use reflection api allowing a later binding of the jar with the compression algorithms and letting the core module free of third-party jars!				
			try {
				Class clas = Class.forName("org.apache.commons.compress.bzip2.CBZip2OutputStream");
				Class[] types = new Class[] { OutputStream.class };
				@SuppressWarnings("unchecked")
				Constructor<? extends OutputStream> cons = clas.getConstructor(types);
				Object[] args = new Object[] { inferior };
				return cons.newInstance(args);
			} catch (Exception ex) {
				System.err.println("WARNING: Tried to use BZIP2 compression, but the corresponding jar is not included in the class path => do not use any compression!");
				System.err.println(ex);
				ex.printStackTrace();
				return inferior;
			}
		}		
	};

	// the following strategies are commented out due to non-availability in maven repositories!
	
	//	/**
//	 * Compression strategy used by 7zip. Available from
//	 * http://contrapunctus.net/league/haques/lzmajio/.
//	 * or https://github.com/league/lzmajio
//	 */
//	LZMA {
//
//		@Override
//		public InputStream createInputStream(final InputStream inferior)
//				throws IOException {
//			return new LzmaInputStream(inferior);
//		}
//
//		@Override
//		public OutputStream createOutputStream(final OutputStream inferior)
//				throws IOException {
//			return new LzmaOutputStream(inferior);
//		}
//	},
//	/**
//	 * High-level compression strategy implementing prediction by partial
//	 * matching. Available from http://www.colloquial.com/ArithmeticCoding/
//	 */
//	PPM {
//
//		/**
//		 * The size of considered context for the next byte to be
//		 * un-/compressed
//		 */
//		private static final int maxContentLength = 6;
//
//		@Override
//		public InputStream createInputStream(final InputStream inferior)
//				throws IOException {
//			return new ArithCodeInputStream(inferior, new PPMModel(
//					maxContentLength));
//		}
//
//		@Override
//		public OutputStream createOutputStream(final OutputStream inferior)
//				throws IOException {
//			return new ArithCodeOutputStream(inferior, new PPMModel(
//					maxContentLength)) {
//
//				@Override
//				public void write(final int i) throws IOException {
//					super.write(i & 255);
//				}
//
//				@Override
//				public void write(final byte[] bs, final int off,
//						final int len) throws IOException {
//					for (int i = off, l = off + len; i < l; i++) {
//						write(bs[i]);
//					}
//				}
//
//				@Override
//				public void write(final byte[] bs) throws IOException {
//					write(bs, 0, bs.length);
//				}
//
//			};
//		}

	/**
	 * Wraps the
	 * <code>inferior</stream> stream in an input stream uncompressing its content.
	 *
	 * @param inferior
	 *            Compressed input stream
	 * @return The wrapping input stream
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public abstract InputStream createInputStream(final InputStream inferior)
			throws IOException;

	/**
	 * Wraps the <code>inferior</stream> stream in an output stream
	 * compressing the written data before passing it.
	 *
	 * @param inferior
	 *            Output stream to contain the compressed data
	 * @return The wrapping output stream
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public abstract OutputStream createOutputStream(final OutputStream inferior) throws IOException;
}
