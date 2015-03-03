
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.sparql1_1.operatorgraph.helper;

import java.util.Collection;

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.stream.Stream;
import lupos.engine.operators.tripleoperator.TriggerOneTime;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
public class IndexScanCreator_Stream implements IndexScanCreatorInterface {

	protected PatternMatcher currentPatternMatcher = new PatternMatcher();
	protected Stream stream = null;

	/**
	 * <p>Constructor for IndexScanCreator_Stream.</p>
	 */
	public IndexScanCreator_Stream(){
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator getRoot() {
		if(this.stream != null) {
			return this.stream;
		} else {
			return this.currentPatternMatcher;
		}
	}

	/**
	 * <p>Setter for the field <code>stream</code>.</p>
	 *
	 * @param stream a {@link lupos.engine.operators.stream.Stream} object.
	 */
	public void setStream(final Stream stream){
		this.stream = stream;
	}

	/**
	 * <p>Getter for the field <code>stream</code>.</p>
	 *
	 * @return a {@link lupos.engine.operators.stream.Stream} object.
	 */
	public Stream getStream(){
		return this.stream;
	}

	/**
	 * <p>Getter for the field <code>currentPatternMatcher</code>.</p>
	 *
	 * @return a {@link lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher} object.
	 */
	public PatternMatcher getCurrentPatternMatcher() {
		return this.currentPatternMatcher;
	}

	/**
	 * <p>Setter for the field <code>currentPatternMatcher</code>.</p>
	 *
	 * @param currentPatternMatcher a {@link lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher} object.
	 */
	public void setCurrentPatternMatcher(final PatternMatcher currentPatternMatcher) {
		this.currentPatternMatcher = currentPatternMatcher;
	}

	/** {@inheritDoc} */
	@Override
	public BasicOperator createIndexScanAndConnectWithRoot(final OperatorIDTuple opID, final Collection<TriplePattern> triplePatterns, final Item graphConstraint) {
		if(triplePatterns.size()>1){
			final Join join = new Join();
			int i=0;
			for(final TriplePattern tp : triplePatterns){
				this.currentPatternMatcher.add(tp);
				tp.addSucceedingOperator(new OperatorIDTuple(join, i));
				i++;
			}
			if(opID!=null) {
				join.addSucceedingOperator(opID);
			}
			return join;
		} else if(triplePatterns.size()==1){
			final TriplePattern tp = triplePatterns.iterator().next();
			if(opID!=null) {
				tp.addSucceedingOperator(opID);
			}
			this.currentPatternMatcher.add(tp);
			return tp;
		} else {
			throw new Error("There should be at least one Triple Pattern given!");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void createEmptyIndexScanSubmittingQueryResultWithOneEmptyBindingsAndConnectWithRoot(final OperatorIDTuple opID, final Item graphConstraint) {
		if(graphConstraint!=null) {
			throw new Error("This evaluator does not support named graphs!");
		}
		final TriggerOneTime trigger = new TriggerOneTime(true);
		this.currentPatternMatcher.addSucceedingOperator(trigger);
		if(opID!=null) {
			trigger.addSucceedingOperator(opID);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void createEmptyIndexScanAndConnectWithRoot(final OperatorIDTuple opID) {
		final TriggerOneTime trigger = new TriggerOneTime(false);
		this.currentPatternMatcher.addSucceedingOperator(trigger);
		if(opID!=null) {
			trigger.addSucceedingOperator(opID);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Dataset getDataset() {
		throw new UnsupportedOperationException("This evaluator does not support index structures!");
	}

	/** {@inheritDoc} */
	@Override
	public void addDefaultGraph(final String defaultgraph) {
		throw new UnsupportedOperationException("This evaluator does not support different default graphs!");
	}

	/** {@inheritDoc} */
	@Override
	public void addNamedGraph(final String namedgraph) {
		throw new UnsupportedOperationException("This evaluator does not support named graphs!");
	}
}
