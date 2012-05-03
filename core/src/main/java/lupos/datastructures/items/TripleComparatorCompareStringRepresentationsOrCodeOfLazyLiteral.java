package lupos.datastructures.items;

import java.io.Serializable;

import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex;

public class TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral
		extends TripleComparator implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -742270859357719454L;

	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral() {
		super();
	}

	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final COMPARE primary) {
		super(primary);
	}

	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final COMPARE primary, final COMPARE secondary) {
		super(primary, secondary);
	}

	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final COMPARE primary, final COMPARE secondary,
			final COMPARE tertiary) {
		super(primary, secondary, tertiary);
	}

	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final RDF3XIndex.CollationOrder orderPattern) {
		super(orderPattern);
	}

	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final byte readByte) {
		super(readByte);
	}

	@Override
	public int compare(final Triple t0, final Triple t1) {
		try {
			if (primary == COMPARE.NONE)
				return 0;
			Literal l0 = getLiteral(primary, t0);
			Literal l1 = getLiteral(primary, t1);
			if (l0 == null || l1 == null)
				return 0;
			int compare = (l0 instanceof LazyLiteral) ? l0
					.compareToNotNecessarilySPARQLSpecificationConform(l1) : l0
					.toString().compareTo(l1.toString());
			if (compare != 0)
				return compare;

			if (secondary == COMPARE.NONE)
				return 0;
			l0 = getLiteral(secondary, t0);
			l1 = getLiteral(secondary, t1);
			if (l0 == null || l1 == null)
				return 0;
			compare = (l0 instanceof LazyLiteral) ? l0
					.compareToNotNecessarilySPARQLSpecificationConform(l1) : l0
					.toString().compareTo(l1.toString());
			if (compare != 0)
				return compare;

			if (tertiary == COMPARE.NONE)
				return 0;

			l0 = getLiteral(tertiary, t0);
			l1 = getLiteral(tertiary, t1);
			if (l0 == null || l1 == null)
				return 0;
			return (l0 instanceof LazyLiteral) ? l0
					.compareToNotNecessarilySPARQLSpecificationConform(l1) : l0
					.toString().compareTo(l1.toString());
		} catch (final Exception e) {
			System.err.println(" t0:" + t0);
			System.err.println(" t1:" + t1);
			System.err.println(e);
			e.printStackTrace();
			return 0;
		}
	}
}
