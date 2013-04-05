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
package lupos.rif.operator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.rdf.Prefix;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;

public class PredicatePattern extends Operator implements Iterable<Item> {
	private URILiteral patternName;
	private List<Item> patternArgs;

	public PredicatePattern() {
		this(null, (Item[]) null);
	}	
	
	@SuppressWarnings("unchecked")
	public PredicatePattern(final URILiteral name, final Item... params) {
		this.patternName = name;
		this.patternArgs = (List<Item>) (params != null ? Arrays.asList(params) : Arrays.asList());
	}

	public List<Item> getPatternItems() {
		return this.patternArgs;
	}

	public void setPatternItems(List<Item> items) {
		this.patternArgs = items;
	}
	
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		this.unionVariables = new HashSet<Variable>(msg.getVariables());
		for (final Item item : this.patternArgs)
			if (item.isVariable())
				this.unionVariables.add((Variable) item);
		this.intersectionVariables = new HashSet<Variable>(this.unionVariables);
		result.getVariables().addAll(this.intersectionVariables);
		return result;
	}

	@Override
	public QueryResult process(final QueryResult queryResult,
			final int operandID) {
		final QueryResult result = QueryResult.createInstance();
		final RuleResult input = (RuleResult) (queryResult instanceof QueryResultDebug ? ((QueryResultDebug) queryResult)
				.getOriginalQueryResult() : queryResult);
		// Pattern auf alle Praedikate anwenden
		final Iterator<Predicate> predicateIterator = input
				.getPredicateIterator();
		while (predicateIterator.hasNext()) {
			final Predicate pred = predicateIterator.next();
			// Nur Praedikate, in dem die Anzahl der Parameter uebereinstimmt
			// ueberhaut betrachten
			if (pred.getParameters().size() == this.patternArgs.size()
					&& pred.getName().equals(this.patternName)) {
				final Bindings bind = Bindings.createNewInstance();
				boolean matched = true;
				for (int idx = 0; idx < pred.getParameters().size(); idx++)
					if (this.patternArgs.get(idx).isVariable())
						bind.add((Variable) this.patternArgs.get(idx), pred
								.getParameters().get(idx));
					else if (!this.patternArgs.get(idx).equals(
							pred.getParameters().get(idx))) {
						matched = false;
						break;
					}
				if (matched)
					result.add(bind);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer();
		str.append("PredicatePattern On ").append("\n")
				.append(this.patternName.toString()).append("(");
		for (int idx = 0; idx < this.patternArgs.size(); idx++) {
			str.append(this.patternArgs.get(idx).toString());
			if (idx < this.patternArgs.size() - 1)
				str.append(", ");
			else
				str.append(")");
		}
		return str.toString();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer();
		str.append("PredicatePattern On ").append("\n")
				.append(this.patternName.toString(prefixInstance)).append("(");
		for (int idx = 0; idx < this.patternArgs.size(); idx++) {
			str.append(this.patternArgs.get(idx).toString());
			if (idx < this.patternArgs.size() - 1){
				str.append(", ");
			}
		}
		str.append(")");
		return str.toString();
	}

	public URILiteral getPredicateName() {
		return this.patternName;
	}
	
	public void setPredicateName(URILiteral name) {
		this.patternName = name;
	}

	@Override
	public Iterator<Item> iterator() {
		return new Iterator<Item>(){
			private Item next = PredicatePattern.this.patternName;
			private Iterator<Item> iterator = PredicatePattern.this.patternArgs.iterator();

			@Override
			public boolean hasNext() {
				if(this.next!=null){
					return true;
				}				
				return this.iterator.hasNext();
			}

			@Override
			public Item next() {
				if(this.next!=null){
					Item zNext = this.next;
					this.next = null;
					return zNext;
				}				
				return this.iterator.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}			
		};
	}
}