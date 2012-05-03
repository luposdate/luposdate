package lupos.datastructures.items.literal.codemap;

import java.util.concurrent.locks.ReentrantLock;

public class IntegerStringMapLock implements IntegerStringMap {

	private final ReentrantLock lock;
	private final IntegerStringMap map;

	public IntegerStringMapLock(final ReentrantLock lock, final IntegerStringMap map){
		this.lock=lock;
		this.map=map;
	}

	public String get(int key) {
		lock.lock();
		try{
			return map.get(key);
		}finally{
			lock.unlock();
		}
	}

	public void put(int key, String s) {
		lock.lock();
		try{
			map.put(key, s);
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

	public void forEachValue(TProcedureValue<String> arg0) {
		lock.lock();
		try{
			map.forEachValue(arg0);
		}finally{
			lock.unlock();
		}
	}

	public void forEachEntry(TProcedureEntry<Integer, String> arg0) {
		lock.lock();
		try{
			map.forEachEntry(arg0);
		}finally{
			lock.unlock();
		}
	}
}
