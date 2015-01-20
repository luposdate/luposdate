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
package lupos.gui.operatorgraph.visualeditor.visualrif.operators;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.PrefixOperatorPanel;

import org.json.JSONException;
import org.json.JSONObject;

public class PrefixOperator extends AbstractPrefixOperator {
	private boolean startNode;
	private boolean base = false;
	protected final HashMap<String, String> prefixList = new HashMap<String, String>();
	protected int prefixCount = 0; // internal count for the prefixes
	private int prefixRowCnt = 1;

	// Constructor
	public PrefixOperator(){
		super();
	}

	// Constructor
	public PrefixOperator(final String name, final JSONObject loadObject) throws JSONException {
		super(name, loadObject);
	}

	public PrefixOperator(final JSONObject opLoadObject) {
		try {
			this.fromJSON(opLoadObject);
		} catch (final JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public AbstractGuiComponent<Operator> draw(final GraphWrapper gw,
			final VisualGraph<Operator> parent) {
		this.panel = new PrefixOperatorPanel(parent, gw, this,
				this.determineNameForDrawing(), this.startNode,
				this.alsoSubClasses);
		return this.panel;
	}

	public void addEntry(final String prefix, final String namespace) {
		this.prefixCount++;
		this.prefixList.put(namespace, prefix); // key , value
	}

	public void removeEntry(final String namespace, final boolean notify) {
		String prefix = this.prefixList.get(namespace);
		if (prefix == null) {
			prefix = "";
		}
		this.prefixList.remove(namespace);
		this.prefixCount--;
	}

	public void changeEntryName(final String oldPrefix, final String newPrefix) {
		final String namespace = this.getNamespace(oldPrefix);
		this.prefixList.remove(namespace);
		this.prefixList.put(namespace, newPrefix);
	}

	public String getPrefix(final String namespace){
		for (final Entry<String, String> entry : this.prefixList.entrySet()) {
			if (entry.getKey().equals(namespace)) {
				return entry.getValue();
			}
		}
		return "";
	}

	public String getNamespace(final String prefix) {
		for (final Entry<String, String> entry : this.prefixList.entrySet()) {
			if (entry.getValue().equals(prefix)) {
				return entry.getKey().substring(1, entry.getKey().length() - 1);
			}
		}
		return "";
	}

	public boolean prefixIsInUse(final String prefix){
		for (final Entry<String, String> entry : this.prefixList.entrySet()) {
			if (entry.getValue().equals(prefix)) {
				return true;
			}
		}
		return false;
	}

	public boolean namespaceIsInUse(final String namespace){
		for (final Entry<String, String> entry : this.prefixList.entrySet()) {
			if (entry.getValue().equals(namespace)) {
				return true;
			}
		}
		return false;
	}

	public boolean baseIsAlreadySet(){
		for (final Entry<String, String> entry : this.prefixList.entrySet()) {
			if (entry.getKey().equals("BASE")) {
				return true;
			}
		}
		return false;
	}

	/** This method determines whether the internal prefixList is empty or not.
	 *
	 * @return true, if internal prefixList is not empty, false if it is
	 */
	public boolean hasElements() {
		return !this.prefixList.isEmpty();
	}

	public HashMap<String, String> getPrefixList() {
		return this.prefixList;
	}

	@Override
	public StringBuffer serializeOperator() {
		final StringBuffer sb = new StringBuffer();
		sb.append("");
		return sb;
	}

	@Override
	public StringBuffer serializeOperatorAndTree(final HashSet<Operator> arg0) {
		final StringBuffer sb = new StringBuffer();
		sb.append("");
		return sb;
	}

	@Override
	public boolean validateOperator(final boolean showErrors,
			final HashSet<Operator> visited, final Object data) {
		final boolean ret = super.validateOperator(showErrors, visited, data);
		if (!ret) {
			return ret;
		}
		return true;
	}

	public JSONObject toJSON(final JSONObject connectionsObject) throws JSONException {
		final JSONObject saveObject = new JSONObject();
		final JSONObject saveObjectPrefixList = new JSONObject();

		final Point position = ((PrefixOperatorPanel) this.panel).getPositionAndDimension().getFirst();

		saveObject.put("OP TYPE", this.getClass().getSimpleName());

		saveObject.put("POSITION", new double[]{position.getX(), position.getY()});

		for (final Entry<String, String> entry : this.prefixList.entrySet()) {
			saveObjectPrefixList.put(entry.getKey(), entry.getValue());
		}

		saveObject.put("PREFIXLIST", saveObjectPrefixList);
		return saveObject;
	}

	private void fromJSON(final JSONObject opLoadObject) throws JSONException {
		final JSONObject prefixList = opLoadObject.getJSONObject("PREFIXLIST");
		@SuppressWarnings("unchecked")
		final
		Iterator<String> keyIt = prefixList.keys();
		while(keyIt.hasNext()) {
			final String key = keyIt.next();
			this.prefixList.put(key, prefixList.getString(key));
		}
	}

	// Getter + Setter
	public boolean isStartNode() {
		return this.startNode;
	}

	public void setStartNode(final boolean startNode) {
		this.startNode = startNode;
	}

	public boolean isBase() {
		return this.base;
	}

	public void setBase(final boolean base) {
		this.base = base;
	}

	public int getPrefixCount() {
		return this.prefixCount;
	}

	public void setPrefixCount(final int prefixCount) {
		this.prefixCount = prefixCount;
	}

	public int getPrefixRowCnt() {
		return this.prefixRowCnt;
	}

	public void setPrefixRowCnt(final int prefixRowCnt) {
		this.prefixRowCnt = prefixRowCnt;
	}

	public PrefixOperatorPanel getPrefixOperatorPanel(){
		return (PrefixOperatorPanel) this.panel;
	}
}
