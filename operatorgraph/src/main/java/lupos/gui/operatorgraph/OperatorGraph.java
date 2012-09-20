/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.gui.operatorgraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import lupos.gui.debug.AbstractCommentPanel;
import lupos.gui.operatorgraph.GraphBox.GraphBoxCreator;
import lupos.gui.operatorgraph.GraphBox.StandardGraphBoxCreator;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.guielements.ContainerArrange;
import lupos.misc.Tuple;
import xpref.IXPref;
import xpref.XPref;
import xpref.datatypes.BooleanDatatype;
import xpref.datatypes.FontDatatype;
import xpref.datatypes.IntegerDatatype;

/**
 * This class constructs the QueryGraph recursively by going through the given
 * start operators and there children. It arranges the operators so they don't
 * overlap. At the end the QueryGraph is a JPanel with JLabels and other JPanels
 * on it.
 * 
 * @author schleife
 */
public class OperatorGraph extends JPanel implements IXPref {
	/**
	 * The serial version uid.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Holds the list of operators which are the root level.
	 */
	protected LinkedList<GraphWrapper> rootList = new LinkedList<GraphWrapper>();

	/**
	 * Associated list of all boxes in the QueryGraph. The association is
	 * GraphWrapper to corresponding GraphBox.
	 */
	protected LinkedHashMap<GraphWrapper, GraphBox> boxes = new LinkedHashMap<GraphWrapper, GraphBox>();

	protected Hashtable<GraphWrapper, LinkedList<GraphWrapper>> drawnLineAnnotations = new Hashtable<GraphWrapper, LinkedList<GraphWrapper>>();

	/**
	 * Space between boxes.
	 */
	public double SPACING_X = 60;

	/**
	 * Space between rows.
	 */
	public double SPACING_Y = 90;

	/**
	 * Padding of the main window.
	 */
	public double PADDING = 5;

	/**
	 * Flag for line color usage.
	 */
	private boolean useLineColors = false;

	/**
	 * Current zoom factor.
	 */
	private double zoomFactor = 1.0;

	/**
	 * Font size of the elements of the OperatorGraph.
	 */
	protected double FONTSIZE = 16;

	/**
	 * Font of the elements of the OperatorGraph.
	 */
	private Font FONT = new Font("serif", Font.PLAIN, (int) this.FONTSIZE);

	protected LinkedHashSet<OperatorGraph> childComponents = new LinkedHashSet<OperatorGraph>();

	private final LinkedList<AbstractCommentPanel> comments = new LinkedList<AbstractCommentPanel>();

	private final LinkedHashMap<GraphWrapper, JPopupMenu> contextMenus = new LinkedHashMap<GraphWrapper, JPopupMenu>();

	/**
	 * used to lock accesses to the comments list!
	 */
	private final ReentrantLock commentsLock = new ReentrantLock();
	
	public GraphBoxCreator graphBoxCreator = new StandardGraphBoxCreator();
	
