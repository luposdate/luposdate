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

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.engine.operators.multiinput.optional.OptionalResult;
public class Join extends MultiInputOperator {

	protected double estimatedCardinality = -1.0;
	protected int realCardinality = -1;

	/**
	 * <p>Constructor for Join.</p>
	 */
	public Join() {
		// no initializations...
	}

	/**
	 * <p>Getter for the field <code>estimatedCardinality</code>.</p>
	 *
	 * @return a double.
	 */
	public double getEstimatedCardinality() {
		return this.estimatedCardinality;
	}

	/**
	 * <p>Setter for the field <code>estimatedCardinality</code>.</p>
	 *
	 * @param estimatedCardinality a double.
	 */
	public void setEstimatedCardinality(final double estimatedCardinality) {
		this.estimatedCardinality = estimatedCardinality;
	}

	/**
	 * <p>Getter for the field <code>realCardinality</code>.</p>
	 *
	 * @return a int.
	 */
	public int getRealCardinality() {
		return this.realCardinality;
	}

	/**
	 * <p>Setter for the field <code>realCardinality</code>.</p>
	 *
	 * @param realCardinality a int.
	 */
	public void setRealCardinality(final int realCardinality) {
		this.realCardinality = realCardinality;
	}

	/**
	 * Adds all bindings of the second bindings object to the first one and
	 * returns a new query result object which contains the updated first
	 * bindings object.<br>
	 * If the second bindings object contains a binding which conflicts with a
	 * binding of the first bindings object, <code>null</code> will be returned
	 * to indicate this (unsolvable) conflict<br>
	 * <br>
	 * Note that the first bindings object is first bindings object provided is
	 * actually altered if additional bindings were found in the second bindings
	 * object.
	 *
	 * @param bindings1
	 *            the first bindings object
	 * @param bindings2
	 *            the second bindings object
	 * @return a query result object which contains the combination of bindings
	 *         or <code>null</code> if an unsolvable conflict was detected
	 */
	public static QueryResult joinBindings(final Bindings bindings1,
			final Bindings bindings2) {

		// create a new instance of a query result object to take up
		// the combination of the two bindings object
		final QueryResult qResult = QueryResult.createInstance();

		// if the bindings can be combined (they contain no conflicting
		// bindings) return the new query result object. Otherwise
		// return null
		return (joinBindings(qResult, bindings1, bindings2)) ? qResult : null;
		/*
		 * // only the keys of the second bindings object have to be // checked
		 * since the the first bindings object is updated for (final String
		 * b2key : bindings2.getKeySet()) { final Literal literalB2 =
		 * bindings2.get(b2key); final Literal literalB1 = bindings1.get(b2key);
		 * 
		 * // if the literal computed from the second bindings conflicts with //
		 * the corresponding one located in the first bindings object, // null
		 * is returned to indicate an unsolvable conflict if ( (literalB1 !=
		 * null) && !literalB1.valueEquals(literalB2) ){ return null; }
		 * 
		 * // otherwise add the new binding of the key and literal to // the
		 * first bindings object bindings1.add(b2key, literalB2); }
		 * 
		 * // return a new instance of a query result which contains the // the
		 * combination of bindings from the first and the second // bindings
		 * object final QueryResult result=QueryResult.createInstance();
		 * result.add(bindings1); return result;
		 */
	}

	/**
	 * Combines the bindings of two bindings objects and adds them to a query
	 * result object if no conflicting bindings were found. If the second
	 * bindings object contains a binding which conflicts with a binding of the
	 * first bindings object, <code>false</code> will be returned to indicate
	 * this (unsolvable) conflict<br>
	 * <br>
	 * Note that the first bindings object provided is actually altered if
	 * additional bindings were found in the second bindings object.
	 *
	 * @param result
	 *            the query result object which is to be updated
	 * @param bindings1
	 *            the first bindings object
	 * @param bindings2
	 *            the second bindings object
	 * @return <code>false</code> if an unsolvable conflict was detected,
	 *         <code>true</code> otherwise
	 */
	public static boolean joinBindings(final QueryResult result,
			final Bindings bindings1, final Bindings bindings2) {

		// only the keys of the second bindings object have to be
		// checked since the the first bindings object is updated
		for (final Variable b2key : bindings2.getVariableSet()) {

			final Literal literalB2 = bindings2.get(b2key);
			final Literal literalB1 = bindings1.get(b2key);

			// if the literal computed from the second bindings conflicts with
			// the corresponding one located in the first bindings object,
			// null is returned to indicate an unsolvable conflict
			if (literalB1 != null) {
				if (!literalB1.valueEquals(literalB2)) {
					return false;
				}
			}

			// otherwise add the new binding of the key and literal to
			// the first bindings object
			else {
				bindings1.add(b2key, literalB2);
			}

		}

		// return a new instance of a query result which contains the
		// the combination of bindings from the first and the second
		// bindings object
		// final QueryResult result=QueryResult.createInstance();
		bindings1.addAllTriples(bindings2);
		bindings1.addAllPresortingNumbers(bindings2);
		result.add(bindings1);
		return true;
	}

