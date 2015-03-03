
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
	/**
	 * <p>Constructor for VisualDataGenerator.</p>
	 *
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 */
	public VisualDataGenerator(Prefix prefix) {
		super(prefix);
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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
