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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator;

public class VariablePanel extends AbstractGuiComponent<Operator>{

	private static final long serialVersionUID = -6789368326247215391L;

	protected GridBagConstraints gbc = null;
	private VariableOperator variableOperator;

	public VariablePanel(final VisualGraph<Operator> parent, final GraphWrapper gw,
			final VariableOperator operator, final boolean movable) {
		super(parent, gw, operator, movable);
		this.parent = parent;
		this.setVariableOperator(operator);
		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.NORTHWEST;
		this.gbc.gridwidth = this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1.0;

		this.gbc.insets = new Insets((int) parent.PADDING,
				(int) parent.PADDING, (int) parent.PADDING,
				(int) parent.PADDING);

		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.fill = GridBagConstraints.BOTH;

		final JTextFieldResizing textField = new JTextFieldResizing(
				"", this.parent.getFONT(), this);

		textField.setPreferredSize(new Dimension(15 , 20));

		textField.addFocusListener((new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent fe){
				VariablePanel.this.updateSize();
			}

			@Override
			public void focusLost(final FocusEvent fe) {
				VariablePanel.this.variableOperator.setVariable(textField.getText());
				VariablePanel.this.updateSize();
			}
		}));
		textField.addKeyListener( new KeyListener()
		{
			@Override
			public void keyTyped( final KeyEvent e ) {
			}
			@Override
			public void keyPressed( final KeyEvent e ) {
			}
			@Override
			public void keyReleased( final KeyEvent e ) {
				if(e.getKeyCode()==10){
					VariablePanel.this.updateSize();
				}
			}
		});

		textField.setText(this.variableOperator.getVariable());
		this.add(textField,this.gbc);

		//		LineBorder lineBorder = new LineBorder(Color.black,1);
		final Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		TitledBorder titled;
		final Font font = new Font(Font.SANS_SERIF, Font.ROMAN_BASELINE, 11);
		titled = BorderFactory.createTitledBorder(raisedbevel," Variable ", 0, 0, font, Color.BLACK);
		this.setBorder(titled);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				VariablePanel.this.updateSize();
			}
		});
		this.updateSize();
	}

	@Override
	public void updateSize(){
		int maxWidthLeftColumn = 50;
		Container textField = null;
		Dimension d = null;
		textField = (Container) this.getComponent(0);
		final Dimension size = (textField instanceof JTextFieldResizing) ? ((JTextFieldResizing) textField)
				.calculateSize()
				: textField.getPreferredSize();

				maxWidthLeftColumn = Math.max(maxWidthLeftColumn,
						size.width);

				textField = (Container) this.getComponent(0);
				d = new Dimension(maxWidthLeftColumn, textField
						.getPreferredSize().height);
				textField.setPreferredSize(d);
				textField.setSize(d);
				textField.setMaximumSize(d);
				textField.setMinimumSize(d);
				textField.repaint();

				if (this.getBox() != null) {
					this.getBox().height = this.getPreferredSize().height;
				}

				this.setSize(this.getPreferredSize());
				this.revalidate();
	}

	@Override
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
		return true;
	}

	public VariableOperator getVariableOperator() {
		return this.variableOperator;
	}

	public void setVariableOperator(final VariableOperator variableOperator) {
		this.variableOperator = variableOperator;
	}
}
