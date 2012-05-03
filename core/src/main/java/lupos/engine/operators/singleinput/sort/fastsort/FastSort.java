package lupos.engine.operators.singleinput.sort.fastsort;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class sorts its input according to presorting numbers or according to
 * the code of LazyLiterals.
 * 
 * It is used for the optimizations MergeJoinSort, MergeJoinSortSimple and
 * MergeJoinSortLazyLiteral
 * 
 * @author groppe
 * 
 */
public abstract class FastSort extends SingleInputOperator {

	protected List<TriplePattern> triplePatterns;
	protected Collection<Variable> sortCriterium;

	public FastSort(final List<TriplePattern> triplePatterns,
			final Collection<Variable> sortCriterium) {
		super();
		this.triplePatterns = new LinkedList<TriplePattern>();
		for (final TriplePattern tp : triplePatterns) {
			for (final Variable v : sortCriterium) {
				if (tp.getVariables().contains(v)) {
					this.triplePatterns.add(tp);
					break;
				}
			}
		}
		this.sortCriterium = sortCriterium;
	}

	public Collection<Variable> getSortCriterium() {
		return sortCriterium;
	}

	@Override
	public String toString() {
		return super.toString() + " on " + this.sortCriterium;
	}

	public static FastSort createInstance(final BasicOperator root,
			final List<TriplePattern> triplePatterns,
			final Collection<Variable> sortCriterium) {
		if (LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERAL
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
			return new FastSortLazyLiteral(root, triplePatterns, sortCriterium);
		} else {
			System.err.println("FastSort: Not supported literal type!");
			return null;
//			return new FastSortPresortingNumbers(triplePatterns, sortCriterium,
//					presortion);
		}
	}
}
