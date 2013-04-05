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

public class ReduceEnv extends SingleInputOperator {

	private List<Variable> letThrough = new LinkedList<Variable>();
	private final List<Variable> substitutionsVariableLeft = new LinkedList<Variable>();
	private final List<Variable> substitutionsVariableRight = new LinkedList<Variable>();
	private List<Variable> substitutionsLiteralLeft = new LinkedList<Variable>();
	private List<Literal> substitutionsLiteralRight = new LinkedList<Literal>();
	private List<Literal> filterLeft = new LinkedList<Literal>();
	private final List<Variable> filterRight = new LinkedList<Variable>();

	// Simulate
	// private Item[] valueOrVariable;

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
		for (final Variable i : substitutionsLiteralLeft) {
			result.getVariables().add(i);
			intersectionVariables.add(i);
			unionVariables.add(i);
		}
		return result;
	}

	public ReduceEnv() {
	}

	/*
	 * public Variable getVariable(Item right){ Variable var = null;
	 * if(right.isVariable()){ int index =
	 * substitutionsVariableRight.indexOf(right); if(index!=-1){ var =
	 * substitutionsVariableLeft.get(index); } } else{ int index =
	 * substitutionsLiteralRight.indexOf(right); if(index!=-1){ var =
	 * substitutionsLiteralLeft.get(index); } } return var; }
	 */

	public List<Literal> getFilterLeft() {
		return filterLeft;
	}

	public void setFilterLeft(final List<Literal> filterLeft) {
		this.filterLeft = filterLeft;
	}

	public List<Variable> getSubstitutionsLiteralLeft() {
		return substitutionsLiteralLeft;
	}

	public void setSubstitutionsLiteralLeft(
			final List<Variable> substitutionsLiteralLeft) {
		this.substitutionsLiteralLeft = substitutionsLiteralLeft;
	}

	public List<Literal> getSubstitutionsLiteralRight() {
		return substitutionsLiteralRight;
	}

	public void setSubstitutionsLiteralRight(
			final List<Literal> substitutionsLiteralRight) {
		this.substitutionsLiteralRight = substitutionsLiteralRight;
	}

	public List<Variable> getLetThrough() {
		return letThrough;
	}

	public void setLetThrough(final List<Variable> letThrough) {
		this.letThrough = letThrough;
	}

	public List<Variable> getSubstitutionsVariableLeft() {
		return substitutionsVariableLeft;
	}

	public List<Variable> getSubstitutionsVariableRight() {
		return substitutionsVariableRight;
	}

	public void addSubstitution(final Variable variable, final Variable content) {
		substitutionsVariableLeft.add(variable);
		substitutionsVariableRight.add(content);
	}

	public void addSubstitution(final Variable variable, final Literal content) {
		substitutionsLiteralLeft.add(variable);
		substitutionsLiteralRight.add(content);
	}

	public void addFilter(final Literal content, final Variable variable) {
		filterLeft.add(content);
		filterRight.add(variable);
	}

	@Override
	public QueryResult process(final QueryResult oldBindings,
			final int operandID) {
		final QueryResult qr = QueryResult.createInstance();

		for (final Bindings oldBinding : oldBindings) {
			// Triple triple = getTriple();

			final Bindings bindings = Bindings.createNewInstance();
			Literal literal = null;

			for (int i = 0; i < filterLeft.size(); i++) {
				// its value has to be equal to the corresponding value of
				// the triple pattern
				if (!filterLeft.get(i).getLiteral(null).valueEquals(
						oldBinding.get(filterRight.get(i)))) {
					return null;
				}
			}

			// process all items
			// for(int i = 0; i < 3; i++){
			for (int i = 0; i < substitutionsLiteralLeft.size(); i++) {
				// if the item is an unbound variable
				final Variable item = substitutionsLiteralLeft.get(i);
				if ((literal = bindings.get(item)) == null) {
					bindings.add(item, substitutionsLiteralRight.get(i));
				}
				// if the item is a variable which is already bound
				// and the value differs from the value of the triple
				// which would be used as binding, a conflict was
				// detected
				else if (!literal.valueEquals(substitutionsLiteralRight.get(i))) {
					return null; // join within triple pattern!
				}
			}

			for (int i = 0; i < substitutionsVariableLeft.size(); i++) {
				// if the item is an unbound variable
				final Variable item = substitutionsVariableLeft.get(i);
				if ((literal = bindings.get(item)) == null) {
					bindings.add(item, oldBinding
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

			qr.add(bindings);
		}
		// bindings.addTriple(triple);
		return qr;
	}

	/*
	 * private Bindings addAllLiteralSubstitutions(){ Bindings bnew =
	 * Bindings.createNewInstance(); //Process all literal substitutions for(int
	 * i=0; i<substitutionsLiteralLeft.size();i++){ Item left =
	 * substitutionsLiteralLeft.get(i); Literal right =
	 * substitutionsLiteralRight.get(i); if((!left.isVariable()) &&
	 * (!left.equals(right))){ return null; } //Add Tupel (left variable, right
	 * literal) else if(left.isVariable()){ bnew.add(left.toString(), right); }
	 * } return bnew; }
	 */

	@Override
	public String toString() {
		final String text = "Reduce to (" + substitutionsVariableLeft + ","
				+ substitutionsVariableRight + ")";
		// if(substitutionsLiteralLeft.size()>0) text +=
		// "\n "+substitutionsLiteralLeft;
		return text;
	}
}