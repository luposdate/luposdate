package lupos.optimizations.sparql2core_sparql;

import lupos.rdf.Prefix;
import lupos.sparql1_1.ASTQuotedURIRef;

public class SPARQLParserVisitorImplementationDumperShort extends SPARQLParserVisitorImplementationDumper {
	Prefix prefixInstance = null;

	public SPARQLParserVisitorImplementationDumperShort(Prefix prefixInstance) {
		super();

		this.prefixInstance = prefixInstance;
	}

	@Override
	public String visit(final ASTQuotedURIRef node) {
		return this.prefixInstance.add("<" + node.getQRef() + ">");
	}
}