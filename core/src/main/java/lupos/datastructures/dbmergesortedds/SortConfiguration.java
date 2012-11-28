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
package lupos.datastructures.dbmergesortedds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

	private static TOSORT DEFAULT_TOSORT = TOSORT.PARALLELMERGESORT;
	private static int DEFAULT_TOSORT_SIZE = 16;
	private static HEAPTYPE DEFAULT_HEAP_TYPE = HEAPTYPE.OPTIMIZEDSEQUENTIAL;
	private static int DEFAULT_HEIGHT = 12;
	private static HEAPTYPE DEFAULT_MERGEHEAP_TYPE = HEAPTYPE.OPTIMIZEDSEQUENTIAL;
	private static int DEFAULT_MERGEHEAP_HEIGHT = 16;
	private static int DEFAULT_K = 2;
	private static Compression DEFAULT_COMPRESSION = Compression.NONE;

	private TOSORT toSort;
	private HEAPTYPE heapType;
	private int size;
	private HEAPTYPE mergeHeapType;
	private int mergeHeapHeight;	
	private int elementsToPopWhenHeapIsFull;
	
	private Compression compression; 
	
	public SortConfiguration(){
		this.compression = SortConfiguration.DEFAULT_COMPRESSION;
		this.useReplacementSelection();
	}
	
	public void setBZIP2Compression(){
		this.compression = Compression.BZIP2;
	}
	
	public void setGZIPCompression(){
		this.compression = Compression.GZIP;
	}
	
	public InputStream createInputStream(final InputStream inferior) throws IOException{
		return this.compression.createInputStream(inferior);
	}
	
	public OutputStream createOutputStream(final OutputStream inferior) throws IOException{
		return this.compression.createOutputStream(inferior);
	}
	
	public void useReplacementSelection(){
		this.useReplacementSelection(SortConfiguration.DEFAULT_HEIGHT, SortConfiguration.DEFAULT_MERGEHEAP_HEIGHT);
	}

	public void useReplacementSelection(final int height, final int mergeHeapHeight_param){
		this.useReplacementSelection(SortConfiguration.DEFAULT_HEAP_TYPE, height, SortConfiguration.DEFAULT_MERGEHEAP_TYPE, mergeHeapHeight_param);
	}

	public void useReplacementSelection(final HEAPTYPE heapType_param, final int height, final HEAPTYPE mergeHeapType_param, final int mergeHeapHeight_param){
		this.toSort = null;
		this.heapType = heapType_param;
		this.size = height;
		this.mergeHeapType = mergeHeapType_param;
		this.mergeHeapHeight = mergeHeapHeight_param;
		this.elementsToPopWhenHeapIsFull = 1;
	}
	
	public void useExternalMergeSort(){
		this.useExternalMergeSort(SortConfiguration.DEFAULT_TOSORT_SIZE, SortConfiguration.DEFAULT_MERGEHEAP_HEIGHT);
	}
	
	public void useExternalMergeSort(final int size_param, final int mergeHeapHeight_param){
		this.useExternalMergeSort(SortConfiguration.DEFAULT_TOSORT, size_param, SortConfiguration.DEFAULT_MERGEHEAP_TYPE, mergeHeapHeight_param);
	}
	
	public void useExternalMergeSort(final TOSORT sortingAlgorithm, final int size_param, final HEAPTYPE mergeHeapType_param, final int mergeHeapHeight_param){
		this.toSort = sortingAlgorithm;
		this.size = size_param;
		this.heapType = null;
		this.mergeHeapType = mergeHeapType_param;
		this.mergeHeapHeight = mergeHeapHeight_param;
		this.elementsToPopWhenHeapIsFull = 1;
	}

	public void useChunksMergeSort(){
		this.useChunksMergeSort(SortConfiguration.DEFAULT_K, SortConfiguration.DEFAULT_HEIGHT);
	}

	public void useChunksMergeSort(final int K, final int size_param){
		this.useChunksMergeSort(K, size_param, SortConfiguration.DEFAULT_MERGEHEAP_HEIGHT);
	}

	public void useChunksMergeSort(final int K, final int size_param, final int mergeHeapHeight_param){
		this.useChunksMergeSort(K, size_param, SortConfiguration.DEFAULT_MERGEHEAP_TYPE, mergeHeapHeight_param);
	}

	public void useChunksMergeSort(final int K, final int size_param, final HEAPTYPE mergeHeapType_param, final int mergeHeapHeight_param){
		this.useChunksMergeSort(K, true, size_param, mergeHeapType_param, mergeHeapHeight_param);
	}
	
	public void useChunksMergeSort(final int K, final boolean useParallelMergeSort, final int size_param, final HEAPTYPE mergeHeapType_param, final int mergeHeapHeight_param){
		this.toSort = null;
		this.heapType = (useParallelMergeSort)? HEAPTYPE.SORTANDMERGEHEAPUSINGMERGESORT : HEAPTYPE.SORTANDMERGEHEAP;
		this.size = size_param;
		this.mergeHeapType = mergeHeapType_param;
		this.mergeHeapHeight = mergeHeapHeight_param;		
		this.elementsToPopWhenHeapIsFull = (1 << size_param) / K;
	}
	
	public ToSort<E> createToSort(){
		if(this.toSort == null){
			return null;
		}
		ToSort<E> result = ToSort.createInstance(this.toSort, this.size);
		return result;
	}
	
	public Heap<E> createHeap(){
		if(this.heapType == null){
			return null;
		}
		Heap<E> result = Heap.createInstance(this.size, this.heapType);
		return result;		
	}

	public Heap<E> createMergeHeap(){
		return Heap.createInstance(this.mergeHeapHeight, this.mergeHeapType);
	}
	
	public int getElementsToPopWhenHeapIsFull(){
		return this.elementsToPopWhenHeapIsFull;
	}

	public static TOSORT getDEFAULT_TOSORT() {
		return DEFAULT_TOSORT;
	}

	public static void setDEFAULT_TOSORT(TOSORT dEFAULT_TOSORT) {
		DEFAULT_TOSORT = dEFAULT_TOSORT;
	}

	public static int getDEFAULT_TOSORT_SIZE() {
		return DEFAULT_TOSORT_SIZE;
	}

	public static void setDEFAULT_TOSORT_SIZE(int dEFAULT_TOSORT_SIZE) {
		DEFAULT_TOSORT_SIZE = dEFAULT_TOSORT_SIZE;
	}

	public static HEAPTYPE getDEFAULT_HEAP_TYPE() {
		return DEFAULT_HEAP_TYPE;
	}

	public static void setDEFAULT_HEAP_TYPE(HEAPTYPE dEFAULT_HEAP_TYPE) {
		DEFAULT_HEAP_TYPE = dEFAULT_HEAP_TYPE;
	}

	public static int getDEFAULT_HEIGHT() {
		return DEFAULT_HEIGHT;
	}

	public static void setDEFAULT_HEIGHT(int dEFAULT_HEIGHT) {
		DEFAULT_HEIGHT = dEFAULT_HEIGHT;
	}

	public static HEAPTYPE getDEFAULT_MERGEHEAP_TYPE() {
		return DEFAULT_MERGEHEAP_TYPE;
	}

	public static void setDEFAULT_MERGEHEAP_TYPE(HEAPTYPE dEFAULT_MERGEHEAP_TYPE) {
		DEFAULT_MERGEHEAP_TYPE = dEFAULT_MERGEHEAP_TYPE;
	}

	public static int getDEFAULT_MERGE_HEAP_HEIGHT() {
		return DEFAULT_MERGEHEAP_HEIGHT;
	}

	public static void setDEFAULT_MERGE_HEAP_HEIGHT(int dEFAULT_MERGE_HEAP_HEIGHT) {
		DEFAULT_MERGEHEAP_HEIGHT = dEFAULT_MERGE_HEAP_HEIGHT;
	}

	public static int getDEFAULT_K() {
		return DEFAULT_K;
	}

	public static void setDEFAULT_K(int dEFAULT_K) {
		DEFAULT_K = dEFAULT_K;
	}

	public static Compression getDEFAULT_COMPRESSION() {
		return DEFAULT_COMPRESSION;
	}

	public static void setDEFAULT_COMPRESSION(Compression dEFAULT_COMPRESSION) {
		DEFAULT_COMPRESSION = dEFAULT_COMPRESSION;
	}

	public int getMergeHeapHeight() {
		return this.mergeHeapHeight;
	}
}
