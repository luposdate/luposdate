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
package lupos.gui.anotherSyntaxHighlighting.javacc;

import java.io.InputStream;
import java.io.Reader;

import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.ILuposToken;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE_ENUM;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__JAVA;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.javacc.JAVACCParser.PARSER;
import lupos.gui.anotherSyntaxHighlighting.javacc.JAVACCParser.TOKEN;
import lupos.gui.anotherSyntaxHighlighting.javacc.java.JavaParser1_5Constants;
import lupos.gui.anotherSyntaxHighlighting.javacc.java.Token;
public class JavaScanner implements PARSER {

	private static TYPE__JAVA[] TOKEN_MAP;
	private final lupos.gui.anotherSyntaxHighlighting.javacc.java.JavaParser1_5 parser;

	private JavaScanner(final LuposDocumentReader reader){
		this.parser = new lupos.gui.anotherSyntaxHighlighting.javacc.java.JavaParser1_5(reader);
	}

	/**
	 * <p>createILuposParser.</p>
	 *
	 * @param reader a {@link lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader} object.
	 * @return a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 */
	public static ILuposParser createILuposParser(final LuposDocumentReader reader){
		return new JAVACCParser(reader, new JavaScanner(reader));
	}

	/** {@inheritDoc} */
	@Override
	public TOKEN getNextToken() {
		final Token token = this.parser.getNextToken();
		if(token==null){
			return null;
		} else {
			return new JavaToken(token);
		}
	}

	/** {@inheritDoc} */
	@Override
	public TYPE__JAVA[] getTokenMap() {
		return JavaScanner.TOKEN_MAP;
	}

	/**
	 * <p>getStaticTokenMap.</p>
	 *
	 * @return an array of {@link lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__JAVA} objects.
	 */
	public static TYPE__JAVA[] getStaticTokenMap() {
		return JavaScanner.TOKEN_MAP;
	}

	/** {@inheritDoc} */
	@Override
	public void ReInit(final Reader reader) {
		this.parser.ReInit(reader);
	}

