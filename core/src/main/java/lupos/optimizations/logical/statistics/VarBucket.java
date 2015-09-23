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
package lupos.optimizations.logical.statistics;

import java.util.ArrayList;
import java.util.List;

import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.misc.Tuple;
public class VarBucket implements Cloneable {

	public Literal minimum = null;
	public Literal maximum = null;
	public List<Entry> selectivityOfInterval = new ArrayList<Entry>();

	/**
	 * <p>getSum.</p>
	 *
	 * @return a double.
	 */
	public double getSum() {
		double sum = 0.0;
		for (final Entry e : selectivityOfInterval) {
			sum += e.selectivity;
		}
		return sum;
	}

	/**
	 * <p>getSumDistinctLiterals.</p>
	 *
	 * @return a double.
	 */
	public double getSumDistinctLiterals() {
		double sum = 0.0;
		for (final Entry e : selectivityOfInterval) {
			sum += e.distinctLiterals;
		}
		return sum;
	}

	/**
	 * <p>multiplySelectivities.</p>
	 *
	 * @param d a double.
	 */
	public void multiplySelectivities(final double d) {
		for (final Entry e : selectivityOfInterval) {
			e.selectivity *= d;
			e.distinctLiterals *= d;
		}
	}

	/**
	 * <p>multiplySelectivities.</p>
	 *
	 * @param vb a {@link lupos.optimizations.logical.statistics.VarBucket} object.
	 * @param factor a int.
	 */
	public void multiplySelectivities(final VarBucket vb, final int factor) {
		Literal lowerLimit = null;
		boolean first = true;
		int index = 0;
		final List<Entry> le = new ArrayList<Entry>(selectivityOfInterval
				.size());
		for (final Entry e : selectivityOfInterval) {
			final Tuple<Integer, Double> result;
			if (first) {
				result = vb.getEstimatedSelectivity(this, e, lowerLimit);
				first = false;
			} else
				result = vb.getEstimatedSelectivity(this, e, lowerLimit, index);
			final double estSel = result.getSecond();
			index = result.getFirst() - 1;
			final double div = (factor == 0) ? estSel : estSel / factor;
			e.selectivity *= div;
			e.distinctLiterals *= div;
			lowerLimit = e.literal;
			if (e.selectivity > 0.0)
				le.add(e);
		}
		selectivityOfInterval = le;
	}

	private int searchLowerLimit(final Literal literal, final int left,
			final int right) {
		if (right - left == 0) {
			final int comp = this.selectivityOfInterval.get(left).literal
					.compareToNotNecessarilySPARQLSpecificationConform(literal);
			if (comp == 0 || comp < 0)
				return left;
			else
				// if(comp>0)
				return left - 1;
		} else if (right - left == 1) {
			final int comp = this.selectivityOfInterval.get(left).literal
					.compareToNotNecessarilySPARQLSpecificationConform(literal);
			if (comp == 0)
				return left;
			else if (comp > 0)
				return left - 1;
			return right;
		} else {
			final int middle = (left + right) / 2;
			final int comp = this.selectivityOfInterval.get(middle).literal
					.compareToNotNecessarilySPARQLSpecificationConform(literal);
			if (comp == 0) {
				return middle;
			} else if (comp < 0) {
				return searchLowerLimit(literal, middle + 1, right);
			} else {
				return searchLowerLimit(literal, left, middle - 1);
			}
		}
	}

