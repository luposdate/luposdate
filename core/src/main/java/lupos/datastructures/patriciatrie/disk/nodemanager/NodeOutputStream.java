
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.datastructures.patriciatrie.disk.nodemanager;

import java.io.IOException;
import java.io.OutputStream;

import lupos.datastructures.patriciatrie.diskseq.DeSerializer.Writer;
import lupos.misc.ByteHelper;
public class NodeOutputStream extends OutputStream implements Writer {
	
	/**
	 * Stream to use for writing
	 */
	private OutputStream stream;
	
	/**
	 * <p>Constructor for NodeOutputStream.</p>
	 *
	 * @param stream
	 *            Stream to use for writing
	 */
	public NodeOutputStream(final OutputStream stream) {
		this.stream = stream;
	}

	/** {@inheritDoc} */
	@Override
	public void write(int b) throws IOException {
		this.stream.write(b);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Writes an integer value to the stream (4 bytes)
	 */
	@Override
	public void writeInt(int i) throws IOException {
		this.stream.write(ByteHelper.intToByte(i));
	}
	
	/**
	 * Writes a long value to the stream (8 bytes)
	 *
	 * @param l
	 *            Long to write
	 * @throws java.io.IOException if any.
	 */
	public void writeLong(long l) throws IOException {
		this.stream.write(ByteHelper.longToByte(l));
	}
	
	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		this.stream.close();
		super.close();
	}
}
