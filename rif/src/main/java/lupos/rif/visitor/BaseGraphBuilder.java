package lupos.rif.visitor;

import lupos.datastructures.items.Item;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.model.Constant;
import lupos.rif.model.Equality;
import lupos.rif.model.External;
import lupos.rif.model.RuleList;
import lupos.rif.model.RuleVariable;
import lupos.rif.model.Uniterm;
import lupos.rif.operator.BooleanIndex;
import lupos.rif.operator.PredicateIndex;
import lupos.rif.operator.RuleFilter;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;

public abstract class BaseGraphBuilder implements IRuleVisitor<Object, Object> {
	protected final IndexScanCreatorInterface indexScanCreator;
	protected PredicateIndex predicateIndex;
	protected BooleanIndex booleanIndex;

	public BaseGraphBuilder(final IndexScanCreatorInterface indexScanCreator) {
		super();
		this.indexScanCreator = indexScanCreator;
	}

	public Object visit(RuleList obj, Object arg) throws RIFException {
		throw new RIFException("Lists should be translated to Triples before processing in Operatorgraph!");
	}

	protected abstract RuleFilter buildRuleFilter(IExpression expr, Object arg);

	public Object visit(Equality obj, Object arg) throws RIFException {
		return buildRuleFilter(obj, arg);
	}

	public Object visit(External obj, Object arg) throws RIFException {
		return buildRuleFilter(obj, arg);
	}

	public Object visit(Constant obj, Object arg) throws RIFException {
		return obj.getLiteral();
	}

	public Object visit(RuleVariable obj, Object arg) throws RIFException {
		return obj.getVariable();
	}

	protected TriplePattern unitermToTriplePattern(Uniterm obj) {
		Item subject = (Item) obj.termParams.get(0).accept(this, null);
		Item predicate = (Item) obj.termName.accept(this, null);
		Item object = (Item) obj.termParams.get(1).accept(this, null);
		TriplePattern pattern = new TriplePattern(subject, predicate, object);
		return pattern;
	}
}