	/**
	 * <p>getEstimatedSelectivity.</p>
	 *
	 * @param vb a {@link lupos.optimizations.logical.statistics.VarBucket} object.
	 * @param e a {@link lupos.optimizations.logical.statistics.Entry} object.
	 * @param lowerLimit a {@link lupos.datastructures.items.literal.Literal} object.
	 * @return a {@link lupos.misc.Tuple} object.
	 */
	protected Tuple<Integer, Double> getEstimatedSelectivity(
			final VarBucket vb, final Entry e, final Literal lowerLimit) {
		double result = 0.0;
		int overlappedInterval = -1;
		if (lowerLimit != null) {
			// first determine the interval, which overlaps with the lower
			// limit
			overlappedInterval = searchLowerLimit(lowerLimit, 0,
					this.selectivityOfInterval.size() - 1);

			final Literal lowerLimit2 = (overlappedInterval < 0) ? null
					: this.selectivityOfInterval.get(overlappedInterval).literal;
			if (overlappedInterval + 1 < this.selectivityOfInterval.size()) {
				final Entry e3 = this.selectivityOfInterval
						.get(overlappedInterval + 1);
				if (e3.literal
						.compareToNotNecessarilySPARQLSpecificationConform(lowerLimit) > 0) {
					int intervalSize;
					if (lowerLimit2 == null)
						intervalSize = distance(e3.literal, null);
					else
						intervalSize = distance(e3.literal, lowerLimit2);
					int fraction;
					// does e3 completely contain e?
					if (e3.literal
							.compareToNotNecessarilySPARQLSpecificationConform(e.literal) > 0) {
						fraction = distance(e.literal, lowerLimit);
						double sel = (e3.distinctLiterals == 0.0) ? e3.selectivity
								: e3.selectivity / e3.distinctLiterals;
						sel *= (intervalSize == 0) ? fraction
								: (double) fraction / intervalSize;
						return new Tuple<Integer, Double>(
								overlappedInterval + 1, sel);
					} else {
						fraction = distance(e3.literal, lowerLimit);
					}
					double sel = (e3.distinctLiterals == 0.0) ? e3.selectivity
							: e3.selectivity / e3.distinctLiterals;
					sel *= (intervalSize == 0) ? fraction : (double) fraction
							/ intervalSize;
					result = sel;
				}
			}
		}

		// then all intervals, which are completely contained in the given
		// interval
		Literal lowerLimit2 = null;
		int index = overlappedInterval + 1;
		boolean next = (lowerLimit == null);
		while (index < this.selectivityOfInterval.size()) {
			final Entry e3 = this.selectivityOfInterval.get(index);
			if (next) {
				final int comp = e3.literal
						.compareToNotNecessarilySPARQLSpecificationConform(e.literal);
				if (comp > 0) {
					// at last the interval, which overlaps with the upper
					// limit
					int intervalSize;
					if (lowerLimit2 == null)
						intervalSize = distance(e3.literal, null);
					else
						intervalSize = distance(e3.literal, lowerLimit2);
					final int fraction = distance(e.literal, lowerLimit);
					double sel = (e3.distinctLiterals == 0.0) ? e3.selectivity
							: e3.selectivity / e3.distinctLiterals;
					sel *= (intervalSize == 0) ? fraction : (double) fraction
							/ intervalSize;
					result *= sel;
					break;
				} else {
					result = (e3.distinctLiterals == 0) ? result
							+ e3.selectivity : result
							+ (e3.selectivity / e3.distinctLiterals);
				}
				if (comp == 0)
					break;
			} else if (e3.literal
					.compareToNotNecessarilySPARQLSpecificationConform(lowerLimit) > 0) {
				next = true;
			}
			lowerLimit2 = e3.literal;
			index++;
		}

		return new Tuple<Integer, Double>(index, result);
	}

