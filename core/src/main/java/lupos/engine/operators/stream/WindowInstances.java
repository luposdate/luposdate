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
package lupos.engine.operators.stream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.TimestampedTriple;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.misc.debug.DebugStep;
import lupos.rdf.Prefix;

public abstract class WindowInstances extends Window {
	
	private final Literal RDF_TYPE = LiteralFactory.createURILiteralWithoutException("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"); 
	
	protected final Literal instanceClass;
	
	/**
	 * Buffer for all incoming triples, used like a queue (new triples get appended to end of list)
	 */
	protected LinkedList<TimestampedTriple> tripleBuffer;
	protected LinkedList<TimestampedTriple> typeTripleBuffer;
	
	
	public WindowInstances(final Literal instanceClass){
		this.instanceClass = instanceClass;
	}
	
	/**
	 * Checks if given triple has predicate==rdf:type and object==instanceClass
	 */
	protected boolean isMatchingTypeTriple(Triple t) {
		return t.getPredicate().compareToNotNecessarilySPARQLSpecificationConform(this.RDF_TYPE) == 0
				&& t.getObject().compareToNotNecessarilySPARQLSpecificationConform(this.instanceClass)==0;
	}
	
	protected boolean haveSameSubject(Triple t1, Triple t2) {
		if(t1==null || t2==null){
			return false;
		}
		return 0==t1.getSubject().compareToNotNecessarilySPARQLSpecificationConform(t2.getSubject());
	}
	
	/**
	 * Deletes all triples which have the same subject as a given triple t.
	 * @param t
	 */
	protected void deleteInstance(Triple t) {
		// 1. search for triples with same subject
		List<TimestampedTriple> instanceTriples = new ArrayList<TimestampedTriple>();
		for(TimestampedTriple tmp : this.tripleBuffer) {
			if(haveSameSubject(tmp,t)){
				instanceTriples.add(tmp);
			}
		}
		// 2. delete them
		for(TimestampedTriple tmp : instanceTriples) {
			this.tripleBuffer.remove(tmp);
			super.deleteTriple(tmp);
		}
	}
	
	protected void deleteInstanceDebug(Triple t, DebugStep debugstep) {
		// 1. search for triples with same subject
		List<TimestampedTriple> instanceTriples = new ArrayList<TimestampedTriple>();
		for(TimestampedTriple tmp : this.tripleBuffer) {
			if(haveSameSubject(tmp,t)){
				instanceTriples.add(tmp);
			}
		}
		// 2. delete them
		for(TimestampedTriple tmp : instanceTriples) {
			this.tripleBuffer.remove(tmp);
			super.deleteTripleDebug(tmp, debugstep);
		}
	}
	
	@Override
	public String toString(){
		return super.toString()+" on "+this.instanceClass.toString();
	}

	@Override
	public String toString(Prefix prefixInstance){
		return super.toString()+" on "+this.instanceClass.toString(prefixInstance);
	}
}
