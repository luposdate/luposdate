package lupos.engine.operators.singleinput;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;

public class AddBinding extends SingleInputOperator {
	private static final long serialVersionUID = -1619006408122462537L;
	private Variable var;
	private Literal literalName;

	public AddBinding() {}

	public AddBinding(final Variable var, final Literal literal) {
		this.var = var;
		this.literalName = literal;
	}

	// bindings should contain exactly one element!
	@Override
	public QueryResult process(final QueryResult oldBindings,
			final int operandID) {
		return QueryResult.createInstance(new Iterator<Bindings>() {
			Iterator<Bindings> itb = oldBindings.oneTimeIterator();

			public boolean hasNext() {
				return itb.hasNext();
			}

			public Bindings next() {
				final Bindings b = itb.next();
				if (b != null) {
					final Literal literal = b.get(var);
					if (literal == null) {
						b.add(var, literalName);
					}
					// if the item is a variable which is already bound
					// and the value differs from the value of the triple
					// which would be used as binding, a conflict is
					// detected
					else if (!literal.valueEquals(literalName)) {
						System.err.println("AddBinding received a bindings, where the variable is already bound to another value!");
						return null;
					}

				}
				return b;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		});
	}

	public Message preProcessMessage(final BoundVariablesMessage msg) {
		msg.getVariables().add(var);
		intersectionVariables = new LinkedList<Variable>();
		intersectionVariables.addAll(msg.getVariables());
		unionVariables = intersectionVariables;
		return msg;
	}

	public Variable getVar() {
		return var;
	}

	public Literal getLiteral() {
		return this.literalName;
	}

	public void setVar(Variable var) {
		this.var = var;
	}

	public void setLiteral(Literal literalName) {
		this.literalName = literalName;
	}

	@Override
	public String toString() {
		return "Add (" + var.toString() + "=" + literalName + ")";
	}
	
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		return "Add (" + this.var.toString() + "="
		+ prefixInstance.add(this.literalName.toString()) + ")";
	}
	
	public boolean remainsSortedData(Collection<Variable> sortCriterium){
		return !sortCriterium.contains(getVar());
	}
}