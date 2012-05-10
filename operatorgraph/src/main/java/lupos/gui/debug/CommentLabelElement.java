package lupos.gui.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.net.URL;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.table.JTableHeader;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.Result;
import lupos.gui.operatorgraph.DrawObject;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.viewer.OperatorGraphWithPrefix;
import lupos.gui.operatorgraph.util.VEImageIcon;
import lupos.misc.Tuple;

public class CommentLabelElement extends AbstractCommentPanel {
	private static final long serialVersionUID = -4304918759737643079L;

	/**
	 * The operator to which this CommentLabelElement is attached to
	 */
	private final BasicOperator fromBasicOperator;

	/**
	 * The operator to which this CommentLabelElement is moving to
	 */
	private final BasicOperator toBasicOperator;

	/**
	 * the animation thread for this panel
	 */
	private Thread animationthread = null;

	/**
	 * shared variable to interrupt the animation
	 */
	public volatile boolean stopAnimation = false;

	/**
	 * Number of milliseconds waited after each animation step
	 */
	protected volatile static int pause = 10;

	/**
	 * The percentage of steps made for each animation, should be <= 100 and >0
	 */
	protected volatile static double percentageSteps = 50;

	protected final Color backgroundColor;

	private static enum Icon {
		LIGHT, MAIL, TABS
	};

	private static String[] filenames = { "lightbulb_48.png", "mail_48.png",
			"tabs_48.png" };

	private static ImageIcon[] imageIcons = new ImageIcon[Icon.values().length];

	private static ImageIcon getImageIcon(final Icon icon, final int height) {
		if (imageIcons[icon.ordinal()] == null
				|| imageIcons[icon.ordinal()].getIconHeight() != height) {
			final URL url = CommentLabelElement.class.getResource("/icons/"
					+ filenames[icon.ordinal()]);
			if (url != null)
				imageIcons[icon.ordinal()] = VEImageIcon.getScaledIcon(url,
						2 * height);
			else
				imageIcons[icon.ordinal()] = VEImageIcon.getScaledIcon(url.getFile(), 2 * height);
		}
		return imageIcons[icon.ordinal()];
	}

