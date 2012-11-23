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
package lupos.optimizations.physical.joinorder.withinindexscan;

import java.util.HashSet;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class implements the scoring for least new variables by determining the number of new variables in a triple pattern 
 */
public class ScoringTriplePatternLeastNewVariables implements ScoringTriplePattern<HashSet<Variable>>{
	
	/**
	 * the additional punishment score for cartesian products
	 */
	public static final int PUNISHMENT_FOR_CARTESIAN_PRODUCT = 100;

	@Override
	public int determineScore(BasicIndexScan indexScan,
			TriplePattern triplePattern, HashSet<Variable> additionalInformation) {
		final HashSet<Variable> v = triplePattern.getVariables();
		boolean flag = v.removeAll(additionalInformation);
		// flag == false means that no variables are in common, i.e., it is a cartesian product => make the score worser for cartesian products! 
		return v.size() + ((flag)? 0 : ScoringTriplePatternLeastNewVariables.PUNISHMENT_FOR_CARTESIAN_PRODUCT);
	}

	@Override
	public boolean scoreIsAscending() {
		return true;
	}
}
