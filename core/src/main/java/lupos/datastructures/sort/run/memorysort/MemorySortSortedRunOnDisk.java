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
package lupos.datastructures.sort.run.memorysort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.sort.run.Run;
import lupos.datastructures.sort.run.memorysort.MemorySortSortedRun.StringWithoutCommonPrefix;
import lupos.io.Registration;
import lupos.io.Registration.DeSerializerExactClass;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;

/**
 * This run represents an already sorted run stored on disk.
 * It is saving space as it uses difference encoding
 * in order to avoid storing common prefixes of succeeding elements.
 */
public class MemorySortSortedRunOnDisk extends Run {

	static {
		/*
		 * Register a class to de-/serialize a StringWithoutCommonPrefix object
		 * with the luposdate i/o framework in an optimal way!
		 */
		Registration.addDeSerializer(new
				DeSerializerExactClass<StringWithoutCommonPrefix>(){

					@SuppressWarnings("unchecked")
					@Override
					public Class<? extends StringWithoutCommonPrefix>[] getRegisteredClasses() {
						return new Class[] {StringWithoutCommonPrefix.class};
					}

					@Override
					public int length(final StringWithoutCommonPrefix t) {
						return LengthHelper.lengthLuposString(t.content) + LengthHelper.lengthLuposInt();
					}

					@Override
					public void serialize(final StringWithoutCommonPrefix t, final OutputStream out) throws IOException {
						OutHelper.writeLuposString(t.content, out);
						OutHelper.writeLuposInt(t.endCommonPrefix, out);
					}

					@Override
					public StringWithoutCommonPrefix deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
						final String content = InputHelper.readLuposString(in);
						if(content==null){
							// end of file reached!
							return null;
						}
						return new StringWithoutCommonPrefix(content, InputHelper.readLuposInt(in));
					}

		});
	}

	/**
	 * the disk collection, which stores the collection of difference encoded strings on disk
	 */
	private final DiskCollection<StringWithoutCommonPrefix> diskCollection;

	public MemorySortSortedRunOnDisk(final StringWithoutCommonPrefix[] arrayOfStrings) {
		this.diskCollection = new DiskCollection<StringWithoutCommonPrefix>(StringWithoutCommonPrefix.class);
		for(final StringWithoutCommonPrefix swcp: arrayOfStrings){
			this.diskCollection.add(swcp);
		}
		this.diskCollection.close();
	}

	public MemorySortSortedRunOnDisk(final Iterator<String> emptyDatastructure, final boolean set) {
		this.diskCollection = new DiskCollection<StringWithoutCommonPrefix>(StringWithoutCommonPrefix.class);
		boolean firstTime = true;
		String lastString = "";
		while(emptyDatastructure.hasNext()){
			final String string = emptyDatastructure.next();
			if(!set || firstTime || string.compareTo(lastString)!=0){
				firstTime = false;
				this.diskCollection.add(MemorySortSortedRun.createStringWithoutCommonPrefix(string, lastString));
			}
			lastString = string;
		}
		if(this.diskCollection.size()==0){
			throw new RuntimeException("Empty runs are not allowed to become a MemorySortSortedRunOnDisk!");
		}
		this.diskCollection.close();
	}

	@Override
	public boolean add(final String toBeAdded) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Run sort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Run swapRun() {
		// this run is already stored on disk!
		return this;
	}

	@Override
	public boolean isEmpty() {
		// empty runs are not going to become sorted runs!
		return false;
	}

	@Override
	public ParallelIterator<String> iterator() {
		return new ParallelIterator<String>(){

			ParallelIterator<StringWithoutCommonPrefix> it = MemorySortSortedRunOnDisk.this.diskCollection.iterator();
			String lastString = "";

			@Override
			public final boolean hasNext() {
				return this.it.hasNext();
			}

			@Override
			public final String next() {
				if(this.hasNext()){
					final String result = this.it.next().getWholeString(this.lastString);
					this.lastString = result;
					return result;
				} else {
					return null;
				}
			}

			@Override
			public final void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {
				this.it.close();
			}

			@Override
			public void finalize(){
				this.close();
			}
		};
	}

	@Override
	public int size() {
		return this.diskCollection.size();
	}

	@Override
	public void release() {
		this.diskCollection.release();
	}
}
