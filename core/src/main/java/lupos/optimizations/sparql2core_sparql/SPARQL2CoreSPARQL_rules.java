package lupos.optimizations.sparql2core_sparql;

/**
 * This class is a rule representation used to transform SPARQL expressions to
 * CoreSPARQL expressions
 */
public class SPARQL2CoreSPARQL_rules extends SPARQLParserVisitorImplementationDumper {
	// CONSTANTS
	public final static int PO_LISTS = 0; // replace predicate-object-lists
	public final static int O_LISTS = 1; // replace object-lists
	public final static int BLANK_NODES = 2; // replace [] by blank node labels
	public final static int RDF_COLLECTIONS = 3; // replace collections
	public final static int RDF_TYPE = 4; // replace "a" by rdf:type
	public final static int BN_VARS = 5; // Blank nodes to variables
	public final static int FILTER_AND = 6;
	public final static int FILTER_OR = 7;
	public final static int FILTER_NOT = 8;
	public final static int NESTEDGROUPS = 9; // transforms e.g. {{{A}}{B}} into
												// {A B} REQUIRES rule 10
	public final static int CUT_CONSTS = 10; // Looks for constant subtrees
												// which are pre-evaluated
	public final static int SCOPE = 11;
	public final static int PREFIX = 12; // deletes prefix declarations but
	// inserts them directly into the
	// query
	public final static int BASE = 13; // deletes base declarations but inserts
	// them directly into the query
	public final static int DESCRIBE = 14;
	
	public final static int ADD =15;
	public final static int COPY =16;
	public final static int MOVE =17;

	// RULE ARRAY
	protected boolean[] rules = { true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true }; // apply all rules
	public final int LENGTH = rules.length;

	public SPARQL2CoreSPARQL_rules() {
	}

	public boolean applyRule(final int i) {
		return rules[i];
	}

	public boolean setRules(final boolean[] rules) throws NoSuchFieldException {
		if (this.rules.length == rules.length)
			this.rules = rules;
		else
			return false;
		return true;
	}

	public boolean setRule(final int rule_nmbr, final boolean on_off)
			throws NoSuchFieldException {
		if (rule_nmbr < rules.length && rule_nmbr >= 0)
			rules[rule_nmbr] = on_off;
		else
			return false;
		return true;
	}

	public boolean[] get_rules() {
		return rules;
	}

}
