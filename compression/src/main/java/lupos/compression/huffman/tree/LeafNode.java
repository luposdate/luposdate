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
package lupos.compression.huffman.tree;

import java.io.IOException;
import java.util.LinkedList;

import lupos.compression.bitstream.BitInputStream;
import lupos.compression.bitstream.BitOutputStream;

public class LeafNode extends Node {
	final int symbol;
	
	public LeafNode(final int symbol){
		this.symbol = symbol;
	}

	@Override
	public void encode(final BitOutputStream out) throws IOException {
		out.write(false); // bit cleared for leaf node!
		out.write(true); // bit set for non-EOF!
		this.writeSymbol(out);
	}
	
	protected void writeSymbol(final BitOutputStream out) throws IOException{
		int toWrite = this.symbol;
		// write 8 bits...
		for(int i=0; i<8; i++){
			out.write((toWrite%2) != 0);
			// divide by 2 in a fast way!
			toWrite >>= 1;
		}
	}

	@Override
	public int getDepth() {
		return 0;
	}

	@Override
	public int getMin() {
		return this.symbol;
	}

	@Override
	public int getMax() {
		return this.symbol;
	}

	@Override
	protected void fillCodeArray(final LinkedList<Boolean> currentCode, final Boolean[][] codeArray, final int min) {
		this.fill(currentCode, codeArray, this.symbol - min);
	}

	@Override
	public int getSymbol(BitInputStream in) throws IOException {
		return this.symbol;
	}
	
	@Override
	public String toString(){
		return ""+this.symbol;
	}
}