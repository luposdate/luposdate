package lupos.engine.operators.singleinput;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;

public class Projection extends SingleInputOperator {
	private final HashSet<Variable> s = new HashSet<Variable>();

	public Projection() {
	}

	public void addProjectionElement(final Variable var) {
		if (!s.contains(var)) {
			s.add(var);
		}
	}

	public HashSet<Variable> getProjectedVariables() {
		return s;
	}

	/**
	 * Handles the BoundVariablesMessage by removing all variables from it that
	 * are not projected to.
	 * 
	 * @param msg
	 *            The BoundVariablesMessage
	 * @return The modified message
	 */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		for (final Variable v : msg.getVariables()) {
			if (s.contains(v)) {
				result.getVariables().add(v);
			}
		}
		unionVariables = result.getVariables();
		intersectionVariables = result.getVariables();
		return result;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> itb = new ParallelIterator<Bindings>() {
			final Iterator<Bindings> itbold = bindings.oneTimeIterator();

			public boolean hasNext() {
				return itbold.hasNext();
			}

			public Bindings next() {
				if (!itbold.hasNext())
					return null;
				final Bindings bind1 = itbold.next();
				if (!itbold.hasNext()) {
					if (itbold instanceof ParallelIterator) {
						((ParallelIterator) itbold).close();
					}
				}
				final Bindings bnew = Bindings.createNewInstance();

				final Iterator<Variable> it = s.iterator();
				while (it.hasNext()) {
					final Variable elem = it.next();
					bnew.add(elem, bind1.get(elem));
				}
				bnew.addAllTriples(bind1);
				bnew.addAllPresortingNumbers(bind1);
				return bnew;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void finalize() {
				close();
			}

			public void close() {
				if (itbold instanceof ParallelIterator) {
					((ParallelIterator) itbold).close();
				}
			}
		};

		return QueryResult.createInstance(itb);
	}

	@Override
	public String toString() {
		return super.toString()+" to " + s;
	}
	
	public boolean remainsSortedData(Collection<Variable> sortCriterium){
		return true;
	}
}