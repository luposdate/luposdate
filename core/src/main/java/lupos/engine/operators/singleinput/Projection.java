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

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;

public class Projection extends SingleInputOperator {
	private final HashSet<Variable> s = new HashSet<Variable>();

	public Projection() {
	}

	public void addProjectionElement(final Variable var) {
		if (!s.contains(var)) {
			s.add(var);
		}
	}

	public HashSet<Variable> getProjectedVariables() {
		return s;
	}

	/**
	 * Handles the BoundVariablesMessage by removing all variables from it that
	 * are not projected to.
	 * 
	 * @param msg
	 *            The BoundVariablesMessage
	 * @return The modified message
	 */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		for (final Variable v : msg.getVariables()) {
			if (s.contains(v)) {
				result.getVariables().add(v);
			}
		}
		unionVariables = result.getVariables();
		intersectionVariables = result.getVariables();
		return result;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> itb = new ParallelIterator<Bindings>() {
			final Iterator<Bindings> itbold = bindings.oneTimeIterator();

			public boolean hasNext() {
				return itbold.hasNext();
			}

			public Bindings next() {
				if (!itbold.hasNext())
					return null;
				final Bindings bind1 = itbold.next();
				if (!itbold.hasNext()) {
					if (itbold instanceof ParallelIterator) {
						((ParallelIterator) itbold).close();
					}
				}
				final Bindings bnew = Bindings.createNewInstance();

				final Iterator<Variable> it = s.iterator();
				while (it.hasNext()) {
					final Variable elem = it.next();
					bnew.add(elem, bind1.get(elem));
				}
				bnew.addAllTriples(bind1);
				bnew.addAllPresortingNumbers(bind1);
				return bnew;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void finalize() {
				close();
			}

			public void close() {
				if (itbold instanceof ParallelIterator) {
					((ParallelIterator) itbold).close();
				}
			}
		};

		return QueryResult.createInstance(itb);
	}

	@Override
	public String toString() {
		return super.toString()+" to " + s;
	}
	
	public boolean remainsSortedData(Collection<Variable> sortCriterium){
		return true;
	}
}