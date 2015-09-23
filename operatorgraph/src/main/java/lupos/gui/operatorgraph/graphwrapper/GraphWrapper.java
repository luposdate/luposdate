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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.DrawObject;
import lupos.gui.operatorgraph.DrawObject.InnerAttribute;
import lupos.gui.operatorgraph.DrawObject.OuterAttribute;
import lupos.gui.operatorgraph.DrawObject.Type;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.misc.Helper;

import xpref.datatypes.ColorDatatype;
import xpref.datatypes.EnumDatatype;
public abstract class GraphWrapper {
	/** Constant <code>styles</code> */
	protected static HashMap<String, DrawObject> styles = new HashMap<String, DrawObject>();
	protected Object element;

	/**
	 * <p>Constructor for GraphWrapper.</p>
	 *
	 * @param element a {@link java.lang.Object} object.
	 */
	public GraphWrapper(final Object element) {
		this.element = element;
	}

	/**
	 * <p>getGraphWrapperIDTuple.</p>
	 *
	 * @param op a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @return a {@link lupos.gui.operatorgraph.GraphWrapperIDTuple} object.
	 */
	public GraphWrapperIDTuple getGraphWrapperIDTuple(final GraphWrapper op) {
		for(final GraphWrapperIDTuple opid : this.getSucceedingElements()) {
			if(opid.getOperator().equals(op)) {
				return opid;
			}
		}

		return null;
	}

	/**
	 * <p>clearColorCache.</p>
	 */
	public static void clearColorCache() {
		GraphWrapper.styles.clear();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return System.identityHashCode(this.element);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object element_param) {
		if(element_param instanceof GraphWrapper) {
			return this.element == ((GraphWrapper) element_param).element;
		}

		return false;
	}
	
	/**
	 * <p>getOperatorStyle.</p>
	 *
	 * @param styleName a {@link java.lang.String} object.
	 * @return a {@link lupos.gui.operatorgraph.DrawObject} object.
	 */
	protected DrawObject getOperatorStyle(final String styleName) {
		try {
			if(!GraphWrapper.styles.containsKey(styleName)) {
				final Type shapeType = Helper.castEnum(EnumDatatype.getValues(styleName + ".shape").get(0));
				final Color color1 = ColorDatatype.getValues(styleName + ".color1").get(0);
				final Color color2 = ColorDatatype.getValues(styleName + ".color2").get(0);

				GraphWrapper.styles.put(styleName, new DrawObject(shapeType, InnerAttribute.GRADIENTPAINT, OuterAttribute.NONE, color1, color2));
			}
		}
		catch(final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return GraphWrapper.styles.get(styleName);
	}

	/**
	 * <p>Getter for the field <code>element</code>.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getElement() {
		return this.element;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.element.toString();
	}

	/**
	 * Checks if an object is instance of a List of Classes
	 *
	 * @param instance a {@link java.lang.Object} object.
	 * @param classes a {@link java.util.List} object.
	 * @return a boolean.
	 */
	@SuppressWarnings("rawtypes")
	protected boolean instanceOf(final Object instance, final List classes) {
		for(final Object clazz : classes) {
			if(((Class) clazz).isInstance(instance)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>getPrecedingElements.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public abstract LinkedList<GraphWrapper> getPrecedingElements();
	/**
	 * <p>getSucceedingElements.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public abstract LinkedList<GraphWrapperIDTuple> getSucceedingElements();

	/**
	 * <p>toString.</p>
	 *
	 * @param prefixInstance a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String toString(Prefix prefixInstance);

	/**
	 * <p>serializeObjectAndTree.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public abstract StringBuffer serializeObjectAndTree();

	/**
	 * <p>createObject.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.OperatorGraph} object.
	 * @return a {@link lupos.gui.operatorgraph.AbstractSuperGuiComponent} object.
	 */
	public abstract AbstractSuperGuiComponent createObject(OperatorGraph parent);

	/**
	 * <p>drawLineAnnotations.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.OperatorGraph} object.
	 * @return a {@link java.util.Hashtable} object.
	 */
	public abstract Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawLineAnnotations(OperatorGraph parent);
	/**
	 * <p>drawBackground.</p>
	 *
	 * @param g2d a {@link java.awt.Graphics2D} object.
	 * @param size a {@link java.awt.Dimension} object.
	 */
	public abstract void drawBackground(Graphics2D g2d, Dimension size);
	/**
	 * <p>drawAnnotationsBackground.</p>
	 *
	 * @param g2d a {@link java.awt.Graphics2D} object.
	 * @param size a {@link java.awt.Dimension} object.
	 */
	public abstract void drawAnnotationsBackground(Graphics2D g2d, Dimension size);

	/**
	 * <p>isContainer.</p>
	 *
	 * @return a boolean.
	 */
	public abstract boolean isContainer();
	/**
	 * <p>getContainerElements.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public abstract LinkedList<GraphWrapper> getContainerElements();

	/**
	 * <p>usePrefixesActive.</p>
	 *
	 * @return a boolean.
	 */
	public abstract boolean usePrefixesActive();

	/**
	 * <p>getWantedPreferencesID.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String getWantedPreferencesID();
}
