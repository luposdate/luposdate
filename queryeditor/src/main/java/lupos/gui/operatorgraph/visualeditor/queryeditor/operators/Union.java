
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
package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.MultiInputOperator;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.misc.util.OperatorIDTuple;
public class Union extends MultiInputOperator {
	/** {@inheritDoc} */
	public void addAvailableOperators(JPopupMenu popupMenu, final VisualGraph<Operator> parent, final GraphWrapper oldGW) {
		JMenuItem joinOpMI = new JMenuItem("change operator to JOIN");
		joinOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				replaceOperator(new Join(), parent, oldGW);
			}
		});

		popupMenu.add(joinOpMI);

		JMenuItem optionalOpMI = new JMenuItem("change operator to OPTIONAL");
		optionalOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				replaceOperator(new Optional(), parent, oldGW);
			}
		});

		popupMenu.add(optionalOpMI);
	}

	/**
	 * <p>serializeOperator.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public StringBuffer serializeOperator() {
		StringBuffer ret = new StringBuffer();

		ret.append("{\n");
		ret.append(this.succeedingOperators.get(0).getOperator().serializeOperator());
		ret.append("} UNION {\n");
		ret.append(this.succeedingOperators.get(1).getOperator().serializeOperator());
		ret.append("}\n");

		return ret;
	}

	/** {@inheritDoc} */
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = new StringBuffer();

		ret.append("{\n");
		ret.append(this.succeedingOperators.get(0).getOperator().serializeOperatorAndTree(visited));
		ret.append("}\n");

		for(int i = 1; i < this.succeedingOperators.size(); ++i) {
			ret.append("UNION {\n");
			ret.append(this.succeedingOperators.get(i).getOperator().serializeOperatorAndTree(visited));
			ret.append("}\n");
		}

		return ret;
	}

	/** {@inheritDoc} */
	public boolean validateOperator(boolean showErrors, HashSet<Operator> visited, Object data) {
		return super.validateOperator(showErrors, visited, data);
	}

	/**
	 * <p>canAddSucceedingOperator.</p>
	 *
	 * @return a boolean.
	 */
	public boolean canAddSucceedingOperator() {
		return true;
	}

	/**
	 * <p>getFreeOpID.</p>
	 *
	 * @return a int.
	 */
	public int getFreeOpID() {
		int prevID = -1;

		for(OperatorIDTuple<Operator> opIDt : this.succeedingOperators) {
			int id = opIDt.getId();

			if(prevID + 1 < id)
				return prevID + 1;

			prevID = id;
		}

		return prevID;
	}
}
