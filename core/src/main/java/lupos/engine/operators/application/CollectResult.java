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

import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

public class CollectResult implements Application {
	
	protected final boolean oneTime;
	protected QueryResult qr;
	protected GraphResult gr;
	protected List<BooleanResult> br_list;
	protected Application.Type type;
	
	public CollectResult(final boolean oneTime){
		this.oneTime = oneTime;
	}

	public void call(final QueryResult res) {
		if (res != null) {
			if (res instanceof GraphResult) {
				if(oneTime){
					if (gr == null){
						gr = (GraphResult) res;
					} else {
						gr.addAll((GraphResult) res);
					}
				} else {
					if (gr == null){
						gr = new GraphResult(((GraphResult) res).getTemplate());
					}
					gr.addAll((GraphResult) res);
				}
			} else if (res instanceof BooleanResult) {
				if (br_list == null){
					br_list = new LinkedList<BooleanResult>();
				}
				if(oneTime){
					br_list.add((BooleanResult)res);
				} else {
					final BooleanResult br = new BooleanResult();
					br.addAll(res);
					br_list.add(br);
				}
			} else {
				if(oneTime){
					if (qr == null){
						qr = res;
					} else {
						qr.addAll(res);
					}
				} else {
					if (qr == null){
						qr = QueryResult.createInstance();
					}
					qr.addAll(res);
				}
			}
		}
	}

	public void start(final Type type) {
		qr = null;
		final QueryResult qr;
		gr = null;
		br_list = null;
		this.type = type;
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
		QueryResult result = qr;
		if (result == null)
			result = gr;
		if (result == null && br_list != null)
			result = br_list.get(0);

		if (result == null) {
			if (type == null)
				return null;
			switch (type) {
			case ASK:
				return new BooleanResult();
			case CONSTRUCT:
				return new GraphResult();
			default:
				return QueryResult.createInstance();
			}
		}
		return result;
	}

	public void deleteResult(final QueryResult res) {
		if (res instanceof GraphResult) {
			if (gr != null)
				gr.removeAll(res);
		} else if (res instanceof BooleanResult) {
			if (br_list != null) {
				for (final BooleanResult br : br_list)
					br.removeAll(res);
			}
		} else if (qr != null) {
			qr.removeAll(res);
		}
	}

	public void deleteResult() {
		if (qr != null)
			qr.release();
		if (gr != null)
			gr.release();
		if (br_list != null) {
			for (final BooleanResult br : br_list) {
				br.release();
			}
		}
		qr = null;
		gr = null;
		br_list = null;
	}

	public QueryResult[] getQueryResults() {
		final int size = (qr == null ? 0 : 1) + (gr == null ? 0 : 1)
				+ (br_list == null ? 0 : br_list.size());
		final QueryResult[] result = new QueryResult[size];
		int index = 0;
		if (qr != null)
			result[index++] = qr;
		if (gr != null)
			result[index++] = gr;
		if (br_list != null) {
			for (final BooleanResult br : br_list)
				result[index++] = br;
		}
		return result;
	}
}
