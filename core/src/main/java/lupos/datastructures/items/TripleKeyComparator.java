package lupos.datastructures.items;

import java.util.Comparator;

public class TripleKeyComparator implements Comparator<TripleKey> {
	
	private final TripleComparator tripleComparator;
	
	public TripleKeyComparator(TripleComparator tripleComparator){
		this.tripleComparator = tripleComparator;
	}

	@Override
	public int compare(TripleKey o1, TripleKey o2) {
		return tripleComparator.compare(o1.getTriple(), o2.getTriple());
	}

}
