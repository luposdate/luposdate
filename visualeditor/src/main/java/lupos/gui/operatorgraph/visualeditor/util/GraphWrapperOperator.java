/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.gui.operatorgraph.visualeditor.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.DrawObject;
import lupos.gui.operatorgraph.DrawObject.InnerAttribute;
import lupos.gui.operatorgraph.DrawObject.OuterAttribute;
import lupos.gui.operatorgraph.DrawObject.Type;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;
import lupos.misc.util.OperatorIDTuple;
import xpref.datatypes.BooleanDatatype;

public class GraphWrapperOperator extends GraphWrapperEditable {
	public GraphWrapperOperator(final Operator element) {
		super(element);

		// TODO: test this later...
		// this.prefix.addOperator(element);
	}

	/**
	 * Returns a list of GraphWrapper elements which are the preceding elements
	 * of the current element.
	 * 
	 * @return List of preceding elements
	 */
	@Override
	public LinkedList<GraphWrapper> getPrecedingElements() {
		// get preceding Operators...
		final LinkedList<Operator> precedingOperators = ((Operator) this.element).getPrecedingOperators();

		// create list of GraphWrapper elements for preceding elements...
		final LinkedList<GraphWrapper> precedingElements = new LinkedList<GraphWrapper>();

		// walk through preceding BasicOperators...
		// put current BasicOperator in GraphWrapper class
		// and add it to list of preceding elements...
		for(final Operator op : precedingOperators) {
			precedingElements.add(new GraphWrapperOperator(op));
		}

		return precedingElements; // return preceding elements
	}

	@Override
	public Operator getElement() {
		return (Operator) this.element;
	}

	/**
	 * Returns a list of GraphWrapperIdTuple elements which are the succeeding
	 * elements of the current element.
	 * 
	 * @return List of succeeding elements
	 */
	@Override
	public LinkedList<GraphWrapperIDTuple> getSucceedingElements() {
		// get preceding Operators...
		LinkedList<OperatorIDTuple<Operator>> succedingOperators = new LinkedList<OperatorIDTuple<Operator>>();
		succedingOperators = ((Operator) this.element).getSucceedingOperators();

		// create list of GraphWrapperIDTuples for succeeding elements...
		final LinkedList<GraphWrapperIDTuple> succedingElements = new LinkedList<GraphWrapperIDTuple>();

		// walk through succeeding BasicOperators...
		for(final OperatorIDTuple<Operator> oit : succedingOperators) {
			// put BasicOperator in GraphWrapper class...
			final GraphWrapper element = new GraphWrapperOperator(oit.getOperator());

			// add GraphWrapperIDTuple with current BasicOperator to list of
			// succeeding elements...
			succedingElements.add(new GraphWrapperIDTuple(element, oit.getId()));
		}

		return succedingElements; // return succeeding elements
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		return this.element.toString();
	}

	@Override
	public boolean isContainer() {
		if(this.element instanceof OperatorContainer) {
			return true;
		}

		return false;
	}

