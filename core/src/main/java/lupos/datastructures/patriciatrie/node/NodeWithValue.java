/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.datastructures.patriciatrie.node;

public abstract class NodeWithValue<T> extends Node {
	
	/** the values of leaf nodes (only for bags or maps) */
	private T[] values;


	public NodeWithValue() {
		this.setValues(null);
	}
	
	
	/**
	 * overrides method of Node because of more precise return type
	 */
	@Override
	protected abstract NodeWithValue<T> createNode();


	/**
	 * overrides method of Node because of more precise return type
	 */
	@Override
	public abstract NodeWithValue<T> getChild(int i);

	/**
	 * Sets the i-th element of the children array to the node. If necessary, the children array will be
	 * initialized first. It will not be initialized, if node is null.
	 * 
	 * @param i
	 *            Array index of the node
	 * @param node
	 *            Node instance to be stored
	 */
	protected abstract void setChild(int i, NodeWithValue<T> node);

	@SuppressWarnings("unchecked")
	@Override
	protected void setChild(int i, Node node){
		this.setChild(i, (NodeWithValue<T>) node);
	}

	/**
	 * @param i
	 *            Array index of the value
	 * @return The value if available, <strong>null</strong> otherwise
	 */
	public T getValue(final int i) {
		if (this.getValues() != null){
			return this.getValues()[i];
		} else {
			return null;
		}
	}

	/**
	 * Sets the i-th element of the values array to the value. If necessary, the values array will be
	 * initialized first.
	 * 
	 * @param i
	 *            Array index of the value
	 * @param value
	 *            Value to be stored
	 */
	@SuppressWarnings("unchecked")
	public void setValue(final int i, final T value) {
		if (this.getValues() == null){
			this.setValues((T[]) new Object[1]);
		}

		this.getValues()[i] = value;
		
		this.setChanged(true);
	}
	
	/**
	 * Increases the size of the values array by inserting <i>amount</i> null elements, starting at the given index
	 * 
	 * @param idx
	 *            Index for the new element
	 * @param amount
	 *            Amount of entries to add
	 */
	protected void increaseValuesArraySize(final int idx, final int amount) {
		@SuppressWarnings("unchecked")
		final T[] newValues = (T[]) new Object[this.getValuesLength() + amount];

		if (this.getValuesLength() > 0) {
			System.arraycopy(this.getValues(), 0, newValues, 0, idx);
			System.arraycopy(this.getValues(), idx, newValues, idx + amount, this.getValuesLength() - idx - amount + 1);
		}

		for (int i = idx; i < idx + amount; i++){
			newValues[i] = null;
		}

		this.setValues(newValues);
	}
	
	/**
	 * Decreases the size of the values array by removing the <i>idx</i>-th element.
	 * 
	 * @param idx
	 *            Index of the element to remove
	 */
	protected void removeValuesArrayElement(final int idx) {
		if (this.getValuesLength() > 1) {
			@SuppressWarnings("unchecked")
			final T[] newValues = (T[]) new Object[this.getValuesLength() - 1];
			
			System.arraycopy(this.getValues(), 0, newValues, 0, idx);
			System.arraycopy(this.getValues(), idx + 1, newValues, idx, this.getValuesLength() - idx - 1);
			
			this.setValues(newValues);
		} else {
			this.setValues(null);
		}
	}	

	/**
	 * Increases the size of the content and the children array by <i>amount</i>.
	 * 
	 * @param idx
	 *            Index for the new element
	 * @param amount
	 *            Amount of entries to add
	 */
	@Override
	protected void increaseArraySizes(final int idx, final int amount) {
		super.increaseArraySizes(idx, amount);
		this.increaseValuesArraySize(idx, amount);
	}
	
	/**
	 * Decreases the size of the content and the children array by 1.
	 * 
	 * @param idx
	 *            Index of the element to remove
	 */
	@Override
	protected void decreaseArraySizes(final int idx) {
		super.decreaseArraySizes(idx);
		this.removeValuesArrayElement(idx);
	}
	
	/**
	 * @return The length of the values array if it is initialized, 0 otherwise
	 */
	public int getValuesLength() {
		return (this.getValues() == null ? 0 : this.getValues().length);
	}
	
	@Override
	public String toString() {
		return NodeHelper.toString(this, "");
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof NodeWithValue)){
			return false;
		}
				
		@SuppressWarnings("unchecked")
		final NodeWithValue<T> node = (NodeWithValue<T>) obj;
		
		boolean superresult = super.equals(obj);
		
		if(superresult){
			// Values all equal?
			for (int i = 0; i < this.getValuesLength(); i++){
				if (!this.getValue(i).equals(node.getValue(i))){
					return false;
				}
			}
			
			return true;
			
		} else {
			return false;
		}
	}


	/**
	 * @param values the values to set
	 */
	public void setValues(T[] values) {
		this.values = values;
	}


	/**
	 * @return the values
	 */
	public T[] getValues() {
		return this.values;
	}
}
