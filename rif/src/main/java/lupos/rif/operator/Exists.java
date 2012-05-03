package lupos.rif.operator;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.multiinput.join.Join;

public class Exists extends Join {
	final int operantIdOfMBR;
	private STATUS status = STATUS.WAIT;
	private final Map<Integer, QueryResult> savedBindings = Maps.newHashMap();

	private enum STATUS {
		WAIT, CLOSED, OPEN
	}

	public Exists() {
		this(1);
	}

	public Exists(final int operant) {
		super();
		operantIdOfMBR = operant;
	}

	@Override
	public QueryResult process(QueryResult queryResult, int operandID) {
		return null;
	}

	@Override
	protected boolean isPipelineBreaker() {
		return true;
	}

}
