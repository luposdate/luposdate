/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.compression.bitstream;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class converts a byte-oriented input stream into a bit-oriented input stream. 
 */
public class BitInputStream {

	// the underlying input stream from which bytes are read and converted into a stream of bits
	protected final InputStream in;

	// the current position in the byte is initially set to 8 such that right the first time a new byte is read from the underlying input stream
	protected int currentPosInByte = 8;
	
	// the current byte to be transformed into a stream of bits
	protected int currentByte;
	
	/**
	 * Constructor
	 * @param in the underlying input stream
	 */
	public BitInputStream(final InputStream in){
		this.in = in;
	}
	
	/**
	 * Returns the next bit of the stream
	 * @return the next bit
	 * @throws IOException if something fails when reading from the underlying input stream
	 */
	public boolean readBit() throws IOException {
		if(this.currentPosInByte>7){
			// read in next byte from underlying input stream
			this.currentByte = this.in.read();
			this.currentPosInByte=0;
		}
		// test next bit...
		boolean result = (this.currentByte % 2) == 1; 
		this.currentPosInByte++;
		// prepare to read next bit by division through 2 (implemented by fast shifting bit positions)
		this.currentByte >>= 1;
		return result;
	}
	
	/**
	 * Closes this bit input stream (and its underlying input stream)
	 * @throws IOException if closing the underlying input stream fails...
	 */
	public void close() throws IOException{
		// closes underlying input stream
		this.in.close();
	}	
}
