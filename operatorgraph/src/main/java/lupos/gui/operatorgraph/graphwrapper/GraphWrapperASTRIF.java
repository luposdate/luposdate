package lupos.gui.operatorgraph.graphwrapper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.DrawObject;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.viewer.ElementPanel;
import lupos.rif.generated.syntaxtree.INode;
import lupos.rif.generated.syntaxtree.NodeChoice;
import lupos.rif.generated.syntaxtree.NodeList;
import lupos.rif.generated.syntaxtree.NodeListOptional;
import lupos.rif.generated.syntaxtree.NodeOptional;
import lupos.rif.generated.syntaxtree.NodeSequence;
import lupos.rif.generated.syntaxtree.NodeToken;
import lupos.rif.visitor.ChildrenSyntaxTreeVisitor;
import xpref.datatypes.BooleanDatatype;

/**
 * Graphwrapper for RIF Abstract Syntaxtree in lupos.rif.generated.syntaxtree
 */
public class GraphWrapperASTRIF extends GraphWrapper {
	public GraphWrapperASTRIF(final INode element) {
		super(element);
	}

	@Override
	public AbstractSuperGuiComponent createObject(final OperatorGraph parent) {
		return new ElementPanel(parent, this);
	}

	@Override
	public void drawAnnotationsBackground(final Graphics2D g2d,
			final Dimension size) {
	}

	@Override
	public void drawBackground(final Graphics2D g2d, final Dimension size) {
		try {
			if (!BooleanDatatype.getValues("ast_useStyledBoxes").get(0)
					.booleanValue()) {
				DrawObject.drawSimpleBoxOuterLines(g2d, 0, 0, size.width - 1,
						size.height - 1, Color.WHITE, Color.BLACK);
			} else {
				DrawObject drawObject = null;

				if (instanceOf(this.element, Arrays.asList(NodeToken.class))) {
					drawObject = this
							.getOperatorStyle("ast_style_terminalnode");
				} else
					drawObject = this
							.getOperatorStyle("ast_style_nonterminalnode");

				if (drawObject != null) {
					drawObject.draw(g2d, 0, 0, size.width, size.height);
				} else {
					DrawObject.drawGradientPaintRoundBox(g2d, 0, 0, size.width,
							size.height, Color.LIGHT_GRAY, Color.WHITE);
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
		return new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();
	}

	@Override
	public LinkedList<GraphWrapper> getContainerElements() {
		return new LinkedList<GraphWrapper>();
	}

	@Override
	public LinkedList<GraphWrapper> getPrecedingElements() {
		final LinkedList<GraphWrapper> precedingElements = new LinkedList<GraphWrapper>();

		if (((INode) element).getParent() != null) {
			INode parent = ((INode) element).getParent();
			while (instanceOf(parent, Arrays.asList(NodeList.class,
					NodeListOptional.class, NodeSequence.class,
					NodeOptional.class, NodeChoice.class)))
				parent = parent.getParent();

			precedingElements.add(new GraphWrapperASTRIF(parent));
		}

		return precedingElements;
	}

	@Override
	public LinkedList<GraphWrapperIDTuple> getSucceedingElements() {
		final List<INode> children = ((INode) element).accept(
				new ChildrenSyntaxTreeVisitor(), null);
		final LinkedList<GraphWrapperIDTuple> succedingElements = new LinkedList<GraphWrapperIDTuple>();

		if (children != null) {
			for (int i = 0; i < children.size(); ++i) {
				if (children.get(i) != null)
					succedingElements.add(new GraphWrapperIDTuple(
							new GraphWrapperASTRIF(children.get(i)), i));
			}
		}

		return succedingElements;
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
		return element instanceof NodeToken ? element.toString() : element
				.getClass().getSimpleName().replaceAll("RIF", "");
	}

	@Override
	public INode getElement() {
		return (INode) this.element;
	}

	@Override
	public boolean usePrefixesActive() {
		return false;
	}

	@Override
	public String getWantedPreferencesID() {
		return "ast_useStyledBoxes";
	}
}