	@Override
	public LinkedList<GraphWrapper> getContainerElements() {
		final LinkedList<GraphWrapper> gwList = new LinkedList<GraphWrapper>();

		for(final Operator op : ((OperatorContainer) this.element).getOperators()) {
			gwList.add(new GraphWrapperOperator(op));
		}

		return gwList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public AbstractGuiComponent<Operator> createObject(final OperatorGraph parent) {
		return ((Operator) this.element).draw(this, (VisualGraph<Operator>) parent);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawLineAnnotations(final OperatorGraph parent) {
		return ((Operator) this.element).drawAnnotations((VisualGraph<Operator>) parent);
	}

	@Override
	public StringBuffer serializeObjectAndTree() {
		return ((Operator) this.element).serializeOperatorAndTree(new HashSet<Operator>());
	}

	@Override
	public boolean validateObject(final boolean showErrors, final Object data) {
		return ((Operator) this.element).validateOperator(showErrors, new HashSet<Operator>(), data);
	}

	@Override
	public boolean variableInUse(final String variable) {
		return ((Operator) this.element).variableInUse(variable, new HashSet<Operator>());
	}

	@Override
	public void drawBackground(final Graphics2D g2d, final Dimension size) {
		try {
			String className = ((Operator) this.element).getGUIComponent().getParentQG().visualEditor.getXPrefPrefix();
			className = className.substring(0, 1).toLowerCase() + className.substring(1);

			if(!BooleanDatatype.getValues(className + "_useStyledBoxes").get(0).booleanValue()) {
				DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width - 1, size.height - 1, Color.WHITE, Color.BLACK);
			}
			else {
				DrawObject drawObject = null;

				if(this.element instanceof OperatorContainer) {
					drawObject = new DrawObject(Type.SIMPLEBOX, InnerAttribute.GRADIENTPAINT, OuterAttribute.NONE, Color.WHITE, Color.WHITE);
				} else {
					drawObject = this.getOperatorStyle(((Operator)this.element).getXPrefID());
				}

				if(drawObject != null) {
					drawObject.draw(g2d, 0, 0, size.width, size.height);
				}
				else {
					DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width, size.height, Color.WHITE, Color.BLACK);
				}
			}
		}
		catch(final Exception e) {
			System.err.println(e);

			e.printStackTrace();
		}
	}

	@Override
	public void drawAnnotationsBackground(final Graphics2D g2d, final Dimension size) {
		try {
			AbstractGuiComponent<Operator> component = ((Operator) this.element).getGUIComponent();
			boolean already=false;
			if(component!=null){
				String className = component.getParentQG().visualEditor.getXPrefPrefix();
				className = className.substring(0, 1).toLowerCase() + className.substring(1);

				if(!BooleanDatatype.getValues(className + "_useStyledBoxes").get(0).booleanValue()) {
					DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width - 1, size.height - 1, Color.WHITE, Color.BLACK);
					already = true;
				}
			}
			if(!already){
				DrawObject drawObject = null;
				
				drawObject = this.getOperatorStyle(((Operator) this.element).getXPrefIDForAnnotation());

				if(drawObject != null) {
					drawObject.draw(g2d, 0, 0, size.width, size.height);
				}
				else {
					DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width, size.height, Color.WHITE, Color.BLACK);
				}
			}
		}
		catch(final Exception e) {
			System.err.println(e);

			e.printStackTrace();
		}
	}

	@Override
	public int hashCode() {
		final Operator elem = (Operator) this.element;

		return System.identityHashCode(elem);
	}

	@Override
	public boolean equals(final Object element) {
		if(element instanceof GraphWrapperOperator) {
			return (Operator) this.element == (Operator) ((GraphWrapperOperator) element).element;
		}

		return false;
	}

	@Override
	public void addSucceedingElement(final GraphWrapperIDTuple gwidt) {
		((Operator) this.element).addSucceedingOperator(new OperatorIDTuple<Operator>((Operator) gwidt.getOperator().getElement(), gwidt.getId()));
	}

	@Override
	public AbstractGuiComponent<Operator> getGUIComponent() {
		return ((Operator) this.element).getGUIComponent();
	}

	@Override
	public boolean canAddSucceedingElement() {
		return ((Operator) this.element).canAddSucceedingOperator();
	}

	@Override
	public void addPrecedingElement(final GraphWrapper gw) {
		((Operator) this.element).addPrecedingOperator((Operator) gw.getElement());
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(final VisualGraph parent) {
		return ((Operator) this.element).drawAnnotations(parent);
	}

	@Override
	public void removeSucceedingElement(final GraphWrapper gw) {
		((Operator) this.element).removeSucceedingOperator((Operator) gw.getElement());
	}

	@Override
	public void deleteAnnotation(final GraphWrapper gw) {
		((Operator) this.element).deleteAnnotation((Operator) gw.getElement());
	}

	@Override
	public void delete(final boolean subtree) {
		((Operator) this.element).delete(subtree);
	}

	@Override
	public StringBuffer serializeOperator() {
		return ((Operator) this.element).serializeOperator();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public AbstractGuiComponent getAnnotationLabel(final GraphWrapper gw) {
		return ((Operator) this.element).getAnnotationLabel((Operator) gw.getElement());
	}
}