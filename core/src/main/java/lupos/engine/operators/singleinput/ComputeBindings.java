package lupos.engine.operators.singleinput;

import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.rdf.Prefix;

public class ComputeBindings extends SingleInputOperator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6315017556187823149L;

	protected QueryResult queryResult;

	public ComputeBindings(QueryResult qr) {
		this.queryResult = qr;
	}

	/**
	 * saving the QueryResult
	 * 
	 * @param QueryResult
	 * @param int
	 * @return QueryResult
	 */
	@Override
	public synchronized QueryResult process(final QueryResult bindings, final int operandID) {		
		return QueryResult.createInstance(new TransformBindings(this.queryResult));
	}

	/**
	 * Sets the intersection variables and the union variables for the given
	 * query result and returns a message with all used variables
	 * 
	 * @param BoundVariablesMessage msg
	 * @return Message
	 */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		BoundVariablesMessage msgResult = new BoundVariablesMessage(msg);
		final HashSet<Variable> variables = new HashSet<Variable>();
		if (!this.queryResult.isEmpty()) {
			variables.addAll(this.queryResult.iterator().next().getVariableSet());
		}
		for (final Bindings b : this.queryResult) {
			variables.retainAll(b.getVariableSet());
		}
		this.intersectionVariables = variables;
		this.unionVariables = this.queryResult.getVariableSet();
		msgResult.setVariables(variables);
		return msgResult;
	}

	@Override
	public String toString() {
		return super.toString() + " " + this.queryResult;
	}

	@Override
	public String toString(Prefix prefixInstance) {
		return super.toString() + " " + this.queryResult.toString(prefixInstance);
	}
	
	/**
	 * This class transforms a QueryResult using the actual configured Bindings instance class if necessary.
	 * This avoids problems with mixed Bindings instances with different implementation classes.
	 * Background: A queryresult is already computed during compilation for the BINDINGS clause in SPARQL 1.1.
	 * However, at that time not all used variables are known, such that the BindingsArray cannot be used
	 * (and instead BindingsMap is used...). However, at runtime, BindingsArray may be used for the other operations,
	 * which may lead to problems when Bindings are stored on disk and afterwards read again.   
	 */
	public static class TransformBindings implements ParallelIterator<Bindings>{
		
		private final Iterator<Bindings> iterator;
		
		public TransformBindings(final QueryResult queryResult){
			this.iterator = queryResult.oneTimeIterator();
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public Bindings next() {
			Bindings bindings = this.iterator.next();
			if(bindings==null){
				return null;
			}
			if(bindings.getClass() == Bindings.instanceClass){
				return bindings;
			} else {
				Bindings bnew = Bindings.createNewInstance();
				bnew.addAll(bindings);
				return bnew;
			}
		}

		@Override
		public void remove() {
			this.iterator.remove();
		}

		@Override
		public void close() {
			if(this.iterator instanceof ParallelIterator){
				((ParallelIterator<Bindings>)this.iterator).close();
			}
		}		
	}
}
