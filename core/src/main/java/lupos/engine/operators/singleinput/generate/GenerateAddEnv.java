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
package lupos.engine.operators.singleinput.generate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.SingleInputOperator;
public class GenerateAddEnv extends SingleInputOperator {
	private HashMap<Variable, Literal> constants;
	private HashMap<Variable, Literal> conditions;

	/**
	 * <p>Constructor for GenerateAddEnv.</p>
	 */
	public GenerateAddEnv() {
	}

	/**
	 * <p>Getter for the field <code>constants</code>.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<Variable, Literal> getConstants() {
		return constants;
	}

	/**
	 * <p>Getter for the field <code>conditions</code>.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<Variable, Literal> getConditions() {
		return conditions;
	}

	/**
	 * <p>Constructor for GenerateAddEnv.</p>
	 *
	 * @param conditions a {@link java.util.HashMap} object.
	 * @param constants a {@link java.util.HashMap} object.
	 */
	public GenerateAddEnv(final HashMap<Variable, Literal> conditions,
			final HashMap<Variable, Literal> constants) {
		this.constants = constants;
		this.conditions = conditions;
	}

	// bindings should contain exactly one element!
	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final QueryResult result = QueryResult.createInstance();

		final Iterator<Bindings> itb = bindings.iterator();
		Bindings bind1;
		if (itb.hasNext())
			bind1 = itb.next();
		else
			return null;

		boolean conditionFulfilled = true;

		// check conditions...
		Iterator<Variable> it = conditions.keySet().iterator();

		while (it.hasNext()) {
			final Variable elem = it.next();
			if (!conditions.get(elem).valueEquals(bind1.get(elem))) {
				conditionFulfilled = false;
			}
		}

		if (conditionFulfilled) {
			final Bindings bnew = bind1.clone();

			it = constants.keySet().iterator();
			while (it.hasNext()) {
				final Variable elem = it.next();
				bind1.add(elem, constants.get(elem));
			}
			result.add(bnew);
		}

		result.add(bind1);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		msg.getVariables().addAll(constants.keySet());
		intersectionVariables = new LinkedList<Variable>();
		intersectionVariables.addAll(msg.getVariables());
		unionVariables = new LinkedList<Variable>();
		unionVariables.addAll(msg.getVariables());
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString() + conditions + "," + constants;
	}
}
