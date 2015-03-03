
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
package lupos.gui.anotherSyntaxHighlighting;
public interface ILuposParser {

	/** Constant <code>STREAM_END=-1</code> */
	public final int STREAM_END = -1;
	/** Constant <code>STREAM_FIRST=-1</code> */
	public final int STREAM_FIRST = -1;

	/**
	 * Returns the next token from stream. Returns null if the end of stream is reached.
	 *
	 * @return Returns the next Token by iteration from the given Stream.
	 * @param content a {@link java.lang.String} object.
	 */
	public ILuposToken getNextToken(final String content);

	/**
	 * Sets the Input Stream which is used by the specific Parser and reinitializes the parser.
	 * The accounted area is defined by the parameters beginChar and endChar.
	 *
	 * @param stream The used stream for parsing.
	 * @param beginChar The first char which shall be regarded within parsing.
	 * @param endChar The last char which shall be regarded within parsing.
	 */
	public void setReader(LuposDocumentReader stream, int beginChar, int endChar);

	/**
	 * Sets the Input Stream which is used by the specific Parser and reinitializes the parser.
	 *
	 * @param s The used stream for parsing.
	 */
	public void setReader(LuposDocumentReader s);

	/**
	 * Returns the used input stream.
	 *
	 * @return a {@link lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader} object.
	 */
	public LuposDocumentReader getReader();

	/**
	 *  Tells whether the end of file is reached within parsing. This means the end of the given inputStream was reached.
	 *
	 * @return Returns true if the end of file is reached within parsing.
	 */
	public boolean isEOF();

	/**
	 * Sets the Input Stream which is used by the specific Parser and reinitializes the parser.
	 * The accounted area is defined by the parameters beginChar and endChar but will be fitted
	 * to given Token already read.
	 *
	 * @param stream The used stream for parsing.
	 * @param beginChar The first char which shall be regarded within parsing.
	 * @param endChar The last char which shall be regarded within parsing.
	 */
	void setReaderTokenFriendly(LuposDocumentReader stream, int beginChar, int endChar);

}
