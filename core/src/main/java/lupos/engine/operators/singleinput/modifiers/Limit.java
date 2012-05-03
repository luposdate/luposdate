package lupos.engine.operators.singleinput.modifiers;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class Limit extends SingleInputOperator {

	private int limit;
	private int pos = 0;

	/**
	 * Constructs a limit-operator
	 * 
	 * @param limit
	 *            limitation of return values
	 */
	public Limit(final int limit) {
		this.limit = limit;
		if (limit < 0) {
			System.out
					.println("Error: The value of limit has to be positive or zero!");
		}
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		limit = ((Limit) op).limit;
	}

	/**
	 * @return the given list of bindings cut to limit entries.
	 */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (pos >= limit || bindings.size() == 0)
			return null; // to do: close evaluation of query!
		if (pos + bindings.size() < limit) {
			pos += bindings.size();
			return bindings;
		}
		final QueryResult ret = QueryResult.createInstance();
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (itb.hasNext()) {
			if (pos < limit)
				ret.add(itb.next());
			else
				break;
			pos++;
		}
		return ret;
	}

	public int getLimit() {
		return limit;
	}

	@Override
	public String toString() {
		return super.toString() + " " + limit;
	}
}
