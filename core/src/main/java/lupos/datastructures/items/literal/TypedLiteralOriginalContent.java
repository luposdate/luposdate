package lupos.datastructures.items.literal;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URISyntaxException;

import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class TypedLiteralOriginalContent extends TypedLiteral implements
Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -913934251866276647L;
	protected Literal originalContent;

	public TypedLiteralOriginalContent() {
	}

	protected TypedLiteralOriginalContent(final String content,
			final String type) throws java.net.URISyntaxException {
		this(content, LiteralFactory.createURILiteralWithoutLazyLiteral(type));
	}

	protected TypedLiteralOriginalContent(final String content2,
			final URILiteral type) {
		super(content2, type);
		originalContent = (this.content.toString().compareTo(content2) != 0) ? LiteralFactory
				.createLiteralWithoutLazyLiteral(content2)
				: this.content;
	}

	protected TypedLiteralOriginalContent(final int codeContent,
			final URILiteral type) {
		super(codeContent, type);
		this.originalContent = this.content;
		final String uniqueRepresentation = checkContent(originalContent
				.toString(), this.type);
		this.content = (uniqueRepresentation.compareTo(originalContent
				.toString()) != 0) ? LiteralFactory
						.createLiteralWithoutLazyLiteral(uniqueRepresentation)
						: this.originalContent;
	}

	public static TypedLiteral createTypedLiteral(final String content2,
			final String type) throws URISyntaxException {
		if (checkContent(content2, type).compareTo(content2) != 0)
			return new TypedLiteralOriginalContent(content2, type);
		else
			return new TypedLiteral(content2, type);
	}

	public static TypedLiteral createTypedLiteral(final String content2,
			final URILiteral type) {
		if (checkContent(content2, type).compareTo(content2) != 0)
			return new TypedLiteralOriginalContent(content2, type);
		else
			return new TypedLiteral(content2, type);
	}

	public static TypedLiteral createTypedLiteral(final int codeContent,
			final URILiteral type) {
		final String content2 = CodeMapLiteral.getValue(codeContent);
		if (checkContent(content2, type).compareTo(content2) != 0)
			return new TypedLiteralOriginalContent(content2, type);
		else
			return new TypedLiteral(content2, type);
	}

	@Override
	public String getOriginalContent() {
		return originalContent.toString();
	}

	@Override
	public String originalString() {
		return commonToOriginalString(originalContent.toString());
	}

	@Override
	public String toString(lupos.rdf.Prefix prefixInstance) {
		return commonToOriginalString(originalContent.toString(), prefixInstance);
	}
	
	@Override
	public String[] getUsedStringRepresentations() {
		final String[] typeRepr = type.getUsedStringRepresentations();
		return new String[] { content.toString(), originalContent.toString(),
				typeRepr[0], typeRepr[1] };
	}

	public String printYagoStringWithPrefix() {
		return originalContent.printYagoStringWithPrefix() + "^^"
		+ type.printYagoStringWithPrefix();
	}

	@Override
	public boolean originalStringDiffers() {
		return true;
	}

	public Literal getOriginalContentLiteral() {
		return this.originalContent;
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
	ClassNotFoundException {
		super.readExternal(in);
		originalContent = LuposObjectInputStream.readLuposLiteral(in);
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		LuposObjectOutputStream.writeLuposLiteral(originalContent, out);
	}
}