	/**
	 * <p>joinBindingsAndReturnBindings.</p>
	 *
	 * @param bindings1 a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param bindings2 a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a {@link lupos.datastructures.bindings.Bindings} object.
	 */
	public static Bindings joinBindingsAndReturnBindings(final Bindings bindings1, final Bindings bindings2) {

		// only the keys of the second bindings object have to be
		// checked since the the first bindings object is updated
		for (final Variable b2key : bindings2.getVariableSet()) {

			final Literal literalB2 = bindings2.get(b2key);
			final Literal literalB1 = bindings1.get(b2key);

			// if the literal computed from the second bindings conflicts with
			// the corresponding one located in the first bindings object,
			// null is returned to indicate an unsolvable conflict
			if (literalB1 != null) {
				if (!literalB1.valueEquals(literalB2)) {
					return null;
				}
			}

			// otherwise add the new binding of the key and literal to
			// the first bindings object
			else {
				bindings1.add(b2key, literalB2);
			}

		}

		bindings1.addAllTriples(bindings2);
		bindings1.addAllPresortingNumbers(bindings2);
		return bindings1;
	}
	
	/**
	 * <p>joinBindings_tmp.</p>
	 *
	 * @param result a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param bindings1 a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param bindings2 a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a boolean.
	 */
	public static boolean joinBindings_tmp(final QueryResult result,
			final Bindings bindings1, final Bindings bindings2) {

		for (final Variable b2key : bindings2.getVariableSet()) {
			bindings1.add(b2key, bindings2.get(b2key));
		}

		bindings1.addAllTriples(bindings2);
		bindings1.addAllPresortingNumbers(bindings2);
		result.add(bindings1);
		return true;
	}

	/*
	 * public static QueryResult joinBindings(final QueryResult bindings) {
	 * final Bindings b=Bindings.createNewInstance(); final Iterator<Bindings>
	 * ib=bindings.iterator(); while(ib.hasNext()) { final Bindings c=ib.next();
	 * final Iterator<String> keys=c.getKeys(); while(keys.hasNext()) { final
	 * String k=keys.next(); final Literal value=c.get(k);
	 * 
	 * // join of (k, value) with the rest... final Iterator<Bindings>
	 * ib2=bindings.iterator(); while(ib2.hasNext() && !c.equals(ib2.next())) {
	 * ; }
	 * 
	 * while(ib2.hasNext()) { final Bindings d=ib2.next(); final Literal
	 * value2=d.get(k);
	 * //System.out.println("*************"+BindingsArray.getVariables
	 * ()+":"+k+"="+value+":"+value2); if((value2==null) ||
	 * !value2.valueEquals(value)) { return null; } }
	 * 
	 * b.add(k,value); } } final QueryResult
	 * result=QueryResult.createInstance(); result.add(b); return result; }
	 */

	/**
	 * <p>joinBindings.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult joinBindings(final QueryResult qr) {

		final QueryResult result = QueryResult.createInstance();
		final Iterator<Bindings> ib = qr.iterator();

		if (ib.hasNext()) {
			final Bindings bindings1 = ib.next().clone();
			while (ib.hasNext()) {
				final Bindings bindings2 = ib.next();
				// only the keys of the second bindings object have to be
				// checked since the the first bindings object is updated
				for (final Variable b2key : bindings2.getVariableSet()) {

					final Literal literalB2 = bindings2.get(b2key);
					final Literal literalB1 = bindings1.get(b2key);

					// if the literal computed from the second bindings
					// conflicts with
					// the corresponding one located in the first bindings
					// object,
					// null is returned to indicate an unsolvable conflict
					if (literalB1 != null) {
						if (!literalB1.valueEquals(literalB2)) {
							return null;
						}
					}

					// otherwise add the new binding of the key and literal to
					// the first bindings object
					else {
						bindings1.add(b2key, literalB2);
					}
				}
				bindings1.addAllTriples(bindings2);
				bindings1.addAllPresortingNumbers(bindings2);
			}
			result.add(bindings1);
		}
		if (!result.isEmpty())
			return result;
		else
			return null;
	}

	/**
	 * This method must be overridden by the specific join algorithms. It
	 * supports the reuse of code for the Optional operator.
	 *
	 * @param bindings a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param operandID a int.
	 * @return a {@link lupos.engine.operators.multiinput.optional.OptionalResult} object.
	 */
	@SuppressWarnings("unused")
	public OptionalResult processJoin(final QueryResult bindings, final int operandID) {
		throw (new UnsupportedOperationException("This Operator(" + this
				+ ") should have been replaced before being used."));
	}

	/**
	 * <p>joinBeforeEndOfStream.</p>
	 *
	 * @return a {@link lupos.engine.operators.multiinput.optional.OptionalResult} object.
	 */
	public OptionalResult joinBeforeEndOfStream() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
		if (op instanceof Join) {
			final Join join = (Join) op;
			this.estimatedCardinality = join.estimatedCardinality;
			this.realCardinality = join.realCardinality;
		} else {
			if (!(op instanceof Optional)) {
				System.err
						.println("Join.java: Join (or subclass of Join) or Optional (subclass of Optional) expected, but got "
								+ op);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String result = super.toString() + " on " + this.intersectionVariables;
		if (this.realCardinality >= 0) {
			result += "\nCardinality: " + this.realCardinality;
			if (this.estimatedCardinality >= 0)
				result += " (estimated " + this.estimatedCardinality + ")";
		} else if (this.estimatedCardinality >= 0)
			result += "\nEstminated Cardinality:" + this.estimatedCardinality;
		return result;
	}

}
