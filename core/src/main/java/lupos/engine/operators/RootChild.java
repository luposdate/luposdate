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
package lupos.engine.operators;

import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.index.Dataset;
import lupos.misc.debug.DebugStep;

public abstract class RootChild extends BasicOperator {
	
	public void startProcessing(final Dataset dataset){
		QueryResult queryresult = this.process(dataset);
		if (queryresult == null){
			return;
		}
		if(this.succeedingOperators.size()>1){
			queryresult.materialize();
		}
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			((Operator) opId.getOperator()).processAll(queryresult, opId.getId());
		}		
	}
	
	public void startProcessingDebug(final Dataset dataset, final DebugStep debugstep) {
		final QueryResult queryresult = this.process(dataset);
		if (queryresult == null){
			return;
		}
		if (this.succeedingOperators.size() > 1) {
			queryresult.materialize();
		}
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(queryresult, debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId.getId(), debugstep);
		}
	}
	
	/**
	 * start the evaluation under the root operator with a given dataset
	 * @param dataset the dataset as input for this operator
	 * @return the queryresult to be processed by the children of this operator
	 */
	protected abstract QueryResult process(final Dataset dataset);
	
}
