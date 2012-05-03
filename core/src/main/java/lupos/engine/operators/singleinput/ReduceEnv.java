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

public class ReduceEnv extends SingleInputOperator {

	private List<Variable> letThrough = new LinkedList<Variable>();
	private final List<Variable> substitutionsVariableLeft = new LinkedList<Variable>();
	private final List<Variable> substitutionsVariableRight = new LinkedList<Variable>();
	private List<Variable> substitutionsLiteralLeft = new LinkedList<Variable>();
	private List<Literal> substitutionsLiteralRight = new LinkedList<Literal>();
	private List<Literal> filterLeft = new LinkedList<Literal>();
	private final List<Variable> filterRight = new LinkedList<Variable>();

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
		for (final Variable i : substitutionsLiteralLeft) {
			result.getVariables().add(i);
			intersectionVariables.add(i);
			unionVariables.add(i);
		}
		return result;
	}

	public ReduceEnv() {
	}

	/*
	 * public Variable getVariable(Item right){ Variable var = null;
	 * if(right.isVariable()){ int index =
	 * substitutionsVariableRight.indexOf(right); if(index!=-1){ var =
	 * substitutionsVariableLeft.get(index); } } else{ int index =
	 * substitutionsLiteralRight.indexOf(right); if(index!=-1){ var =
	 * substitutionsLiteralLeft.get(index); } } return var; }
	 */

	public List<Literal> getFilterLeft() {
		return filterLeft;
	}

	public void setFilterLeft(final List<Literal> filterLeft) {
		this.filterLeft = filterLeft;
	}

	public List<Variable> getSubstitutionsLiteralLeft() {
		return substitutionsLiteralLeft;
	}

	public void setSubstitutionsLiteralLeft(
			final List<Variable> substitutionsLiteralLeft) {
		this.substitutionsLiteralLeft = substitutionsLiteralLeft;
	}

	public List<Literal> getSubstitutionsLiteralRight() {
		return substitutionsLiteralRight;
	}

	public void setSubstitutionsLiteralRight(
			final List<Literal> substitutionsLiteralRight) {
		this.substitutionsLiteralRight = substitutionsLiteralRight;
	}

	public List<Variable> getLetThrough() {
		return letThrough;
	}

	public void setLetThrough(final List<Variable> letThrough) {
		this.letThrough = letThrough;
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

	public void addSubstitution(final Variable variable, final Literal content) {
		substitutionsLiteralLeft.add(variable);
		substitutionsLiteralRight.add(content);
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

			final Bindings bindings = Bindings.createNewInstance();
			Literal literal = null;

			for (int i = 0; i < filterLeft.size(); i++) {
				// its value has to be equal to the corresponding value of
				// the triple pattern
				if (!filterLeft.get(i).getLiteral(null).valueEquals(
						oldBinding.get(filterRight.get(i)))) {
					return null;
				}
			}

			// process all items
			// for(int i = 0; i < 3; i++){
			for (int i = 0; i < substitutionsLiteralLeft.size(); i++) {
				// if the item is an unbound variable
				final Variable item = substitutionsLiteralLeft.get(i);
				if ((literal = bindings.get(item)) == null) {
					bindings.add(item, substitutionsLiteralRight.get(i));
				}
				// if the item is a variable which is already bound
				// and the value differs from the value of the triple
				// which would be used as binding, a conflict was
				// detected
				else if (!literal.valueEquals(substitutionsLiteralRight.get(i))) {
					return null; // join within triple pattern!
				}
			}

			for (int i = 0; i < substitutionsVariableLeft.size(); i++) {
				// if the item is an unbound variable
				final Variable item = substitutionsVariableLeft.get(i);
				if ((literal = bindings.get(item)) == null) {
					bindings.add(item, oldBinding
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

			qr.add(bindings);
		}
		// bindings.addTriple(triple);
		return qr;
	}

	/*
	 * private Bindings addAllLiteralSubstitutions(){ Bindings bnew =
	 * Bindings.createNewInstance(); //Process all literal substitutions for(int
	 * i=0; i<substitutionsLiteralLeft.size();i++){ Item left =
	 * substitutionsLiteralLeft.get(i); Literal right =
	 * substitutionsLiteralRight.get(i); if((!left.isVariable()) &&
	 * (!left.equals(right))){ return null; } //Add Tupel (left variable, right
	 * literal) else if(left.isVariable()){ bnew.add(left.toString(), right); }
	 * } return bnew; }
	 */

	@Override
	public String toString() {
		final String text = "Reduce to (" + substitutionsVariableLeft + ","
				+ substitutionsVariableRight + ")";
		// if(substitutionsLiteralLeft.size()>0) text +=
		// "\n "+substitutionsLiteralLeft;
		return text;
	}
}