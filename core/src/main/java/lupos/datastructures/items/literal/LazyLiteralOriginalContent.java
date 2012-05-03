package lupos.datastructures.items.literal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

/**
 * This class determines the type of it (like URILiteral, AnonymousLiteral,
 * TypedLiteral, ...) lazy, i.e., only up on request by a special method.
 * Internally, it uses a code for its string representation. Furthermore, it
 * stores a code for the original content, too, such that the original content
 * can be retrieved (as required by the DAWG test cases)!
 * 
 * @author groppe
 */
public class LazyLiteralOriginalContent extends LazyLiteral implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8200285980229881388L;
	private int codeOriginalContent;
	private String originalString = null;

	public LazyLiteralOriginalContent() {
	}

	public LazyLiteralOriginalContent(final String content,
			final String originalContent) {
		super(content);
		final Integer codeFromHashMap = hm.get(originalContent);
		if (codeFromHashMap != null && codeFromHashMap != 0) {
			this.codeOriginalContent = codeFromHashMap.intValue();
		} else {
			this.codeOriginalContent = v.size() + 1;
			hm.put(originalContent, new Integer(this.codeOriginalContent));
			if (codeOriginalContent == Integer.MAX_VALUE)
				System.err.println("Literal code overflow! Not good!");
			v.put(new Integer(this.codeOriginalContent), originalContent);
		}
	}

	public LazyLiteralOriginalContent(final int code,
			final String originalContent) {
		super(code);
		final Integer codeFromHashMap = hm.get(originalContent);
		if (codeFromHashMap != null && codeFromHashMap != 0) {
			this.codeOriginalContent = codeFromHashMap.intValue();
		} else {
			this.codeOriginalContent = v.size() + 1;
			hm.put(originalContent, new Integer(this.codeOriginalContent));
			if (codeOriginalContent == Integer.MAX_VALUE)
				System.err.println("Literal code overflow! Not good!");
			v.put(new Integer(this.codeOriginalContent), originalContent);
		}
	}

	public LazyLiteralOriginalContent(final int code,
			final int codeOriginalContent) {
		super(code);
		this.codeOriginalContent = codeOriginalContent;
	}

	public LazyLiteralOriginalContent(final int code,
			final int codeOriginalContent, final Literal materializedLiteral) {
		super(code, materializedLiteral);
		this.codeOriginalContent = codeOriginalContent;
		this.originalString = materializedLiteral.originalString();
	}

	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { toString(), originalString() };
	}

	@Override
	public String originalString() {
		if (originalString == null)
			originalString = v.get(codeOriginalContent);
		return originalString;
	}

	public int getCodeOriginalContent() {
		return this.codeOriginalContent;
	}

	@Override
	public boolean originalStringDiffers() {
		return true;
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		codeOriginalContent = LuposObjectInputStream.readLuposInt(in);
		// codeOriginalContent = in.readInt();
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		LuposObjectOutputStream.writeLuposInt(codeOriginalContent, out);
		// out.writeInt(codeOriginalContent);
	}
}
