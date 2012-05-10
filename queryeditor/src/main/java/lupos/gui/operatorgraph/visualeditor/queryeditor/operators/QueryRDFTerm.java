package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

public class QueryRDFTerm extends RDFTerm {
	public QueryRDFTerm(Prefix prefix) {
		super(prefix);
	}

	public QueryRDFTerm(Prefix prefix, Item item) {
		super(prefix);

		this.item = item;
	}

	public void addPredicate(RDFTerm child, String predicate) throws ModificationException {
		try {
			SimpleNode node = SPARQL1_1Parser.parseVerb(predicate, this.prefix.getPrefixNames());

			this.addPredicate(child, this.getItem(node));
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	public void setPredicate(RDFTerm child, String predicate, int index) throws ModificationException {
		try {
			// new element...
			if(this.predicates.get(child).size() == index) {
				this.predicates.get(child).add(null);
			}

			// parse new value...
			SimpleNode node = SPARQL1_1Parser.parseVerb(predicate, this.prefix.getPrefixNames());

			// remove old value...
			if(this.predicates.get(child).get(index) != null) {
				this.predicates.get(child).remove(index);
			}

			// add new value...
			this.predicates.get(child).add(index, this.getItem(node));
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	public void applyChange(String value) throws ModificationException {
		try {
			SimpleNode node = SPARQL1_1Parser.parseRDFTerm(value, this.prefix.getPrefixNames());

			this.item = this.getItem(node);
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}
	
	@Override
	public String getXPrefID(){
		return "queryEditor_style_rdfterm";
	}
	
	@Override
	public String getXPrefIDForAnnotation(){		
		return "queryEditor_style_predicate";
	}
}