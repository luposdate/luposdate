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

import com.hp.hpl.jena.n3.turtle.parser.Token;
import com.hp.hpl.jena.n3.turtle.parser.TurtleParserConstants;

import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb;
import lupos.gui.anotherSyntaxHighlighting.javacc.JAVACCParser.TOKEN;

public class TurtleParser extends SemanticWebParser {

	private static TYPE__SemanticWeb[] TOKEN_MAP;
	private final com.hp.hpl.jena.n3.turtle.parser.TurtleParser parser;
	
	private TurtleParser(final LuposDocumentReader reader){
		this.parser = new com.hp.hpl.jena.n3.turtle.parser.TurtleParser(reader);
	}
	
	public static ILuposParser createILuposParser(final LuposDocumentReader reader){
		return new JAVACCParser(reader, new TurtleParser(reader));
	}
	
	@Override
	public TOKEN getNextToken() {
		Token token = this.parser.getNextToken();
		if(token==null){
			return null;
		} else {
			return new TurtleToken(token);
		}			
	}

	@Override
	public TYPE__SemanticWeb[] getTokenMap() {
		return TurtleParser.TOKEN_MAP;
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
		return content.charAt(beginChar)=='#';
	}
	
	@Override
	public SemanticWebToken handleComment(final String content, final int beginChar){
		int endOfComment = beginChar+1;
		while(endOfComment<content.length() && content.charAt(endOfComment)!='\n'){
			endOfComment++;
		}
		return new SemanticWebToken(TYPE__SemanticWeb.COMMENT, content.substring(beginChar, endOfComment), beginChar);
	}
	
	@Override
	public boolean endOfSearchOfComment(final String content, final int beginChar){
		return content.charAt(beginChar)=='\n';
	}
	
	{
		TurtleParser.TOKEN_MAP = new TYPE__SemanticWeb[TurtleParserConstants.tokenImage.length];
		
		
		final String[] reservedWords = {		
				"\"@prefix\"",
				"\"@base\"",
				"\"a\""
		};
		
		insertIntoTokenMap(reservedWords, TYPE__SemanticWeb.RESERVEDWORD);
		
		final String[] operators = {
				"\"!\"",
				"\"|\"",
				"\"^\"",
				"\"->\"",
				"\"<-\"",
				"\"=\"",
				"\"=>\"",
				"\"*\"",
				"\"/\"",
				"\"\\\\\"",
				};
		
		insertIntoTokenMap(operators, TYPE__SemanticWeb.OPERATOR);

		insertIntoTokenMap(new String[]{
				"\"(\"",
				"\")\"",
				"<NIL>",
				"\"{\"",
				"\"}\"",
				"\"[\"",
				"\"]\"",
				"<ANON>",
				"\";\"",
				"\",\"",
				"\".\"",
				"\"~\"",
				"\":\"",
				"\"^^\""
				}, TYPE__SemanticWeb.SEPARATOR);
		
		insertIntoTokenMap(new String[]{"<BLANK_NODE_LABEL>"}, TYPE__SemanticWeb.BLANKNODE);
		
		insertIntoTokenMap(new String[]{ "<VARNAME>", "<VAR>", "\"$\"", "\"?\""}, TYPE__SemanticWeb.VARIABLE);
		
		insertIntoTokenMap(new String[]{ "\"true\"", "\"false\""}, TYPE__SemanticWeb.BOOLEAN);
		
		insertIntoTokenMap(new String[]{ "<INTEGER>", "<DECIMAL>", "<DOUBLE>", "<EXPONENT>", "<DIGITS>"}, TYPE__SemanticWeb.NUMBER);
		
		insertIntoTokenMap(new String[]{ "<LANGTAG>", "\"@\""}, TYPE__SemanticWeb.LANGTAG);
		
		insertIntoTokenMap(new String[]{ "<PNAME_NS>", "<PNAME_LN>", "<PN_CHARS_BASE>", "<PN_CHARS_U>", "<PN_CHARS>", "<PN_PREFIX>", "<PN_LOCAL>", "<A2Z>", "<A2ZN>"}, TYPE__SemanticWeb.QUALIFIEDURI);
		
		insertIntoTokenMap(new String[]{ "<IRIref>"}, TYPE__SemanticWeb.URI);

		insertIntoTokenMap(new String[]{ "<STRING_LITERAL1>", "<STRING_LITERAL2>", "<STRING_LITERAL_LONG1>", "<STRING_LITERAL_LONG2>", "\"\\\"\\\"\\\"\"", "\"\\\'\\\'\\\'\""}, TYPE__SemanticWeb.LITERAL);

		insertIntoTokenMap(new String[]{ "<EOF>", "\" \"", "\"\\t\"", "\"\\n\"", "\"\\r\"", "\"\\f\"", "<WS>", "<ECHAR>" /* ??? */}, TYPE__SemanticWeb.WHITESPACE);
		
		insertIntoTokenMap(new String[]{ "<SINGLE_LINE_COMMENT>"}, TYPE__SemanticWeb.COMMENT);

		insertIntoTokenMap(new String[]{ "<UNKNOWN>"}, TYPE__SemanticWeb.ERROR);

		checkTopicMap();
	}
	
	protected static void insertIntoTokenMap(final String[] imagesToSet, final TYPE__SemanticWeb type){
		JAVACCParser.insertIntoTokenMap(TurtleParserConstants.tokenImage, TurtleParser.TOKEN_MAP, imagesToSet, type);
	}

	protected static void checkTopicMap(){
		JAVACCParser.checkTopicMap(TurtleParserConstants.tokenImage, TurtleParser.TOKEN_MAP);
	}
	
	public static class TurtleToken implements TOKEN {

		private final Token turtletoken;
		
		public TurtleToken(final Token sparql1_1token){
			this.turtletoken = sparql1_1token; 
		}
		
		@Override
		public int getKind() {
			return this.turtletoken.kind;
		}

		@Override
		public String getImage() {
			return this.turtletoken.image;
		}		
	}
}
