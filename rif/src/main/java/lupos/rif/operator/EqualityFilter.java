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

	public EqualityFilter(IExpression expression,
			Multimap<IExpression, IExpression> eqMap) {
		super(expression, eqMap);
	}

	@Override
	public QueryResult process(QueryResult bindings, int operandID) {
		try {
			if (bindings instanceof EqualityResult
					&& !filteredBindings.isEmpty()) {
				final QueryResult qr = QueryResult.createInstance();
				for (final Bindings bind : filteredBindings)
					qr.add(bind);
				saveFilteredBindings = false;
				return super.process(qr, operandID);
			} else
				return super.process(bindings, operandID);
		} finally {
			saveFilteredBindings = true;
		}
	}

	@Override
	protected boolean filter(Bindings bind) {
		boolean result = super.filter(bind);
		if (result)
			return result;
		else {
			replace.bindings = bind;
			final Equality replacedEq = (Equality) expression.accept(replace,
					null);
			return equalityMap.get(replacedEq.leftExpr).contains(
					replacedEq.rightExpr)
					|| equalityMap.get(replacedEq.rightExpr).contains(
							replacedEq.leftExpr);
		}
	}

	@Override
	protected void onAccepted(Bindings bind) {
		if (!saveFilteredBindings)
			filteredBindings.remove(bind);
	}

	@Override
	protected void onFilteredOut(final Bindings bind) {
		if (saveFilteredBindings)
			filteredBindings.add(bind);
	}

	public String toString() {
		String result = "Equalityfilter\n" + expression.toString();

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	public String toString(final Prefix prefixInstance) {
		String result = "Equalityfilter\n"
				+ expression.toString(prefixInstance);

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

}
