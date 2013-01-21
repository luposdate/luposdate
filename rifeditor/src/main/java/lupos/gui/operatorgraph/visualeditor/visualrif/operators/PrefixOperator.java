/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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



import org.json.JSONException;
import org.json.JSONObject;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;

import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.PrefixOperatorPanel;







public class PrefixOperator extends AbstractPrefixOperator {


	private boolean startNode;
	private boolean base = false;






	protected final HashMap<String, String> prefixList = new HashMap<String, String>();
	protected int prefixCount = 0; // internal count for the prefixes
	private int prefixRowCnt = 1;
	

	// Constructor
	public PrefixOperator(){
		super();
//		this.getClass().getSimpleName().
		
	}
	
	// Constructor
	public PrefixOperator(String name, JSONObject loadObject) throws JSONException {
		super(name, loadObject);
	}

	
	public PrefixOperator(JSONObject opLoadObject) {
		try {
			this.fromJSON(opLoadObject);
		} catch (JSONException e) {

			e.printStackTrace();
		}
	}



	@Override
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw,
			VisualGraph<Operator> parent) {
	
		this.panel = new PrefixOperatorPanel(parent, gw, this,
				this.determineNameForDrawing(), this.startNode,
				this.alsoSubClasses);
		
		return (AbstractGuiComponent<Operator>) this.panel;
	}

	public void addEntry(String prefix, final String namespace) {
		this.prefixCount++;
		this.prefixList.put(namespace, prefix); // key , value

	}
	
	public void removeEntry(String namespace, final boolean notify) {
		String prefix = this.prefixList.get(namespace);

		if (prefix == null) {
			prefix = "";
		}

		this.prefixList.remove(namespace);
		this.prefixCount--;
//		if (namespace.matches("<.*>")) {
//			namespace = namespace.substring(1, namespace.length() - 1);
//		}


		
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
	
	
	public StringBuffer serializeOperator() {

		StringBuffer sb = new StringBuffer();
//		
//		
//		sb.append("\n");
//		for(Entry<String,String> e : prefixList.entrySet()){
//				
//			if(e.getValue().equals("BASE")) {
//				sb.append("\tBase (<"+e.getKey()+"#>)");
//			}else
//				sb.append("\tPrefix ("+e.getValue()+"  <"+e.getKey()+">)");
//				
//				sb.append("\n");
//				
//			
//		
//		}
//		sb.append("\n");
		sb.append("");
		return sb;
	}


	public StringBuffer serializeOperatorAndTree(HashSet<Operator> arg0) {
		
		StringBuffer sb = new StringBuffer();
//	
//		sb.append("\n");
//		
//		for(Operator op : this.getPrecedingOperators()) {
//			sb.append("\t"+op.serializeOperator());
//			System.out.println(op.serializeOperator());
//		}
//		
//		
//		for(Entry<String,String> e : prefixList.entrySet()){
//				
//			if(e.getValue().equals("BASE")) {
//				sb.append("\tBase (<"+e.getKey()+">)");
//			}else
//				sb.append("\tPrefix ("+e.getValue()+"  <"+e.getKey()+">)");
//				
//				sb.append("\n");
//				
//			
//		
//		}
//		
//		sb.append("\n");
		sb.append("");
		return sb;
	}
	
	public boolean validateOperator(boolean showErrors,
			HashSet<Operator> visited, Object data) {
		
		boolean ret = super.validateOperator(showErrors, visited, data);

		
		
		if (!ret) {
			return ret;
		}

//		if (this.getPrecedingOperators().size() == 0
//				&& this.getSucceedingOperators().size() == 0) {
//			if (showErrors) {
//				JOptionPane
//						.showOptionDialog(
//								this.panel.getParentQG().visualEditor,
//								"A PrefixOperator must have preceding or succeeding elements!",
//								"Error", JOptionPane.DEFAULT_OPTION,
//								JOptionPane.ERROR_MESSAGE, null, null, null);
//			}
//			System.out
//					.println("PrefixOperator.validateOperator(boolean showErrors, HashSet<Operator> visited, Object data)");// TODO
//			return false;
//		}

		return true;
	}
	

	public JSONObject toJSON(JSONObject connectionsObject) throws JSONException {
		JSONObject saveObject = new JSONObject();
		JSONObject saveObjectPrefixList = new JSONObject();

		Point position = ((PrefixOperatorPanel) this.panel).getPositionAndDimension().getFirst();

		saveObject.put("OP TYPE", this.getClass().getSimpleName());

		saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
		
		for (Entry<String, String> entry : this.prefixList.entrySet()) {
			saveObjectPrefixList.put(entry.getKey(), entry.getValue());
		}
		
		saveObject.put("PREFIXLIST", saveObjectPrefixList);
		

		// --- handle connections - begin ---
//		JSONArray connectionsArray = new JSONArray();

//		for(Operator child : this.annotationLabels.keySet()) {
//			AbstractRuleOperator childOp = (AbstractRuleOperator) child;
//			AnnotationPanel ap = (AnnotationPanel) this.annotationLabels.get(child);
//
//			JSONObject childConnectionObject = new JSONObject();
//			childConnectionObject.put("to", childOp.getName());
//			childConnectionObject.put("active", ap.isActive());
//			childConnectionObject.put("id", ap.getOpID());
//			childConnectionObject.put("id label", ap.getOpLabel());
//			childConnectionObject.put("mode", ap.getMode().name());
//
//			connectionsArray.put(childConnectionObject);
//		}
//
//		if(connectionsArray.length() > 0) {
//			connectionsObject.put(this.getName(), connectionsArray);
//		}
		// --- handle connections - end ---

		return saveObject;
	}
	
	private void fromJSON(JSONObject opLoadObject) throws JSONException {
	
	
		JSONObject prefixList = opLoadObject.getJSONObject("PREFIXLIST");
		
		@SuppressWarnings("unchecked")
		Iterator<String> keyIt = prefixList.keys();

		while(keyIt.hasNext()) {
			String key = keyIt.next();
			this.prefixList.put(key, prefixList.getString(key));

			

		}
	}
	
	
	// Getter + Setter
	
	public boolean isStartNode() {
		return startNode;
	}

	public void setStartNode(boolean startNode) {
		this.startNode = startNode;
	}

	public boolean isBase() {
		return base;
	}

	public void setBase(boolean base) {
		this.base = base;
	}

	public int getPrefixCount() {
		return prefixCount;
	}

	public void setPrefixCount(int prefixCount) {
		this.prefixCount = prefixCount;
	}

	public int getPrefixRowCnt() {
		return prefixRowCnt;
	}

	public void setPrefixRowCnt(int prefixRowCnt) {
		this.prefixRowCnt = prefixRowCnt;
	}
	
	public PrefixOperatorPanel getPrefixOperatorPanel(){
		return (PrefixOperatorPanel) this.panel;
	}
		
}
