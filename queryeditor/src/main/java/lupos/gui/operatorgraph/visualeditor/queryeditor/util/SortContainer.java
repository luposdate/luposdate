
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
 *
 * @author groppe
 * @version $Id: $Id
 */
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

	/**
	 * <p>Constructor for SortContainer.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 * @param desc a boolean.
	 * @param sortString a {@link java.lang.String} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public SortContainer(Prefix prefix, boolean desc, String sortString) throws ModificationException {
		this.prefix = prefix;
		this.desc = desc;

		this.setSortString(sortString);
	}

	/**
	 * <p>isDesc.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDesc() {
		return this.desc;
	}

	/**
	 * <p>Setter for the field <code>desc</code>.</p>
	 *
	 * @param desc a boolean.
	 */
	public void setDesc(boolean desc) {
		this.desc = desc;
	}

	/**
	 * <p>Getter for the field <code>sortString</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSortString() {
		return this.sortString;
	}

	/**
	 * <p>Setter for the field <code>sortString</code>.</p>
	 *
	 * @param sortString a {@link java.lang.String} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
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

	/**
	 * <p>serializeSortContainer.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
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

	/**
	 * <p>Setter for the field <code>operator</code>.</p>
	 *
	 * @param operator a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveDataWithSolutionModifier} object.
	 */
	public void setOperator(RetrieveDataWithSolutionModifier operator) {
		this.operator = operator;
	}

	/**
	 * <p>getUsedVariables.</p>
	 *
	 * @param variables a {@link java.util.HashSet} object.
	 */
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
