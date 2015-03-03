
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
package lupos.gui.debug;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JPanel;

import lupos.engine.operators.BasicOperator;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
public abstract class AbstractCommentPanel extends JPanel implements
		MouseMotionListener, MouseListener {
	private static final long serialVersionUID = -2094761391105187397L;
	protected OperatorGraph operatorGraph;
	private int positionX = 0;
	private int positionY = 0;
	private final boolean movable = true;

	/**
	 * <p>Constructor for AbstractCommentPanel.</p>
	 *
	 * @param operatorGraph a {@link lupos.gui.operatorgraph.OperatorGraph} object.
	 */
	public AbstractCommentPanel(final OperatorGraph operatorGraph) {
		super();

		this.operatorGraph = operatorGraph;

		this.setFont(this.operatorGraph.getFONT());

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	/** {@inheritDoc} */
	public void setFont(final Font newFont) {
		super.setFont(newFont);

		this.updateFont(this, newFont);

		if (this.operatorGraph != null) {
			this.updateSize();
		}
	}

	/**
	 * <p>updateSize.</p>
	 */
	protected void updateSize() {
		if (this.getPreferredSize() != this.getSize())
			this.setSize(this.getPreferredSize());
	}

	/**
	 * <p>updateFont.</p>
	 *
	 * @param component a {@link java.awt.Container} object.
	 * @param newFont a {@link java.awt.Font} object.
	 */
	protected void updateFont(final Container component, final Font newFont) {
		for (int i = 0; i < component.getComponentCount(); i += 1) {
			final Component child = component.getComponent(i);

			child.setFont(newFont);

			if (child instanceof Container) {
				this.updateFont((Container) child, newFont);
			}
		}
	}

	/**
	 * <p>finalizeComponent.</p>
	 */
	protected void finalizeComponent() {
		this.determinePosition(); // determine the position

		this.operatorGraph.addComment(this); // add this panel to the operator
		// graph
	}

	/**
	 * Method to find the GraphWrapper to a given BasicOperator in the children
	 * of the given root GraphWrapper. If the GraphWrapper to the given
	 * BasicOperator was not found, the method returns null;
	 * 
	 * @param rootGW
	 *            GraphWrapper to start the search in
	 * @param basicOp
	 *            The given BasicOperator, where the GraphWrapper is searched
	 *            for
	 * 
	 * @return the GraphWrapper to the BasicOperator or null if there is none
	 */
	private GraphWrapper findChildGW(final GraphWrapper graphWrapper,
			final BasicOperator basicOp, final HashSet<GraphWrapper> visited) {
		if (visited.contains(graphWrapper)) {
			return null;
		}

		visited.add(graphWrapper);

		// check the root GraphWrapper...
		if (basicOp.equals(graphWrapper.getElement())) {
			return graphWrapper;
		}

		// walk through succeeding elements of this root GraphWrapper...
		for (final GraphWrapperIDTuple gwIDT : graphWrapper
				.getSucceedingElements()) {
			final GraphWrapper gw = gwIDT.getOperator(); // get the GraphWrapper

			final GraphWrapper found = this.findChildGW(gw, basicOp, visited);

			if (found != null) {
				return found;
			}
		}

		return null;
	}

	/**
	 * This method finds the GraphWrapper for a BasicOperator
	 *
	 * @param basicOperator
	 *            The BasicOperator for which the corresponding GraphWrapper
	 *            object should be found
	 * @return the found GraphWrapper or null in the case that the GraphWrapper
	 *         has not been found
	 */
	protected GraphWrapper findGraphWrapper(final BasicOperator basicOperator) {
		// get the root elements of the OperatorGraph...
		final LinkedList<GraphWrapper> rootList = this.operatorGraph
				.getRootList(false);

		// finding the GraphWrapper to the "from"-operator...
		final HashSet<GraphWrapper> visited = new HashSet<GraphWrapper>();
		GraphWrapper graphWrapper = null;

		// walk through root GraphWrappers of the OperatorGraph...
		for (final GraphWrapper rootGW : rootList) {
			graphWrapper = this.findChildGW(rootGW, basicOperator, visited);

			if (graphWrapper != null) {
				return graphWrapper;
			}
		}

		return null;
	}

	/** {@inheritDoc} */
	public void mouseDragged(final MouseEvent me) {
		if (!this.movable) {
			return;
		}

		// --- move the component - begin ---
		// determine offset between current position and position where movement
		// began...
		final int dx = me.getX() - this.positionX;
		final int dy = me.getY() - this.positionY;

		// calculate new position...
		final int newX = this.getLocation().x + dx;
		final int newY = this.getLocation().y + dy;

		// set new x position...
		if (0 <= newX) {
			this.setLocation(newX, this.getLocation().y);
		}

		// set new y position...
		if (0 <= newY) {
			this.setLocation(this.getLocation().x, newY);
		}

		this.operatorGraph.updateSize();
		this.operatorGraph.repaint(); // repaint parent to get arrowMovements
		// --- move the component - end ---
	}

	/** {@inheritDoc} */
	public void mousePressed(final MouseEvent me) {
		// save position of click...
		this.positionX = me.getX();
		this.positionY = me.getY();
	}

	/** {@inheritDoc} */
	public void mouseMoved(final MouseEvent me) {
	}

	/** {@inheritDoc} */
	public void mouseEntered(final MouseEvent me) {
	}

	/** {@inheritDoc} */
	public void mouseExited(final MouseEvent me) {
	}

	/** {@inheritDoc} */
	public void mouseReleased(final MouseEvent me) {
	}

	/** {@inheritDoc} */
	public void mouseClicked(final MouseEvent arg0) {
	}

	/**
	 * This function is called, when the operator graph was zoomed.
	 */
	public abstract void determinePosition();
}
