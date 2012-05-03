package lupos.engine.operators.singleinput.generate;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.debug.DebugStep;

public class Generate extends SingleInputOperator {

	private Item[] valueOrVariable;

	/**
	 * @param valueOrVariable
	 */
	public Generate(final Item[] valueOrVariable) {
		this.valueOrVariable = valueOrVariable;
	}

	public Generate(TriplePattern tp) {
		this.valueOrVariable = tp.getItems().clone();
	}

	public Generate() {
	}

	public Generate(final TripleOperator pm, final Item... valueOrVariable) {
		this.valueOrVariable = valueOrVariable;
		setSucceedingOperator(new OperatorIDTuple(pm, 0));
	}

	@Override
	public void cloneFrom(final BasicOperator bo) {
		super.cloneFrom(bo);
		if (bo instanceof Generate) {
			final Generate g = (Generate) bo;
			this.valueOrVariable = new Item[g.valueOrVariable.length];
			for (int i = 0; i < valueOrVariable.length; i++) {
				this.valueOrVariable[i] = g.valueOrVariable[i];
			}
		}
	}

	public Item[] getValueOrVariable() {
		return valueOrVariable;
	}

	public void setValueOrVariable(Item[] valueOrVariable) {
		this.valueOrVariable=valueOrVariable;
	}

	public void replaceItems(final Item toBeReplaced, final Item replacement) {
		for (int i = 0; i < valueOrVariable.length; i++) {
			if (valueOrVariable[i].equals(toBeReplaced))
				valueOrVariable[i] = replacement;
		}
	}

	// bindings should contain exactly one element!
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> pib = bindings.oneTimeIterator();
		while (pib.hasNext()) {
			final Bindings bind1 = pib.next();
			// TODO Prevent duplicates
			// bind1.getTriples();

			final Triple triple = new Triple();

			for (int i = 0; i < 3; i++) {
				if (valueOrVariable[i].isVariable())
					triple.setPos(i, bind1.get((Variable) valueOrVariable[i]));
				else
					triple.setPos(i, (Literal) valueOrVariable[i]);
			}

			if (triple.getSubject() == null || triple.getPredicate()==null || triple.getObject()==null || !(triple.getPredicate() instanceof URILiteral && (triple.getSubject() instanceof AnonymousLiteral || triple.getSubject() instanceof URILiteral))) {
				System.err.println("Tried to generate triple "
								+ triple
								+ ", which does not conform to the RDF conventions (B v U) X U X (B v U v L), where B is the set of blank nodes, U the set of URIs and L the set of literals!");
			} else
				for (final OperatorIDTuple oit : succeedingOperators) {
					((TripleConsumer) oit.getOperator()).consume(triple);
				}
		}
		if (pib instanceof ParallelIterator)
			((ParallelIterator) pib).close();
		return null;
	}


	@Override
	public String toString() {
		final StringBuffer result = new StringBuffer(super.toString()+" (");

		for (int i = 0; i <= 2; ++i) {
			result.append(this.valueOrVariable[i]);

			if (i < 2) {
				result.append(", ");
			}
		}

		return result.toString() + ")";
	}
	
	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		// specified...
		final StringBuffer result = new StringBuffer("Generate ("); // start
																	// result
																	// string

		for (int i = 0; i < 3; ++i) { // walk through items...
			// namespace shortening is active and item has a link...
			result.append(prefixInstance
					.add(this.valueOrVariable[i].toString()));

			if (i < 2) { // add ", " between the items...
				result.append(", ");
			}
		}

		return result.toString() + ")";
	}


	public boolean matched(final int pos, final Literal lit) {
		if (valueOrVariable[pos].isVariable())
			return true;
		else if (((Literal) valueOrVariable[pos]).isBlank())
			return false;
		else {
			if (lit.equals(valueOrVariable[pos]))
				return true;
			else
				return false;
		}
	}

	public boolean strictlyMatched(final int pos, final Literal lit) {
		if (valueOrVariable[pos].isVariable())
			return false;
		else if (((Literal) valueOrVariable[pos]).isBlank())
			return false;
		else {
			if (lit.equals(valueOrVariable[pos]))
				return true;
			else
				return false;
		}
	}

	@Override
	public void processAllDebug(final QueryResult queryResult,
			final int operandID, final DebugStep debugstep) {
		processDebugStep(queryResult, debugstep);
	}

	public QueryResult processDebugStep(final QueryResult bindings,
			final DebugStep debugStep) {
		final Iterator<Bindings> pib = bindings.oneTimeIterator();
		while (pib.hasNext()) {
			final Bindings bind1 = pib.next();
			// TODO Prevent duplicates
			// bind1.getTriples();

			final Triple triple = new Triple();

			for (int i = 0; i < 3; i++) {
				if (valueOrVariable[i].isVariable())
					triple.setPos(i, bind1.get((Variable) valueOrVariable[i]));
				else
					triple.setPos(i, (Literal) valueOrVariable[i]);
			}

			if (triple.getSubject() instanceof TypedLiteral) {
				System.err.println("Tried to generate triple "
						+ triple
						+ ", which does not conform to the RDF conventions: The subject is a literal!");
			} else if (triple.getPredicate() instanceof TypedLiteral) {
				System.err.println("Tried to generate triple "
						+ triple
						+ ", which does not conform to the RDF conventions: The predicate is a literal!");
			} else if (triple.getPredicate() instanceof AnonymousLiteral) {
				System.err.println("Tried to generate triple "
						+ triple
						+ ", which does not conform to the RDF conventions: The predicate is a blank node!");
			} else
				for (final OperatorIDTuple oit : succeedingOperators) {
					debugStep.step(this, oit.getOperator(), triple);
					((TripleOperator) oit.getOperator()).consumeDebug(triple,
							debugStep);
				}
		}
		if (pib instanceof ParallelIterator)
			((ParallelIterator) pib).close();
		return null;
	}
}