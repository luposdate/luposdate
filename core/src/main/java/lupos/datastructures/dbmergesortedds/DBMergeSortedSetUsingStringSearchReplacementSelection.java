package lupos.datastructures.dbmergesortedds;

import java.util.Comparator;
import java.util.Iterator;

public class DBMergeSortedSetUsingStringSearchReplacementSelection extends
		DBMergeSortedSetUsingStringSearch {

	private String nextToRemove = null;

	public DBMergeSortedSetUsingStringSearchReplacementSelection(
			final int heapHeight, final Class<? extends String> classOfElements){
		super(heapHeight, classOfElements);
	}

	public DBMergeSortedSetUsingStringSearchReplacementSelection(){
		super();
	}

	public DBMergeSortedSetUsingStringSearchReplacementSelection(
			final Class<? extends String> classOfElements){
		super(classOfElements);
	}

	public DBMergeSortedSetUsingStringSearchReplacementSelection(
			final int heapHeight, final Comparator<? super String> comp,
			final Class<? extends String> classOfElements){
		super(heapHeight, comp, classOfElements);
	}

	public DBMergeSortedSetUsingStringSearchReplacementSelection(
			final Comparator<? super String> comp,
			final Class<? extends String> classOfElements){
		super(comp, classOfElements);
	}

	@Override
	public boolean add(final String ele) {

		if (currentRun != null) {
			final int compare = ele.compareTo(currentRun.max);
			if (compare == 0)
				return false;
			if (compare > 0) {
				if (nextToRemove == null || nextToRemove.compareTo(ele) > 0)
					nextToRemove = ele;
			}
		}

		if (searchtree.add(ele)) {
			size++;
			while (searchtree.isFull()) {
				if (nextToRemove == null) {
					if (currentRun == null)
						currentRun = Run.createInstance(this);
					else {
						closeAndNewCurrentRun();
					}
					final Iterator<String> it = searchtree.iterator();
					final String toRemove = it.next();
					nextToRemove = it.next();
					searchtree.remove(toRemove);
					currentRun.add(new Entry<String>(toRemove,
							new StandardComparator<String>(), n++));
				} else {
					currentRun.add(new Entry<String>(nextToRemove,
							new StandardComparator<String>(), n++));
					nextToRemove = searchtree
							.removeAndGetNextLargerOne(nextToRemove);
				}
			}
		}
		return true;
	}

}
