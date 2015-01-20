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
package lupos.gui.operatorgraph.visualeditor.operators;

import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.guielements.MultiInputPanel;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.misc.util.OperatorIDTuple;

public abstract class MultiInputOperator extends Operator {
	@Override
	public AbstractGuiComponent<Operator> draw(final GraphWrapper gw, final VisualGraph<Operator> parent) {
		this.panel = new MultiInputPanel(this, gw, parent, this.toString());
		this.panel.setContextMenu(this.buildContextMenu(gw, parent));

		return this.panel;
	}

	private JPopupMenu buildContextMenu(final GraphWrapper gw, final VisualGraph<Operator> parent) {
		final JPopupMenu contextMenu = new JPopupMenu();

		this.addAvailableOperators(contextMenu, parent, gw);

		return contextMenu;
	}

	protected void replaceOperator(final Operator newOP, final VisualGraph<Operator> parent, final GraphWrapper oldGW) {
		newOP.cloneFrom(this);
		this.replaceWith(newOP);

		final GraphWrapper gw = new GraphWrapperOperator(newOP);
		final GraphBox box = parent.getBoxes().get(oldGW);

		box.initBox(gw);
		box.arrange(Arrange.values()[0]);

		parent.getBoxes().remove(oldGW);
		parent.getBoxes().put(gw, box);

		parent.remove(this.panel);
		parent.add(box.getElement());

		for(final Operator preOp : this.precedingOperators) {
			final GraphWrapper preGW = new GraphWrapperOperator(preOp);
			final GraphBox preBox = parent.getBoxes().get(preGW);

			preBox.setLineAnnotations(preOp.drawAnnotations(parent));
		}

		parent.revalidate();
		parent.repaint();
	}

	@Override
	public boolean validateOperator(final boolean showErrors, final HashSet<Operator> visited, final Object data) {
		if(this.succeedingOperators.size() < 2) {
			JOptionPane.showOptionDialog(this.panel.getVisualEditor(), "A multiinput operator must have at least two child!", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null , null);

			return false;
		}

		for(final OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			if(!opIDT.getOperator().validateOperator(showErrors, visited, data)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean canAddSucceedingOperator() {
		return (this.succeedingOperators.size() < 2);
	}

	@Override
	public boolean variableInUse(final String variable, final HashSet<Operator> visited) {
		for(final OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getOperator().variableInUse(variable, visited))
				return true;

		return false;
	}

	public void prefixRemoved(final String prefix, final String namespace) {}
	public void prefixAdded() {}
	public void prefixModified(final String oldPrefix, final String newPrefix) {}

	public abstract void addAvailableOperators(JPopupMenu popupMenu, final VisualGraph<Operator> parent, final GraphWrapper oldGW);
	public abstract int getFreeOpID();
	
	@Override
	public String getXPrefID(){
		return "queryEditor_style_multiinputoperator";
	}
}