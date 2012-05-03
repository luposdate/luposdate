package lupos.datastructures.items.literal;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class AnonymousLiteral extends Literal implements Externalizable {
	private static final long serialVersionUID = -2205699226774394906L;

	protected Literal content;

	public AnonymousLiteral() {
	}

	public AnonymousLiteral(final String content) {
		this.content = LiteralFactory.createLiteralWithoutLazyLiteral(content);
	}

	protected AnonymousLiteral(final int code) {
		this.content = new CodeMapLiteral(code);
	}

	@Override
	public String toString() {
		return content.toString();
	}
	
	public String getBlankNodeLabel(){
		return toString().substring(2);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof AnonymousLiteral) {
			final AnonymousLiteral lit = (AnonymousLiteral) obj;
			return content.equals(lit.content);
		} else
			return (this.toString().compareTo(obj.toString()) == 0);
	}

	public Literal getContent() {
		return content;
	}

	@Override
	public String[] getUsedStringRepresentations() {
		return content.getUsedStringRepresentations();
	}

	public void readExternal(final ObjectInput in) throws IOException,
	ClassNotFoundException {
		content = LuposObjectInputStream.readLuposLiteral(in);
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		LuposObjectOutputStream.writeLuposLiteral(content, out);
	}
}
