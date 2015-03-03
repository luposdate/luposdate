
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.rif.visitor;

import lupos.datastructures.items.Item;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.model.Constant;
import lupos.rif.model.Equality;
import lupos.rif.model.External;
import lupos.rif.model.RuleList;
import lupos.rif.model.RuleVariable;
import lupos.rif.model.Uniterm;
import lupos.rif.operator.BooleanIndexScan;
import lupos.rif.operator.PredicateIndexScan;
import lupos.rif.operator.RuleFilter;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
public abstract class BaseGraphBuilder implements IRuleVisitor<Object, Object> {
	protected final IndexScanCreatorInterface indexScanCreator;
	protected PredicateIndexScan predicateIndex;
	protected BooleanIndexScan booleanIndex;

	/**
	 * <p>Constructor for BaseGraphBuilder.</p>
	 *
	 * @param indexScanCreator a {@link lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface} object.
	 */
	public BaseGraphBuilder(final IndexScanCreatorInterface indexScanCreator) {
		super();
		this.indexScanCreator = indexScanCreator;
	}

	/** {@inheritDoc} */
	public Object visit(RuleList obj, Object arg) throws RIFException {
		throw new RIFException("Lists should be translated to Triples before processing in Operatorgraph!");
	}

	/**
	 * <p>buildRuleFilter.</p>
	 *
	 * @param expr a {@link lupos.rif.IExpression} object.
	 * @param arg a {@link java.lang.Object} object.
	 * @return a {@link lupos.rif.operator.RuleFilter} object.
	 */
	protected abstract RuleFilter buildRuleFilter(IExpression expr, Object arg);

	/** {@inheritDoc} */
	public Object visit(Equality obj, Object arg) throws RIFException {
		return buildRuleFilter(obj, arg);
	}

	/** {@inheritDoc} */
	public Object visit(External obj, Object arg) throws RIFException {
		return buildRuleFilter(obj, arg);
	}

	/** {@inheritDoc} */
	public Object visit(Constant obj, Object arg) throws RIFException {
		return obj.getLiteral();
	}

	/** {@inheritDoc} */
	public Object visit(RuleVariable obj, Object arg) throws RIFException {
		return obj.getVariable();
	}

	/**
	 * <p>unitermToTriplePattern.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Uniterm} object.
	 * @return a {@link lupos.engine.operators.tripleoperator.TriplePattern} object.
	 */
	protected TriplePattern unitermToTriplePattern(Uniterm obj) {
		Item subject = (Item) obj.termParams.get(0).accept(this, null);
		Item predicate = (Item) obj.termName.accept(this, null);
		Item object = (Item) obj.termParams.get(1).accept(this, null);
		TriplePattern pattern = new TriplePattern(subject, predicate, object);
		return pattern;
	}
}
