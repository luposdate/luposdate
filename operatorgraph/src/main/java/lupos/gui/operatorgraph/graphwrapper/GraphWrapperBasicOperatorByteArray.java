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
import java.util.Hashtable;
import java.util.LinkedList;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.DrawObject;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.viewer.AnnotationPanel;
import lupos.gui.operatorgraph.viewer.ElementPanel;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.misc.util.OperatorIDTuple;
import xpref.datatypes.BooleanDatatype;

public class GraphWrapperBasicOperatorByteArray extends GraphWrapper {

	private final static String[] MAPPING_OPERATORTYPE_TO_PREFERENCES = {
			"join", "optional", "union", "basicindexscan", "root",
			"sort", "result", "filter", "projection", "limit", "offset",
			"basicoperator" };

	public GraphWrapperBasicOperatorByteArray(final BasicOperatorByteArray element) {
		super(element);
	}

	@Override
	public AbstractSuperGuiComponent createObject(final OperatorGraph parent) {
		return new ElementPanel(parent, this);
	}

	@Override
	public void drawAnnotationsBackground(final Graphics2D g2d, final Dimension size) {
		try {
			if (!BooleanDatatype.getValues("operatorGraph_useStyledBoxes").get(0).booleanValue()) {
				DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width - 1, size.height - 1, Color.WHITE, Color.BLACK);
			} else {
				final DrawObject drawObject = this.getOperatorStyle("operatorGraph_style_annotations");
				drawObject.draw(g2d, 0, 0, size.width, size.height);
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	@Override
	public void drawBackground(final Graphics2D g2d, final Dimension size) {
		try {
			if (!BooleanDatatype.getValues("operatorGraph_useStyledBoxes").get(0).booleanValue()) {
				DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width - 1, size.height - 1, Color.WHITE, Color.BLACK);
			} else {
				DrawObject drawObject = null;

				final BasicOperatorByteArray operator = (BasicOperatorByteArray) this.element;

				final byte type = operator.getTypeASByte();

				drawObject = this.getOperatorStyle("operatorGraph_style_" + MAPPING_OPERATORTYPE_TO_PREFERENCES[type]);

				if (drawObject != null) {
					drawObject.draw(g2d, 0, 0, size.width, size.height);
				} else {
					DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width,
							size.height, Color.WHITE, Color.BLACK);
				}
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	@Override
	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawLineAnnotations(
			final OperatorGraph parent) {
		final Hashtable<GraphWrapper, AbstractSuperGuiComponent> annotations = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();

		for (final OperatorIDTuple<BasicOperatorByteArray> opIDt : ((BasicOperatorByteArray) this.element)
				.getSucceedingOperators()) {
			final BasicOperatorByteArray op = opIDt.getOperator();

			if (op.isMultiInputOperator()) {
				final GraphWrapperBasicOperatorByteArray gw = new GraphWrapperBasicOperatorByteArray(op);

				final AbstractSuperGuiComponent annotation = new AnnotationPanel(parent, gw, Integer.toString(opIDt.getId()));

				annotations.put(gw, annotation);
			}
		}

		return annotations;
	}

	@Override
	public LinkedList<GraphWrapper> getContainerElements() {
		return new LinkedList<GraphWrapper>();
	}

	/**
	 * Returns a list of GraphWrapper elements which are the preceding elements
	 * of the current element.
	 *
	 * @return List of preceding elements
	 */
	@Override
	public LinkedList<GraphWrapper> getPrecedingElements() {
		// create list of GraphWrapper elements for preceding elements...
		final LinkedList<GraphWrapper> precedingElements = new LinkedList<GraphWrapper>();

		// walk through preceding BasicOperators...
		// put current BasicOperator in GraphWrapper class
		// and add it to list of preceding elements...
		for (final BasicOperatorByteArray bo : ((BasicOperatorByteArray) this.element).getPrecedingOperators()) {
			precedingElements.add(new GraphWrapperBasicOperatorByteArray(bo));
		}

		return precedingElements; // return preceding elements
	}

	/**
	 * Returns a list of GraphWrapperIdTuple elements which are the succeeding
	 * elements of the current element.
	 *
	 * @return List of succeeding elements
	 */
	@Override
	public LinkedList<GraphWrapperIDTuple> getSucceedingElements() {
		// create list of GraphWrapperIDTuples for succeeding elements...
		final LinkedList<GraphWrapperIDTuple> succedingElements = new LinkedList<GraphWrapperIDTuple>();

		// walk through succeeding BasicOperators...
		for (final OperatorIDTuple<BasicOperatorByteArray> oit : ((BasicOperatorByteArray) this.element).getSucceedingOperators()) {
			// put BasicOperator in GraphWrapper class...
			final GraphWrapperBasicOperatorByteArray element = new GraphWrapperBasicOperatorByteArray(
					oit.getOperator());

			// add GraphWrapperIDTuple with current BasicOperator to list of
			// succeeding elements...
			succedingElements.add(new GraphWrapperIDTuple(element, oit.getId()));
		}

		return succedingElements; // return succeeding elements
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public StringBuffer serializeObjectAndTree() {
		return new StringBuffer();
	}

	@Override
	public String toString(final Prefix prefixInstance) {
		return ((BasicOperatorByteArray) this.element).toString(prefixInstance);
	}

	@Override
	public String toString() {
		return this.element.toString();
	}

	@Override
	public boolean usePrefixesActive() {
		return true;
	}

	@Override
	public BasicOperatorByteArray getElement() {
		return (BasicOperatorByteArray) this.element;
	}

	@Override
	public String getWantedPreferencesID() {
		return "operatorGraph_useStyledBoxes";
	}

	@Override
	public int hashCode() {
		return ((BasicOperatorByteArray) this.element).hashCode();
	}

	@Override
	public boolean equals(final Object element) {
		if (element instanceof GraphWrapper) {
			return this.element.equals(((GraphWrapper) element).element);
		}
		return false;
	}
}