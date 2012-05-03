package lupos.datastructures.items.literal;

import java.io.Externalizable;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;
import lupos.rdf.Prefix;

public abstract class Literal implements Item, Comparable<Literal>,
		Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean valueEquals(final Literal lit) {
		return (toString().compareTo(lit.toString()) == 0);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj.getClass() == Literal.class || obj instanceof StringLiteral
				|| obj instanceof CodeMapLiteral) {
			final Literal lit = (Literal) obj;
			return toString().equals(lit.toString());
		} else if (obj instanceof TypedLiteral) {
			final TypedLiteral tl = (TypedLiteral) obj;
			if (tl.getType().compareTo(
					"<http://www.w3.org/2001/XMLSchema#string>") == 0)
				return (tl.toString().compareTo(this.toString()) == 0);
			else
				return false;
		} else if (obj instanceof Literal) {
			return this
					.compareToNotNecessarilySPARQLSpecificationConform((Literal) obj) == 0;
		} else
			return false;
	}

	public int compareTo(final Literal other) {

		return ComparatorAST.intComp(this, other);
	}

	public int compareToNotNecessarilySPARQLSpecificationConform(
			final Literal other) {
		return ComparatorAST.intComp(this, other);
	}

	public abstract String[] getUsedStringRepresentations();

	public Literal getLiteral(final Bindings b) {
		return this;
	}

	public String getName() {
		return toString();
	}

	public boolean isVariable() {
		return false;
	}

	public boolean isBlank() {
		return (this instanceof AnonymousLiteral);
	}

	public boolean isURI() {
		return (this instanceof URILiteral);
	}

	public String originalString() {
		return toString();
	}

	public boolean originalStringDiffers() {
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public String printYagoStringWithPrefix() {
		return toString();
	}
	
	public String toString(Prefix prefix){
		return toString();
	}
}
