package lupos.datastructures.dbmergesortedds;

import java.io.Serializable;
import java.util.Comparator;

public class Entry<E> implements Comparable<Entry<E>>, Serializable {
	private static final long serialVersionUID = -5186882148047627193L;

	public final E e;
	public int run;
	public int n;
	transient protected Comparator<? super E> comp;

	public Entry(final E e, final Comparator<? super E> comp, final int n) {
		this.n = n;
		run = 1;
		this.e = e;
		this.comp = comp;
	}

	public Entry(final E e, final int n) {
		this.n = n;
		this.e = e;
		this.comp = null;
	}

	public Entry(final E e) {
		this.e = e;
		this.comp = null;
	}

	@Override
	public boolean equals(final Object other) {
		return comp.compare(e, ((Entry<E>) other).e) == 0;
	}

	public boolean runMatters = true;

	public int compareTo(final Entry<E> other) {
		if (other == null) {
			return -1;
		}
		if (run == other.run) {
			final int compResult = comp.compare(e, other.e);
			if (compResult == 0) {
				if (n > other.n)
					return 1;
				else if (n == other.n)
					return 0;
				else
					return -1;
			} else {
				return compResult;
			}
		} else if (!runMatters) {
			final int compResult = comp.compare(e, other.e);
			if (compResult == 0) {
				if (run > other.run)
					return 1;
				else if (run < other.run)
					return -1;
				if (n > other.n)
					return 1;
				else if (n == other.n)
					return 0;
				else
					return -1;
			} else {
				return compResult;
			}
		} else {
			return run > other.run ? 1 : -1;
		}
	}

	@Override
	public String toString() {
		return run + " - " + e.toString();
	}
}
