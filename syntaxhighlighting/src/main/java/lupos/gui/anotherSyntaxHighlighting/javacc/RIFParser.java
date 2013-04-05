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
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb;
import lupos.gui.anotherSyntaxHighlighting.javacc.JAVACCParser.TOKEN;
import lupos.rif.generated.parser.RIFParserConstants;
import lupos.rif.generated.parser.Token;

public class RIFParser extends SemanticWebParser {

	private static TYPE__SemanticWeb[] TOKEN_MAP;
	private final lupos.rif.generated.parser.RIFParser parser;
	
	private RIFParser(final LuposDocumentReader reader){
		this.parser = new lupos.rif.generated.parser.RIFParser(reader);
	}
	
	public static ILuposParser createILuposParser(final LuposDocumentReader reader){
		return new JAVACCParser(reader, new RIFParser(reader));
	}
	
	@Override
	public TOKEN getNextToken() {
		Token token = this.parser.getNextToken();
		if(token==null){
			return null;
		} else {
			return new RIFToken(token);
		}			
	}

	@Override
	public TYPE__SemanticWeb[] getTokenMap() {
		return RIFParser.TOKEN_MAP;
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
		return content.length()>beginChar+1 && content.charAt(beginChar)=='(' && content.charAt(beginChar+1)=='*';
	}
	
	@Override
	public SemanticWebToken handleComment(final String content, final int beginChar){
		int endOfComment = beginChar+1;
		while(endOfComment<content.length() && !endOfSearchOfComment(content, endOfComment)){
			endOfComment++;
		}
		if(endOfComment<content.length()){
			endOfComment+=2;
		}
		return new SemanticWebToken(TYPE__SemanticWeb.COMMENT, content.substring(beginChar, endOfComment), beginChar);
	}
	
	@Override
	public boolean endOfSearchOfComment(final String content, final int beginChar){
		return content.length()>beginChar+1 && content.charAt(beginChar)=='*' && content.charAt(beginChar+1)==')';
	}
	
	{
		RIFParser.TOKEN_MAP = new TYPE__SemanticWeb[RIFParserConstants.tokenImage.length];
				
		final String[] reservedWords = {		
	    	    "\"document\"",
	    	    "\"group\"",
	    	    "\"forall\"",
	    	    "\"base\"",
	    	    "\"prefix\"",
	    	    "\"import\"",
	    	    "\"and\"",
	    	    "\"or\"",
	    	    "\"external\"",
	    	    "\"exists\"",
	    	    "\"list\"",
	    	    "\"conclusion\"",
			    "\"#\"",
			    "\"##\"",
			    "\"not\"",
		};
		
		insertIntoTokenMap(reservedWords, TYPE__SemanticWeb.RESERVEDWORD);
		
		insertIntoTokenMap(new String[]{ "\":-\"", "\"->\"", "\"=\"", "\"|\""}, TYPE__SemanticWeb.OPERATOR);

		insertIntoTokenMap(new String[]{ "\"(\"", "\")\"", "\",\"", "\".\"", "\"[\"", "\"]\"", "\"^^\""}, TYPE__SemanticWeb.SEPARATOR);
		
		insertIntoTokenMap(new String[]{ "<BNODE_LABEL>"}, TYPE__SemanticWeb.BLANKNODE);
		
		insertIntoTokenMap(new String[]{ "\"?\""}, TYPE__SemanticWeb.VARIABLE);
				
		insertIntoTokenMap(new String[]{ "<INTEGER_10>", "<FLOATING_POINT>", "<DIGITS>"}, TYPE__SemanticWeb.NUMBER);
		
		insertIntoTokenMap(new String[]{ "<LANGTAG>", "\"@\""}, TYPE__SemanticWeb.LANGTAG);
		
		insertIntoTokenMap(new String[]{ "<QNAME_NS>", "<QNAME>"}, TYPE__SemanticWeb.QUALIFIEDURI);
		
		insertIntoTokenMap(new String[]{ "<Q_URIref>"}, TYPE__SemanticWeb.URI);

		insertIntoTokenMap(new String[]{ "<STRING_LITERAL1>", "<STRING_LITERAL2>", "<STRING_LITERALLONG1>", "<STRING_LITERALLONG2>"}, TYPE__SemanticWeb.LITERAL);

		insertIntoTokenMap(new String[]{ "<EOF>", "\" \"", "\"\\t\"", "\"\\n\"", "\"\\r\"", "\"\\f\"", "<WS>"}, TYPE__SemanticWeb.WHITESPACE);
		
		insertIntoTokenMap(new String[]{ "<token of kind 6>", "\"(*\"", "\"*)\""}, TYPE__SemanticWeb.COMMENT);
		
		insertIntoTokenMap(new String[]{ "<NCNAME>", "<A2ZN>", "<A2Z>", "<NCCHAR1>", "<NCCHAR_END>", "<NCCHAR_END_WOUT>", "<NCCHAR_FULL>", "<NCNAME1>", "<NCNAME2>", "<NCNAME_PREFIX>"}, TYPE__SemanticWeb.IDENTIFIER);
		
		checkTopicMap();
	}
	
	protected static void insertIntoTokenMap(final String[] imagesToSet, final TYPE__SemanticWeb type){
		JAVACCParser.insertIntoTokenMap(RIFParserConstants.tokenImage, RIFParser.TOKEN_MAP, imagesToSet, type);
	}

	protected static void checkTopicMap(){
		JAVACCParser.checkTopicMap(RIFParserConstants.tokenImage, RIFParser.TOKEN_MAP);
	}
	
	public static class RIFToken implements TOKEN {

		private final String image;
		private final int kind;
		
		public RIFToken(final Token riftoken){
			this.image = riftoken.image; 
			this.kind = riftoken.kind;
		}
		
		public RIFToken(final String image, final int kind){
			this.image = image;
			this.kind = kind;
		}
		
		@Override
		public int getKind() {
			return this.kind;
		}

		@Override
		public String getImage() {
			return this.image;
		}		
	}
}
