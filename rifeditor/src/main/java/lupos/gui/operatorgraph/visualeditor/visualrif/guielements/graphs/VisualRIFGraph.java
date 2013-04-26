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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs;

import java.util.HashSet;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ClassificationOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator;

public abstract class VisualRIFGraph<T> extends VisualGraph<T> {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 8524683982131639118L;
	protected VisualRifEditor visualRifEditor;

	protected VisualRIFGraph(final VisualEditor<T> visualEditor) {
		super(visualEditor);
		this.visualEditor = visualEditor;
	}

	/**
	 * Adds the chosen operator with the given content in it to the
	 * QueryGraphCanvas. Returns the new added Operator.
	 */
	@Override
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

			this.createOperator(newOp);

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
//			this.createOperator(newOp);

			this.revalidate();
			this.visualEditor.repaint();

			return newOp;
		}
		catch(final Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	@Override
	public void addOperator(final int x, final int y, final T newOp) {
		this.visualEditor.isInInsertMode = false; // leave insertMode

		// get some class names...
		final String newClassName = newOp.getClass().getSimpleName();
		final String newClassSuperName = newOp.getClass().getSuperclass().getSimpleName();

		this.validateAddOperator(x, y, newClassName);

		this.visualEditor.activateGraphMenus();

		this.createOperator(newOp);

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

	private void createOperator(final T newOp) {
		/* Document */
		// add Rule
		if ( newOp instanceof RuleOperator){
			final RuleOperator ro =  (RuleOperator) newOp;
			this.createNewRule(ro);
		}

		// add Prefix
		if ( newOp instanceof PrefixOperator ){
			final PrefixOperator po = (PrefixOperator) newOp;
			this.createNewPrefix(po);
		}

		// add Import
		if ( newOp instanceof ImportOperator ){
			final ImportOperator io = (ImportOperator) newOp;
			this.createNewImport(io);
		}

		/* Rule */
		if ( newOp instanceof UnitermOperator ){
			final UnitermOperator fo = (UnitermOperator) newOp;
			this.createNewUniterm(fo);
		}

		if ( newOp instanceof AbstractContainer ){
			final AbstractContainer oc = (AbstractContainer) newOp;
			this.createNewOperatorContainer(oc);
		}

		if ( newOp instanceof ListOperator ){
			final ListOperator lo = (ListOperator) newOp;
			this.createNewListOperator(lo);
		}

		if ( newOp instanceof FrameOperator ){
			final FrameOperator fo = (FrameOperator) newOp;
			this.createNewFrameOperator(fo);
		}

		if ( newOp instanceof ConstantOperator ){
			final ConstantOperator co = (ConstantOperator) newOp;
			this.createNewConstantOperator(co);
		}

		if ( newOp instanceof VariableOperator ){
			final VariableOperator vo = (VariableOperator) newOp;
			this.createNewVariableOperator(vo);
		}
	}

	/* ******************** **
	 * Canvas input methods **
	 * ******************** */
	protected abstract void createNewRule(RuleOperator ro);
	protected abstract void createNewPrefix(PrefixOperator po);
	protected abstract void createNewImport(ImportOperator io);
	protected abstract void createNewUniterm(UnitermOperator fo);
	protected abstract void createNewOperatorContainer(AbstractContainer oc);
	protected abstract void createNewListOperator(ListOperator lo);
	protected abstract void createNewFrameOperator(FrameOperator fo);
	protected abstract void createNewConstantOperator(ConstantOperator co);
	protected abstract void createNewVariableOperator(VariableOperator vo);

	@Override
	public String serializeSuperGraph() {
		final StringBuffer ret = new StringBuffer();
		for (int i = 0; i < super.rootList.size(); ++i) {
			if (i > 1) {
				ret.append("\n");
			}
			if( ! (super.rootList.get(i).getElement() instanceof ClassificationOperatorPanel) ){
				ret.append(super.rootList.get(i).serializeObjectAndTree());
			}
			else {
				System.out.println("ClassificationOperatorPanel"); // TODO
			}
		}
		return ret.toString();
	}

	public VisualRIFGraph<T> getVisualGraph(){
		return this;
	}
}