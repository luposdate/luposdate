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
package lupos.gui.operatorgraph.visualeditor.guielements;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;

import javax.swing.ImageIcon;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.util.Connection;
import lupos.gui.operatorgraph.visualeditor.util.DummyItem;
import lupos.gui.operatorgraph.util.VEImageIcon;

public abstract class VisualGraph<T> extends OperatorGraph implements MouseListener, MouseMotionListener {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 8524683982131639117L;

	/**
	 * A reference to the VisualEditor class.
	 */
	public VisualEditor<T> visualEditor;

	public AbstractGuiComponent<T> outerReference = null;

	public ImageIcon addIcon;
	public ImageIcon delIcon;
	public ImageIcon resizeIcon;

	protected VisualGraph(final VisualEditor<T> visualEditor) {
		super();

		this.visualEditor = visualEditor;
	}

	protected void construct() {
		this.addIcon = VEImageIcon.getPlusIcon((int) (this.FONTSIZE));
		this.delIcon = VEImageIcon.getMinusIcon((int) (this.FONTSIZE));
		this.resizeIcon = VEImageIcon.getResizeIcon((int) (this.FONTSIZE));

		// register listeners...
		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		this.createInternalNewGraph(lupos.gui.operatorgraph.arrange.Arrange.values()[0]);
	}

	/**
	 * Adds the chosen operator with an empty string as content in it to the
	 * QueryGraphCanvas. Returns the new added Operator.
	 */
	public T addOperator(final int x, final int y) {
		return this.addOperator(x, y, new DummyItem());
	}

	/**
	 * Adds the chosen operator with the given content in it to the
	 * QueryGraphCanvas. Returns the new added Operator.
	 */
	public T addOperator(final int x, final int y, final Item content) {
		this.visualEditor.isInInsertMode = false; // leave insertMode

		final Class<? extends T> clazz = this.visualEditor.getInsertOperator(); // get the class

		// get some class names...
		final String newClassName = clazz.getSimpleName();

		if(!this.validateAddOperator(x, y, newClassName)) {
			return null;
		}

		this.visualEditor.activateGraphMenus();

		try {
			final T newOp = this.createOperator(clazz, content);

			this.handleAddOperator(newOp);

			final GraphWrapper gw = this.createGraphWrapper(newOp);

			// create the GraphBox at the right position...
			final GraphBox box = this.graphBoxCreator.createGraphBox(this, gw);
			box.setY(y);
			box.updateX(x, y, new HashSet<GraphBox>());
			box.arrange(Arrange.values()[0]);
			box.getElement().revalidate();

			this.boxes.put(gw, box);
			this.rootList.add(gw);

			this.revalidate();
			this.visualEditor.repaint();

			return newOp;
		}
		catch(final Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	public void addOperator(final int x, final int y, final T newOp) {
		this.visualEditor.isInInsertMode = false; // leave insertMode

		// get some class names...
		final String newClassName = newOp.getClass().getSimpleName();
		final String newClassSuperName = newOp.getClass().getSuperclass().getSimpleName();

		this.validateAddOperator(x, y, newClassName);

		this.visualEditor.activateGraphMenus();

		this.handleAddOperator(newOp);

		//		final GraphWrapper gw = new GraphWrapperOperator(newOp, this.prefix);
		final GraphWrapper gw = this.createGraphWrapper(newOp);

		// find out whether the operator is a subclass of RetrieveData...
		if(newClassSuperName.startsWith("RetrieveData")) {
			this.rootList.add(gw);
		}

		// create the GraphBox at the right position...
		final GraphBox box = this.graphBoxCreator.createGraphBox(this, gw);
		box.setX(x);
		box.setY(y);
		box.arrange(Arrange.values()[0]);
		box.getElement().revalidate();

		this.boxes.put(gw, box);
		this.rootList.add(gw);

		this.revalidate();
		this.visualEditor.repaint();
	}


	public boolean validateGraph(final boolean showErrors, final Object data) {
		for(final GraphWrapper gw : this.rootList) {
			if(((GraphWrapperEditable) gw).validateObject(showErrors, data) == false) {
				return false;
			}
		}

		return true;
	}

	public String serializeSuperGraph() {
		return super.serializeGraph();
	}

	public String getFreeVariable(final String variableName) {
		final StringBuffer variable = new StringBuffer(variableName);

		boolean inUse = true;

		while(inUse) {
			for(final GraphWrapper op : this.rootList) {
				inUse = ((GraphWrapperEditable) op).variableInUse(variable.toString());

				if(inUse) {
					variable.append("0");

					break;
				}
			}
		}

		return variable.toString();
	}

	/**
	 * This internal method resets some displacement variables according to the
	 * given zoomFactor.
	 * 
	 * @param zoomFactor the zoom factor
	 */
	@Override
	protected void setZoomFactors(final double zoomFactor) {
		this.addIcon = VEImageIcon.getPlusIcon((int) (this.FONTSIZE * zoomFactor));
		this.delIcon = VEImageIcon.getMinusIcon((int) (this.FONTSIZE * zoomFactor));
		this.resizeIcon = VEImageIcon.getResizeIcon((int) (this.FONTSIZE * zoomFactor));

		super.setZoomFactors(zoomFactor);
	}

	public void clear() {
		// do not need to handle this here
	}


	@Override
	public void mouseMoved(final MouseEvent me) {
		// get connection mode...
		final Connection<T> connectionMode = this.visualEditor.connectionMode;

		if(connectionMode != null) {
			final GraphBox dummyBox = connectionMode.getDummyBox(this); // get dummy operator

			if(dummyBox != null) { // update position of dummy box...
				dummyBox.setX(me.getX());
				dummyBox.setY(me.getY());

				this.visualEditor.repaint();
			}
		}
	}

	@Override
	public void mouseClicked(final MouseEvent me) {
		if(this.visualEditor.isInInsertMode) { // deal with insert mode...
			this.addOperator(me.getX(), me.getY());

			this.visualEditor.getStatusBar().clear();

			return;
		}
	}

	@Override
	public void mouseDragged(final MouseEvent me) {
		// need only e.g. mouseClicked to listen
	}
	@Override
	public void mouseEntered(final MouseEvent me) {
		// need only e.g. mouseClicked to listen		
	}
	@Override
	public void mouseExited(final MouseEvent me) {
		// need only e.g. mouseClicked to listen		
	}
	@Override
	public void mousePressed(final MouseEvent me) {
		// need only e.g. mouseClicked to listen		
	}
	@Override
	public void mouseReleased(final MouseEvent me) {
		// need only e.g. mouseClicked to listen		
	}


	protected abstract boolean validateAddOperator(int x, int y, String newClassName);
	protected abstract void handleAddOperator(T newOp);
	@Override
	public abstract String serializeGraph();

	public abstract GraphWrapperEditable createGraphWrapper(T op);
	public abstract T createDummyOperator();
	protected abstract T createOperator(Class<? extends T> clazz, Item content) throws Exception;
}