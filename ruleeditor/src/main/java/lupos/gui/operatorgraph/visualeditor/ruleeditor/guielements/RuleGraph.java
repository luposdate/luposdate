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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements;

import java.util.HashMap;
import java.util.Iterator;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperator;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.JumpOverOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleConnection;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RuleGraph extends VisualGraphOperator {
	private static final long serialVersionUID = 2078366369309044348L;

	public RuleGraph(VisualEditor<Operator> visualEditor) {
		super(visualEditor);

		this.SPACING_X = 90;
		this.SPACING_Y = 190;

		this.construct();
	}

	protected void handleAddOperator(Operator newOp) {}

	public String serializeGraph() {
		return "";
	}

	protected boolean validateAddOperator(int x, int y, String newClassName) {
		return true;
	}

	protected Operator createOperator(Class<? extends Operator> clazz, Item content) throws Exception {
		// get the chosen operator...
		Operator newOp = null;

		try {
			newOp = clazz.getDeclaredConstructor(RuleEnum.class, String.class).newInstance(RuleEnum.values()[0], "");
		}
		catch(NoSuchMethodException nsme) {
			newOp = clazz.newInstance();
		}

		return newOp;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject saveObject = new JSONObject();

		JSONObject connectionsObject = new JSONObject();

		for(GraphWrapper gw : this.boxes.keySet()) {
			AbstractRuleOperator op = (AbstractRuleOperator) gw.getElement();

			saveObject.put(op.getName(), op.toJSON(connectionsObject));
		}

		if(connectionsObject.length() > 0) {
			saveObject.put("connections", connectionsObject);
		}

		return saveObject;
	}

	@SuppressWarnings("unchecked")
	public void fromJSON(JSONObject loadObject) throws JSONException {
		Iterator<String> keyIt = loadObject.keys();

		HashMap<String, GraphWrapperEditable> tmp = new HashMap<String, GraphWrapperEditable>();

		while(keyIt.hasNext()) {
			String ruleOpName = keyIt.next();

			if(ruleOpName.equals("connections")) {
				continue;
			}

			JSONObject ruleOpLoadObject = loadObject.getJSONObject(ruleOpName);

			AbstractRuleOperator op;

			if(ruleOpLoadObject.has("op type") && ruleOpLoadObject.getString("op type").equals("JumpOverOperator")) {
				op = new JumpOverOperator(ruleOpName, ruleOpLoadObject);
			}
			else {
				op = new RuleOperator(ruleOpName, ruleOpLoadObject);
			}

			JSONArray positionArray = ruleOpLoadObject.getJSONArray("position");

			this.addOperator(positionArray.getInt(0), positionArray.getInt(1), op);

			tmp.put(ruleOpName, this.createGraphWrapper(op));
		}

		if(loadObject.has("connections")) {
			JSONObject connectionsLoadObject = loadObject.getJSONObject("connections");

			RuleConnection ruleConnection = new RuleConnection(this.visualEditor, this);

			keyIt = connectionsLoadObject.keys();

			while(keyIt.hasNext()) {
				String parentName = keyIt.next();
				JSONArray connections = connectionsLoadObject.getJSONArray(parentName);

				for(int i = 0; i < connections.length(); i += 1) {
					ruleConnection.createConnection(tmp.get(parentName), connections.getJSONObject(i), tmp);
				}
			}
		}
	}
}