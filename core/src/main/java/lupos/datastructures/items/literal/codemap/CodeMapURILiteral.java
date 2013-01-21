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
package lupos.datastructures.items.literal.codemap;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.datastructures.trie.Trie;
import lupos.datastructures.trie.TrieMap;
import lupos.datastructures.trie.TrieMap.TrieAndArraySizesDoNotFitException;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class CodeMapURILiteral extends URILiteral implements Externalizable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2826285205704387677L;
	private final static ReentrantLock lock=new ReentrantLock();


	protected Literal content;
	protected int prefix;

	public CodeMapURILiteral() {
	}

	public CodeMapURILiteral(final String content)
			throws java.net.URISyntaxException {
		update(content);
	}

	@Override
	public void update(final String content) throws java.net.URISyntaxException {
		final String[] preAndPostfix = getPreAndPostfix(content);
		final String stringPrefix = preAndPostfix[0];
		final String stringContent = preAndPostfix[1];
		int code = -1;
		lock.lock();
		try{
			final Integer codeFromHashMap = hm.get(stringPrefix);
			if (codeFromHashMap != null && codeFromHashMap != 0) {
				code = codeFromHashMap.intValue();
			} else {
				code = v.size() + 1;
				hm.put(stringPrefix, new Integer(code));
				if (code == Integer.MAX_VALUE)
					System.err.println("Literal code overflow! Not good!");
				v.put(new Integer(code), stringPrefix);
			}
		}finally{
			lock.unlock();
		}
		this.prefix = code;
		this.content = LiteralFactory.createPostFixOfURI(stringContent);
	}

	public CodeMapURILiteral(final String content, final int prefixCode) {
		this.content = new StringLiteral(content);
		this.prefix = prefixCode;
	}

	public CodeMapURILiteral(final int contentCode, final int prefixCode) {
		this.content = new CodeMapLiteral(contentCode);
		this.prefix = prefixCode;
	}

	public Literal getContent() {
		return content;
	}

	public int getPrefixCode() {
		return prefix;
	}

	public String getPrefixString() {
		return v.get(prefix);
	}

	private static int max(final int a, final int b, final int c) {
		if (a > b && a > c)
			return a;
		if (b > c)
			return b;
		return c;
	}

	private int max(final int[] values) {
		Arrays.sort(values);
		return values[values.length - 1];
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof CodeMapURILiteral) {
			final CodeMapURILiteral lit = (CodeMapURILiteral) obj;
			return this.content.equals(lit.content)
					&& this.prefix == lit.prefix;
		} else if (obj instanceof URILiteral) {
			return super.equals(obj);
		} else
			return false; // (this.toString().compareTo(obj.toString()) == 0);
	}

	@Override
	public String getString() {
		return v.get(prefix) + content.toString();
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}

	public String printYagoStringWithPrefix() {
		return ">p"
				+ prefix
				+ ":"
				+ content.toString().replaceAll(Pattern.quote("<"),
						Matcher.quoteReplacement("&lt;")) + "<";
	}

	public static String[] getPreAndPostfix(String content)
			throws URISyntaxException {
		if (content == null
				|| !(content.startsWith("<") && content.endsWith(">"))) {
			System.out.println("Error: Expected URI, but " + content
					+ " is not an URI!");
			throw new java.net.URISyntaxException("Error: Expected URI, but "
					+ content + " is not an URI!", content);
		}
		content = content.substring(1, content.length() - 1);

		final int prefixEnd = max(content.lastIndexOf("/"), content
				.lastIndexOf("#"), content.lastIndexOf(":"));

		final String[] preAndPostfix = new String[2];
		if (prefixEnd >= 0) {
			preAndPostfix[0] = content.substring(0, prefixEnd + 1);
			preAndPostfix[1] = content.substring(prefixEnd + 1);
		} else {
			preAndPostfix[0] = "";
			preAndPostfix[1] = content;
		}
		return preAndPostfix;
	}

	protected static StringIntegerMap hm = null;
	protected static IntegerStringMap v = null;

	public static void init() {
		if (hm == null
				&& LiteralFactory.getMapType() != MapType.NOCODEMAP
				&& LiteralFactory.getMapType() != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
			switch (LiteralFactory.getMapType()) {
			default:
				hm = new StringIntegerMapJava(LiteralFactory.getMapType());
				v = new IntegerStringMapJava(LiteralFactory.getMapType());
				break;
			case TRIEMAP:
				TrieMap<Integer> trieMap;
				try {
					trieMap = new TrieMap<Integer>(new Trie(),new Integer[]{});
					ReentrantLock lock=new ReentrantLock();
					hm = new StringIntegerMapLock(lock,new StringIntegerMapJava(trieMap));
					v = new IntegerStringMapLock(lock,new IntegerStringMapTrieSlave(trieMap));
					break;
				} catch (TrieAndArraySizesDoNotFitException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}			
		}
	}

	public static StringIntegerMap getHm() {
		return hm;
	}

	public static void setHm(final StringIntegerMap hm) {
		CodeMapURILiteral.hm = hm;
	}

	public static IntegerStringMap getV() {
		return v;
	}

	public static void setV(final IntegerStringMap v) {
		CodeMapURILiteral.v = v;
	}

	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { content.toString(), getPrefixString() };
	}

	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		prefix = LuposObjectInputStream.readLuposInt(in);
		content = LuposObjectInputStream.readLuposLiteral(in);
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		LuposObjectOutputStream.writeLuposInt(prefix, out);
		LuposObjectOutputStream.writeLuposLiteral(content, out);
	}
}