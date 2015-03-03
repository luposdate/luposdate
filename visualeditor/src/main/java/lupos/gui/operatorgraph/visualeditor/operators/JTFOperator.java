
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
package lupos.gui.operatorgraph.visualeditor.operators;

import java.awt.Font;
import java.net.URISyntaxException;

import lupos.datastructures.items.BlankNode;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LanguageTaggedLiteralOriginalLanguage;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteralOriginalContent;
import lupos.datastructures.items.literal.URILiteral;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.OperatorPanel;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.sparql1_1.ASTBlankNode;
import lupos.sparql1_1.ASTBooleanLiteral;
import lupos.sparql1_1.ASTDoubleCircumflex;
import lupos.sparql1_1.ASTFloatingPoint;
import lupos.sparql1_1.ASTInteger;
import lupos.sparql1_1.ASTLangTag;
import lupos.sparql1_1.ASTNIL;
import lupos.sparql1_1.ASTQName;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTRDFLiteral;
import lupos.sparql1_1.ASTStringLiteral;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.operatorgraph.SPARQLCoreParserVisitorImplementation;
public abstract class JTFOperator extends Operator {
	protected Prefix prefix;

	/**
	 * <p>Constructor for JTFOperator.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 */
	protected JTFOperator(Prefix prefix) {
		this.prefix = prefix;
	}

	/** {@inheritDoc} */
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new OperatorPanel(this, gw, parent, this.prefix.add(this.toString()));

		return this.panel;
	}

	/**
	 * <p>draw.</p>
	 *
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param PADDING a double.
	 * @param font a {@link java.awt.Font} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent} object.
	 */
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, double PADDING, Font font) {
		this.panel = new OperatorPanel(this, gw, PADDING, font, this.prefix.add(this.toString()), "");

		return this.panel;
	}

	/**
	 * <p>prefixAdded.</p>
	 */
	public void prefixAdded() {
		((OperatorPanel) this.panel).setValue(this.prefix.add(((OperatorPanel) this.panel).getValue()));
	}

	/** {@inheritDoc} */
	public void prefixModified(String oldPrefix, String newPrefix) {
		((OperatorPanel) this.panel).setValue(((OperatorPanel) this.panel).getValue().replaceFirst(oldPrefix + ":", newPrefix + ":"));
	}

	/** {@inheritDoc} */
	public void prefixRemoved(String prefix, String namespace) {
		String replacement = ((OperatorPanel) this.panel).getValue().replaceFirst(prefix + ":", namespace);

		if(!replacement.equals(((OperatorPanel) this.panel).getValue())) {
			((OperatorPanel) this.panel).setValue("<" + replacement + ">");
		}
	}

	/**
	 * <p>getItem.</p>
	 *
	 * @param n a {@link lupos.sparql1_1.Node} object.
	 * @return a {@link lupos.datastructures.items.Item} object.
	 */
	protected Item getItem(Node n) {
		return SPARQLCoreParserVisitorImplementation.getItem(n);
	}

	/**
	 * <p>applyChange.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public abstract void applyChange(String value) throws ModificationException;
}
