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
package lupos.datastructures.items.literal.codemap;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.datastructures.patriciatrie.ram.RBTrieMap;
import lupos.datastructures.patriciatrie.util.TrieMapImplementation;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class CodeMapLiteral extends Literal implements Item,
		Comparable<Literal>, Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3118665134140832208L;
	protected int code;
	
	private final static ReentrantLock lock=new ReentrantLock();

	public CodeMapLiteral(final int code) {
		this.code = code;
	}

	public CodeMapLiteral() {
	}

	public CodeMapLiteral(String content) {
		if (content.length() >= 6 && content.startsWith("\"\"\"")
				&& content.endsWith("\"\"\""))
			content = "\"" + content.substring(3, content.length() - 3) + "\"";
		else if (content.length() >= 6 && content.startsWith("'''")
				&& content.endsWith("'''"))
			content = "\"" + content.substring(3, content.length() - 3) + "\"";
		else if (content.length() >= 2 && content.startsWith("'")
				&& content.endsWith("'"))
			content = "\"" + content.substring(1, content.length() - 1) + "\"";
		lock.lock();
		try{
			final Integer codeFromHashMap = hm.get(content);
			if (codeFromHashMap != null && codeFromHashMap != 0) {
				this.code = codeFromHashMap.intValue();
			} else {
				this.code = v.size() + 1;
				hm.put(content, new Integer(this.code));
				if (code == Integer.MAX_VALUE)
					System.err.println("Literal code overflow! Not good!");
				v.put(new Integer(this.code), content);
			}
		}finally{
			lock.unlock();
		}
	}

	public boolean valueEquals(final CodeMapLiteral lit) {
		return (code == lit.code);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof CodeMapLiteral) {
			final CodeMapLiteral lit = (CodeMapLiteral) obj;
			return valueEquals(lit);
		} else if (obj instanceof StringLiteral) {
			return super.equals(obj);
		} else if (obj instanceof TypedLiteral) {
			final TypedLiteral tl = (TypedLiteral) obj;
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#string>") == 0)
				return (tl.getContent().compareTo(this.toString()) == 0);
			else
				return false;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return code;
	}

	@Override
	public String toString() {
		return v.get(code);
	}

	public static String getValue(final int codeParam) {
		return v.get(codeParam);
	}

	public int getCode() {
		return code;
	}

	private void writeObject(final java.io.ObjectOutputStream out)
			throws IOException {
		out.writeInt(code);
	}

	private void readObject(final java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		code = in.readInt();
	}

	protected static StringIntegerMap hm = null;
	protected static IntegerStringMap v = null;

	public static int maxID() {
		return v.size();
	}

	public static void init() {
		if (hm == null
				&& LiteralFactory.getMapType() != MapType.NOCODEMAP
				&& LiteralFactory.getMapType() != MapType.LAZYLITERAL
				&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
			switch (LiteralFactory.getMapType()) {
			default:
				hm = new StringIntegerMapJava(LiteralFactory.getMapType());
				v = new IntegerStringMapJava(LiteralFactory.getMapType());
				break;
			case TRIEMAP:
				TrieMapImplementation<Integer> trieMap = new TrieMapImplementation<Integer>(new RBTrieMap<Integer>());
				hm = new StringIntegerMapLock(lock,new StringIntegerMapJava(trieMap));
				v = new IntegerStringMapLock(lock,new IntegerStringMapArray());
				break;
			}
		}
	}

	public static StringIntegerMap getHm() {
		return hm;
	}

	public static void setHm(final StringIntegerMap hm) {
		CodeMapLiteral.hm = hm;
	}

	public static IntegerStringMap getV() {
		return v;
	}

	public static void setV(final IntegerStringMap v) {
		CodeMapLiteral.v = v;
	}

	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { this.toString() };
	}

	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		code = LuposObjectInputStream.readLuposInt(in);
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		LuposObjectOutputStream.writeLuposInt(code, out);
	}

	@Override
	public String printYagoStringWithPrefix() {
		final String s = toString();
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return "\""
					+ s.substring(1, s.length() - 1).replaceAll(
							Pattern.quote("\""),
							Matcher.quoteReplacement("&quot;")) + "\"";
		} else
			return s;
	}

	@Override
	public Literal createThisLiteralNew() {
		return LiteralFactory.createLiteral(this.originalString());
	}
}
