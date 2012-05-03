package lupos.datastructures.queryresult;

import java.util.Iterator;

public interface ParallelIterator<T> extends Iterator<T> {
	public void close();
}
