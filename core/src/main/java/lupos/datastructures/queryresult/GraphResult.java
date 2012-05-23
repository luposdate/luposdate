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
package lupos.datastructures.queryresult;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.BlankNode;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class GraphResult extends QueryResult {
	private static final long serialVersionUID = -8408070282837185178L;

	private Collection<TriplePattern> template;

	private Collection<Triple> triples = new HashSet<Triple>();

	// for blank node generation:
	private final HashSet<AnonymousLiteral> alreadyUsedBlankNodes = new HashSet<AnonymousLiteral>();
	private int bnodeid = 0;

	public GraphResult() {
		reset();
	}

	@Override
	public void reset() {
		super.reset();
		template = new LinkedList<TriplePattern>();
		triples = new LinkedList<Triple>();
	}

	public GraphResult(final Collection<TriplePattern> template/*
																 * , boolean
																 * sorted
																 */) {
		super(/* sorted, false */);
		this.template = template;
		addConstantTriples();
	}

	protected void addConstantTriples() {
		for (final TriplePattern tp : template) {
			if (!tp.getPos(0).isVariable() && !tp.getPos(1).isVariable()
					&& !tp.getPos(2).isVariable())
				triples.add(new Triple((Literal) tp.getPos(0), (Literal) tp
						.getPos(1), (Literal) tp.getPos(2)));
		}
	}

	@Override
	public GraphResult clone() {
		final GraphResult result = new GraphResult();
		result.reset();
		result.addAll(this);
		result.template.addAll(this.template);
		result.triples.addAll(this.triples);
		return result;
	}

	@Override
	public boolean add(final Bindings bindings) {
		final HashMap<BlankNode, AnonymousLiteral> assignedBlankNodes = new HashMap<BlankNode, AnonymousLiteral>();
		if (template != null) {
			for (final TriplePattern tp : template) {
				final Triple trip = new Triple();
				for (int i = 0; i < 3; i++) {
					if (tp.getPos(i) instanceof BlankNode) {
						final BlankNode bn = (BlankNode) tp.getPos(i);
						AnonymousLiteral al = assignedBlankNodes.get(bn);
						if (al == null) {
							do {
								al = new AnonymousLiteral("_:_constructedBN"
										+ bnodeid++);
							} while (alreadyUsedBlankNodes.contains(al));
							assignedBlankNodes.put(bn, al);
						}
						trip.setPos(i, al);
					} else
						trip.setPos(i, tp.getPos(i).getLiteral(bindings));
				}
				if (trip.getPos(0) != null && trip.getPos(1) != null
						&& trip.getPos(2) != null)
					triples.add(trip);
			}
		}
		return super.add(bindings);
	}

	@Override
	public boolean add(final QueryResult qr) {
		// first determine already used blank nodes!
		for (final Bindings b : qr) {
			for (final Variable v : b.getVariableSet()) {
				final Literal l = b.get(v);
				if (l instanceof AnonymousLiteral)
					alreadyUsedBlankNodes.add((AnonymousLiteral) l);
			}
		}
		boolean flag = true;
		for (final Bindings b : qr)
			flag = flag && add(b);
		return flag;
	}

	public void addAll(final GraphResult gr) {
		this.template = null;
		add(gr);
		if (gr.triples != null) {
			for (final Triple t : gr.triples) {
				addGraphResultTriple(t);
			}
		}
	}

	@Override
	public String toString() {
		return triples.toString();
	}

	public Collection<Triple> getGraphResultTriples() {
		return triples;
	}

	public void addGraphResultTriple(final Triple t) {
		if (t.getPos(0) != null && t.getPos(1) != null && t.getPos(2) != null)
			triples.add(t);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof GraphResult) {
			final Collection<Triple> ct = ((GraphResult) o)
					.getGraphResultTriples();
			return triples.containsAll(ct) && ct.containsAll(triples);
		} else
			return false;
	}

	public Collection<TriplePattern> getTemplate() {
		return template;
	}

	public void setTemplate(Collection<TriplePattern> template) {
		this.template=template;
	}

	@Override
	public int size() {
		return triples.size();
	}

	public boolean containsAllExceptAnonymousLiterals(final QueryResult o) {
		if (o instanceof GraphResult) {
			final Collection<Triple> triples2 = ((GraphResult) o).triples;
			for (final Triple t1 : this.triples) {
				boolean flag = false;
				for (final Triple t2 : triples2) {
					if (t2.equivalentExceptAnonymousLiterals(t1)) {
						flag = true;
						break;
					}
				}
				if (!flag)
					return false;
			}
			return true;
		} else
			return false;
	}
}
