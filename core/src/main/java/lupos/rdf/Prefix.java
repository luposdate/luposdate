/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.rdf;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import lupos.sparql1_1.ASTPrefixDecl;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SimpleNode;

/**
 * Class for the prefixes. This class holds all prefixes of the operators in the
 * OperatorGraph.
 */
public class Prefix {
	// defines whether the prefix class is active or not...
	protected boolean active;
	protected int prefixCount = 1; // internal count for the prefixes
	// string to define the prefix of the prefix...
	protected final String prefixPrefix = "p";
	// HashMap for the prefixes <namespace, prefix>...
	protected final HashMap<String, String> prefixList = new HashMap<String, String>();
	// HashMap with predefined prefixes <namespace, prefix>...
	protected HashMap<String, String> predefined = new HashMap<String, String>();

	/**
	 * This is the constructor of the prefix class. The constructor initiates
	 * the internal prefixList and adds some basic name-spaces.
	 */
	public Prefix(final boolean active) {
		this.active = active;

		// initiate predefined HashMap and add default values...
		this.predefined.put("<http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
				"rdf");
		this.predefined.put("<http://www.w3.org/2000/01/rdf-schema#>", "rdfs");
		this.predefined.put("<http://www.w3.org/2001/XMLSchema#>", "xsd");
		this.predefined.put("<http://www.w3.org/2005/xpath-functions#>", "fn");
	}

	public Prefix(final boolean active, final Prefix prefixReference) {
		this(active);

		if (prefixReference != null) {
			// get entries of reference...
			HashMap<String, String> pl = prefixReference.getPredefinedList();

			// walk through name-spaces of reference...
			for (final String namespace : pl.keySet()) {
				// add entry to predefined list...
				this.predefined.put(namespace, pl.get(namespace));
			}

			// get entries of reference...
			pl = prefixReference.getPrefixList();

			// walk through name-spaces of reference...
			for (final String namespace : pl.keySet()) {
				// add entry to predefined list...
				this.predefined.put(namespace, pl.get(namespace));
			}
		}
	}

	public HashMap<String, String> getPredefinedList() {
		return this.predefined;
	}

	public HashMap<String, String> getPrefixList() {
		return this.prefixList;
	}

	public HashSet<String> getPrefixNames() {
		final HashSet<String> prefixNames = new HashSet<String>();

		for (final String key : this.prefixList.keySet()) {
			prefixNames.add(this.prefixList.get(key));
		}

		return prefixNames;
	}

	public boolean isActive() {
		return this.active;
	}

	/**
	 * Checks whether the given prefix or the given name-space exist in the
	 * prefixList.
	 * 
	 * @param prefix
	 *            prefix to check
	 * @param namespace
	 *            name-space to check
	 * 
	 * @return true, if prefix or name-space exist in the prefixList, false if
	 *         both are not present
	 */
	protected boolean entryExists(final String namespace, final String prefix) {
		return this.prefixList.containsKey(namespace)
				|| this.prefixList.containsValue(prefix);
	}

	/**
	 * Public method to add a name-space. This method adds the given name-space.
	 * 
	 * @param namespace
	 *            name-space to add
	 * 
	 * @return returns the prefix of the name-space
	 */
	public String add(final String item) {
		if (item == null)
			return null;

		// if prefix class is active and a name space was found in the item...
		if (this.active && Pattern.matches("<.*://.*(:|#|/).*>", item)) {
			// calculate position of name-space end...
			final int namespaceEnd = this.max(item.lastIndexOf("/"), item
					.lastIndexOf("#"), item.lastIndexOf(":")) + 1;

			if (namespaceEnd > -1) { // suffix exists...
				final String suffix = item.substring(namespaceEnd, item
						.length() - 1); // get suffix

				// get name space...
				String namespace = item.substring(0, namespaceEnd) + ">";
				namespace = this.addInternal(namespace); // replace name space

				// rebuild item from name space and suffix...
				return namespace + ":" + suffix;
			}
			// suffix does not exist {complete string item is name space}...
			else {
				return this.addInternal(item); // replace name-space
			}
		} else { // name space replacement is inactive...
			return item; // return inserted string
		}
	}

	public void addEntry(final String prefix, final String namespace,
			final boolean notify) {
		this.prefixList.put(namespace, prefix);
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
	}

	public void changeEntryName(final String oldPrefix, final String newPrefix,
			final boolean notify) {
		final String namespace = this.getNamespace(oldPrefix);

		this.prefixList.remove(namespace);
		this.prefixList.put(namespace, newPrefix);

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

			return prefix; // return prefix
		} else { // if entry does exist...
			// return prefix of the name space...
			return this.prefixList.get(namespace);
		}
	}

	private int max(final int a, final int b, final int c) {
		if (a > b && a > c) {
			return a;
		}

		if (b > c) {
			return b;
		}

		return c;
	}

	/**
	 * This method determines whether the internal prefixList is empty or not.
	 * 
	 * @return true, if internal prefixList is not empty, false if it is
	 */
	public boolean hasElements() {
		return !this.prefixList.isEmpty();
	}

	public String getNamespace(final String prefix) {
		for (final Entry<String, String> entry : this.prefixList.entrySet()) {
			if (entry.getValue().equals(prefix)) {
				return entry.getKey().substring(1, entry.getKey().length() - 1);
			}
		}

		return "";
	}

	public void registerElementsInPrefixInstance(final SimpleNode root) {
		// walk through children of root node...
		for (int i = 0; i < root.jjtGetNumChildren(); ++i) {
			final Node child = root.jjtGetChild(i); // get current child

			// if children is a prefix declaration...
			if (child instanceof ASTPrefixDecl) {
				// get prefix...
				final String prefixString = ((ASTPrefixDecl) child).getPrefix();

				// get child of PrefixDecl to get the name space...
				final Node prefixDeclChild = child.jjtGetChild(0);

				// if child of PrefixDecl is QuotedURIRef...
				if (prefixDeclChild instanceof ASTQuotedURIRef) {
					// get name space...
					final String namespace = ((ASTQuotedURIRef) prefixDeclChild)
							.toQueryString();

					// add name space and prefix to predefined list of prefix
					// instance...
					this.predefined.put(namespace, prefixString);
				}
			}
		}
	}

	public StringBuffer getPrefixString(final String strPre,
			final String strPost) {
		final List<String> namespaces = new LinkedList<String>();
		namespaces.addAll(this.prefixList.keySet());

		Collections.sort(namespaces, new Comparator<String>() {
			public int compare(final String ns1, final String ns2) {
				final String p1 = prefixList.get(ns1);
				final String p2 = prefixList.get(ns2);

				return p1.compareTo(p2);
			}
		});

		final StringBuffer ret = new StringBuffer();

		for (final String namespace : namespaces) {
			ret.append(strPre + this.prefixList.get(namespace) + ":\t"
					+ namespace + strPost + "\n");
		}

		return ret;
	}

	public void setStatus(final boolean status) {
		this.active = status;
	}

	public String toString() {
		return this.getPrefixString("", "").toString();
	}
}