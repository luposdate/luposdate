/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.datastructures.paged_dbbptree.node.nodedeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lupos.datastructures.paged_dbbptree.node.DBBPTreeEntry;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.Tuple;

public class StringIntegerNodeDeSerializer implements NodeDeSerializer<String, Integer> {

	@Override
	public Tuple<String, Integer> getNextInnerNodeEntry(final String lastKey2, final InputStream in2) {
		Integer nextFilename;
		try {
			nextFilename = InputHelper.readLuposIntVariableBytes(in2);
			if(nextFilename == null) {
				return null;
			}
		} catch (final java.io.EOFException e) {
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			System.out.println(e);
			return null;
		}
		if (nextFilename < 0) {
			return null;
		}
		String nextKey;
		try {
			nextKey = InputHelper.readLuposString(lastKey2, in2);
			return new Tuple<String, Integer>(nextKey, nextFilename);
		} catch (final java.io.EOFException e) {
			return new Tuple<String, Integer>(null, nextFilename);
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println(e);
			return null;
		}
	}

	@Override
	public DBBPTreeEntry<String, Integer> getNextLeafEntry(final InputStream in, final String lastKey, final Integer lastValue) {
		try {
			final Integer filenameOfNextLeafNode_or_Value = InputHelper.readLuposIntVariableBytes(in);
			if(filenameOfNextLeafNode_or_Value==null){
				return null;
			}
			final String nextKey = InputHelper.readLuposString(lastKey, in);
			if(nextKey==null){
				// no entry any more!
				// only reference to next leaf node!
				in.close();
				return new DBBPTreeEntry<String, Integer>(null, null, filenameOfNextLeafNode_or_Value);
			} else {
				// real leaf node entry with key and value!
				return new DBBPTreeEntry<String, Integer>(nextKey, filenameOfNextLeafNode_or_Value, -1);
			}

		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void writeInnerNodeEntry(final int fileName, final String key, final OutputStream out, final String lastKey) throws IOException {
		OutHelper.writeLuposIntVariableBytes(fileName, out);
		OutHelper.writeLuposString(key, lastKey, out);
	}

	@Override
	public void writeInnerNodeEntry(final int fileName, final OutputStream out) throws IOException {
		OutHelper.writeLuposIntVariableBytes(fileName, out);
	}

	@Override
	public void writeLeafEntry(final String k, final Integer v, final OutputStream out, final String lastKey, final Integer lastValue) throws IOException {
		OutHelper.writeLuposIntVariableBytes(v, out);
		OutHelper.writeLuposString(k, lastKey, out);
	}

	@Override
	public void writeLeafEntryNextFileName(final int filename, final OutputStream out) throws IOException {
		OutHelper.writeLuposIntVariableBytes(filename, out);
	}
}
