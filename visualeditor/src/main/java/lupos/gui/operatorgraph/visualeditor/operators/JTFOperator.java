/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

public abstract class JTFOperator extends Operator {
	protected Prefix prefix;

	protected JTFOperator(Prefix prefix) {
		this.prefix = prefix;
	}

	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new OperatorPanel(this, gw, parent, this.prefix.add(this.toString()));

		return this.panel;
	}

	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, double PADDING, Font font) {
		this.panel = new OperatorPanel(this, gw, PADDING, font, this.prefix.add(this.toString()), "");

		return this.panel;
	}

	public void prefixAdded() {
		((OperatorPanel) this.panel).setValue(this.prefix.add(((OperatorPanel) this.panel).getValue()));
	}

	public void prefixModified(String oldPrefix, String newPrefix) {
		((OperatorPanel) this.panel).setValue(((OperatorPanel) this.panel).getValue().replaceFirst(oldPrefix + ":", newPrefix + ":"));
	}

	public void prefixRemoved(String prefix, String namespace) {
		String replacement = ((OperatorPanel) this.panel).getValue().replaceFirst(prefix + ":", namespace);

		if(!replacement.equals(((OperatorPanel) this.panel).getValue())) {
			((OperatorPanel) this.panel).setValue("<" + replacement + ">");
		}
	}

	protected Item getItem(Node n) {
		Item item = null;

		if(n instanceof ASTNIL) {
			try {
				item = LiteralFactory.createURILiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>");
			}
			catch(URISyntaxException e1) {
				e1.printStackTrace();
			}
		}

		if(n instanceof ASTBlankNode) {
			ASTBlankNode blankNode = (ASTBlankNode) n;
			String name = blankNode.getIdentifier();
			item = new BlankNode(name.substring(2));
		}
		else if(n instanceof ASTQuotedURIRef) {
			ASTQuotedURIRef uri = (ASTQuotedURIRef) n;
			String name = uri.getQRef();

			if(URILiteral.isURI("<" + name + ">")) {
				try {
					item = LiteralFactory.createURILiteral("<" + name + ">");
				}
				catch(Exception e) {
					item = LiteralFactory.createLiteral("<" + name + ">");
				}
			}
			else {
				item = LiteralFactory.createLiteral("<" + name + ">");
			}
		}
		else if (n instanceof ASTRDFLiteral) {
			n = n.jjtGetChild(0);
		}

		if(item != null) {
			return item;
		}

		if(n instanceof ASTVar) {
			ASTVar var = (ASTVar) n;
			String name = var.getName();
			item = new Variable(name);
		}
		else if(n instanceof ASTStringLiteral) {
			ASTStringLiteral lit = (ASTStringLiteral) n;
			String quotedContent = lit.getStringLiteral();

			try {
				item = TypedLiteralOriginalContent.createTypedLiteral(quotedContent, "<http://www.w3.org/2001/XMLSchema#string>");
			}
			catch(URISyntaxException e) {
				item = LiteralFactory.createLiteral(quotedContent);
			}
		}
		else if(n instanceof ASTInteger) {
			ASTInteger lit = (ASTInteger) n;
			String content = String.valueOf(lit.getValue());

			try {
				item = TypedLiteralOriginalContent.createTypedLiteral("\"" + content + "\"", "<http://www.w3.org/2001/XMLSchema#integer>");
			}
			catch(URISyntaxException e) {
				item = LiteralFactory.createLiteral(content);
			}
		}
		else if(n instanceof ASTFloatingPoint) {
			ASTFloatingPoint lit = (ASTFloatingPoint) n;
			String content = lit.getValue();

			try {
				if(content.contains("e") || content.contains("E")) {
					item = TypedLiteralOriginalContent.createTypedLiteral("\"" + content + "\"", "<http://www.w3.org/2001/XMLSchema#double>");
				}
				else {
					item = TypedLiteralOriginalContent.createTypedLiteral("\"" + content + "\"", "<http://www.w3.org/2001/XMLSchema#decimal>");
				}
			}
			catch(URISyntaxException e) {
				item = LiteralFactory.createLiteral(content);
			}
		}
//		else if(n instanceof ASTNegation) {
//			ASTNegation negation = (ASTNegation) n;
//			ASTNumericInterface numeric = (ASTNumericInterface) negation.getChildren()[0];
//
//			if(numeric instanceof ASTInteger) {
//				ASTInteger lit = (ASTInteger) numeric;
//				String content = String.valueOf(lit.getValue());
//
//				try {
//					item = TypedLiteralOriginalContent.createTypedLiteral("\"-" + content + "\"", "<http://www.w3.org/2001/XMLSchema#integer>");
//				}
//				catch(URISyntaxException e) {
//					item = LiteralFactory.createLiteral(content);
//				}
//			}
//
//			if(numeric instanceof ASTFloatingPoint) {
//				ASTFloatingPoint lit = (ASTFloatingPoint) n;
//				String content = lit.getValue();
//
//				try {
//					if(content.contains("e") || content.contains("E")) {
//						item = TypedLiteralOriginalContent.createTypedLiteral("\"-" + content + "\"", "<http://www.w3.org/2001/XMLSchema#double>");
//					}
//					else {
//						item = TypedLiteralOriginalContent.createTypedLiteral("\"-" + content + "\"", "<http://www.w3.org/2001/XMLSchema#decimal>");
//					}
//				}
//				catch(URISyntaxException e) {
//					item = LiteralFactory.createLiteral(content);
//				}
//			}
//		}
		else if(n instanceof ASTBooleanLiteral) {
			String content = ((ASTBooleanLiteral) n).getState() + "";

			try {
				item = TypedLiteralOriginalContent.createTypedLiteral("\"" + content + "\"", "<http://www.w3.org/2001/XMLSchema#boolean>");
			}
			catch(URISyntaxException e) {
				item = LiteralFactory.createLiteral(content);
			}
		}
		else if(n instanceof ASTDoubleCircumflex) {
			if(n.jjtGetNumChildren() != 2) {
				System.err.println(n + " is expected to have 2 children!");
			}
			else {
				String content = this.getItem(n.jjtGetChild(0)).toString();
				String type = this.getItem(n.jjtGetChild(1)).toString();

				try {
					item = TypedLiteralOriginalContent.createTypedLiteral(content, type);
				}
				catch(Exception e) {
					item = LiteralFactory.createLiteral(content + "^^" + type);
				}
			}
		}
		else if(n instanceof ASTLangTag) {
			String content = this.getItem(n.jjtGetChild(0)).toString();
			String lang = ((ASTLangTag) n).getLangTag();
			item = LanguageTaggedLiteralOriginalLanguage.createLanguageTaggedLiteral(content, lang);
		}
		else if(n instanceof ASTQName) {
			ASTQName uri = (ASTQName) n;
			String namespace = this.prefix.getNamespace(uri.getNameSpace());
			String localName = uri.getLocalName();

			String name = namespace + localName;

			if(URILiteral.isURI("<" + name + ">")) {
				try {
					item = LiteralFactory.createURILiteral("<" + name + ">");
				}
				catch(Exception e) {
					item = LiteralFactory.createLiteral("<" + name + ">");
				}
			}
			else {
				item = LiteralFactory.createLiteral("<" + name + ">");
			}
		}
		else {
			System.err.println("Unexpected type in TripleSet! " + n.getClass().getSimpleName());
		}

		return item;
	}

	public abstract void applyChange(String value) throws ModificationException;
}