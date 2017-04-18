package lupos.datastructures.lsmtree.level;
/**
* Container to store value and removed marker for a key-value-pair
*
* @author Maike Herting
*
*/

public class Container<V>{

	/**
	* the value belonging to a certain key
	*/
	public final V value;

	/**
	* marker if key was removed
	*/
	public final boolean removed;

	/**
	* Constructor specifying the value and whether it was removed
	*
	* @param value
	* @param removed true if key was removed
	*/
	public Container(final V value, final boolean removed){
		this.removed = removed;
		this.value = value;
	}

	/**
	* Returns the value
	*
	* @param V a value
	*/
	public V getValue(){
		return this.value;
	}

	/**
	* Returns true if key was removed
	*
	*@return boolean
	*/
	public boolean isDeleted(){
		return this.removed;
	}

	/**
	* Returns the Container as a string for debugging
	*
	* @return String
	*/
	@Override
	public String toString(){
		return "("+(this.value==null?null:this.value.toString())+", "+(this.removed?"removed":"inserted")+")";
	}
}