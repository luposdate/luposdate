package lupos.engine.operators.stream;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.TimestampedTriple;
import lupos.datastructures.items.Triple;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.debug.DebugStep;

public abstract class Stream extends TripleOperator implements TripleConsumer {

	protected LinkedList<NotifyStreamResult> notifyStreamResults = new LinkedList<NotifyStreamResult>();
	protected CollectResult collectResult;

	public Stream(final CollectResult cr) {
		collectResult = cr;
	}

	public List<PatternMatcher> getPatternMatchers() {
		final LinkedList<PatternMatcher> llpm = new LinkedList<PatternMatcher>();
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			if (oid.getOperator() instanceof PatternMatcher)
				llpm.add((PatternMatcher) oid.getOperator());
			else if (oid.getOperator() instanceof Window) {
				llpm.add(((Window) oid.getOperator()).getPatternMatcher());
			}
		}
		return llpm;
	}

	public void consume(final Triple triple) {
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			((TripleConsumer) oid.getOperator()).consume(new TimestampedTriple(
					triple, (new Date()).getTime()));
		}
	}

	public void addNotifyStreamResult(
			final NotifyStreamResult notifyStreamResult) {
		notifyStreamResults.add(notifyStreamResult);
	}

	protected void notifyStreamResults() {
		this.sendMessage(new ComputeIntermediateResultMessage());
		for (final NotifyStreamResult nsr : notifyStreamResults)
			nsr.notifyStreamResult(collectResult.getResult());
	}
	
	protected void notifyStreamResultsDebug(DebugStep debugstep) {
		this.sendMessageDebug(new ComputeIntermediateResultMessage(),debugstep);
		for (final NotifyStreamResult nsr : notifyStreamResults)
			nsr.notifyStreamResult(collectResult.getResult());
	}
	
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			final TimestampedTriple timestampedTriple = new TimestampedTriple(
					triple, (new Date()).getTime());
			debugstep.step(this, oid.getOperator(), timestampedTriple);
			((TripleOperator) oid.getOperator()).consumeDebug(
					timestampedTriple, debugstep);
		}
	}
	
	public static Stream createDebugInstance(final Stream stream,
			final DebugStep debugstep) {
		if (stream instanceof StreamTriples) {
			return new StreamTriplesDebug((StreamTriples) stream, debugstep);
		} else if (stream instanceof StreamDuration) {
			return new StreamDurationDebug((StreamDuration) stream, debugstep);
		} else {
			System.err
					.println("StreamTriples or StreamDuration class expected, but got "
							+ stream.getClass());
			return null;
		}
	}
}