	/**
	 * The constructor
	 * 
	 * @param operatorGraph
	 *            The operatorgraph
	 * @param fromBasicOperator
	 *            The operator to which this CommentLabelElement is attached to
	 * @param toBasicOperator
	 *            The operator to which this CommentLabelElement is moving to
	 * @param msg
	 *            The message to convey
	 * @param stepDelete
	 *            If true, the color will be red, otherwise it will be green
	 */
	public CommentLabelElement(final OperatorGraph operatorGraph,
			final BasicOperator fromBasicOperator,
			final BasicOperator toBasicOperator, final String msg,
			final boolean stepDelete) {
		super(operatorGraph);

		this.fromBasicOperator = fromBasicOperator;
		this.toBasicOperator = toBasicOperator;

		final GridBagConstraints gbc = this.getGridBagConstraints();

		if (stepDelete) {
			backgroundColor = new Color(255, 0, 0);
		} else {
			backgroundColor = new Color(0, 255, 0);
		}
		this.setLayout(new GridBagLayout());
		final JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0, 0));
		if (stepDelete) {
			panel.add(new JLabel("Delete:"), BorderLayout.WEST);
		}
		panel.add(new JLabel(msg), BorderLayout.EAST);

		final JPanel mainPanel = new JPanel();

		final int height = this.operatorGraph.getFONT().getSize();
		mainPanel.add(new JLabel(getImageIcon(Icon.MAIL, height)),
				BorderLayout.WEST);

		mainPanel.add(panel, BorderLayout.EAST);
		mainPanel.setBackground(new Color(0, 0, 0, 0));

		this.add(mainPanel, gbc);

		this.finalizeComponent();
	}

	/**
	 * Alternate constructor
	 * 
	 * @param operatorGraph
	 *            the operatorgraph
	 * @param fromBasicOperator
	 *            The operator to which this CommentLabelElement is attached to
	 */
	public CommentLabelElement(final OperatorGraph operatorGraph,
			final BasicOperator fromBasicOperator,
			final BasicOperator toBasicOperator, final Vector data,
			final Vector<String> columnNames, final boolean stepDelete) {
		super(operatorGraph);

		this.fromBasicOperator = fromBasicOperator;
		this.toBasicOperator = toBasicOperator;

		final GridBagConstraints gbc = this.getGridBagConstraints();

		if (stepDelete) {
			backgroundColor = new Color(255, 0, 0);
		} else {
			backgroundColor = new Color(0, 255, 0);
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JTable table = getTable(data, columnNames, this.operatorGraph);
		table.setBackground(Color.WHITE);

		this.setLayout(new GridBagLayout());

		panel.add(table, BorderLayout.CENTER);
		panel.add(table.getTableHeader(), BorderLayout.NORTH);

		if (stepDelete) {
			final JPanel zpanel = panel;
			panel = new JPanel();
			panel.setBackground(new Color(0, 0, 0, 0));
			panel.add(new JLabel("Delete:"), BorderLayout.WEST);
			panel.add(zpanel, BorderLayout.EAST);
		}
		final JPanel mainPanel = new JPanel();

		final int height = this.operatorGraph.getFONT().getSize();
		mainPanel.add(new JLabel(getImageIcon(Icon.LIGHT, height)),
				BorderLayout.WEST);

		mainPanel.add(panel, BorderLayout.EAST);
		mainPanel.setBackground(new Color(0, 0, 0, 0));

		this.add(mainPanel, gbc);

		this.finalizeComponent();
	}

	@Override
	protected void finalizeComponent() {
		this.setBackground(new Color(0, 0, 0, 0));
		this.determinePosition(); // determine the position

		this.operatorGraph.addComment(this); // add this panel to the
		// operatorgraph
	}

	public CommentLabelElement(final OperatorGraphWithPrefix operatorGraph,
			final CommentLabelElement lastCommentLabelElement,
			final Result result, final String content) {
		super(operatorGraph);
		this.fromBasicOperator = result;
		this.toBasicOperator = null;
		final GridBagConstraints gbc = this.getGridBagConstraints();

		backgroundColor = new Color(255, 20, 147);

		this.setLayout(new GridBagLayout());
		final JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0, 0));
		panel.add(new JLabel("Result of query:"), BorderLayout.NORTH);
		panel.add(new JLabel(content), BorderLayout.SOUTH);
		final JPanel mainPanel = new JPanel();

		final int height = this.operatorGraph.getFONT().getSize();
		mainPanel.add(new JLabel(getImageIcon(Icon.TABS, height)),
				BorderLayout.WEST);

		mainPanel.add(panel, BorderLayout.EAST);
		mainPanel.setBackground(new Color(0, 0, 0, 0));

		this.add(mainPanel, gbc);

		this.finalizeComponent(lastCommentLabelElement);
	}

	public CommentLabelElement(final OperatorGraphWithPrefix operatorGraph,
			final CommentLabelElement lastCommentLabelElement,
			final Result result, final Vector<Vector<String>> data,
			final Vector<String> columnNames) {
		super(operatorGraph);
		this.fromBasicOperator = result;
		this.toBasicOperator = null;
		final GridBagConstraints gbc = this.getGridBagConstraints();

		backgroundColor = new Color(255, 20, 147);

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JTable table = getTable(data, columnNames, this.operatorGraph);
		table.setBackground(Color.WHITE);

		panel.add(table, BorderLayout.SOUTH);
		panel.add(table.getTableHeader(), BorderLayout.NORTH);

		final JPanel zpanel = new JPanel();
		zpanel.setBackground(new Color(0, 0, 0, 0));
		zpanel.add(new JLabel("Result of query:"), BorderLayout.NORTH);
		zpanel.add(panel, BorderLayout.SOUTH);
		final JPanel mainPanel = new JPanel();

		final int height = this.operatorGraph.getFONT().getSize();
		mainPanel.add(new JLabel(getImageIcon(Icon.TABS, height)),
				BorderLayout.WEST);

		mainPanel.add(zpanel, BorderLayout.EAST);
		mainPanel.setBackground(new Color(0, 0, 0, 0));

		this.add(mainPanel, gbc);

		this.finalizeComponent(lastCommentLabelElement);
	}

	public CommentLabelElement(final OperatorGraphWithPrefix operatorGraph,
			final CommentLabelElement lastCommentLabelElement,
			final Result result, final JPanel resultPanel) {
		super(operatorGraph);
		this.fromBasicOperator = result;
		this.toBasicOperator = null;
		final GridBagConstraints gbc = this.getGridBagConstraints();

		backgroundColor = new Color(255, 20, 147);

		final JPanel mainPanel = new JPanel();

		final int height = this.operatorGraph.getFONT().getSize();
		mainPanel.add(new JLabel(getImageIcon(Icon.TABS, height)),
				BorderLayout.WEST);
		final JPanel zpanel = new JPanel();

		resultPanel.setBackground(new Color(0, 0, 0, 0));
		mainPanel.add(resultPanel, BorderLayout.CENTER);
		mainPanel.setBackground(new Color(0, 0, 0, 0));

		this.add(mainPanel, gbc);

		this.finalizeComponent(lastCommentLabelElement);
	}

	private void finalizeComponent(
			final CommentLabelElement lastCommentLabelElement) {
		if (lastCommentLabelElement == null)
			this.finalizeComponent();
		else {
			this.setBackground(new Color(0, 0, 0, 0));
			this.setLocation(lastCommentLabelElement.getLocation());

			lastCommentLabelElement.remove();

			this.operatorGraph.addComment(this); // add this panel to the
			// operator graph
		}
	}

	private GridBagConstraints getGridBagConstraints() {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets((int) this.operatorGraph.PADDING,
				(int) this.operatorGraph.PADDING,
				2 * (int) this.operatorGraph.PADDING,
				2 * (int) this.operatorGraph.PADDING);
		return gbc;
	}

	public static JTable getTable(final Vector data,
			final Vector<String> columnNames, final OperatorGraph operatorGraph) {
		final JTable table = new JTable(data, columnNames);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		ToolTipManager.sharedInstance().unregisterComponent(table);
		ToolTipManager.sharedInstance().unregisterComponent(
				table.getTableHeader());

		updateTable(table, operatorGraph);

		return table;
	}

	public static void updateTable(final JTable table,
			final OperatorGraph operatorGraph) {
		table.setFont(operatorGraph.getFONT());

		// int totalWidth = 0;
		int maxHeight = 1;

		for (int i = 0; i < table.getColumnCount(); i++) {
			final JLabel sizeLabel = new JLabel(table.getColumnName(i));
			sizeLabel.setFont(operatorGraph.getFONT());
			int maxWidth = sizeLabel.getPreferredSize().width + 2
					* table.getIntercellSpacing().width + 2;
			if (sizeLabel.getPreferredSize().height > maxHeight)
				maxHeight = sizeLabel.getPreferredSize().height
						+ (int) operatorGraph.PADDING;
			for (int j = 0; j < table.getRowCount(); j++) {
				final Object cell = table.getValueAt(j, i);
				if (cell != null) {
					final JComponent sizeLabel2 = new JLabel(cell.toString());
					sizeLabel2.setFont(operatorGraph.getFONT());
					final int width = sizeLabel2.getPreferredSize().width + 2
							* table.getIntercellSpacing().width
							+ table.getInsets().left + table.getInsets().right
							+ (int) operatorGraph.PADDING;
					final int height = sizeLabel2.getPreferredSize().height
							+ table.getInsets().top + table.getInsets().bottom
							+ (int) operatorGraph.PADDING;
					if (width > maxWidth)
						maxWidth = width;
					if (height > maxHeight)
						maxHeight = height;
				}
			}
			table.getColumnModel().getColumn(i).setMinWidth(maxWidth);
			table.getColumnModel().getColumn(i).setPreferredWidth(maxWidth);
			// totalWidth += maxWidth;
		}
		table.setRowHeight(maxHeight);
		// table.setPreferredSize(new Dimension(totalWidth, maxHeight
		// * table.getRowCount()));
	}

	private void updateTable(final Component component) {
		if (component instanceof JTable) {
			updateTable((JTable) component, this.operatorGraph);
		} else if (component instanceof JTableHeader) {
		} else {

			if (component instanceof Container) {
				final Container container = (Container) component;
				for (int i = 0; i < container.getComponentCount(); i++) {
					updateTable(container.getComponent(i));
				}
			}
		}
	}

	/**
	 * This function is called, when the operator graph was zoomed.
	 */
	@Override
	public void determinePosition() {
		final int height = this.operatorGraph.getFONT().getSize();
		getImageIcon(Icon.LIGHT, height);
		getImageIcon(Icon.MAIL, height);
		getImageIcon(Icon.TABS, height);

		updateTable(this);
		this.updateSize();

		if (this.animationthread != null) {
			stopAnimation = true;
			try {
				this.animationthread.join();
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		stopAnimation = false;

		final GraphWrapper fromGW = this
				.findGraphWrapper(this.fromBasicOperator);

		// --- define position for the commentPanel - begin --
		final Tuple<Point, Dimension> positionAndSizeOfFromOp = this.operatorGraph
				.getPositionAndDimension(fromGW);
		final Point positionOfFromOp = positionAndSizeOfFromOp.getFirst();
		final Dimension sizeOfFromOp = positionAndSizeOfFromOp.getSecond();
		// --- define position for the commentPanel - end --

		if (this.toBasicOperator == null) {
			this.setLocation(positionOfFromOp.x, positionOfFromOp.y
					+ sizeOfFromOp.height + (int) this.operatorGraph.PADDING);
			return;
		}

		final GraphWrapper toGW = this.findGraphWrapper(this.toBasicOperator);

		// --- define end position for the commentPanel - begin ---
		final Tuple<Point, Dimension> positionAndSizeOfToOp = this.operatorGraph
				.getPositionAndDimension(toGW);
		final Point positionOfToOp = positionAndSizeOfToOp.getFirst();
		final Dimension sizeOfToOp = positionAndSizeOfToOp.getSecond();
		// --- define end position for the commentPanel - begin ---

		// define the midpoint between starting and ending position
		final Point midpointFrom = new Point(positionOfFromOp.x
				+ sizeOfFromOp.width + (int) (4 * this.operatorGraph.PADDING),
				positionOfFromOp.y);
		final Point midpointTo = new Point(positionOfToOp.x + sizeOfToOp.width
				+ (int) (4 * this.operatorGraph.PADDING), positionOfToOp.y);

		this.setBounds(midpointFrom.x, midpointFrom.y,
				this.getPreferredSize().width, this.getPreferredSize().height);

		final int x = Math.abs(midpointTo.x - midpointFrom.x);
		final int y = Math.abs(midpointTo.y - midpointFrom.y);
		final double steps = Math.sqrt((double) x * x + (double) y * y);

		final double deltaX = ((midpointTo.x - midpointFrom.x)) / steps;
		final double deltaY = ((midpointTo.y - midpointFrom.y)) / steps;

		this.setLocation(midpointFrom.x, midpointFrom.y);

		// --- animation example - begin ---
		this.animationthread = new Thread() {
			@Override
			public void run() {
				for (double i = 0; i < steps; i += 100 / percentageSteps) {
					if (stopAnimation) {
						break;
					} else
						try {
							Thread.sleep(pause); // wait some milliseconds
						} catch (final InterruptedException e) {
							// no output
						}

					CommentLabelElement.this.setLocation(midpointFrom.x
							+ (int) (i * deltaX), midpointFrom.y
							+ (int) (i * deltaY));
				}

			}
		};
		this.animationthread.start();
		// --- animation example - end ---
	}

	/**
	 * This method removes this CommentLabelElement from the Operatorgraph
	 */
	public void remove() {
		// first wait for animations...
		if (this.animationthread != null && this.animationthread.isAlive()) {
			this.stopAnimation = true;
			try {
				this.animationthread.join();
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		// remove this comment label from the operator graph...
		this.operatorGraph.removeComment(this);
	}

	/**
	 * getter for animationgraph
	 * 
	 * @return
	 */
	public Thread getAnimationthread() {
		return animationthread;
	}

	public static synchronized int getPause() {
		return pause;
	}

	public static synchronized void setPause(final int pause) {
		CommentLabelElement.pause = pause;
	}

	public static synchronized double getPercentageSteps() {
		return percentageSteps;
	}

	public static synchronized void setPercentageSteps(
			final double percentageSteps) {
		CommentLabelElement.percentageSteps = percentageSteps;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);

		final Dimension size = this.getPreferredSize();
		final Graphics2D g2d = (Graphics2D) g;
		DrawObject.drawSimpleBoxShade(g2d, 0, 0, size.width - 1
				- (int) this.operatorGraph.PADDING, size.height - 1
				- (int) this.operatorGraph.PADDING, backgroundColor, new Color(
				128, 128, 128, 100), (int) this.operatorGraph.PADDING);
	}
}