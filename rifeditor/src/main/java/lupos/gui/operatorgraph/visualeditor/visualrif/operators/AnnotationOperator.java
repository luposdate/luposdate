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
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.AnnotationOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.RuleOperatorPanel;
import lupos.misc.util.OperatorIDTuple;

public class AnnotationOperator extends Operator {
	
	private String annotation = ""; 
	private boolean minimized = true;
	
	
	
//	public AnnotationOperator(JSONObject loadObject) throws JSONException {
//		this.annotation = (String) loadObject.get("TEXT");
//	}



	@Override

	public void prefixRemoved(String prefix, String namespace) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prefixAdded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prefixModified(String oldPrefix, String newPrefix) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		
		this.panel = new AnnotationOperatorPanel(parent, gw, this,annotation,"Annotation",true);
		
		return this.panel;
	}

	@Override
	public StringBuffer serializeOperator() {
		StringBuffer sb = new StringBuffer();
		sb.append("(* "+this.annotation+" *)");
		return sb;
	}

	@Override
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer sb = new StringBuffer();
		sb.append("(* "+this.annotation+" *)");
		for(OperatorIDTuple<Operator> opIDT : this.getSucceedingOperators()) {
			sb.append(opIDT.getOperator().serializeOperator());
//			System.out.println(opIDT.serializeOperator());
		}
		
		
		return sb;
	}

	@Override
	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean validateOperator(boolean showErrors,
			HashSet<Operator> visited, Object data) {
		

		
		if(visited.contains(this)) {
			return true;
		}

		visited.add(this);
		
	
		
		

		if (this.getSucceedingOperators().size() == 0){
			if (showErrors) {
				JOptionPane
						.showOptionDialog(
								this.panel.getParentQG().visualEditor,
								"Please connect the Annotation Operator with the Operator, you want annotate!",
								"Error", JOptionPane.DEFAULT_OPTION,
								JOptionPane.ERROR_MESSAGE, null, null, null);
				return false;
			
		}

		}

		return true;
		
	}

	
	// Getter + Setter
	
	public String getAnnotation() {
		return annotation;
	}
	
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	
	public boolean isMinimized() {
		return minimized;
	}

	public void setMinimized(boolean minimized) {
		this.minimized = minimized;
	}

	
	public JSONObject toJSON() throws JSONException {
		JSONObject saveObject = new JSONObject();

		saveObject.put("TEXT", this.getAnnotation());
		
		Point position = ((AnnotationOperatorPanel) this.panel).getPositionAndDimension().getFirst();
		saveObject.put("POSITION",  new double[]{position.getX(), position.getY()});
		
		return  saveObject;
	}
	
	public void fromJSON(JSONObject loadObject) throws JSONException {
		this.annotation = (String) loadObject.get("TEXT");
	}
}
