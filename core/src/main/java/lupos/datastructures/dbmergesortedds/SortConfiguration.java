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
package lupos.datastructures.dbmergesortedds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lupos.compression.Compression;
import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT;

/**
 * This class encapsulates the configuration for the sorting algorithms like DBMergeSortedBag, DBMergeSortedSet, ... 
 * An instance should be first generated and afterwards one of the methods useReplacementSelection, 
 * useExternalMergeSort or use ChunksMergeSort should be called in order to set a specific external sorting algorithm.
 */
public class SortConfiguration<E extends Comparable<E>> {

	/**
	 * the default strategy for the in-memory sorting phase (in external merge sorting algorithms)
	 */
	private static TOSORT DEFAULT_TOSORT = TOSORT.PARALLELMERGESORT;
	
	/**
	 * the default reserved space for the in-memory sorting algorithms for external merge sorting algorithms 
	 */
	private static int DEFAULT_TOSORT_SIZE = 16;
	
	/**
	 * the default heap type used in replacement selection
	 */
	private static HEAPTYPE DEFAULT_HEAP_TYPE = HEAPTYPE.OPTIMIZEDSEQUENTIAL;
	
	/**
	 * the default heap height in replacement selection 
	 */
	private static int DEFAULT_HEIGHT = 12;
	
	/**
	 * the default heap type used for the merging phase
	 */
	private static HEAPTYPE DEFAULT_MERGEHEAP_TYPE = HEAPTYPE.OPTIMIZEDSEQUENTIAL;
	
	/**
	 * the default heap size for the used heap in the merging phase
	 */
	private static int DEFAULT_MERGEHEAP_HEIGHT = 16;
	
	/**
	 * the default number of chunks for chunks merge sort
	 */
	private static int DEFAULT_K = 2;
	
	/**
	 * the default compression strategy applied when storing and reading the runs
	 */
	private static Compression DEFAULT_COMPRESSION = Compression.NONE;

	/**
	 * the sorting algorithm for the in-memory sorting phase for external merge sort 
	 */
	private TOSORT toSort;
	
	/**
	 * the heap type used for replacement selection
	 */
	private HEAPTYPE heapType;
	
	/**
	 * the heap height for replacement selection
	 */
	private int size;
	
	/**
	 * the merge heap type
	 */
	private HEAPTYPE mergeHeapType;
	
	/**
	 * the height of the merge heap
	 */
	private int mergeHeapHeight;
	
	/**
	 * the number of elements to be written out if the heap is full (typically 1, but for chunks merge sort it is the size of one chunk)
	 */
	private int elementsToPopWhenHeapIsFull;
	
	/**
	 * the compression strategy to be used for storing and reading the runs
	 */
	private Compression compression; 
	
	/**
	 * Default constructor setting replacement selection as sorting strategy
	 */
	public SortConfiguration(){
		this.compression = SortConfiguration.DEFAULT_COMPRESSION;
		this.useReplacementSelection();
	}
	
	/**
	 * Sets the compression strategy to be used for storing and reading the runs
	 * @param compression the compression strategy to be used for the runs
	 */	
	public void setCompression(final Compression compression){
		this.compression = compression;
	}
	
	/**
	 * No compression strategy will be used to access the runs
	 */
	public void setNoCompression(){
		this.setCompression(Compression.NONE);
	}
	
	/**
	 * The BZIP2 compression strategy will be used to access the runs
	 */
	public void setBZIP2Compression(){
		this.setCompression(Compression.BZIP2);
	}
	
	/**
	 * The GZIP compression strategy will be used to access the runs
	 */
	public void setGZIPCompression(){
		this.setCompression(Compression.GZIP);
	}
	
	/**
	 * Huffman encoding/decoding will be used to access the runs
	 */
	public void setHuffmanCompression(){
		this.setCompression(Compression.HUFFMAN);
	}
	
	/**
	 * Creates an input stream for uncompressing the given input stream according to the current compression strategy 
	 * @param inferior the input stream to be uncompressed
	 * @return the input stream for uncompressed data
	 * @throws IOException if the creation fails
	 */
	public InputStream createInputStream(final InputStream inferior) throws IOException{
		return this.compression.createInputStream(inferior);
	}
	
