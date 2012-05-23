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
package lupos.engine.operators.rdfs.index;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;

public class IndexCollection extends
		lupos.engine.operators.index.IndexCollection {

	@Override
	public BasicIndex newIndex(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, final Item data) {
		return new ToStreamIndex(succeedingOperator, triplePattern, this);
	}

	@Override
	public lupos.engine.operators.index.IndexCollection newInstance(Dataset dataset) {
		this.dataset = dataset;
		return new IndexCollection();
	}

	public void addToPatternMatcher(final PatternMatcher pm) {
		for (final OperatorIDTuple oit : succeedingOperators) {
			final ToStreamIndex tsi = (ToStreamIndex) oit.getOperator();
			if (tsi.getTriplePattern().size() == 1)
				for (final TriplePattern tp : tsi.getTriplePattern()) {
					pm.add(tp);
					tp.setPrecedingOperator(pm);
				}
			else {
				final Join j = new Join();
				j.setSucceedingOperator(tsi.getSucceedingOperators().get(0));
				int i = 0;
				final HashSet<Variable> unionVariables = new HashSet<Variable>();
				final HashSet<Variable> intersectionVariables = new HashSet<Variable>();
				intersectionVariables.addAll(tsi.getTriplePattern().iterator()
						.next().getUnionVariables());
				for (final TriplePattern tp : tsi.getTriplePattern()) {
					final LinkedList<OperatorIDTuple> succeedingOperatorsTP = new LinkedList<OperatorIDTuple>();
					succeedingOperatorsTP.add(new OperatorIDTuple(j, i));
					tp.addSucceedingOperators(succeedingOperatorsTP);
					j.addPrecedingOperator(tp);
					pm.add(tp);
					tp.addPrecedingOperator(pm);
					unionVariables.addAll(tp.getUnionVariables());
					intersectionVariables.retainAll(tp.getUnionVariables());
					i++;
				}
				j.setUnionVariables(unionVariables);
				j.setIntersectionVariables(intersectionVariables);
			}
		}
	}
}
