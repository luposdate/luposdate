package lupos.engine.indexconstruction.interfaces;

import lupos.datastructures.patriciatrie.TrieSet;

public interface IDictionaryGenerator extends IJoiner {
	public void generateDictionary(final TrieSet final_trie);
}
