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
package lupos.gui.operatorgraph.visualeditor.dataeditor.datageneralizer;

import java.util.Hashtable;
import java.util.LinkedHashSet;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

public class CondensedRDFTerm extends RDFTerm {
	private LinkedHashSet<Item> items;

	public CondensedRDFTerm(Prefix prefix, LinkedHashSet<Item> items) {
		super(prefix);

		this.items = items;
	}

	public void addPredicate(RDFTerm child, String predicate) throws ModificationException {
		try {
			SimpleNode node = SPARQL1_1Parser.parseVerbWithoutVar(predicate, this.prefix.getPrefixNames());

			this.addPredicate(child, this.getItem(node));
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new MultiElementsPanel(this, gw, parent);

		return this.panel;
	}

	public LinkedHashSet<Item> getItems() {
		return this.items;
	}

	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(VisualGraph<Operator> parent) {
		Hashtable<GraphWrapper, AbstractSuperGuiComponent> predicates = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();

		// walk through children of this RDFTerm...
		for(OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()) {
			RDFTerm child = (RDFTerm) opIDTuple.getOperator(); // get current children

			// create predicate panel...
			PredicatePanel predicatePanel = new PredicatePanel((VisualGraphOperatorWithPrefix) parent, this, child);

			this.annotationLabels.put(child, predicatePanel);

			// add predicate panel to hash table with its GraphWrapper...
			predicates.put(new GraphWrapperOperator(child), predicatePanel);
		}

		return predicates;
	}

	public StringBuffer serializeOperator() {
		if(this.items.size() == 1) {
			return new StringBuffer(this.items.iterator().next().toString());
		}
		else {
			return new StringBuffer(((MultiElementsPanel) this.panel).getSelectedItem());
		}
	}

	public void setPredicate(RDFTerm child, String predicate, int index) throws ModificationException {}

	public void applyChange(String value) throws ModificationException {}
	
	@Override
	public String getXPrefID(){
		return "condensedViewViewer_style_rdfterm";
	}
	
	@Override
	public String getXPrefIDForAnnotation(){		
		return "condensedViewViewer_style_predicate";
	}
}