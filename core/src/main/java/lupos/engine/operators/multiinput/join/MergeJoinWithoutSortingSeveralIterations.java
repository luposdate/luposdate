package lupos.engine.operators.multiinput.join;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.multiinput.MergeIterator;

public class MergeJoinWithoutSortingSeveralIterations extends Join {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5051512203278340771L;

	protected QueryResult left = null;
	protected QueryResult right = null;

	protected Comparator<Bindings> comp = new Comparator<Bindings>() {

		public int compare(final Bindings o1, final Bindings o2) {
			for (final Variable var : intersectionVariables) {
				final Literal l1 = o1.get(var);
				final Literal l2 = o2.get(var);
				if (l1 != null && l2 != null) {
					final int compare = l1
							.compareToNotNecessarilySPARQLSpecificationConform(l2);
					if (compare != 0)
						return compare;
				} else if (l1 != null)
					return -1;
				else if (l2 != null)
					return 1;
			}
			return 0;
		}

	};

	/**
	 * This method pre-processes the StartOfStreamMessage
	 * 
	 * @param msg
	 *            the message to be pre-processed
	 * @return the pre-processed message
	 */
	@Override
	public Message preProcessMessage(final StartOfEvaluationMessage msg) {
		if (left != null)
			left.release();
		if (right != null)
			right.release();
		left = null;
		right = null;
		return super.preProcessMessage(msg);
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		bindings.materialize();
		final QueryResult oldLeft = left;
		final QueryResult oldRight = right;
		if (operandID == 0) {
			if (left == null)
				left = bindings;
			else
				left = QueryResult.createInstance(new MergeIterator<Bindings>(
						comp, left.iterator(), bindings.iterator()));
		} else if (operandID == 1) {
			if (right == null)
				right = bindings;
			else
				right = QueryResult.createInstance(new MergeIterator<Bindings>(
						comp, right.iterator(), bindings.iterator()));
		} else
			System.err.println("MergeJoin is a binary operator, but received the operand number "
							+ operandID);
		if (left != null && right != null) {
			left.materialize();
			right.materialize();
			// System.out.println(">>>>>>>>>>left " + intersectionVariables
			// + " size:" + left.size());
			// Bindings last = null;
			// int i = 0;
			// for (final Bindings b : left) {
			// i++;
			// if (last != null) {
			// if (comp.compare(last, b) > 0) {
			// System.out.println("Not correctly sorted:" + last
			// + "<---------->" + b);
			// }
			// }
			// last = b;
			// }
			// System.out.println(">>>>>>>>>>right " + intersectionVariables
			// + " size:" + right.size());
			// last = null;
			// for (final Bindings b : right) {
			// if (last != null) {
			// if (comp.compare(last, b) > 0) {
			// System.out.println("Not correctly sorted:" + last
			// + "<---------->" + b);
			// }
			// }
			// last = b;
			// }

			final Iterator<Bindings> leftIterator = (operandID == 0 && oldLeft != null) ? new MinusIterator(
					bindings.iterator(), oldLeft.iterator())
					: left.iterator();
			final QueryResult rightLocal = (operandID == 1 && oldRight != null) ? QueryResult
					.createInstance(new MinusIterator(bindings.iterator(),
							oldRight.iterator()))
					: right;

			// System.out.println("--right:" + rightLocal);
			// if (operandID == 0 && oldLeft != null) {
			// final Iterator<Bindings> leftIterator2 = new MinusIterator(
			// bindings.iterator(), oldLeft.iterator());
			// System.out.print("--left:");
			// while (leftIterator2.hasNext()) {
			// System.out.print(leftIterator2.next() + ",");
			// }
			// System.out.println();
			// } else
			// System.out.println("--left:" + left);

			final ParallelIterator<Bindings> currentResult = (intersectionVariables
					.size() == 0) ? MergeJoin.cartesianProductIterator(
					leftIterator, rightLocal) : MergeJoin.mergeJoinIterator(
					leftIterator, rightLocal.iterator(), comp,
					intersectionVariables);
			if (currentResult != null && currentResult.hasNext()) {
				final QueryResult result = QueryResult
						.createInstance(new ParallelIterator<Bindings>() {

							int number = 0;

							public void close() {
								currentResult.close();
							}

							public boolean hasNext() {
								if (!currentResult.hasNext()) {
									realCardinality = number;
									close();
								}
								return currentResult.hasNext();
							}

							public Bindings next() {
								final Bindings b = currentResult.next();
								if (!currentResult.hasNext()) {
									realCardinality = number;
									close();
								}
								if (b != null)
									number++;
								return b;
							}

							public void remove() {
								currentResult.remove();
							}

							public void finalize() {
								close();
							}
						});
				// System.out.println(this.toString());
				// System.out.println("!!!!!!!!!!Preceding operators:"
				// + this.getPrecedingOperators());
				// System.out.println("!!!!!!!!!!Results: Left:"+left.size()+
				// "\n!!!!!!!!!! Right:"
				// +right.size()+"\n!!!!!!!!!! Result:"+((result
				// ==null)?"null":result.size()));*/
				// System.out.println("!!!!!!!!!!Results: Left:" + left
				// + "\n!!!!!!!!!! Right:" + right
				// + "\n!!!!!!!!!! Result:"
				// + ((result == null) ? "null" : result));
				// System.out.println("Result:" + result);
				// System.out.println("Result size:" + result.size());
				return result;
			} else {
				// left.release();
				// right.release();
				return null;
			}
		} else {

			return null;
		}
	}

	public class MinusIterator implements ParallelIterator<Bindings> {

		final Iterator<Bindings> it;
		final Iterator<Bindings> minus;
		final HashSet<Bindings> currentMinusMap = new HashSet<Bindings>();
		Bindings currentMinus = null;
		Bindings nextMinus = null;
		Bindings next = null;

		public MinusIterator(final Iterator<Bindings> it,
				final Iterator<Bindings> minus) {
			this.it = it;
			this.minus = minus;
			nextMap();
		}

		public void close() {
		}

		public boolean hasNext() {
			if (next == null)
				next = computeNext();
			return (next != null);
		}

		public Bindings next() {
			if (next != null) {
				final Bindings znext = next;
				next = null;
				return znext;
			} else
				return computeNext();
		}

		private Bindings computeNext() {
			while (true) {
				if (!it.hasNext())
					return null;
				final Bindings next = it.next();
				if (next == null)
					return null;
				while (true) {
					if (currentMinus == null)
						return next;
					final int compare = comp.compare(next, currentMinus);
					if (compare == 0) {
						if (currentMinusMap.contains(next)) {
							break;
						} else
							return next;
					}
					if (compare < 0)
						return next;
					nextMap();
				}
			}
		}

		private void nextMap() {
			currentMinusMap.clear();
			if (nextMinus == null && minus.hasNext())
				nextMinus = minus.next();
			if (nextMinus != null) {
				currentMinus = nextMinus;
				currentMinusMap.add(nextMinus);
				nextMinus = null;
				while (minus.hasNext()) {
					nextMinus = minus.next();
					if (comp.compare(nextMinus, currentMinus) == 0)
						currentMinusMap.add(nextMinus);
					else
						break;
					nextMinus = null;
				}
			} else
				currentMinus = null;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
