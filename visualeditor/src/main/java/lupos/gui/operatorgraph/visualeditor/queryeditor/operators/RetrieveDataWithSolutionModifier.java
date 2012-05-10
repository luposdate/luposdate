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

	protected RetrieveDataWithSolutionModifier(Prefix prefix) {
		super(prefix);
	}


	public int getLimitValue() {
		return this.limit;
	}

	public void setLimitValue(String limit) throws ModificationException {
		try {
			this.limit = Integer.parseInt(limit);
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}


	public int getOffsetValue() {
		return this.offset;
	}

	public void setOffsetValue(String offset) throws ModificationException {
		try {
			this.offset = Integer.parseInt(offset);
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}


	public LinkedList<SortContainer> getOrderByList() {
		return this.sortContainerList;
	}

	public void setNewOrderByList(LinkedList<SortContainer> list) {
		this.sortContainerList = list;
	}

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

	public void addOrderByElement(SortContainer sortContainer) {
		this.sortContainerList.add(sortContainer);
	}

	public void removeOrderByElement(int index) {
		if(index == this.sortContainerList.size()) {
			return;
		}

		this.sortContainerList.remove(index);
	}


	public StringBuffer serializeSolutionModifier() {
		StringBuffer ret = new StringBuffer();

		// ORDER BY
		if(this.sortContainerList.size() > 0) {
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