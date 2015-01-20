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
package lupos.rif.operator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.Dataset;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;

public class PredicateIndexScan extends InsertIndexScan {
	final public Set<Predicate> predFacts = new HashSet<Predicate>();

	public PredicateIndexScan() {
		super(null);
		this.triplePatterns = Arrays.asList();
	}

	public void addPredicateFact(final Predicate fact) {
		this.predFacts.add(fact);
	}

	private final QueryResult createQueryResult(){
		final RuleResult gr = new RuleResult();
		gr.getPredicateResults().addAll(this.predFacts);
		return gr;
	}

	@Override
	public QueryResult process(final Dataset dataset) {
		return this.createQueryResult();
	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer("PredicateFacts")
				.append("\n");
		for (final Predicate pr : this.predFacts) {
			str.append(pr.toString()).append("\n");
		}
		return str.toString();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer("PredicateFacts")
				.append("\n");
		for (final Predicate pr : this.predFacts) {
			str.append(pr.toString(prefixInstance)).append("\n");
		}
		return str.toString();
	}

	@Override
	public void consumeOnce() {
		this.processAtSucceedingOperators(this.createQueryResult());
	}

	@Override
	public void consumeDebugOnce(final DebugStep debugstep) {
		this.processAtSucceedingOperatorsDebug(this.createQueryResult(), debugstep);
	}

	@Override
	public boolean joinOrderToBeOptimized(){
		return false;
	}
}
