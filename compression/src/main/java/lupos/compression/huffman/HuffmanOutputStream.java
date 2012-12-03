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
import java.io.OutputStream;

import lupos.compression.bitstream.BitOutputStream;
import lupos.compression.huffman.tree.EndOfFile;
import lupos.compression.huffman.tree.InnerNode;
import lupos.compression.huffman.tree.LeafNode;
import lupos.compression.huffman.tree.Node;
import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.heap.SequentialHeap;

public class HuffmanOutputStream extends OutputStream {
	
	protected final BitOutputStream out;
	
	protected final static int blocksize = 10*1024;
	protected byte[] block = new byte[blocksize];
	protected int current = 0;
		
	public HuffmanOutputStream(final BitOutputStream out){
		this.out = out;
	}

	public HuffmanOutputStream(final OutputStream out){
		this.out = new BitOutputStream(out);
	}

	@Override
	public void write(final int b) throws IOException{
		this.block[this.current]=(byte)b;
		this.current++;
		if(this.current==HuffmanOutputStream.blocksize){
			Node root = this.buildHuffmanTree();
			encode(root);
			this.current=0;
		}		
	}
	
	protected Node buildHuffmanTree(){
		// determine minimum and maximum byte
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for(int i=0; i<HuffmanOutputStream.blocksize; i++){
			int b = this.block[i];
			if(b<min){
				min = b;
			}
			if(b>max){
				max = b; 
			}
		}
		
		// build frequency table and reserve one character for EOF if necessary
		int[] frequency = new int[max - min + 1];
		// initialize frequency table with 0
		for(int i=0; i<frequency.length; i++){
			frequency[i]=0;
		}
		// determine frequencies
		for(int i=0; i<this.current; i++){
			frequency[this.block[i]-min]++;
		}
		// determine number of bytes, which really occur
		int occurrences = 0;
		for(int i=0; i<frequency.length; i++){
			if(frequency[i]>0){
				occurrences++;
			}
		}
		// must EOF be additionally considered?
		if(this.current<HuffmanOutputStream.blocksize){
			occurrences++;
		}
		
		Heap<HeapEntry> heap = new SequentialHeap<HeapEntry>(occurrences, true);
		
		for(int i=0; i<frequency.length; i++){
			if(frequency[i]>0){
				heap.add(new HeapEntry(frequency[i], new LeafNode(min+i)));
			}
		}

		if(this.current<HuffmanOutputStream.blocksize){
			// consider also EOF!
			heap.add(new HeapEntry(1, new EndOfFile()));
		}
		
		while(heap.size()>1){
			HeapEntry a = heap.pop();
			HeapEntry b = heap.pop();
			heap.add(new HeapEntry(a.weight + b.weight, new InnerNode(a.node, b.node)));
		}
		
		// finally get the root (= last element in the heap!)
		HeapEntry root = heap.pop();
		return root.node;
	}
	
	public void encode(final Node root) throws IOException{
		root.encode(this.out);
		if(!(root instanceof EndOfFile)){
			final int depth = root.getDepth();
			final int min = root.getMin();
			final int max = root.getMax();
			
			Boolean[][] codeArray = new Boolean[max - min + 1 + ((this.current<HuffmanOutputStream.blocksize)? 1 : 0)][depth];
			root.fillCodeArray(codeArray, min);
			
			for(int i=0; i<this.current; i++){
				this.writeCode(codeArray[this.block[i] - min]);
				
			}
			if(this.current < HuffmanOutputStream.blocksize){
				// write end of file!
				this.writeCode(codeArray[codeArray.length-1]);
			}
		}
	}
	
	protected void writeCode(final Boolean[] code) throws IOException {
		for(Boolean codeBit: code){
			if(codeBit==null){
				break;
			} else {
				this.out.write(codeBit);
			}
		}
	}
	
	@Override
	public void close() throws IOException{
		Node root = this.buildHuffmanTree();
		encode(root);
		this.current=0;
		this.out.close();
	}
	
	public static class HeapEntry implements Comparable<HeapEntry>{
		
		protected final int weight;
		protected final Node node;
		
		public HeapEntry(final int weight, final Node node){
			this.weight = weight;
			this.node = node;
		}

		@Override
		public int compareTo(HeapEntry o) {			
			return this.weight - o.weight;
		}		
	}
}
