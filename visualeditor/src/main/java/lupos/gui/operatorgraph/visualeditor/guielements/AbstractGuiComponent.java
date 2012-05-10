package lupos.gui.operatorgraph.visualeditor.guielements;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;
import lupos.gui.operatorgraph.visualeditor.util.Connection;

public abstract class AbstractGuiComponent<T> extends AbstractSuperGuiComponent {
	private static final long serialVersionUID = 1L;
	protected VisualGraph<T> parent;
	protected T operator;
	private boolean selected = false;
	protected T parentOp;
	protected T child;

	protected AbstractGuiComponent(final VisualGraph<T> parent, final GraphWrapper gw, final T operator, final boolean movable) {
		super(parent, gw, movable);

		this.parent = parent;
		this.operator = operator;
	}

	public void delete() {
		if(this.isAnnotation()) {
			this.getBox().deleteLineAnnotation(this.parent.createGraphWrapper(this.child));
		}
		else {
			this.getBox().removeAnnotations();

			// remove line annotations to this element...
			for(final GraphWrapper preGW : this.getBox().getOp().getPrecedingElements()) {
				this.parent.getBoxes().get(preGW).removeAnnotationsTo(this.gw);
			}

			// remove box from QueryGraph...
			this.parent.getBoxes().remove(this.gw);
		}

		// removing operator from OperatorContainer if he was in one...
		final AbstractGuiComponent<T> outerRef = this.parent.outerReference;

		if(outerRef != null && outerRef.getParentOp() instanceof OperatorContainer && !this.isAnnotation()) {
			((OperatorContainer) outerRef.getParentOp()).removeOperator((Operator) this.operator);
		}

		// remove this graphical component...
		this.parent.remove(this);
		this.parent.removeFromRootList(this.parent.createGraphWrapper(this.operator));

		this.parent.repaint();
	}

	public VisualEditor<T> getVisualEditor() {
		return this.parent.visualEditor;
	}

	public VisualGraph<T> getParentQG() {
		return this.parent;
	}

	public T getParentOp() {
		if(this.isAnnotation()) {
			return this.parentOp;
		}
		else {
			return this.operator;
		}
	}

	public T getChild() {
		return this.child;
	}

	public T getOperator() {
		return this.operator;
	}


	@SuppressWarnings("rawtypes")
	public static int showCorrectIgnoreOptionDialog(final VisualGraph parent, final String msg) {
		return JOptionPane.showOptionDialog(parent.visualEditor, msg, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[]{"Correct Input", "Ignore Error"}, 0);
	}


	@Override
	@SuppressWarnings("unchecked")
	public void mouseClicked(final MouseEvent me) {
		if(this.handleConnectionMode()) {
			return;
		}

		if(this.parent.visualEditor instanceof Suggester && ((Suggester<T>)this.parent.visualEditor).isInSuggestionMode(this.operator)) { // make suggestions
			makeSuggestions();

			return;
		}


		if(!me.isPopupTrigger()) { // handle left click {selection}...
			if(!this.selected) { // not selected => select now...
				if(!me.isControlDown()) { // de-select all others...
					for(final T operator : this.parent.visualEditor.getDeletionOperatorsList()) {
						final AbstractGuiComponent<T> guiComponent = this.parent.createGraphWrapper(operator).getGUIComponent();

						guiComponent.setBorder(guiComponent.getMyBorder());
					}

					for(final T operator : this.parent.visualEditor.getDeletionAnnotationList().keySet()) {
						for(final T child : this.parent.visualEditor.getDeletionAnnotationList().get(operator)) {
							final AbstractGuiComponent<T> guiComponent = this.parent.createGraphWrapper(operator).getAnnotationLabel(this.parent.createGraphWrapper(child));

							guiComponent.setBorder(guiComponent.getMyBorder());
						}
					}

					this.parent.visualEditor.clearDeletionOperatorsList();
					this.parent.visualEditor.clearDeletionAnnotationList();
				}

				this.selected = true;
				super.setBorderNoRemember(new LineBorder(Color.BLACK));

				if(this.isAnnotation()) {
					this.parent.visualEditor.setAnnotationForDeletion(this.getParentOp(), this.getChild());
				}
				else {
					this.parent.visualEditor.setOperatorForDeletion(this.operator);
				}
			}
			else { // selected => deselect now...
				this.selected = false;
				this.setBorder(this.border);

				if(this.isAnnotation()) {
					this.parent.visualEditor.unsetAnnotationForDeletion(this.getParentOp());
				}
				else {
					this.parent.visualEditor.unsetOperatorForDeletion(this.operator);
				}
			}
		}
	}

	protected void makeSuggestions() {
		this.parent.visualEditor.getStatusBar().clear();

		((Suggester<T>) this.parent.visualEditor).makeSuggestions(this.operator);
	}

	public boolean handleConnectionMode() {
		// get the connection mode...
		final Connection<T> connectionMode = this.parent.visualEditor.connectionMode;

		if(connectionMode != null) { // create a connection...
			connectionMode.addOperator(this.operator);

			return true;
		}

		return false;
	}

	public boolean inConnectionMode() {
		return this.parent.visualEditor.connectionMode != null;
	}

	public abstract boolean validateOperatorPanel(boolean showErrors, Object data);
}