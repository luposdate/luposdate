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
package lupos.rif.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
public class RuleResult extends QueryResult {
	protected final Collection<Predicate> predicateSet = new HashSet<Predicate>();

	/**
	 * <p>Constructor for RuleResult.</p>
	 */
	public RuleResult() {
		super();
	}
	
	/**
	 * <p>getPredicateIterator.</p>
	 *
	 * @return a {@link java.util.Iterator} object.
	 */
	public Iterator<Predicate> getPredicateIterator(){
		return predicateSet.iterator();
	}

	/**
	 * <p>getPredicateResults.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<Predicate> getPredicateResults() {
		return predicateSet;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return predicateSet.size();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return predicateSet.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return predicateSet.toString();
	}

}
