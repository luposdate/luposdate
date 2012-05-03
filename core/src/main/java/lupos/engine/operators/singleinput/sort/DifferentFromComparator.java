package lupos.engine.operators.singleinput.sort;

import java.util.Comparator;



public class DifferentFromComparator<E extends Comparable<E>> implements Comparator<E>{

	private Comparator<E> comp;
	
	/**
	 * Compares two given bindings as specified in SPARQL-specification
	 */
	
	public DifferentFromComparator( Comparator<E> comp ){
		this.comp=comp;
	}

	/**
	 * Compares two bindings considering SPARQL-specifications
	 * @param arg0 first Bindings to compare
	 * @param arg1 second Bindings to compare
	 * @return simlar to any other integer based compare method: <br>
	 * -1 if l0 < l1<br>
	 *  1 if l0 > l1<br>
	 *  0 if l0 = l1<br>
	 *  but modified, as result will be multiplicated by -1 if descending order has been chosen.
	 */
	public int compare(E arg0, E arg1) {
		
		int compare=comp.compare(arg0, arg1);
		if(compare==0) {
			int compare2=arg0.compareTo(arg1);
			return compare2;
		}
		else return compare;
	}
}
