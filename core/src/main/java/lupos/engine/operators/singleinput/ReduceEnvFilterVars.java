
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
package lupos.engine.operators.singleinput;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
public class ReduceEnvFilterVars extends SingleInputOperator {
	private List<Variable> substitutionsVariableLeft = new LinkedList<Variable>();
	private List<Variable> substitutionsVariableRight = new LinkedList<Variable>();
	private List<Literal> filterLeft = new LinkedList<Literal>();
	private List<Variable> filterRight = new LinkedList<Variable>();

	// Simulate
	// private Item[] valueOrVariable;

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		intersectionVariables = new HashSet<Variable>();
		unionVariables = new HashSet<Variable>();
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		for (final Variable i : substitutionsVariableLeft) {
			result.getVariables().add(i);
			intersectionVariables.add(i);
			unionVariables.add(i);
		}
		return result;
	}

	/**
	 * <p>Constructor for ReduceEnvFilterVars.</p>
	 */
	public ReduceEnvFilterVars() {
	}

	/*
	 * public Variable getVariable(Item right){ Variable var = null;
	 * if(right.isVariable()){ int index =
	 * substitutionsVariableRight.indexOf(right); if(index!=-1){ var =
	 * substitutionsVariableLeft.get(index); } } else{ int index =
	 * substitutionsLiteralRight.indexOf(right); if(index!=-1){ var =
	 * substitutionsLiteralLeft.get(index); } } return var; }
	 */

	/**
	 * <p>Getter for the field <code>filterRight</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Variable> getFilterRight() {
		return filterRight;
	}

	/**
	 * <p>Setter for the field <code>filterRight</code>.</p>
	 *
	 * @param filterRight a {@link java.util.List} object.
	 */
	public void setFilterRight(final List<Variable> filterRight) {
		this.filterRight = filterRight;
	}

	/**
	 * <p>Setter for the field <code>substitutionsVariableLeft</code>.</p>
	 *
	 * @param substitutionsVariableLeft a {@link java.util.List} object.
	 */
	public void setSubstitutionsVariableLeft(
			final List<Variable> substitutionsVariableLeft) {
		this.substitutionsVariableLeft = substitutionsVariableLeft;
	}

	/**
	 * <p>Setter for the field <code>substitutionsVariableRight</code>.</p>
	 *
	 * @param substitutionsVariableRight a {@link java.util.List} object.
	 */
	public void setSubstitutionsVariableRight(
			final List<Variable> substitutionsVariableRight) {
		this.substitutionsVariableRight = substitutionsVariableRight;
	}

	/**
	 * <p>Getter for the field <code>filterLeft</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Literal> getFilterLeft() {
		return filterLeft;
	}

	/**
	 * <p>Setter for the field <code>filterLeft</code>.</p>
	 *
	 * @param filterLeft a {@link java.util.List} object.
	 */
	public void setFilterLeft(final List<Literal> filterLeft) {
		this.filterLeft = filterLeft;
	}

	/**
	 * <p>Getter for the field <code>substitutionsVariableLeft</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Variable> getSubstitutionsVariableLeft() {
		return substitutionsVariableLeft;
	}

	/**
	 * <p>Getter for the field <code>substitutionsVariableRight</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Variable> getSubstitutionsVariableRight() {
		return substitutionsVariableRight;
	}

	/**
	 * <p>addSubstitution.</p>
	 *
	 * @param variable a {@link lupos.datastructures.items.Variable} object.
	 * @param content a {@link lupos.datastructures.items.Variable} object.
	 */
	public void addSubstitution(final Variable variable, final Variable content) {
		substitutionsVariableLeft.add(variable);
		substitutionsVariableRight.add(content);
	}

	/**
	 * <p>addFilter.</p>
	 *
	 * @param content a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param variable a {@link lupos.datastructures.items.Variable} object.
	 */
	public void addFilter(final Literal content, final Variable variable) {
		filterLeft.add(content);
		filterRight.add(variable);
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult oldBindings,
			final int operandID) {
		final QueryResult qr = QueryResult.createInstance();

		for (final Bindings oldBinding : oldBindings) {
			// Triple triple = getTriple();

			// final Bindings bindings = Bindings.createNewInstance();
			Literal literal = null;

			for (int i = 0; i < filterLeft.size(); i++) {
				// its value has to be equal to the corresponding value of
				// the triple pattern
				if (!filterLeft.get(i).getLiteral(null).valueEquals(
						oldBinding.get(filterRight.get(i)))) {
					return null;
				}
			}

			for (int i = 0; i < substitutionsVariableLeft.size(); i++) {
				// if the item is an unbound variable
				final Variable itemName = substitutionsVariableLeft.get(i);
				if ((literal = oldBinding.get(itemName)) == null) {
					oldBinding.add(itemName, oldBinding
							.get(substitutionsVariableRight.get(i)));
				}
				// if the item is a variable which is already bound
				// and the value differs from the value of the triple
				// which would be used as binding, a conflict was
				// detected
				else if (!literal.valueEquals(oldBinding
						.get(substitutionsVariableRight.get(i)))) {
					return null; // join within triple pattern!
				}
			}

			qr.add(oldBinding);
		}
		// bindings.addTriple(triple);
		return qr;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final String text = "ReduceEnvVars to (" + substitutionsVariableLeft
				+ "," + substitutionsVariableRight + ") \n Filter("
				+ filterLeft.toString() + "=" + filterRight + ")";
		// if(substitutionsLiteralLeft.size()>0) text +=
		// "\n "+substitutionsLiteralLeft;
		return text;
	}
}
