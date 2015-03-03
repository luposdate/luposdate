
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

import java.awt.Point;
import java.util.HashSet;

import javax.swing.JOptionPane;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.AnnotationOperatorPanel;
import lupos.misc.util.OperatorIDTuple;

import org.json.JSONException;
import org.json.JSONObject;
public class AnnotationOperator extends Operator {

	private String annotation = "";
	private boolean minimized = true;

	/** {@inheritDoc} */
	@Override
	public void prefixRemoved(final String prefix, final String namespace) {
	}

	/** {@inheritDoc} */
	@Override
	public void prefixAdded() {
	}

	/** {@inheritDoc} */
	@Override
	public void prefixModified(final String oldPrefix, final String newPrefix) {
	}

	/** {@inheritDoc} */
	@Override
	public AbstractGuiComponent<Operator> draw(final GraphWrapper gw, final VisualGraph<Operator> parent) {
		this.panel = new AnnotationOperatorPanel(parent, gw, this,this.annotation,"Annotation",true);
		return this.panel;
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer serializeOperator() {
		final StringBuffer sb = new StringBuffer();
		sb.append("(* "+this.annotation+" *)");
		return sb;
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer serializeOperatorAndTree(final HashSet<Operator> visited) {
		final StringBuffer sb = new StringBuffer();
		sb.append("(* "+this.annotation+" *)");
		for(final OperatorIDTuple<Operator> opIDT : this.getSucceedingOperators()) {
			sb.append(opIDT.getOperator().serializeOperator());
		}
		return sb;
	}

	/** {@inheritDoc} */
	@Override
	public boolean variableInUse(final String variable, final HashSet<Operator> visited) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean validateOperator(final boolean showErrors,
			final HashSet<Operator> visited, final Object data) {
		if(visited.contains(this)) {
			return true;
		}
		visited.add(this);
		if (this.getSucceedingOperators().size() == 0){
			if (showErrors) {
				JOptionPane.showOptionDialog(
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
	/**
	 * <p>Getter for the field <code>annotation</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAnnotation() {
		return this.annotation;
	}

	/**
	 * <p>Setter for the field <code>annotation</code>.</p>
	 *
	 * @param annotation a {@link java.lang.String} object.
	 */
	public void setAnnotation(final String annotation) {
		this.annotation = annotation;
	}

	/**
	 * <p>isMinimized.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isMinimized() {
		return this.minimized;
	}

	/**
	 * <p>Setter for the field <code>minimized</code>.</p>
	 *
	 * @param minimized a boolean.
	 */
	public void setMinimized(final boolean minimized) {
		this.minimized = minimized;
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject toJSON() throws JSONException {
		final JSONObject saveObject = new JSONObject();
		saveObject.put("TEXT", this.getAnnotation());
		final Point position = ((AnnotationOperatorPanel) this.panel).getPositionAndDimension().getFirst();
		saveObject.put("POSITION",  new double[]{position.getX(), position.getY()});
		return  saveObject;
	}

	/**
	 * <p>fromJSON.</p>
	 *
	 * @param loadObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public void fromJSON(final JSONObject loadObject) throws JSONException {
		this.annotation = (String) loadObject.get("TEXT");
	}
}
