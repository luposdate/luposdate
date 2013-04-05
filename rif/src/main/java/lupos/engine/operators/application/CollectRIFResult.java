/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
	
	public CollectRIFResult(boolean oneTime) {
		super(oneTime);
	}

	protected RuleResult rr;
	protected EqualityResult er;

	public void call(final QueryResult res) {
		if (res != null) {
			if (res instanceof EqualityResult) {
				if(oneTime){
					if (er == null){
						er = (EqualityResult) res;
					} else {
						er.getEqualityResult().addAll(((EqualityResult) res).getEqualityResult());
					}
				} else {
					if (er == null){
						er = new EqualityResult();
					}
					er.getEqualityResult().addAll(((EqualityResult) res).getEqualityResult());
				}
			} else if (res instanceof RuleResult) {
				if(oneTime){
					if (rr == null){
						rr = (RuleResult) res;
					} else {
						rr.getPredicateResults().addAll(((RuleResult) res).getPredicateResults());
					}
				} else {
					if (rr == null){
						rr = new RuleResult();
					}
					rr.getPredicateResults().addAll(((RuleResult) res).getPredicateResults());
				}
			} else super.call(res);
		}
	}

	public void start(final Type type) {
		super.start(type);
		er = null;
		rr = null;
	}

	public void stop() {
	}

	/**
	 * get result, if there are several types of QueryResults, one of them is
	 * returned...
	 * 
	 * @return
	 */
	public QueryResult getResult() {
		QueryResult result = rr;
		if (result == null)
			result = er;		
		if (result == null)
			return super.getResult();
		else return result;
	}

	public void deleteResult(final QueryResult res) {
		if (res instanceof EqualityResult) {
			if (er != null)
				er.removeAll(res);
		} else if (res instanceof RuleResult) {
			if (rr != null)
				rr.removeAll(res);
		} super.deleteResult(res);
	}

	public void deleteResult() {
		if (rr != null)
			rr.release();
		if (er != null)
			er.release();
		rr = null;
		er = null;
		super.deleteResult();
	}

	public QueryResult[] getQueryResults() {
		final int size = (qr == null ? 0 : 1) + (rr == null ? 0 : 1)
				+ (er == null ? 0 : 1) + (gr == null ? 0 : 1)
				+ (br_list == null ? 0 : br_list.size());
		final QueryResult[] result = new QueryResult[size];
		int index = 0;
		if (qr != null)
			result[index++] = qr;
		if (rr != null)
			result[index++] = rr;
		if (gr != null)
			result[index++] = gr;
		if (er != null)
			result[index++] = er;
		if (br_list != null) {
			for (final BooleanResult br : br_list)
				result[index++] = br;
		}
		return result;
	}
}
