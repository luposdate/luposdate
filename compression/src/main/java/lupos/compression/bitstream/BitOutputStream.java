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
package lupos.compression.bitstream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class converts a bit output stream into a byte-oriented output stream. 
 */
public class BitOutputStream {
	
	/**
	 * the underlying output stream
	 */
	protected final OutputStream out;
	
	/**
	 * the current (incomplete) byte (written later to the underlying output stream)
	 */
	protected int currentByte = 0;
	
	/**
	 * The current bit value (can be 1, 2, 4, 8, 16, 32, 64, 128).
	 * The bit value is added to the current byte if the next bit is set.
	 */
	protected int currentBitValue = 1;

	/**
	 * Constructor
	 * @param out the underlying output stream
	 */
	public BitOutputStream(final OutputStream out){
		this.out = out;
	}
	
	/**
	 * Writes a bit to the bit output stream...
	 * @param b the bit to be written
	 * @throws IOException if something fails when writing to the underlying output stream
	 */
	public void write(boolean b) throws IOException{
		if(b){
			// set the next bit
			this.currentByte += this.currentBitValue;
		}
		// multiply the current bit value with 2 by shifting the bits (which is more fast)
		this.currentBitValue <<= 1;
		if(this.currentBitValue == 256){
			// write the current byte and reset the current byte and bit value afterwards
			this.out.write(this.currentByte);
			this.currentByte = 0;
			this.currentBitValue = 1;
		}
	}

	/**
	 * This method closes the bit input stream by writing the last byte (event when it is incomplete) and closing the underlying output stream.
	 * @throws IOException if something fails when writing into or closing the underlying output stream
	 */
	public void close() throws IOException {
		if(this.currentBitValue>1){
			// write remaining bits (current byte must be written as complete bytes must be always written!)
			this.out.write(this.currentByte);
		}		
		this.out.close();
	}
}
