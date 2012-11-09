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
package lupos.engine.operators.index.memoryindex;

import java.util.HashSet;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class OptimizedRelationalIndex extends MemoryIndexScan {

	protected BasicIndexScan index1;
	protected BasicIndexScan index2;

	public void physicalOptimization() {
		lupos.optimizations.physical.PhysicalOptimizations.replaceOperators(
				this.index1, this);
		lupos.optimizations.physical.PhysicalOptimizations.replaceOperators(
				this.index2, this);
	}

	public OptimizedRelationalIndex(final BasicIndexScan index1,
			final BasicIndexScan index2, final lupos.engine.operators.index.Root root) {
		super(root);
		this.index1 = index1;
		this.index2 = index2;
		this.succeedingOperators = new LinkedList<OperatorIDTuple>();
		this.succeedingOperators.addAll(index1.getSucceedingOperators());
		this.succeedingOperators.addAll(index2.getSucceedingOperators());
		this.triplePatterns = new LinkedList<TriplePattern>();
		this.triplePatterns.addAll(index1.getTriplePattern());
		this.triplePatterns.addAll(index2.getTriplePattern());
	}

	/**
	 * Joins the triple pattern using the index maps and returns the result.<br>
	 * The succeeding operators are passed to the operator pipe to be processed.
	 * 
	 * @param triplePatterns
	 *            - the triple pattern to be joined
	 * @param succeedingOperators
	 *            - the succeeding operators to be passed
	 * @return the result of the performed join
	 */
	@Override
	public QueryResult process(final Dataset dataset) {

		// join the triple patterns
		this.index1.startProcessing(dataset);
		return null; // queryresult already distributed among children 
	}

	@Override
	public void optimizeJoinOrderAccordingToMostRestrictionsAndLeastEntries(
			final Dataset dataset) {
		this.index1.optimizeJoinOrder(BasicIndexScan.MOSTRESTRICTIONSLEASTENTRIES,
				dataset);
		this.index2.optimizeJoinOrder(BasicIndexScan.MOSTRESTRICTIONSLEASTENTRIES,
				dataset);
	}

	@Override
	public void optimizeJoinOrderAccordingToLeastEntries(final Dataset dataset) {
		this.index1.optimizeJoinOrder(BasicIndexScan.LEASTENTRIES, dataset);
		this.index2.optimizeJoinOrder(BasicIndexScan.LEASTENTRIES, dataset);
	}

	@Override
	public void optimizeJoinOrderAccordingToMostRestrictions() {
		this.index1.optimizeJoinOrderAccordingToMostRestrictions();
		this.index2.optimizeJoinOrderAccordingToMostRestrictions();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		final HashSet<Variable> intersectionVariables1 = new HashSet<Variable>();
		for (final TriplePattern tp : this.index1.getTriplePattern()) {
			intersectionVariables1.addAll(tp.getVariables());
		}
		final HashSet<Variable> intersectionVariables2 = new HashSet<Variable>();
		for (final TriplePattern tp : this.index2.getTriplePattern()) {
			intersectionVariables2.addAll(tp.getVariables());
		}
		this.unionVariables = (HashSet<Variable>) intersectionVariables1.clone();
		intersectionVariables1.retainAll(intersectionVariables2);
		result.getVariables().addAll(intersectionVariables1);
		this.intersectionVariables = intersectionVariables1;
		this.unionVariables.addAll(intersectionVariables2);
		return result;
	}

	@Override
	public void replace(final Variable var, final Item item) {
		this.index1.replace(var, item);
		this.index2.replace(var, item);
	}
}
