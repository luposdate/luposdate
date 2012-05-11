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
