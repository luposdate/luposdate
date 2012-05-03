package lupos.rif.builtin;

import java.util.Iterator;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;

public class IteratorPredicates {

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-less-than")
	public static Iterator<Literal> numeric_less_than(final Literal start) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		return new Iterator<Literal>() {
			int value = startValue;

			public boolean hasNext() {
				return true;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(--value, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-less-than-or-equal")
	public static Iterator<Literal> numeric_less_than_or_equal(
			final Literal start) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		return new Iterator<Literal>() {
			int value = startValue;

			public boolean hasNext() {
				return true;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(value--, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-greater-than")
	public static Iterator<Literal> numeric_greater_than(final Literal start) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		return new Iterator<Literal>() {
			int value = startValue;

			public boolean hasNext() {
				return true;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(++value, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-greater-than-or-equal")
	public static Iterator<Literal> numeric_greater_than_or_equal(
			final Literal start) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		return new Iterator<Literal>() {
			int value = startValue;

			public boolean hasNext() {
				return true;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(value++, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-between")
	public static Iterator<Literal> numeric_between(final Literal start,
			final Literal stop) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		final int endValue = BuiltinHelper.getInteger((TypedLiteral) stop);
		return new Iterator<Literal>() {
			int value = startValue;
			final boolean direction = endValue > startValue;

			public boolean hasNext() {
				return direction ? value < (endValue - 1)
						: endValue < (value - 1);
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(direction ? ++value
						: --value, "integer");
			}

			public void remove() {
			}
		};
	}

	@Builtin(Name = "http://www.w3.org/2007/rif-builtin-predicate#numeric-between-enclosing")
	public static Iterator<Literal> numeric_between_enclosing(
			final Literal start, final Literal stop) {
		final int startValue = BuiltinHelper.getInteger((TypedLiteral) start);
		final int endValue = BuiltinHelper.getInteger((TypedLiteral) stop);
		return new Iterator<Literal>() {
			int value = startValue;
			final boolean direction = endValue > startValue;

			public boolean hasNext() {
				return direction ? value <= endValue : value >= endValue;
			}

			public Literal next() {
				return BuiltinHelper.createXSLiteral(direction ? value++
						: value--, "integer");
			}

			public void remove() {
			}
		};
	}
}
