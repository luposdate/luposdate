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
package lupos.datastructures.paged_dbbptree.node.nodedeserializer;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lupos.datastructures.paged_dbbptree.node.DBBPTreeEntry;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.io.LuposObjectInputStream;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.BitVector;
import lupos.misc.Tuple;
public class StringArrayNodeDeSerializer implements NodeDeSerializer<String[], String[]> {

	private static final long serialVersionUID = -8131702796960262942L;

	protected final RDF3XIndexScan.CollationOrder order;

	/** Constant <code>map={ { 0, 1, 2 }, // SPO
			{ 0, 2, 1 }, // SOP
			{ 1, 0, 2 }, // PSO
			{ 1, 2, 0 }, // POS
			{ 2, 0, 1 }, // OSP
			{ 2, 1, 0 } // OPS
	}</code> */
	protected final static int[][] map = { { 0, 1, 2 }, // SPO
			{ 0, 2, 1 }, // SOP
			{ 1, 0, 2 }, // PSO
			{ 1, 2, 0 }, // POS
			{ 2, 0, 1 }, // OSP
			{ 2, 1, 0 } // OPS
	};

	/**
	 * <p>Constructor for LazyLiteralNodeDeSerializer.</p>
	 *
	 * @param order a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public StringArrayNodeDeSerializer(final RDF3XIndexScan.CollationOrder order) {
		this.order = order;
	}

	private final static int lengthOfCommonPrefix(final String current, final String lastKey, final BitVector bits, int index, final OutputStream out){
		try {
			final byte[] bytesOfS = current.getBytes(LuposObjectInputStream.UTF8);
			if(lastKey==null){
				return determineNumberOfBytesForRepresentation(bytesOfS.length, bits, index, out);
			}
			final byte[] previousString = lastKey.getBytes(LuposObjectInputStream.UTF8);
			// determine common prefix of new string and last stored string
			int common = 0;
			while(common<bytesOfS.length && common < previousString.length && bytesOfS[common]==previousString[common]){
				common++;
			}
			final int length = bytesOfS.length;
			index = determineNumberOfBytesForRepresentation(common, bits, index, out);
			index = determineNumberOfBytesForRepresentation(length-common, bits, index, out);
			return index;
		} catch(final Exception e){
			System.err.println(e);
			e.printStackTrace();
			return index;
		}
	}

	public final void writeWithoutCommonPrefix(final String s, final String lastKey, final OutputStream out) throws IOException {
		final byte[] bytesOfS = s.getBytes(LuposObjectInputStream.UTF8);
		final int length = bytesOfS.length;
		if(lastKey==null){
			writeIntWithoutLeadingZeros(length, out);
			out.write(bytesOfS);
			return;
		}
		final byte[] previousString = lastKey.getBytes(LuposObjectInputStream.UTF8);
		// determine common prefix of new string and last stored string
		int common = 0;
		while(common<bytesOfS.length && common < previousString.length && bytesOfS[common]==previousString[common]){
			common++;
		}
		writeIntWithoutLeadingZeros(common, out);

		// now write only difference string
		writeIntWithoutLeadingZeros(length-common, out);
		out.write(bytesOfS, common, length-common);
	}

	/** {@inheritDoc} */
	@Override
	public void writeInnerNodeEntry(final int fileName, final String[] key, final OutputStream out, final String[] lastKey) throws IOException {
		final BitVector bits = new BitVector(7);
		bits.set(0); // full entry with key!
		int index = 0;
		for(int i=0; i<3; i++){
			index++;
			if(lastKey!=null && lastKey[i].compareTo(key[i])==0){
				bits.set(index);
			} else {
				bits.clear(index);
				// determine length of writing String:
				index = lengthOfCommonPrefix(key[i], (lastKey==null)? null: lastKey[i], bits, index, out);
			}
		}
		index = determineNumberOfBytesForRepresentation(fileName, bits, index, out);
		bits.writeWithoutSize(out);
		writeIntWithoutLeadingZeros(fileName, out);
		for(int i=0; i<3; i++){
			if(lastKey==null || lastKey[i].compareTo(key[i])!=0){
				this.writeWithoutCommonPrefix(key[i], (lastKey==null)? null:lastKey[i], out);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void writeInnerNodeEntry(final int fileName, final OutputStream out) throws IOException {
		final BitVector bits = new BitVector(7);
		bits.clear(0);
		determineNumberOfBytesForRepresentation(fileName, bits, 0, out);
		bits.writeWithoutSize(out);
		writeIntWithoutLeadingZeros(fileName, out);
	}

	/** {@inheritDoc} */
	@Override
	public void writeLeafEntry(final String[] key, final String[] v,
			final OutputStream out, final String[] lastKey,
			final String[] lastValue) throws IOException {
		final BitVector bits = new BitVector(7);
		bits.set(0);
		int index = 0;
		for(int i=0; i<3; i++){
			index++;
			if(lastKey!=null && lastKey[i].compareTo(key[i])==0){
				bits.set(index);
			} else {
				bits.clear(index);
				// determine length of writing String:
				index = lengthOfCommonPrefix(key[i], (lastKey==null)? null: lastKey[i], bits, index, out);
			}
		}
		bits.writeWithoutSize(out);
		for(int i=0; i<3; i++){
			if(lastKey==null || lastKey[i].compareTo(key[i])!=0){
				this.writeWithoutCommonPrefix(key[i], (lastKey==null)? null: lastKey[i], out);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void writeLeafEntryNextFileName(final int filename, final OutputStream out) throws IOException {
		final BitVector bits = new BitVector(7);
		bits.clear(0);
		determineNumberOfBytesForRepresentation(filename, bits, 0, out);
		bits.writeWithoutSize(out);
		writeIntWithoutLeadingZeros(filename, out);
	}

	/**
	 * <p>writeIntWithoutLeadingZeros.</p>
	 *
	 * @param diff a int.
	 * @param out a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public static void writeIntWithoutLeadingZeros(final int diff,
			final OutputStream out) throws IOException {
		switch (determineNumberOfBytesForRepresentation(diff)) {
		case 0:
			OutHelper.writeLuposInt1Byte(diff, out);
			break;
		case 1:
			OutHelper.writeLuposInt2Bytes(diff, out);
			break;
		case 2:
			OutHelper.writeLuposInt3Bytes(diff, out);
			break;
		default:
		case 3:
			OutHelper.writeLuposInt(diff, out);
			break;
		}
	}

	/**
	 * <p>determineNumberOfBytesForRepresentation.</p>
	 *
	 * @param diff a int.
	 * @param bits a {@link lupos.misc.BitVector} object.
	 * @param index a int.
	 * @param out a {@link java.io.OutputStream} object.
	 * @return a int.
	 * @throws java.io.IOException if any.
	 */
	public static int determineNumberOfBytesForRepresentation(
			final int diff, final BitVector bits, int index,
			final OutputStream out) throws IOException {
		final int number = determineNumberOfBytesForRepresentation(diff);
		index++;
		if (index == 8) {
			bits.writeWithoutSize(out);
			index = 0;
		}
		if (number >= 2) {
			bits.set(index);
		} else {
			bits.clear(index);
		}
		index++;
		if (index == 8) {
			bits.writeWithoutSize(out);
			index = 0;
		}
		if (number % 2 == 0) {
			bits.clear(index);
		} else {
			bits.set(index);
		}
		return index;
	}

	/**
	 * <p>determineNumberOfBytesForRepresentation.</p>
	 *
	 * @param diff a int.
	 * @return a int.
	 */
	public static int determineNumberOfBytesForRepresentation(int diff) {
		if (diff < 0) {
			diff *= -1;
		}
		int number = 0;
		while (diff >= 256) {
			diff /= 256;
			number++;
		}
		return number;
	}

	/**
	 * <p>readInt.</p>
	 *
	 * @param bv a {@link lupos.misc.BitVector} object.
	 * @param index a int.
	 * @param in a {@link java.io.InputStream} object.
	 * @return a int.
	 * @throws java.io.IOException if any.
	 */
	public static int readInt(final BitVector bv, final int index,
			final InputStream in) throws IOException {
		if (bv.get(index)) {
			if (bv.get(index + 1)) {
				return InputHelper.readLuposInteger(in);
			} else {
				return  InputHelper.readLuposInteger3Bytes(in);
			}
		} else {
			if (bv.get(index + 1)) {
				return  InputHelper.readLuposInteger2Bytes(in);

			} else {
				return  InputHelper.readLuposInteger1Byte(in);
			}
		}
	}

	/**
	 * <p>getIntSize.</p>
	 *
	 * @param bits a {@link lupos.misc.BitVector} object.
	 * @param index a int.
	 * @param in a {@link java.io.InputStream} object.
	 * @return a int.
	 * @throws java.io.IOException if any.
	 */
	public static int getIntSize(final BitVector bits, int index, final InputStream in) throws IOException {
		index++;
		if (index == 8) {
			bits.readWithoutSize(in, 7);
			index = 0;
		}
		int number = 0;
		if (bits.get(index)) {
			number = 2;
		}
		index++;
		if (index == 8) {
			bits.readWithoutSize(in, 7);
			index = 0;
		}
		if (bits.get(index)) {
			number += 1;
		}
		return number;
	}

	/**
	 * <p>getInt.</p>
	 *
	 * @param number a int.
	 * @param in a {@link java.io.InputStream} object.
	 * @return a int.
	 * @throws java.io.IOException if any.
	 */
	public static int getInt(final int number,
			final InputStream in) throws IOException {
		switch (number) {
		case 0:
			return 0;
		case 1:
			return InputHelper.readLuposInteger1Byte(in);
		case 2:
			return InputHelper.readLuposInteger2Bytes(in);
		case 3:
			return InputHelper.readLuposInteger3Bytes(in);
		default:
		case 4:
			return InputHelper.readLuposInteger(in);
		}
	}

	/** {@inheritDoc} */
	@Override
	public DBBPTreeEntry<String[], String[]> getNextLeafEntry(
			final InputStream in, final String[] lastKey,
			final String[] lastValue) {
		return this.getNextLeafEntry(lastValue, in);
	}

	private synchronized DBBPTreeEntry<String[], String[]> getNextLeafEntry(final String[] lastKey, final InputStream in) {
		try {
			BitVector bits;
			try {
				bits = new BitVector(in, 7);
			} catch (final EOFException e) {
				return null;
			}
			if (!bits.get(0)) {
				return new DBBPTreeEntry<String[], String[]>(null, null, readInt(bits, 1, in));
			}
			final int filenameOfNextLeafNode = -1;
			int index = 0;
			final boolean[] same = new boolean[3];
			final int[] numberOfBytesForCommon = new int[3];
			final int[] numberOfBytesForLength = new int[3];
			for(int i=0; i<3; i++){
				index++;
				same[i] = bits.get(index);
				if(!bits.get(index)){
					if(lastKey!=null){
						numberOfBytesForCommon[i] = getIntSize(bits, index, in) + 1;
						index = (index + 2) % 8;
					}
					numberOfBytesForLength[i] = getIntSize(bits, index, in) + 1;
					index = (index + 2) % 8;
				}
			}
			final String[] result = new String[3];
			for(int i=0; i<3; i++){
				if(same[i]){
					result[i] = lastKey[i];
				} else {
					if(lastKey==null){
						result[i] = this.readString(getInt(numberOfBytesForLength[i], in), in);
					} else {
						result[i] = this.readString(getInt(numberOfBytesForCommon[i], in), getInt(numberOfBytesForLength[i], in), lastKey[i], in);
					}
				}
			}
			return new DBBPTreeEntry<String[], String[]>(result, result, filenameOfNextLeafNode);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
		}
		return null;
	}

	public String readString(final int common, final int length, final String lastString, final InputStream in) throws IOException{
		final byte[] lastStringArray = lastString.getBytes(LuposObjectInputStream.UTF8);
		final byte[] bytesOfResult = new byte[common + length];
		System.arraycopy(lastStringArray, 0, bytesOfResult, 0, common);
		// now read only difference string
		in.read(bytesOfResult, common, length);
		return new String(bytesOfResult, LuposObjectInputStream.UTF8);
	}

	public String readString(final int length, final InputStream in) throws IOException{
		final byte[] bytesOfResult = new byte[length];
		// now read string
		in.read(bytesOfResult);
		return new String(bytesOfResult, LuposObjectInputStream.UTF8);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized Tuple<String[], Integer> getNextInnerNodeEntry(final String[] lastKey, final InputStream in) {
		try {
			BitVector bits;
			try {
				bits = new BitVector(in, 7);
			} catch (final EOFException e) {
				return null;
			}
			if (!bits.get(0)) {
				return new Tuple<String[], Integer>(null, readInt(bits, 1, in));
			}
			int index = 0;
			final boolean[] same = new boolean[3];
			final int[] numberOfBytesForCommon = new int[3];
			final int[] numberOfBytesForLength = new int[3];
			for(int i=0; i<3; i++){
				index++;
				same[i] = bits.get(index);
				if(!bits.get(index)){
					if(lastKey!=null){
						numberOfBytesForCommon[i] = getIntSize(bits, index, in) + 1;
						index = (index + 2) % 8;
					}
					numberOfBytesForLength[i] = getIntSize(bits, index, in) + 1;
					index = (index + 2) % 8;
				}
			}
			final int fileName = getInt(getIntSize(bits, index, in) + 1, in);
			final String[] result = new String[3];
			for(int i=0; i<3; i++){
				if(same[i]){
					result[i] = lastKey[i];
				} else {
					if(lastKey==null){
						result[i] = this.readString(getInt(numberOfBytesForLength[i], in), in);
					} else {
						result[i] = this.readString(getInt(numberOfBytesForCommon[i], in), getInt(numberOfBytesForLength[i], in), lastKey[i], in);
					}
				}
			}
			return new Tuple<String[], Integer>(result, fileName);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
			// just ignore...
		}
		return null;
	}

	/**
	 * <p>getCollationOrder.</p>
	 *
	 * @return a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public RDF3XIndexScan.CollationOrder getCollationOrder(){
		return this.order;
	}
}
