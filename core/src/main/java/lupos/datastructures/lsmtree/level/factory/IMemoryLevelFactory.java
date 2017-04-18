package lupos.datastructures.lsmtree.level.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.memory.IMemoryLevel;
import lupos.datastructures.lsmtree.level.memory.MemoryLevelLazySorting;
import lupos.datastructures.lsmtree.level.memory.MemoryLevelTreeMap;

public interface IMemoryLevelFactory<K,V,R> {
	public IMemoryLevel<K,V,R> createMemoryLevel(ILevelFactory<K,V,R> levelFactory, int level, int MEMORYTHRESHOLD, Comparator<K> comparator);

	public static<K, V> IMemoryLevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> createMemoryLevelTreeMapFactory(){
		@SuppressWarnings("unchecked")
		final IMemoryLevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> memoryLevelFactory = createMemoryLevelFactory((Class<? extends IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>>>)MemoryLevelTreeMap.class);
		return memoryLevelFactory;
	}

	public static<K,V> IMemoryLevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> createMemoryLevelLazySortingFactory(){
		@SuppressWarnings("unchecked")
		final IMemoryLevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> memoryLevelFactory = createMemoryLevelFactory((Class<? extends IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>>>)MemoryLevelLazySorting.class);
		return memoryLevelFactory;
	}

	public static<K,V> IMemoryLevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> createMemoryLevelFactory(final Class<? extends IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>>> clazz){
		return new IMemoryLevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>>(){
			@Override
			public IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>> createMemoryLevel(final ILevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> levelFactory, final int level, final int MEMORYTHRESHOLD, final Comparator<K> comparator) {
				try {
					final Constructor<? extends IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>>> constructor = clazz.getDeclaredConstructor(ILevelFactory.class, int.class, int.class, Comparator.class);
					return constructor.newInstance(levelFactory, level, MEMORYTHRESHOLD, comparator);
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					System.err.println(e);
					e.printStackTrace();
				}
				return null;
			}
		};
	}
}