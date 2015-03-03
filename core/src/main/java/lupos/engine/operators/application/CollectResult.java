
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

	/**
	 * <p>Constructor for CollectResult.</p>
	 *
	 * @param oneTime a boolean.
	 */
	public CollectResult(final boolean oneTime){
		this.oneTime = oneTime;
	}

	/** {@inheritDoc} */
	@Override
	public void call(final QueryResult res) {
		if (res != null) {
			if (res instanceof GraphResult) {
				if(this.oneTime){
					if (this.gr == null){
						this.gr = (GraphResult) res;
					} else {
						this.gr.addAll((GraphResult) res);
					}
				} else {
					if (this.gr == null){
						this.gr = new GraphResult(((GraphResult) res).getTemplate());
					}
					this.gr.addAll((GraphResult) res);
				}
			} else if (res instanceof BooleanResult) {
				if (this.br_list == null){
					this.br_list = new LinkedList<BooleanResult>();
				}
				if(this.oneTime){
					this.br_list.add((BooleanResult)res);
				} else {
					final BooleanResult br = new BooleanResult();
					br.addAll(res);
					this.br_list.add(br);
				}
			} else {
				if(this.oneTime){
					if (this.qr == null){
						this.qr = res;
					} else {
						this.qr.addAll(res);
					}
				} else {
					if (this.qr == null){
						this.qr = QueryResult.createInstance();
					}
					this.qr.addAll(res);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void start(final Type type) {
		this.qr = null;
		final QueryResult qr;
		this.gr = null;
		this.br_list = null;
		this.type = type;
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
	}

	/**
	 * get result, if there are several types of QueryResults, one of them is
	 * returned...
	 *
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public QueryResult getResult() {
		QueryResult result = this.qr;
		if (result == null) {
			result = this.gr;
		}
		if (result == null && this.br_list != null) {
			result = this.br_list.get(0);
		}

		if (result == null) {
			if (this.type == null) {
				return null;
			}
			switch (this.type) {
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

	/** {@inheritDoc} */
	@Override
	public void deleteResult(final QueryResult res) {
		if (res instanceof GraphResult) {
			if (this.gr != null) {
				this.gr.removeAll(res);
			}
		} else if (res instanceof BooleanResult) {
			if (this.br_list != null) {
				for (final BooleanResult br : this.br_list) {
					br.removeAll(res);
				}
			}
		} else if (this.qr != null) {
			this.qr.removeAll(res);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteResult() {
		if (this.qr != null) {
			this.qr.release();
		}
		if (this.gr != null) {
			this.gr.release();
		}
		if (this.br_list != null) {
			for (final BooleanResult br : this.br_list) {
				br.release();
			}
		}
		this.qr = null;
		this.gr = null;
		this.br_list = null;
	}

	/**
	 * <p>getQueryResults.</p>
	 *
	 * @return an array of {@link lupos.datastructures.queryresult.QueryResult} objects.
	 */
	public QueryResult[] getQueryResults() {
		final int size = (this.qr == null ? 0 : 1) + (this.gr == null ? 0 : 1)
				+ (this.br_list == null ? 0 : this.br_list.size());
		final QueryResult[] result = new QueryResult[size];
		int index = 0;
		if (this.qr != null) {
			result[index++] = this.qr;
		}
		if (this.gr != null) {
			result[index++] = this.gr;
		}
		if (this.br_list != null) {
			for (final BooleanResult br : this.br_list) {
				result[index++] = br;
			}
		}
		return result;
	}
}
