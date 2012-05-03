package lupos.datastructures.items;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex;

public class TripleComparator implements Comparator<Triple>, Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5337742667604259992L;

	public enum COMPARE {
		NONE, SUBJECT, PREDICATE, OBJECT
	};

	protected COMPARE primary = COMPARE.NONE;
	protected COMPARE secondary = COMPARE.NONE;
	protected COMPARE tertiary = COMPARE.NONE;

	public TripleComparator() {
	}

	public TripleComparator(final COMPARE primary) {
		this.primary = primary;
	}

	public TripleComparator(final COMPARE primary, final COMPARE secondary) {
		this.primary = primary;
		this.secondary = secondary;
	}

	public TripleComparator(final COMPARE primary, final COMPARE secondary,
			final COMPARE tertiary) {
		this.primary = primary;
		this.secondary = secondary;
		this.tertiary = tertiary;
	}

	public TripleComparator(final RDF3XIndex.CollationOrder orderPattern) {
		switch (orderPattern) {
		default:
		case SPO:
			this.primary = COMPARE.SUBJECT;
			this.secondary = COMPARE.PREDICATE;
			this.tertiary = COMPARE.OBJECT;
			break;
		case SOP:
			this.primary = COMPARE.SUBJECT;
			this.secondary = COMPARE.OBJECT;
			this.tertiary = COMPARE.PREDICATE;
			break;
		case PSO:
			this.primary = COMPARE.PREDICATE;
			this.secondary = COMPARE.SUBJECT;
			this.tertiary = COMPARE.OBJECT;
			break;
		case POS:
			this.primary = COMPARE.PREDICATE;
			this.secondary = COMPARE.OBJECT;
			this.tertiary = COMPARE.SUBJECT;
			break;
		case OSP:
			this.primary = COMPARE.OBJECT;
			this.secondary = COMPARE.SUBJECT;
			this.tertiary = COMPARE.PREDICATE;
			break;
		case OPS:
			this.primary = COMPARE.OBJECT;
			this.secondary = COMPARE.PREDICATE;
			this.tertiary = COMPARE.SUBJECT;
			break;
		}
	}

	public TripleComparator(final byte readByte) {
		init(readByte);
	}

	protected void init(final byte readByte) {
		this.primary = COMPARE.values()[readByte % COMPARE.values().length];
		this.secondary = COMPARE.values()[(readByte / COMPARE.values().length)
				% COMPARE.values().length];
		this.tertiary = COMPARE.values()[(readByte / (COMPARE.values().length * COMPARE
				.values().length))
				% COMPARE.values().length];
	}

	public int compare(final Triple t0, final Triple t1) {
		try {
			if (primary != COMPARE.NONE) {
				final Literal l0 = getLiteral(primary, t0);
				final Literal l1 = getLiteral(primary, t1);
				if (!(l0 == null || l1 == null)) {
					final int compare = l0
							.compareToNotNecessarilySPARQLSpecificationConform(l1);
					if (compare != 0)
						return compare;
				} else
					return 0;
			} else
				return 0;

			if (secondary != COMPARE.NONE) {
				final Literal l0 = getLiteral(secondary, t0);
				final Literal l1 = getLiteral(secondary, t1);
				if (!(l0 == null || l1 == null)) {
					final int compare = l0
							.compareToNotNecessarilySPARQLSpecificationConform(l1);
					if (compare != 0)
						return compare;
				} else
					return 0;
			} else
				return 0;

			if (tertiary != COMPARE.NONE) {
				final Literal l0 = getLiteral(tertiary, t0);
				final Literal l1 = getLiteral(tertiary, t1);
				if (!(l0 == null || l1 == null))
					return l0
							.compareToNotNecessarilySPARQLSpecificationConform(l1);
				else
					return 0;
			} else
				return 0;
		} catch (final Exception e) {
			System.err.println(" t0:" + t0);
			System.err.println(" t1:" + t1);
			System.err.println(e);
			e.printStackTrace();
			return 0;
		}
	}

	public static Literal getLiteral(final COMPARE comp, final Triple t) {
		switch (comp) {
		case SUBJECT:
			return t.getSubject();
		case PREDICATE:
			return t.getPredicate();
		case OBJECT:
			return t.getObject();
		default:
			return null;
		}
	}

	public static Object getObject(final COMPARE comp, final Object[] items) {
		switch (comp) {
		case SUBJECT:
			return items[0];
		case PREDICATE:
			return items[1];
		case OBJECT:
			return items[2];
		default:
			return null;
		}
	}

	public static Item getItem(final COMPARE comp, final Item[] items) {
		switch (comp) {
		case SUBJECT:
			return items[0];
		case PREDICATE:
			return items[1];
		case OBJECT:
			return items[2];
		default:
			return null;
		}
	}

	public byte getBytePattern() {
		return (byte) ((byte) primary.ordinal() + COMPARE.values().length
				* (secondary.ordinal() + COMPARE.values().length
						* tertiary.ordinal()));
	}

	public void add(final COMPARE c) {
		if (primary == COMPARE.NONE)
			primary = c;
		else if (secondary == COMPARE.NONE)
			secondary = c;
		else if (tertiary == COMPARE.NONE)
			tertiary = c;
		else
			System.err
					.println("TripleComparator: TripleComparator-Comparisons already full up!");
	}

	public COMPARE getPrimary() {
		return primary;
	}

	public void setPrimary(final COMPARE primary) {
		this.primary = primary;
	}

	public COMPARE getSecondary() {
		return secondary;
	}

	public void setSecondary(final COMPARE secondary) {
		this.secondary = secondary;
	}

	public COMPARE getTertiary() {
		return tertiary;
	}

	public void setTertiary(final COMPARE tertiary) {
		this.tertiary = tertiary;
	}

	public void makeNoneForNull(final Triple t) {
		if (primary != null && primary != COMPARE.NONE) {
			if (t.getPos(primary.ordinal() - 1) == null) {
				primary = COMPARE.NONE;
			}
		}
		if (secondary != null && secondary != COMPARE.NONE) {
			if (t.getPos(secondary.ordinal() - 1) == null) {
				secondary = COMPARE.NONE;
			}
		}
		if (tertiary != null && tertiary != COMPARE.NONE) {
			if (t.getPos(tertiary.ordinal() - 1) == null) {
				tertiary = COMPARE.NONE;
			}
		}
	}

	@Override
	public String toString() {
		return "Compare triple by " + toString(primary) + " "
				+ toString(secondary) + " " + toString(tertiary);
	}

	public static String toString(final COMPARE compare) {
		if (compare == null)
			return "null";
		else
			return compare.toString();
	}

	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		init((byte) in.read());
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeByte(this.getBytePattern());
	}
}
