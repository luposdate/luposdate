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
	protected static HashMap<String, DrawObject> styles = new HashMap<String, DrawObject>();
	protected Object element;

	public GraphWrapper(final Object element) {
		this.element = element;
	}

	public GraphWrapperIDTuple getGraphWrapperIDTuple(final GraphWrapper op) {
		for(final GraphWrapperIDTuple opid : this.getSucceedingElements()) {
			if(opid.getOperator().equals(op)) {
				return opid;
			}
		}

		return null;
	}

	public static void clearColorCache() {
		GraphWrapper.styles.clear();
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this.element);
	}

	@Override
	public boolean equals(final Object element_param) {
		if(element_param instanceof GraphWrapper) {
			return this.element == ((GraphWrapper) element_param).element;
		}

		return false;
	}
	
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

	public Object getElement() {
		return this.element;
	}

	@Override
	public String toString() {
		return this.element.toString();
	}

	/**
	 * Checks if an object is instance of a List of Classes
	 * 
	 * @param instance
	 * @param classes
	 * @return
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

	public abstract LinkedList<GraphWrapper> getPrecedingElements();
	public abstract LinkedList<GraphWrapperIDTuple> getSucceedingElements();

	public abstract String toString(Prefix prefixInstance);

	public abstract StringBuffer serializeObjectAndTree();

	public abstract AbstractSuperGuiComponent createObject(OperatorGraph parent);

	public abstract Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawLineAnnotations(OperatorGraph parent);
	public abstract void drawBackground(Graphics2D g2d, Dimension size);
	public abstract void drawAnnotationsBackground(Graphics2D g2d, Dimension size);

	public abstract boolean isContainer();
	public abstract LinkedList<GraphWrapper> getContainerElements();

	public abstract boolean usePrefixesActive();

	public abstract String getWantedPreferencesID();
}