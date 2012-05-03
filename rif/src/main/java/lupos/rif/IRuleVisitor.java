package lupos.rif;

import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RuleList;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.RuleVariable;

public interface IRuleVisitor<R, A> {
	R visit(Document obj, A arg) throws RIFException;

	R visit(Rule obj, A arg) throws RIFException;

	R visit(ExistExpression obj, A arg) throws RIFException;

	R visit(Conjunction obj, A arg) throws RIFException;

	R visit(Disjunction obj, A arg) throws RIFException;

	R visit(RulePredicate obj, A arg) throws RIFException;

	R visit(Equality obj, A arg) throws RIFException;

	R visit(External obj, A arg) throws RIFException;

	R visit(RuleList obj, A arg) throws RIFException;

	R visit(RuleVariable obj, A arg) throws RIFException;

	R visit(Constant obj, A arg) throws RIFException;

}