	/**
	 * Creates an output stream for compressing the given output stream according to the current compression strategy 
	 * @param inferior the output stream to be compressed
	 * @return the output stream with compressed data
	 * @throws IOException if the creation fails
	 */
	public OutputStream createOutputStream(final OutputStream inferior) throws IOException{
		return this.compression.createOutputStream(inferior);
	}
	
	/**
	 * Replacement selection with default parameters is used for sorting...
	 */
	public void useReplacementSelection(){
		this.useReplacementSelection(SortConfiguration.DEFAULT_HEIGHT, SortConfiguration.DEFAULT_MERGEHEAP_HEIGHT);
	}

	/**
	 * Choose replacement selection as sorting strategy (with default heap type and default merge heap type) 
	 * @param height the height of the heap
	 * @param mergeHeapHeight_param the height of the merge heap
	 */
	public void useReplacementSelection(final int height, final int mergeHeapHeight_param){
		this.useReplacementSelection(SortConfiguration.DEFAULT_HEAP_TYPE, height, SortConfiguration.DEFAULT_MERGEHEAP_TYPE, mergeHeapHeight_param);
	}

	/**
	 * Choose replacement selection as sorting strategy 
	 * @param heapType_param the type of heap used to generate the initial runs
	 * @param height the height of the heap
	 * @param mergeHeapType_param the type of the merge heap
	 * @param mergeHeapHeight_param the height of the merge heap
	 */
	public void useReplacementSelection(final HEAPTYPE heapType_param, final int height, final HEAPTYPE mergeHeapType_param, final int mergeHeapHeight_param){
		this.toSort = null;
		this.heapType = heapType_param;
		this.size = height;
		this.mergeHeapType = mergeHeapType_param;
		this.mergeHeapHeight = mergeHeapHeight_param;
		this.elementsToPopWhenHeapIsFull = 1;
	}
	
	/**
	 * Use external merge sort with default parameters as sorting strategy...
	 */
	public void useExternalMergeSort(){
		this.useExternalMergeSort(SortConfiguration.DEFAULT_TOSORT_SIZE, SortConfiguration.DEFAULT_MERGEHEAP_HEIGHT);
	}
	
	/**
	 * Choose external merge sort as sorting strategy (with default in-memory sorting algorithm and merge heap type)
	 * @param size_param space reserved for the in-memory sorting algorithm
	 * @param mergeHeapHeight_param the height of the merge heap
	 */
	public void useExternalMergeSort(final int size_param, final int mergeHeapHeight_param){
		this.useExternalMergeSort(SortConfiguration.DEFAULT_TOSORT, size_param, SortConfiguration.DEFAULT_MERGEHEAP_TYPE, mergeHeapHeight_param);
	}

	/**
	 * Choose external merge sort as sorting strategy
	 * @param sortingAlgorithm in memory sorting algorithm
	 * @param size_param space reserved for the in-memory sorting algorithm
	 * @param mergeHeapType_param the type of the merge heap
	 * @param mergeHeapHeight_param the height of the merge heap
	 */
	public void useExternalMergeSort(final TOSORT sortingAlgorithm, final int size_param, final HEAPTYPE mergeHeapType_param, final int mergeHeapHeight_param){
		this.toSort = sortingAlgorithm;
		this.size = size_param;
		this.heapType = null;
		this.mergeHeapType = mergeHeapType_param;
		this.mergeHeapHeight = mergeHeapHeight_param;
		this.elementsToPopWhenHeapIsFull = 1;
	}

	/**
	 * Use chunks merge sort with default parameters as sorting strategy...
	 */
	public void useChunksMergeSort(){
		this.useChunksMergeSort(SortConfiguration.DEFAULT_K, SortConfiguration.DEFAULT_HEIGHT);
	}

	/**
	 * Choose chunks merge sort as sorting strategy (use parallel merge sort as in-memory sorting algorithm and the default merge heap type of default height)
	 * @param K number of chunks
	 * @param size_param main memory space reserved for chunks merge sort 
	 */
	public void useChunksMergeSort(final int K, final int size_param){
		this.useChunksMergeSort(K, size_param, SortConfiguration.DEFAULT_MERGEHEAP_HEIGHT);
	}

