/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.datastructures.patriciatrie.ram;

import lupos.datastructures.patriciatrie.node.Node;

/**
 * This class extends the abstract Node class and implements a RAM based behavior.
 */
public class RBNode extends Node {

	/** Pointer to the children */
	protected Node[] children;
	
	/**
	 * Constructor
	 */
	public RBNode() {
		super();
		this.children = null;
	}
	
	@Override
	protected final Node createNode() {
		return new RBNode();
	}
	
	@Override
	public final boolean hasChild(final int i) {
		return this.children != null && i < this.children.length && this.children[i] != null;
	}
	
	@Override
	public final Node getChild(final int i) {
		if (this.children != null){
			return this.children[i];
		} else {
			return null;
		}
	}

	@Override
	public final void setChild(final int i, final Node node) {
		if (this.children == null && node != null) {
			this.children = new Node[this.getContentLength()];
			
			for (int j = 0; j < this.children.length; j++)
				this.children[j] = null;
		}
		
		if (this.children != null){
			this.children[i] = node;
		}
	}
	
	@Override
	protected final boolean isFromSameTrie(final Node node) {
		if (node instanceof RBNode) {
			// System.out.println("fixme: RBNode.isFromSameTrie");
			// node can be reused
			return true;
		} else {
			return false;
		}
	}
		
	@Override
	protected final void increaseChildrenArraySize(final int idx, final int amount) {
		if (this.children != null) {
			final Node[] newChildren = new Node[this.getChildrenLength() + amount];
			
			if (this.getChildrenLength() > 0) {
				System.arraycopy(this.children, 0, newChildren, 0, idx);
				System.arraycopy(this.children, idx, newChildren, idx + amount, this.getChildrenLength() - idx - amount + 1);
				
			}

			for (int i = idx; i < idx + amount; i++)
				newChildren[i] = null;
			
			this.children = newChildren;
		}
	}
	
	@Override
	protected final void removeChildrenArrayElement(final int idx) {
		if (this.children != null) {
			if (this.getChildrenLength() > 1) {
				final Node[] newContent = new Node[this.getChildrenLength() - 1];
				
				System.arraycopy(this.children, 0, newContent, 0, idx);
				System.arraycopy(this.children, idx + 1, newContent, idx, this.getChildrenLength() - idx - 1);
				
				this.children = newContent;
			}
			else
				this.children = null;
		}
	}
	
	@Override
	public final int getChildrenLength() {
		return (this.children == null ? 0 : this.children.length);
	}
}
