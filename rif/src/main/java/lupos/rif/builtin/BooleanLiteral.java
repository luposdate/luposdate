package lupos.rif.builtin;

import java.net.URISyntaxException;

import lupos.datastructures.items.literal.TypedLiteral;
import lupos.rif.RIFException;

public class BooleanLiteral extends TypedLiteral {

	public final static BooleanLiteral FALSE;
	public final static BooleanLiteral TRUE;
	final public boolean value;

	static {
		FALSE = BooleanLiteral.create(false);
		TRUE = BooleanLiteral.create(true);
	}

	private BooleanLiteral(boolean value) throws URISyntaxException {
		super("\"" + Boolean.toString(value) + "\"",
				"<http://www.w3.org/2001/XMLSchema#boolean>");
		this.value = value;
	}

	public static BooleanLiteral create(boolean value) {
		try {
			return new BooleanLiteral(value);
		} catch (URISyntaxException e) {
			throw new RIFException(e.getMessage());
		}
	}

	public static BooleanLiteral not(BooleanLiteral bl) {
		return create(!bl.value);
	}
}
