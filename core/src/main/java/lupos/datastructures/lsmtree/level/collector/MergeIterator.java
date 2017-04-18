package lupos.datastructures.lsmtree.level.collector;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Merges several runs into a new one that is sorted
 * 
 * @author Maike Herting
 */

public class MergeIterator<K,V> implements Iterator<Entry<K,V>>{

	/**
	 * Comparator that is used to compare keys
	 */
	protected Comparator<K> comp;
	
	/**
	 * Array to store the entries of the runs as iterators
	 */
	protected Iterator<Entry<K,V>>[] iterators;

	/**
	 * Array to help sorting the runs, works as a pointer
	 */
	protected Entry<K,V>[] currentValues;

	/**
	 * Constructor sets parameters and sets currentValue-pointer to first entries of runs
	 * 
	 * @param iterators iterator array of entries in runs
	 * @param comp a comparator that is used to compare keys
	 */
	@SuppressWarnings("unchecked")
	public MergeIterator(final Iterator<Entry<K,V>>[] iterators, final Comparator<K> comp){
		this.comp = comp;
		this.iterators = new Iterator[iterators.length];
		
		for(int i=0;i<iterators.length;i++){
			this.iterators[i] = iterators[i];
		}
		this.currentValues=  new Entry[iterators.length];
		for(int i=0;i<iterators.length;i++){
			this.currentValues[i]=this.iterators[i].next();
			
		}
	}

	/**
	* Checks if pointer is at the end of iterators
	*  
	* @return true if any iterator has a next entry
	*/
	@Override
	public boolean hasNext(){
		for(int i=0;i<this.currentValues.length;i++){
			if(this.currentValues[i]!=null){
				return true;
			}
		}
		return false;
	}

	/**
	* Returns the minimum in currenValues, which is the next entry of run with the smallest key
	* if key exist twice, the one from the run with the higher number is chosen and both currentValue-pointers increased
	* 
	* @return Entry<K, V> an Entry-Object consisting of key and value
	*/
	@Override
	public Entry<K,V> next(){

		Entry<K,V> min = this.currentValues[0];
		int minIndex=0;

		for(int i=1;i<this.iterators.length;i++){
			if(this.currentValues[i]==null){
				continue;
			}
	
			if(min==null || this.comp.compare(this.currentValues[i].getKey(),min.getKey())<0){
				min=this.currentValues[i];
				minIndex=i;
			}else if(this.comp.compare(this.currentValues[i].getKey(),min.getKey())==0){
				//replace with new value
				min=this.currentValues[i];
				//increase pointer of old value
				this.currentValues[minIndex] = this.iterators[minIndex].next();
				//set new minIndex
				minIndex=i;
			}
		}
		if(min!=null){
			this.currentValues[minIndex] = this.iterators[minIndex].hasNext()?this.iterators[minIndex].next():null;
		}
		return min;
	}

}