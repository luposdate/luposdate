package lupos.gui.operatorgraph.graphwrapper;

import java.util.Hashtable;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;

public abstract class GraphWrapperEditable extends GraphWrapper {
	public GraphWrapperEditable(Object element) {
		super(element);
	}

	public boolean usePrefixesActive() {
		return true;
	}

	public String getWantedPreferencesID() {
		return "";
	}

	public abstract boolean validateObject(boolean showErrors, Object data);
	public abstract boolean variableInUse(String string);

	public abstract void addSucceedingElement(GraphWrapperIDTuple gwidt);
	public abstract void addPrecedingElement(GraphWrapper gw);
	@SuppressWarnings("rawtypes")
	public abstract AbstractGuiComponent getGUIComponent();
	public abstract boolean canAddSucceedingElement();
	@SuppressWarnings("rawtypes")
	public abstract Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(VisualGraph parent);
	public abstract void removeSucceedingElement(GraphWrapper gw);
	public abstract void deleteAnnotation(GraphWrapper gw);
	public abstract void delete(boolean subtree);
	public abstract StringBuffer serializeOperator();
	@SuppressWarnings("rawtypes")
	public abstract AbstractGuiComponent getAnnotationLabel(GraphWrapper gw);
}