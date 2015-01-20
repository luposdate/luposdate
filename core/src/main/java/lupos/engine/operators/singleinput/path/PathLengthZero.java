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
package lupos.engine.operators.singleinput.path;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsCollection;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class PathLengthZero extends SingleInputOperator{
	
	private static final long serialVersionUID = 1L;
	private Variable subject;
	private Variable object;
	private Set<Literal> allowedSubjects;
	private Set<Literal> allowedObjects;
	private Set<Bindings> zeroPairs;
	
	public PathLengthZero(Variable subject, Variable object){
		this.subject = subject;
		this.object = object;
	}
	
	public PathLengthZero(Variable subject, Variable object, Set<Literal> allowedSubjects, Set<Literal> allowedObjects){
		this.subject = subject;
		this.object = object;
		this.allowedSubjects = allowedSubjects;
		this.allowedObjects = allowedObjects;
	}
	
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
				
		final Iterator<Bindings> itb = bindings.getCollection().iterator();
		if(this.zeroPairs==null){
			this.zeroPairs = new HashSet<Bindings>();	
		}
		
		while (itb.hasNext()){
			Bindings bind = itb.next();
			Bindings newBind = new BindingsCollection();
			if((this.allowedSubjects==null || this.allowedSubjects.contains(bind.get(this.subject))) && (this.allowedObjects==null || this.allowedObjects.contains(bind.get(this.subject)))){
				newBind.add(this.subject, bind.get(this.subject));
				newBind.add(this.object, bind.get(this.subject));
				this.zeroPairs.add(newBind);
			}
			
			if((this.allowedSubjects==null || this.allowedSubjects.contains(bind.get(this.object))) && (this.allowedObjects==null || this.allowedObjects.contains(bind.get(this.object)))){
				newBind = new BindingsCollection();
				newBind.add(this.subject, bind.get(this.object));
				newBind.add(this.object, bind.get(this.object));
				this.zeroPairs.add(newBind);
			}
		}
		
		final Iterator<Bindings> itz = this.zeroPairs.iterator();
		QueryResult result = new QueryResult();

		while(itz.hasNext()){
			Bindings zeroPath = itz.next();
			result.add(zeroPath);
		}
		
		return result;
	}
}
