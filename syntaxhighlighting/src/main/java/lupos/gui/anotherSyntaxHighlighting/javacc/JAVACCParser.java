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
import java.util.Arrays;

import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.ILuposToken;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE_ENUM;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;

/**
 * SPARQL Parser Implements ILuposParser
 * 
 *
 */
public class JAVACCParser implements ILuposParser {

	private final PARSER parser;
	private LuposDocumentReader stream;
	private boolean eof = false;
	
	/**
	 * token information
	 */
	private ILuposToken currentToken = null;
	private final TYPE_ENUM[] TOKEN_MAP;

	/**
	 * Offset information
	 */
	private int beginCharOffset = 0;
	private int endCharOffset = 0;


	/**
	 * Constructor which is initializes the input Stream
	 * @param s inputstream to set
	 */
	public JAVACCParser(LuposDocumentReader s, final PARSER parser) {
		this.stream = s;
		this.parser = parser;
		this.TOKEN_MAP = parser.getTokenMap();
	}

	
	protected ILuposToken testOfComment(final String content, final int beginChar, final int endChar){
		int beginCharVar = beginChar;
		// jump over leading returns
		while(beginCharVar<endChar && content.charAt(beginCharVar)=='\n'){
			beginCharVar++;
		}
		for(int i=beginCharVar; i<endChar && !this.parser.endOfSearchOfComment(content, i); i++){
			if(this.parser.isStartOfComment(content, i)){
				return this.parser.handleComment(content, i);
			}
		}
		return null;
	}

	@Override
	public ILuposToken getNextToken(final String content) {
		
		if (this.parser == null) {
			System.err.println("No Parser set.");
			return null;
		}

		// end of file reached
		if (this.eof == true)
			return null;

		TOKEN sparql1_1token = null;
		ILuposToken token = null;

		int beginChar = (this.currentToken!=null)? 
				this.currentToken.getBeginChar() + this.currentToken.getContents().length() - 1 : 
				this.beginCharOffset - 1;
		
		// catch thrown error if unknown token exists
		try {
			// Get the token from the SPARQL 1.1 parser
			sparql1_1token = this.parser.getNextToken();
						
			//sparql 1.1 parser return eof flag
			if (sparql1_1token.getKind() == 0) {
				token = testOfComment(content, beginChar + 1, content.length());
				if(token!=null){
					this.currentToken = token;
					return token;
				}
				setEOF(true);
				return null;
			}
			
			final int startOfBeginChar = beginChar + 1;
			
			// Generate a LuposToken by using the given SPARQL 1.1 token
			final String image = sparql1_1token.getImage();
			final int imageLength = image.length();
			char firstChar = image.charAt(0);
			// determine real start of the token...
			do {
				beginChar++;
				while(beginChar<content.length() && content.charAt(beginChar)!=firstChar){
					beginChar++;
				}
			} while((beginChar + imageLength < content.length()) && content.substring(beginChar, beginChar + imageLength).compareTo(image)!=0);
			
			token = testOfComment(content, startOfBeginChar, beginChar);
			
			if(token == null){
				token = this.parser.create(this.TOKEN_MAP[sparql1_1token.getKind()], image, beginChar);
			} else {
				setReader(this.stream, token.getBeginChar() + token.getContents().length(), this.endCharOffset);
			}
			
		} catch (Error e) {
			token = errorHandling(beginChar, content);
		}

		// set the current token to the received one.
		this.currentToken = token;

		return token;
	}
	
	private ILuposToken errorHandling(final int beginCharParameter, final String content){
		int beginChar=beginCharParameter+1;
		ILuposToken token = testOfComment(content, beginChar, content.length());
		if(token!=null){
			setReader(this.stream, token.getBeginChar() + token.getContents().length(), this.endCharOffset);
		} else {
			int index = 0;
			boolean flag = false;
			do{
				try{
					index++;
					setReader(this.stream, beginChar+index, this.endCharOffset);
				} catch(Error e){
					flag = true;
				}
			} while(flag && beginChar+index<this.endCharOffset);				
			token = this.parser.createErrorToken(content.substring(beginChar, beginChar+index), beginChar);							
		}
		return token;
	}

