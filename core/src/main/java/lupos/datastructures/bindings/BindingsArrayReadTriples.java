/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.datastructures.bindings;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.rdf.Prefix;

public class BindingsArrayReadTriples extends BindingsArray {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5634651341313907266L;

	protected List<Triple> readTriples = new LinkedList<Triple>();

	@Override
	public BindingsArrayReadTriples clone() {
		final BindingsArrayReadTriples other = new BindingsArrayReadTriples();
		// System.arraycopy(this.literals, 0, other.literals, 0,
		// this.literals.length);
		other.cloneLiterals(getLiterals());
		other.readTriples.addAll(readTriples);

		return other;
	}

	/**
	 * This method adds a triple to the internal list of read triples for these
	 * bindings
	 */
	@Override
	public void addTriple(final Triple triple) {
		readTriples.add(triple);
	}

	/**
	 * This method adds all triples to the internal list of read triples for
	 * these bindings. This method must be overridden by Bindings-subclasses,
	 * which support this feature, e.g. BindingsArrayReadTriples
	 */
	@Override
	public void addAllTriples(final Collection<Triple> triples) {
		if (triples != null)
			readTriples.addAll(triples);
	}

	/**
	 * This method adds all triples of a given Bindings to the internal list of
	 * read triples for these bindings. This method must be overridden by
	 * Bindings-subclasses, which support this feature, e.g.
	 * BindingsArrayReadTriples
	 */
	@Override
	public void addAllTriples(final Bindings bindings) {
		addAllTriples(bindings.getTriples());
	}

	/**
	 * This method returns the internal list of read triples for these bindings.
	 */
	@Override
	public List<Triple> getTriples() {
		return readTriples;
	}

	@Override
	public String toString() {
		return super.toString() + " read triples:" + readTriples + "\n";
	}
	
	@Override
	public String toString(Prefix prefix) {
		String result = super.toString(prefix) + " read triples: [";
		boolean firstTime=true;
		for(Triple t: readTriples){
			if(firstTime)
				firstTime=false;
			else result+=", ";
			result+=t.toString(prefix);
		}
		return result+"]";
	}


	@Override
	public void init() {
		super.init();
		readTriples = new LinkedList<Triple>();
	}

	public void sortReadTriples() {
		final SortedSet<Triple> sst = new TreeSet<Triple>(new TripleComparator(
				RDF3XIndexScan.CollationOrder.SPO));
		sst.addAll(readTriples);
		readTriples.clear();
		readTriples.addAll(sst);
	}
}
