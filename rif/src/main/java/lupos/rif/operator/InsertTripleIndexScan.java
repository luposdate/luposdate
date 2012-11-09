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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.singleinput.sparul.Insert;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreator_BasicIndex;

public class InsertTripleIndexScan extends InsertIndexScan {
	final protected Set<Triple> facts = new HashSet<Triple>();

	public InsertTripleIndexScan(IndexScanCreatorInterface indexScanCreator) {
		super(null);		
		this.triplePatterns = Arrays.asList();
		if(indexScanCreator instanceof IndexScanCreator_BasicIndex){
			Insert insert = new Insert(new ArrayList<URILiteral>(), indexScanCreator.getDataset());
			addSucceedingOperator(insert);
		} else {
			addSucceedingOperator(indexScanCreator.getRoot());
		}
	}

	public void addTripleFact(final Triple fact) {
		this.facts.add(fact);
	}

	@Override
	public QueryResult process(final Dataset dataset) {
		// Leitet ein GraphResult mit den Triple-Fakten weiter
		final GraphResult result = new GraphResult();
		for (final Triple triple : this.facts)
			result.addGraphResultTriple(triple);
		for (final OperatorIDTuple oid : this.succeedingOperators)
			((Operator) oid.getOperator()).processAll(result, oid.getId());
		return result;
	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer("TripleFacts").append("\n");
		for (final Triple tr : this.facts)
			str.append(tr.toString()).append("\n");
		return str.toString();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer("TripleFacts").append("\n");
		for (final Triple tr : this.facts)
			str.append(tr.toString(prefixInstance)).append("\n");
		return str.toString();
	}


	@Override
	public void consumeOnce() {
		for (final Triple t : this.facts){
			for(OperatorIDTuple opID: this.getSucceedingOperators()){
				((TripleConsumer)opID.getOperator()).consume(t);
			}
		}			
	}

	@Override
	public void consumeDebugOnce(final DebugStep debugstep) {
		for (final Triple t : this.facts){
			for(OperatorIDTuple opID: this.getSucceedingOperators()){
				BasicOperator to = opID.getOperator();
				debugstep.step(this, to, t);
				((TripleConsumerDebug)to).consumeDebug(t, debugstep);
			}
		}			
	}
}
