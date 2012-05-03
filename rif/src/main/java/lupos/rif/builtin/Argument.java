package lupos.rif.builtin;

import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.Literal;

public class Argument {
	public Bindings binding;
	public List<Item> arguments;
	public Literal result;

	public boolean isBinding() {
		for (final Item item : arguments)
			if (item.isVariable())
				return true;
		return false;
	}
}
