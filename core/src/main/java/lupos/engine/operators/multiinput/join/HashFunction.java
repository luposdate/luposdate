package lupos.engine.operators.multiinput.join;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;

public class HashFunction {
	private final static int MAXCOMPONENTS = 100;

	private static final long prim = 16785407; // chosen prime number

	private final long[] functionElements;

	public HashFunction(final long[] initValues) {
		this.functionElements = initValues;
	}

	public HashFunction() {
		functionElements = new long[MAXCOMPONENTS];
		for (int i = 0; i < this.functionElements.length; i++) {
			functionElements[i] = (long) (Math.random() * prim);
		}
	}

	public long[] getInitValues() {
		return this.functionElements;
	}

	public long[] getInitValues(final Collection<Variable> intersectionVariables) {
		final long[] la = new long[Math.min(intersectionVariables.size(),
				this.functionElements.length)];
		System.arraycopy(this.functionElements, 0, la, 0, la.length);
		return la;
	}

	public long hash(final String s) {
		long hash = 0;
		for (int i = 0; i < Math.min(s.length(), this.functionElements.length); i++) {
			hash = (hash + ((s.charAt(i) * functionElements[i]) % prim) % prim);
		}
		return hash;
	}

	/**
	 * the key is always greater then zero
	 */
	public long hash(final Collection<Literal> key) {
		long hash = 0;
		int i = 0;
		for (final Literal lit : key) {
			if (i >= this.functionElements.length)
				break;
			hash = (hash + ((lit.hashCode() * functionElements[i]) % prim)
					% prim);
			if (hash < 0)
				hash *= -1;
			i++;
		}
		return hash;
	}

	public long hash(final Literal lit) {
		long hash = (lit.hashCode() * functionElements[0]) % prim;
		if (hash < 0)
			hash *= -1;
		return hash;
	}

	public static Collection<Literal> getKey(final Bindings b,
			final Collection<Variable> intersectionVariables) {
		final Collection<Literal> key = new LinkedList<Literal>();
		for (final Variable v : intersectionVariables) {
			if (b.get(v) == null)
				return null;
			else
				key.add(b.get(v));
		}
		return key;
	}

	public long hash(final Bindings b,
			final Collection<Variable> intersectionVariables) {
		return hash(getKey(b, intersectionVariables));
	}

}
