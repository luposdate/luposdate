package lupos.engine.operators.singleinput;

import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;

public class ReplaceLit extends SingleInputOperator {
	private LinkedList<Variable> substitutionsLiteralLeft = new LinkedList<Variable>();
	private LinkedList<Literal> substitutionsLiteralRight = new LinkedList<Literal>();

	public void removeSubstitution(final Variable left, final Literal right) {
		final LinkedList<Integer> pos = getPositions(substitutionsLiteralLeft,
				left);
		int index;
		for (int i = 0; i < pos.size(); i++) {
			index = pos.get(i);
			if (substitutionsLiteralRight.get(index).equals(right)) {
				substitutionsLiteralLeft.remove(index);
				substitutionsLiteralRight.remove(index);
				break;
			}
		}
	}

	public void removeSubstitutionVars(final int index) {
		substitutionsLiteralLeft.remove(index);
		substitutionsLiteralRight.remove(index);
	}

	public void setSubstitutionsLiteralLeft(
			final LinkedList<Variable> substitutionsLiteralLeft) {
		this.substitutionsLiteralLeft = substitutionsLiteralLeft;
	}

	public void setSubstitutionsLiteralRight(
			final LinkedList<Literal> substitutionsLiteralRight) {
		this.substitutionsLiteralRight = substitutionsLiteralRight;
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		for (final Variable i : substitutionsLiteralLeft) {
			msg.getVariables().add(i);
		}
		intersectionVariables = new LinkedList<Variable>();
		intersectionVariables.addAll(msg.getVariables());
		unionVariables = intersectionVariables;
		return msg;
	}

	public LinkedList<Variable> getSubstitutionsLiteralLeft() {
		return substitutionsLiteralLeft;
	}

	public LinkedList<Literal> getSubstitutionsLiteralRight() {
		return substitutionsLiteralRight;
	}

	private LinkedList<Integer> getPositions(final LinkedList list,
			final Item item) {
		final LinkedList<Integer> pos = new LinkedList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(item)) {
				pos.add(i);
			}
		}
		return pos;
	}

	public boolean contains(final Item left, final Item right) {
		final LinkedList<Integer> indices = getPositions(
				substitutionsLiteralLeft, left);
		if (indices.size() != 0) {
			for (int i = 0; i < indices.size(); i++) {
				if (substitutionsLiteralRight.get(indices.get(i)).equals(right)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addSubstitution(final Variable variable, final Literal content) {
		if (!contains(variable, content)) {
			substitutionsLiteralLeft.add(variable);
			substitutionsLiteralRight.add(content);
		}
	}

	@Override
	public QueryResult process(final QueryResult oldBindings,
			final int operandID) {
		final QueryResult qr = QueryResult.createInstance();

		for (final Bindings oldBinding : oldBindings) {
			Literal literal = null;

			// process all items
			for (int i = 0; i < substitutionsLiteralLeft.size(); i++) {
				final Variable itemName = substitutionsLiteralLeft.get(i);
				// oldBinding.add(itemName, substitutionsLiteralRight.get(i));

				// if the item is an unbound variable
				if ((literal = oldBinding.get(itemName)) == null) {
					oldBinding.add(itemName, substitutionsLiteralRight.get(i));
				}
				// if the item is a variable which is already bound
				// and the value differs from the value of the triple
				// which would be used as binding, a conflict is
				// detected
				else if (!literal.valueEquals(substitutionsLiteralRight.get(i))) {
					System.out.println("Error " + this.toString() + ": "
							+ itemName + ":" + substitutionsLiteralRight.get(i)
							+ "<->" + literal);
					System.out.println(oldBinding);
					return null; // join within triple pattern!
				}
			}
			qr.add(oldBinding);
		}
		return qr;
	}

	@Override
	public void cloneFrom(final BasicOperator bo) {
		super.cloneFrom(bo);
		if (bo instanceof ReplaceLit) {
			final ReplaceLit rl = (ReplaceLit) bo;
			substitutionsLiteralLeft = (LinkedList<Variable>) rl.substitutionsLiteralLeft
					.clone();
			substitutionsLiteralRight = (LinkedList<Literal>) rl.substitutionsLiteralRight
					.clone();
		}
	}

	public boolean equals(final BasicOperator other) {
		if (other instanceof ReplaceLit)
			return equals((ReplaceLit) other);
		else
			return false;
	}

	public boolean equals(final ReplaceLit other) {
		final Iterator<Variable> iv = other.substitutionsLiteralLeft.iterator();
		for (final Variable v : substitutionsLiteralLeft) {
			if (!(v.equals(iv.next())))
				return false;
		}
		if (iv.hasNext())
			return false;
		final Iterator<Literal> il = other.substitutionsLiteralRight.iterator();
		for (final Literal l : substitutionsLiteralRight) {
			if (!(l.equals(il.next())))
				return false;
		}
		if (il.hasNext())
			return false;
		return true;
	}

	@Override
	public String toString() {
		String result = super.toString() + "(";
		for (int i = 0; i < substitutionsLiteralLeft.size(); i++) {
			if (i > 0)
				result += ", ";
			result += substitutionsLiteralLeft.get(i) + "="
					+ substitutionsLiteralRight.get(i);
		}
		return result + ")";
	}
}
