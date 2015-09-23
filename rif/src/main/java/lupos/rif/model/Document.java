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
package lupos.rif.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
public class Document extends AbstractRuleNode {
	private String baseNamespace;
	private final Map<String, String> prefixMap = Maps.newHashMap();
	private final Collection<IExpression> facts = Lists.newArrayList();
	private final Collection<Rule> rules = Lists.newArrayList();
	private IExpression conclusion;

	/** {@inheritDoc} */
	@Override
	public List<IRuleNode> getChildren() {
		return new ArrayList<IRuleNode>(rules);
	}

	/**
	 * <p>Setter for the field <code>baseNamespace</code>.</p>
	 *
	 * @param baseNamespace a {@link java.lang.String} object.
	 */
	public void setBaseNamespace(String baseNamespace) {
		this.baseNamespace = baseNamespace;
	}

	/**
	 * <p>Getter for the field <code>baseNamespace</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBaseNamespace() {
		return baseNamespace;
	}

	/**
	 * <p>Getter for the field <code>prefixMap</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> getPrefixMap() {
		return prefixMap;
	}

	/**
	 * <p>Getter for the field <code>rules</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<Rule> getRules() {
		return rules;
	}

	/**
	 * <p>Setter for the field <code>conclusion</code>.</p>
	 *
	 * @param conclusion a {@link lupos.rif.IExpression} object.
	 */
	public void setConclusion(IExpression conclusion) {
		this.conclusion = conclusion;
	}

	/**
	 * <p>Getter for the field <code>conclusion</code>.</p>
	 *
	 * @return a {@link lupos.rif.IExpression} object.
	 */
	public IExpression getConclusion() {
		return conclusion;
	}

	/**
	 * <p>Getter for the field <code>facts</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<IExpression> getFacts() {
		return facts;
	}

	/**
	 * <p>accept.</p>
	 *
	 * @param visitor a {@link lupos.rif.IRuleVisitor} object.
	 * @param arg a A object.
	 * @param <R> a R object.
	 * @param <A> a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg)
			throws RIFException {
		return visitor.visit(this, arg);
	}

	/**
	 * <p>getLabel.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel() {
		StringBuilder str = new StringBuilder();
		str.append("Document").append("\n");
		if (baseNamespace != null)
			str.append("Base: ").append(baseNamespace).append("\n");
		for (Map.Entry<String, String> entry : prefixMap.entrySet())
			str.append("Prefix: ").append(entry.getKey()).append(" - ")
					.append(entry.getValue()).append("\n");
		return str.toString();
	}
}
