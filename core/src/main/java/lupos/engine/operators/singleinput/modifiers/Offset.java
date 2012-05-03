package lupos.engine.operators.singleinput.modifiers;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class Offset extends SingleInputOperator {

	// the offset to use
	private int offset;
	private int pos = 0;

	/**
	 * constructs an offset-operator with given offset
	 * 
	 * @param offset
	 *            to use
	 */
	public Offset(final int offset) {
		this.offset = offset;
	}

	/**
	 * changes offset to given value
	 * 
	 * @param offset
	 *            the new offset
	 */
	public void setOffset(final int offset) {
		this.offset = offset;
		if (offset < 0)
			System.out
					.println("Error: OFFSET has to be either positive or zero!");
	}

	public int getOffset() {
		return this.offset;
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		offset = ((Offset) op).offset;
	}

	/**
	 * overrides process method from OperatorInterface
	 * 
	 * @return the BindingsList cut to offset:bindings.length;
	 */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (pos >= offset)
			return bindings;
		if (pos + bindings.size() < offset) {
			pos += bindings.size();
			return null;
		}
		final QueryResult ret = QueryResult.createInstance();
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (itb.hasNext()) {
			if (pos >= offset)
				ret.add(itb.next());
			else
				itb.next();
			pos++;
		}
		return ret;
	}

	@Override
	public String toString() {
		return super.toString() + " " + offset;
	}
}
