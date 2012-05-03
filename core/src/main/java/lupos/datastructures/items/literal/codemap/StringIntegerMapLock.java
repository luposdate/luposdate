package lupos.datastructures.items.literal.codemap;

import java.util.concurrent.locks.ReentrantLock;

public class StringIntegerMapLock implements StringIntegerMap{
	private final ReentrantLock lock;
	private final StringIntegerMap map;

	public StringIntegerMapLock(final ReentrantLock lock, final StringIntegerMap map){
		this.lock=lock;
		this.map=map;
	}

	public Integer get(String s) {
		lock.lock();
		try{
			return map.get(s);
		}finally{
			lock.unlock();
		}
	}

	public void put(String s, int value) {
		lock.lock();
		try{
			map.put(s, value);
		}finally{
			lock.unlock();
		}
	}

	public void clear() {
		lock.lock();
		try{
			map.clear();
		}finally{
			lock.unlock();
		}
	}

	public int size() {
		lock.lock();
		try{
			return map.size();
		}finally{
			lock.unlock();
		}
	}
}
