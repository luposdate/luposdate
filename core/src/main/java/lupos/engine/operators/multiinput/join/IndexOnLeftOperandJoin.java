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
package lupos.engine.operators.multiinput.join;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;
import lupos.misc.util.ImmutableIterator;

/**
 * This join operator creates an index on the left operand and
 * iterates one time through the results of the right operand
 * using the previously created index to find join partners.
 *
 * Thus, the operand with less intermediate results should be the left operand.
 *
 * This operator is not suitable for recursive queries and rule processing, where cycles in the operatorgraph can occur.
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class IndexOnLeftOperandJoin extends Join {
	protected ParallelIteratorMultipleQueryResults[] operands = {	new ParallelIteratorMultipleQueryResults(),
																	new ParallelIteratorMultipleQueryResults()};

	/**
	 * <p>Constructor for IndexOnLeftOperandJoin.</p>
	 */
	public IndexOnLeftOperandJoin() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
	}

	/**
	 * <p>createDatastructure.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public abstract Map<String, QueryResult> createDatastructure();

	/** {@inheritDoc} */
	@Override
	public synchronized QueryResult process(final QueryResult bindings, final int operandID) {
		//bindings.materialize(); // I do not know why this is necessary, but if there are several IndexOnLeftOperandJoin operators after each other this seems to be necessary...
		this.operands[operandID].addQueryResult(bindings); // just store the queryresult!
		return null; // wait for EndOfStreamMessage...
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		if(!this.operands[0].isEmpty() && ! this.operands[1].isEmpty()){
			final Map<String, QueryResult> leftOperandsData = this.createDatastructure();
			final QueryResult cartesianProduct = QueryResult.createInstance();

			IndexOnLeftOperandJoin.indexQueryResult(this.operands[0].getQueryResult(), this.intersectionVariables, leftOperandsData, cartesianProduct);

			final QueryResult result = QueryResult.createInstance(new JoinIterator(this.intersectionVariables, this.operands[1].getQueryResult(), leftOperandsData, cartesianProduct));

			if(this.succeedingOperators.size()>1){
				result.materialize();
			}

			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAll(result);
			}
		}
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg, final DebugStep debugstep) {
		if(!this.operands[0].isEmpty() && ! this.operands[1].isEmpty()){
			final Map<String, QueryResult> leftOperandsData = this.createDatastructure();
			final QueryResult cartesianProduct = QueryResult.createInstance();

			IndexOnLeftOperandJoin.indexQueryResult(this.operands[0].getQueryResult(), this.intersectionVariables, leftOperandsData, cartesianProduct);

			final QueryResult result = QueryResult.createInstance(new JoinIterator(this.intersectionVariables, this.operands[1].getQueryResult(), leftOperandsData, cartesianProduct));

			if(this.succeedingOperators.size()>1){
				result.materialize();
			}

			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAllDebug(new QueryResultDebug(result, debugstep, this, opId.getOperator(), true), debugstep);
			}
		}
		return msg;
	}


	/**
	 * <p>indexQueryResult.</p>
	 *
	 * @param toIndex a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param joinVariables a {@link java.util.Collection} object.
	 * @param index a {@link java.util.Map} object.
	 * @param cartesianProduct a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static void indexQueryResult(final QueryResult toIndex, final Collection<Variable> joinVariables, final Map<String, QueryResult> index, final QueryResult cartesianProduct){
		final Iterator<Bindings> itbindings = toIndex.oneTimeIterator();
		while (itbindings.hasNext()) {

			final Bindings bindings = itbindings.next();

			final String keyJoin = IndexOnLeftOperandJoin.getKey(bindings, joinVariables);

			if (keyJoin == null){
				cartesianProduct.add(bindings);
				continue;
			}

			QueryResult lb = index.get(keyJoin);
			if (lb == null){
				lb = QueryResult.createInstance();
			}
			lb.add(bindings);
			index.put(keyJoin, lb);
		}
		if(itbindings instanceof ParallelIterator){
			((ParallelIterator<Bindings>)itbindings).close();
		}
	}

	/**
	 * <p>getKey.</p>
	 *
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param joinVariables a {@link java.util.Collection} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getKey(final Bindings bindings, final Collection<Variable> joinVariables){
		String keyJoin = "";
		final Iterator<Variable> it = joinVariables.iterator();
		while (it.hasNext()) {
			final Literal literal = bindings.get(it.next());
			if (literal == null) {
				keyJoin = null;
				break;
			}
			keyJoin += "|" + literal.getKey();
		}
		return keyJoin;
	}

	public static class DebugIterator implements Iterator<Bindings>{

		private final Iterator<Bindings> innerIterator;
		private final String id;

		public DebugIterator(final String id, final Iterator<Bindings> innerIterator){
			this.innerIterator = innerIterator;
			this.id = id;
		}

		@Override
		public boolean hasNext() {
			final boolean result = this.innerIterator.hasNext();
			System.out.println(this.id+".hasNext():"+result);
			return result;
		}

		@Override
		public Bindings next() {
			final Bindings result = this.innerIterator.next();
			System.out.println(this.id+".next():"+result);
			return result;
		}

		@Override
		public void remove() {
			this.innerIterator.remove();
		}
	}

	public static class JoinIterator implements ParallelIterator<Bindings>{

		protected final Map<String, QueryResult> leftOperandsData;
		protected final QueryResult cartesianProduct;
		protected final Iterator<Bindings> rightOperandIt;
		protected Iterator<Bindings> currentBindingsOfRightOperandIt;
		protected final Collection<Variable> joinVariables;

		public JoinIterator(final Collection<Variable> joinVariables, final QueryResult rightOperand, final Map<String, QueryResult> leftOperandsData, final QueryResult cartesianProduct){
			this(joinVariables, rightOperand.oneTimeIterator(), leftOperandsData, cartesianProduct);
		}

		public JoinIterator(final Collection<Variable> joinVariables, final Iterator<Bindings> rightOperandIt, final Map<String, QueryResult> leftOperandsData, final QueryResult cartesianProduct){
			this.joinVariables = joinVariables;
			this.rightOperandIt = rightOperandIt;
			this.leftOperandsData = leftOperandsData;
			this.cartesianProduct = cartesianProduct;
		}

		@Override
		public boolean hasNext() {
			if(this.currentBindingsOfRightOperandIt!=null && this.currentBindingsOfRightOperandIt.hasNext()){
				return true;
			} else {
				this.currentBindingsOfRightOperandIt = this.nextIterator();
				if(this.currentBindingsOfRightOperandIt!=null){
					return true;
				}
			}
			return false;
		}

		private Iterator<Bindings> nextIterator() {
			while(this.rightOperandIt.hasNext()){
				Iterator<Bindings> result = null;
				final Bindings bindings = this.rightOperandIt.next();
				final String keyJoin = IndexOnLeftOperandJoin.getKey(bindings, this.joinVariables);
				final QueryResult fromLeft;
				if(keyJoin != null){
					fromLeft = this.leftOperandsData.get(keyJoin);
				} else {
					if(this.cartesianProduct!=null && this.cartesianProduct.size()>0){
						fromLeft = this.cartesianProduct;
					} else {
						fromLeft = null;
					}
				}
				if(fromLeft!=null){
					result = new ImmutableIterator<Bindings>(){
						final Iterator<Bindings> it = fromLeft.oneTimeIterator();
						Bindings next = null;
						@Override
						public boolean hasNext() {
							if(this.next!=null){
								return true;
							}
							this.next = this.next();
							return (this.next!=null);
						}

						@Override
						public Bindings next() {
							if(this.next!=null){
								final Bindings zBindings = this.next;
								this.next = null;
								return zBindings;
							}
							if(!this.it.hasNext()){
								return null;
							}
							Bindings bnew;
							do {
								bnew = bindings.clone();
								final Bindings bindings2 = this.it.next();
								bnew = Join.joinBindingsAndReturnBindings(bnew, bindings2);
							} while(bnew==null && this.it.hasNext());
							return bnew;
						}
					};
				}
				if(result!=null && result.hasNext()){
					return result;
				}
			}
			return null;
		}

		@Override
		public Bindings next() {
			if(this.hasNext()){
				return this.currentBindingsOfRightOperandIt.next();
			} else {
				return null;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			if(this.rightOperandIt instanceof ParallelIterator){
				((ParallelIterator<Bindings>)this.rightOperandIt).close();
			}
		}
	}
}
