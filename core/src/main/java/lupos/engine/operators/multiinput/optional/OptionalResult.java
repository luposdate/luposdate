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
package lupos.engine.operators.multiinput.optional;

import lupos.datastructures.queryresult.QueryResult;
public class OptionalResult {
	private QueryResult joinResult;
	private QueryResult joinPartnerFromLeftOperand;

	/**
	 * <p>Constructor for OptionalResult.</p>
	 *
	 * @param joinResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param joinPartnerFromLeftOperand a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public OptionalResult(final QueryResult joinResult,
			final QueryResult joinPartnerFromLeftOperand) {
		this.joinResult = joinResult;
		this.joinPartnerFromLeftOperand = joinPartnerFromLeftOperand;
	}

	/**
	 * <p>Constructor for OptionalResult.</p>
	 */
	public OptionalResult() {
		// no initializations
	}

	/**
	 * <p>Getter for the field <code>joinResult</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public QueryResult getJoinResult() {
		return this.joinResult;
	}

	/**
	 * <p>Setter for the field <code>joinResult</code>.</p>
	 *
	 * @param joinResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public void setJoinResult(final QueryResult joinResult) {
		this.joinResult = joinResult;
	}

	/**
	 * <p>Getter for the field <code>joinPartnerFromLeftOperand</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public QueryResult getJoinPartnerFromLeftOperand() {
		return this.joinPartnerFromLeftOperand;
	}

	/**
	 * <p>Setter for the field <code>joinPartnerFromLeftOperand</code>.</p>
	 *
	 * @param joinPartnerFromLeftOperand a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public void setJoinPartnerFromLeftOperand(
			final QueryResult joinPartnerFromLeftOperand) {
		this.joinPartnerFromLeftOperand = joinPartnerFromLeftOperand;
	}

	/**
	 * <p>addAll.</p>
	 *
	 * @param or a {@link lupos.engine.operators.multiinput.optional.OptionalResult} object.
	 */
	public void addAll(final OptionalResult or) {
		this.joinResult.addAll(or.joinResult);
		this.joinPartnerFromLeftOperand.addAll(or.joinPartnerFromLeftOperand);
	}

	/**
	 * <p>release.</p>
	 */
	public void release() {
		this.joinResult.release();
		this.joinPartnerFromLeftOperand.release();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "joinResult: " + this.joinResult + " joinPartnerFromLeftOperand: "
				+ this.joinPartnerFromLeftOperand;
	}
}
