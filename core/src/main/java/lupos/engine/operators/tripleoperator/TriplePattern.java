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
package lupos.engine.operators.tripleoperator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.messages.BindingsFactoryMessage;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.SIPFilterOperator;
import lupos.engine.operators.stream.TripleDeleter;
import lupos.misc.BitVector;
import lupos.misc.debug.DebugStep;
import lupos.misc.util.ImmutableIterator;

public class TriplePattern extends TripleOperator implements TripleConsumer,
TripleDeleter, Iterable<Item> {

	private static final long serialVersionUID = 6269668218008457381L;

	protected long cardinality = -1;

	protected BitVector[] bloomFilters = null;

	protected BindingsFactory bindingsFactory;

	public BitVector[] getBloomFilters() {
		return this.bloomFilters;
	}

	public void setBloomFilters(final BitVector[] bloomFilters) {
		this.bloomFilters = bloomFilters;
	}

	public void recomputeVariables(){
		this.intersectionVariables = new HashSet<Variable>();
		this.unionVariables = new HashSet<Variable>();
		for (final Item i : this.items) {
			if (i.isVariable()) {
				this.intersectionVariables.add((Variable) i);
				this.unionVariables.add((Variable) i);
			}
		}
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		this.recomputeVariables();
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().addAll(this.unionVariables);
		/*
		 * if(rdfGraph!=null && rdfGraph.isVariable()) {
		 * intersectionVariables.add((Variable)rdfGraph);
		 * unionVariables.add((Variable)rdfGraph); }
		 */
		return result;
	}

	protected void computeVariables() {
		this.intersectionVariables = new HashSet<Variable>();
		this.unionVariables = new HashSet<Variable>();
		for (final Item i : this.items) {
			if (i != null && i.isVariable()) {
				this.intersectionVariables.add((Variable) i);
				this.unionVariables.add((Variable) i);
			}
		}
	}

	protected Item[] items = new Item[3];

	protected HashMap<BasicOperator, LinkedList<Variable>> projectionPresortingNumbers = null;

	protected HashMap<Integer, Container> minMaxPresortingNumbers = null;

	protected int idmax = 0;

	protected Object[] order1 = null;
	protected Object[] order2 = null;
	protected int[][] min = null;
	protected int[][] max = null;

	protected HashMap<Variable, Literal[]> histogram;
	protected HashMap<Variable, Double> histogramSel;

	public enum BooleanAndUnknown {
		TRUE, FALSE, UNKNOWN
	};

	protected BooleanAndUnknown objectOriginalStringMayDiffer = BooleanAndUnknown.UNKNOWN;

	private class Container {
		public int min;
		public int max;
	}

	public TriplePattern(final boolean[] variable, final Literal[] literal,
			final String[] varname) {
		for (int i = 0; i < 3; i++) {
			if (variable[i]) {
				this.items[i] = new Variable(varname[i]);
			} else {
				this.items[i] = literal[i];
			}
		}
		this.computeVariables();
	}

	public TriplePattern(final Item i1, final Item i2, final Item i3) {
		this.items[0] = i1;
		this.items[1] = i2;
		this.items[2] = i3;
		this.computeVariables();
	}

	public TriplePattern() {
	}

	public TriplePattern(final Item[] items2) {
		this.items = items2;
		this.computeVariables();
	}

	@Override
	public TriplePattern clone() {
		final TriplePattern clone = new TriplePattern(this.items[0],
				this.items[1], this.items[2]);
		clone.setSucceedingOperators(this.succeedingOperators);
		if (this.projectionPresortingNumbers != null) {
			for (final BasicOperator bo : this.projectionPresortingNumbers
					.keySet()) {
				final LinkedList<Variable> li = new LinkedList<Variable>();
				li.addAll(this.projectionPresortingNumbers.get(bo));
				clone.projectionPresortingNumbers.put(bo, li);
			}
		}
		return clone;
	}

	public Item getPos(final int i) {
		return this.items[i];
	}

	public Item getSubject(){
		return this.items[0];
	}

	public Item getPredicate(){
		return this.items[1];
	}

	public Item getObject(){
		return this.items[2];
	}

	public void setPos(final Item item, final int i) {
		this.items[i] = item;
		this.computeVariables();
	}

	public Item[] getItems() {
		return this.items;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ImmutableIterator<Item>() {
			int pos = 0;

			@Override
			public boolean hasNext() {
				return this.pos <= 2;
			}

			@Override
			public Item next() {
				return TriplePattern.this.items[this.pos++];
			}
		};
	}

	/*
	 * public Bindings process (QueryResult qresult, Bindings bindings, final
	 * Triple triple){
	 *
	 * Literal literal = null; boolean altered = false;
	 *
	 * // process all items of this triple pattern for(int i = 0; i < 3; i++){
	 *
	 * // if the item is a variable if(items[i].isVariable()){
	 *
	 * // if the item is an unbound variable final String itemName =
	 * items[i].getName(); if((literal = bindings.get(itemName)) == null) {
	 * bindings.add(itemName,triple.getPos(i)); altered = true; }
	 *
	 * // if the item is a variable which is already bound // and the value
	 * differs from the value of the triple // which would be used as binding, a
	 * conflict was // detected else if(!literal.valueEquals(triple.getPos(i)))
	 * { bindings = bindings.clone(); bindings.add(itemName,triple.getPos(i));
	 * altered = true; } }
	 *
	 * // if the item is no variable ... else {
	 *
	 * // its value has to be equal to the corresponding value of // the triple
	 * pattern if(!items[i].getLiteral(null).valueEquals(triple.getPos(i))) {
	 * return null; } } } if (altered){ bindings.addTriple(triple);
	 * qresult.add(bindings); } return bindings; }
	 */
	@Override
	public void consume(final Triple triple) {
		final Bindings b = this.process(triple, false);
		if (b != null) {
			// System.out.println(b+" result of "+tp[i]);
			final QueryResult ll = QueryResult.createInstance();
			ll.add(b);
			for (final OperatorIDTuple op : this.getSucceedingOperators()) {
				((Operator) op.getOperator()).processAll(ll, op.getId());
			}
		}
	}

	public Bindings process(final Triple triple, final boolean failOnBlank) {
		return this.process(triple, failOnBlank, 0);
	}

	@Override
	public Message preProcessMessage(final BindingsFactoryMessage msg){
		this.bindingsFactory = msg.getBindingsFactory();
		return msg;
	}

	public void setBindingsFactory(final BindingsFactory bindingsFactory){
		this.bindingsFactory = bindingsFactory;
	}

	public Bindings process(final Triple triple, final boolean failOnBlank,
			final int id) {

		final Bindings bindings = this.bindingsFactory.createInstance();
		Literal literal = null;

		// process all items of this triple pattern
		for (int i = 0; i < 3; i++) {

			if (this.bloomFilters != null) {
				if (this.bloomFilters[i] != null) {
					if (!this.bloomFilters[i]
					                  .get((Math.abs(triple.getPos(i).hashCode()) % SIPFilterOperator.NUMBEROFBITSFORBLOOMFILTER))) {
						return null;
					}
				}
			}

			// if the triple's node at the current position is a blank
			// one which is not allowed due to the current configuration
			// return null to indicate this problem
			if (failOnBlank && triple.getPos(i).isBlank()) {
				return null;
			}

			// if the item is a variable
			if (this.items[i].isVariable()) {

				// if the item is an unbound variable
				final Variable item = triple.getVariable((Variable) this.items[i]);
				if ((literal = bindings.get(item)) == null) {
					bindings.add(item, triple.getPos(i));
				}

				// if the item is a variable which is already bound
				// and the value differs from the value of the triple
				// which would be used as binding, a conflict was
				// detected
				else if (!literal.valueEquals(triple.getPos(i))) {
					return null; // join within triple pattern!
				}
			}

			// if the item is no variable ...
			else {

				// its value has to be equal to the corresponding value of
				// the triple pattern
				try {
					if (!this.items[i].getLiteral(null)
							.valueEquals(triple.getPos(i))) {
						return null;
					}
				} catch (final Exception e) {
					System.out.println(e);
					e.printStackTrace();
					return null;
				}
			}
		}
		bindings.addTriple(triple);
		if (this.projectionPresortingNumbers != null) {
			if (this.order1 == null) {
				final int i = 0;
				for (final BasicOperator bo : this.projectionPresortingNumbers.keySet()) {
					final LinkedList<Variable> li = this.projectionPresortingNumbers.get(bo);

					// else if (triple instanceof SortedTripleElement) {
					// final Object o =
					// lupos.engine.operators.index.mergeJoin.MergeJoinIndex
					// .getSortPatterns4OptimizedJoin(this,
					// projectionPresortingNumbers);
					// bindings
					// .addPresortingNumber(
					// this,
					// o,
					// ((SortedTripleElement) triple)
					// .getPosition((SortedTripleElement.ORDER_PATTERN) o),
					// ((SortedTripleElement) triple).getMax());
					// }
				}
				// System.out.println(this);
				// System.out.println("order1, size " + order1.length + ":");
				// for (int j = 0; j < order1.length; j++) {
				// System.out.print(order1[j] + ", ");
				// }
				// System.out.println();
			}
		} else if (bindings instanceof BindingsArrayVarMinMax
				&& this.minMaxPresortingNumbers != null) {
			// this can only be reached for --optimization
			// MergeJoinSortLazyLiteral --codemap LazyLiteral
			final Map<Variable, Integer> hmvi = this.bindingsFactory.getPosVariables();
			for (final Variable v : bindings.getVariableSet()) {
				final Container container = this.minMaxPresortingNumbers.get(hmvi
						.get(v));
				if (container != null) {
					((BindingsArrayVarMinMax) bindings).addMinMax(v,
							container.min, container.max);
				}
			}
		}
		return bindings;
	}

	private int sizeOfProjectionPresortingNumbers() {
		int size = 0;

		for (final BasicOperator bo : this.projectionPresortingNumbers.keySet()) {
			size += this.projectionPresortingNumbers.get(bo).size();
		}

		return size;
	}

	public void addProjectionPresortingNumbers(final Variable v,
			final BasicOperator basicoperator) {
		if (this.projectionPresortingNumbers == null) {
			this.projectionPresortingNumbers = new HashMap<BasicOperator, LinkedList<Variable>>();
		}
		LinkedList<Variable> li = this.projectionPresortingNumbers
		.get(basicoperator);
		if (li == null) {
			li = new LinkedList<Variable>();
		}
		if (!li.contains(v)) {
			li.add(v);
		}
		this.projectionPresortingNumbers.put(basicoperator, li);
	}

	public void resetProjectionPresortingNumbers() {
		this.projectionPresortingNumbers = null;
	}

	public void addMinMaxLazyLiteral(final Variable v, final int min,
			final int max) {
		if (this.minMaxPresortingNumbers == null) {
			this.minMaxPresortingNumbers = new HashMap<Integer, Container>();
		}
		final Container container = new Container();
		container.min = min;
		container.max = max;
		this.minMaxPresortingNumbers.put(this.bindingsFactory.getPosVariables().get(v),
				container);
	}

	public void addMinMaxLazyLiteral(final int varcode, final int min,
			final int max) {
		if (this.minMaxPresortingNumbers == null) {
			this.minMaxPresortingNumbers = new HashMap<Integer, Container>();
		}
		final Container container = new Container();
		container.min = min;
		container.max = max;
		this.minMaxPresortingNumbers.put(varcode, container);
	}

	public void addMinMaxPresortingNumbers(final int order, final int ordermax,
			final int id, final int min, final int max) {
		if (this.minMaxPresortingNumbers == null) {
			this.minMaxPresortingNumbers = new HashMap<Integer, Container>();
		}
		final Container container = new Container();
		container.min = min;
		container.max = max;
		this.minMaxPresortingNumbers.put(order + id * ordermax, container);
		if (id + 1 > this.idmax) {
			this.idmax = id + 1;
		}
	}

	public HashSet<String> getVariableNames() {
		final HashSet<String> h = new HashSet<String>();
		for (final Item i : this.items) {
			if (i.isVariable()) {
				h.add(i.getName());
			}
		}
		return h;
	}

	public HashSet<Variable> getVariables() {
		final HashSet<Variable> h = new HashSet<Variable>();
		for (final Item i : this.items) {
			if (i.isVariable()) {
				h.add((Variable) i);
			}
		}
		return h;
	}

	public String getVariableName(final int i) {
		return this.items[i].getName();
	}

	public int getPos(final Item item) {
		int pos = 0;
		for (final Item i : this.items) {
			if (i.equals(item)) {
				return pos;
			}
			pos++;
		}
		return -1;
	}

	public String getLiteralKey() {
		String key = "";
		for (int i = 0; i < 3; i++) {
			if (!this.items[i].isVariable()) {
				key += this.items[i].toString();
			}
			if (i < 2) {
				key += "|";
			}
		}
		return key;
	}

	@Override
	public String toString() {
		final StringBuffer result = new StringBuffer(super.toString()+" (");

		for (int i = 0; i < 3; i++) {
			result.append(this.items[i]);

			if (i < 2) {
				result.append(", ");
			}
		}

		return result.toString() + ")" + this.getCardinalityString();
	}

	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		final StringBuffer result = new StringBuffer(super.toString()+" ("); // start result string

		for (int i = 0; i < 3; i++) { // walk through items...
			if (this.items[i] instanceof Literal) {
				result.append(((Literal) this.items[i]).toString(prefixInstance));
			} else {
				result.append(prefixInstance.add(this.items[i].toString())); // add item to result string
			}
			if (i < 2) { // add ", " between the items...
				result.append(", ");
			}
		}

		return result.toString() + ")" + this.getCardinalityString();
	}

	public String toN3String() {
		final StringBuffer result = new StringBuffer();

		for (int i = 0; i < 3; i++) {
			result.append(this.items[i].toString());
			result.append(" ");
		}

		result.append(".");

		return result.toString();
	}

	public String toN3String(final lupos.rdf.Prefix prefixInstance) {
		final StringBuffer result = new StringBuffer(); // start result string

		for (int i = 0; i < 3; i++) { // walk through items...
			if (this.items[i] instanceof Literal) {
				result.append(((Literal) this.items[i]).toString(prefixInstance));
			} else {
				result.append(prefixInstance.add(this.items[i].toString()));// add item to result string
			}
			result.append(" ");
		}
		result.append(".");

		return result.toString();
	}


	private String getCardinalityString() {
		if (this.cardinality >= 0) {
			return "\nCardinality: " + this.cardinality;
		} else {
			return "";
		}
	}

	/*
	 * public boolean matches(final Generate g) { for(int i=0; i<3; i++) {
	 * if(!items[i].isVariable()) { if(!g.matched(i,items[i].getLiteral(null)))
	 * { return false; } } } return true; }
	 */

	/*
	 * strictly matching means that every triple generated by g will be matched
	 * by this triple pattern...
	 */
	/*
	 * public boolean strictlyMatches(final Generate g) { for(int i=0; i<3; i++)
	 * { if(!items[i].isVariable()) {
	 * if(!g.strictlyMatched(i,items[i].getLiteral(null))) { return false; } }
	 * else if((g.getType())[i]==Generate.blankNode) { return false; } } return
	 * true; }
	 */

	public Set<Variable> replace(final Variable var, final Item item) {
		final HashSet<Variable> vars = new HashSet<Variable>();

		for (int i = 0; i < 3; i++) {
			if (this.items[i].isVariable() && var.equalsNormalOrVariableInInferenceRule(this.items[i])) {
				vars.add((Variable) this.items[i]);

				this.items[i] = item;
			}
		}

		this.computeVariables();

		return vars;
	}

	public void addHistogram(final Variable v, final Literal[] buckets,
			final double selectivity) {
		if (this.histogram == null) {
			this.histogram = new HashMap<Variable, Literal[]>();
			this.histogramSel = new HashMap<Variable, Double>();
		}
		this.histogram.put(v, buckets);
		this.histogramSel.put(v, selectivity);
	}

	public Literal[] getHistogram(final Variable v) {
		if (this.histogram == null) {
			return null;
		}
		return this.histogram.get(v);
	}

	public Double getSel(final Variable v) {
		if (this.histogram == null) {
			return null;
		}
		return this.histogramSel.get(v);
	}

	public BooleanAndUnknown getObjectOriginalStringMayDiffer() {
		return this.objectOriginalStringMayDiffer;
	}

	public void setObjectOriginalStringMayDiffer(
			final BooleanAndUnknown objectOriginalStringMayDiffer) {
		this.objectOriginalStringMayDiffer = objectOriginalStringMayDiffer;
	}

	public long getCardinality() {
		return this.cardinality;
	}

	public void setCardinality(final long cardinality) {
		this.cardinality = cardinality;
	}

	public TripleKey getKey(final Bindings k, final RDF3XIndexScan.CollationOrder order) {
		final Literal[] literals = new Literal[3];
		for (int i = 0; i < 3; i++) {
			if (!this.items[i].isVariable()) {
				literals[i] = (Literal) this.items[i];
			} else {
				literals[i] = k.get((Variable) this.items[i]);
			}
		}
		final TripleKey tk = new TripleKey(new Triple(literals[0], literals[1],
				literals[2]), order);
		return tk;
	}

	public TripleKey getKey(final Literal[] t, final RDF3XIndexScan.CollationOrder order) {
		final Literal[] literals = new Literal[3];
		for (int i = 0; i < 3; i++) {
			if (!this.items[i].isVariable()) {
				literals[i] = (Literal) this.items[i];
			} else {
				literals[i] = t[i];
			}
		}
		final TripleKey tk = new TripleKey(new Triple(literals[0], literals[1],
				literals[2]), order);
		return tk;
	}

	@Override
	public void deleteTriple(final Triple triple) {
		final Bindings b = this.process(triple, false);
		if (b != null) {
			// System.out.println(b+" result of "+tp[i]);
			final QueryResult ll = QueryResult.createInstance();
			ll.add(b);
			for (final OperatorIDTuple op : this.getSucceedingOperators()) {
				((Operator) op.getOperator()).deleteAll(ll, op.getId());
			}
		}
	}

	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		final Bindings b = this.process(triple, false);
		if (b != null) {
			// System.out.println(b+" result of "+tp[i]);
			final QueryResult ll = QueryResult.createInstance();
			ll.add(b);
			for (final OperatorIDTuple op : this.getSucceedingOperators()) {
				debugstep.step(this, op.getOperator(), b);
				((Operator) op.getOperator()).processAllDebug(ll, op.getId(),
						debugstep);
			}
		}
	}

	@Override
	public void deleteTripleDebug(final Triple triple, final DebugStep debugstep) {
		final Bindings b = this.process(triple, false);
		if (b != null) {
			// System.out.println(b+" result of "+tp[i]);
			final QueryResult ll = QueryResult.createInstance();
			ll.add(b);
			for (final OperatorIDTuple op : this.getSucceedingOperators()) {
				debugstep.stepDelete(this, op.getOperator(), b);
				((Operator) op.getOperator()).deleteAllDebug(ll, op.getId(),
						debugstep);
			}
		}
	}
}