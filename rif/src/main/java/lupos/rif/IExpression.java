package lupos.rif;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Multimap;

import lupos.datastructures.bindings.Bindings;
import lupos.rif.model.RuleVariable;
import lupos.rif.model.Uniterm;

public interface IExpression extends IRuleNode {

	boolean containsOnlyVariables();

	Set<RuleVariable> getVariables();

	List<Uniterm> getPredicates();

	Object evaluate(final Bindings binding);

	Object evaluate(final Bindings binding, final Object optionalResult);

	Object evaluate(final Bindings binding, final Object optionalResult,
			final Multimap<IExpression, IExpression> equalities);

	boolean isBound(final RuleVariable var,
			final Collection<RuleVariable> boundVars);

	boolean isPossibleAssignment();

	String toString(final lupos.rdf.Prefix prefixInstance);
}
