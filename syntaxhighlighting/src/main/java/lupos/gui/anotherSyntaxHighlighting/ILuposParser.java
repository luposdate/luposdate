package lupos.gui.anotherSyntaxHighlighting;

public interface ILuposParser {

	public final int STREAM_END = -1;
	public final int STREAM_FIRST = -1;
	
	/**
	 * Returns the next token from stream. Returns null if the end of stream is reached.
	 * 
	 * @return Returns the next Token by iteration from the given Stream.
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
	 * @return
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
