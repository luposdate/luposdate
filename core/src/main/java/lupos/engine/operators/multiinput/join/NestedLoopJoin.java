package lupos.engine.operators.multiinput.join;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.multiinput.optional.OptionalResult;

public class NestedLoopJoin extends Join {
	private QueryResult[] lba;

	public NestedLoopJoin() {
	}

	public NestedLoopJoin(final int numberOfOperands) {
		super();
		init();
	}

	public void init() {
		lba = new QueryResult[this.getNumberOfOperands()];
		for (int i = 0; i < this.getNumberOfOperands(); i++) {
			lba[i] = QueryResult.createInstance();
		}
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
		init();
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final QueryResult qr = QueryResult.createInstance();
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (itb.hasNext())
			lba[operandID].add(itb.next());

		for (final Bindings binding : bindings) {

			for (int i = 0; i < this.getNumberOfOperands(); i++) {
				if (lba[i].isEmpty()) {
					return null;
				}
			}
			qr.addAll(combineAndProcess(operandID, binding, 0, QueryResult
					.createInstance()));
		}
		if (qr.size() > 0)
			return qr;
		else
			return null;
	}

	@Override
	public OptionalResult processJoin(final QueryResult bindings,
			final int operandID) {
		final OptionalResult or = new OptionalResult();
		final QueryResult qr = QueryResult.createInstance();
		final QueryResult joinPartnerFromLeftOperand = QueryResult
				.createInstance();
		lba[operandID].addAll(bindings);

		for (final Bindings binding : bindings) {

			for (int i = 0; i < this.getNumberOfOperands(); i++) {
				if (lba[i].isEmpty()) {
					return or;
				}
			}
			// like qr.addAll(combineAndProcess(operandID, binding , 0,
			// QueryResult.createInstance())), but for determining the
			// joinPartnerFromLeftOperand
			if (operandID == 0) {
				final QueryResult bl = QueryResult.createInstance();
				bl.add(binding);
				final QueryResult qr2 = combineAndProcess(operandID, binding,
						1, bl);
				qr.addAll(qr2);
				if (qr2 != null && qr2.size() > 0)
					joinPartnerFromLeftOperand.add(binding);
			} else {
				final Iterator<Bindings> it = lba[0].iterator();
				while (it.hasNext()) {
					final Bindings b = it.next();
					final QueryResult bl = QueryResult.createInstance();
					bl.add(b);
					final QueryResult joinResult = combineAndProcess(operandID,
							binding, 1, bl);
					if (joinResult.size() > 0) {
						qr.addAll(joinResult);
						joinPartnerFromLeftOperand.add(b);
					}
				}
			}
		}
		or.setJoinPartnerFromLeftOperand(joinPartnerFromLeftOperand);
		or.setJoinResult(qr);
		return or;
	}

	private QueryResult combineAndProcess(final int pos,
			final Bindings binding, final int currentPos,
			final QueryResult bindings) {
		final QueryResult qr = QueryResult.createInstance();
		if (pos == currentPos) {
			bindings.add(binding);
			qr
					.addAll(combineAndProcess(pos, binding, currentPos + 1,
							bindings));
		} else if (currentPos < this.getNumberOfOperands()) {
			final Iterator<Bindings> it = lba[currentPos].iterator();
			while (it.hasNext()) {
				final Bindings b = it.next();
				final QueryResult bl = bindings.clone();
				bl.add(b);
				qr.addAll(combineAndProcess(pos, binding, currentPos + 1, bl));
			}
		}
		if (currentPos == this.getNumberOfOperands()) {
			qr.addAll(joinBindings(bindings));
		}
		if (qr.size() == 0)
			return null;
		else
			return qr;
	}
}
