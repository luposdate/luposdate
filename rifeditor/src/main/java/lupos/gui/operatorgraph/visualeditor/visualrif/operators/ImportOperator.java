
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.operatorgraph.visualeditor.visualrif.operators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ImportOperatorPanel;

import org.json.JSONException;
import org.json.JSONObject;
public class ImportOperator  extends AbstractPrefixOperator {
	private boolean startNode;
	protected final HashMap<String, String> importList = new HashMap<String, String>();
	protected int importCount = 0; // internal count for the prefixes
	private int importRowCnt = 1;
	private ImportOperator importOperator;

	// Constructor
	/**
	 * <p>Constructor for ImportOperator.</p>
	 */
	public ImportOperator(){
		super();
	}

	// Constructor
	/**
	 * <p>Constructor for ImportOperator.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param loadObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public ImportOperator(final String name, final JSONObject loadObject) throws JSONException {
		super(name, loadObject);
	}

	/** {@inheritDoc} */
	@Override
	public AbstractGuiComponent<Operator> draw(final GraphWrapper gw,
			final VisualGraph<Operator> parent) {
		this.panel = new ImportOperatorPanel(parent, gw, this,
				this.determineNameForDrawing(), this.startNode,
				this.alsoSubClasses);
		return this.panel;
	}

	/**
	 * <p>addEntry.</p>
	 *
	 * @param prefix a {@link java.lang.String} object.
	 * @param namespace a {@link java.lang.String} object.
	 */
	public void addEntry(final String prefix, final String namespace) {
		this.importCount++;
		this.importList.put(namespace, prefix); // key , value
	}

	/**
	 * <p>removeEntry.</p>
	 *
	 * @param namespace a {@link java.lang.String} object.
	 * @param notify a boolean.
	 */
	public void removeEntry(final String namespace, final boolean notify) {
		String prefix = this.importList.get(namespace);
		if (prefix == null) {
			prefix = "";
		}
		this.importList.remove(namespace);
		this.importCount--;
	}

	/**
	 * <p>changeEntryName.</p>
	 *
	 * @param oldPrefix a {@link java.lang.String} object.
	 * @param newPrefix a {@link java.lang.String} object.
	 */
	public void changeEntryName(final String oldPrefix, final String newPrefix) {
		final String namespace = this.getNamespace(oldPrefix);
		this.importList.remove(namespace);
		this.importList.put(namespace, newPrefix);
	}

	/**
	 * <p>getPrefix.</p>
	 *
	 * @param namespace a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getPrefix(final String namespace){
		for (final Entry<String, String> entry : this.importList.entrySet()) {
			if (entry.getKey().equals(namespace)) {
				return entry.getValue();
			}
		}
		return "";
	}

	/**
	 * <p>getNamespace.</p>
	 *
	 * @param prefix a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getNamespace(final String prefix) {
		for (final Entry<String, String> entry : this.importList.entrySet()) {
			if (entry.getValue().equals(prefix)) {
				return entry.getKey().substring(1, entry.getKey().length() - 1);
			}
		}
		return "";
	}

	/**
	 * <p>prefixIsInUse.</p>
	 *
	 * @param prefix a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean prefixIsInUse(final String prefix){
		for (final Entry<String, String> entry : this.importList.entrySet()) {
			if (entry.getValue().equals(prefix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>namespaceIsInUse.</p>
	 *
	 * @param namespace a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean namespaceIsInUse(final String namespace){
		for (final Entry<String, String> entry : this.importList.entrySet()) {
			if (entry.getValue().equals(namespace)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>baseIsAlreadySet.</p>
	 *
	 * @return a boolean.
	 */
	public boolean baseIsAlreadySet(){
		for (final Entry<String, String> entry : this.importList.entrySet()) {
			if (entry.getKey().equals("BASE")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method determines whether the internal prefixList is empty or not.
	 *
	 * @return a boolean.
	 */
	public boolean hasElements() {
		return !this.importList.isEmpty();
	}

	/**
	 * <p>getPrefixList.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<String, String> getPrefixList() {
		return this.importList;
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer serializeOperator() {
		final StringBuffer sb = new StringBuffer();
		sb.append("");
		return sb;
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer serializeOperatorAndTree(final HashSet<Operator> arg0) {
		final StringBuffer sb = new StringBuffer();
		sb.append("");
		return sb;
	}

	/** {@inheritDoc} */
	@Override
	public boolean validateOperator(final boolean showErrors,
			final HashSet<Operator> visited, final Object data) {
		final boolean ret = super.validateOperator(showErrors, visited, data);
		if (!ret) {
			return ret;
		}
		return true;
	}

	// Getter + Setter
	/**
	 * <p>isStartNode.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isStartNode() {
		return this.startNode;
	}

	/**
	 * <p>Setter for the field <code>startNode</code>.</p>
	 *
	 * @param startNode a boolean.
	 */
	public void setStartNode(final boolean startNode) {
		this.startNode = startNode;
	}

	/**
	 * <p>Getter for the field <code>importCount</code>.</p>
	 *
	 * @return a int.
	 */
	public int getImportCount() {
		return this.importCount;
	}

	/**
	 * <p>Setter for the field <code>importCount</code>.</p>
	 *
	 * @param importCount a int.
	 */
	public void setImportCount(final int importCount) {
		this.importCount = importCount;
	}

	/**
	 * <p>Getter for the field <code>importRowCnt</code>.</p>
	 *
	 * @return a int.
	 */
	public int getImportRowCnt() {
		return this.importRowCnt;
	}

	/**
	 * <p>Setter for the field <code>importRowCnt</code>.</p>
	 *
	 * @param importRowCnt a int.
	 */
	public void setImportRowCnt(final int importRowCnt) {
		this.importRowCnt = importRowCnt;
	}

	/**
	 * <p>Getter for the field <code>importList</code>.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<String, String> getImportList() {
		return this.importList;
	}

	/**
	 * <p>Getter for the field <code>importOperator</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator} object.
	 */
	public ImportOperator getImportOperator() {
		return this.importOperator;
	}

	/**
	 * <p>Setter for the field <code>importOperator</code>.</p>
	 *
	 * @param importOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator} object.
	 */
	public void setImportOperator(final ImportOperator importOperator) {
		this.importOperator = importOperator;
	}

	/**
	 * <p>getImportOperatorPanel.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ImportOperatorPanel} object.
	 */
	public ImportOperatorPanel getImportOperatorPanel() {
		return (ImportOperatorPanel) this.panel;
	}
}
