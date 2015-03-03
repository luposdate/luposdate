
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
package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;

import lupos.datastructures.items.Variable;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.OperatorPanel;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.JTFOperator;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;
public class Filter extends JTFOperator {
	private String filterExpression = "";

	/**
	 * <p>Constructor for Filter.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 */
	public Filter(Prefix prefix) {
		super(prefix);
	}

	/**
	 * <p>Constructor for Filter.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 * @param filterExpression a {@link java.lang.String} object.
	 */
	public Filter(Prefix prefix, String filterExpression) {
		super(prefix);

		this.filterExpression = filterExpression;
	}

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return this.filterExpression;
	}

	/** {@inheritDoc} */
	public void applyChange(String value) throws ModificationException {
		try {
			SPARQL1_1Parser.parseFilter( value, prefix.getPrefixNames());

			this.filterExpression = value;
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	/** {@inheritDoc} */
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new OperatorPanel(this, gw, parent, this.prefix.add(this.toString()), "Filter");

		return this.panel;
	}

	/**
	 * <p>serializeOperator.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public StringBuffer serializeOperator() {
		StringBuffer ret = new StringBuffer();
		ret.append("FILTER(" + this.filterExpression + ") .\n");

		return ret;
	}

	/** {@inheritDoc} */
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = this.serializeOperator();

		for(OperatorIDTuple<Operator> opIDt : this.succeedingOperators)
			ret.append(opIDt.getOperator().serializeOperatorAndTree(visited));

		return ret;
	}

	/** {@inheritDoc} */
	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		if(visited.contains(this))
			return false;

		visited.add(this);

		try {
			HashSet<Variable> variables = new HashSet<Variable>();

			SimpleNode node = SPARQL1_1Parser.parseFilter("FILTER(" + this.filterExpression + ")", this.prefix.getPrefixNames());

			Operator.computeUsedVariables(node, variables);

			if(variables.contains(new Variable(variable)))
				return true;
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getOperator().variableInUse(variable, visited))
				return true;

		return false;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getXPrefID(){
		return "queryEditor_style_filter";
	}
}
