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
package lupos.gui.operatorgraph.visualeditor.queryeditor.util;

import java.util.HashSet;

import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Construct;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.ConstructTemplateContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveData;
import lupos.gui.operatorgraph.visualeditor.util.Connection;

public class QueryConnection extends Connection<Operator> {
	public QueryConnection(VisualEditor<Operator> visualEditor) {
		super(visualEditor);
	}

	protected String validateConnection() {
		String errorString = "";

		// we can't connect two operators on different layers...
		if(((Operator) this.firstOp.getElement()).getParentContainer() != ((Operator) this.secondOp.getElement()).getParentContainer()) {
			errorString = "You can only connect two operators in the same layer!";
		}

		if(this.secondOp.getElement() instanceof ConstructTemplateContainer && !(this.firstOp.getElement() instanceof Construct)) {
			errorString = "A ConstructTemplateContainer can only be a child of a CONSTRUCT operator!";
		}

		if(this.secondOp.getElement() instanceof RetrieveData) {
			errorString = "RetrieveData operators (ASK, CONSTRUCT, DESCRIBE, SELECT) can't have preceeding elements!";
		}

		if(((Operator) this.firstOp.getElement()).getParentContainer() == null) {
			this.firstOp.addSucceedingElement(new GraphWrapperIDTuple(this.secondOp, 0));

			if(this.hasCircle(this.firstOp, new HashSet<GraphWrapper>())) {
				errorString = "Circles are not allowed outside a Container!";
			}

			this.firstOp.removeSucceedingElement(this.secondOp);
		}

		return errorString;
	}

	private boolean hasCircle(GraphWrapper operator, HashSet<GraphWrapper> visited) {
		if(visited.contains(operator)) {
			return true;
		}

		visited.add(operator);

		for(GraphWrapperIDTuple opidt : operator.getSucceedingElements()) {
			if(this.hasCircle(opidt.getOperator(), visited) == true) {
				return true;
			}
		}

		return false;
	}
}