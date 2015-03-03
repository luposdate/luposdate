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
package lupos.compression.huffman.tree;

import java.io.IOException;
import java.util.LinkedList;

import lupos.compression.bitstream.BitInputStream;
import lupos.compression.bitstream.BitOutputStream;

/**
 * Class to represent the symbol End Of File in the huffman tree!
 *
 * @author groppe
 * @version $Id: $Id
 */
public class EndOfFile extends Node {

	/** {@inheritDoc} */
	@Override
	public void encode(final BitOutputStream out) throws IOException {
		out.write(false); // bit cleared for leaf node!
		out.write(false); // bit cleared for EOF!
	}
	
	/** {@inheritDoc} */
	@Override
	public int getDepth() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public int getMin() {
		return Integer.MAX_VALUE;
	}

	/** {@inheritDoc} */
	@Override
	public int getMax() {
		return Integer.MIN_VALUE;
	}

	/** {@inheritDoc} */
	@Override
	protected void fillCodeArray(LinkedList<Boolean> currentCode, Boolean[][] codeArray, int min) {
		// the code of EOF is stored in the last element of the array!
		this.fill(currentCode, codeArray, codeArray.length - 1);
	}

	/** {@inheritDoc} */
	@Override
	public int getSymbol(BitInputStream in) throws IOException {
		// end of stream reached!
		return -1;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString(){
		return "EndOfFile";
	}
}
