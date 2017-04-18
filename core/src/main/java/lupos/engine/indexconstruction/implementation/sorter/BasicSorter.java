package lupos.engine.indexconstruction.implementation.sorter;

/**
 * Quicksort for sorting a 'block' according to the secondary and tertiary sort criteria.
 * A 'block' is a set of triples with the same id at the primary position.
 */
public class BasicSorter extends Thread {

	private final int[][] blockOfSortedIdTriples;
	private final int secondary_pos;
	private final int tertiary_pos;
	private final int[][] blockOfFinallySortedIdTriples;
	private final int start;
	private final int end;

	public BasicSorter(final int[][] blockOfSortedIdTriples, final int[][] blockOfFinallySortedIdTriples, final int secondary_pos, final int tertiary_pos, final int start, final int end) {
		this.blockOfSortedIdTriples = blockOfSortedIdTriples;
		this.blockOfFinallySortedIdTriples = blockOfFinallySortedIdTriples;
		this.secondary_pos = secondary_pos;
		this.tertiary_pos = tertiary_pos;
		this.start = start;
		this.end = end;
	}

	@Override
	public void run(){
		System.arraycopy(this.blockOfSortedIdTriples, this.start, this.blockOfFinallySortedIdTriples, this.start, this.end - this.start);
		this.quicksort(this.start, this.end - 1);
	}

	private void quicksort(final int low, final int high) {
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		final int[] pivot = this.blockOfFinallySortedIdTriples[low + (high-low)/2];

		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is smaller then the pivot
			// element then get the next element from the left list.
			// Check primary and tertiary sort condition!
			while (this.blockOfFinallySortedIdTriples[i][this.secondary_pos] < pivot[this.secondary_pos] ||
					(this.blockOfFinallySortedIdTriples[i][this.secondary_pos] == pivot[this.secondary_pos] &&
							this.blockOfFinallySortedIdTriples[i][this.tertiary_pos] < pivot[this.tertiary_pos])) {
				i++;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list.
			// Check primary and tertiary sort condition!
			while (this.blockOfFinallySortedIdTriples[j][this.secondary_pos] > pivot[this.secondary_pos] ||
					(this.blockOfFinallySortedIdTriples[j][this.secondary_pos] == pivot[this.secondary_pos] &&
					this.blockOfFinallySortedIdTriples[j][this.tertiary_pos] > pivot[this.tertiary_pos])) {
				j--;
			}

			// If we have found a values in the left list which is larger then
			// the pivot element and if we have found a value in the right list
			// which is smaller then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				final int[] tmp = this.blockOfFinallySortedIdTriples[i];
				this.blockOfFinallySortedIdTriples[i] = this.blockOfFinallySortedIdTriples[j];
				this.blockOfFinallySortedIdTriples[j] =tmp;
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j){
			this.quicksort(low, j);
		}
		if (i < high){
			this.quicksort(i, high);
		}
	}
}
