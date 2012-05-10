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
	private VisualGraph<Operator> qg = null;
	private final JScrollPane graphSP;
	private final JPanel graphPanel;
	private final JLabel resizeLabel;

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

	public void resize() {
		this.graphPanel.setSize(this.graphPanel.getPreferredSize());

		this.revalidate();

		this.parent.repaint();

		if(this.getBox() != null) {
			this.getBox().width = this.getWidth();
			this.getBox().height = this.getHeight();
		}
	}

	public void arrange(final boolean flipX, final boolean flipY,
			final boolean rotate,
			final Arrange arrange) {
		final int centerX = this.getLocation().x + (this.getPreferredSize().width / 2);

		this.qg.setSize(new Dimension(0, 0));
		this.qg.setPreferredSize(new Dimension(0, 0));
		this.qg.arrange(flipX, flipY, rotate, arrange);

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

	@Override
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
		return this.qg.validateGraph(showErrors, data);
	}

	public VisualGraph<Operator> getQueryGraph() {
		return this.qg;
	}
}

class ResizeLabel extends JLabel implements MouseMotionListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private ContainerPanel parent = null;
	private int positionX = 0;
	private int positionY = 0;

	public ResizeLabel(final ContainerPanel parent) {
		super(parent.getQueryGraph().resizeIcon);

		this.parent = parent;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

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

	public void mousePressed(final MouseEvent me) {
		this.positionX = me.getX();
		this.positionY = me.getY();
	}

	public void mouseClicked(final MouseEvent me) {}
	public void mouseEntered(final MouseEvent me) {}
	public void mouseExited(final MouseEvent me) {}
	public void mouseReleased(final MouseEvent me) {}
	public void mouseMoved(final MouseEvent me) {}
}