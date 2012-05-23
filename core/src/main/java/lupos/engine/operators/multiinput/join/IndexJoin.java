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
package lupos.engine.operators.multiinput.join;

import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.multiinput.optional.OptionalResult;

public abstract class IndexJoin extends Join {
	protected Map<String, QueryResult>[] lba;
	protected QueryResult[] cartesianProduct = { QueryResult.createInstance(),
			QueryResult.createInstance() };

	public IndexJoin() {
		super();
		init();
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
		init();
	}

	public abstract void init();

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		final QueryResult result = QueryResult.createInstance();

		int otherOperand = 1-operandID;

		final Iterator<Bindings> itbindings = bindings.oneTimeIterator();
		while (itbindings.hasNext()) {

			final Bindings binding = itbindings.next();
			String keyJoin = "";
			final Iterator<Variable> it = intersectionVariables.iterator();
			while (it.hasNext()) {
				final Literal literal = binding.get(it.next());
				if (literal == null) {
					cartesianProduct[operandID].add(binding);
					// build the cartesian product
					for (final Bindings b2 : cartesianProduct[otherOperand]) {
						joinBindings(result, binding.clone(), b2);
					}

					for (final QueryResult qr : lba[otherOperand].values()) {
						for (final Bindings b2 : qr) {
							joinBindings(result, binding.clone(), b2);
						}
					}

					keyJoin = null;
					break;
				}
				keyJoin += "|" + literal.toString();
			}

			if (keyJoin == null)
				continue;

			QueryResult lb = lba[operandID].get(keyJoin);
			if (lb == null)
				lb = QueryResult.createInstance();
			lb.add(binding);
			lba[operandID].put(keyJoin, lb);

			final QueryResult toJoin = lba[otherOperand].get(keyJoin);
			if (toJoin != null) {

				final Iterator<Bindings> itb = toJoin.iterator();
				while (itb.hasNext()) {
					final Bindings b2 = itb.next();

					joinBindings(result, binding.clone(), b2);
				}
			}

			// build cartesian product
			for (final Bindings b2 : cartesianProduct[otherOperand]) {
				joinBindings(result, binding.clone(), b2);
			}
		}
		if (result.size() == 0)
			return null;
		else {
			if (this.realCardinality < 0)
				this.realCardinality = result.size();
			else
				this.realCardinality += result.size();
			return result;
		}
	}

	@Override
	public synchronized OptionalResult processJoin(final QueryResult bindings,
			final int operandID) {
		// different from process:
		final OptionalResult or = new OptionalResult();
		// different from process:
		final QueryResult joinPartnerFromLeftOperand = QueryResult
				.createInstance();
		final QueryResult result = QueryResult.createInstance();
		int otherOperand = 1-operandID;
		final Iterator<Bindings> itbindings = bindings.oneTimeIterator();
		while (itbindings.hasNext()) {
			final Bindings binding = itbindings.next();
			String keyJoin = "";
			final Iterator<Variable> it = intersectionVariables.iterator();
			while (it.hasNext()) {
				final Literal literal = binding.get(it.next());
				if (literal == null) {
					cartesianProduct[operandID].add(binding);
					// build the cartesian product
					for (final Bindings b2 : cartesianProduct[otherOperand]) {
						if(joinBindings(result, binding.clone(), b2)){
							if (operandID == 1) {
								joinPartnerFromLeftOperand.add(b2);
							} else {
								joinPartnerFromLeftOperand.add(binding);
							}							
						}
					}

					for (final QueryResult qr : lba[otherOperand].values()) {
						for (final Bindings b2 : qr) {
							if(joinBindings(result, binding.clone(), b2)){
								if (operandID == 1) {
									joinPartnerFromLeftOperand.add(b2);
								} else {
									joinPartnerFromLeftOperand.add(binding);
								}								
							}
						}
					}

					keyJoin = null;
					break;
				}
				keyJoin += "|" + literal.toString();
			}

			if (keyJoin == null)
				continue;
			
			QueryResult lb = lba[operandID].get(keyJoin);
			if (lb == null)
				lb = QueryResult.createInstance();
			lb.add(binding);
			lba[operandID].put(keyJoin, lb);

			final QueryResult toJoin = lba[otherOperand].get(keyJoin);
			if (toJoin != null) {

				final Iterator<Bindings> itb = toJoin.iterator();
				while (itb.hasNext()) {
					final Bindings b2 = itb.next();

					// different from process:
					if (joinBindings(result, binding.clone(), b2)) {
						if (operandID == 1) {
							joinPartnerFromLeftOperand.add(b2);
						} else {
							joinPartnerFromLeftOperand.add(binding);
						}
					}
				}
			}
			// build cartesian product
			for (final Bindings b2 : cartesianProduct[otherOperand]) {
				if(joinBindings(result, binding.clone(), b2)){
					if (operandID == 1) {
						joinPartnerFromLeftOperand.add(b2);
					} else {
						joinPartnerFromLeftOperand.add(binding);
					}
				}
			}
		}
		// different from process:
		or.setJoinPartnerFromLeftOperand(joinPartnerFromLeftOperand);
		// different from process:
		or.setJoinResult(result);
		// different from process:
		return or;
	}

	public Map<String, QueryResult>[] getLba() {
		return lba;
	}

	public QueryResult[] getCartesianProduct() {
		return cartesianProduct;
	}

	public void deleteAll(final int operandID) {
		cartesianProduct[operandID].release();
		cartesianProduct[operandID] = QueryResult.createInstance();
		lba[operandID].clear();
	}

	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		final QueryResult result = QueryResult.createInstance();

		int otherOperand;
		if (operandID == 0)
			otherOperand = 1;
		else
			otherOperand = 0;

		final Iterator<Bindings> itbindings = queryResult.oneTimeIterator();
		while (itbindings.hasNext()) {

			final Bindings binding = itbindings.next();
			String keyJoin = "";
			final Iterator<Variable> it = intersectionVariables.iterator();
			while (it.hasNext()) {
				final Literal literal = binding.get(it.next());
				if (literal == null) {
					cartesianProduct[operandID].remove(binding);
					// build the cartesian product
					for (final Bindings b2 : cartesianProduct[otherOperand]) {
						joinBindings(result, binding.clone(), b2);
					}

					for (final QueryResult qr : lba[otherOperand].values()) {
						for (final Bindings b2 : qr) {
							joinBindings(result, binding.clone(), b2);
						}
					}

					keyJoin = null;
					break;
				}
				keyJoin += "|" + literal.toString();
			}

			if (keyJoin == null)
				continue;

			final QueryResult lb = lba[operandID].get(keyJoin);
			if (lb != null)
				lb.remove(binding);
			lba[operandID].put(keyJoin, lb);

			final QueryResult toJoin = lba[otherOperand].get(keyJoin);
			if (toJoin != null) {

				final Iterator<Bindings> itb = toJoin.iterator();
				while (itb.hasNext()) {
					final Bindings b2 = itb.next();

					joinBindings(result, binding.clone(), b2);
				}
			}

			// build cartesian product
			for (final Bindings b2 : cartesianProduct[otherOperand]) {
				joinBindings(result, binding.clone(), b2);
			}
		}
		if (result.size() == 0)
			return null;
		else {
			if (this.realCardinality < 0)
				this.realCardinality = result.size();
			else
				this.realCardinality -= result.size();
			return result;
		}
	}
}
