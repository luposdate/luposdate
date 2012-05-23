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
package lupos.engine.operators.index;

import java.util.Collection;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.debug.DebugStep;

public abstract class IndexCollection extends Operator {
	public List<String> defaultGraphs;
	public List<String> namedGraphs;
	public Dataset dataset;

	public abstract BasicIndex newIndex(OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePattern, Item data);

	public QueryResult process(final int opt, final Dataset dataset) {
		if (succeedingOperators.size() == 0)
			return null;
		for (final OperatorIDTuple oit : succeedingOperators) {
			((BasicIndex) oit.getOperator()).process(opt, dataset);
		}
		return null;
	}


	public void physicalOptimization() {
		lupos.optimizations.physical.PhysicalOptimizations.replaceOperators(
				this, this);
	}

	public void optimizeJoinOrder(final int opt, final Dataset dataset) {
		switch (opt) {
		case BasicIndex.MOSTRESTRICTIONS:
			optimizeJoinOrderAccordingToMostRestrictions();
			break;
		}
	}

	public void optimizeJoinOrderAccordingToMostRestrictions() {
		for (final OperatorIDTuple oit : succeedingOperators) {
			final BasicIndex index = (BasicIndex) oit.getOperator();
			index.optimizeJoinOrderAccordingToMostRestrictions();
		}
	}

	public void remove(final BasicIndex i) {
		removeSucceedingOperator(i);
	}

	public abstract IndexCollection newInstance(Dataset dataset);

	public void printGraphURLs() {
		String graph;
		System.out.println();
		System.out.println("default graphs: ");
		if (defaultGraphs != null)
			for (int i = 0; i < defaultGraphs.size(); i++) {
				graph = defaultGraphs.get(i);
				System.out.println(i + ": " + graph);
			}
		System.out.println();
		System.out.println("named graphs: ");
		if (namedGraphs != null)
			for (int i = 0; i < namedGraphs.size(); i++) {
				graph = namedGraphs.get(i);
				System.out.println(i + ": " + graph);
			}
		System.out.println();
	}
	
	public QueryResult processDebug(final int opt, final Dataset dataset,
			final DebugStep debugstep) {
		if (succeedingOperators.size() == 0)
			return null;
		for (final OperatorIDTuple oit : succeedingOperators) {
			((BasicIndex) oit.getOperator()).processDebug(opt, dataset,
					debugstep);
		}
		return null;
	}
}