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
import lupos.misc.util.OperatorIDTuple;
import lupos.misc.debug.BasicOperatorByteArray;
import xpref.datatypes.BooleanDatatype;

public class GraphWrapperBasicOperatorByteArray extends GraphWrapper {

	private final static String[] MAPPING_OPERATORTYPE_TO_PREFERENCES = {
			"join", "optional", "union", "basicindex", "indexcollection",
			"sort", "result", "filter", "projection", "limit", "offset",
			"basicoperator" };

	public GraphWrapperBasicOperatorByteArray(
			final BasicOperatorByteArray element) {
		super(element);
	}

	@Override
	public AbstractSuperGuiComponent createObject(final OperatorGraph parent) {
		return new ElementPanel(parent, this);
	}

	@Override
	public void drawAnnotationsBackground(final Graphics2D g2d,
			final Dimension size) {
		try {
			if (!BooleanDatatype.getValues("operatorGraph_useStyledBoxes").get(
					0).booleanValue()) {
				DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width - 1,
						size.height - 1, Color.WHITE, Color.BLACK);
			} else {
				final DrawObject drawObject = this
						.getOperatorStyle("operatorGraph_style_annotations");
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
			if (!BooleanDatatype.getValues("operatorGraph_useStyledBoxes").get(
					0).booleanValue()) {
				DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width - 1,
						size.height - 1, Color.WHITE, Color.BLACK);
			} else {
				DrawObject drawObject = null;

				final BasicOperatorByteArray operator = (BasicOperatorByteArray) this.element;

				final byte type = operator.getTypeASByte();

				drawObject = this.getOperatorStyle("operatorGraph_style_"
						+ MAPPING_OPERATORTYPE_TO_PREFERENCES[type]);

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
				final GraphWrapperBasicOperatorByteArray gw = new GraphWrapperBasicOperatorByteArray(
						op);

				final AbstractSuperGuiComponent annotation = new AnnotationPanel(
						parent, gw, Integer.toString(opIDt.getId()));

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
		for (final BasicOperatorByteArray bo : ((BasicOperatorByteArray) this.element)
				.getPrecedingOperators()) {
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
		for (final OperatorIDTuple<BasicOperatorByteArray> oit : ((BasicOperatorByteArray) this.element)
				.getSucceedingOperators()) {
			// put BasicOperator in GraphWrapper class...
			final GraphWrapperBasicOperatorByteArray element = new GraphWrapperBasicOperatorByteArray(
					oit.getOperator());

			// add GraphWrapperIDTuple with current BasicOperator to list of
			// succeeding elements...
			succedingElements
					.add(new GraphWrapperIDTuple(element, oit.getId()));
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