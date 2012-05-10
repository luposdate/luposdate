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