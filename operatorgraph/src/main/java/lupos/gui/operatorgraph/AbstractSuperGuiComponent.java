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
package lupos.gui.operatorgraph;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.misc.Tuple;

public abstract class AbstractSuperGuiComponent extends JPanel implements MouseMotionListener, MouseListener {
	private static final long serialVersionUID = 4151343022557835950L;
	protected GraphWrapper gw;
	protected boolean movable = true;
	protected Border border = null;
	protected int positionX = 0;
	protected int positionY = 0;
	protected OperatorGraph parent;
	protected GraphBox box = null;
	private JPopupMenu contextMenu = null;

	protected AbstractSuperGuiComponent(OperatorGraph parent, GraphWrapper gw, boolean movable) {
		this.parent = parent;
		this.gw = gw;
		this.movable = movable;

		this.setOpaque(false);
		this.setBorder(new EmptyBorder(1, 1, 1, 1));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public GraphBox getBox() {
		if(this.box == null) { // if the box does not exist...
			this.box = this.parent.getBoxes().get(this.gw); // get it from the parent
		}

		return this.box; // return the box
	}

	/**
	 * This method checks whether the given panel is overlapping with this one
	 * or other way round.
	 * 
	 * @param panel
	 *            the panel to check with
	 * 
	 * @return true if the two panels overlap, otherwise false
	 */
	public boolean isOverlapping(AbstractSuperGuiComponent panel) {
		return this.overlaps(panel) || panel.overlaps(this);
	}

	/**
	 * This method checks if the given panel is overlapping with this one.
	 * 
	 * @param panel the panel to check with
	 * 
	 * @return true, if the given panel overlaps with this one, false otherwise
	 */
	protected boolean overlaps(AbstractSuperGuiComponent panel) {
		Point topLeft = panel.getLocation();
		Dimension size = panel.getPreferredSize();

		// check all four corners of the given panel. if one of them lies in this panel. return true, else return false...
		return this.isIn(topLeft.x, topLeft.y) || this.isIn(topLeft.x + size.width, topLeft.y) || this.isIn(topLeft.x, topLeft.y + size.height) || this.isIn(topLeft.x + size.width, topLeft.y + size.height);
	}

	/**
	 * Checks whether a point is in this panel.
	 * 
	 * @param x the x coordinate of the point to check
	 * @param y the y coordinate of the point to check
	 * 
	 * @return true, if the point is in this panel, false otherwise
	 */
	protected boolean isIn(int x, int y) {
		Point topLeft = this.getLocation();
		Dimension size = this.getPreferredSize();

		// point is between left and right edge of the panel...
		if(topLeft.x <= x && x <= topLeft.x + size.width) {
			// point is between top and bottom edge of the panel...
			if(topLeft.y <= y && y <= topLeft.y + size.height) {
				return true;
			}
		}

		return false;
	}

	public void setMovable(boolean movable) {
		this.movable = movable;
	}

	public boolean isAnnotation() {
		return !this.movable;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		if(this.isAnnotation()) {
			this.gw.drawAnnotationsBackground(g2d, this.getPreferredSize());
		}
		else {
			this.gw.drawBackground(g2d, this.getPreferredSize());
		}
	}

	public void setBorder(Border border) {
		super.setBorder(border);

		this.border = border;
	}

	public Border getMyBorder() {
		return this.border;
	}

	public void setBorderNoRemember(Border border) {
		super.setBorder(border);
	}

	public void updateSize() {}

	public Tuple<Point, Dimension> getPositionAndDimension() {
		return new Tuple<Point, Dimension>(this.getLocation(), this.getPreferredSize());
	}

	private void showContextMenu(MouseEvent me) {
		this.contextMenu.show(this, me.getX(), me.getY());
	}

	/**
	 * Method to add a context menu to this operator panel.
	 * 
	 * @param contextMenu
	 *            The context menu to add
	 */
	public void setContextMenu(JPopupMenu contextMenu) {
		this.contextMenu = contextMenu;
	}

	/**
	 * Method to remove the context menu from this operator panel.
	 */
	public void unsetContextMenu() {
		this.contextMenu = null;
	}

	/**
	 * This method determines whether this panel has a context menu or not.
	 * 
	 * @return true, if this panel has a context menu; false otherwise
	 */
	public boolean hasContextMenu() {
		return this.contextMenu != null;
	}


	public void mouseDragged(MouseEvent me) {
		if(!this.movable) {
			return;
		}

		// --- move the component - begin ---
		// determine offset between current position and position where movement began...
		int dx = me.getX() - this.positionX;
		int dy = me.getY() - this.positionY;

		// calculate new position...
		int newX = this.getLocation().x + dx;
		int newY = this.getLocation().y + dy;

		// set new x position...
		if(0 <= newX) {
			this.setLocation(newX, this.getLocation().y);
			this.getBox().setX(newX);
		}

		// set new y position...
		if(0 <= newY) {
			this.setLocation(this.getLocation().x, newY);
			this.getBox().setY(newY);
		}

		//		this.updateSize();

		this.parent.repaint(); // repaint parent to get arrowMovements
		// --- move the component - end ---
	}

	public void mousePressed(MouseEvent me) {
		// save position of click...
		this.positionX = me.getX();
		this.positionY = me.getY();

		if(this.contextMenu != null && me.isPopupTrigger()) {
			this.showContextMenu(me);
		}
	}

	public void mouseReleased(final MouseEvent me) {
		if(this.contextMenu != null && me.isPopupTrigger()) {
			this.showContextMenu(me);
		}
	}

	public void mouseMoved(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mouseClicked(MouseEvent arg0) {}
}