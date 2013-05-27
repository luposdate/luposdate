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
package lupos.datastructures.items.literal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;

/**
 * This class determines the type of it (like URILiteral, AnonymousLiteral,
 * TypedLiteral, ...) lazy, i.e., only up on request by a special method.
 * Internally, it uses a code for its string representation. Furthermore, it
 * stores a code for the original content, too, such that the original content
 * can be retrieved (as required by the DAWG test cases)!
 */
public class LazyLiteralOriginalContent extends LazyLiteral {

	/**
	 *
	 */
	private static final long serialVersionUID = 8200285980229881388L;
	private int codeOriginalContent;
	private String originalString = null;

	public LazyLiteralOriginalContent() {
		// nothing to initialize...
	}

	public LazyLiteralOriginalContent(final String content,
			final String originalContent) {
		super(content);
		final Integer codeFromHashMap = hm.get(originalContent);
		if (codeFromHashMap != null && codeFromHashMap != 0) {
			this.codeOriginalContent = codeFromHashMap.intValue();
		} else {
			lock.lock();
			try{
				this.codeOriginalContent = v.size() + 1;
				hm.put(originalContent, new Integer(this.codeOriginalContent));
				if (this.codeOriginalContent == Integer.MAX_VALUE) {
					System.err.println("Literal code overflow! Not good!");
				}
				v.put(new Integer(this.codeOriginalContent), originalContent);
			} finally{
				lock.unlock();
			}
		}
	}

	public LazyLiteralOriginalContent(final int code,
			final String originalContent) {
		super(code);
		final Integer codeFromHashMap = hm.get(originalContent);
		if (codeFromHashMap != null && codeFromHashMap != 0) {
			this.codeOriginalContent = codeFromHashMap.intValue();
		} else {
			lock.lock();
			try{
				this.codeOriginalContent = v.size() + 1;
				hm.put(originalContent, new Integer(this.codeOriginalContent));
				if (this.codeOriginalContent == Integer.MAX_VALUE) {
					System.err.println("Literal code overflow! Not good!");
				}
				v.put(new Integer(this.codeOriginalContent), originalContent);
			} finally{
				lock.unlock();
			}
		}
	}

	public LazyLiteralOriginalContent(final int code,
			final int codeOriginalContent) {
		super(code);
		this.codeOriginalContent = codeOriginalContent;
	}

	public LazyLiteralOriginalContent(final int code,
			final int codeOriginalContent, final Literal materializedLiteral) {
		super(code, materializedLiteral);
		this.codeOriginalContent = codeOriginalContent;
		this.originalString = materializedLiteral.originalString();
	}

	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { this.toString(), this.originalString() };
	}

	@Override
	public String getOriginalKey(){
		return ""+this.codeOriginalContent;
	}

	@Override
	public String originalString() {
		if (this.originalString == null) {
			this.originalString = v.get(this.codeOriginalContent);
		}
		return this.originalString;
	}

	public int getCodeOriginalContent() {
		return this.codeOriginalContent;
	}

	@Override
	public boolean originalStringDiffers() {
		return true;
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		this.codeOriginalContent = InputHelper.readLuposInt(in);
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		OutHelper.writeLuposInt(this.codeOriginalContent, out);
	}
}
