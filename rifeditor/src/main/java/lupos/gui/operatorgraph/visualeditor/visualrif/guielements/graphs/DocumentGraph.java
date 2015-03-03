
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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AnnotationOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.AnnotationConnection;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.VisualGraphOperator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class DocumentGraph  extends VisualGraphOperator{
	
	private static final long serialVersionUID = -2936295936044533187L;
	
	private RulePanel rulePanel;
	

	private  HashMap<String, String> prefixList;
	private  HashMap<String, String> importList;
	
	// Constructor
	/**
	 * <p>Constructor for DocumentGraph.</p>
	 *
	 * @param visualEditor a {@link lupos.gui.operatorgraph.visualeditor.VisualEditor} object.
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public DocumentGraph(VisualEditor<Operator> visualEditor, VisualRifEditor visualRifEditor) {
		super(visualEditor);

		this.setVisualRifEditor(visualRifEditor);
		
		this.SPACING_X = 190;
		this.SPACING_Y = 190;

		this.construct();
	}

	
	/** {@inheritDoc} */
	@Override
	protected Operator createOperator(Class<? extends Operator> clazz, Item content) throws Exception {
		
		Operator newOp = null;

	
		newOp = clazz.newInstance();

		
		
		return newOp;
	}

	
	/** {@inheritDoc} */
	@Override
	public void createNewRule(RuleOperator ro) {
		ro.setVisualRifEditor(visualRifEditor);
		if ( !ro.isInitRule() ) ro.initRule();
		ro.setDocumentName( visualRifEditor.getDocumentContainer().getNameOfActiveElement());
	}


	/** {@inheritDoc} */
	public void createNewPrefix(PrefixOperator po) {}
	
	
	/** {@inheritDoc} */
	@Override
	protected void createNewImport(ImportOperator io) {}
	
	/** {@inheritDoc} */
	@Override
	protected void createNewUniterm(UnitermOperator fo) {}
	
	/**
	 * <p>handleAddOperator.</p>
	 *
	 * @param arg0 a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	protected void handleAddOperator(Operator arg0) {}

	
	/**
	 * <p>serializeGraph.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String serializeGraph() {
		final String graph = super.serializeSuperGraph();
		final StringBuffer ret = new StringBuffer();
		ret.append("Document(\n\n");
		
		// Prefix
		if (prefixList != null  && !prefixList.isEmpty()) {
			
			for (Entry<String, String> e : prefixList.entrySet()) {
				if (e.getValue().equals("BASE")) {
					ret.append("Base (<" + e.getKey() + ">)");
					ret.append(" \n");
					
					
				}
				
				
			}
			
			
			for (Entry<String, String> e : prefixList.entrySet()) {
				if (!e.getValue().equals("BASE")){
					ret.append("Prefix (" + e.getValue() + "  <" + e.getKey()
							+ ">)");

				ret.append(" \n");
				}
			}
		}
		
		// Import
		if (importList != null  && !importList.isEmpty()) {
			
			for (Entry<String, String> e : importList.entrySet()) {

					ret.append("Import (" + e.getValue() + "  <" + e.getKey()
							+ ">)");

				ret.append("\n");
				
			}
		}
		
		
		ret.append("\n");
		ret.append("Group\n");
		ret.append("(\n");
		
		ret.append(graph);
		
		ret.append("\n)");
		ret.append("\n\n)");
		return ret.toString();
	}

	
	/** {@inheritDoc} */
	@Override
	protected boolean validateAddOperator(int arg0, int arg1, String arg2) {
		return true;
	}


	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject saveObject = new JSONObject();

		JSONObject connectionsObject = new JSONObject();

		for(GraphWrapper gw : this.boxes.keySet()) {
			Operator op = (Operator) gw.getElement();
		
//			// Rule 
//			if (op instanceof RuleOperator){
//				RuleOperator ro = ( RuleOperator ) op;
//				saveObject.put(ro.getRuleName(), ro.toJSON(connectionsObject));
//			}
			
			// Prefix
			if (op instanceof PrefixOperator){
				PrefixOperator po = ( PrefixOperator ) op;
				saveObject.put("PREFIXOPERATOR", po.toJSON(connectionsObject));
			}
		}

		if(connectionsObject.length() > 0) {
			saveObject.put("CONNECTIONS", connectionsObject);
		}

		return saveObject;
	}
	
	/**
	 * <p>fromJSON.</p>
	 *
	 * @param loadObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	@SuppressWarnings("unchecked")
	public void fromJSON(JSONObject loadObject) throws JSONException {
		Iterator<String> keyIt = loadObject.keys();

//		HashMap<String, GraphWrapperEditable> tmp = new HashMap<String, GraphWrapperEditable>();

		while(keyIt.hasNext()) {
			String ruleOpName = keyIt.next();

			if(ruleOpName.equals("CONNECTIONS")) {
				continue;
			}

			JSONObject opLoadObject = loadObject.getJSONObject(ruleOpName);

			Operator op = null;


			
			if(opLoadObject.has("OP TYPE") && opLoadObject.getString("OP TYPE").equals("PrefixOperator")) {
				op = new PrefixOperator(opLoadObject);
			}
			


			JSONArray positionArray = opLoadObject.getJSONArray("POSITION");

			this.addOperator(positionArray.getInt(0), positionArray.getInt(1), op);

//			tmp.put(ruleOpName, this.createGraphWrapper(op));
		}


	}
	
//	@SuppressWarnings("unchecked")
	/**
	 * <p>ruleFromJSON.</p>
	 *
	 * @param loadObject a {@link org.json.JSONObject} object.
	 * @param documentName a {@link java.lang.String} object.
	 * @param ruleOpName a {@link java.lang.String} object.
	 * @throws org.json.JSONException if any.
	 */
	public void ruleFromJSON(JSONObject loadObject, String documentName, String ruleOpName) throws JSONException {
		
//		Iterator<String> keyIt = loadObject.keys();

//		HashMap<String, GraphWrapperEditable> tmp = new HashMap<String, GraphWrapperEditable>();

		RuleOperator op = null;

//		JSONObject opLoadObject = loadObject.getJSONObject(ruleOpName);
		
		JSONObject canvasInfo = loadObject.getJSONObject("CANVASINFO");
		
		JSONArray positionArray = canvasInfo.getJSONArray("POSITION");
		
		JSONObject ruleEditorPane = loadObject.getJSONObject("RULEEDITORPANE");



			if( canvasInfo.has("OP TYPE") && canvasInfo.getString("OP TYPE").equals("RuleOperator") ) {
				
				
				
				op = new RuleOperator(ruleOpName, loadObject);
				op.setDocumentName(documentName);
				op.setVisualRifEditor(this.visualRifEditor);
				op.setDocumentName(documentName);
				op.initRule();
				op.getRulePanel().getRuleEditorPane().fromJSON(ruleEditorPane);

			}

			this.addOperator(positionArray.getInt(0), positionArray.getInt(1), op);
			
			JSONObject annotation = null;
			
			if(canvasInfo.has("ANNOTATION")){
				annotation = canvasInfo.getJSONObject("ANNOTATION");
				AnnotationOperator annotationOperator = new AnnotationOperator();
				annotationOperator.fromJSON(annotation);
				JSONArray positionArrayAnnotation = annotation.getJSONArray("POSITION");
				this.addOperator(positionArrayAnnotation.getInt(0), positionArrayAnnotation.getInt(1), annotationOperator);
				AnnotationConnection annotationConnection = new AnnotationConnection(visualEditor, annotationOperator,op);
			}
	}
	
	/**
	 * <p>ruleToJSON.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject ruleToJSON(String ruleName) throws JSONException {
		JSONObject saveObject = new JSONObject();

		JSONObject connectionsObject = new JSONObject();

		for(GraphWrapper gw : this.boxes.keySet()) {
			Operator op = (Operator) gw.getElement();
		
			// Rule 
			if (op instanceof RuleOperator){
				RuleOperator ro = ( RuleOperator ) op;
				
				if( ro.getRuleName().equals(ruleName) ){
					
//					saveObject.put(ro.getRuleName(), ro.toJSON(connectionsObject));
					
					return ro.toJSON(connectionsObject);
				
				}
			}
			
		
		}

		if(connectionsObject.length() > 0) {
			saveObject.put("CONNECTIONS", connectionsObject);
		}

		return saveObject;
	}
	
	
	/* *************** **
	 * Getter + Setter **
	 * *************** */
	
	
	/**
	 * <p>Getter for the field <code>prefixList</code>.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<String, String> getPrefixList() {
		return this.prefixList;
	}

	/**
	 * <p>Setter for the field <code>prefixList</code>.</p>
	 *
	 * @param prefixList a {@link java.util.HashMap} object.
	 */
	public void setPrefixList(HashMap<String, String> prefixList) {
		this.prefixList = prefixList;
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
	 * <p>Setter for the field <code>importList</code>.</p>
	 *
	 * @param importList a {@link java.util.HashMap} object.
	 */
	public void setImportList(HashMap<String, String> importList) {
		this.importList = importList;
	}

	/**
	 * <p>Getter for the field <code>rulePanel</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public RulePanel getRulePanel() {
		return this.rulePanel;
	}

	/**
	 * <p>Setter for the field <code>rulePanel</code>.</p>
	 *
	 * @param rulePanel a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public void setRulePanel(RulePanel rulePanel) {
		this.rulePanel = rulePanel;
	}

	/**
	 * <p>getVisualRifEditor.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	/**
	 * <p>setVisualRifEditor.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	/** {@inheritDoc} */
	@Override
	public VisualRIFGraph getVisualGraph(){
		return super.getVisualGraph();
	}


	/** {@inheritDoc} */
	@Override
	protected void createNewOperatorContainer(AbstractContainer oc) {
	}


	/** {@inheritDoc} */
	@Override
	protected void createNewListOperator(ListOperator lo) {
	}


	/** {@inheritDoc} */
	@Override
	protected void createNewFrameOperator(FrameOperator fo) {
	}


	/** {@inheritDoc} */
	@Override
	protected void createNewConstantOperator(ConstantOperator co) {
	}


	/** {@inheritDoc} */
	@Override
	protected void createNewVariableOperator(VariableOperator vo) {
	}





	
	
	
	


















	

}
