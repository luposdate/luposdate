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
package lupos.engine.operators.singleinput.sparul;

import java.net.URISyntaxException;

import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class Create extends SingleInputOperator {

	protected final boolean silent;
	
	protected URILiteral uri;
	protected final Dataset dataset;
	
	public Create(Dataset dataset, final boolean isSilent){
		this.dataset=dataset;
		this.silent=isSilent;
	}
	
	public void setURI(URILiteral uri){
		this.uri=uri;
	}
	
	public URILiteral getURI(){
		return uri;
	}

	public QueryResult process(QueryResult bindings, final int operandID) {
		Indices indices = dataset.getNamedGraphIndices(uri);
		if (indices != null){
			if(silent) return null;
			else throw new Error("Named Graph "+uri+" already exists");
		}
//		indices = dataset.getDefaultGraphIndices(uri);
//		if (indices != null && !silent)
//			throw new Error("Default Graph "+uri+" already exists");
		try {
			dataset.addNamedGraph(uri, new StringURILiteral("<inlinedata: >"), false, false);
		} catch (URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}		
		return null;
	}
}
