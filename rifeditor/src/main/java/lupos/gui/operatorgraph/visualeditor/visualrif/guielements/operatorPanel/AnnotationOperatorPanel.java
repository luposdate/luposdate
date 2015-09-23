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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AnnotationOperator;
public class AnnotationOperatorPanel extends AbstractGuiComponent<Operator> {

	private static final long serialVersionUID = -6993813701569431678L;

	protected GridBagConstraints gbc = null;
	private JTextField textField;
	private JLabel label;
	private JButton addButton;
	private boolean minimizedByLostFocus;

	protected AnnotationOperator annotationOperator;

	// Constructor
	/**
	 * <p>Constructor for AnnotationOperatorPanel.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param operator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.AnnotationOperator} object.
	 * @param text a {@link java.lang.String} object.
	 * @param slabel a {@link java.lang.String} object.
	 * @param movable a boolean.
	 */
	public AnnotationOperatorPanel(final VisualGraph<Operator> parent,
			final GraphWrapper gw, final AnnotationOperator operator, final String text, final String slabel, final boolean movable) {

		super(parent, gw, operator, movable);

		this.annotationOperator = operator;


		/* ********************************************************************** **
		 * EBNF:                                            					  **
		 *                                              					      **
		 * IRIMETA        ::= '(*' IRICONST? (Frame | 'And' '(' Frame* ')')? '*)' **
		 * *********************************************************************** */

		this.init(operator, parent.PADDING, parent.getFONT(), text, slabel);
		this.updateSize();
		}

	private void init(final Operator operator, final double PADDING, final Font font, final String text, final String label) {
		this.setLayout(new GridBagLayout());

		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.NORTHWEST;
		this.gbc.gridwidth = this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1.0;

		this.gbc.insets = new Insets((int) this.parent.PADDING,
				(int) this.parent.PADDING, (int) this.parent.PADDING,
				(int) this.parent.PADDING);

		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.fill = GridBagConstraints.BOTH;

		final Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		this.setBorder(raisedbevel);

		this.textField = new JTextFieldResizing(text, font, this);

		this.textField.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(final FocusEvent fe) {
			    AnnotationOperatorPanel.this.minimizedByLostFocus = true;
				AnnotationOperatorPanel.this.minimizeTextField();
				AnnotationOperatorPanel.this.updateSize();
				}});

		final Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		this.label = new JLabel("Annotation");
		this.label.setBorder(loweredetched);
		this.label.setFont(this.parent.getFONT());
		this.addButton = new JButton("+");
		this.addButton.setFont(this.parent.getFONT());
		this.addButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(final ActionEvent e) {
				if(AnnotationOperatorPanel.this.annotationOperator.isMinimized()&&!AnnotationOperatorPanel.this.minimizedByLostFocus){
				AnnotationOperatorPanel.this.maximizeTextField();
				AnnotationOperatorPanel.this.textField.grabFocus();
				}
				AnnotationOperatorPanel.this.minimizedByLostFocus=false;
				AnnotationOperatorPanel.this.updateSize();
			}});
		this.add(this.label,this.gbc);
		this.gbc.gridy++;
		this.add(this.addButton,this.gbc);
	}

	private void maximizeTextField(){
		this.annotationOperator.setMinimized(false);
		this.minimizedByLostFocus = false;
		this.addButton.setText("-");

		this.textField.setText(this.annotationOperator.getAnnotation());

		this.gbc.gridx = 0;
		this.gbc.gridy++;

		this.textField.setPreferredSize(new Dimension(this.textField
				.getPreferredSize().width + 250, this.textField
				.getPreferredSize().height));

		this.add(this.textField,this.gbc);
		this.gbc.gridx++;
	}

	private void minimizeTextField(){
		this.annotationOperator.setMinimized(true);
		this.addButton.setText("+");
		this.annotationOperator.setAnnotation(this.textField.getText());
		this.remove(this.textField);
	}

	/** {@inheritDoc} */
	@Override
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void updateSize(){
		if(this.getComponentCount() >= 2){
			int maxWidthLeftColumn = 50;

			Container textField = null;
			Dimension d = null;
			textField = (Container) this.getComponent(1);
			final Dimension size = (textField instanceof JTextFieldResizing) ? ((JTextFieldResizing) textField)
					.calculateSize()
					: textField.getPreferredSize();

					maxWidthLeftColumn = Math.max(maxWidthLeftColumn,
							size.width);

					textField = (Container) this.getComponent(1);
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

		if (this.getComponentCount() < 2){

//			this.setMinimumSize(this.labelButton.getSize());
			if (this.getBox() != null) {
				this.getBox().height = this.getPreferredSize().height;
			}

			this.setSize(this.getPreferredSize());
			this.revalidate(); // re-validate the PrefixPanel
		}
	}

	/**
	 * <p>Getter for the field <code>textField</code>.</p>
	 *
	 * @return a {@link javax.swing.JTextField} object.
	 */
	public JTextField getTextField() {
		return this.textField;
	}

	/**
	 * <p>Setter for the field <code>textField</code>.</p>
	 *
	 * @param textField a {@link javax.swing.JTextField} object.
	 */
	public void setTextField(final JTextField textField) {
		this.textField = textField;
	}

	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return a {@link javax.swing.JLabel} object.
	 */
	public JLabel getLabel() {
		return this.label;
	}

	/**
	 * <p>Setter for the field <code>label</code>.</p>
	 *
	 * @param label a {@link javax.swing.JLabel} object.
	 */
	public void setLabel(final JLabel label) {
		this.label = label;
	}

	/**
	 * <p>Getter for the field <code>addButton</code>.</p>
	 *
	 * @return a {@link javax.swing.JButton} object.
	 */
	public JButton getAddButton() {
		return this.addButton;
	}

	/**
	 * <p>Setter for the field <code>addButton</code>.</p>
	 *
	 * @param addButton a {@link javax.swing.JButton} object.
	 */
	public void setAddButton(final JButton addButton) {
		this.addButton = addButton;
	}

	/**
	 * <p>isMinimizedByLostFocus.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isMinimizedByLostFocus() {
		return this.minimizedByLostFocus;
	}

	/**
	 * <p>Setter for the field <code>minimizedByLostFocus</code>.</p>
	 *
	 * @param minimizedByLostFocus a boolean.
	 */
	public void setMinimizedByLostFocus(final boolean minimizedByLostFocus) {
		this.minimizedByLostFocus = minimizedByLostFocus;
	}
}
