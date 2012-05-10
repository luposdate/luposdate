package lupos.gui.operatorgraph.visualeditor.queryeditor.util;

import java.util.HashSet;

import lupos.datastructures.items.Variable;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveDataWithSolutionModifier;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

public class SortContainer {
	private boolean desc = false;
	private String sortString = "";
	private Prefix prefix;
	private RetrieveDataWithSolutionModifier operator;

	public SortContainer(Prefix prefix, boolean desc, String sortString) throws ModificationException {
		this.prefix = prefix;
		this.desc = desc;

		this.setSortString(sortString);
	}

	public boolean isDesc() {
		return this.desc;
	}

	public void setDesc(boolean desc) {
		this.desc = desc;
	}

	public String getSortString() {
		return this.sortString;
	}

	public void setSortString(String sortString) throws ModificationException {
		if(!sortString.equals("")) {
			try {
				SPARQL1_1Parser.parseOrderCondition(sortString, this.prefix.getPrefixNames());

				this.sortString = sortString;
			}
			catch(Throwable t) {
				this.operator.handleParseError(t);
			}
		}
	}

	public StringBuffer serializeSortContainer() {
		StringBuffer ret = new StringBuffer();

		if(this.isDesc())
			ret.append("DESC(");

		if(!this.sortString.equals(""))
			ret.append(this.prefix.add(this.sortString));

		if(this.isDesc())
			ret.append(")");

		return ret;
	}

	public void setOperator(RetrieveDataWithSolutionModifier operator) {
		this.operator = operator;
	}

	public void getUsedVariables(HashSet<Variable> variables) {
		try {
			SimpleNode node = SPARQL1_1Parser.parseOrderCondition(this.sortString, this.prefix.getPrefixNames());

			Operator.computeUsedVariables(node, variables);
		}
		catch(ParseException e) {
			e.printStackTrace();
		}
	}
}