	/**
	 * Choose chunks merge sort as sorting strategy (use parallel merge sort as in-memory sorting algorithm and the default merge heap type)
	 * @param K number of chunks
	 * @param size_param main memory space reserved for chunks merge sort 
	 * @param mergeHeapHeight_param the height of the merge heap
	 */
	public void useChunksMergeSort(final int K, final int size_param, final int mergeHeapHeight_param){
		this.useChunksMergeSort(K, size_param, SortConfiguration.DEFAULT_MERGEHEAP_TYPE, mergeHeapHeight_param);
	}

	/**
	 * Choose chunks merge sort as sorting strategy (use parallel merge sort as in-memory sorting algorithm)
	 * @param K number of chunks
	 * @param size_param main memory space reserved for chunks merge sort 
	 * @param mergeHeapType_param the type of the merge heap
	 * @param mergeHeapHeight_param the height of the merge heap
	 */
	public void useChunksMergeSort(final int K, final int size_param, final HEAPTYPE mergeHeapType_param, final int mergeHeapHeight_param){
		this.useChunksMergeSort(K, true, size_param, mergeHeapType_param, mergeHeapHeight_param);
	}
	
	/**
	 * Choose chunks merge sort as sorting strategy
	 * @param K number of chunks
	 * @param useParallelMergeSort true for parallel merge sort as in-memory sorting algorithm, otherwise quicksort is used 
	 * @param size_param main memory space reserved for chunks merge sort 
	 * @param mergeHeapType_param the type of the merge heap
	 * @param mergeHeapHeight_param the height of the merge heap
	 */
	public void useChunksMergeSort(final int K, final boolean useParallelMergeSort, final int size_param, final HEAPTYPE mergeHeapType_param, final int mergeHeapHeight_param){
		this.toSort = null;
		this.heapType = (useParallelMergeSort)? HEAPTYPE.SORTANDMERGEHEAPUSINGMERGESORT : HEAPTYPE.SORTANDMERGEHEAP;
		this.size = size_param;
		this.mergeHeapType = mergeHeapType_param;
		this.mergeHeapHeight = mergeHeapHeight_param;		
		this.elementsToPopWhenHeapIsFull = (1 << size_param) / K;
	}
	
	/**
	 * Creates the in-memory sorting algorithm for external merge sorting algrorithms
	 * @return instance for in-memory sorting
	 */
	public ToSort<E> createToSort(){
		if(this.toSort == null){
			return null;
		}
		ToSort<E> result = ToSort.createInstance(this.toSort, this.size);
		return result;
	}
	
	/**
	 * Creates the heap for replacement selection
	 * @return the heap for replacement selection
	 */
	public Heap<E> createHeap(){
		if(this.heapType == null){
			return null;
		}
		Heap<E> result = Heap.createInstance(this.size, this.heapType);
		return result;		
	}

	/**
	 * Creates the heap for the merging phase
	 * @return the heap for the merging phase
	 */
	public Heap<E> createMergeHeap(){
		return Heap.createInstance(this.mergeHeapHeight, this.mergeHeapType);
	}
	
	/**
	 * Returns the elements to pop from a full heap (1 for replacement selection, size of a chunk for chunks merge sort)
	 * @return the elements to pop from a full heap
	 */
	public int getElementsToPopWhenHeapIsFull(){
		return this.elementsToPopWhenHeapIsFull;
	}

	/**
	 * @return the default in-memory sorting algorithm used in external merge sort
	 */
	public static TOSORT getDEFAULT_TOSORT() {
		return DEFAULT_TOSORT;
	}

	/**
	 * Sets the default in-memory sorting algorithm used in external merge sort
	 * @param dEFAULT_TOSORT the default in-memory sorting algorithm used in external merge sort
	 */
	public static void setDEFAULT_TOSORT(TOSORT dEFAULT_TOSORT) {
		DEFAULT_TOSORT = dEFAULT_TOSORT;
	}

