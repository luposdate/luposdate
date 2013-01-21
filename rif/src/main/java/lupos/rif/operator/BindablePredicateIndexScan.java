/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.rif.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rif.datatypes.RuleResult;

public class BindablePredicateIndexScan extends BindableIndexScan {
	private final PredicatePattern predicatePattern;

	public BindablePredicateIndexScan(final PredicateIndexScan index,
			final PredicatePattern pattern) {
		super(index);
		this.predicatePattern = pattern;
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = (BoundVariablesMessage) predicatePattern
				.preProcessMessage(msg);
		result.getVariables().removeAll(msg.getVariables());
		unionVariables = new HashSet<Variable>(result.getVariables());
		intersectionVariables = new HashSet<Variable>(unionVariables);
		return result;
	}

	@Override
	protected void processIndexScan(QueryResult result, Bindings bind) {
		final Item[] newItems = new Item[predicatePattern.getPatternItems()
				.size()];
		int i = 0;
		for (final Item item : predicatePattern.getPatternItems()) {
			Item toSet = null;
			if (item.isVariable() && bind.getVariableSet().contains(item))
				toSet = item.getLiteral(bind);
			else
				toSet = item;
			newItems[i++] = toSet;
		}
		final PredicatePattern newPattern = new PredicatePattern(
				predicatePattern.getPredicateName(), newItems);
		// Scan durchf�hren
		RuleResult ruleResult = (RuleResult) index.process(dataSet);
		QueryResult tempResult = newPattern.process(ruleResult, 0);
		result.add(tempResult);
	}

	@Override
	public Collection<TriplePattern> getTriplePattern() {
		return new ArrayList<TriplePattern>();
	}

}
