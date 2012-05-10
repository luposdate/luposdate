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
	 * Checks this token to see if it is a reserved word. Reserved words are
	 * explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
	 * Language Specification</A>.
	 * 
	 * @return true if this token is a reserved word, false otherwise
	 */
//	public boolean isReservedWord() {
//		return ((this.ID >> 8) == 0x1);
//	}

	/**
	 * Checks this token to see if it is an identifier. Identifiers are
	 * explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java
	 * Language Specification</A>.
	 * 
	 * @return true if this token is an identifier, false otherwise
	 */
//	public boolean isIdentifier() {
//		return ((this.ID >> 8) == 0x2);
//	}

	/**
	 * Checks this token to see if it is a literal. Literals are explained in <A
	 * Href=http://java.sun.com/docs/books/jls/html/>Java Language
	 * Specification</A>.
	 * 
	 * @return true if this token is a literal, false otherwise
	 */
//	public boolean isLiteral() {
//		return ((this.ID >> 8) == 0x3);
//	}

	/**
	 * Checks this token to see if it is a Separator. Separators are explained
	 * in <A Href=http://java.sun.com/docs/books/jls/html/>Java Language
	 * Specification</A>.
	 * 
	 * @return true if this token is a Separator, false otherwise
	 */
//	public boolean isSeparator() {
//		return ((this.ID >> 8) == 0x4);
//	}

	/**
	 * Checks this token to see if it is a Operator. Operators are explained in
	 * <A Href=http://java.sun.com/docs/books/jls/html/>Java Language
	 * Specification</A>.
	 * 
	 * @return true if this token is a Operator, false otherwise
	 */
//	public boolean isOperator() {
//		return ((this.ID >> 8) == 0x5);
//	}

	/**
	 * Checks this token to see if it is a comment.
	 * 
	 * @return true if this token is a comment, false otherwise
	 */
//	@Override
//	public boolean isComment() {
//		return ((this.ID >> 8) == 0xD);
//	}

	/**
	 * Checks this token to see if it is White Space. Usually tabs, line breaks,
	 * form feed, spaces, etc.
	 * 
	 * @return true if this token is White Space, false otherwise
	 */
//	@Override
//	public boolean isWhiteSpace() {
//		return ((this.ID >> 8) == 0xE);
//	}

	/**
	 * Checks this token to see if it is an Error. Unfinished comments, numbers
	 * that are too big, unclosed strings, etc.
	 * 
	 * @return true if this token is an Error, false otherwise
	 */
//	@Override
//	public boolean isError() {
//		return ((this.ID >> 8) == 0xF);
//	}
//
//	public boolean isUri() {
//		return ((this.ID >> 8) == 0x7);
//	}
//
//	public boolean isQualifieduri() {
//		return ((this.ID >> 8) == 0x6);
//	}
//
//	public boolean isVariable() {
//		return ((this.ID >> 8) == 0x8);
//	}
//
//	public boolean isLangtag() {
//		return ((this.ID >> 8) == 0x9);
//	}
//
//	public boolean isBlanknode() {
//		return ((this.ID >> 8) == 0xA);
//	}
//
//	public boolean isNumber() {
//		return ((this.ID >> 8) == 0xB);
//	}
//
//	public boolean isBoolean() {
//		return ((this.ID >> 8) == 0xC);
//	}

	/**
	 * matrix which contains the different colorability 
	 */
//	private final static LANGUAGE.TYPE__SPARQL_N3[] matrix = {
//			LANGUAGE.TYPE__SPARQL_N3.ERROR, // 0
//			LANGUAGE.TYPE__SPARQL_N3.RESERVEDWORD, // 1
//			LANGUAGE.TYPE__SPARQL_N3.IDENTIFIER, // 2
//			LANGUAGE.TYPE__SPARQL_N3.LITERAL, // 3
//			LANGUAGE.TYPE__SPARQL_N3.SEPARATOR, // 4
//			LANGUAGE.TYPE__SPARQL_N3.OPERATOR, // 5
//			LANGUAGE.TYPE__SPARQL_N3.QUALIFIEDURI, // 6
//			LANGUAGE.TYPE__SPARQL_N3.URI, // 7
//			LANGUAGE.TYPE__SPARQL_N3.VARIABLE, // 8
//			LANGUAGE.TYPE__SPARQL_N3.LANGTAG, // 9
//			LANGUAGE.TYPE__SPARQL_N3.BLANKNODE, // 10
//			LANGUAGE.TYPE__SPARQL_N3.NUMBER, // 11
//			LANGUAGE.TYPE__SPARQL_N3.BOOLEAN, // 12
//			LANGUAGE.TYPE__SPARQL_N3.COMMENT, // 13
//			LANGUAGE.TYPE__SPARQL_N3.WHITESPACE, // 14
//			LANGUAGE.TYPE__SPARQL_N3.ERROR // 15
//	};

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
