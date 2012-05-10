package lupos.gui.operatorgraph.visualeditor.util;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.Literal;

public class DummyItem implements Item {
	private static final long serialVersionUID = 1L;

	public Literal getLiteral(Bindings b) {
		return null;
	}

	public String getName() {
		return "";
	}

	public boolean isVariable() {
		return false;
	}

	public String toString() {
		return "";
	}
}