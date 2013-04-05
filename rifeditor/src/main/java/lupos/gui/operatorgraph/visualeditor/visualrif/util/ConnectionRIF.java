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
package lupos.gui.operatorgraph.visualeditor.visualrif.util;

import javax.swing.JOptionPane;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.Connection;
import lupos.gui.operatorgraph.visualeditor.util.DummyItem;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AndContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.OrContainer;

/**
 * This class handles the request to create a new connection between two
 * operators. The user issues a connection-request from the tool-bar of the
 * VisualEditor. Once active the user has to do two clicks. The first click on
 * the first operator he wants to create a connection from and the second on the
 * operator he wants to create a connection two. After the first click an arrow
 * is drawn to the mouse where it goes. After the second click the connection is
 * added, if it is allowed to make a connection between these two operators.
 * 
 * @author schleife
 */
public abstract class ConnectionRIF<T> extends Connection<T>{

	/**
	 * Creates a Connection object to connect two operators.
	 * 
	 * @param visualEditor
	 *            reference to the main visual editor
	 */
	protected ConnectionRIF(final VisualEditor<T> visualEditor) {
		super(visualEditor);
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
	@Override
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
			final GraphBox dummyBox = this.visualEditor.getVisualGraphs().get(0).graphBoxCreator.createGraphBox(this.queryGraph,this.dummyGW);

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

	/**
	 * This method checks whether the requested connection is valid or not and
	 * creates it, if it is.
	 */
	protected boolean createConnection() {
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

			return false;
		}
		// --- error handling - end ---

		int opID = 0;

//		if(this.firstOp.getElement() instanceof MultiInputOperator) {
//			opID = ((MultiInputOperator) this.firstOp.getElement()).getFreeOpID();
//		}

		// create connection between the two operators...
		this.firstOp.addSucceedingElement(new GraphWrapperIDTuple(this.secondOp, opID));
		this.secondOp.addPrecedingElement(this.firstOp);

		// sort root nodes of operator containers...
		if(this.firstOp.getGUIComponent().getParentQG().outerReference != null) {
			if(this.firstOp.getGUIComponent().getParentQG().outerReference.getParentOp() instanceof AndContainer)
			((AndContainer) this.firstOp.getGUIComponent().getParentQG().outerReference.getParentOp()).determineRootNodes();
			if(this.firstOp.getGUIComponent().getParentQG().outerReference.getParentOp() instanceof OrContainer)
				((OrContainer) this.firstOp.getGUIComponent().getParentQG().outerReference.getParentOp()).determineRootNodes();
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
		return true;
	}
}
