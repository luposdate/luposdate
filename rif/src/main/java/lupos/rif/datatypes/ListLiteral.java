package lupos.rif.datatypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import lupos.datastructures.items.literal.Literal;
import lupos.rdf.Prefix;

/**
 * <p>ListLiteral class.</p>
 *
 * @author groppe
 * @version $Id: $Id
 */
public class ListLiteral extends Literal {

	/**
	 * The entries of this list
	 */
	private final List<Literal> entries;

	/**
	 * Gets the entries of this list...
	 *
	 * @return the entries of this list
	 */
	public List<Literal> getEntries(){
		return this.entries;
	}

	/**
	 * Constructor, which expects the entries of this list as parameter
	 *
	 * @param entries the entries of this list...
	 */
	public ListLiteral(final List<Literal> entries){
		this.entries = entries;
	}

	/** {@inheritDoc} */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public String[] getUsedStringRepresentations() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Literal createThisLiteralNew() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public String toString(){
		final StringBuilder sa = new StringBuilder();
		sa.append("[");
		boolean flag = false;
		for(final Literal l: this.entries){
			if(flag){
				sa.append(", ");
			}
			flag = true;
			sa.append(l.toString());
		}
		sa.append("]");
		return sa.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final Prefix prefix){
		final StringBuilder sa = new StringBuilder();
		sa.append("[");
		boolean flag = false;
		for(final Literal l: this.entries){
			if(flag){
				sa.append(", ");
			}
			flag = true;
			sa.append(l.toString(prefix));
		}
		sa.append("]");
		return sa.toString();
	}

	/** {@inheritDoc} */
	@Override
	public ListLiteral clone(){
		return new ListLiteral(new ArrayList<Literal>(this.entries));
	}
}