	protected OperatorGraph() {
		try {
			this.setLayout(null); // we don't want any LayoutManager
			this.setBackground(Color.WHITE);

			XPref.getInstance().registerComponent(this);
			this.preferencesChanged();
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * This is one way to get a OperatorGraph. This method should be used if you
	 * have one root element.
	 * 
	 * @param root
	 *            the root element
	 * 
	 * @return the JPanel with the QueryGraph on it
	 */
	public JPanel createGraph(final GraphWrapper root, final boolean flipX,
			final boolean flipY, final boolean rotate, final Arrange arrange) {
		// create rootList for the one root operator...
		final LinkedList<GraphWrapper> rootList = new LinkedList<GraphWrapper>();
		rootList.add(root);

		return this.createGraph(rootList, flipX, flipY, rotate, arrange); // create
																			// the
																	// QueryGraph
																	// and
																	// return
		// it
	}

	/**
	 * This is one way to get a OperatorGraph. This method should be used if you
	 * have a List of root elements.
	 * 
	 * @param rootList
	 *            the List of root elements
	 * 
	 * @return the JPanel with the QueryGraph on it
	 */
	public JPanel createGraph(final LinkedList<GraphWrapper> rootList,
			final boolean flipX, final boolean flipY, final boolean rotate,
			final Arrange arrange) {
		this.rootList = rootList;

		this.boxes.clear();

		this.removeAll();
		this.arrange(flipX, flipY, rotate, arrange);
		this.arrange(flipX, flipY, rotate, arrange);

		this.setPreferredSize(new Dimension(this.getPreferredSize().width + 5,
				this.getPreferredSize().height + 5));

		return this;
	}

	protected void createInternalNewGraph(final boolean flipX,
			final boolean flipY, final boolean rotate, final Arrange arrange) {
		this.createGraph(new LinkedList<GraphWrapper>(), flipX, flipY, rotate, arrange); 
		// create the QueryGraph and return it
	}

	public int getMax(final boolean X) {
		int max = 0;
		for (final Map.Entry<GraphWrapper, GraphBox> entry : this.boxes
				.entrySet()) {
			final GraphBox graphBox = entry.getValue();
			final int compare = X ? graphBox.getX() + graphBox.width : graphBox
					.getY() + graphBox.height;
			if (compare > max)
				max = compare;
		}
		return max;
	}

	public void mirror(final boolean X) {
		final int max = getMax(X);
		for (final Map.Entry<GraphWrapper, GraphBox> entry : this.boxes
				.entrySet()) {
			final GraphBox graphBox = entry.getValue();
			if (X) {
				final int oldValue = graphBox.getX() + graphBox.width;
				graphBox.setX(max - oldValue);
			} else {
				final int oldValue = graphBox.getY() + graphBox.height;
				graphBox.setY(max - oldValue);
			}
		}
	}

	public void addNewBoxes(final HashSet<GraphWrapper> visited,
			final GraphWrapper op) {
		if (visited.contains(op)) { // if current operator was visited before...
			return; // abort
		}
		visited.add(op);

		if (!this.boxes.containsKey(op)) {
			this.boxes.put(op, this.graphBoxCreator.createGraphBox(this, op));
		}

		// walk trough the children of the current operator and add them
		// recursively...
		for (final GraphWrapperIDTuple child : op.getSucceedingElements()) {
			addNewBoxes(visited, child.getOperator());
		}
	}

	public void addNewBoxes() {
		final HashSet<GraphWrapper> visited = new HashSet<GraphWrapper>();
		for (final GraphWrapper op : this.rootList)
			addNewBoxes(visited, op);
	}

	public synchronized void arrange(final boolean flipX, final boolean flipY,
			final boolean rotate,
			final Arrange arrange) {

		addNewBoxes();

		if (rotate) {
			exchangeHeightWidth(false);
		}

		arrange.arrange(this, flipX, flipY, rotate);

		if (rotate) {
			exchangeHeightWidth(true);
			for (final GraphBox b : this.boxes.values()) {
					final int tmp = b.getX();
					b.setXWithoutUpdatingParentsSize(b.getY());
					b.setYWithoutUpdatingParentsSize(tmp);
			}
		}

		if (flipX)
			mirror(true);
		if (flipY)
			mirror(false);

		this.updateSize();

		this.repaint(); // repaint the panel to trigger G2D redraw
	}

	public void exchangeHeightWidth(final boolean exchangeContainerPanel) {
		for (final GraphBox b : this.boxes.values()) {
			if (!(b.element instanceof ContainerArrange)
					|| !exchangeContainerPanel) {
				final int tmp = b.height;
				b.height = b.width;
				b.width = tmp;
			}
		}
	}

	public String serializeGraph() {
		final StringBuffer ret = new StringBuffer();

		for (int i = 0; i < this.rootList.size(); ++i) {
			if (i > 1) {
				ret.append("\n");
			}

			ret.append(this.rootList.get(i).serializeObjectAndTree());
		}

		return ret.toString();
	}



	public void clearAll() {
		this.removeAll();
		this.rootList.clear();
		this.boxes.clear();
		this.drawnLineAnnotations.clear();
		GraphBox.resetLineColorIndex();

		this.createInternalNewGraph(false, false, false, Arrange.values()[0]);
	}

	public boolean hasElements() {
		return !this.boxes.isEmpty();
	}

	/**
	 * This returns the boxes.
	 * 
	 * @return the HashMap GraphWrapper to GraphBox for all boxes
	 */
	public HashMap<GraphWrapper, GraphBox> getBoxes() {
		return this.boxes;
	}

	public void removeFromRootList(final GraphWrapper gw) {
		this.rootList.remove(gw);
	}

	public void addToRootList(final GraphWrapper gw) {
		if(!this.rootList.contains(gw)){
			this.rootList.add(gw);
		}
	}

	/**
	 * This overrides the paintComponent() method from JComponent to add redraw
	 * features for the GraphBox elements in the GraphRows that need the
	 * Graphics2D component.
	 */
	@Override
	protected synchronized void paintComponent(final Graphics g) {
		super.paintComponent(g);

		final Graphics2D g2d = (Graphics2D) g;

		this.drawnLineAnnotations.clear();

		// draw the Graphics2D part of the boxes...
		for (final GraphBox box : this.boxes.values()) {
			box.draw(g2d);
		}

		// set context menu to panel again, if it is not set...
		for (final GraphWrapper graphWrapper : this.contextMenus.keySet()) {
			final AbstractSuperGuiComponent asgc = this.boxes.get(graphWrapper)
			.getElement();

			if (!asgc.hasContextMenu()) {
				asgc.setContextMenu(this.contextMenus.get(graphWrapper));
			}
		}

		// add comment panels again...
		final HashSet<Component> components = new HashSet<Component>();
		for (final Component component : this.getComponents()) {
			components.add(component);
		}
		for (final JPanel commentPanel : this.comments) {
			if (!components.contains(commentPanel)) {
				this.add(commentPanel, 0);
			}
		}
	}

	/**
	 * lock the paint call, such that no comment panels are added or removed
	 * during painting the child components...
	 */
	@Override
	public void paint(final Graphics g) {
		commentsLock.lock();
		try {
			super.paint(g);
		} finally {
			commentsLock.unlock();
		}
	}

	public boolean annotationWasProcessed(final GraphWrapper from,
			final GraphWrapper to) {
		if (!this.drawnLineAnnotations.containsKey(from)) {
			return false;
		}

		if (!this.drawnLineAnnotations.get(from).contains(to)) {
			return false;
		}

		return true;
	}

	public void addProcessedAnnotation(final GraphWrapper from,
			final GraphWrapper to) {
		if (!this.drawnLineAnnotations.containsKey(from)) {
			this.drawnLineAnnotations.put(from, new LinkedList<GraphWrapper>());
		}

		if (!this.drawnLineAnnotations.get(from).contains(to)) {
			this.drawnLineAnnotations.get(from).add(to);
		}
	}

	public int getMaxY() {
		int y = 0;
		for (final Map.Entry<GraphWrapper, GraphBox> entry : this.boxes
				.entrySet()) {
			y = Math.max(y, entry.getValue().getY() + entry.getValue().height);
		}
		return y;
	}

	/**
	 * Updates the size of the Canvas and sets it to the minimum needed size.
	 */
	public void updateSize() {
		int width = 0;
		int height = 0;

		for (final GraphBox box : this.boxes.values()) {
			height = Math.max(height, box.getY() + box.height
					+ (int) (2 * this.PADDING));
			width = Math.max(width, box.getX() + box.width
					+ (int) (2 * this.PADDING));

			for (final GraphWrapper gw : box.getLineAnnotations().keySet()) {
				width = Math.max(width, box.getLineAnnotations().get(gw)
						.getPreferredSize().width
						+ (int) (2 * this.PADDING));
			}
		}

		commentsLock.lock();
		try {
			for (final AbstractCommentPanel acp : this.comments) {
				final Point location = acp.getLocation();
				final Dimension dimension = acp.getPreferredSize();

				height = Math.max(height, location.y + dimension.height + 2
						* (int) this.PADDING);
				width = Math.max(width, location.x + dimension.width + 2
						* (int) this.PADDING);
			}
		} finally {
			commentsLock.unlock();
		}

		// if (width < this.getMinimumSize().width) {
		// width = this.getMinimumSize().width;
		// }
		//
		// if (height < this.getMinimumSize().height) {
		// height = this.getMinimumSize().height;
		// }

		final Dimension d = new Dimension(width, height);

		this.setSize(d);
		this.setPreferredSize(d);

		this.revalidate();
	}

	/**
	 * Method to update the zoom factor of the OperatorGraph.
	 * 
	 * @param zFactor
	 *            the new zoom factor
	 * 
	 * @return true, if the zoom factor was different then the previous, false
	 *         otherwise
	 */
	public boolean updateZoomFactor(final double zFactor) {
		// disable zoom to 0%...
		if(zFactor == 0.0) {
			return false;
		}

		// update zoom factor in child operator graphs...
		for(final OperatorGraph opGraph : this.childComponents) {
			opGraph.updateZoomFactor(zFactor);
		}

		if(zFactor != this.zoomFactor) { // if zoom factor changed...
			if(this.zoomFactor != 1.0) { // if old zoom factor is not 1...
				// reset variables to zoom factor 1...
				this.setZoomFactors(1 / this.zoomFactor);
			}

			if(zFactor != 1.0) { // if zoom factor is not 1...
				this.setZoomFactors(zFactor); // set variables to zoom factor
			}

			this.zoomFactor = zFactor; // save zoom factor

			return true;
		}

		return false;
	}

	/**
	 * This internal method resets some displacement variables according to the
	 * given zoomFactor.
	 * 
	 * @param zoomFactor
	 *            the zoom factor
	 */
	protected void setZoomFactors(final double zoomFactor) {
		this.SPACING_X = zoomFactor * this.SPACING_X;
		this.SPACING_Y = zoomFactor * this.SPACING_Y;
		this.PADDING = zoomFactor * this.PADDING;

		this.FONTSIZE *= zoomFactor;
		this.FONT = new Font(this.FONT.getFontName(), this.FONT.getStyle(), (int) Math.ceil(this.FONTSIZE));
	}

	public double getSPACING_X() {
		return this.SPACING_X;
	}

	public double getSPACING_Y() {
		return this.SPACING_Y;
	}

	public Font getFONT() {
		return this.FONT;
	}

	public double getFONTSize() {
		return this.FONTSIZE;
	}

	public double getZoomFactor() {
		return this.zoomFactor;
	}

	/**
	 * Returns the root list of GraphWrappers of the OperatorGraph.
	 * 
	 * @param clone
	 *            true if the list should be cloned (needed for zoom update).
	 * 
	 * @return list of root GraphWrapper elements
	 */
	@SuppressWarnings("unchecked")
	public LinkedList<GraphWrapper> getRootList(final boolean clone) {
		if(clone) {
			return (LinkedList<GraphWrapper>) this.rootList.clone();
		}
		else {
			return this.rootList;
		}
	}

	public void addChildComponent(final OperatorGraph child) {
		this.childComponents.add(child);

		child.zoomFactor = this.zoomFactor;
		child.setZoomFactors(this.zoomFactor);
	}

	public void setLineColorStatus(final boolean newStatus) {
		this.useLineColors = newStatus;

		for(final OperatorGraph opGraph : this.childComponents) {
			opGraph.setLineColorStatus(newStatus);
		}
	}

	public boolean useLineColors() {
		return this.useLineColors;
	}

	public void preferencesChanged() {
		try {
			this.SPACING_X = IntegerDatatype.getFirstValue("viewer_spacing_X") * this.zoomFactor;
			this.SPACING_Y = IntegerDatatype.getFirstValue("viewer_spacing_Y") * this.zoomFactor;
			this.useLineColors = BooleanDatatype.getValues("viewer_useColoredArrows").get(0).booleanValue();

			// --- resetting zoom - begin ---
			final double oldZoomFactor = this.zoomFactor;

			this.updateZoomFactor(1.0);
			// --- resetting zoom - end ---

			// --- updating font - begin ---
			this.FONT = FontDatatype.getValues("viewer_font").get(0);
			this.FONTSIZE = this.FONT.getSize();
			// --- updating font - end ---

			// --- updating zoom - begin ---
			if (oldZoomFactor != 1.0) {
				this.updateZoomFactor(oldZoomFactor);
			}

			this.removeAll();
			this.boxes.clear();
			this.drawnLineAnnotations.clear();
			GraphBox.resetLineColorIndex();
			GraphWrapper.clearColorCache();

			// TODO arrange with values from toolbar
			// this.arrange();
			// --- updating zoom - end ---
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Internal method to create the graph from the given start node and wrap it
	 * in a JScrollPane.
	 */
	public void updateMainPanel(final JPanel mainPanel) {
		final JComponent containingMainPanel = (JComponent) mainPanel.getParent().getParent().getParent();

		containingMainPanel.removeAll();

		// create scrollPane for main panel set speed of scrollPane...
		final JScrollPane operatorGraphScrollPane = new JScrollPane(mainPanel);
		operatorGraphScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		operatorGraphScrollPane.getHorizontalScrollBar().setUnitIncrement(10);

		operatorGraphScrollPane.getViewport().setBackground(Color.WHITE);

		containingMainPanel.add(operatorGraphScrollPane, BorderLayout.CENTER);
		containingMainPanel.revalidate();

		// update comments...
		this.commentsLock.lock();

		try {
			for(final AbstractCommentPanel acp : this.comments) {
				acp.setFont(this.FONT);
				acp.determinePosition();
			}
		}
		finally {
			this.commentsLock.unlock();
		}

		this.updateSize();
	}

	public Tuple<Point, Dimension> getPositionAndDimension(final GraphWrapper gw) {
		if (this.boxes.containsKey(gw)) {
			final AbstractSuperGuiComponent panel = this.boxes.get(gw)
			.getElement();

			if (panel != null) {
				return panel.getPositionAndDimension();
			} else {
				return new Tuple<Point, Dimension>(new Point(0, 0),
						new Dimension(0, 0));
			}
		} else {
			return new Tuple<Point, Dimension>(new Point(0, 0), new Dimension(
					0, 0));
		}
	}

	/**
	 * Method to add a comment to the operator graph.
	 * 
	 * @param commentPanel
	 *            the AbstractCommentPanel, that defines the comment
	 */
	public void addComment(final AbstractCommentPanel commentPanel) {
		this.commentsLock.lock();

		try {
			this.comments.add(commentPanel);
		} finally {
			this.commentsLock.unlock();
		}

		commentPanel.setFont(this.FONT);
		commentPanel.setVisible(true);

		this.updateSize();
		this.repaint();
	}

	/**
	 * Method to remove the given comment panel from the operator graph.
	 * 
	 * @param commentPanel
	 *            the comment panel to be removed.
	 */
	public void removeComment(final AbstractCommentPanel commentPanel) {
		commentPanel.setVisible(false);

		this.commentsLock.lock();

		try {
			this.comments.remove(commentPanel);
		} finally {
			this.commentsLock.unlock();
		}

		this.repaint();
	}

	/**
	 * This method sets the given context menu to the operator wrapped in the
	 * given GraphWrapper.
	 * 
	 * @param graphWrapper
	 *            the element to set the context menu of
	 * @param contextMenu
	 *            the context menu to add
	 */
	public void setContextMenuOfOperator(final GraphWrapper graphWrapper,
			final JPopupMenu contextMenu) {
		if (this.boxes.containsKey(graphWrapper)) {
			this.boxes.get(graphWrapper).getElement().setContextMenu(
					contextMenu);

			this.contextMenus.put(graphWrapper, contextMenu);
		}
	}

	/**
	 * This method unsets the context menu of the operator wrapped in the given
	 * GraphWrapper.
	 * 
	 * @param graphWrapperthe
	 *            element to unset the context menu of
	 */
	public void unsetContextMenuOfOperator(final GraphWrapper graphWrapper) {
		if (this.boxes.containsKey(graphWrapper)) {
			this.boxes.get(graphWrapper).getElement().unsetContextMenu();

			this.contextMenus.remove(graphWrapper);
		}
	}

	/**
	 * Internal method to save the graph to the given filename.
	 * 
	 * @param filename
	 *            filename to save the file to
	 * @throws IOException 
	 */
	public void saveGraph(String filename) throws IOException {
		// add file extension, if necessary...
		if (!(filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".gif"))) {
			filename += ".png";
		}
		
		String format = filename.endsWith(".jpeg")?"jpeg":filename.substring(filename.length()-3);

		OutputStream out = new FileOutputStream(new File(filename));
		
		this.saveGraph(format, out);

		out.close();
	}
	
	/**
	 * Internal method to save the graph to an outputstream.
	 * 
	 * @param format
	 *            the format of the picture (png, jpg or gif)
	 * @param out
	 *            the outputstream to save the file to
	 */
	public void saveGraph(String format, OutputStream out) {
		// add file extension, if necessary...
		if (!(format.compareTo("png")==0 || format.compareTo("jpeg")==0 || format.compareTo("jpg")==0 || format.compareTo("gif")==0)) {
			format = "png";
		}

		try {
			// create image of graph to save it...
			final BufferedImage img = new BufferedImage(this.getPreferredSize().width, this.getPreferredSize().height, BufferedImage.TYPE_INT_RGB);
			this.printComponents(img.createGraphics());
			this.paint(img.createGraphics()); // paint main panel with graph image

			ImageIO.write(img, format, out);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public Hashtable<GraphWrapper, LinkedList<GraphWrapper>> getDrawnLineAnnotations() {
		return this.drawnLineAnnotations;
	}
	
	public boolean isEmpty() {
		return this.boxes.size() == 0;
	}
}