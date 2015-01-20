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
package lupos.gui.operatorgraph.visualeditor.util;

import javax.swing.JOptionPane;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.operators.MultiInputOperator;
import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;

/**
 * This class handles the request to create a new connection between two
 * operators. The user issues a connection-request from the tool-bar of the
 * VisualEditor. Once active the user has to do two clicks. The first click on
 * the first operator he wants to create a connection from and the second on the
 * operator he wants to create a connection two. After the first click an arrow
 * is drawn to the mouse where it goes. After the second click the connection is
 * added, if it is allowed to make a connection between these two operators.
 */
public abstract class Connection<T> {
	/**
	 * The operator to create a connection from.
	 */
	protected GraphWrapperEditable firstOp;

	/**
	 * The operator to create a connection to.
	 */
	protected GraphWrapperEditable secondOp;

	/**
	 * The dummy operator. This is needed to be able to draw an arrow to the
	 * mouse after the first click.
	 */
	protected T dummyOperator = null;

	/**
	 * The dummy GraphWrapper. This is needed to be able to draw an arrow to the
	 * mouse after the first click.
	 */
	protected GraphWrapperEditable dummyGW = null;

	/**
	 * The main VisualEditor.
	 */
	protected VisualEditor<T> visualEditor;

	/**
	 * The QueryGraph, where the connection is created on.
	 */
	protected VisualGraph<T> queryGraph;

	/**
	 * The item to use as content if the connection has content.
	 */
	protected Item item = new DummyItem();


	/**
	 * Creates a Connection object to connect two operators.
	 * 
	 * @param visualEditor
	 *            reference to the main visual editor
	 */
	protected Connection(final VisualEditor<T> visualEditor) {
		this.visualEditor = visualEditor;

		this.visualEditor.getStatusBar().setText("ConnectionMode: Click on the first operator you want to connect.");
	}

	/**
	 * Returns the dummy GraphBox on the given QueryGraphCanvas.
	 * 
	 * @param qgc
	 *            the QueryGraphCanvas to get the dummy GraphBox from
	 * 
	 * @return the dummy GraphBox
	 */
	public GraphBox getDummyBox(final VisualGraph<T> qgc) {
		return qgc.getBoxes().get(this.dummyGW);
	}

	/**
	 * This methods adds an operator to the Connection class and determines the
	 * next step. After the second operator is added this method automatically
	 * cancels the connectionMode of the VisualEditor and adds the connection.
	 * This method should be called two times during a connectionMode.
	 * 
	 * @param op
	 *            The operator to add to the connection.
	 */
	@SuppressWarnings("unchecked")
	public void addOperator(final T op) {
		if(this.firstOp == null) { // first call... add the first operator and
			// the dummy...
			this.firstOp = this.visualEditor.getVisualGraphs().get(0).createGraphWrapper(op); // save operator

			// determine the QueryGraph we are working on...
			this.queryGraph = this.firstOp.getGUIComponent().getParentQG();

			// add dummy operator to be able to draw the arrow to the current
			// mouse position...
			this.dummyOperator = this.queryGraph.createDummyOperator();
			this.dummyGW = this.queryGraph.createGraphWrapper(this.dummyOperator);
			final GraphBox dummyBox = this.queryGraph.graphBoxCreator.createGraphBox(this.queryGraph,this.dummyGW);

			this.queryGraph.getBoxes().put(this.dummyGW, dummyBox);

			this.firstOp.addSucceedingElement(new GraphWrapperIDTuple(this.dummyGW, 0));

			this.visualEditor.getStatusBar().setText("ConnectionMode: Click on the second operator you want to connect to finish the connection.");
		}
		else { // second call... end the connectionMode of the editor, remove
			// the dummy and create the connection.../
			this.secondOp = this.queryGraph.createGraphWrapper(op); // save operator

			this.cancel();

			this.createConnection(); // create the connection

			this.visualEditor.getStatusBar().clear();
		}
	}

	public void setConnectionContent(final Item item) {
		this.item = item;
	}

	/**
	 * This method checks whether the requested connection is valid or not and
	 * creates it, if it is.
	 */
	private void createConnection() {
		// --- error handling - begin ---
		String errorString = "";

		// no connection to itself if is not a RDFTerm...
		if(this.firstOp == this.secondOp && !(this.firstOp.getElement() instanceof RDFTerm)) {
			errorString = "You can't connect an operator, which is not a RDFTerm, with itself!";
		}

		// these two operators are already connected...
		for(final GraphWrapperIDTuple gwIDT : this.firstOp.getSucceedingElements()) {
			if(gwIDT.getOperator() == this.secondOp) {
				errorString = "This two operators are already connected!";

				break;
			}
		}

		if(!this.firstOp.canAddSucceedingElement()) {
			errorString = "You can't add any more children to this operator";
		}

		errorString = this.validateConnection();

		if(!errorString.equals("")) { // if there is any error... show a
			// message and end this method call...
			JOptionPane.showOptionDialog(this.visualEditor, errorString,
					"Connection Error", JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE, null, null, null);

			this.visualEditor.repaint();

			return;
		}
		// --- error handling - end ---

		int opID = 0;

		if(this.firstOp.getElement() instanceof MultiInputOperator) {
			opID = ((MultiInputOperator) this.firstOp.getElement()).getFreeOpID();
		}

		// create connection between the two operators...
		this.firstOp.addSucceedingElement(new GraphWrapperIDTuple(this.secondOp, opID));
		this.secondOp.addPrecedingElement(this.firstOp);

		// sort root nodes of operator containers...
		if(this.firstOp.getGUIComponent().getParentQG().outerReference != null) {
			((OperatorContainer) this.firstOp.getGUIComponent().getParentQG().outerReference.getParentOp()).determineRootNodes();
		}

		// special treatment for connections between two RDFTerms (add an empty
		// predicate)...
		if(this.firstOp.getElement() instanceof RDFTerm && this.secondOp.getElement() instanceof RDFTerm) {
			((RDFTerm) this.firstOp.getElement()).addPredicate((RDFTerm) this.secondOp.getElement(), this.item);
		}

		this.queryGraph.removeFromRootList(this.secondOp);

		// get the GraphBox of the GraphWrapper of the first operator...
		final GraphBox firstBox = this.queryGraph.getBoxes().get(this.firstOp);

		// draw all annotations of the first Operator...
		firstBox.setLineAnnotations(this.firstOp.drawAnnotations(this.queryGraph));

		this.firstOp.getGUIComponent().repaint();

		this.queryGraph.revalidate();
		this.queryGraph.repaint();
		this.visualEditor.repaint();
	}

	public void cancel() {
		this.visualEditor.connectionMode = null; // end connectionMode of the editor

		// remove the dummy elements...
		if(this.firstOp != null) {
			this.firstOp.removeSucceedingElement(this.dummyGW);
		}

		this.queryGraph.getBoxes().remove(this.dummyGW);
	}

	protected abstract String validateConnection();
}