
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
package lupos.gui.operatorgraph.visualeditor.guielements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.guielements.ContainerArrange;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;
public class ContainerPanel extends AbstractGuiComponent<Operator> implements ContainerArrange{
	private static final long serialVersionUID = 1L;
	protected VisualGraph<Operator> qg = null;
	protected final JScrollPane graphSP;
	protected final JPanel graphPanel;
	protected final JLabel resizeLabel;

	/**
	 * <p>Constructor for ContainerPanel.</p>
	 *
	 * @param operator a {@link lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer} object.
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param graphPanel a {@link javax.swing.JPanel} object.
	 * @param qg a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 */
	public ContainerPanel(final OperatorContainer operator, final GraphWrapper gw, final JPanel graphPanel, final VisualGraph<Operator> qg, final VisualGraph<Operator> parent) {
		super(parent, gw, operator, true);

		this.qg = qg;
		this.qg.outerReference = this;
		this.graphPanel = graphPanel;

		this.setBackground(Color.WHITE);

		this.resizeLabel = new ResizeLabel(this);

		final Dimension sizeDimension = new Dimension(this.graphPanel.getPreferredSize().width + this.resizeLabel.getPreferredSize().width + 2, this.graphPanel.getPreferredSize().height + 2);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		this.setOpaque(true);
		this.setLayout(new GridBagLayout());
		this.setMinimumSize(sizeDimension);
		this.setPreferredSize(sizeDimension);

		this.graphSP = new JScrollPane(this.graphPanel);
		this.graphSP.setMinimumSize(this.graphPanel.getPreferredSize());
		this.graphSP.getVerticalScrollBar().setUnitIncrement(10);
		this.graphSP.getHorizontalScrollBar().setUnitIncrement(10);

		this.add(this.graphSP, gbc);

		gbc.gridx++;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.fill = GridBagConstraints.NONE;

		this.add(this.resizeLabel, gbc);
	}

	/**
	 * <p>resize.</p>
	 */
	public void resize() {
		this.graphPanel.setSize(this.graphPanel.getPreferredSize());

		this.revalidate();

		this.parent.repaint();

		if(this.getBox() != null) {
			this.getBox().width = this.getWidth();
			this.getBox().height = this.getHeight();
		}
	}

	/** {@inheritDoc} */
	public void arrange(final Arrange arrange) {
		final int centerX = this.getLocation().x + (this.getPreferredSize().width / 2);

		this.qg.setSize(new Dimension(0, 0));
		this.qg.setPreferredSize(new Dimension(0, 0));
		this.qg.arrange(arrange);

		final Dimension newSize = new Dimension(this.qg.getPreferredSize().width + this.resizeLabel.getPreferredSize().width + 5, this.qg.getPreferredSize().height + 5);

		this.setSize(newSize);
		this.setPreferredSize(newSize);
		this.resize();

		if(this.getBox() != null) {
			final int newX = centerX - (this.getPreferredSize().width / 2);

			this.getBox().updateX(newX, this.getBox().getY(),
					new HashSet<GraphBox>());
			this.getBox().width = newSize.width;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
		return this.qg.validateGraph(showErrors, data);
	}

	/**
	 * <p>getQueryGraph.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 */
	public VisualGraph<Operator> getQueryGraph() {
		return this.qg;
	}
}

class ResizeLabel extends JLabel implements MouseMotionListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private ContainerPanel parent = null;
	private int positionX = 0;
	private int positionY = 0;

	/**
	 * <p>Constructor for ResizeLabel.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.ContainerPanel} object.
	 */
	public ResizeLabel(final ContainerPanel parent) {
		super(parent.getQueryGraph().resizeIcon);

		this.parent = parent;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	/** {@inheritDoc} */
	public void mouseDragged(final MouseEvent me) {
		final int dx = me.getX() - this.positionX;
		final int dy = me.getY() - this.positionY;

		final int newX = this.getLocation().x + dx;
		final int newY = this.getLocation().y + dy;

		if(0 <= newX && newX + this.getWidth() >= this.parent.getMinimumSize().width) {
			this.setLocation(newX, this.getLocation().y);

			this.parent.setSize(newX + this.getWidth(), this.parent.getSize().height);
		}

		if(0 <= newY && newY + this.getHeight() >= this.parent.getMinimumSize().height) {
			this.setLocation(this.getLocation().x, newY);

			this.parent.setSize(this.parent.getSize().width, newY + this.getHeight());
		}

		this.parent.resize();
	}

	/** {@inheritDoc} */
	public void mousePressed(final MouseEvent me) {
		this.positionX = me.getX();
		this.positionY = me.getY();
	}

	/** {@inheritDoc} */
	public void mouseClicked(final MouseEvent me) {}
	/** {@inheritDoc} */
	public void mouseEntered(final MouseEvent me) {}
	/** {@inheritDoc} */
	public void mouseExited(final MouseEvent me) {}
	/** {@inheritDoc} */
	public void mouseReleased(final MouseEvent me) {}
	/** {@inheritDoc} */
	public void mouseMoved(final MouseEvent me) {}
}
