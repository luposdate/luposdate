package lupos.datastructures.items;

import java.io.Serializable;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.Literal;

/**
 * Interface for items that can be part of triple patterns.
 * 
 * @see {@link AnonymousLiteral}, {@link Literal}, {@link URILiteral}, {@link Variable}
 */
public interface Item extends Serializable {
	public boolean isVariable();
	public Literal getLiteral(Bindings b);
	public String getName();
}
