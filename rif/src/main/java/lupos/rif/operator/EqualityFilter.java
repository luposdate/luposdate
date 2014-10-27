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

import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.datatypes.EqualityResult;
import lupos.rif.model.Equality;
import lupos.rif.visitor.ReplaceVarsVisitor;

import com.google.common.collect.Multimap;

public class EqualityFilter extends RuleFilter {

	private final Set<Bindings> filteredBindings = new HashSet<Bindings>();
	private boolean saveFilteredBindings = true;
	private final ReplaceVarsVisitor replace = new ReplaceVarsVisitor();

	public EqualityFilter(final IExpression expression,
			final Multimap<IExpression, IExpression> eqMap) {
		super(expression, eqMap);
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		try {
			if (bindings instanceof EqualityResult
					&& !this.filteredBindings.isEmpty()) {
				final QueryResult qr = QueryResult.createInstance();
				for (final Bindings bind : this.filteredBindings) {
					qr.add(bind);
				}
				this.saveFilteredBindings = false;
				return super.process(qr, operandID);
			} else {
				return super.process(bindings, operandID);
			}
		} finally {
			this.saveFilteredBindings = true;
		}
	}

	@Override
	protected boolean filter(final Bindings bind, final Object result){
		final boolean booleanResult = super.filter(bind, result);
		if (booleanResult) {
			return booleanResult;
		} else {
			this.replace.bindings = bind;
			final Equality replacedEq = (Equality) this.expression.accept(this.replace, null);
			return this.equalityMap.get(replacedEq.leftExpr).contains(replacedEq.rightExpr)
					|| this.equalityMap.get(replacedEq.rightExpr).contains(replacedEq.leftExpr);
		}
	}

	@Override
	protected void onAccepted(final Bindings bind) {
		if (!this.saveFilteredBindings) {
			this.filteredBindings.remove(bind);
		}
	}

	@Override
	protected void onFilteredOut(final Bindings bind) {
		if (this.saveFilteredBindings) {
			this.filteredBindings.add(bind);
		}
	}

	@Override
	public String toString() {
		String result = "Equalityfilter\n" + this.expression.toString();
		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}
		return result;
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		String result = "Equalityfilter\n" + this.expression.toString(prefixInstance);
		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}
		return result;
	}
}
