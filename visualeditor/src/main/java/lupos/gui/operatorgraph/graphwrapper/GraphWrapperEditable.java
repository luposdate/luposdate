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
package lupos.gui.operatorgraph.graphwrapper;

import java.util.Hashtable;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
public abstract class GraphWrapperEditable extends GraphWrapper {
	/**
	 * <p>Constructor for GraphWrapperEditable.</p>
	 *
	 * @param element a {@link java.lang.Object} object.
	 */
	public GraphWrapperEditable(Object element) {
		super(element);
	}

	/**
	 * <p>usePrefixesActive.</p>
	 *
	 * @return a boolean.
	 */
	public boolean usePrefixesActive() {
		return true;
	}

	/**
	 * <p>getWantedPreferencesID.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getWantedPreferencesID() {
		return "";
	}

	/**
	 * <p>validateObject.</p>
	 *
	 * @param showErrors a boolean.
	 * @param data a {@link java.lang.Object} object.
	 * @return a boolean.
	 */
	public abstract boolean validateObject(boolean showErrors, Object data);
	/**
	 * <p>variableInUse.</p>
	 *
	 * @param string a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public abstract boolean variableInUse(String string);

	/**
	 * <p>addSucceedingElement.</p>
	 *
	 * @param gwidt a {@link lupos.gui.operatorgraph.GraphWrapperIDTuple} object.
	 */
	public abstract void addSucceedingElement(GraphWrapperIDTuple gwidt);
	/**
	 * <p>addPrecedingElement.</p>
	 *
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 */
	public abstract void addPrecedingElement(GraphWrapper gw);
	/**
	 * <p>getGUIComponent.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent} object.
	 */
	@SuppressWarnings("rawtypes")
	public abstract AbstractGuiComponent getGUIComponent();
	/**
	 * <p>canAddSucceedingElement.</p>
	 *
	 * @return a boolean.
	 */
	public abstract boolean canAddSucceedingElement();
	/**
	 * <p>drawAnnotations.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 * @return a {@link java.util.Hashtable} object.
	 */
	@SuppressWarnings("rawtypes")
	public abstract Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(VisualGraph parent);
	/**
	 * <p>removeSucceedingElement.</p>
	 *
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 */
	public abstract void removeSucceedingElement(GraphWrapper gw);
	/**
	 * <p>deleteAnnotation.</p>
	 *
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 */
	public abstract void deleteAnnotation(GraphWrapper gw);
	/**
	 * <p>delete.</p>
	 *
	 * @param subtree a boolean.
	 */
	public abstract void delete(boolean subtree);
	/**
	 * <p>serializeOperator.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public abstract StringBuffer serializeOperator();
	/**
	 * <p>getAnnotationLabel.</p>
	 *
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent} object.
	 */
	@SuppressWarnings("rawtypes")
	public abstract AbstractGuiComponent getAnnotationLabel(GraphWrapper gw);
}
