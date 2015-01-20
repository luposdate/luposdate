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
package lupos.distributed.p2p.network;

import lupos.datastructures.items.Triple;

/**
 * This is a p2p network for distributing {@link Triple}s,
 * and gives access to helper functions to use with {@link Triple}.
 * 
 * @author Bjoern
 * 
 */
public abstract class P2PTripleNetwork extends AbstractP2PNetwork<Triple> {

	/**
	 * Gets the subject of a triple
	 * @param triple triple
	 * @return subject
	 */
	protected String getS(Triple triple) {
		return triple.getSubject().originalString();
	}

	/**
	 * Gets the predicate of a triple
	 * @param triple triple
	 * @return predicate
	 */
	protected String getP(Triple triple) {
		return triple.getPredicate().originalString();
	}

	/**
	 * Gets the object of a triple
	 * @param triple the triple
	 * @return object
	 */
	protected String getO(Triple triple) {
		return triple.getObject().originalString();
	}

	/**
	 * Gets the subject+predicate of a triple
	 * @param triple triple
	 * @return subject+predicate
	 */
	protected String getSP(Triple triple) {
		return triple.getSubject().originalString()
				+ triple.getPredicate().originalString();
	}

	/**
	 * Gets the subject+object of a triple
	 * @param triple triple
	 * @return subject+object
	 */
	protected String getSO(Triple triple) {
		return triple.getSubject().originalString()
				+ triple.getObject().originalString();
	}

	/**
	 * Gets the predicate+object of a triple
	 * @param triple triple
	 * @return predicate+object
	 */
	protected String getPO(Triple triple) {
		return triple.getPredicate().originalString()
				+ triple.getObject().originalString();
	}

	/**
	 * Gets the subject+predicate+object of a triple
	 * @param triple triple
	 * @return subject+predicate+object
	 */
	protected String getSPO(Triple triple) {
		return triple.getPos(0).originalString()
				+ triple.getPredicate().originalString()
				+ triple.getObject().originalString();
	}

}
