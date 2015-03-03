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
package lupos.compression.huffman;

import java.io.IOException;
import java.io.InputStream;

import lupos.compression.bitstream.BitInputStream;
import lupos.compression.huffman.tree.Node;

/**
 * The block-wise huffman input stream for reading in a huffman encoded stream.
 * In comparison to the adaptive huffman tree, we choose a block-wise encoding scheme due to performance reasons.
 * In the block-wise encoding scheme, each block contains a huffman tree and a huffman tree is valid (and is not changed) for a complete block.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class HuffmanInputStream extends InputStream {

	/**
	 * the underlying bit input stream
	 */
	protected final BitInputStream in;
	
	/**
	 * the current position in the block (necessary to detect when to read in a new huffman tree)
	 */
	protected int current = 0;
	
	/**
	 * the root of the current huffman tree
	 */
	protected Node rootOfHuffmanTree;
	
	/**
	 * Constructor
	 *
	 * @param in the underlying bit input stream from which for each block the huffman tree is read and the huffman encoded block
	 */
	public HuffmanInputStream(final BitInputStream in){
		this.in = in;
	}
	
	/**
	 * Constructor
	 *
	 * @param in the underlying input stream, from which a bit input stream is created and from which for each block the huffman tree is read and the huffman encoded block
	 */
	public HuffmanInputStream(final InputStream in){
		this.in = new BitInputStream(in);
	}
		
	/** {@inheritDoc} */
	@Override
	public int read() throws IOException {
		if(this.current == 0){
			// initialization or next block
			// => read in huffman tree
			this.rootOfHuffmanTree = Node.readInHuffmanTree(this.in);
		}
		// get next symbol
		final int result = this.rootOfHuffmanTree.getSymbol(this.in);
		this.current++;
		if(this.current==HuffmanOutputStream.blocksize){
			// is one block finished?
			this.current = 0;
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		// close underlying bit input stream...
		this.in.close();
	}
}
