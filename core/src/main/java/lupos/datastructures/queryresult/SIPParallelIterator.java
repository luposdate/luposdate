package lupos.datastructures.queryresult;

public interface SIPParallelIterator<T, K> extends ParallelIterator<T> {
	public T next(K k);
}
