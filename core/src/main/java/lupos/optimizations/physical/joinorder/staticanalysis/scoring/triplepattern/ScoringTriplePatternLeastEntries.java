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
package lupos.optimizations.physical.joinorder.staticanalysis.scoring.triplepattern;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class implements the scoring for least entries by determining the number of results of a triple pattern
 */
public class ScoringTriplePatternLeastEntries implements ScoringTriplePattern<HashSet<Variable>> {

	@Override
	public int determineScore(final BasicIndexScan indexScan,
			final TriplePattern triplePattern, final HashSet<Variable> additonalInformation) {
		// just do dirty trick to determine the number of results of a triple pattern...
		final LinkedList<TriplePattern> tpc = new LinkedList<TriplePattern>();
		tpc.add(triplePattern);
		final Collection<TriplePattern> zTP = indexScan.getTriplePattern();
		indexScan.setTriplePatterns(tpc);
		final QueryResult queryResult = indexScan.join(indexScan.getRoot().dataset);
		indexScan.setTriplePatterns(zTP);
		final int entries = (queryResult==null)? 0 : queryResult.oneTimeSize();
		return entries;
	}

	@Override
	public boolean scoreIsAscending() {
		return true;
	}

}
