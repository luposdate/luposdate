/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.gui.operatorgraph.prefix;

import java.util.HashSet;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

/**
 * Class for the prefixes. This class holds all prefixes of the operators in the
 * OperatorGraph.
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class Prefix extends lupos.rdf.Prefix {

	protected IPrefixPanel panel = null;
	private final HashSet<IPrefix> prefixOperators = new HashSet<IPrefix>();

	/**
	 * This is the constructor of the prefix class. The constructor initiates
	 * the internal prefixList and adds some basic name-spaces.
	 *
	 * @param active a boolean.
	 */
	public Prefix(final boolean active) {
		super(active);
	}

	/**
	 * <p>Constructor for Prefix.</p>
	 *
	 * @param active a boolean.
	 * @param prefixReference a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 */
	public Prefix(final boolean active, final Prefix prefixReference) {
		super(active, prefixReference);
	}


	/** {@inheritDoc} */
	public void addEntry(final String prefix, final String namespace,
			final boolean notify) {
		this.prefixList.put(namespace, prefix);

		if (notify) {
			for (final IPrefix op : this.prefixOperators) {
				op.prefixAdded();
			}
		}
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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

	/**
	 * <p>addOperator.</p>
	 *
	 * @param op a {@link lupos.gui.operatorgraph.prefix.IPrefix} object.
	 */
	public void addOperator(final IPrefix op) {
		this.prefixOperators.add(op);
	}

	/**
	 * <p>draw.</p>
	 *
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param parent a {@link lupos.gui.operatorgraph.OperatorGraph} object.
	 * @return a {@link lupos.gui.operatorgraph.AbstractSuperGuiComponent} object.
	 */
	public abstract AbstractSuperGuiComponent draw(GraphWrapper gw,
			OperatorGraph parent);
}