	/** {@inheritDoc} */
	@Override
	public void ReInit(final InputStream inputstream) {
		this.parser.ReInit(inputstream);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isStartOfComment(final String content, final int beginChar){
		boolean flag = false;
		if(content.length()>beginChar+2){
			flag = content.substring(beginChar, beginChar+2).compareTo("//")==0;
			if(flag){
				return true;
			}
			return content.substring(beginChar, beginChar+2).compareTo("/*")==0;
		}
		return flag;
	}

	/** {@inheritDoc} */
	@Override
	public ILuposToken handleComment(final String content, final int beginChar){
		if(content.charAt(beginChar+1)=='*'){
			int endOfComment = beginChar+1;
			while(endOfComment<content.length()+2 && content.substring(endOfComment, endOfComment+2).compareTo("*/")!=0){
				endOfComment++;
			}
			return this.create(TYPE__JAVA.COMMENT, content.substring(beginChar, endOfComment+2), beginChar);
		} else {
			int endOfComment = beginChar+1;
			while(endOfComment<content.length() && content.charAt(endOfComment)!='\n'){
				endOfComment++;
			}
			return this.create(TYPE__JAVA.COMMENT, content.substring(beginChar, endOfComment), beginChar);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean endOfSearchOfComment(final String content, final int beginChar){
		final boolean flag = content.charAt(beginChar)=='\n';
		if(flag){
			return true;
		}
		if(content.length()>beginChar+2){
			return content.substring(beginChar, beginChar+2).compareTo("*/")==0;
		}
		return false;
	}

	{
		JavaScanner.TOKEN_MAP = new TYPE__JAVA[JavaParser1_5Constants.tokenImage.length];

		insertIntoTokenMap(	new String[]{
				   "<EOF>",
				    "\" \"",
				    "\"\\t\"",
				    "\"\\n\"",
				    "\"\\r\"",
				    "\"\\f\""
				}, TYPE__JAVA.WHITESPACE);

		insertIntoTokenMap(	new String[]{
			    "<token of kind 6>",
			    "<token of kind 7>"
				}, TYPE__JAVA.COMMENT);

		insertIntoTokenMap(	new String[]{
			    "\"abstract\"",
			    "\"assert\"",
			    "\"boolean\"",
			    "\"break\"",
			    "\"byte\"",
			    "\"case\"",
			    "\"catch\"",
			    "\"char\"",
			    "\"class\"",
			    "\"const\"",
			    "\"continue\"",
			    "\"default\"",
			    "\"do\"",
			    "\"double\"",
			    "\"else\"",
			    "\"enum\"",
			    "\"extends\"",
			    "\"false\"",
			    "\"final\"",
			    "\"finally\"",
			    "\"float\"",
			    "\"for\"",
			    "\"goto\"",
			    "\"if\"",
			    "\"implements\"",
			    "\"import\"",
			    "\"instanceof\"",
			    "\"int\"",
			    "\"interface\"",
			    "\"long\"",
			    "\"native\"",
			    "\"new\"",
			    "\"null\"",
			    "\"package\"",
			    "\"private\"",
			    "\"protected\"",
			    "\"public\"",
			    "\"return\"",
			    "\"short\"",
			    "\"static\"",
			    "\"strictfp\"",
			    "\"super\"",
			    "\"switch\"",
			    "\"synchronized\"",
			    "\"this\"",
			    "\"throw\"",
			    "\"throws\"",
			    "\"transient\"",
			    "\"true\"",
			    "\"try\"",
			    "\"void\"",
			    "\"volatile\"",
			    "\"while\""
				}, TYPE__JAVA.RESERVEDWORD);

		insertIntoTokenMap(	new String[]{
			    "<LONG_LITERAL>",
			    "<INTEGER_LITERAL>",
			    "<DECIMAL_LITERAL>",
			    "<HEX_LITERAL>",
			    "<OCTAL_LITERAL>",
			    "<FLOATING_POINT_LITERAL>",
			    "<DECIMAL_FLOATING_POINT_LITERAL>",
			    "<DECIMAL_EXPONENT>",
			    "<HEXADECIMAL_FLOATING_POINT_LITERAL>",
			    "<HEXADECIMAL_EXPONENT>",
			    "<CHARACTER_LITERAL>",
			    "<STRING_LITERAL>"
				}, TYPE__JAVA.LITERAL);

		insertIntoTokenMap(	new String[]{
			    "<IDENTIFIER>",
			    "<LETTER>",
			    "<PART_LETTER>"
				}, TYPE__JAVA.IDENTIFIER);

		insertIntoTokenMap(	new String[]{
			    "\"(\"",
			    "\")\"",
			    "\"{\"",
			    "\"}\"",
			    "\"[\"",
			    "\"]\"",
			    "\";\"",
			    "\",\"",
			    "\".\"",
			    "\"@\""
				}, TYPE__JAVA.SEPARATOR);

		insertIntoTokenMap(	new String[]{
			    "\"=\"",
			    "\"<\"",
			    "\"!\"",
			    "\"~\"",
			    "\"?\"",
			    "\":\"",
			    "\"==\"",
			    "\"<=\"",
			    "\">=\"",
			    "\"!=\"",
			    "\"||\"",
			    "\"&&\"",
			    "\"++\"",
			    "\"--\"",
			    "\"+\"",
			    "\"-\"",
			    "\"*\"",
			    "\"/\"",
			    "\"&\"",
			    "\"|\"",
			    "\"^\"",
			    "\"%\"",
			    "\"<<\"",
			    "\"+=\"",
			    "\"-=\"",
			    "\"*=\"",
			    "\"/=\"",
			    "\"&=\"",
			    "\"|=\"",
			    "\"^=\"",
			    "\"%=\"",
			    "\"<<=\"",
			    "\">>=\"",
			    "\">>>=\"",
			    "\"...\"",
			    "\"^^\"",
			    "\">>>\"",
			    "\">>\"",
			    "\">\""
				}, TYPE__JAVA.OPERATOR);


		checkTopicMap();
	}

	/**
	 * <p>insertIntoTokenMap.</p>
	 *
	 * @param imagesToSet an array of {@link java.lang.String} objects.
	 * @param type a {@link lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__JAVA} object.
	 */
	protected static void insertIntoTokenMap(final String[] imagesToSet, final TYPE__JAVA type){
		JAVACCParser.insertIntoTokenMap(JavaParser1_5Constants.tokenImage, JavaScanner.TOKEN_MAP, imagesToSet, type);
	}

	/**
	 * <p>checkTopicMap.</p>
	 */
	protected static void checkTopicMap(){
		JAVACCParser.checkTopicMap(JavaParser1_5Constants.tokenImage, JavaScanner.TOKEN_MAP);
	}

	public static class JavaToken implements TOKEN {

		private final Token javatoken;

		public JavaToken(final Token sparql1_1token){
			this.javatoken = sparql1_1token;
		}

		@Override
		public int getKind() {
			return this.javatoken.kind;
		}

		@Override
		public String getImage() {
			return this.javatoken.image;
		}
	}

	/** {@inheritDoc} */
	@Override
	public ILuposToken create(final TYPE_ENUM description, final String contents, final int beginChar) {
		return new lupos.gui.anotherSyntaxHighlighting.javacc.JavaToken(description, contents, beginChar);
	}

	/** {@inheritDoc} */
	@Override
	public ILuposToken createErrorToken(final String contents, final int beginChar) {
		return new lupos.gui.anotherSyntaxHighlighting.javacc.JavaToken(TYPE__JAVA.ERROR, contents, beginChar);
	}
}