	/**
	 * <p>getEstimatedSelectivity.</p>
	 *
	 * @param vb a {@link lupos.optimizations.logical.statistics.VarBucket} object.
	 * @param e a {@link lupos.optimizations.logical.statistics.Entry} object.
	 * @param lowerLimit a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param overlappedInterval a int.
	 * @return a {@link lupos.misc.Tuple} object.
	 */
	protected Tuple<Integer, Double> getEstimatedSelectivity(
			final VarBucket vb, final Entry e, final Literal lowerLimit,
			int overlappedInterval) {
		double result = 0.0;
		if (lowerLimit != null) {
			while (overlappedInterval + 1 < this.selectivityOfInterval.size()) {
				// first determine the interval, which overlaps with the
				// lower
				// limit

				final Literal lowerLimit2 = (overlappedInterval < 0) ? null
						: this.selectivityOfInterval.get(overlappedInterval).literal;
				final Entry e3 = this.selectivityOfInterval
						.get(overlappedInterval + 1);
				if (e3.literal
						.compareToNotNecessarilySPARQLSpecificationConform(lowerLimit) > 0) {
					int intervalSize;
					if (lowerLimit2 == null)
						intervalSize = distance(e3.literal, null);
					else
						intervalSize = distance(e3.literal, lowerLimit2);
					int fraction;
					// does e3 completely contain e?
					if (e3.literal
							.compareToNotNecessarilySPARQLSpecificationConform(e.literal) > 0) {
						fraction = distance(e.literal, lowerLimit);
						double sel = (e3.distinctLiterals == 0.0) ? e3.selectivity
								: e3.selectivity / e3.distinctLiterals;
						sel *= (intervalSize == 0) ? fraction
								: (double) fraction / intervalSize;
						return new Tuple<Integer, Double>(
								overlappedInterval + 1, sel);
					} else {
						fraction = distance(e3.literal, lowerLimit);
					}
					final double sel = (e3.distinctLiterals == 0.0) ? e3.selectivity
							: e3.selectivity / e3.distinctLiterals;
					result = sel
							* ((intervalSize == 0) ? fraction
									: (double) fraction / intervalSize);
					break;
				}
				overlappedInterval++;
			}
		}

		// then all intervals, which are completely contained in the given
		// interval
		Literal lowerLimit2 = null;
		int index = overlappedInterval + 1;
		boolean next = (lowerLimit == null);
		while (index < this.selectivityOfInterval.size()) {
			final Entry e3 = this.selectivityOfInterval.get(index);
			if (next) {
				final int comp = e3.literal
						.compareToNotNecessarilySPARQLSpecificationConform(e.literal);
				if (comp > 0) {
					// at last the interval, which overlaps with the upper
					// limit
					int intervalSize;
					if (lowerLimit2 == null)
						intervalSize = distance(e3.literal, null);
					else
						intervalSize = distance(e3.literal, lowerLimit2);
					final int fraction = distance(e.literal, lowerLimit);
					double sel = (e3.distinctLiterals == 0.0) ? e3.selectivity
							: e3.selectivity / e3.distinctLiterals;
					sel *= (intervalSize == 0) ? fraction : fraction
							/ intervalSize;
					result += sel;
					break;
				} else {
					result += (e3.distinctLiterals == 0.0) ? e3.selectivity
							: e3.selectivity / e3.distinctLiterals;
				}
				if (comp == 0)
					break;
			} else if (e3.literal
					.compareToNotNecessarilySPARQLSpecificationConform(lowerLimit) > 0) {
				next = true;
			}
			lowerLimit2 = e3.literal;
			index++;
		}

		return new Tuple<Integer, Double>(index, result);
	}

	/**
	 * <p>distance.</p>
	 *
	 * @param la a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param lb a {@link lupos.datastructures.items.literal.Literal} object.
	 * @return a int.
	 */
	public int distance(final Literal la, final Literal lb) {
		if (la == null) {
			if (lb == null)
				return 0;
			else
				return distance(lb, la);
		}
		if (la instanceof LazyLiteral) {
			if (lb == null)
				return ((LazyLiteral) la).getCode();
			else
				return Math.abs(((LazyLiteral) la).getCode()
						- ((LazyLiteral) lb).getCode());
		} else {
			final String a = (la == null) ? "" : la.toString();
			final String b = (lb == null) ? "" : lb.toString();

			for (int i = 0; i < a.length(); i++) {
				if (i + 1 > b.length())
					return a.charAt(i);
				if (a.charAt(i) != b.charAt(i)) {
					return Math.abs(b.charAt(i) - a.charAt(i));
				}
			}
			return 0;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object clone() {
		final VarBucket vb = new VarBucket();
		vb.maximum = this.maximum;
		vb.minimum = this.minimum;
		for (final Entry e : selectivityOfInterval) {
			vb.selectivityOfInterval.add((Entry) e.clone());
		}
		return vb;
	}

	/**
	 * <p>add.</p>
	 *
	 * @param vb a {@link lupos.optimizations.logical.statistics.VarBucket} object.
	 */
	public void add(final VarBucket vb) {
		Literal lowerLimit = null;
		boolean first = true;
		int index = 0;
		for (final Entry e : selectivityOfInterval) {
			final Tuple<Integer, Double> result;
			if (first) {
				result = vb.getEstimatedSelectivity(this, e, lowerLimit);
				first = false;
			} else
				result = vb.getEstimatedSelectivity(this, e, lowerLimit, index);
			final double estSel = result.getSecond();
			index = result.getFirst();
			e.selectivity *= estSel;
			e.distinctLiterals *= estSel;
			lowerLimit = e.literal;
		}
	}
};
