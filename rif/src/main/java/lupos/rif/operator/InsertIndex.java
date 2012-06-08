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

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;

public abstract class InsertIndex extends BasicIndex implements TripleConsumer, TripleConsumerDebug, TripleDeleter {

	public InsertIndex(IndexCollection indexCollection) {
		super(indexCollection);
	}

	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		return null;
	}

	private boolean firstTime = true;

	@Override
	public void deleteTriple(Triple triple) {
		// no triple to delete as fixed triples or facts are only submitted
	}

	@Override
	public void deleteTripleDebug(Triple triple, DebugStep debugstep) {
		// no triple to delete as fixed triples or facts are only submitted
	}

	@Override
	public void consume(Triple triple) {
		if(this.firstTime){
			this.consumeOnce();
			this.firstTime = false;
		}
	}

	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		if(this.firstTime){
			this.consumeDebugOnce(debugstep);
			this.firstTime = false;
		}
	}
	
	protected abstract void consumeOnce();
	protected abstract void consumeDebugOnce(final DebugStep debugstep);	
}
