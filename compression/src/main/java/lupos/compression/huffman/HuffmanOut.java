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
package lupos.compression.huffman;

import java.io.IOException;

import lupos.compression.bitstream.BitOutputStream;

public class HuffmanOut extends Huffman {
	
	protected final BitOutputStream out;
	
	protected int[] leafPointers = new int[256];
	
	protected int NYT_Pos = 0;
	
	public HuffmanOut(final BitOutputStream out){
		this.out = out;
		for(int i=0; i<this.leafPointers.length; i++){
			this.leafPointers[i] = -1;
		}
	}

	protected void write(final int b) throws IOException{
		final int pos_of_b = this.leafPointers[b];
		if(pos_of_b == -1){
			// b is currently not in the adaptive huffman tree!
			this.writeCode(this.NYT_Pos);
			final int new_NYT_Pos = getLeftChild(this.NYT_Pos);
			final int new_Leaf_pos = new_NYT_Pos + 1;
			this.nodes[new_NYT_Pos] = this.nodes[this.NYT_Pos];
			this.nodes[this.NYT_Pos] = new InnerNode();
			this.NYT_Pos = new_NYT_Pos;
			this.nodes[new_Leaf_pos] = new LeafNode();
			this.leafPointers[b] = new_Leaf_pos;
			this.incrementWeight(new_Leaf_pos);			
		} else {
			// b is in the adaptive huffman tree!
			this.writeCode(pos_of_b);
			this.incrementWeight(pos_of_b);
		}
	}
	
	protected int getParent(int pos){
		return (pos-1)/2;
	}
	
	protected int getLeftChild(int pos){
		return (pos*2)+1;
	}	
	
	protected int getRightChild(int pos){
		return this.getLeftChild(pos)+1;
	}
	
	/**
	 * writes the code of a byte by first navigating to the root and on the way back write the bits of the code...
	 * @param pos
	 * @throws IOException
	 */
	protected void writeCode(final int pos) throws IOException{
		if(pos==0){
			return;
		}
		final int parent = this.getParent(pos);
		this.writeCode(parent);
		this.out.write(pos==this.getRightChild(parent));		
	}
	
	protected void writeByte(final int b) throws IOException{
		int toWrite = b;
		// write 8 bits...
		for(int i=0; i<8; i++){
			this.out.write((toWrite%2) == 1);
			// divide by 2 in a fast way!
			toWrite >>= 1;
		}
	}
	
	protected void incrementWeight(final int pos){
		
	}
}
