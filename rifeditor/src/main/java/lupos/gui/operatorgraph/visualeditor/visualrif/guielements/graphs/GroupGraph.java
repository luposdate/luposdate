
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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.VisualGraphOperator;
public class GroupGraph extends VisualGraphOperator{

	private static final long serialVersionUID = -2936295936044533187L;

	// Constructor
	/**
	 * <p>Constructor for GroupGraph.</p>
	 *
	 * @param visualEditor a {@link lupos.gui.operatorgraph.visualeditor.VisualEditor} object.
	 */
	public GroupGraph(final VisualEditor<Operator> visualEditor) {
		super(visualEditor);

		this.SPACING_X = 190;
		this.SPACING_Y = 190;

		this.construct();
	}

	/** {@inheritDoc} */
	@Override
	protected Operator createOperator(final Class<? extends Operator> clazz, final Item content) throws Exception {

		Operator newOp = null;

		newOp = clazz.newInstance();

		return newOp;
	}

	/** {@inheritDoc} */
	@Override
	protected void handleAddOperator(final Operator arg0) {}

	/** {@inheritDoc} */
	@Override
	public String serializeGraph() {
		final String graph = super.serializeSuperGraph();
		final StringBuffer ret = new StringBuffer();
		ret.append("Group( \n\n");

		ret.append("\t"+graph);


		ret.append("\n\n)");
		return ret.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean validateAddOperator(final int arg0, final int arg1, final String arg2) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected void createNewRule(final RuleOperator ro) {
	}

	/** {@inheritDoc} */
	@Override
	protected void createNewPrefix(final PrefixOperator po) {
	}

	/** {@inheritDoc} */
	@Override
	protected void createNewImport(final ImportOperator io) {
	}

	/** {@inheritDoc} */
	@Override
	protected void createNewUniterm(final UnitermOperator fo) {
	}

	/** {@inheritDoc} */
	@Override
	protected void createNewOperatorContainer(final AbstractContainer oc) {
	}

	/** {@inheritDoc} */
	@Override
	protected void createNewListOperator(final ListOperator lo) {
	}

	/** {@inheritDoc} */
	@Override
	protected void createNewFrameOperator(final FrameOperator fo) {
	}

	/** {@inheritDoc} */
	@Override
	protected void createNewConstantOperator(final ConstantOperator co) {
	}

	/** {@inheritDoc} */
	@Override
	protected void createNewVariableOperator(final VariableOperator vo) {
	}
}
