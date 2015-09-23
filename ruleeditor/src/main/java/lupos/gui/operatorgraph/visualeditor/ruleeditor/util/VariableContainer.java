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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.util;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator;
public class VariableContainer {
	private int dimension = 0;
	private Class<?> classType = null;
	private String opName = "";
	private AbstractRuleOperator countProvider = null;

	/**
	 * <p>Constructor for VariableContainer.</p>
	 *
	 * @param opName a {@link java.lang.String} object.
	 * @param classType a {@link java.lang.Class} object.
	 * @param dimension a int.
	 */
	public VariableContainer(String opName, Class<?> classType, int dimension) {
		this.opName = opName;
		this.classType = classType;
		this.dimension = dimension;
	}

	/**
	 * <p>Setter for the field <code>dimension</code>.</p>
	 *
	 * @param dimension a int.
	 */
	public void setDimension(int dimension) {
		this.dimension = Math.max(this.dimension, dimension);
	}

	/**
	 * <p>Setter for the field <code>countProvider</code>.</p>
	 *
	 * @param countProvider a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator} object.
	 */
	public void setCountProvider(AbstractRuleOperator countProvider) {
		this.countProvider = countProvider;
	}

	/**
	 * <p>Getter for the field <code>countProvider</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator} object.
	 */
	public AbstractRuleOperator getCountProvider() {
		return this.countProvider;
	}

	/**
	 * <p>Getter for the field <code>opName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getOpName() {
		return this.opName;
	}

	/**
	 * <p>declate_variable.</p>
	 *
	 * @param spaces a {@link java.lang.StringBuffer} object.
	 * @param global a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public String declate_variable(StringBuffer spaces, boolean global) {
		StringBuffer buffer = new StringBuffer(spaces);

		if(global) {
			buffer.append("private ");
		}

		buffer.append(this.classType.getName());

		//		if(this.dimension > 0) {
		for(int i = 0; i < this.dimension; i += 1) {
			buffer.append("[]");
		}
		//		}

		buffer.append(" " + this.opName + " = null;\n");

		return buffer.toString();
	}

	/**
	 * <p>initiate_next_dimension.</p>
	 *
	 * @param spaces a {@link java.lang.StringBuffer} object.
	 * @param dimension a int.
	 * @param countString a {@link java.lang.String} object.
	 * @param global a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public String initiate_next_dimension(StringBuffer spaces, int dimension, String countString, boolean global) {
		if(this.dimension == 0) {
			return "";
		}

		StringBuffer buffer = new StringBuffer(spaces);

		if(global) {
			buffer.append("this.");
		}

		buffer.append(this.opName);

		for(int i = 1; i <= dimension; i += 1) {
			buffer.append("[this._dim_" + i + "]");
		}

		buffer.append(" = new " + this.classType.getName() + "[" + countString + "]");

		for(int i = dimension+1; i < this.dimension; i += 1) {
			buffer.append("[]");
		}

		buffer.append(";\n");

		return buffer.toString();
	}

	/**
	 * <p>assign_variable.</p>
	 *
	 * @param nodeName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String assign_variable(String nodeName) {
		StringBuffer buffer = new StringBuffer("this." + this.opName);

		for(int i = 0; i < this.dimension; i += 1) {
			buffer.append("[this._dim_" + i + "]");
		}

		buffer.append(" = (" + this.classType.getName() + ") " + nodeName + ";\n");

		return buffer.toString();
	}

	/**
	 * <p>Getter for the field <code>dimension</code>.</p>
	 *
	 * @return a int.
	 */
	public int getDimension() {
		return this.dimension;
	}
}
