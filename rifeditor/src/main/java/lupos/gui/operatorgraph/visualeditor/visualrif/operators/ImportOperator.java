/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
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
	public ImportOperator(){
		super();
		
	}
	
	// Constructor
	public ImportOperator(String name, JSONObject loadObject) throws JSONException {
		super(name, loadObject);
	}

	
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw,
			VisualGraph<Operator> parent) {
	
		this.panel = new ImportOperatorPanel(parent, gw, this,
				this.determineNameForDrawing(), this.startNode,
				this.alsoSubClasses);
		
		return (AbstractGuiComponent<Operator>) this.panel;
	}

	public void addEntry(String prefix, final String namespace) {
		this.importCount++;
		this.importList.put(namespace, prefix); // key , value


	}
	
	public void removeEntry(String namespace, final boolean notify) {
		String prefix = this.importList.get(namespace);

		if (prefix == null) {
			prefix = "";
		}

		this.importList.remove(namespace);
		this.importCount--;
//		if (namespace.matches("<.*>")) {
//			namespace = namespace.substring(1, namespace.length() - 1);
//		}


		
	}

	public void changeEntryName(final String oldPrefix, final String newPrefix) {
		final String namespace = this.getNamespace(oldPrefix);

		this.importList.remove(namespace);
		this.importList.put(namespace, newPrefix);


	}

	public String getPrefix(final String namespace){
		for (final Entry<String, String> entry : this.importList.entrySet()) {
			if (entry.getKey().equals(namespace)) {
				return entry.getValue();
			}
		}

		return "";
	}
	
	public String getNamespace(final String prefix) {
		for (final Entry<String, String> entry : this.importList.entrySet()) {
			if (entry.getValue().equals(prefix)) {
				return entry.getKey().substring(1, entry.getKey().length() - 1);
			}
		}

		return "";
	}
	
	public boolean prefixIsInUse(final String prefix){
		for (final Entry<String, String> entry : this.importList.entrySet()) {
			if (entry.getValue().equals(prefix)) {
				return true;
			}
		}

		return false;
	}
	
	public boolean namespaceIsInUse(final String namespace){
		for (final Entry<String, String> entry : this.importList.entrySet()) {
			if (entry.getValue().equals(namespace)) {
				return true;
			}
		}

		return false;
	}
	
	public boolean baseIsAlreadySet(){
		for (final Entry<String, String> entry : this.importList.entrySet()) {
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
		return !this.importList.isEmpty();
	}
	
	public HashMap<String, String> getPrefixList() {
		return this.importList;
	}
	
	
	public StringBuffer serializeOperator() {

		StringBuffer sb = new StringBuffer();

		sb.append("");
		return sb;
	}


	public StringBuffer serializeOperatorAndTree(HashSet<Operator> arg0) {
		
		StringBuffer sb = new StringBuffer();

		sb.append("");
		return sb;
	}
	
	public boolean validateOperator(boolean showErrors,
			HashSet<Operator> visited, Object data) {
		
		boolean ret = super.validateOperator(showErrors, visited, data);

		
		
		if (!ret) {
			return ret;
		}



		return true;
	}
	

	
	

	
	// Getter + Setter
	

	public boolean isStartNode() {
		return startNode;
	}

	public void setStartNode(boolean startNode) {
		this.startNode = startNode;
	}

	public int getImportCount() {
		return importCount;
	}

	public void setImportCount(int importCount) {
		this.importCount = importCount;
	}

	public int getImportRowCnt() {
		return importRowCnt;
	}

	public void setImportRowCnt(int importRowCnt) {
		this.importRowCnt = importRowCnt;
	}

	public HashMap<String, String> getImportList() {
		return importList;
	}

	public ImportOperator getImportOperator() {
		return importOperator;
	}

	public void setImportOperator(ImportOperator importOperator) {
		this.importOperator = importOperator;
	}

	
	public ImportOperatorPanel getImportOperatorPanel() {
		// TODO Auto-generated method stub
		return (ImportOperatorPanel) this.panel;
	}
	
}
