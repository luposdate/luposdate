package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.SortedMap;

public interface SortedMapOfCollections<K, V, CV extends Collection<V>> extends SortedMap<K, CV>, MapOfCollections<K, V, CV>{
}
