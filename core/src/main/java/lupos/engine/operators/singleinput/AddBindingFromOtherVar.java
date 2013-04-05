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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;

public class AddBindingFromOtherVar extends SingleInputOperator {
	private Variable var;
	private Variable valueFromVar;

	public AddBindingFromOtherVar(final Variable var,
			final Variable valueFromVar) {
		this.var = var;
		this.valueFromVar = valueFromVar;
	}

	public AddBindingFromOtherVar() {
	}

	public void setVar(final Variable var) {
		this.var = var;
	}

	public void setOtherVar(final Variable valueFromVar) {
		this.valueFromVar = valueFromVar;
	}

	public Variable getVar() {
		return var;
	}

	public Variable getOtherVar() {
		return valueFromVar;
	}

	// bindings should contain exactly one element!
	@Override
	public QueryResult process(final QueryResult oldBindings,
			final int operandID) {
		return QueryResult.createInstance(new Iterator<Bindings>() {
			Iterator<Bindings> itb = oldBindings.oneTimeIterator();

			public boolean hasNext() {
				return itb.hasNext();
			}

			public Bindings next() {
				if (!itb.hasNext())
					return null;
				final Bindings oldBinding = itb.next();
				final Literal literal = oldBinding.get(var);
				if (literal == null) {
					oldBinding.add(var, oldBinding.get(valueFromVar));
				}
				// if the item is a variable which is already bound
				// and the value differs from the value of the triple
				// which would be used as binding, a conflict was
				// detected
				else if (!literal.valueEquals(oldBinding.get(valueFromVar))) {
					System.out
							.println("AddBindingFromOtherVar: Error: Variable "
									+ var + " was already bound!");
					return null;
				}
				return oldBinding;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		});
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		msg.getVariables().add(var);
		intersectionVariables = new LinkedList<Variable>();
		intersectionVariables.addAll(msg.getVariables());
		unionVariables = intersectionVariables;
		return msg;
	}

	@Override
	public String toString() {
		return "Add (" + var.toString() + "=" + valueFromVar.toString() + ")";
	}
	
	public boolean remainsSortedData(Collection<Variable> sortCriterium){
		return true;
	}
	
	public Collection<Variable> transformSortCriterium(Collection<Variable> sortCriterium){
		if (sortCriterium
				.contains(this.getVar())) {
			final Set<Variable> sortCriterium2 = new HashSet<Variable>();
			sortCriterium2.addAll(sortCriterium);
			sortCriterium2.remove(this.getVar());
			sortCriterium2.add(this.getOtherVar());
			return sortCriterium2;
		}
		else return sortCriterium;
	}
}