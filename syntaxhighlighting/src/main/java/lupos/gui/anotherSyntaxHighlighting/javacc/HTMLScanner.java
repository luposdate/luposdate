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
package lupos.gui.anotherSyntaxHighlighting.javacc;

import java.io.InputStream;
import java.io.Reader;

import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.ILuposToken;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE_ENUM;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__HTML;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.javacc.JAVACCParser.PARSER;
import lupos.gui.anotherSyntaxHighlighting.javacc.JAVACCParser.TOKEN;
import lupos.gui.anotherSyntaxHighlighting.javacc.html.HTMLScannerConstants;
import lupos.gui.anotherSyntaxHighlighting.javacc.html.Token;

public class HTMLScanner implements PARSER {

	private static TYPE__HTML[] TOKEN_MAP;
	private final lupos.gui.anotherSyntaxHighlighting.javacc.html.HTMLScanner parser;
	
	private HTMLScanner(final LuposDocumentReader reader){
		this.parser = new lupos.gui.anotherSyntaxHighlighting.javacc.html.HTMLScanner(reader);
	}
	
	public static ILuposParser createILuposParser(final LuposDocumentReader reader){
		return new JAVACCParser(reader, new HTMLScanner(reader));
	}
	
	@Override
	public TOKEN getNextToken() {
		Token token = this.parser.getNextToken();
		if(token==null){
			return null;
		} else {
			return new HTMLToken(token);
		}			
	}

	@Override
	public TYPE__HTML[] getTokenMap() {
		return HTMLScanner.TOKEN_MAP;
	}

	@Override
	public void ReInit(Reader reader) {
		this.parser.ReInit(reader);
	}
	
	@Override
	public void ReInit(InputStream inputstream) {
		this.parser.ReInit(inputstream);
	}

	@Override
	public boolean isStartOfComment(final String content, final int beginChar){
		if(content.length()>beginChar+4){
			return content.substring(beginChar, beginChar+4).compareTo("<!--")==0;
		}		
		return false;
	}
	
	@Override
	public ILuposToken handleComment(final String content, final int beginChar){
		int endOfComment = beginChar+1;
		while(endOfComment<content.length()+3 && content.substring(endOfComment, endOfComment+3).compareTo("-->")!=0){
			endOfComment++;
		}
		return create(TYPE__HTML.COMMENT, content.substring(beginChar, endOfComment+3), beginChar);
	}
	
	@Override
	public boolean endOfSearchOfComment(final String content, final int beginChar){
		if(content.length()>beginChar+3){
			return content.substring(beginChar, beginChar+3).compareTo("-->")==0;
		}		
		return false;
	}
	
	{
		HTMLScanner.TOKEN_MAP = new TYPE__HTML[HTMLScannerConstants.tokenImage.length];
		
		insertIntoTokenMap(	new String[]{
						    "<PN_CHARS_BASE>",
    						"<IDENTIFIER>",
						    "<TAG>"
							}, TYPE__HTML.TAG);
		
		insertIntoTokenMap(	new String[]{
    						"<TEXT>"
							}, TYPE__HTML.TEXT);
		
		insertIntoTokenMap(	new String[]{
    						"<token of kind 5>"
							}, TYPE__HTML.COMMENT);
		
		insertIntoTokenMap(	new String[]{
    						"<ENDTAG>"
							}, TYPE__HTML.ENDTAG);
		
		insertIntoTokenMap(	new String[]{
    						"<STRING>"
							}, TYPE__HTML.VALUE);
		
		insertIntoTokenMap(	new String[]{
							"<ATTRIBUTE>"
							}, TYPE__HTML.NAME);
		
		insertIntoTokenMap(	new String[]{
							"<EOF>",
    						"\" \"",
    						"\"\\t\"",
    						"\"\\n\"",
    						"\"\\r\""
							}, TYPE__HTML.WHITESPACE);
		
		// to be checked: some types of TYPE__HTML are not used...
		checkTopicMap();
	}
	
	protected static void insertIntoTokenMap(final String[] imagesToSet, final TYPE__HTML type){
		JAVACCParser.insertIntoTokenMap(HTMLScannerConstants.tokenImage, HTMLScanner.TOKEN_MAP, imagesToSet, type);
	}

	protected static void checkTopicMap(){
		JAVACCParser.checkTopicMap(HTMLScannerConstants.tokenImage, HTMLScanner.TOKEN_MAP);
	}
	
	public static class HTMLToken implements TOKEN {

		private final Token htmltoken;
		
		public HTMLToken(final Token sparql1_1token){
			this.htmltoken = sparql1_1token; 
		}
		
		@Override
		public int getKind() {
			return this.htmltoken.kind;
		}

		@Override
		public String getImage() {
			return this.htmltoken.image;
		}		
	}

	@Override
	public ILuposToken create(final TYPE_ENUM description, final String contents, final int beginChar) {
		return new lupos.gui.anotherSyntaxHighlighting.javacc.HTMLToken(description, contents, beginChar);
	}

	@Override
	public ILuposToken createErrorToken(String contents, int beginChar) {
		return new lupos.gui.anotherSyntaxHighlighting.javacc.HTMLToken(TYPE__HTML.ERROR, contents, beginChar);
	}
}
