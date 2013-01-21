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
 * Super class for all nodes in the huffman tree 
 */
public abstract class Node {

	/**
	 * Method to write out this huffman tree into the given bit output stream 
	 * @param out the bit output stream into which this huffman tree is written
	 * @throws IOException if something fails during writing out this huffman tree
	 */
	public abstract void encode(final BitOutputStream out) throws IOException;

	/**
	 * @return the depth of this tree
	 */
	public abstract int getDepth();
	
	/**
	 * @return the minimum symbol in this huffman tree
	 */
	public abstract int getMin();

	/**
	 * @return the maximum symbol in this huffman tree
	 */
	public abstract int getMax();
	
	/**
	 * This method reads in the next symbol from a given bit input stream.
	 * @param in the underlying bit input stream
	 * @return the next symbol
	 * @throws IOException is something fails when reading from the underlying bit input stream
	 */
	public abstract int getSymbol(final BitInputStream in) throws IOException ;

	/**
	 * This method starts to fill the code array with the codes of the symbols in the huffman tree.
	 * It is usually only called at the root node of a huffman tree.
	 * @param codeArray the code array, which is filled with the codes of the symbols in the huffman tree
	 * @param min the minimum symbol in the huffman tree (used to avoid handling larger code arrays, the code array starts at position 0 with the code of the minimum symbol)
	 */
	public void fillCodeArray(final Boolean[][] codeArray, final int min){
		this.fillCodeArray(new LinkedList<Boolean>(), codeArray, min);
	}

	/**
	 * This method fills the code array with the codes of the symbols in the huffman tree.
	 * @param currentCode the current code prefix
	 * @param codeArray the code array to be filled
	 * @param min the minimum symbol in the huffman tree (used to avoid handling larger code arrays, the code array starts at position 0 with the code of the minimum symbol)
	 */
	protected abstract void fillCodeArray(final LinkedList<Boolean> currentCode, final Boolean[][] codeArray, final int min);

	/**
	 * Method to store the current code into the code array
	 * @param currentCode the current code to be stored
	 * @param codeArray the code array for storing he codes of the symbols in the huffman tree
	 * @param pos the position in the code array
	 */
	protected void fill(final LinkedList<Boolean> currentCode, final Boolean[][] codeArray, final int pos){
		int i = 0;
		for(final Boolean codeBit: currentCode){
			codeArray[pos][i] = codeBit;
			i++;
		}
	}
	
	/**
	 * Static method to read in a huffman tree.
	 * @param in the bit input stream form which the huffman tree is read.
	 * @return the root of the read huffman tree
	 * @throws IOException if something fails in the underlying bit input stream
	 */
	public static Node readInHuffmanTree(final BitInputStream in) throws IOException {
		if(in.readBit()){
			// inner node ! -> read left and right operand!
			final Node left = Node.readInHuffmanTree(in);
			final Node right = Node.readInHuffmanTree(in);
			return new InnerNode(left, right);
		} else {
			if(in.readBit()){
				// leaf node
				return new LeafNode(Node.readSymbol(in));
			} else {
				// Node for End of File!
				return new EndOfFile();
			}
		}
	}
	
	/**
	 * Reads in one symbol (one byte consisting of eight bits)
	 * @param in the underlying bit input stream
	 * @return the symbol
	 * @throws IOException if something fails in the underlying bit input stream
	 */
	protected static int readSymbol(final BitInputStream in) throws IOException {
		int value = 0;
		int bitValue = 1; // bit value (1 -> 2 -> 4 -> 8 -> 16 -> 32 -> 64 -> 128)
		// reading in the next symbol
		for(int i=0; i<8; i++){
			if(in.readBit()){
				// if bit is set add the current bit value
				value += bitValue;
			}
			// multiply the bit value with 2 in a fast way!
			bitValue <<= 1;
		}
		return value;
	}
}
