package lupos.rif.operator;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.datatypes.EqualityResult;
import lupos.rif.model.Constant;
import lupos.rif.model.Equality;
import lupos.rif.model.External;
import lupos.rif.visitor.ReplaceVarsVisitor;

import com.google.common.collect.Multimap;

public class ConstructEquality extends Operator {
	private final Multimap<IExpression, IExpression> equalityMap;
	private final Equality[] equalities;
	private final ReplaceVarsVisitor replace = new ReplaceVarsVisitor();

	public ConstructEquality(Multimap<IExpression, IExpression> eqMap,
			Equality... equality) {
		super();
		equalityMap = eqMap;
		this.equalities = equality;
	}

	@Override
	public QueryResult process(QueryResult queryResult, int operandID) {
		final EqualityResult eqResult = new EqualityResult();
		final Iterator<Bindings> it = queryResult.oneTimeIterator();
		while (it.hasNext()) {
			replace.bindings = it.next();
			for (final Equality nextEq : equalities) {
				final Equality replacedEq = (Equality) nextEq.accept(replace,
						null);
				// Externals in Equality auswerten
				if (replacedEq.leftExpr instanceof External) {
					final Literal evaluated = (Literal) replacedEq.leftExpr
							.evaluate(replace.bindings, null, equalityMap);
					replacedEq.leftExpr = new Constant(evaluated, replacedEq);
				}
				if (replacedEq.rightExpr instanceof External) {
					final Literal evaluated = (Literal) replacedEq.rightExpr
							.evaluate(replace.bindings, null, equalityMap);
					replacedEq.rightExpr = new Constant(evaluated, replacedEq);
				}
				eqResult.getEqualityResult().add(replacedEq);
				equalityMap.put(replacedEq.leftExpr, replacedEq.rightExpr);
				equalityMap.put(replacedEq.rightExpr, replacedEq.leftExpr);
			}
		}
		return eqResult;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("ConstructEquality\n");
		for (final Equality eq : equalities)
			str.append(eq.toString()).append("\n");
		return str.toString();
	}

	@Override
	public String toString(Prefix prefixInstance) {
		final StringBuilder str = new StringBuilder("ConstructEquality\n");
		for (final Equality eq : equalities)
			str.append(eq.toString(prefixInstance)).append("\n");
		return str.toString();
	}
}
