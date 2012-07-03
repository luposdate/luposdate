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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.util;

import java.util.HashMap;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator;
import lupos.gui.operatorgraph.visualeditor.util.Connection;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;

import org.json.JSONException;
import org.json.JSONObject;

public class RuleConnection extends Connection<Operator> {
	public RuleConnection(VisualEditor<Operator> visualEditor) {
		super(visualEditor);
	}

	public RuleConnection(VisualEditor<Operator> visualEditor, VisualGraph<Operator> queryGraph) {
		this(visualEditor);

		this.queryGraph = queryGraph;
	}

	protected String validateConnection() {
		return "";
	}

	public void createConnection(GraphWrapperEditable firstOp, JSONObject loadObject, HashMap<String, GraphWrapperEditable> tmp) throws JSONException {
		this.firstOp = firstOp;
		this.secondOp = tmp.get(loadObject.getString("to"));
		int opID = loadObject.getInt("id");

		String opIDLabel = (!loadObject.getString("id label").equals("")) ? loadObject.getString("id label") : "" + opID;


		// create connection between the two operators...
		this.firstOp.addSucceedingElement(new GraphWrapperIDTuple(this.secondOp, opID));
		this.secondOp.addPrecedingElement(this.firstOp);

		AbstractRuleOperator ruleOp1 = (AbstractRuleOperator) this.firstOp.getElement();
		AbstractRuleOperator ruleOp2 = (AbstractRuleOperator) this.secondOp.getElement();

		ruleOp1.setActiveConnection(ruleOp2, loadObject.getBoolean("active"));
		ruleOp1.setChildOpID(ruleOp2, opIDLabel);
		ruleOp1.setMode(ruleOp2, ModeEnum.valueOf(loadObject.getString("mode")));

		if(!opIDLabel.equals("-1")) {
			try {
				ruleOp2.setOpID(opIDLabel, loadObject.getBoolean("active"));
			}
			catch(ModificationException e) {
				e.printStackTrace();
			}
		}

		this.queryGraph.removeFromRootList(this.secondOp);

		// get the GraphBox of the GraphWrapper of the first operator...
		GraphBox firstBox = this.queryGraph.getBoxes().get(this.firstOp);

		// draw all annotations of the first Operator...
		firstBox.setLineAnnotations(this.firstOp.drawAnnotations(this.queryGraph));

		this.queryGraph.revalidate();
		this.visualEditor.repaint();
	}
}