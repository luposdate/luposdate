package lupos.gui.operatorgraph.visualeditor.dataeditor.parsing;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.dataeditor.operators.DataRDFTerm;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.parsing.VisualQueryGenerator;
import lupos.misc.util.OperatorIDTuple;
import lupos.sparql1_1.ASTGroupConstraint;
import lupos.sparql1_1.ASTTripleSet;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SPARQL1_1ParserVisitor;
import lupos.sparql1_1.operatorgraph.SPARQLCoreParserVisitorImplementation;

public class VisualDataGenerator extends VisualQueryGenerator {
	public VisualDataGenerator(Prefix prefix) {
		super(prefix);
	}

	@SuppressWarnings("unchecked")
	public Object visit(ASTTripleSet node, Object data) {
		Item[] item = { null, null, null };

		for(int i = 0; i < 3; i++) {
			Node n = node.jjtGetChild(i);
			item[i] = SPARQLCoreParserVisitorImplementation.getItem(n);
		}

		HashMap<Item, DataRDFTerm> rdfHash = (HashMap<Item, DataRDFTerm>) data;
		DataRDFTerm rdfTermSubject = rdfHash.get(item[0]);

		if(rdfTermSubject == null) {
			rdfTermSubject = new DataRDFTerm(this.prefix, item[0]);
			rdfHash.put(item[0], rdfTermSubject);
		}

		DataRDFTerm rdfTermObject = rdfHash.get(item[2]);

		if(rdfTermObject == null) {
			rdfTermObject = new DataRDFTerm(this.prefix, item[2]);
			rdfHash.put(item[2], rdfTermObject);
		}

		LinkedList<Item> predicates = rdfTermSubject.getPredicates(rdfTermObject);

		if(predicates == null || !predicates.contains(item[1])) {
			rdfTermSubject.addPredicate(rdfTermObject, item[1]);
		}

		OperatorIDTuple<Operator> opIDT = new OperatorIDTuple<Operator>(rdfTermObject, 0);

		if(!rdfTermSubject.getSucceedingOperators().contains(opIDT)) {
			rdfTermSubject.addSucceedingOperator(opIDT);
		}

		return rdfTermSubject;
	}

	@SuppressWarnings("unchecked")
	public Object visit(ASTGroupConstraint node, Object data) {
		try {
			HashMap<Item, DataRDFTerm> rdfHash = (HashMap<Item, DataRDFTerm>) data;
			LinkedHashSet<Operator> rdfTermToJoin = new LinkedHashSet<Operator>();

			for(int i = 0; i < node.jjtGetNumChildren(); i++) {
				Node n = node.jjtGetChild(i);

				if(n instanceof ASTTripleSet) {
					DataRDFTerm rdft = (DataRDFTerm) n.jjtAccept((SPARQL1_1ParserVisitor) this, rdfHash);

					rdfTermToJoin.add(rdft);
				}
			}

			return rdfTermToJoin;
		}
		catch(Exception e) {
			e.printStackTrace();

			return null;
		}
	}
}