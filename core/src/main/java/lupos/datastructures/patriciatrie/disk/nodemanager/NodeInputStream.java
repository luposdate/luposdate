/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.datastructures.patriciatrie.disk.nodemanager;

import java.io.IOException;
import java.io.InputStream;

import lupos.misc.ByteHelper;

public class NodeInputStream extends InputStream {
	
	/**
	 * Stream to use for reading
	 */
	private final InputStream stream;

	/**
	 * @param stream
	 *            Stream to use for reading
	 */
	public NodeInputStream(final InputStream stream) {
		this.stream = stream;
	}

	@Override
	public int read() throws IOException {
		return this.stream.read();
	}
	
	/**
	 * Reads the next 4 bytes from the stream and converts them to an integer value
	 * 
	 * @return Integer representation of the next 4 bytes
	 * @throws IOException
	 */
	public int readInt() throws IOException {
		byte[] intVal = new byte[4];
		intVal[0] = (byte) this.stream.read();
		intVal[1] = (byte) this.stream.read();
		intVal[2] = (byte) this.stream.read();
		intVal[3] = (byte) this.stream.read();
		
		return ByteHelper.byteToInt(intVal);		
	}
	
	/**
	 * Reads the next 8 bytes from the stream and converts them to a long value
	 * 
	 * @return Long representation of the next 8 bytes
	 * @throws IOException
	 */
	public long readLong() throws IOException {
		byte[] intVal = new byte[8];
		intVal[0] = (byte) this.stream.read();
		intVal[1] = (byte) this.stream.read();
		intVal[2] = (byte) this.stream.read();
		intVal[3] = (byte) this.stream.read();
		intVal[4] = (byte) this.stream.read();
		intVal[5] = (byte) this.stream.read();
		intVal[6] = (byte) this.stream.read();
		intVal[7] = (byte) this.stream.read();
		
		return ByteHelper.byteToInt(intVal);		
	}
	
	@Override
	public void close() throws IOException {
		this.stream.close();
		super.close();
	}
}
