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
package lupos.gui.anotherSyntaxHighlighting;

import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE_ENUM;

/**
 * An interface for the Token
 *
 * @author groppe
 * @version $Id: $Id
 */
public interface ILuposToken {


	/**
	 * A description of this token. The description should be appropriate for
	 * syntax highlighting. For example "comment" might be returned for a
	 * comment. This should make it easy to do html syntax highlighting. Just
	 * use style sheets to define classes with the same name as the description
	 * and write the token in the html file with that css class name.
	 *
	 * @return a description of this token.
	 */
	public abstract TYPE_ENUM getDescription();

	/**
	 * The actual meat of the token.
	 *
	 * @return a string representing the text of the token.
	 */
	public abstract String getContents();

	/**
	 * get a String that explains the error, if this token is an error.
	 *
	 * @return a String that explains the error, if this token is an error, null
	 *         otherwise.
	 */
	public abstract String errorString();

	
	/**
	 * Gets the position of the first char of this token within the given text.
	 *
	 * @return The beginning position of this token.
	 */
	public int getBeginChar();
	
	/**
	 * <p>create.</p>
	 *
	 * @param description a {@link lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE_ENUM} object.
	 * @param contents a {@link java.lang.String} object.
	 * @param beginChar a int.
	 * @return a {@link lupos.gui.anotherSyntaxHighlighting.ILuposToken} object.
	 */
	public ILuposToken create(TYPE_ENUM description, String contents, int beginChar); 
	
}
