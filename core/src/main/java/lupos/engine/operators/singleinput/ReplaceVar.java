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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;

public class ReplaceVar extends SingleInputOperator {
	private LinkedList<Variable> substitutionsVariableLeft = new LinkedList<Variable>();
	private LinkedList<Variable> substitutionsVariableRight = new LinkedList<Variable>();

	public Variable getReplacement(final Variable v) {
		for (int i = 0; i < substitutionsVariableLeft.size(); i++) {
			if (substitutionsVariableRight.get(i).equals(v))
				return substitutionsVariableLeft.get(i);
		}
		return null;
	}

	public void removeSubstitution(final Variable left, final Variable right) {
		final LinkedList<Integer> pos = getPositions(substitutionsVariableLeft,
				left);
		int index;
		for (int i = 0; i < pos.size(); i++) {
			index = pos.get(i);
			if (substitutionsVariableRight.get(index).equals(right)) {
				substitutionsVariableLeft.remove(index);
				substitutionsVariableRight.remove(index);
				break;
			}
		}
	}

	public void setSubstitutionsVariableLeft(
			final LinkedList<Variable> substitutionsVariableLeft) {
		this.substitutionsVariableLeft = substitutionsVariableLeft;
	}

	public void setSubstitutionsVariableRight(
			final LinkedList<Variable> substitutionsVariableRight) {
		this.substitutionsVariableRight = substitutionsVariableRight;
	}

	public LinkedList<Variable> getSubstitutionsVariableLeft() {
		return substitutionsVariableLeft;
	}

	public LinkedList<Variable> getSubstitutionsVariableRight() {
		return substitutionsVariableRight;
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		intersectionVariables = new LinkedList<Variable>();
		intersectionVariables.addAll(substitutionsVariableLeft);
		unionVariables = intersectionVariables;
		msg.getVariables().clear();
		msg.getVariables().addAll(intersectionVariables);
		return msg;
	}

	private LinkedList<Integer> getPositions(final LinkedList<Variable> list,
			final Item item) {
		final LinkedList<Integer> pos = new LinkedList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(item)) {
				pos.add(i);
			}
		}
		return pos;
	}

	private boolean contains(final Item left, final Item right) {
		final LinkedList<Integer> indices = getPositions(
				substitutionsVariableLeft, left);
		if (indices.size() != 0) {
			for (int i = 0; i < indices.size(); i++) {
				if (substitutionsVariableRight.get(indices.get(i))
						.equals(right)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addSubstitution(final Variable variable, final Variable content) {
		if (!contains(variable, content)) {
			substitutionsVariableLeft.add(variable);
			substitutionsVariableRight.add(content);
		}
	}

	public void removeSubstitutionVars(final int index) {
		substitutionsVariableLeft.remove(index);
		substitutionsVariableRight.remove(index);
	}

	@Override
	public QueryResult process(final QueryResult oldBindings,
			final int operandID) {
		return QueryResult.createInstance(new Iterator<Bindings>() {

			Iterator<Bindings> oldIt = oldBindings.oneTimeIterator();

			public boolean hasNext() {
				return oldIt.hasNext();
			}

			public Bindings next() {
				if (!hasNext())
					return null;
				final Bindings oldBinding = oldIt.next();
				if (oldBindings == null)
					return null;
				final Bindings newBinding = Bindings.createNewInstance();
				for (int i = 0; i < substitutionsVariableLeft.size(); i++) {

					final Variable itemName = substitutionsVariableLeft.get(i);
					// always do like this!
					newBinding.add(itemName, oldBinding
							.get(substitutionsVariableRight.get(i)));
				}
				return newBinding;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		});
	}

	@Override
	public void cloneFrom(final BasicOperator bo) {
		super.cloneFrom(bo);
		if (bo instanceof ReplaceVar) {
			final ReplaceVar rv = (ReplaceVar) bo;
			this.substitutionsVariableLeft = (LinkedList<Variable>) rv.substitutionsVariableLeft
					.clone();
			this.substitutionsVariableRight = (LinkedList<Variable>) rv.substitutionsVariableRight
					.clone();
		}
	}

	@Override
	public String toString() {
		String result = super.toString() + "(";
		for (int i = 0; i < substitutionsVariableLeft.size(); i++) {
			if (i > 0)
				result += ", ";
			result += substitutionsVariableLeft.get(i) + "="
					+ substitutionsVariableRight.get(i);
		}
		return result + ")";
	}
	
	public boolean remainsSortedData(Collection<Variable> sortCriterium){
		return true;
	}
	
	public Collection<Variable> transformSortCriterium(Collection<Variable> sortCriterium){
		for (Variable var : new ArrayList<Variable>(sortCriterium))
			if (this.getSubstitutionsVariableRight().contains(var)) {
				sortCriterium.remove(var);
				sortCriterium.add(this.getSubstitutionsVariableLeft().get(this.getSubstitutionsVariableRight().indexOf(var)));
			}
		return sortCriterium;
	}
}
