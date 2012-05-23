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
package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.RetrieveDataPanel;
import lupos.misc.util.OperatorIDTuple;

public class Construct extends RetrieveDataWithSolutionModifier {
	public Construct(Prefix prefix) {
		super(prefix);
	}

	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new RetrieveDataPanel(gw, this, parent, "Construct");
		((RetrieveDataPanel) this.panel).addDatasetClause();
		((RetrieveDataPanel) this.panel).addSolutionModifier();
		((RetrieveDataPanel) this.panel).finalize();

		return this.panel;
	}

	public StringBuffer serializeOperator() {
		StringBuffer ret = super.serializeOperator();
		ret.append(this.serializeDatasetClause());
		ret.append(this.serializeSolutionModifier());

		return ret;
	}

	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = super.serializeOperator();
		ret.append(this.serializeDatasetClauseAndWhereClause(visited));
		ret.append(this.serializeSolutionModifier());

		return ret;
	}

	public StringBuffer serializeDatasetClauseAndWhereClause(HashSet<Operator> visited) {
		StringBuffer ret = this.serializeDatasetClause();

		ret.append("{\n");

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			Operator op = opIDT.getOperator();

			if(op instanceof ConstructTemplateContainer) {
				ret.append(op.serializeOperatorAndTree(visited));
			}
		}

		ret.append("}\n");

		// WHERE
		ret.append("WHERE {\n");

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			Operator op = opIDT.getOperator();

			if(!(op instanceof ConstructTemplateContainer)) {
				ret.append(opIDT.getOperator().serializeOperatorAndTree(visited));
			}
		}

		ret.append("}\n");

		return ret;
	}

	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		if(visited.contains(this))
			return false;

		visited.add(this);


		if(super.variableInUse(variable, visited))
			return true;

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getOperator().variableInUse(variable, visited))
				return true;

		return false;
	}

	public boolean canAddSucceedingOperator() {
		return (this.succeedingOperators.size() < 2);
	}
}