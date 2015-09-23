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
package lupos.datastructures.items.literal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.codemap.CodeMapURILiteral;
import lupos.datastructures.items.literal.string.PlainStringLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.io.LuposObjectInputStream;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;
public class LiteralFactory {

	/**
	 * setting semanticInterpretationOfLiterals to true leads to
	 * handle e.g. +4 like 3 or "hello"@en like "hello"@EN, i.e.
	 * some values are interpreted according to their datatypes.
	 * However, some test cases of W3C state that this is not
	 * 100% according to their specification!
	 */
	public static boolean semanticInterpretationOfLiterals = false;

	/**
	 * <p>writeLuposLiteral.</p>
	 *
	 * @param lit a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param out a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public static void writeLuposLiteral(final Literal lit, final OutputStream out) throws IOException {
		if (lit instanceof CodeMapURILiteral) {
			OutHelper.writeLuposByte((byte) LuposObjectInputStream.URILITERAL, out);
			writeContentOfLiteral(((CodeMapURILiteral) lit).getContent(), out);
			OutHelper.writeLuposInt(((CodeMapURILiteral) lit).getPrefixCode(), out);
		} else if (lit instanceof StringURILiteral) {
			OutHelper.writeLuposByte((byte) LuposObjectInputStream.URILITERAL, out);
			OutHelper.writeLuposString(((StringURILiteral) lit).getString(), out);
		} else if (lit instanceof TypedLiteral) {
			OutHelper.writeLuposByte((byte) LuposObjectInputStream.TYPEDLITERAL, out);
			if (lit instanceof TypedLiteralOriginalContent) {
				writeContentOfLiteral(((TypedLiteralOriginalContent) lit).originalContent, out);
			} else {
				writeContentOfLiteral(((TypedLiteral) lit).content, out);
			}
			writeLuposLiteral(((TypedLiteral) lit).type, out);
		} else if (lit instanceof LanguageTaggedLiteral) {
			OutHelper.writeLuposByte((byte) LuposObjectInputStream.LANGUAGETAGGEDLITERAL, out);
			writeContentOfLiteral(((LanguageTaggedLiteral) lit).content, out);
			if (lit instanceof LanguageTaggedLiteralOriginalLanguage) {
				writeContentOfLiteral(((LanguageTaggedLiteralOriginalLanguage) lit).originalLang, out);
			} else {
				writeContentOfLiteral(((LanguageTaggedLiteral) lit).lang, out);
			}
		} else if (lit instanceof AnonymousLiteral) {
			OutHelper.writeLuposByte((byte) LuposObjectInputStream.ANONYMOUSLITERAL, out);
			writeContentOfLiteral(((AnonymousLiteral) lit).content, out);
		} else if (lit instanceof LazyLiteralOriginalContent) {
			if (((LazyLiteralOriginalContent) lit).isMaterialized()) {
				OutHelper.writeLuposByte((byte) LuposObjectInputStream.LAZYLITERALORIGINALCONTENTMATERIALIZED, out);
			} else {
				OutHelper.writeLuposByte((byte) LuposObjectInputStream.LAZYLITERALORIGINALCONTENT, out);
			}
			OutHelper.writeLuposInt(((LazyLiteral) lit).getCode(), out);
			OutHelper.writeLuposInt(((LazyLiteralOriginalContent) lit).getCodeOriginalContent(), out);
			if (((LazyLiteralOriginalContent) lit).isMaterialized()) {
				writeLuposLiteral(((LazyLiteralOriginalContent) lit).getLiteral(), out);
			}
		} else if (lit instanceof LazyLiteral) {
			if (((LazyLiteral) lit).isMaterialized()) {
				OutHelper.writeLuposByte((byte) LuposObjectInputStream.LAZYLITERALMATERIALIZED, out);
			} else {
				OutHelper.writeLuposByte((byte) LuposObjectInputStream.LAZYLITERAL, out);
			}
			OutHelper.writeLuposInt(((LazyLiteral) lit).getCode(), out);
			if (((LazyLiteral) lit).isMaterialized()) {
				writeLuposLiteral(((LazyLiteral) lit).getLiteral(), out);
			}
		} else if (lit.getClass() == PlainStringLiteral.class) {
			OutHelper.writeLuposByte((byte) LuposObjectInputStream.PLAINSTRINGLITERAL, out);
			OutHelper.writeLuposString(lit.toString(), out);
		} else {
			// "normal" Literal object!
			OutHelper.writeLuposByte((byte) LuposObjectInputStream.LITERAL, out);
			writeContentOfLiteral(lit, out);
		}
	}

	/**
	 * <p>writeContentOfLiteral.</p>
	 *
	 * @param lit a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param out a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	protected static void writeContentOfLiteral(final Literal lit, final OutputStream out) throws IOException {
		if (lit instanceof StringLiteral) {
			OutHelper.writeLuposString(lit.toString(), out);
		} else {
			OutHelper.writeLuposInt(((CodeMapLiteral) lit).getCode(), out);
		}
	}

	/**
	 * <p>lengthLuposLiteral.</p>
	 *
	 * @param lit a {@link lupos.datastructures.items.literal.Literal} object.
	 * @return a int.
	 */
	public static int lengthLuposLiteral(final Literal lit) {
		if (lit instanceof CodeMapURILiteral) {
			return 	LengthHelper.lengthLuposByte() +
					lengthContentOfLiteral(((CodeMapURILiteral) lit).getContent()) +
					LengthHelper.lengthLuposInt(((CodeMapURILiteral) lit).getPrefixCode());
		} else if (lit instanceof StringURILiteral) {
			return 	LengthHelper.lengthLuposByte() +
					LengthHelper.lengthLuposString(((StringURILiteral) lit).getString());
		} else if (lit instanceof TypedLiteral) {
			int result = LengthHelper.lengthLuposByte();
			if (lit instanceof TypedLiteralOriginalContent) {
				result += lengthContentOfLiteral(((TypedLiteralOriginalContent) lit).originalContent);
			} else {
				result += lengthContentOfLiteral(((TypedLiteral) lit).content);
			}
			return result + lengthLuposLiteral(((TypedLiteral) lit).type);
		} else if (lit instanceof LanguageTaggedLiteral) {
			int result = LengthHelper.lengthLuposByte() + lengthContentOfLiteral(((LanguageTaggedLiteral) lit).content);
			if (lit instanceof LanguageTaggedLiteralOriginalLanguage) {
				result += lengthContentOfLiteral(((LanguageTaggedLiteralOriginalLanguage) lit).originalLang);
			} else {
				result += lengthContentOfLiteral(((LanguageTaggedLiteral) lit).lang);
			}
			return result;
		} else if (lit instanceof AnonymousLiteral) {
			return LengthHelper.lengthLuposByte() + lengthContentOfLiteral(((AnonymousLiteral) lit).content);
		} else if (lit instanceof LazyLiteralOriginalContent) {
			return	LengthHelper.lengthLuposByte() +
					LengthHelper.lengthLuposInt(((LazyLiteral) lit).getCode()) +
					LengthHelper.lengthLuposInt(((LazyLiteralOriginalContent) lit).getCodeOriginalContent()) +
					((((LazyLiteralOriginalContent) lit).isMaterialized())?
							lengthLuposLiteral(((LazyLiteralOriginalContent) lit).getLiteral())
							: 0);
		} else if (lit instanceof LazyLiteral) {
			return 	LengthHelper.lengthLuposByte() +
					LengthHelper.lengthLuposInt(((LazyLiteral) lit).getCode()) +
					((((LazyLiteral) lit).isMaterialized())?
							lengthLuposLiteral(((LazyLiteral) lit).getLiteral())
							:0);
		} else if (lit.getClass() == PlainStringLiteral.class) {
			return LengthHelper.lengthLuposByte()  +LengthHelper.lengthLuposString(lit.toString());
		} else {
			// "normal" Literal object!
			return LengthHelper.lengthLuposByte() + lengthContentOfLiteral(lit);
		}
	}

