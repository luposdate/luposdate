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
package lupos.distributed.query.operator.histogramsubmission;

import java.util.Collection;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.distributed.query.operator.withouthistogramsubmission.QueryClientIndexScan;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.statistics.VarBucket;

/**
 * This class represents an index scan operator for the distributed query evaluators...
 */
public class QueryClientIndexScanWithHistogramSubmission extends QueryClientIndexScan {

	protected final IHistogramExecutor histogramExecutor;

	public QueryClientIndexScanWithHistogramSubmission(final OperatorIDTuple succeedingOperator, final Collection<TriplePattern> triplePatterns, final Item rdfGraph, final QueryClientRootWithHistogramSubmission root) {
		super(succeedingOperator, triplePatterns, rdfGraph, root);
		this.histogramExecutor = root.histogramExecutor;
	}

	public QueryClientIndexScanWithHistogramSubmission(final QueryClientRootWithHistogramSubmission root, final Collection<TriplePattern> triplePatterns) {
		super(root, triplePatterns);
		this.histogramExecutor = root.histogramExecutor;
	}


	@Override
	public Map<Variable, VarBucket> getVarBuckets(final TriplePattern triplePattern,
			final Class<? extends Bindings> classBindings,
			final Collection<Variable> joinPartners,
			final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima) {
		return this.histogramExecutor.getHistograms(triplePattern, joinPartners, minima, maxima);
	}

	@Override
	public Map<Variable, Tuple<Literal, Literal>> getMinMax(final TriplePattern triplePattern, final Collection<Variable> variables) {
		return this.histogramExecutor.getMinMax(triplePattern, variables);
	}

}
