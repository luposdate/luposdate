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
package lupos.optimizations.sparql2core_sparql;

/**
 * This class is a rule representation used to transform SPARQL expressions to
 * CoreSPARQL expressions
 *
 * @author groppe
 * @version $Id: $Id
 */
public class SPARQL2CoreSPARQL_rules extends SPARQLParserVisitorImplementationDumper {
	// CONSTANTS
	/** Constant <code>PO_LISTS=0</code> */
	public final static int PO_LISTS = 0; // replace predicate-object-lists
	/** Constant <code>O_LISTS=1</code> */
	public final static int O_LISTS = 1; // replace object-lists
	/** Constant <code>BLANK_NODES=2</code> */
	public final static int BLANK_NODES = 2; // replace [] by blank node labels
	/** Constant <code>RDF_COLLECTIONS=3</code> */
	public final static int RDF_COLLECTIONS = 3; // replace collections
	/** Constant <code>RDF_TYPE=4</code> */
	public final static int RDF_TYPE = 4; // replace "a" by rdf:type
	/** Constant <code>BN_VARS=5</code> */
	public final static int BN_VARS = 5; // Blank nodes to variables
	/** Constant <code>FILTER_AND=6</code> */
	public final static int FILTER_AND = 6;
	/** Constant <code>FILTER_OR=7</code> */
	public final static int FILTER_OR = 7;
	/** Constant <code>FILTER_NOT=8</code> */
	public final static int FILTER_NOT = 8;
	/** Constant <code>NESTEDGROUPS=9</code> */
	public final static int NESTEDGROUPS = 9; // transforms e.g. {{{A}}{B}} into
												// {A B} REQUIRES rule 10
	/** Constant <code>CUT_CONSTS=10</code> */
	public final static int CUT_CONSTS = 10; // Looks for constant subtrees
												// which are pre-evaluated
	/** Constant <code>SCOPE=11</code> */
	public final static int SCOPE = 11;
	/** Constant <code>PREFIX=12</code> */
	public final static int PREFIX = 12; // deletes prefix declarations but
	// inserts them directly into the
	// query
	/** Constant <code>BASE=13</code> */
	public final static int BASE = 13; // deletes base declarations but inserts
	// them directly into the query
	/** Constant <code>DESCRIBE=14</code> */
	public final static int DESCRIBE = 14;
	
	/** Constant <code>ADD=15</code> */
	public final static int ADD =15;
	/** Constant <code>COPY=16</code> */
	public final static int COPY =16;
	/** Constant <code>MOVE=17</code> */
	public final static int MOVE =17;

	// RULE ARRAY
	protected boolean[] rules = { true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true }; // apply all rules
	public final int LENGTH = rules.length;

	/**
	 * <p>Constructor for SPARQL2CoreSPARQL_rules.</p>
	 */
	public SPARQL2CoreSPARQL_rules() {
	}

	/**
	 * <p>applyRule.</p>
	 *
	 * @param i a int.
	 * @return a boolean.
	 */
	public boolean applyRule(final int i) {
		return rules[i];
	}

	/**
	 * <p>Setter for the field <code>rules</code>.</p>
	 *
	 * @param rules an array of boolean.
	 * @return a boolean.
	 * @throws java.lang.NoSuchFieldException if any.
	 */
	public boolean setRules(final boolean[] rules) throws NoSuchFieldException {
		if (this.rules.length == rules.length)
			this.rules = rules;
		else
			return false;
		return true;
	}

	/**
	 * <p>setRule.</p>
	 *
	 * @param rule_nmbr a int.
	 * @param on_off a boolean.
	 * @return a boolean.
	 * @throws java.lang.NoSuchFieldException if any.
	 */
	public boolean setRule(final int rule_nmbr, final boolean on_off)
			throws NoSuchFieldException {
		if (rule_nmbr < rules.length && rule_nmbr >= 0)
			rules[rule_nmbr] = on_off;
		else
			return false;
		return true;
	}

	/**
	 * <p>get_rules.</p>
	 *
	 * @return an array of boolean.
	 */
	public boolean[] get_rules() {
		return rules;
	}

}
