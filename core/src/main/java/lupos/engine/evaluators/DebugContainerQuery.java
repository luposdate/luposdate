package lupos.engine.evaluators;

import java.util.List;

import lupos.optimizations.logical.rules.DebugContainer;
import lupos.sparql1_1.Node;

public class DebugContainerQuery<T, A> {

	private A ast;
	private String coreSPARQLQuery;
	private Node astCoreSPARQLQuery;
	private List<DebugContainer<T>> correctOperatorGraphRules;

	/**
	 * @param ast
	 * @param astCoreSPARQLQuery
	 * @param coreSPARQLQuery
	 */
	public DebugContainerQuery(final A ast, final String coreSPARQLQuery,
			final Node astCoreSPARQLQuery,
			final List<DebugContainer<T>> correctOperatorGraphRules) {
		this.ast = ast;
		this.astCoreSPARQLQuery = astCoreSPARQLQuery;
		this.coreSPARQLQuery = coreSPARQLQuery;
		this.correctOperatorGraphRules = correctOperatorGraphRules;
	}

	public A getAst() {
		return ast;
	}

	public void setAst(final A ast) {
		this.ast = ast;
	}

	public String getCoreSPARQLQuery() {
		return coreSPARQLQuery;
	}

	public void setCoreSPARQLQuery(final String coreSPARQLQuery) {
		this.coreSPARQLQuery = coreSPARQLQuery;
	}

	public Node getAstCoreSPARQLQuery() {
		return astCoreSPARQLQuery;
	}

	public void setAstCoreSPARQLQuery(final Node astCoreSPARQLQuery) {
		this.astCoreSPARQLQuery = astCoreSPARQLQuery;
	}

	public List<DebugContainer<T>> getCorrectOperatorGraphRules() {
		return correctOperatorGraphRules;
	}

	public void setCorrectOperatorGraphRules(
			final List<DebugContainer<T>> correctOperatorGraphRules) {
		this.correctOperatorGraphRules = correctOperatorGraphRules;
	}

}
