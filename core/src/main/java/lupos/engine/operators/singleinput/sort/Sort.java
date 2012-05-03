/**
 *
 */
package lupos.engine.operators.singleinput.sort;

import java.util.Collection;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings;

/**
 * This is almost an abstract class, but as it needs to be
 * instantiated in operatorPipe it is not. Nevertheless it should not be
 * instantiated, as no useful results will be created. 
 * DO ONLY USE EXTENDING CLASSES
 */
public class Sort extends SingleInputOperator {

	protected ComparatorBindings comparator; // Comparator which compares in order to sort

	public Sort() {
	}

	/**
	 * Contructor
	 * 
	 * @param node
	 *            the current sort node. From this node all other informations
	 *            like variables to sort after will be extracted.
	 */
	public Sort(final lupos.sparql1_1.Node node) {
		comparator = new ComparatorAST(node);
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
		comparator = ((Sort) op).getComparator();
	}

	public ComparatorBindings getComparator() {
		return comparator;
	}

	public void setComparator(ComparatorBindings comparator) {
		this.comparator=comparator;
	}

	@Override
	public String toString() {
		return super.toString() + "\nSortcriterium:" + getSortCriterium();
	}

	public Collection<Variable> getSortCriterium() {
		return getComparator().getSortCriterium();
	}
}
