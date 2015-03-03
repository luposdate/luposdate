
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.autocomplete.misc;

import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.ILuposToken;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
public class RIFParserHelper implements ILuposParser {

	protected final ILuposParser parser;
	protected ILuposToken token = null;

	/**
	 * <p>Constructor for RIFParserHelper.</p>
	 *
	 * @param parser a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 */
	public RIFParserHelper(final ILuposParser parser) {
		this.parser = parser;
	}

	/** {@inheritDoc} */
	@Override
	public ILuposToken getNextToken(String content) {
		if (this.token != null) {
			ILuposToken iToken = this.token;
			this.token = null;
			return iToken;
		}
		ILuposToken current = this.parser.getNextToken(content);
		if (current == null
				|| current.getDescription() != TYPE__SemanticWeb.VARIABLE) {
			return current;
		}
		ILuposToken next = this.parser.getNextToken(content);
		if (next == null
				|| next.getDescription() != TYPE__SemanticWeb.IDENTIFIER) {
			this.token = next;
			return current;
		}
		return current.create(
				TYPE__SemanticWeb.VARIABLE,
				content.substring(current.getBeginChar(), next.getBeginChar()
						+ next.getContents().length()), current.getBeginChar());
	}

	/** {@inheritDoc} */
	@Override
	public void setReader(LuposDocumentReader stream, int beginChar, int endChar) {
		this.parser.setReader(stream, beginChar, endChar);
	}

	/** {@inheritDoc} */
	@Override
	public void setReader(LuposDocumentReader s) {
		this.parser.setReader(s);
	}

	/** {@inheritDoc} */
	@Override
	public LuposDocumentReader getReader() {
		return this.parser.getReader();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEOF() {
		return this.parser.isEOF();
	}

	/** {@inheritDoc} */
	@Override
	public void setReaderTokenFriendly(LuposDocumentReader stream,
			int beginChar, int endChar) {
		this.parser.setReaderTokenFriendly(stream, beginChar, endChar);
	}
}
