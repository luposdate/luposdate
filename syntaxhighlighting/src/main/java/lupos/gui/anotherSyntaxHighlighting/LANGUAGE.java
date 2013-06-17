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
package lupos.gui.anotherSyntaxHighlighting;


import java.awt.Color;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;

import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import lupos.gui.anotherSyntaxHighlighting.javacc.SemanticWebToken;
import xpref.datatypes.BooleanDatatype;
import xpref.datatypes.ColorDatatype;
import xpref.datatypes.FontDatatype;
import xpref.datatypes.IntegerDatatype;

/**
 * Enumeration with all colors for different highlighting.
 *
 */
public enum LANGUAGE {
	SEMANTIC_WEB() {
		@Override
		public TYPE_ENUM[] getValues() {
			return TYPE__SemanticWeb.values();
		}

		@Override
		public void setStyles() {
			initStylesArray(this);

			for(final TYPE__SemanticWeb t : TYPE__SemanticWeb.values()) {
				try {
					final String typeName = t.toString().toLowerCase();

					final int alpha = IntegerDatatype.getValues("syntaxHighlighting_" + typeName + ".backgroundTransparency").get(0).intValue();
					final Color tmp = ColorDatatype.getValues("syntaxHighlighting_" + typeName + ".backgroundColor").get(0);
					final Color background = new Color(tmp.getRed(), tmp.getGreen(), tmp.getBlue(), alpha);
					final Color foreground = ColorDatatype.getValues("syntaxHighlighting_" + typeName + ".foregroundColor").get(0);

					final boolean bold = BooleanDatatype.getValues("syntaxHighlighting_" + typeName + ".bold").get(0).booleanValue();
					final boolean italic = BooleanDatatype.getValues("syntaxHighlighting_" + typeName + ".italic").get(0).booleanValue();

					addStyle(t, background, foreground, bold, italic);
				}
				catch(final Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}

		@Override
		public void setDefaultStyles(final Font tfFont) {
			initStylesArray(this);

			final Color maroon = new Color(0xB03060);
			final Color darkBlue = new Color(0x000080);
			final Color darkGreen = Color.GREEN.darker();
			final Color darkerGreen = darkGreen.darker();
			final Color darkYellow = Color.YELLOW.darker().darker().darker();
			final Color brightBlue = Color.BLUE.brighter();
			final Color brighterBlue = new Color(0x8080FF);
			final Color darkPurple = new Color(0xA020F0).darker();
			final Color darkRed = Color.MAGENTA.darker().darker();
			final Color bg = new Color(0, 0, 0, 0);

			addStyle(TYPE__SemanticWeb.RESERVEDWORD, bg, darkRed, true, false, tfFont);
			addStyle(TYPE__SemanticWeb.IDENTIFIER, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.LITERAL, bg, brightBlue, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.INTEGER, bg, darkBlue, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.DECIMAL, bg, darkBlue.darker(), false, false, tfFont);
			addStyle(TYPE__SemanticWeb.SEPARATOR, bg, maroon, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.OPERATOR, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.COMMENT, bg, Color.DARK_GRAY, false, true, tfFont);
			addStyle(TYPE__SemanticWeb.WHITESPACE, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.ERROR, bg, Color.RED, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.VARIABLE, bg, darkPurple, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.URI, bg, darkGreen, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.QUALIFIEDURI, bg, darkerGreen, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.PREFIXLABEL, bg, darkerGreen.darker(), false, false, tfFont);
			addStyle(TYPE__SemanticWeb.LANGTAG, bg, brighterBlue, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.BLANKNODE, bg, darkYellow, false, false, tfFont);
			addStyle(TYPE__SemanticWeb.BOOLEAN, bg, maroon, true, true, tfFont);
		}
	},
	HTML() {
		@Override
		public TYPE_ENUM[] getValues() {
			return TYPE__HTML.values();
		}

		@Override
		public void setStyles() {
			initStylesArray(this);
		}

		@Override
		public void setDefaultStyles(final Font tfFont) {
			initStylesArray(this);

			final Color maroon = new Color(0xB03060);
			final Color darkGreen = Color.GREEN.darker();
			final Color darkerGreen = darkGreen.darker();
			final Color brightBlue = Color.BLUE.brighter();
			final Color darkRed = Color.MAGENTA.darker().darker();
			final Color bg = new Color(0, 0, 0, 0);

			addStyle(TYPE__HTML.ERROR, bg, Color.RED, false, false, tfFont);
			addStyle(TYPE__HTML.SEPARATOR, bg, maroon, false, false, tfFont);
			addStyle(TYPE__HTML.TEXT, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__HTML.TAG, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__HTML.NAME, bg, darkRed, true, false, tfFont);
			addStyle(TYPE__HTML.VALUE, bg, brightBlue, false, false, tfFont);
			addStyle(TYPE__HTML.REFERENCE, bg, darkGreen, false, false, tfFont);
			addStyle(TYPE__HTML.PREPROCESSOR, bg, maroon, true, true, tfFont);
			addStyle(TYPE__HTML.COMMENT, bg, Color.DARK_GRAY, false, true, tfFont);
			addStyle(TYPE__HTML.WHITESPACE, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__HTML.ENDTAG, bg, darkerGreen, false, false, tfFont);
		}
	},
	JAVA() {
		@Override
		public TYPE_ENUM[] getValues() {
			return TYPE__JAVA.values();
		}

		@Override
		public void setStyles() {
			initStylesArray(this);
		}

		@Override
		public void setDefaultStyles(final Font tfFont) {
			initStylesArray(this);

			final Color darkRed = Color.MAGENTA.darker().darker();
			final Color bg = new Color(0, 0, 0, 0);

			addStyle(TYPE__JAVA.ERROR, bg, Color.RED, false, false, tfFont);
			addStyle(TYPE__JAVA.SEPARATOR, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__JAVA.IDENTIFIER, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__JAVA.RESERVEDWORD, bg, darkRed, true, false, tfFont);
			addStyle(TYPE__JAVA.LITERAL, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__JAVA.COMMENT, bg, Color.DARK_GRAY, false, true, tfFont);
			addStyle(TYPE__JAVA.WHITESPACE, bg, Color.BLACK, false, false, tfFont);
			addStyle(TYPE__JAVA.OPERATOR, bg, Color.BLACK, false, false, tfFont);
		}
	};
	public abstract TYPE_ENUM[] getValues();

	public abstract void setStyles();

	public abstract void setDefaultStyles(Font tfFont);

	public void setBlankStyles() {
		initStylesArray(this);
		final Color foreGround = UIManager.getColor("TextPane.foreground");

		final Font tfFont = getTextFieldFont();

		final boolean bold = tfFont.isBold();
		final boolean italic = tfFont.isItalic();

		for(final TYPE_ENUM t : this.getValues()) {
			addStyle(t, new Color(0, 0, 0, 0), foreGround, bold, italic);
		}
	}

	private static void initStylesArray(final LANGUAGE l) {
		styles[l.ordinal()] = new SimpleAttributeSet[l.getValues().length];
	}

	private static void addStyle(final TYPE_ENUM type, final Color bg, final Color fg, final boolean bold, final boolean italic, final Font tfFont) {
		final SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setFontFamily(style, tfFont.getFamily());
		StyleConstants.setFontSize(style, tfFont.getSize());
		StyleConstants.setBackground(style, bg);
		StyleConstants.setForeground(style, fg);
		StyleConstants.setBold(style, bold);
		StyleConstants.setItalic(style, italic);

		styles[type.getLanguage().ordinal()][type.ordinal()] = style;
	}

	private static void addStyle(final TYPE_ENUM type, final Color bg, final Color fg, final boolean bold, final boolean italic) {
		addStyle(type, bg, fg, bold, italic, getTextFieldFont());
	}

	/**
	 * An array containing the text styles. The first dimension is for
	 * distinguishing different languages...
	 */
	private static SimpleAttributeSet[][] styles = new SimpleAttributeSet[LANGUAGE.values().length][];

	public static AttributeSet getAttributeSet(final TYPE_ENUM type) {
		return styles[type.getLanguage().ordinal()][type.ordinal()];
	}

	public static interface TYPE_ENUM {
		public LANGUAGE getLanguage();

		public int ordinal();

		public TYPE_ENUM getErrorEnum();

		public boolean isError();

		public boolean errorWhenDirectlyFollowingToken(TYPE_ENUM type);

		// to allow to combine two tokens to one (if used scanner is not perfect)
		public TYPE_ENUM combineWith(String thisImage);

		// to allow some kind of context-sensitive syntax highlighting
		public Set<ILuposToken> expectedNextTokens();
	}

	public static enum TYPE__SemanticWeb implements TYPE_ENUM {
		ERROR, RESERVEDWORD, IDENTIFIER, LITERAL, INTEGER, DECIMAL, SEPARATOR, OPERATOR, COMMENT, WHITESPACE,  VARIABLE, URI, QUALIFIEDURI, PREFIXLABEL, LANGTAG, BLANKNODE, BOOLEAN;

		@Override
		public LANGUAGE getLanguage() {
			return LANGUAGE.SEMANTIC_WEB;
		}

		@Override
		public TYPE__SemanticWeb getErrorEnum(){
			return ERROR;
		}

		@Override
		public boolean isError() {
			return this == ERROR;
		}

		public boolean isSeperating(){
			return this == SEPARATOR || this == OPERATOR || this == LITERAL;
		}

		public boolean isNonSeparating(){
			return this == RESERVEDWORD || this == IDENTIFIER || this == INTEGER || this == DECIMAL || this == VARIABLE || this == QUALIFIEDURI || this == PREFIXLABEL || this == BLANKNODE || this == BOOLEAN;
		}

		@Override
		public boolean errorWhenDirectlyFollowingToken(final TYPE_ENUM type) {
			if(this.isError() || type.isError()){
				return true;
			}
			if(this == IDENTIFIER){
				// only the RIFParser uses IDENTIFIER
				// these cases maybe could be fixed in another better way!
				if(type == VARIABLE){
					// Because the RIFParser parses ?var as <QUESTION> <NCNAME> instead of one <varname> or similar (should be changed in the future!)
					return false;
				}
			}
			if(type instanceof TYPE__SemanticWeb){
				final TYPE__SemanticWeb typeSW = (TYPE__SemanticWeb) type;
				if(this.isSeperating() && typeSW.isNonSeparating() || this.isNonSeparating() && typeSW.isSeperating()){
					return false;
				} else if(this.isNonSeparating() && typeSW.isNonSeparating()){
					return true;
				}
				if(this.isError() && typeSW.isNonSeparating() || type.isError() && this.isNonSeparating()){
					return true;
				}
			}
			return false;
		}

		@Override
		public TYPE_ENUM combineWith(final String thisImage) {
			// Because the RIFParser parses ?var as <QUESTION> <NCNAME> instead of one <varname> or similar (should be changed in the future!)
			if(this == VARIABLE && thisImage.compareTo("?")==0){
				return IDENTIFIER;
			} else {
				return null;
			}
		}

		@Override
		public Set<ILuposToken> expectedNextTokens() {
			if(this == IDENTIFIER){
				final HashSet<ILuposToken> result = new HashSet<ILuposToken>();
				result.add(new SemanticWebToken(OPERATOR, "->", 0));
				result.add(new SemanticWebToken(URI, "", 0));
				return result;
			}
			return null;
		}
	}

	public static enum TYPE__HTML implements TYPE_ENUM {
		ERROR, SEPARATOR, TEXT, TAG, NAME, VALUE, REFERENCE, PREPROCESSOR, COMMENT, WHITESPACE, ENDTAG;

		@Override
		public LANGUAGE getLanguage() {
			return LANGUAGE.HTML;
		}

		@Override
		public TYPE__HTML getErrorEnum(){
			return ERROR;
		}

		@Override
		public boolean isError() {
			return this == ERROR;
		}

		@Override
		public boolean errorWhenDirectlyFollowingToken(final TYPE_ENUM type) {
			if(this.isError() && type.isError()){
				return true;
			} else {
				return false;
			}
		}

		@Override
		public TYPE_ENUM combineWith(final String thisImage) {
			return null;
		}

		@Override
		public Set<ILuposToken> expectedNextTokens() {
			return null;
		}
	}

	public static enum TYPE__JAVA implements TYPE_ENUM {
		ERROR, WHITESPACE, COMMENT, OPERATOR, SEPARATOR, LITERAL, IDENTIFIER, RESERVEDWORD;

		@Override
		public LANGUAGE getLanguage() {
			return LANGUAGE.JAVA;
		}

		@Override
		public TYPE__JAVA getErrorEnum(){
			return ERROR;
		}

		@Override
		public boolean isError() {
			return this == ERROR;
		}

		@Override
		public boolean errorWhenDirectlyFollowingToken(final TYPE_ENUM type) {
			if(this.equals(RESERVEDWORD) && type.equals(RESERVEDWORD)){
				return true;
			}
			if(this.isError() && type.isError()){
				return true;
			}
			if(this.isError() && type.equals(RESERVEDWORD) || type.isError() && this.equals(RESERVEDWORD)){
				return true;
			}
			return false;
		}

		@Override
		public TYPE_ENUM combineWith(final String thisImage) {
			return null;
		}

		@Override
		public Set<ILuposToken> expectedNextTokens() {
			return null;
		}
	}

	private static Font getTextFieldFont() {
		try {
			if(BooleanDatatype.getValues("textFieldFont.fontEnable").get(0).booleanValue()) {
				return FontDatatype.getValues("textFieldFont.font").get(0);
			}
		}
		catch(final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return UIManager.getFont("TextPane.font");
	}
}