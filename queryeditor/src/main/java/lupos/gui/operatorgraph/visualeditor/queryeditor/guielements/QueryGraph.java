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
package lupos.gui.operatorgraph.visualeditor.queryeditor.guielements;

import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperPrefix;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperPrefixNonEditable;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.ConstructTemplateContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.QueryRDFTerm;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.TripleContainer;

import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;
import lupos.gui.operatorgraph.visualeditor.guielements.ContainerPanel;


public class QueryGraph extends VisualGraphOperatorWithPrefix {
	private static final long serialVersionUID = -9170756530703844692L;

	public QueryGraph(final VisualEditor<Operator> visualEditor, final Prefix prefix) {
		super(visualEditor, prefix);
	}

	/**
	 * This is one way to get a QueryGraph. This method should be used if you
	 * have one root element.
	 * 
	 * @param root
	 *            the root element
	 * 
	 * @return the JPanel with the QueryGraph on it
	 */
	@Override
	public JPanel createGraph(final GraphWrapper root, final Arrange arrange) {
		// create rootList for the one root operator...
		final LinkedList<GraphWrapper> rootList = new LinkedList<GraphWrapper>();
		rootList.add(root);
		rootList.add(new GraphWrapperPrefix(this.prefix));

		return this.createGraph(rootList, arrange);
	}

	@Override
	protected void createInternalNewGraph(final Arrange arrange) {
		final LinkedList<GraphWrapper> rootList = new LinkedList<GraphWrapper>();
		rootList.add(new GraphWrapperPrefix(this.prefix));

		this.createGraph(rootList, arrange);
	}

	@Override
	protected boolean validateAddOperator(final int x, final int y, final String newClassName) {
		// --- error handling - begin ---
		if(newClassName.equals("QueryRDFTerm") && this.outerReference == null) {
			final int ret = JOptionPane.showOptionDialog(
					this.visualEditor,
					"RDFTerm is only allowed in TripleContainer and ConstructTemplateContainer!",
					"Insert Error",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.ERROR_MESSAGE,
					null,
					new Object[] {"Ok", "Create an TripleContainer and insert it there", "Create a ConstructTemplateContainer and insert it there" }, 0);

			if(ret == 1) {
				this.visualEditor.prepareOperatorForAdd(TripleContainer.class);
			}
			else if(ret == 2) {
				this.visualEditor.prepareOperatorForAdd(ConstructTemplateContainer.class);
			}

			if(ret == 1 || ret == 2) {
				final Operator operator = this.addOperator(x, y);

				this.visualEditor.prepareOperatorForAdd(QueryRDFTerm.class);
				((ContainerPanel) operator.getGUIComponent()).getQueryGraph().addOperator((int) this.PADDING, (int) this.PADDING);
			}

			return false;
		}

		if(this.outerReference != null && !newClassName.equals("QueryRDFTerm")) {
			JOptionPane.showOptionDialog(this.visualEditor, "Only RDFTerm-Operators are allowed inside a Container!", "Insert Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

			return false;
		}
		// --- error handling - end ---

		return true;
	}

	@Override
	protected void handleAddOperator(final Operator newOp) {
		if(this.outerReference != null) {
			newOp.setParentContainer((OperatorContainer) this.outerReference.getOperator(), new HashSet<Operator>());

			((OperatorContainer) this.outerReference.getOperator()).addOperator(newOp);
		}
	}


	@Override
	public void mouseMoved(final MouseEvent me) {
		super.mouseMoved(me);

		if(!this.visualEditor.isInInsertMode && this.outerReference != null) {
			this.outerReference.mouseMoved(me);
		}
	}

	@Override
	public void mouseDragged(final MouseEvent me) {
		super.mouseDragged(me);

		if(!this.visualEditor.isInInsertMode && this.outerReference != null) {
			this.outerReference.mouseDragged(me);
		}
	}

	@Override
	public void mouseEntered(final MouseEvent me) {
		super.mouseEntered(me);

		if(!this.visualEditor.isInInsertMode && this.outerReference != null) {
			this.outerReference.mouseEntered(me);
		}
	}

	@Override
	public void mouseExited(final MouseEvent me) {
		super.mouseExited(me);

		if(!this.visualEditor.isInInsertMode && this.outerReference != null) {
			this.outerReference.mouseExited(me);
		}
	}

	@Override
	public void mousePressed(final MouseEvent me) {
		super.mousePressed(me);

		if(!this.visualEditor.isInInsertMode && this.outerReference != null) {
			this.outerReference.mousePressed(me);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent me) {
		super.mouseReleased(me);

		if(!this.visualEditor.isInInsertMode && this.outerReference != null) {
			this.outerReference.mouseReleased(me);
		}
	}

	@Override
	public void mouseClicked(final MouseEvent me) {
		super.mouseClicked(me);

		if(this.outerReference != null) {
			this.outerReference.mouseClicked(me);
		}
	}

	@Override
	public String serializeGraph() {
		final String object = super.serializeSuperGraph();

		final StringBuffer ret = this.prefix.getPrefixString("PREFIX ", "");
		ret.append(object);

		return ret.toString();
	}

	@Override
	public VisualGraphOperatorWithPrefix newInstance(VisualEditor<Operator> visualEditor, Prefix prefix) {
		return new QueryGraph(visualEditor, prefix);
	}
}