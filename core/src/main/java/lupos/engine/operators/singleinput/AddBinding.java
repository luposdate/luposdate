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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.util.ImmutableIterator;

public class AddBinding extends SingleInputOperator {
	private static final long serialVersionUID = -1619006408122462537L;
	private Variable var;
	private Literal literalName;

	public AddBinding() {}

	public AddBinding(final Variable var, final Literal literal) {
		this.var = var;
		this.literalName = literal;
	}

	// bindings should contain exactly one element!
	@Override
	public QueryResult process(final QueryResult oldBindings,
			final int operandID) {
		return QueryResult.createInstance(new ImmutableIterator<Bindings>() {
			Iterator<Bindings> itb = oldBindings.oneTimeIterator();

			@Override
			public boolean hasNext() {
				return this.itb.hasNext();
			}

			@Override
			public Bindings next() {
				final Bindings b = this.itb.next();
				if (b != null) {
					final Literal literal = b.get(AddBinding.this.var);
					if (literal == null) {
						b.add(AddBinding.this.var, AddBinding.this.literalName);
					}
					// if the item is a variable which is already bound
					// and the value differs from the value of the triple
					// which would be used as binding, a conflict is
					// detected
					else if (!literal.valueEquals(AddBinding.this.literalName)) {
						System.err.println("AddBinding received a bindings, where the variable is already bound to another value!");
						return null;
					}

				}
				return b;
			}
		});
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		msg.getVariables().add(this.var);
		this.intersectionVariables = new LinkedList<Variable>();
		this.intersectionVariables.addAll(msg.getVariables());
		this.unionVariables = this.intersectionVariables;
		return msg;
	}

	public Variable getVar() {
		return this.var;
	}

	public Literal getLiteral() {
		return this.literalName;
	}

	public void setVar(final Variable var) {
		this.var = var;
	}

	public void setLiteral(final Literal literalName) {
		this.literalName = literalName;
	}

	@Override
	public String toString() {
		return "Add (" + this.var.toString() + "=" + this.literalName + ")";
	}

	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		return "Add (" + this.var.toString() + "="
		+ prefixInstance.add(this.literalName.toString()) + ")";
	}

	@Override
	public boolean remainsSortedData(final Collection<Variable> sortCriterium){
		return !sortCriterium.contains(this.getVar());
	}
}