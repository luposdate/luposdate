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
package lupos.compression.huffman.tree;

import java.io.IOException;
import java.util.LinkedList;

import lupos.compression.bitstream.BitInputStream;
import lupos.compression.bitstream.BitOutputStream;

/**
 * An inner node of the huffman tree 
 */
public class InnerNode extends Node {
	
	/**
	 * the left child
	 */
	public final Node left;
	
	/**
	 * the right child
	 */
	public final Node right;
	
	/**
	 * Constructor
	 * @param left the left child
	 * @param right the right child
	 */
	public InnerNode(final Node left, final Node right){
		this.left = left;
		this.right = right;
	}
	
	@Override
	public void encode(final BitOutputStream out) throws IOException{
		out.write(true); // bit set for inner node!
		// write out left and right children!
		this.left.encode(out);
		this.right.encode(out);
	}

	@Override
	public int getDepth() {
		return Math.max(this.left.getDepth() + 1, this.right.getDepth() + 1);
	}

	@Override
	public int getMin() {
		return Math.min(this.left.getMin(), this.right.getMin());
	}

	@Override
	public int getMax() {
		return Math.max(this.left.getMax(), this.right.getMax());
	}

	@Override
	protected void fillCodeArray(final LinkedList<Boolean> currentCode, final Boolean[][] codeArray, final int min) {
		// the codes of the symbols in the left child start with a cleared bit!
		currentCode.add(false);
		this.left.fillCodeArray(currentCode, codeArray, min);
		currentCode.removeLast();
		// the codes of the symbols in the right child start with a set bit!
		currentCode.add(true);
		this.right.fillCodeArray(currentCode, codeArray, min);
		currentCode.removeLast();
	}

	@Override
	public int getSymbol(BitInputStream in) throws IOException {
		if(in.readBit()){
			// bit is set => symbol is in the right child
			return this.right.getSymbol(in);
		} else {
			// bit is cleared => symbol is in the left child
			return this.left.getSymbol(in);
		}
	}
	
	@Override
	public String toString(){
		return "(" + this.left.toString() + ", " + this.right.toString() + ")";
	}
}
