package lupos.datastructures.items.literal;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public abstract class URILiteral extends Literal implements Externalizable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2826285205704387677L;

	public static boolean isURI(final String content) {
		if (content.length() < 2
				|| content.substring(0, 1).compareTo("<") != 0
				|| content.substring(content.length() - 1, content.length())
				.compareTo(">") != 0)
			return false;
		try {
			URI.create(content.substring(1, content.length() - 1));
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "<" + getString() + ">";
	}
	
	@Override
	public String toString(lupos.rdf.Prefix prefixInstance) {
		return prefixInstance.add(this.toString());
	}

	public InputStream openStream() throws IOException {
		if (getString().startsWith("inlinedata:")) {
			return new ByteArrayInputStream(getString().substring(11)
					.getBytes());
		}

		if (getString().startsWith("file:")) {
			return new FileInputStream(getString().substring(5));
		}
		final URI u = URI.create(getString());
		if (u.getScheme().compareTo("file:") == 0) {
			return new FileInputStream(u.getSchemeSpecificPart()
					+ u.getFragment());
		} else
			return (u.toURL().openStream());
	}

	public abstract String getString();

	public abstract void update(String s) throws java.net.URISyntaxException;
}