	@Override
	public void setReaderTokenFriendly(LuposDocumentReader stream, int beginChar, int endChar) {
		
		if (beginChar == JAVACCParser.STREAM_FIRST){
			beginChar = 0;
		}

		String content = this.getTextFromStream(stream);
		
		if (endChar == JAVACCParser.STREAM_END || endChar > content.length()){
			endChar = content.length();
		}

		setReader(stream, beginChar, endChar);
	}

	
	/**
	 * get the text from the stream
	 * @param stream Where to get the text from
	 * @return the text on the stream
	 */
	private String getTextFromStream(LuposDocumentReader stream) {
		return stream.getText();
	}

	@Override
	public void setReader(LuposDocumentReader stream, int beginChar, int endChar) {

		// if (beginChar == STREAM_FIRST && endChar == STREAM_END)
		// this.setReader(stream);

		//catch Stream first and stream end
		if (beginChar == JAVACCParser.STREAM_FIRST)
			beginChar = 0;

		if (endChar == JAVACCParser.STREAM_END)
			endChar = getTextFromStream(stream).length();


		//receive offset stream from the stream
		InputStream offsetStream = stream.getStreamWithOffset(beginChar, endChar);
		
		//create a new parser if none exists
		this.parser.ReInit(offsetStream); //reinvokes the parser
		
		this.stream = stream;
		this.setEOF(false);

		this.currentToken = null;
		this.beginCharOffset = beginChar;
		this.endCharOffset = endChar;
		
//		System.out.println("parse from "+beginChar+" to "+endChar);
	}

	@Override
	public void setReader(LuposDocumentReader stream) {
		this.parser.ReInit(stream);

		this.stream = stream;
		this.setEOF(false);

		this.currentToken = null;
		this.beginCharOffset = 0;
	}

	@Override
	public LuposDocumentReader getReader() {
		return this.stream;
	}

	

	/**
	 * Sets the end of file flag to the given value.
	 * 
	 * @param eof
	 *            True if end of file is reached.
	 */
	private void setEOF(boolean eof) {
		this.eof = eof;
	}

	/**
	 * Tells whether the end of file is reached within parsing. This means the
	 * end of the given inputStream was reached.
	 * 
	 * @return Returns true if the end of file is reached within parsing.
	 */
	@Override
	public boolean isEOF() {
		return this.eof;
	}
		
	protected static void checkTopicMap(final String[] images, final TYPE_ENUM[] tokenMap){
		int index = 0;
		for(TYPE_ENUM type: tokenMap){
			if(type == null){
				System.err.println("WARNING: Token map not set for image " + images[index]);
			}
			index++;
		}
	}
	
	protected static void insertIntoTokenMap(final String[] images, final TYPE_ENUM[] tokenMap, final String[] imagesToSet, final TYPE_ENUM type){
		for(String currentImage: imagesToSet){
			boolean flag = false;
			for(int i=0; i<images.length; i++){
				if(images[i].compareTo(currentImage)==0){
					if(tokenMap[i] != null){
						System.err.println("WARNING: map of tokens has been already set for " + currentImage + " to " + tokenMap[i] + ", but now it is set to "+type);
					}
					tokenMap[i] = type;
					flag=true;
					break;
				}
			}
			if(!flag){
				System.err.println("WARNING: cannot find image "+currentImage+" in "+Arrays.toString(images));
			}
		}
	}
	
	public static interface TOKEN {
		public int getKind();
		public String getImage();
	}
	
	public static interface PARSER {
		public TOKEN getNextToken();
		public TYPE_ENUM[] getTokenMap();
		public void ReInit(Reader reader);
		public void ReInit(InputStream inputstream);
		public boolean isStartOfComment(final String content, final int beginChar);
		public ILuposToken handleComment(final String content, final int beginChar);
		public boolean endOfSearchOfComment(final String content, final int beginChar);
		public ILuposToken create(TYPE_ENUM description, String contents, int beginChar);
		public ILuposToken createErrorToken(String contents, int beginChar);
	}
}
