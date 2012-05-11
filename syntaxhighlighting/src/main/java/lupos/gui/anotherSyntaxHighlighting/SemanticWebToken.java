package lupos.gui.anotherSyntaxHighlighting;

import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE_ENUM;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE__SemanticWeb;


/**
 * {@link SemanticWebToken}
 *
 */
public class SemanticWebToken implements ILuposToken {
	
	private final int ID;
	private final String contents;

	private final int beginChar;
	private final int endChar;
	
	/**
	 * Create a new token. The constructor is typically called by the parser.
	 * 
	 * @param ID
	 *            the id number of the token
	 * @param contents
	 *            A string representing the text of the token
	 * @param beginColumn
	 *            the line number of the input on which this token started
	 * @param beginColumn
	 *            the offset into the input in characters at which this token
	 *            started
	 * @param endColumn
	 *            the offset into the input in characters at which this token
	 *            ended
	 */
	public SemanticWebToken(int ID, String contents, int beginChar, int endChar) {
		this.ID = ID;
		this.contents = new String(contents);
		this.beginChar = beginChar;
		this.endChar = endChar;
	}


	public SemanticWebToken(TYPE__SemanticWeb description, String contents, int beginChar) {
		this(description.ordinal(), contents, beginChar, beginChar + contents.length());
	}

	/**
	 * A description of this token. The description should be appropriate for
	 * syntax highlighting. For example "comment" is returned for a comment.
	 * 
	 * @return a description of this token.
	 */
	@Override
	public TYPE__SemanticWeb getDescription() {
		return TYPE__SemanticWeb.values()[this.ID];
	}

	/**
	 * get a String that explains the error, if this token is an error.
	 * 
	 * @return a String that explains the error, if this token is an error, null
	 *         otherwise.
	 */
	@Override
	public String errorString() {
		String s;
		if (this.getDescription().isError()) {
			s = "Error from " + this.beginChar + " to " + this.endChar + ": ";
		} else {
			s = null;
		}
		return (s);
	}

	/**
	 * get a representation of this token as a human readable string. The format
	 * of this string is subject to change and should only be used for debugging
	 * purposes.
	 * 
	 * @return a string representation of this token
	 */
	@Override
	public String toString() {
		return ("Token #" + Integer.toHexString(this.ID) + ": " + this.getDescription() + " Position from " + this.beginChar + " to " + this.endChar + " : " + this.contents);
	}

	/**
	 * @return the iD
	 */
	public int getID() {
		return ID;
	}

	/**
	 * @return the contents
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * @return the beginChar
	 */
	public int getBeginChar() {
		return beginChar;
	}

	/**
	 * @return the endChar
	 */
	public int getEndChar() {
		return endChar;
	}


	@Override
	public ILuposToken create(TYPE_ENUM description, String contents, int beginChar) {
		return new SemanticWebToken((TYPE__SemanticWeb)description, contents, beginChar);
	}
}
