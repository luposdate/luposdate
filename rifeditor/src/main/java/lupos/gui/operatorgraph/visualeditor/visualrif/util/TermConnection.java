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



import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;

public class TermConnection extends ConnectionRIF<Operator> {
	
	Term term;

	public TermConnection(RuleGraph ruleGraph, FrameOperator frameOperator, Term term) {
		super(ruleGraph.getVisualEditor());
		this.addOperator(frameOperator);
		
		this.term = term;

		term.setDummyOperator(this.dummyOperator);
	}
	

	public TermConnection(RuleGraph ruleGraph, AbstractTermOperator abstractTermOperator, Term term) {
		super(ruleGraph.getVisualEditor());
		this.addOperator(abstractTermOperator);
		
		this.term = term;

		term.setDummyOperator(this.dummyOperator);
	}
	

	@Override
	protected String validateConnection() {
		String errorString = "";
		
//		if ( !(( this.secondOp.getElement() instanceof UnitermOperator) || (this.secondOp.getElement() instanceof ListOperator)) ){
//			return "Please link this operator with an uniterm operator or an list operator";
//		}
//			
//			if (term.isUniterm() &&  !( this.secondOp.getElement() instanceof UnitermOperator) )
//			return "Please link this operator with a uniterm operator";
//			
//			if (term.isList() &&  !( this.secondOp.getElement() instanceof ListOperator) )
//				return "Please link this operator with a list operator";
//		
//		
//	
//		
		
		
		return errorString;
	}

	/**
	 * This method checks whether the requested connection is valid or not and
	 * creates it, if it is.
	 */
	protected boolean createConnection() {
		
		if(super.createConnection()){
		
			term.setSucceedingOperator(this.secondOp);
			if (this.secondOp.getElement() instanceof AbstractTermOperator)
			term.setAbstractTermOperator( (AbstractTermOperator) this.secondOp.getElement());
			// get the GraphBox of the GraphWrapper of the first operator...
			final GraphBox firstBox = this.queryGraph.getBoxes().get(this.firstOp);
			// draw all annotations of the first Operator...
			firstBox.setLineAnnotations(this.firstOp.drawAnnotations(this.queryGraph));

			this.firstOp.getGUIComponent().repaint();

			this.queryGraph.revalidate();
			this.queryGraph.repaint();
			this.visualEditor.repaint();
			return true;
		} else {
			return false;
		}
	
	}
}
