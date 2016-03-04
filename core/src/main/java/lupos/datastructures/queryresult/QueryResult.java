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
package lupos.datastructures.queryresult;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.smallerinmemorylargerondisk.CollectionImplementation;
import lupos.datastructures.smallerinmemorylargerondisk.PagedCollection;
import lupos.engine.operators.index.adaptedRDF3X.MergeIndicesTripleIterator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rdf.Prefix;
public class QueryResult implements Iterable<Bindings>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7478127561182059056L;

	public enum TYPE {DISKBASED,ADAPTIVE, MEMORY}

	/** Constant <code>type</code> */
	public static TYPE type=TYPE.MEMORY;

	/**
	 * <p>createInstance.</p>
	 *
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance() {
			return new QueryResult(type);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(final Bindings bindings){
		final QueryResult result = QueryResult.createInstance();
		result.add(bindings);
		return result;
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param bindings a {@link java.util.Collection} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(final Collection<Bindings> bindings){
		return new QueryResult(bindings);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param memoryLimit a int.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(final int memoryLimit) {
		return new QueryResult(memoryLimit);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param type a {@link lupos.datastructures.queryresult.QueryResult.TYPE} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(final TYPE type) {
		return new QueryResult(type);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param itb a {@link java.util.Iterator} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(
			final Iterator<? extends Bindings> itb) {
		return new IteratorQueryResult(itb);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param itb a {@link lupos.datastructures.queryresult.ParallelIterator} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(
			final ParallelIterator<Bindings> itb) {
		return new ParallelIteratorQueryResult(itb);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param itt a {@link java.util.Iterator} object.
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param order a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(final Iterator<Triple> itt,
			final TriplePattern tp,
			final RDF3XIndexScan.CollationOrder order) {
		return new IteratorQueryResult(itt, tp, order);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param itt a {@link lupos.engine.operators.index.adaptedRDF3X.MergeIndicesTripleIterator} object.
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(
			final MergeIndicesTripleIterator itt, final TriplePattern tp) {
		return new IdIteratorQueryResult(itt, tp);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param queue a {@link lupos.datastructures.parallel.BoundedBuffer} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(final BoundedBuffer<Bindings> queue) {
		return new ParallelIteratorQueryResult(queue);
	}

	protected Collection<Bindings> bindings;

	/**
	 * <p>Constructor for QueryResult.</p>
	 */
	public QueryResult() {
		this.reset();
	}

	/**
	 * <p>Constructor for QueryResult.</p>
	 *
	 * @param type a {@link lupos.datastructures.queryresult.QueryResult.TYPE} object.
	 */
	public QueryResult(final TYPE type) {
		switch (type) {
		default:
		case DISKBASED:
			try {
				this.bindings = new PagedCollection<Bindings>(Bindings.class);
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			break;
		case ADAPTIVE:
			this.bindings = new CollectionImplementation<Bindings>();
			break;
		case MEMORY:
			this.bindings = new LinkedList<Bindings>();
			break;
		}
	}

	/**
	 * <p>Constructor for QueryResult.</p>
	 *
	 * @param bindings a {@link java.util.Collection} object.
	 */
	public QueryResult(final Collection<Bindings> bindings) {
		this.bindings = bindings;
	}

	/**
	 * <p>Constructor for QueryResult.</p>
	 *
	 * @param memoryLimit a int.
	 */
	public QueryResult(final int memoryLimit) {
		if (memoryLimit == 0) {
			try {
				this.bindings = new PagedCollection<Bindings>(Bindings.class);
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		} else {
			this.bindings = new CollectionImplementation<Bindings>(memoryLimit);
		}
	}

	/**
	 * <p>reset.</p>
	 */
	public void reset() {
		if (type == TYPE.MEMORY) {
			this.bindings = new LinkedList<Bindings>();
		} else if (type == TYPE.ADAPTIVE) {
			this.bindings = new CollectionImplementation<Bindings>();
		} else {
			try {
				this.bindings = new PagedCollection<Bindings>(Bindings.class);
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * <p>getCollection.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<Bindings> getCollection() {
		return this.bindings;
	}

	/**
	 * <p>contains.</p>
	 *
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a boolean.
	 */
	public boolean contains(final Bindings b) {
		return this.bindings.contains(b);
	}

	/**
	 * <p>add.</p>
	 *
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a boolean.
	 */
	public boolean add(final Bindings b) {
		return this.bindings.add(b);
	}

	/**
	 * <p>add.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean add(final QueryResult qr) {
		return this.addAll(qr);
	}

	/**
	 * <p>containsAll.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean containsAll(final QueryResult qr) {
		if (qr == null) {
			return false;
		}
		for (final Bindings b : qr) {
			if (!this.contains(b)) {
				return false;
			}
		}
		return true;
	}

	private static enum TYPE_OF_TEST {
		NORMAL, NORMALEXCEPTBLANKS, NORMALEXCEPTBLANKSANDIRIRS, SEMANTICINTERPRETATION, SEMANTICINTERPRETATIONEXCEPTBLANKS, SEMANTICINTERPRETATIONEXCEPTBLANKSANDIRIRS
	}

	private boolean containsAll(final QueryResult qr, final TYPE_OF_TEST type) {
		if (qr == null) {
			return false;
		}
		for (final Bindings b : qr) {
			boolean flag = false;
			for (final Bindings b2 : this){
				switch(type){
					default:
					case NORMAL:
						if (b2.equals(b)) {
							flag = true;
						}
						break;
					case NORMALEXCEPTBLANKS:
						if (b2.equalsExceptAnonymousLiterals(b)) {
							flag = true;
						}
						break;
					case NORMALEXCEPTBLANKSANDIRIRS:
						if (b2.equalsExceptAnonymousLiteralsAndInlineDataIRIs(b)) {
							flag = true;
						}
						break;
					case SEMANTICINTERPRETATION:
						if (b2.semanticallyEquals(b)) {
							flag = true;
						}
						break;
					case SEMANTICINTERPRETATIONEXCEPTBLANKS:
						if (b2.semanticallyEqualsExceptAnonymousLiterals(b)) {
							flag = true;
						}
						break;
					case SEMANTICINTERPRETATIONEXCEPTBLANKSANDIRIRS:
						if (b2.semanticallyEqualsExceptAnonymousLiteralsAndInlineDataIRIs(b)) {
							flag = true;
						}
						break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		return true;
	}


	/**
	 * <p>containsAllExceptAnonymousLiterals.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean containsAllExceptAnonymousLiterals(final QueryResult qr) {
		return this.containsAll(qr, TYPE_OF_TEST.NORMALEXCEPTBLANKS);
	}

	/**
	 * <p>semanticallyContainsAllExceptAnonymousLiteralsAndInlineDataIRIs.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean semanticallyContainsAllExceptAnonymousLiteralsAndInlineDataIRIs(final QueryResult qr) {
		return this.containsAll(qr, TYPE_OF_TEST.NORMALEXCEPTBLANKSANDIRIRS);
	}

	/**
	 * <p>semanticallyContainsAll.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean semanticallyContainsAll(final QueryResult qr) {
		return this.containsAll(qr, TYPE_OF_TEST.SEMANTICINTERPRETATION);
	}

	/**
	 * <p>semanticallyContainsAllExceptAnonymousLiterals.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean semanticallyContainsAllExceptAnonymousLiterals(final QueryResult qr) {
		return this.containsAll(qr, TYPE_OF_TEST.SEMANTICINTERPRETATIONEXCEPTBLANKS);
	}

	/**
	 * <p>containsAllExceptAnonymousLiteralsAndInlineDataIRIs.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean containsAllExceptAnonymousLiteralsAndInlineDataIRIs(final QueryResult qr) {
		return this.containsAll(qr, TYPE_OF_TEST.SEMANTICINTERPRETATIONEXCEPTBLANKSANDIRIRS);
	}

	/**
	 * <p>remove.</p>
	 *
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a boolean.
	 */
	public boolean remove(final Bindings b) {
		return this.bindings.remove(b);
	}

	/**
	 * <p>removeAll.</p>
	 *
	 * @param res a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean removeAll(final QueryResult res) {
		if (res == null) {
			return false;
		}
		boolean success = true;
		for (final Bindings b : res) {
			if (!this.remove(b)) {
				success = false;
			}
		}
		return success;
	}

	/**
	 * <p>addFirst.</p>
	 *
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a boolean.
	 */
	public boolean addFirst(final Bindings b) {
		if (this.bindings instanceof HashSet) {
			return this.bindings.add(b);
		}
		if (this.bindings instanceof TreeSet) {
			return this.bindings.add(b);
		}
		if (this.bindings instanceof LinkedList) {
			((LinkedList<Bindings>) this.bindings).addFirst(b);
			return true;
		}
		return false;
	}

	/**
	 * <p>addLast.</p>
	 *
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a boolean.
	 */
	public boolean addLast(final Bindings b) {
		if (this.bindings instanceof HashSet) {
			return this.bindings.add(b);
		}
		if (this.bindings instanceof TreeSet) {
			return this.bindings.add(b);
		}
		if (this.bindings instanceof LinkedList) {
			((LinkedList<Bindings>) this.bindings).addLast(b);
			return true;
		}
		return false;
	}

	/**
	 * <p>add.</p>
	 *
	 * @param pos a int.
	 * @param b a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a boolean.
	 */
	public boolean add(final int pos, final Bindings b) {
		if (this.bindings instanceof HashSet) {
			return this.bindings.add(b);
		}
		if (this.bindings instanceof TreeSet) {
			return this.bindings.add(b);
		}
		if (this.bindings instanceof LinkedList) {
			((LinkedList<Bindings>) this.bindings).add(pos, b);
			return true;
		}
		return false;
	}

	/**
	 * <p>getFirst.</p>
	 *
	 * @return a {@link lupos.datastructures.bindings.Bindings} object.
	 */
	public Bindings getFirst() {
		final Iterator<Bindings> iter = this.bindings.iterator();
		return iter.next();
	}

	/**
	 * <p>getLast.</p>
	 *
	 * @return a {@link lupos.datastructures.bindings.Bindings} object.
	 */
	public Bindings getLast() {
		final Iterator<Bindings> iter = this.bindings.iterator();
		Bindings ret = null;
		while (iter.hasNext()) {
			ret = iter.next();
		}
		return ret;
	}

	/**
	 * <p>get.</p>
	 *
	 * @param pos a int.
	 * @return a {@link lupos.datastructures.bindings.Bindings} object.
	 */
	public Bindings get(final int pos) {
		if (this.bindings instanceof LinkedList) {
			return ((LinkedList<Bindings>) this.bindings).get(pos);
		}
		final Iterator<Bindings> iter = this.bindings.iterator();
		for (int i = 0; i < this.bindings.size(); i++) {
			if (i == pos) {
				return iter.next();
			}
			iter.next();
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult clone() {
		final QueryResult ret = createInstance();
		ret.reset();
		ret.addAll(this);
		return ret;
	}

	/**
	 * <p>oneTimeSize.</p>
	 *
	 * @return a int.
	 */
	public int oneTimeSize() {
		return this.size();
	}

	/**
	 * <p>size.</p>
	 *
	 * @return a int.
	 */
	public int size() {
		return this.bindings.size();
	}

	/**
	 * Because of optimization purposes, you must call this method only once,
	 * because the iterator returned from a second call of the method may forget
	 * bindings. This is not the case for this QueryResult class, but for some
	 * derived classes like IteratorQueryResult, which are used in order not to
	 * store unnecessarily intermediate data on disk!
	 *
	 * @return a {@link java.util.Iterator} object.
	 */
	public Iterator<Bindings> oneTimeIterator() {
		return this.iterator();
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Bindings> iterator() {
		return this.bindings.iterator();
	}

	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEmpty() {
		return this.bindings.isEmpty();
	}

	/**
	 * <p>addAll.</p>
	 *
	 * @param res a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean addAll(final QueryResult res) {
		if (res == null) {
			return false;
		}
		boolean success = true;
		for (final Bindings b : res) {
			success = this.add(b) && success;
		}
		return success;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.bindings.toString();
	}

	/**
	 * <p>toString.</p>
	 *
	 * @param prefix a {@link lupos.rdf.Prefix} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String toString(final Prefix prefix) {
		String result="[";
		boolean firstTime=true;
		for(final Bindings b: this){
			if(firstTime) {
				firstTime=false;
			} else {
				result+=", ";
			}
			result+=b.toString(prefix);
		}
		return result+"]";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof QueryResult) {
			return this.containsAll((QueryResult) o) && ((QueryResult) o).containsAll(this);
		} else {
			return false;
		}
	}

	/**
	 * <p>sameOrder.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean sameOrder(final QueryResult qr) {
		if (this.equals(qr)) {
			if ((this instanceof BooleanResult && qr instanceof BooleanResult)
					|| (this instanceof GraphResult && qr instanceof GraphResult)) {
				return true;
			}
			final Iterator<Bindings> it2 = qr.iterator();
			for (final Bindings b1 : this) {
				if (it2.hasNext()) {
					final Bindings b2 = it2.next();
					if (!b1.equals(b2)) {
						return false;
					}
				} else {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>sameOrderExceptAnonymousLiterals.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a boolean.
	 */
	public boolean sameOrderExceptAnonymousLiterals(final QueryResult qr) {
		if (this.containsAllExceptAnonymousLiterals(qr) && qr.containsAllExceptAnonymousLiterals(this) && this.size()==qr.size()) {
			if ((this instanceof BooleanResult && qr instanceof BooleanResult)
					|| (this instanceof GraphResult && qr instanceof GraphResult)) {
				return true;
			}
			final Iterator<Bindings> it2 = qr.iterator();
			for (final Bindings b1 : this) {
				if (it2.hasNext()) {
					final Bindings b2 = it2.next();
					if (!b1.equalsExceptAnonymousLiterals(b2)) {
						return false;
					}
				} else {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>getTriples.</p>
	 *
	 * @param lct a {@link java.util.LinkedList} object.
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<Collection<Triple>> getTriples(
			final LinkedList<Collection<Triple>> lct) {
		for (final Bindings b : this.bindings) {
			lct.add(b.getTriples());
		}
		return lct;
	}

	/**
	 * <p>release.</p>
	 */
	public void release() {
		if (!this.bindings.isEmpty()) {
			if (this.bindings instanceof CollectionImplementation) {
				((CollectionImplementation<Bindings>) this.bindings).release();
			} else if (this.bindings instanceof PagedCollection) {
				try {
					((PagedCollection<Bindings>) this.bindings).release();
				} catch (final IOException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * <p>materialize.</p>
	 */
	public void materialize() {
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param it a {@link java.util.Iterator} object.
	 * @param tp a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 * @param collationOrder a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 * @param considerBloomFilters a boolean.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult createInstance(final Iterator<Triple> it,
			final TriplePattern tp, final CollationOrder collationOrder,
			final boolean considerBloomFilters) {
		if (!considerBloomFilters
				|| (LiteralFactory.getMapType() != LiteralFactory.MapType.LAZYLITERAL && LiteralFactory
						.getMapType() != LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
			return createInstance(it, tp, collationOrder);
		} else if (it instanceof SIPParallelIterator) {
			return new IteratorQueryResult(
					(SIPParallelIterator<Triple, TripleKey>) it, tp,
					collationOrder, true);
		} else {
			return createInstance(it, tp, collationOrder);
		}
	}

	/**
	 * <p>removeBindingsBasedOnTriple.</p>
	 *
	 * @param triple a {@link lupos.datastructures.items.Triple} object.
	 * @return a boolean.
	 */
	public boolean removeBindingsBasedOnTriple(final Triple triple) {
		final boolean deleted = false;
		final QueryResult toDelete = QueryResult.createInstance();
		for (final Bindings b : this) {
			if (b.getTriples().contains(triple)) {
				toDelete.add(b);
			}
		}
		this.removeAll(toDelete);
		return toDelete.size() > 0;
	}

	/**
	 * <p>getVariableSet.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Variable> getVariableSet() {
		final HashSet<Variable> variables = new HashSet<Variable>();
		for (final Bindings b : this) {
			variables.addAll(b.getVariableSet());
		}
		return variables;
	}
}
