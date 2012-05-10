package lupos.gui.anotherSyntaxHighlighting;

import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE_ENUM;

/**
 * An interface for the Token
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
	 * Determine if this token is a comment. Sometimes comments should be
	 * ignored (compiling code) other times they should be used (syntax
	 * highlighting). This provides a method to check in case you feel you
	 * should ignore comments.
	 * 
	 * @return true if this token represents a comment.
	 */
//	public abstract boolean isComment();

	/**
	 * Determine if this token is whitespace. Sometimes whitespace should be
	 * ignored (compiling code) other times they should be used (code
	 * beautification). This provides a method to check in case you feel you
	 * should ignore whitespace.
	 * 
	 * @return true if this token represents whitespace.
	 */
//	public abstract boolean isWhiteSpace();

	/**
	 * Determine if this token is an error. Lets face it, not all code conforms
	 * to spec. The lexer might know about an error if a string literal is not
	 * closed, for example.
	 * 
	 * @return true if this token is an error.
	 */
//	public abstract boolean isError();


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
	
	public ILuposToken create(TYPE_ENUM description, String contents, int beginChar); 
	
}
