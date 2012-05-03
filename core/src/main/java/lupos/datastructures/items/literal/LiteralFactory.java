package lupos.datastructures.items.literal;

import java.io.IOException;
import java.net.URISyntaxException;

import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.codemap.CodeMapURILiteral;
import lupos.datastructures.items.literal.string.PlainStringLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class LiteralFactory {

	public static void writeLuposLiteral(final Literal lit,
			final LuposObjectOutputStream out) throws IOException {
		if (lit instanceof CodeMapURILiteral) {
			out.writeLuposByte((byte) LuposObjectInputStream.URILITERAL);
			writeContentOfLiteral(((CodeMapURILiteral) lit).getContent(), out);
			out.writeLuposInt(((CodeMapURILiteral) lit).getPrefixCode());
		} else if (lit instanceof StringURILiteral) {
			out.writeLuposByte((byte) LuposObjectInputStream.URILITERAL);
			out.writeLuposString(((StringURILiteral) lit).getString());
		} else if (lit instanceof TypedLiteral) {
			out.writeLuposByte((byte) LuposObjectInputStream.TYPEDLITERAL);
			if (lit instanceof TypedLiteralOriginalContent)
				writeContentOfLiteral(
						((TypedLiteralOriginalContent) lit).originalContent,
						out);
			else
				writeContentOfLiteral(((TypedLiteral) lit).content, out);
			writeLuposLiteral(((TypedLiteral) lit).type, out);
		} else if (lit instanceof LanguageTaggedLiteral) {
			out
					.writeLuposByte((byte) LuposObjectInputStream.LANGUAGETAGGEDLITERAL);
			writeContentOfLiteral(((LanguageTaggedLiteral) lit).content, out);
			if (lit instanceof LanguageTaggedLiteralOriginalLanguage)
				writeContentOfLiteral(
						((LanguageTaggedLiteralOriginalLanguage) lit).originalLang,
						out);
			else
				writeContentOfLiteral(((LanguageTaggedLiteral) lit).lang, out);
		} else if (lit instanceof AnonymousLiteral) {
			out.writeLuposByte((byte) LuposObjectInputStream.ANONYMOUSLITERAL);
			writeContentOfLiteral(((AnonymousLiteral) lit).content, out);
		} else if (lit instanceof LazyLiteralOriginalContent) {
			if (((LazyLiteralOriginalContent) lit).isMaterialized())
				out
						.writeLuposByte((byte) LuposObjectInputStream.LAZYLITERALORIGINALCONTENTMATERIALIZED);
			else
				out
						.writeLuposByte((byte) LuposObjectInputStream.LAZYLITERALORIGINALCONTENT);
			out.writeLuposInt(((LazyLiteral) lit).getCode());
			out.writeLuposInt(((LazyLiteralOriginalContent) lit)
					.getCodeOriginalContent());
			if (((LazyLiteralOriginalContent) lit).isMaterialized())
				writeLuposLiteral(((LazyLiteralOriginalContent) lit)
						.getLiteral(), out);
		} else if (lit instanceof LazyLiteral) {
			if (((LazyLiteral) lit).isMaterialized())
				out
						.writeLuposByte((byte) LuposObjectInputStream.LAZYLITERALMATERIALIZED);
			else
				out.writeLuposByte((byte) LuposObjectInputStream.LAZYLITERAL);
			out.writeLuposInt(((LazyLiteral) lit).getCode());
			if (((LazyLiteral) lit).isMaterialized())
				writeLuposLiteral(((LazyLiteral) lit).getLiteral(), out);
		} else if (lit.getClass() == PlainStringLiteral.class) {
			out
					.writeLuposByte((byte) LuposObjectInputStream.PLAINSTRINGLITERAL);
			out.writeLuposString(lit.toString());
		} else {
			// "normal" Literal object!
			out.writeLuposByte((byte) LuposObjectInputStream.LITERAL);
			writeContentOfLiteral(lit, out);
		}
	}

	protected static void writeContentOfLiteral(final Literal lit,
			final LuposObjectOutputStream out) throws IOException {
		if (lit instanceof StringLiteral) {
			out.writeLuposString(lit.toString());
		} else {
			out.writeLuposInt(((CodeMapLiteral) lit).getCode());
		}
	}

	public static Literal readLuposLiteral(final LuposObjectInputStream in)
			throws IOException {
		try {
			final int type = in.readLuposByte();
			switch (type) {
			case LuposObjectInputStream.URILITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
					return new StringURILiteral("<" + in.readLuposString()
							+ ">");
				} else if (mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.LAZYLITERAL) {
					return new CodeMapURILiteral(in.readLuposString(), in
							.readLuposInt());
				} else
					return new CodeMapURILiteral(in.readLuposInt(), in
							.readLuposInt());
			case LuposObjectInputStream.TYPEDLITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERAL
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
						|| mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.URICODEMAP) {
					final String content = in.readLuposString();
					return TypedLiteralOriginalContent.createTypedLiteral(
							content, (URILiteral) readLuposLiteral(in));
				} else {
					final int content = in.readLuposInt();
					return TypedLiteralOriginalContent.createTypedLiteral(
							content, (URILiteral) readLuposLiteral(in));
				}
			case LuposObjectInputStream.LANGUAGETAGGEDLITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERAL
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
						|| mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.URICODEMAP) {
					final String content = in.readLuposString();
					final String lang = in.readLuposString();
					return LanguageTaggedLiteralOriginalLanguage
							.createLanguageTaggedLiteral(content, lang);
				} else {
					final int content = in.readLuposInt();
					final int lang = in.readLuposInt();
					return LanguageTaggedLiteralOriginalLanguage
							.createLanguageTaggedLiteral(content, lang);
				}
			case LuposObjectInputStream.ANONYMOUSLITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERAL
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
						|| mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.URICODEMAP) {
					return new AnonymousLiteral(in.readLuposString());
				} else {
					return new AnonymousLiteral(in.readLuposInt());
				}
			case LuposObjectInputStream.LAZYLITERAL:
				return new LazyLiteral(in.readLuposInt());
			case LuposObjectInputStream.LAZYLITERALORIGINALCONTENT:
				return new LazyLiteralOriginalContent(in.readLuposInt(), in
						.readLuposInt());
			case LuposObjectInputStream.LAZYLITERALMATERIALIZED:
				return new LazyLiteral(in.readLuposInt(), readLuposLiteral(in));
			case LuposObjectInputStream.LAZYLITERALORIGINALCONTENTMATERIALIZED:
				return new LazyLiteralOriginalContent(in.readLuposInt(), in
						.readLuposInt(), readLuposLiteral(in));
			case LuposObjectInputStream.PLAINSTRINGLITERAL:
				return new PlainStringLiteral(in.readLuposString());
			default:
			case LuposObjectInputStream.LITERAL:
				if (mapType == MapType.NOCODEMAP
						|| mapType == MapType.LAZYLITERAL
						|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
						|| mapType == MapType.PREFIXCODEMAP
						|| mapType == MapType.URICODEMAP) {
					return new StringLiteral(in.readLuposString());
				} else {

					return new CodeMapLiteral(in.readLuposInt());
				}
			}
		} catch (final URISyntaxException e) {
			throw new IOException(
					"Expected URI in InputStream, but it is not an URI!");
		}
	}

	public static URILiteral createURILiteralWithoutLazyLiteral(
			final String content) throws java.net.URISyntaxException {
		if (mapType == MapType.NOCODEMAP
				|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)
			return new StringURILiteral(content);
		else
			return new CodeMapURILiteral(content);
	}
	
	public static URILiteral createURILiteralWithoutLazyLiteralWithoutException(
			final String content) {
		try {
			if (mapType == MapType.NOCODEMAP
					|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)
				return new StringURILiteral(content);
			else
				return new CodeMapURILiteral(content);
		} catch(java.net.URISyntaxException e){
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}
	
	public static URILiteral createStringURILiteral(String content) throws URISyntaxException {
		return new StringURILiteral(content);
	}

	public static URILiteral createStringURILiteralWithoutException(String content) {
		try {
			return new StringURILiteral(content);
		} catch(URISyntaxException e){
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	public static TypedLiteral createTypedLiteralWithoutLazyLiteral(
			final String content, final String type)
			throws java.net.URISyntaxException {
		return TypedLiteralOriginalContent.createTypedLiteral(content, type);
	}

	public static TypedLiteral createTypedLiteralWithoutLazyLiteral(
			final String content, final URILiteral type)
			throws java.net.URISyntaxException {
		return TypedLiteralOriginalContent.createTypedLiteral(content, type);
	}

	public static Literal createLiteralWithoutLazyLiteral(final String content) {
		if (mapType == MapType.NOCODEMAP || mapType == MapType.LAZYLITERAL
				|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
				|| mapType == MapType.PREFIXCODEMAP
				|| mapType == MapType.URICODEMAP)
			return new StringLiteral(content);
		else
			return new CodeMapLiteral(content);
	}

	public static Literal createStringLiteral(final String content) {
			return new StringLiteral(content);
	}

	public static LanguageTaggedLiteral createLanguageTaggedLiteralWithoutLazyLiteral(
			final String content, final String language) {
		return LanguageTaggedLiteralOriginalLanguage
				.createLanguageTaggedLiteral(content, language);
	}

	public static AnonymousLiteral createAnonymousLiteralWithoutLazyLiteral(
			final String content) {
		return new AnonymousLiteral(content);
	}

	public static Literal createURILiteral(final String content)
	throws java.net.URISyntaxException {
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP))
			return new LazyLiteral(content);
		else
			return createURILiteralWithoutLazyLiteral(content);

	}

	public static Literal createURILiteralWithoutException(final String content) {
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP))
			return new LazyLiteral(content);
		else
			return createURILiteralWithoutLazyLiteralWithoutException(content);
	}

	public static Literal createTypedLiteral(final String content,
			final String type) throws java.net.URISyntaxException {
		final Literal typedLiteral = createTypedLiteralWithoutLazyLiteral(
				content, type);
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			if (typedLiteral.originalStringDiffers())
				return new LazyLiteralOriginalContent(typedLiteral.toString(),
						typedLiteral.originalString());
			else
				return new LazyLiteral(typedLiteral.toString());
		} else
			return typedLiteral;
	}

	public static Literal createTypedLiteral(final String content,
			final URILiteral type) throws java.net.URISyntaxException {
		final Literal typedLiteral = createTypedLiteralWithoutLazyLiteral(
				content, type);
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			if (typedLiteral.originalStringDiffers())
				return new LazyLiteralOriginalContent(typedLiteral.toString(),
						typedLiteral.originalString());
			else
				return new LazyLiteral(typedLiteral.toString());
		} else
			return typedLiteral;
	}

	public static PlainStringLiteral creatPlainStringLiteral(
			final String content) {
		return new PlainStringLiteral(content);
	}

	public static Literal createLiteral(final String content) {
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP))
			return new LazyLiteral(content);
		else
			return createLiteralWithoutLazyLiteral(content);
	}

	public static Literal createLanguageTaggedLiteral(final String content,
			final String language) {
		final Literal languageTaggedLiteral = createLanguageTaggedLiteralWithoutLazyLiteral(
				content, language);
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			if (languageTaggedLiteral.originalStringDiffers())
				return new LazyLiteralOriginalContent(languageTaggedLiteral
						.toString(), languageTaggedLiteral.originalString());
			else
				return new LazyLiteral(languageTaggedLiteral.toString());
		} else
			return languageTaggedLiteral;
	}

	public static Literal createAnonymousLiteral(final String content) {
		if (LazyLiteral.getHm() != null
				&& (mapType == MapType.LAZYLITERAL || mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP))
			return new LazyLiteral(content);
		else
			return createAnonymousLiteralWithoutLazyLiteral(content);
	}

	public static Literal createPostFixOfURI(final String content) {
		if (mapType == MapType.NOCODEMAP || mapType == MapType.LAZYLITERAL
				|| mapType == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
				|| mapType == MapType.PREFIXCODEMAP)
			return new StringLiteral(content);
		else
			return new CodeMapLiteral(content);
	}

	public enum MapType {
		TRIEMAP, HASHMAP, DBBPTREE, SMALLERINHASHMAPLARGERINDBBPTREE, NOCODEMAP, PREFIXCODEMAP, URICODEMAP, LAZYLITERAL, LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
	};

	protected static volatile MapType mapType = MapType.NOCODEMAP;

	public static void setType(final MapType mapType) {
		LiteralFactory.mapType = mapType;
		if (mapType != MapType.NOCODEMAP && mapType != MapType.LAZYLITERAL
				&& mapType != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
				&& mapType != MapType.PREFIXCODEMAP)
			CodeMapLiteral.init();
		if (mapType != MapType.NOCODEMAP && mapType != MapType.LAZYLITERAL
				&& mapType != MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)
			CodeMapURILiteral.init();
	}

	public static void setTypeWithoutInitializing(final MapType mapType) {
		LiteralFactory.mapType = mapType;
	}

	public static MapType getMapType() {
		return mapType;
	}
}
