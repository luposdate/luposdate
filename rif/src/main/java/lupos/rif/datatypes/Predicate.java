package lupos.rif.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.rdf.Prefix;
import lupos.rif.RIFException;

public class Predicate implements Serializable {
	private static final long serialVersionUID = 253370338303245743L;
	protected URILiteral name;
	protected ArrayList<Literal> literals = new ArrayList<Literal>();

	public Literal getName() {
		return name;
	}

	public void setName(Literal name) {
		if (name instanceof URILiteral)
			this.name = (URILiteral) name;
		else
			throw new RIFException("Predicatename can only be URILiteral!");
	}

	public List<Literal> getParameters() {
		return literals;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj != null && obj instanceof Predicate) {
			final Predicate pred = (Predicate) obj;
			if (!pred.name.equals(name))
				return false;
			if (pred.literals.size() != literals.size())
				return false;
			for (int i = 0; i < literals.size(); i++)
				if (!pred.literals.get(i).equals(literals.get(i)))
					return false;
			return true;
		} else
			return false;
	}

	public String toString() {
		final StringBuffer str = new StringBuffer();
		str.append(name.toString()).append("(");
		for (int idx = 0; idx < literals.size(); idx++) {
			str.append(literals.get(idx).toString());
			if (idx < literals.size() - 1)
				str.append(", ");
			else
				str.append(")");
		}
		if (str.substring(str.length() - 1) != ")")
			str.append(")");
		return str.toString();
	}

	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer();
		str.append(name.toString(prefixInstance)).append("(");
		for (int idx = 0; idx < literals.size(); idx++) {
			str.append(literals.get(idx).toString(prefixInstance));
			if (idx < literals.size() - 1)
				str.append(", ");
			else
				str.append(")");
		}
		if (str.substring(str.length() - 1) != ")")
			str.append(")");
		return str.toString();
	}

}
