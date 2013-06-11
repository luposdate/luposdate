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
import lupos.optimizations.logical.statistics.VarBucket;

public class StringVarBucketArrayNodeDeSerializer implements NodeDeSerializer<String, VarBucket[]> {

	private final static byte MORE = 0;
	private final static byte LAST = 1;

	@Override
	public Tuple<String, Integer> getNextInnerNodeEntry(final String lastKey2, final InputStream in2) {
		Integer file = null;
		try {
			file = InputHelper.readLuposIntVariableBytes(in2);
		} catch (final IOException e1) {
		}
		if(file==null){
			return null;
		}
		String newKey = null;
		try {
			newKey = InputHelper.readLuposString(lastKey2, in2);
		} catch (final IOException e) {
		}
		return new Tuple<String, Integer>(newKey, file);
	}

	@Override
	public DBBPTreeEntry<String, VarBucket[]> getNextLeafEntry(final InputStream in, final String lastKey, final VarBucket[] lastValue) {
		try {
			final byte moreOrLAST = InputHelper.readLuposByte(in);
			if(moreOrLAST == StringVarBucketArrayNodeDeSerializer.MORE){
				return new DBBPTreeEntry<String, VarBucket[]>(InputHelper.readLuposString(lastKey, in), InputHelper.readLuposVarBucketArray(in));
			} else {
				return new DBBPTreeEntry<String, VarBucket[]>(InputHelper.readLuposIntVariableBytes(in));
			}
		} catch (final IOException e) {
			return null;
		}
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
	public void writeLeafEntry(final String k, final VarBucket[] v, final OutputStream out, final String lastKey, final VarBucket[] lastValue) throws IOException {
		OutHelper.writeLuposByte(StringVarBucketArrayNodeDeSerializer.MORE, out);
		OutHelper.writeLuposString(k, lastKey, out);
		OutHelper.writeLuposVarBucketArray(v, out);
	}

	@Override
	public void writeLeafEntryNextFileName(final int filename, final OutputStream out) throws IOException {
		OutHelper.writeLuposByte(StringVarBucketArrayNodeDeSerializer.LAST, out);
		OutHelper.writeLuposIntVariableBytes(filename, out);
	}
}
