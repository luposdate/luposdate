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
package lupos.distributed.storage.distributionstrategy;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class implements the distribution strategy, where the
 * triples are distributed according to the subject, predicate and object
 * (to three different nodes).
 */
public class OneKeyDistribution implements IDistribution<String> {

	@Override
	public String[] getKeysForStoring(Triple triple) {		
		return new String[]{
				"S" + triple.getSubject().originalString(),
				"P" + triple.getPredicate().originalString(),
				"O" + triple.getObject().originalString()
		};
	}

	@Override
	public String[] getKeysForQuerying(TriplePattern triplePattern) throws TriplePatternNotSupportedError {
		if(triplePattern.getSubject().isVariable()){
			if(triplePattern.getObject().isVariable()){
				if(triplePattern.getPredicate().isVariable()){
					// only variables in the triple pattern is not supported!
					throw new TriplePatternNotSupportedError(this, triplePattern);
				} else {
					return new String[]{ "P" + ((Literal)triplePattern.getPredicate()).originalString() };
				}
			} else {
				return new String[]{ "O" + ((Literal)triplePattern.getObject()).originalString() };
			}
		} else {
			return new String[]{ "S" + ((Literal)triplePattern.getSubject()).originalString() };
		}
	}
	
	@Override
	public String toString(){
		return "One key distribution strategy (triple (s, p, o) has keys { 'S' + s, 'P' + p, 'O' + o })";
	}
}
