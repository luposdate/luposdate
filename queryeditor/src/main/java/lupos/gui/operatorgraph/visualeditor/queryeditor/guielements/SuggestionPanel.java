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
package lupos.gui.operatorgraph.visualeditor.queryeditor.guielements;

import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.util.LinkedList;

import javax.swing.JComboBox;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboItem;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboListener;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboRenderer;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;

public class SuggestionPanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 1L;
	private final LinkedList<ComboItem> elements;
	private final JComboBox jCoBo;

	public SuggestionPanel(final VisualGraph<Operator> parent,
			final Operator op, final LinkedList<ComboItem> elements,
			final SuggestionRowPanel rowPanel) {
		super(parent, new GraphWrapperOperator(op), op, false);

		this.elements = elements;

		for (final MouseListener ml : this.getMouseListeners())
			this.removeMouseListener(ml);

		this.jCoBo = new JComboBox(elements.toArray());
		this.jCoBo.setRenderer(new ComboRenderer());
		this.jCoBo.addActionListener(new ComboListener(this.jCoBo, rowPanel));

		this.add(this.jCoBo);

		this.setPreferredSize(new Dimension(this.jCoBo.getPreferredSize().width
				+ (int) (2 * parent.PADDING),
				this.jCoBo.getPreferredSize().height
				+ (int) (2 * parent.PADDING)));
	}

	public SuggestionPanel(final int PADDING, final Operator op,
			final LinkedList<ComboItem> elements,
			final SuggestionRowPanel rowPanel) {
		super(null, new GraphWrapperOperator(op), op, false);

		this.elements = elements;

		for (final MouseListener ml : this.getMouseListeners())
			this.removeMouseListener(ml);

		this.jCoBo = new JComboBox(elements.toArray());
		this.jCoBo.setRenderer(new ComboRenderer());
		this.jCoBo.addActionListener(new ComboListener(this.jCoBo, rowPanel));

		this.add(this.jCoBo);

		this.setPreferredSize(new Dimension(this.jCoBo.getPreferredSize().width
				+ 2 * PADDING, this.jCoBo.getPreferredSize().height + 2
				* PADDING));
	}

	public LinkedList<ComboItem> getElements() {
		return this.elements;
	}

	public Item getSelectedElement() {
		return (Item) ((ComboItem) this.jCoBo.getSelectedItem()).getObject();
	}

	public boolean validateOperatorPanel(final boolean showErrors, Object data) {
		return true;
	}
}