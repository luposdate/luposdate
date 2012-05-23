/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;
import lupos.misc.debug.DebugStepRIF;
import lupos.rdf.Prefix;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;

public class PredicateIndex extends BasicIndex implements TripleConsumer, TripleConsumerDebug, TripleDeleter {
	final public Set<Predicate> predFacts = new HashSet<Predicate>();

	public PredicateIndex() {
		super(null);
		triplePatterns = Arrays.asList();
	}

	public void addPredicateFact(final Predicate fact) {
		predFacts.add(fact);
	}

	@Override
	public QueryResult process(final int opt, final Dataset dataset) {
		final RuleResult gr = new RuleResult();
		gr.getPredicateResults().addAll(predFacts);
		for (final OperatorIDTuple succOperator : succeedingOperators)
			((Operator) succOperator.getOperator()).processAll(gr,
					succOperator.getId());
		return gr;
	}

	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		final RuleResult gr = new RuleResult();
		gr.getPredicateResults().addAll(predFacts);
		for (final OperatorIDTuple succOperator : succeedingOperators) {
			if (!gr.isEmpty())
				((DebugStepRIF)debugstep).step(this, succOperator.getOperator(), gr);
			final QueryResultDebug debug = new QueryResultDebug(gr, debugstep,
					this, succOperator.getOperator(), true);
			((Operator) succOperator.getOperator()).processAllDebug(debug,
					succOperator.getId(), debugstep);
		}
		return gr;
	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer("PredicateFacts")
				.append("\n");
		for (final Predicate pr : predFacts)
			str.append(pr.toString()).append("\n");
		return str.toString();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer("PredicateFacts")
				.append("\n");
		for (final Predicate pr : predFacts)
			str.append(pr.toString(prefixInstance)).append("\n");
		return str.toString();
	}

	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		return null;
	}

	private boolean firstTime = true;

	@Override
	public void deleteTriple(Triple triple) {
	}

	@Override
	public void deleteTripleDebug(Triple triple, DebugStep debugstep) {
	}

	@Override
	public void consume(Triple triple) {
		if(firstTime){
			process(0, null);
			firstTime = false;
		}
	}

	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		if(firstTime){
			processDebug(0, null, debugstep);
			firstTime = false;
		}
	}
}
