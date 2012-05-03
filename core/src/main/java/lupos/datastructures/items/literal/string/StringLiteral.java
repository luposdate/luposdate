package lupos.datastructures.items.literal.string;

import java.io.Externalizable;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.Literal;

public class StringLiteral extends PlainStringLiteral implements Item,
		Comparable<Literal>, Externalizable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7110464399527264155L;

	public StringLiteral() {
	}

	public StringLiteral(String content) {
		if (content.length() >= 6 && content.startsWith("\"\"\"")
				&& content.endsWith("\"\"\""))
			content = "\"" + content.substring(3, content.length() - 3) + "\"";
		else if (content.length() >= 6 && content.startsWith("'''")
				&& content.endsWith("'''"))
			content = "\"" + content.substring(3, content.length() - 3) + "\"";
		else if (content.length() >= 2 && content.startsWith("'")
				&& content.endsWith("'"))
			content = "\"" + content.substring(1, content.length() - 1) + "\"";
		this.content = content;
	}
}
