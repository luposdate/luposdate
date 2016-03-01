/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;
import java.util.LinkedList;

import lupos.datastructures.items.Variable;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.RetrieveDataPanel;
import lupos.gui.operatorgraph.visualeditor.queryeditor.util.SortContainer;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
public abstract class RetrieveDataWithSolutionModifier extends RetrieveData {
	private LinkedList<SortContainer> sortContainerList = new LinkedList<SortContainer>();
	private int offset = -1;
	private int limit = -1;

	/**
	 * <p>Constructor for RetrieveDataWithSolutionModifier.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 */
	protected RetrieveDataWithSolutionModifier(Prefix prefix) {
		super(prefix);
	}


	/**
	 * <p>getLimitValue.</p>
	 *
	 * @return a int.
	 */
	public int getLimitValue() {
		return this.limit;
	}

	/**
	 * <p>setLimitValue.</p>
	 *
	 * @param limit a {@link java.lang.String} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public void setLimitValue(String limit) throws ModificationException {
		try {
			this.limit = Integer.parseInt(limit);
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}


	/**
	 * <p>getOffsetValue.</p>
	 *
	 * @return a int.
	 */
	public int getOffsetValue() {
		return this.offset;
	}

	/**
	 * <p>setOffsetValue.</p>
	 *
	 * @param offset a {@link java.lang.String} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public void setOffsetValue(String offset) throws ModificationException {
		try {
			this.offset = Integer.parseInt(offset);
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}


	/**
	 * <p>getOrderByList.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<SortContainer> getOrderByList() {
		return this.sortContainerList;
	}

	/**
	 * <p>setNewOrderByList.</p>
	 *
	 * @param list a {@link java.util.LinkedList} object.
	 */
	public void setNewOrderByList(LinkedList<SortContainer> list) {
		this.sortContainerList = list;
	}

	/**
	 * <p>setOrderByElement.</p>
	 *
	 * @param index a int.
	 * @param sortContainer a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.util.SortContainer} object.
	 */
	public void setOrderByElement(int index, SortContainer sortContainer) {
		try {
			// new element...
			if(this.sortContainerList.size() == index) {
				this.sortContainerList.add(null);
			}

			// remove old value...
			if(this.sortContainerList.get(index) != null) {
				this.sortContainerList.remove(index);
			}

			// add new value...
			this.sortContainerList.add(index, sortContainer);

			sortContainer.setOperator(this);
		}
		catch(IndexOutOfBoundsException ioobe) {
			return;
		}
	}

	/**
	 * <p>addOrderByElement.</p>
	 *
	 * @param sortContainer a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.util.SortContainer} object.
	 */
	public void addOrderByElement(SortContainer sortContainer) {
		this.sortContainerList.add(sortContainer);
	}

	/**
	 * <p>removeOrderByElement.</p>
	 *
	 * @param index a int.
	 */
	public void removeOrderByElement(int index) {
		if(index == this.sortContainerList.size()) {
			return;
		}

		this.sortContainerList.remove(index);
	}


	/**
	 * <p>serializeSolutionModifier.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public StringBuffer serializeSolutionModifier() {
		StringBuffer ret = new StringBuffer();

		// ORDER BY
		if(!this.sortContainerList.isEmpty()) {
			StringBuffer innerBuffer = new StringBuffer();

			boolean orderByPresent = false;

			for(SortContainer sc : this.sortContainerList) {
				if(!orderByPresent && !sc.getSortString().equals("")) {
					orderByPresent = true;
				}

				innerBuffer.append(sc.serializeSortContainer() + " ");
			}

			if(orderByPresent && ((RetrieveDataPanel) this.panel).getElementStatus("orderBy")) {
				ret.append("ORDER BY ");
				ret.append(innerBuffer);
				ret.append("\n");
			}
		}

		// LIMIT
		if(this.limit > -1 && ((RetrieveDataPanel) this.panel).getElementStatus("limit")) {
			ret.append("LIMIT " + this.limit + "\n");
		}

		// OFFSET
		if(this.offset > 0 && ((RetrieveDataPanel) this.panel).getElementStatus("offset")) {
			ret.append("OFFSET " + this.offset + "\n");
		}

		return ret;
	}

	/** {@inheritDoc} */
	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		for(SortContainer sc : this.sortContainerList) {
			HashSet<Variable> variables = new HashSet<Variable>();

			sc.getUsedVariables(variables);

			if(variables.contains(new Variable(variable))) {
				return true;
			}
		}

		return false;
	}
}
