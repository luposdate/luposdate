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
package lupos.gui.operatorgraph.visualeditor.queryeditor.guielements;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboItem;
public class SuggestionRowPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private int startX = -1;
	private int startY = -1;
	private int endX = -1;
	private int endY = -1;
	private int count = 0;
	private QueryResult queryResult;
	private JComboBox predBox;
	private JComboBox soBox;
	private Variable varPred;
	private Variable varSO;
	private LinkedList<ComboItem> predElements;
	private LinkedList<ComboItem> soElements;

	/**
	 * <p>Constructor for SuggestionRowPanel.</p>
	 *
	 * @param queryResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param varSO a {@link lupos.datastructures.items.Variable} object.
	 * @param varPred a {@link lupos.datastructures.items.Variable} object.
	 */
	public SuggestionRowPanel(QueryResult queryResult, Variable varSO, Variable varPred) {
		super();

		this.queryResult = queryResult;
		this.varPred = varPred;
		this.varSO = varSO;
	}

	/** {@inheritDoc} */
	public Component add(Component comp) {
		super.add(comp);

		this.count++;

		if(this.count == 1) { // first component...
			this.startX = comp.getLocation().x + comp.getPreferredSize().width;
			this.startY = comp.getLocation().y + (comp.getPreferredSize().height / 2);

			if(comp instanceof SuggestionPanel) {
				SuggestionPanel panel = (SuggestionPanel) comp;
				this.soBox = (JComboBox) panel.getComponent(0);
				this.soElements = panel.getElements();
			}
		}
		else if(this.count == 2) { // the predicate component...
			SuggestionPanel panel = (SuggestionPanel) comp;
			this.predBox = (JComboBox) panel.getComponent(0);
			this.predElements = panel.getElements();
		}
		else if(this.count == 3) { // last component...
			this.endX = comp.getLocation().x;
			this.endY = comp.getLocation().y + (comp.getPreferredSize().height / 2);

			if(comp instanceof SuggestionPanel) {
				SuggestionPanel panel = (SuggestionPanel) comp;
				this.soBox = (JComboBox) panel.getComponent(0);
				this.soElements = panel.getElements();
			}
		}

		return comp;
	}

	/** {@inheritDoc} */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		GraphBox.drawConnection((Graphics2D) g, this.startX, this.startY, this.endX, this.endY, true);
	}


	/** {@inheritDoc} */
	public void actionPerformed(ActionEvent ae) {
		ComboItem choice;

		String first = ((JComboBox) ae.getSource()).getItemAt(0).toString();

		if(first.equalsIgnoreCase(this.varSO.toString())) {
			choice = (ComboItem) this.soBox.getSelectedItem();

			if(choice.toString().equalsIgnoreCase(this.varSO.toString())) {
				for(ComboItem ci : this.predElements)
					ci.isEnabled = true;

				return;
			}

			for(ComboItem ci : this.predElements)
				ci.isEnabled = false;

			this.predElements.get(0).isEnabled = true;

			for(Bindings b : this.queryResult) {
				int i = this.predElements.indexOf(new ComboItem(b.get(this.varPred)));

				if(b.get(this.varSO).toString().equalsIgnoreCase(choice.toString()))
					this.predElements.get(i).isEnabled = true;
			}

			if(!((ComboItem) this.predBox.getSelectedItem()).isEnabled)
				this.predBox.setSelectedIndex(0);

			return;
		}

		if(first.equalsIgnoreCase(this.varPred.toString())) {
			choice = (ComboItem) this.predBox.getSelectedItem();

			if(choice.toString().equalsIgnoreCase(this.varPred.toString())) {
				for(ComboItem ci : this.soElements)
					ci.isEnabled = true;

				return;
			}

			for(ComboItem ci : this.soElements)
				ci.isEnabled = false;

			this.soElements.get(0).isEnabled = true;

			for(Bindings b : this.queryResult) {
				int i = this.soElements.indexOf(new ComboItem(b.get(this.varSO)));

				if(b.get(this.varPred).toString().equalsIgnoreCase(choice.toString()))
					this.soElements.get(i).isEnabled = true;
			}

			if(!((ComboItem) this.soBox.getSelectedItem()).isEnabled)
				this.soBox.setSelectedIndex(0);

			return;
		}
	}
}
