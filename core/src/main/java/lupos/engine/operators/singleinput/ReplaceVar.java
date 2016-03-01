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
package lupos.engine.operators.singleinput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.messages.BindingsFactoryMessage;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
public class ReplaceVar extends SingleInputOperator {
	private LinkedList<Variable> substitutionsVariableLeft = new LinkedList<Variable>();
	private LinkedList<Variable> substitutionsVariableRight = new LinkedList<Variable>();
	protected BindingsFactory bindingsFactory;

	/**
	 * <p>getReplacement.</p>
	 *
	 * @param v a {@link lupos.datastructures.items.Variable} object.
	 * @return a {@link lupos.datastructures.items.Variable} object.
	 */
	public Variable getReplacement(final Variable v) {
		for (int i = 0; i < this.substitutionsVariableLeft.size(); i++) {
			if (this.substitutionsVariableRight.get(i).equals(v)) {
				return this.substitutionsVariableLeft.get(i);
			}
		}
		return null;
	}

	/**
	 * <p>removeSubstitution.</p>
	 *
	 * @param left a {@link lupos.datastructures.items.Variable} object.
	 * @param right a {@link lupos.datastructures.items.Variable} object.
	 */
	public void removeSubstitution(final Variable left, final Variable right) {
		final LinkedList<Integer> pos = this.getPositions(this.substitutionsVariableLeft,
				left);
		int index;
		for (int i = 0; i < pos.size(); i++) {
			index = pos.get(i);
			if (this.substitutionsVariableRight.get(index).equals(right)) {
				this.substitutionsVariableLeft.remove(index);
				this.substitutionsVariableRight.remove(index);
				break;
			}
		}
	}

	/**
	 * <p>Setter for the field <code>substitutionsVariableLeft</code>.</p>
	 *
	 * @param substitutionsVariableLeft a {@link java.util.LinkedList} object.
	 */
	public void setSubstitutionsVariableLeft(
			final LinkedList<Variable> substitutionsVariableLeft) {
		this.substitutionsVariableLeft = substitutionsVariableLeft;
	}

	/**
	 * <p>Setter for the field <code>substitutionsVariableRight</code>.</p>
	 *
	 * @param substitutionsVariableRight a {@link java.util.LinkedList} object.
	 */
	public void setSubstitutionsVariableRight(
			final LinkedList<Variable> substitutionsVariableRight) {
		this.substitutionsVariableRight = substitutionsVariableRight;
	}

	/**
	 * <p>Getter for the field <code>substitutionsVariableLeft</code>.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<Variable> getSubstitutionsVariableLeft() {
		return this.substitutionsVariableLeft;
	}

	/**
	 * <p>Getter for the field <code>substitutionsVariableRight</code>.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<Variable> getSubstitutionsVariableRight() {
		return this.substitutionsVariableRight;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		this.intersectionVariables = new LinkedList<Variable>();
		this.intersectionVariables.addAll(this.substitutionsVariableLeft);
		this.unionVariables = this.intersectionVariables;
		msg.getVariables().clear();
		msg.getVariables().addAll(this.intersectionVariables);
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
		final LinkedList<Integer> indices = this.getPositions(
				this.substitutionsVariableLeft, left);
		if (!indices.isEmpty()) {
			for (int i = 0; i < indices.size(); i++) {
				if (this.substitutionsVariableRight.get(indices.get(i))
						.equals(right)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * <p>addSubstitution.</p>
	 *
	 * @param variable a {@link lupos.datastructures.items.Variable} object.
	 * @param content a {@link lupos.datastructures.items.Variable} object.
	 */
	public void addSubstitution(final Variable variable, final Variable content) {
		if (!this.contains(variable, content)) {
			this.substitutionsVariableLeft.add(variable);
			this.substitutionsVariableRight.add(content);
		}
	}

	/**
	 * <p>removeSubstitutionVars.</p>
	 *
	 * @param index a int.
	 */
	public void removeSubstitutionVars(final int index) {
		this.substitutionsVariableLeft.remove(index);
		this.substitutionsVariableRight.remove(index);
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BindingsFactoryMessage msg){
		this.bindingsFactory = msg.getBindingsFactory();
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult oldBindings,
			final int operandID) {
		return QueryResult.createInstance(new Iterator<Bindings>() {

			Iterator<Bindings> oldIt = oldBindings.oneTimeIterator();

			@Override
			public boolean hasNext() {
				return this.oldIt.hasNext();
			}

			@Override
			public Bindings next() {
				if (!this.hasNext()) {
					return null;
				}
				final Bindings oldBinding = this.oldIt.next();
				if (oldBindings == null) {
					return null;
				}
				final Bindings newBinding = ReplaceVar.this.bindingsFactory.createInstance();
				for (int i = 0; i < ReplaceVar.this.substitutionsVariableLeft.size(); i++) {

					final Variable itemName = ReplaceVar.this.substitutionsVariableLeft.get(i);
					// always do like this!
					newBinding.add(itemName, oldBinding
							.get(ReplaceVar.this.substitutionsVariableRight.get(i)));
				}
				return newBinding;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		});
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String result = super.toString() + "(";
		for (int i = 0; i < this.substitutionsVariableLeft.size(); i++) {
			if (i > 0) {
				result += ", ";
			}
			result += this.substitutionsVariableLeft.get(i) + "="
					+ this.substitutionsVariableRight.get(i);
		}
		return result + ")";
	}

	/** {@inheritDoc} */
	@Override
	public boolean remainsSortedData(final Collection<Variable> sortCriterium){
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<Variable> transformSortCriterium(final Collection<Variable> sortCriterium){
		for (final Variable var : new ArrayList<Variable>(sortCriterium)) {
			if (this.getSubstitutionsVariableRight().contains(var)) {
				sortCriterium.remove(var);
				sortCriterium.add(this.getSubstitutionsVariableLeft().get(this.getSubstitutionsVariableRight().indexOf(var)));
			}
		}
		return sortCriterium;
	}
}
