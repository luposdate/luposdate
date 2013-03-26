package lupos.distributed.storage;

import lupos.datastructures.items.Triple;

public interface IStorage {
	public void startImportData();
	public void endImportData();
	public void addTriple(Triple triple);
	public boolean containsTriple(Triple triple);	
}
