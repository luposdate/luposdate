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
package lupos.rif.operator;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.datatypes.EqualityResult;
import lupos.rif.model.Constant;
import lupos.rif.model.Equality;
import lupos.rif.model.External;
import lupos.rif.visitor.ReplaceVarsVisitor;

import com.google.common.collect.Multimap;

public class ConstructEquality extends Operator {
	private final Multimap<IExpression, IExpression> equalityMap;
	private final Equality[] equalities;
	private final ReplaceVarsVisitor replace = new ReplaceVarsVisitor();

	public ConstructEquality(Multimap<IExpression, IExpression> eqMap,
			Equality... equality) {
		super();
		equalityMap = eqMap;
		this.equalities = equality;
	}

	@Override
	public QueryResult process(QueryResult queryResult, int operandID) {
		final EqualityResult eqResult = new EqualityResult();
		final Iterator<Bindings> it = queryResult.oneTimeIterator();
		while (it.hasNext()) {
			replace.bindings = it.next();
			for (final Equality nextEq : equalities) {
				final Equality replacedEq = (Equality) nextEq.accept(replace,
						null);
				// Externals in Equality auswerten
				if (replacedEq.leftExpr instanceof External) {
					final Literal evaluated = (Literal) replacedEq.leftExpr
							.evaluate(replace.bindings, null, equalityMap);
					replacedEq.leftExpr = new Constant(evaluated, replacedEq);
				}
				if (replacedEq.rightExpr instanceof External) {
					final Literal evaluated = (Literal) replacedEq.rightExpr
							.evaluate(replace.bindings, null, equalityMap);
					replacedEq.rightExpr = new Constant(evaluated, replacedEq);
				}
				eqResult.getEqualityResult().add(replacedEq);
				equalityMap.put(replacedEq.leftExpr, replacedEq.rightExpr);
				equalityMap.put(replacedEq.rightExpr, replacedEq.leftExpr);
			}
		}
		return eqResult;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("ConstructEquality\n");
		for (final Equality eq : equalities)
			str.append(eq.toString()).append("\n");
		return str.toString();
	}

	@Override
	public String toString(Prefix prefixInstance) {
		final StringBuilder str = new StringBuilder("ConstructEquality\n");
		for (final Equality eq : equalities)
			str.append(eq.toString(prefixInstance)).append("\n");
		return str.toString();
	}
}
