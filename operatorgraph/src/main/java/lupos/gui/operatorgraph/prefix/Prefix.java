package lupos.gui.operatorgraph.prefix;

import java.util.HashSet;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

/**
 * Class for the prefixes. This class holds all prefixes of the operators in the
 * OperatorGraph.
 */
public abstract class Prefix extends lupos.rdf.Prefix {

	protected IPrefixPanel panel = null;
	private final HashSet<IPrefix> prefixOperators = new HashSet<IPrefix>();

	/**
	 * This is the constructor of the prefix class. The constructor initiates
	 * the internal prefixList and adds some basic name-spaces.
	 */
	public Prefix(final boolean active) {
		super(active);
	}

	public Prefix(final boolean active, final Prefix prefixReference) {
		super(active, prefixReference);
	}


	public void addEntry(final String prefix, final String namespace,
			final boolean notify) {
		this.prefixList.put(namespace, prefix);

		if (notify) {
			for (final IPrefix op : this.prefixOperators) {
				op.prefixAdded();
			}
		}
	}

	public void removeEntry(String namespace, final boolean notify) {
		String prefix = this.prefixList.get(namespace);

		if (prefix == null) {
			prefix = "";
		}

		this.prefixList.remove(namespace);

		if (namespace.matches("<.*>")) {
			namespace = namespace.substring(1, namespace.length() - 1);
		}

		if (notify && !prefix.equals("")) {
			for (final IPrefix op : this.prefixOperators) {
				op.prefixRemoved(prefix, namespace);
			}
		}
	}

	public void changeEntryName(final String oldPrefix, final String newPrefix,
			final boolean notify) {
		final String namespace = this.getNamespace(oldPrefix);

		this.prefixList.remove(namespace);
		this.prefixList.put(namespace, newPrefix);

		if (notify) {
			for (final IPrefix op : this.prefixOperators) {
				op.prefixModified(oldPrefix, newPrefix);
			}
		}
	}

	private String addInternal(final String namespace) {
		String prefix = "";

		// name space is predefined...
		if (this.predefined.containsKey(namespace)) {
			// fetch predefined name space...
			prefix = this.predefined.get(namespace);
		} else { // if name-space is not predefined...
			prefix = this.prefixPrefix + this.prefixCount; // generate prefix
		}

		if (!this.entryExists(namespace, prefix)) { // if entry doesn't exist...
			this.prefixList.put(namespace, prefix); // add entry

			// if prefix is generated...
			if (prefix.equals(this.prefixPrefix + this.prefixCount)) {
				this.prefixCount++; // increment prefixCount
			}

			if (this.panel != null) { // update panel...
				this.panel.createPrefixRow(prefix, namespace);
				this.panel.updateSize();
			}

			return prefix; // return prefix
		} else { // if entry does exist...
			// return prefix of the name space...
			return this.prefixList.get(namespace);
		}
	}

	public void addOperator(final IPrefix op) {
		this.prefixOperators.add(op);
	}

	public abstract AbstractSuperGuiComponent draw(GraphWrapper gw,
			OperatorGraph parent);
}