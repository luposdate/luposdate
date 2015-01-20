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
package lupos.gui.operatorgraph.visualeditor.dataeditor.operators;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

public class DataRDFTerm extends RDFTerm {
	public DataRDFTerm(Prefix prefix) {
		super(prefix);
	}

	public DataRDFTerm(Prefix prefix, Item item) {
		super(prefix);

		this.item = item;
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

	public void setPredicate(RDFTerm child, String predicate, int index) throws ModificationException {
		try {
			// new element...
			if(this.predicates.get(child).size() == index) {
				this.predicates.get(child).add(null);
			}

			// parse new value...
			SimpleNode node = SPARQL1_1Parser.parseVerbWithoutVar(predicate, this.prefix.getPrefixNames());

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
			SimpleNode node = SPARQL1_1Parser.parseGraphTerm(value, this.prefix.getPrefixNames());

			this.item = this.getItem(node);
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}
	
	@Override
	public String getXPrefID(){
		return "dataEditor_style_rdfterm";
	}
	
	@Override
	public String getXPrefIDForAnnotation(){		
		return "dataEditor_style_predicate";
	}
}