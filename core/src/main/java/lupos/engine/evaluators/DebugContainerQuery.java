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
package lupos.engine.evaluators;

import java.util.List;

import lupos.optimizations.logical.rules.DebugContainer;
import lupos.sparql1_1.Node;
public class DebugContainerQuery<T, A> {

	private A ast;
	private String coreSPARQLQuery;
	private Node astCoreSPARQLQuery;
	private List<DebugContainer<T>> correctOperatorGraphRules;

	/**
	 * <p>Constructor for DebugContainerQuery.</p>
	 *
	 * @param ast
	 * @param astCoreSPARQLQuery
	 * @param astCoreSPARQLQuery a lupos$sparql1_1$Node object.
	 * @param coreSPARQLQuery a {@link java.lang.String} object.
	 * @param correctOperatorGraphRules a {@link java.util.List} object.
	 */
	public DebugContainerQuery(final A ast, final String coreSPARQLQuery,
			final Node astCoreSPARQLQuery,
			final List<DebugContainer<T>> correctOperatorGraphRules) {
		this.ast = ast;
		this.astCoreSPARQLQuery = astCoreSPARQLQuery;
		this.coreSPARQLQuery = coreSPARQLQuery;
		this.correctOperatorGraphRules = correctOperatorGraphRules;
	}

	/**
	 * <p>Getter for the field <code>ast</code>.</p>
	 *
	 * @return a A object.
	 */
	public A getAst() {
		return ast;
	}

	/**
	 * <p>Setter for the field <code>ast</code>.</p>
	 *
	 * @param ast a A object.
	 */
	public void setAst(final A ast) {
		this.ast = ast;
	}

	/**
	 * <p>Getter for the field <code>coreSPARQLQuery</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCoreSPARQLQuery() {
		return coreSPARQLQuery;
	}

	/**
	 * <p>Setter for the field <code>coreSPARQLQuery</code>.</p>
	 *
	 * @param coreSPARQLQuery a {@link java.lang.String} object.
	 */
	public void setCoreSPARQLQuery(final String coreSPARQLQuery) {
		this.coreSPARQLQuery = coreSPARQLQuery;
	}

	/**
	 * <p>Getter for the field <code>astCoreSPARQLQuery</code>.</p>
	 *
	 * @return a lupos$sparql1_1$Node object.
	 */
	public Node getAstCoreSPARQLQuery() {
		return astCoreSPARQLQuery;
	}

	/**
	 * <p>Setter for the field <code>astCoreSPARQLQuery</code>.</p>
	 *
	 * @param astCoreSPARQLQuery a lupos$sparql1_1$Node object.
	 */
	public void setAstCoreSPARQLQuery(final Node astCoreSPARQLQuery) {
		this.astCoreSPARQLQuery = astCoreSPARQLQuery;
	}

	/**
	 * <p>Getter for the field <code>correctOperatorGraphRules</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<DebugContainer<T>> getCorrectOperatorGraphRules() {
		return correctOperatorGraphRules;
	}

	/**
	 * <p>Setter for the field <code>correctOperatorGraphRules</code>.</p>
	 *
	 * @param correctOperatorGraphRules a {@link java.util.List} object.
	 */
	public void setCorrectOperatorGraphRules(
			final List<DebugContainer<T>> correctOperatorGraphRules) {
		this.correctOperatorGraphRules = correctOperatorGraphRules;
	}

}
