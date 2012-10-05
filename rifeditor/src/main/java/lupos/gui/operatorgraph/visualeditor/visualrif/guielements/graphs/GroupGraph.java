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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.VisualGraphOperator;

public class GroupGraph extends VisualGraphOperator{
	
	private static final long serialVersionUID = -2936295936044533187L;
	

	
	// Constructor
	public GroupGraph(VisualEditor<Operator> visualEditor) {
		super(visualEditor);

		this.SPACING_X = 190;
		this.SPACING_Y = 190;

		this.construct();
	}


	@Override
	protected Operator createOperator(Class<? extends Operator> clazz, Item content) throws Exception {
		
		Operator newOp = null;
	
		newOp = clazz.newInstance();

		return newOp;
	}


	protected void handleAddOperator(Operator arg0) {}


	public String serializeGraph() {
		final String graph = super.serializeSuperGraph();
		final StringBuffer ret = new StringBuffer();
		ret.append("Group( \n\n");

		ret.append("\t"+graph);
		

		ret.append("\n\n)");
		return ret.toString();
	}

	
	@Override
	protected boolean validateAddOperator(int arg0, int arg1, String arg2) {
		return true;
	}


	@Override
	protected void createNewRule(RuleOperator ro) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void createNewPrefix(PrefixOperator po) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void createNewImport(ImportOperator io) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void createNewUniterm(UnitermOperator fo) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void createNewOperatorContainer(AbstractContainer oc) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void createNewListOperator(ListOperator lo) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void createNewFrameOperator(FrameOperator fo) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void createNewConstantOperator(ConstantOperator co) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void createNewVariableOperator(VariableOperator vo) {
		// TODO Auto-generated method stub
		
	}


















	

}
