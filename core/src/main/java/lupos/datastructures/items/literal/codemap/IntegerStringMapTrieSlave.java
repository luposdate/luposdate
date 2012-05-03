package lupos.datastructures.items.literal.codemap;

import java.util.Map.Entry;

import lupos.datastructures.trie.TrieMap;

public class IntegerStringMapTrieSlave implements IntegerStringMap {

	protected final TrieMap<Integer> trieMap;
	protected int[] indexMap;

	public IntegerStringMapTrieSlave(final TrieMap<Integer> trieMap){
		this.trieMap=trieMap;
		updateIndexMap();
	}

	private void updateIndexMap(){
		indexMap=new int[trieMap.size()];
		Integer[] objectArray=trieMap.getObjectArray();
		for(int index=0;index<objectArray.length;index++){
			indexMap[objectArray[index]-1]=index;
		}
	}

	public String get(int key) {
		return trieMap.getTrie().get(indexMap[key-1]);
	}

	public void put(int key, String s) {
		// this is the slave
		// => we assume that trieMap has been updated before correspondingly
		// => only update our indexMap according to trieMap!
		updateIndexMap();
	}

	public void clear() {
		trieMap.clear();
		updateIndexMap();
	}

	public int size() {
		return trieMap.size();
	}

	public void forEachValue(TProcedureValue<String> arg0) {
		for(String value: trieMap.getTrie())
			arg0.execute(value);
	}

	public void forEachEntry(TProcedureEntry<Integer, String> arg0) {
		for (final Entry<String,Integer> entry : trieMap.entrySet()) {
			arg0.execute(entry.getValue(), entry.getKey());
		}
	}
}
