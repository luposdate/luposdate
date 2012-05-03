package lupos.datastructures.bindings;

import java.util.HashMap;
import java.util.Map.Entry;

import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.rdf.Prefix;

public class BindingsArrayPresortingNumbers extends BindingsArray {

	protected HashMap<TriplePattern, HashMap<Object, Container>> presortingnumbers = null;

	@Override
	public String toString() {
		return super.toString() + " presorting numbers:" + presortingnumbers + "\n";
	}
	
	@Override
	public String toString(Prefix prefix) {
		return super.toString(prefix) + " presorting numbers:" + presortingnumbers + "\n";
	}


	@Override
	public void init() {
		super.init();
	}

	@Override
	public void addPresortingNumber(final TriplePattern tp,
			final Object orderPattern, final int pos, final int max,
			final int id) {
		if (presortingnumbers == null)
			presortingnumbers = new HashMap<TriplePattern, HashMap<Object, Container>>();
		HashMap<Object, Container> hm = presortingnumbers.get(tp);
		if (hm == null)
			hm = new HashMap<Object, Container>();
		hm.put(orderPattern, new Container(pos, max, id));
		presortingnumbers.put(tp, hm);
	}

	public int getPos(final TriplePattern tp, final Object orderPattern) {
		if (presortingnumbers == null)
			return -1;
		final HashMap<Object, Container> hm = presortingnumbers.get(tp);
		if (hm == null) {
			// System.out
			// .println(
			// "Error: Asked presorting number is not available in Binding:"
			// + tp + "," + orderPattern);
		} else {
			final Container c = hm.get(orderPattern);
			if (c == null) {
				// System.out
				// .println(
				// "Error: Asked presorting number is not available in Binding:"
				// + tp + "," + orderPattern);
			} else
				return c.pos;
		}
		return -1;
	}

	public int getMax(final TriplePattern tp, final Object orderPattern) {
		if (presortingnumbers == null)
			return -1;
		final HashMap<Object, Container> hm = presortingnumbers.get(tp);
		if (hm == null) {
			// System.out
			// .println(
			// "Error: Asked presorting number is not available in Binding:"
			// + tp + "," + orderPattern);
		} else {
			final Container c = hm.get(orderPattern);
			if (c == null) {
				// System.out
				// .println(
				// "Error: Asked presorting number is not available in Binding:"
				// + tp + "," + orderPattern);
			} else
				return c.max;
		}
		return -1;
	}

	public int getId(final TriplePattern tp, final Object orderPattern) {
		if (presortingnumbers == null)
			return -1;
		final HashMap<Object, Container> hm = presortingnumbers.get(tp);
		if (hm == null) {
			// System.out
			// .println(
			// "Error: Asked presorting number is not available in Binding:"
			// + tp + "," + orderPattern);
		} else {
			final Container c = hm.get(orderPattern);
			if (c == null) {
				// System.out
				// .println(
				// "Error: Asked presorting number is not available in Binding:"
				// + tp + "," + orderPattern);
			} else
				return c.id;
		}
		return -1;
	}

	private class Container {
		public int pos;
		public int max;
		public int id;

		public Container(final int pos, final int max, final int id) {
			this.pos = pos;
			this.max = max;
			this.id = id;
		}

		@Override
		public String toString() {
			return "(pos:" + pos + ", max:" + max + ", id:" + id + ")";
		}
	}

	@Override
	public BindingsArrayPresortingNumbers clone() {
		final BindingsArrayPresortingNumbers other = new BindingsArrayPresortingNumbers();
		// System.arraycopy(this.literals, 0, other.literals, 0,
		// this.literals.length);
		other.cloneLiterals(getLiterals());

		if (presortingnumbers != null)
			for (final Entry<TriplePattern, HashMap<Object, Container>> entry : presortingnumbers
					.entrySet()) {
				for (final Entry<Object, Container> innerEntry : entry
						.getValue().entrySet()) {
					other.addPresortingNumber(entry.getKey(), innerEntry
							.getKey(), innerEntry.getValue().pos, innerEntry
							.getValue().max, innerEntry.getValue().id);
				}
			}

		return other;
	}

	@Override
	public void addAllPresortingNumbers(final Bindings bindings) {
		if (!(bindings instanceof BindingsArrayPresortingNumbers))
			return;
		if (((BindingsArrayPresortingNumbers) bindings).presortingnumbers == null)
			return;
		for (final Entry<TriplePattern, HashMap<Object, Container>> entry : ((BindingsArrayPresortingNumbers) bindings).presortingnumbers
				.entrySet()) {
			for (final Entry<Object, Container> innerEntry : entry.getValue()
					.entrySet()) {
				addPresortingNumber(entry.getKey(), innerEntry.getKey(),
						innerEntry.getValue().pos, innerEntry.getValue().max,
						innerEntry.getValue().id);
			}
		}
	}
}