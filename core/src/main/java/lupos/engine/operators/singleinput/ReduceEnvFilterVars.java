package lupos.engine.operators.singleinput;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.generate.Generate;

/**
 * @author B.Sc.Inf. Dana Linnepe
 */
public class ReduceEnvFilterVars extends SingleInputOperator {
	private List<Variable> substitutionsVariableLeft = new LinkedList<Variable>();
	private List<Variable> substitutionsVariableRight = new LinkedList<Variable>();
	private List<Literal> filterLeft = new LinkedList<Literal>();
	private List<Variable> filterRight = new LinkedList<Variable>();

	// Simulate
	// private Item[] valueOrVariable;

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		intersectionVariables = new HashSet<Variable>();
		unionVariables = new HashSet<Variable>();
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		for (final Variable i : substitutionsVariableLeft) {
			result.getVariables().add(i);
			intersectionVariables.add(i);
			unionVariables.add(i);
		}
		return result;
	}

	public ReduceEnvFilterVars() {
	}

	/*
	 * public Variable getVariable(Item right){ Variable var = null;
	 * if(right.isVariable()){ int index =
	 * substitutionsVariableRight.indexOf(right); if(index!=-1){ var =
	 * substitutionsVariableLeft.get(index); } } else{ int index =
	 * substitutionsLiteralRight.indexOf(right); if(index!=-1){ var =
	 * substitutionsLiteralLeft.get(index); } } return var; }
	 */

	public List<Variable> getFilterRight() {
		return filterRight;
	}

	public void setFilterRight(final List<Variable> filterRight) {
		this.filterRight = filterRight;
	}

	public void setSubstitutionsVariableLeft(
			final List<Variable> substitutionsVariableLeft) {
		this.substitutionsVariableLeft = substitutionsVariableLeft;
	}

	public void setSubstitutionsVariableRight(
			final List<Variable> substitutionsVariableRight) {
		this.substitutionsVariableRight = substitutionsVariableRight;
	}

	public List<Literal> getFilterLeft() {
		return filterLeft;
	}

	public void setFilterLeft(final List<Literal> filterLeft) {
		this.filterLeft = filterLeft;
	}

	public List<Variable> getSubstitutionsVariableLeft() {
		return substitutionsVariableLeft;
	}

	public List<Variable> getSubstitutionsVariableRight() {
		return substitutionsVariableRight;
	}

	public void addSubstitution(final Variable variable, final Variable content) {
		substitutionsVariableLeft.add(variable);
		substitutionsVariableRight.add(content);
	}

	public void addFilter(final Literal content, final Variable variable) {
		filterLeft.add(content);
		filterRight.add(variable);
	}

	@Override
	public QueryResult process(final QueryResult oldBindings,
			final int operandID) {
		final QueryResult qr = QueryResult.createInstance();

		for (final Bindings oldBinding : oldBindings) {
			// Triple triple = getTriple();

			// final Bindings bindings = Bindings.createNewInstance();
			Literal literal = null;

			for (int i = 0; i < filterLeft.size(); i++) {
				// its value has to be equal to the corresponding value of
				// the triple pattern
				if (!filterLeft.get(i).getLiteral(null).valueEquals(
						oldBinding.get(filterRight.get(i)))) {
					return null;
				}
			}

			for (int i = 0; i < substitutionsVariableLeft.size(); i++) {
				// if the item is an unbound variable
				final Variable itemName = substitutionsVariableLeft.get(i);
				if ((literal = oldBinding.get(itemName)) == null) {
					oldBinding.add(itemName, oldBinding
							.get(substitutionsVariableRight.get(i)));
				}
				// if the item is a variable which is already bound
				// and the value differs from the value of the triple
				// which would be used as binding, a conflict was
				// detected
				else if (!literal.valueEquals(oldBinding
						.get(substitutionsVariableRight.get(i)))) {
					return null; // join within triple pattern!
				}
			}

			qr.add(oldBinding);
		}
		// bindings.addTriple(triple);
		return qr;
	}

	@Override
	public String toString() {
		final String text = "ReduceEnvVars to (" + substitutionsVariableLeft
				+ "," + substitutionsVariableRight + ") \n Filter("
				+ filterLeft.toString() + "=" + filterRight + ")";
		// if(substitutionsLiteralLeft.size()>0) text +=
		// "\n "+substitutionsLiteralLeft;
		return text;
	}
}