	/**
	 * <p>lengthContentOfLiteral.</p>
	 *
	 * @param lit a {@link lupos.datastructures.items.literal.Literal} object.
	 * @return a int.
	 */
	protected static int lengthContentOfLiteral(final Literal lit) {
		if (lit instanceof StringLiteral) {
			return LengthHelper.lengthLuposString(lit.toString());
		} else {
			return LengthHelper.lengthLuposInt(((CodeMapLiteral) lit).getCode());
		}
	}


	/**
	 * <p>readLuposLiteral.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java.io.IOException if any.
	 */
	public static Literal readLuposLiteral(final InputStream in)
			throws IOException {
		try {
			final int type = InputHelper.readLuposByte(in);
			switch (type) {
			case LuposObjectInputStream.URILITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
					return new StringURILiteral("<" + InputHelper.readLuposString(in)
							+ ">");
				} else if (mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.LAZYLITERAL) {
					return new CodeMapURILiteral(InputHelper.readLuposString(in), InputHelper.readLuposInt(in));
				} else {
					return new CodeMapURILiteral(InputHelper.readLuposInt(in), InputHelper.readLuposInt(in));
				}
			case LuposObjectInputStream.TYPEDLITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERAL
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
						|| mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.URICODEMAP) {
					final String content = InputHelper.readLuposString(in);
					return TypedLiteralOriginalContent.createTypedLiteral(
							content, (URILiteral) readLuposLiteral(in));
				} else {
					final int content = InputHelper.readLuposInt(in);
					return TypedLiteralOriginalContent.createTypedLiteral(
							content, (URILiteral) readLuposLiteral(in));
				}
			case LuposObjectInputStream.LANGUAGETAGGEDLITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERAL
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
						|| mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.URICODEMAP) {
					final String content = InputHelper.readLuposString(in);
					final String lang = InputHelper.readLuposString(in);
					return LanguageTaggedLiteralOriginalLanguage
							.createLanguageTaggedLiteral(content, lang);
				} else {
					final int content = InputHelper.readLuposInt(in);
					final int lang = InputHelper.readLuposInt(in);
					return LanguageTaggedLiteralOriginalLanguage
							.createLanguageTaggedLiteral(content, lang);
				}
			case LuposObjectInputStream.ANONYMOUSLITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERAL
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
						|| mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.URICODEMAP) {
					return new AnonymousLiteral(InputHelper.readLuposString(in));
				} else {
					return new AnonymousLiteral(InputHelper.readLuposInt(in));
				}
			case LuposObjectInputStream.LAZYLITERAL:
				return new LazyLiteral(InputHelper.readLuposInt(in));
			case LuposObjectInputStream.LAZYLITERALORIGINALCONTENT:
				return new LazyLiteralOriginalContent(InputHelper.readLuposInt(in), InputHelper.readLuposInt(in));
			case LuposObjectInputStream.LAZYLITERALMATERIALIZED:
				return new LazyLiteral(InputHelper.readLuposInt(in), readLuposLiteral(in));
			case LuposObjectInputStream.LAZYLITERALORIGINALCONTENTMATERIALIZED:
				return new LazyLiteralOriginalContent(InputHelper.readLuposInt(in), InputHelper.readLuposInt(in), readLuposLiteral(in));
			case LuposObjectInputStream.PLAINSTRINGLITERAL:
				return new PlainStringLiteral(InputHelper.readLuposString(in));
			default:
			case LuposObjectInputStream.LITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERAL
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
						|| mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.URICODEMAP) {
					return new StringLiteral(InputHelper.readLuposString(in));
				} else {

					return new CodeMapLiteral(InputHelper.readLuposInt(in));
				}
			}
		} catch (final URISyntaxException e) {
			throw new IOException(
					"Expected URI in InputStream, but it is not an URI!");
		}
	}

	/**
	 * <p>createURILiteralWithoutLazyLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	public static URILiteral createURILiteralWithoutLazyLiteral(
			final String content) throws java.net.URISyntaxException {
		if (mapType == MapType.NOCODEMAP
				|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
			return new StringURILiteral(content);
		} else {
			return new CodeMapURILiteral(content);
		}
	}

	/**
	 * <p>createURILiteralWithoutLazyLiteralWithoutException.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.URILiteral} object.
	 */
	public static URILiteral createURILiteralWithoutLazyLiteralWithoutException(
			final String content) {
		try {
			if (mapType == MapType.NOCODEMAP
					|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
				return new StringURILiteral(content);
			} else {
				return new CodeMapURILiteral(content);
			}
		} catch(final java.net.URISyntaxException e){
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>createStringURILiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	public static URILiteral createStringURILiteral(final String content) throws URISyntaxException {
		return new StringURILiteral(content);
	}

	/**
	 * <p>createStringURILiteralWithoutException.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.URILiteral} object.
	 */
	public static URILiteral createStringURILiteralWithoutException(final String content) {
		try {
			return new StringURILiteral(content);
		} catch(final URISyntaxException e){
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>createTypedLiteralWithoutLazyLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	public static TypedLiteral createTypedLiteralWithoutLazyLiteral(final String content, final String type)
			throws java.net.URISyntaxException {
		return TypedLiteralOriginalContent.createTypedLiteral(content, type);
	}

	/**
	 * <p>createTypedLiteralWithoutLazyLiteralWithoutException.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 */
	public static TypedLiteral createTypedLiteralWithoutLazyLiteralWithoutException(final String content, final String type) {
		try {
			return TypedLiteralOriginalContent.createTypedLiteral(content, type);
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>createTypedLiteralWithoutLazyLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param type a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @return a {@link lupos.datastructures.items.literal.TypedLiteral} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	public static TypedLiteral createTypedLiteralWithoutLazyLiteral(
			final String content, final URILiteral type)
			throws java.net.URISyntaxException {
		return TypedLiteralOriginalContent.createTypedLiteral(content, type);
	}

	/**
	 * <p>createLiteralWithoutLazyLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createLiteralWithoutLazyLiteral(final String content) {
		if (mapType == MapType.NOCODEMAP || mapType == MapType.LAZYLITERAL
				|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
				|| mapType == MapType.PREFIXCODEMAP
				|| mapType == MapType.URICODEMAP) {
			return new StringLiteral(content);
		} else {
			return new CodeMapLiteral(content);
		}
	}

	/**
	 * <p>createStringLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createStringLiteral(final String content) {
			return new StringLiteral(content);
	}

	/**
	 * <p>createLanguageTaggedLiteralWithoutLazyLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param language a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.LanguageTaggedLiteral} object.
	 */
	public static LanguageTaggedLiteral createLanguageTaggedLiteralWithoutLazyLiteral(
			final String content, final String language) {
		return LanguageTaggedLiteralOriginalLanguage.createLanguageTaggedLiteral(content, language);
	}

	/**
	 * <p>createAnonymousLiteralWithoutLazyLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.AnonymousLiteral} object.
	 */
	public static AnonymousLiteral createAnonymousLiteralWithoutLazyLiteral(
			final String content) {
		return new AnonymousLiteral(content);
	}

	/**
	 * <p>createURILiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	public static Literal createURILiteral(final String content)
	throws java.net.URISyntaxException {
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			return new LazyLiteral(content);
		} else {
			return createURILiteralWithoutLazyLiteral(content);
		}

	}

	/**
	 * <p>createURILiteralWithoutException.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createURILiteralWithoutException(final String content) {
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			return new LazyLiteral(content);
		} else {
			return createURILiteralWithoutLazyLiteralWithoutException(content);
		}
	}

	/**
	 * <p>createTypedLiteralWithoutException.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createTypedLiteralWithoutException(final String content, final String type) {
		try {
			return LiteralFactory.createTypedLiteral(content, type);
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>createTypedLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	public static Literal createTypedLiteral(final String content, final String type) throws java.net.URISyntaxException {
		final Literal typedLiteral = createTypedLiteralWithoutLazyLiteral(content, type);
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			if (typedLiteral.originalStringDiffers()) {
				return new LazyLiteralOriginalContent(typedLiteral.toString(),
						typedLiteral.originalString());
			} else {
				return new LazyLiteral(typedLiteral.toString());
			}
		} else {
			return typedLiteral;
		}
	}

	/**
	 * <p>createTypedLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param type a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java$net$URISyntaxException if any.
	 */
	public static Literal createTypedLiteral(final String content, final URILiteral type) throws java.net.URISyntaxException {
		final Literal typedLiteral = createTypedLiteralWithoutLazyLiteral(content, type);
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			if (typedLiteral.originalStringDiffers()) {
				return new LazyLiteralOriginalContent(typedLiteral.toString(),
						typedLiteral.originalString());
			} else {
				return new LazyLiteral(typedLiteral.toString());
			}
		} else {
			return typedLiteral;
		}
	}

	/**
	 * <p>creatPlainStringLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.string.PlainStringLiteral} object.
	 */
	public static PlainStringLiteral creatPlainStringLiteral(
			final String content) {
		return new PlainStringLiteral(content);
	}

	/**
	 * <p>createLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createLiteral(final String content) {
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			return new LazyLiteral(content);
		} else {
			return createLiteralWithoutLazyLiteral(content);
		}
	}

	/**
	 * <p>createLanguageTaggedLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param language a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createLanguageTaggedLiteral(final String content,
			final String language) {
		final Literal languageTaggedLiteral = createLanguageTaggedLiteralWithoutLazyLiteral(
				content, language);
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			if (languageTaggedLiteral.originalStringDiffers()) {
				return new LazyLiteralOriginalContent(languageTaggedLiteral
						.toString(), languageTaggedLiteral.originalString());
			} else {
				return new LazyLiteral(languageTaggedLiteral.toString());
			}
		} else {
			return languageTaggedLiteral;
		}
	}

	/**
	 * <p>createAnonymousLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createAnonymousLiteral(final String content) {
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			return new LazyLiteral(content);
		} else {
			return createAnonymousLiteralWithoutLazyLiteral(content);
		}
	}

	/**
	 * <p>createPostFixOfURI.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal createPostFixOfURI(final String content) {
		if (mapType == MapType.NOCODEMAP || mapType == MapType.LAZYLITERAL
				|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
				|| mapType == MapType.PREFIXCODEMAP) {
			return new StringLiteral(content);
		} else {
			return new CodeMapLiteral(content);
		}
	}

	public enum MapType {
		TRIEMAP, HASHMAP, DBBPTREE, SMALLERINHASHMAPLARGERINDBBPTREE, NOCODEMAP, PREFIXCODEMAP, URICODEMAP, LAZYLITERAL, LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
	};

	/** Constant <code>mapType</code> */
	protected static volatile MapType mapType = MapType.NOCODEMAP;

	/**
	 * <p>setType.</p>
	 *
	 * @param mapType a {@link lupos.datastructures.items.literal.LiteralFactory.MapType} object.
	 */
	public static void setType(final MapType mapType) {
		LiteralFactory.mapType = mapType;
		if (mapType != MapType.NOCODEMAP && mapType != MapType.LAZYLITERAL
				&& mapType != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
				&& mapType != MapType.PREFIXCODEMAP) {
			CodeMapLiteral.init();
		}
		if (mapType != MapType.NOCODEMAP && mapType != MapType.LAZYLITERAL
				&& mapType != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
			CodeMapURILiteral.init();
		}
	}

	/**
	 * <p>setTypeWithoutInitializing.</p>
	 *
	 * @param mapType a {@link lupos.datastructures.items.literal.LiteralFactory.MapType} object.
	 */
	public static void setTypeWithoutInitializing(final MapType mapType) {
		LiteralFactory.mapType = mapType;
	}

	/**
	 * <p>Getter for the field <code>mapType</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.LiteralFactory.MapType} object.
	 */
	public static MapType getMapType() {
		return mapType;
	}
}
