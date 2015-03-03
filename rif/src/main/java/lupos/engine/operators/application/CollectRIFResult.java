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
package lupos.engine.operators.application;

import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.rif.datatypes.EqualityResult;
import lupos.rif.datatypes.RuleResult;

public class CollectRIFResult extends CollectResult {

	public CollectRIFResult(final boolean oneTime) {
		super(oneTime);
	}

	protected RuleResult rr;
	protected EqualityResult er;

	@Override
	public void call(final QueryResult res) {
		if (res != null) {
			if (res instanceof EqualityResult) {
				if(this.oneTime){
					if (this.er == null){
						this.er = (EqualityResult) res;
					} else {
						this.er.getEqualityResult().addAll(((EqualityResult) res).getEqualityResult());
					}
				} else {
					if (this.er == null){
						this.er = new EqualityResult();
					}
					this.er.getEqualityResult().addAll(((EqualityResult) res).getEqualityResult());
				}
			} else if (res instanceof RuleResult) {
				if(this.oneTime){
					if (this.rr == null){
						this.rr = (RuleResult) res;
					} else {
						this.rr.getPredicateResults().addAll(((RuleResult) res).getPredicateResults());
					}
				} else {
					if (this.rr == null){
						this.rr = new RuleResult();
					}
					this.rr.getPredicateResults().addAll(((RuleResult) res).getPredicateResults());
				}
			} else {
				super.call(res);
			}
		}
	}

	@Override
	public void start(final Type type) {
		super.start(type);
		this.er = null;
		this.rr = null;
	}

	@Override
	public void stop() {
	}

	/**
	 * get result, if there are several types of QueryResults, one of them is
	 * returned...
	 */
	@Override
	public QueryResult getResult() {
		QueryResult result = this.rr;
		if (result == null) {
			result = this.er;
		}
		if (result == null) {
			return super.getResult();
		} else {
			return result;
		}
	}

	@Override
	public void deleteResult(final QueryResult res) {
		if (res instanceof EqualityResult) {
			if (this.er != null) {
				this.er.removeAll(res);
			}
		} else if (res instanceof RuleResult) {
			if (this.rr != null) {
				this.rr.removeAll(res);
			}
		} super.deleteResult(res);
	}

	@Override
	public void deleteResult() {
		if (this.rr != null) {
			this.rr.release();
		}
		if (this.er != null) {
			this.er.release();
		}
		this.rr = null;
		this.er = null;
		super.deleteResult();
	}

	@Override
	public QueryResult[] getQueryResults() {
		final int size = (this.qr == null ? 0 : 1) + (this.rr == null ? 0 : 1)
				+ (this.er == null ? 0 : 1) + (this.gr == null ? 0 : 1)
				+ (this.br_list == null ? 0 : this.br_list.size());
		final QueryResult[] result = new QueryResult[size];
		int index = 0;
		if (this.qr != null) {
			result[index++] = this.qr;
		}
		if (this.rr != null) {
			result[index++] = this.rr;
		}
		if (this.gr != null) {
			result[index++] = this.gr;
		}
		if (this.er != null) {
			result[index++] = this.er;
		}
		if (this.br_list != null) {
			for (final BooleanResult br : this.br_list) {
				result[index++] = br;
			}
		}
		return result;
	}
}
