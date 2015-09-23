/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.datastructures.items;

import java.io.Serializable;

import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
public class TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral
		extends TripleComparator implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -742270859357719454L;

	/**
	 * <p>Constructor for TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral.</p>
	 */
	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral() {
		super();
	}

	/**
	 * <p>Constructor for TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral.</p>
	 *
	 * @param primary a COMPARE object.
	 */
	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final COMPARE primary) {
		super(primary);
	}

	/**
	 * <p>Constructor for TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral.</p>
	 *
	 * @param primary a COMPARE object.
	 * @param secondary a COMPARE object.
	 */
	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final COMPARE primary, final COMPARE secondary) {
		super(primary, secondary);
	}

	/**
	 * <p>Constructor for TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral.</p>
	 *
	 * @param primary a COMPARE object.
	 * @param secondary a COMPARE object.
	 * @param tertiary a COMPARE object.
	 */
	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final COMPARE primary, final COMPARE secondary,
			final COMPARE tertiary) {
		super(primary, secondary, tertiary);
	}

	/**
	 * <p>Constructor for TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral.</p>
	 *
	 * @param orderPattern a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final RDF3XIndexScan.CollationOrder orderPattern) {
		super(orderPattern);
	}

	/**
	 * <p>Constructor for TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral.</p>
	 *
	 * @param readByte a byte.
	 */
	public TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(
			final byte readByte) {
		super(readByte);
	}

	/** {@inheritDoc} */
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