	/**
	 * @return the default reserved space for the in-memory sorting algorithm used in external merge sort
	 */
	public static int getDEFAULT_TOSORT_SIZE() {
		return DEFAULT_TOSORT_SIZE;
	}

	/**
	 * Sets the default reserved space for the in-memory sorting algorithm used in external merge sort
	 * @param dEFAULT_TOSORT_SIZE the default reserved space for the in-memory sorting algorithm used in external merge sort
	 */
	public static void setDEFAULT_TOSORT_SIZE(int dEFAULT_TOSORT_SIZE) {
		DEFAULT_TOSORT_SIZE = dEFAULT_TOSORT_SIZE;
	}

	/**
	 * @return the default heap type in replacement selection
	 */
	public static HEAPTYPE getDEFAULT_HEAP_TYPE() {
		return DEFAULT_HEAP_TYPE;
	}

	/**
	 * Sets the default heap type in replacement selection
	 * @param dEFAULT_HEAP_TYPE the default heap type in replacement selection
	 */
	public static void setDEFAULT_HEAP_TYPE(HEAPTYPE dEFAULT_HEAP_TYPE) {
		DEFAULT_HEAP_TYPE = dEFAULT_HEAP_TYPE;
	}

	/**
	 * @return the default heap height of the heap in replacement selection
	 */
	public static int getDEFAULT_HEIGHT() {
		return DEFAULT_HEIGHT;
	}

	/**
	 * Sets the default heap height of the heap in replacement selection
	 * @param dEFAULT_HEIGHT the default heap height of the heap in replacement selection
	 */
	public static void setDEFAULT_HEIGHT(int dEFAULT_HEIGHT) {
		DEFAULT_HEIGHT = dEFAULT_HEIGHT;
	}

	/**
	 * @return the default heap type of the merge heap
	 */
	public static HEAPTYPE getDEFAULT_MERGEHEAP_TYPE() {
		return DEFAULT_MERGEHEAP_TYPE;
	}

	/**
	 * Sets the default heap type of the merge heap
	 * @param dEFAULT_MERGEHEAP_TYPE the default heap type of the merge heap
	 */
	public static void setDEFAULT_MERGEHEAP_TYPE(HEAPTYPE dEFAULT_MERGEHEAP_TYPE) {
		DEFAULT_MERGEHEAP_TYPE = dEFAULT_MERGEHEAP_TYPE;
	}

	/**
	 * @return the default heap height of the merge heap
	 */
	public static int getDEFAULT_MERGE_HEAP_HEIGHT() {
		return DEFAULT_MERGEHEAP_HEIGHT;
	}

	/**
	 * Sets the default heap height of the merge heap
	 * @param dEFAULT_MERGE_HEAP_HEIGHT the default heap height of the merge heap
	 */
	public static void setDEFAULT_MERGE_HEAP_HEIGHT(int dEFAULT_MERGE_HEAP_HEIGHT) {
		DEFAULT_MERGEHEAP_HEIGHT = dEFAULT_MERGE_HEAP_HEIGHT;
	}

	/**
	 * @return the number of chunks used in chunks merge sort
	 */
	public static int getDEFAULT_K() {
		return DEFAULT_K;
	}

	/**
	 * Sets the number of chunks used in chunks merge sort
	 * @param dEFAULT_K the number of chunks used in chunks merge sort
	 */
	public static void setDEFAULT_K(int dEFAULT_K) {
		DEFAULT_K = dEFAULT_K;
	}

	/**
	 * @return the default compression strategy for the runs
	 */
	public static Compression getDEFAULT_COMPRESSION() {
		return DEFAULT_COMPRESSION;
	}

	/**
	 * Sets the default compression strategy for the runs
	 * @param dEFAULT_COMPRESSION the default compression strategy for the runs
	 */
	public static void setDEFAULT_COMPRESSION(Compression dEFAULT_COMPRESSION) {
		DEFAULT_COMPRESSION = dEFAULT_COMPRESSION;
	}

	/**
	 * @return the heap height of the merge heap
	 */
	public int getMergeHeapHeight() {
		return this.mergeHeapHeight;
	}
}
