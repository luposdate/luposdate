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
package lupos.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The functionality of this class is similar to ByteArrayOutputStream,
 * except that the stream is written into an existing byte array
 * rather than creating a new one.
 */
public class ExistingByteArrayOutputStream extends OutputStream {

	private final byte[] byteArray;
	private int offset;

	/**
	 * Constructor
	 * @param byteArray the byte array into which the stream is written.
	 * @param offset the offset in the byte array at which the writing of the stream is started.
	 */
	public ExistingByteArrayOutputStream(final byte[] byteArray, final int offset){
		this.byteArray = byteArray;
		this.offset = offset;
	}

	/**
	 * Constructor
	 * @param byteArray the byte array into which the stream is written.
	 */
	public ExistingByteArrayOutputStream(final byte[] byteArray){
		this(byteArray, 0);
	}

	@Override
	public void write(final int b) throws IOException {
		this.byteArray[this.offset++] = (byte) b;
	}

	/**
	 * This method is overridden just for performance reasons (such that System.arraycopy can be used...)
	 */
    @Override
    public void write(final byte b[], final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        System.arraycopy(b, off, this.byteArray, this.offset, len);
        this.offset += len;
    }

    /**
     * @return the current offset in the byte array
     */
    public int getOffset(){
    	return this.offset;
    }
}
