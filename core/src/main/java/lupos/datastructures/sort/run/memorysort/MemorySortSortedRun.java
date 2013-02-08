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
package lupos.datastructures.sort.run.memorysort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import lupos.datastructures.sort.run.Run;

/**
 * This run represents an already sorted run in main memory.
 * It is saving main memory as it uses difference encoding
 * in order to avoid storing common prefixes of succeeding elements.  
 */
public class MemorySortSortedRun extends Run {
	
	/**
	 * Class to store a string without the common prefix with the previous string.
	 * This class is used to implement difference encoding in order to save space. 
	 */
	public final static class StringWithoutCommonPrefix implements Serializable {
		
		public final String content;
		
		public final int endCommonPrefix;
		
		public StringWithoutCommonPrefix(final String content, final int endCommonPrefix){
			this.content = content;
			this.endCommonPrefix = endCommonPrefix;
		}
		
		public final String getWholeString(String lastString){
			return lastString.substring(0, this.endCommonPrefix) + this.content;
		}
	}
	
	/**
	 * This method computes a difference encoded string
	 * @param content the string to be encoded with difference encoding
	 * @param lastString the previously stored string with which content is checked to have a common prefix
	 * @return the difference encoded string equivalent to content
	 */
	public static StringWithoutCommonPrefix createStringWithoutCommonPrefix(final String content, final String lastString){
		final int max = Math.min(content.length(), lastString.length()); 
		
		int end = max;
		
		for(int i=0; i<max; i++){
			if(content.charAt(i) != lastString.charAt(i)){
				end = i;
				break;
			}
		}
		
		return new StringWithoutCommonPrefix(content.substring(end), end);
	}
	
	protected final StringWithoutCommonPrefix[] runContent;
	
	public MemorySortSortedRun(final Iterator<String> emptyDatastructure, final int maxsize, final boolean set) {
		if(set){
			ArrayList<String> contents = new ArrayList<String>(maxsize);
			String lastString = null;
			while(emptyDatastructure.hasNext()){
				String nextString = emptyDatastructure.next();
				if(lastString==null || lastString.compareTo(nextString)!=0){
					contents.add(nextString);
				}
				lastString = nextString;
			}
			if(contents.size()==0){
				throw new RuntimeException("Empty runs are not allowed to become a MemorySortSortedRun!");
			}
			this.runContent = new StringWithoutCommonPrefix[contents.size()];
			lastString = "";
			int id = 0;
			for(String string: contents){
				this.runContent[id] = MemorySortSortedRun.createStringWithoutCommonPrefix(string, lastString);
				id++;
				lastString = string;
			}
		} else {
			this.runContent = new StringWithoutCommonPrefix[maxsize];
			String lastString = "";
			int id = 0;
			while(emptyDatastructure.hasNext()){
				String nextString = emptyDatastructure.next();
				this.runContent[id] = MemorySortSortedRun.createStringWithoutCommonPrefix(nextString, lastString);
				id++;
				lastString = nextString;
			}			
		}
	}

	@Override
	public boolean add(String toBeAdded) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Run sort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Run swapRun() {
		return new MemorySortSortedRunOnDisk(this.runContent);
	}

	@Override
	public boolean isEmpty() {
		// empty runs are not going to become sorted runs!
		return false;
	}

	@Override
	public Iterator<String> iterator() {		
		return new Iterator<String>(){
			
			int index = 0;
			String lastString = "";

			@Override
			public boolean hasNext() {				
				return (this.index < MemorySortSortedRun.this.runContent.length);
			}

			@Override
			public String next() {
				if(this.hasNext()){
					String result = MemorySortSortedRun.this.runContent[this.index].getWholeString(this.lastString);
					this.index++;
					this.lastString = result;
					return result;
				} else {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}			
		};
	}

	@Override
	public int size() {		
		return this.runContent.length;
	}
}
