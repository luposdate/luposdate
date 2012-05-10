package lupos.gui.operatorgraph.viewer;

import java.awt.Dimension;

import javax.swing.JLabel;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

public class AnnotationPanel extends AbstractSuperGuiComponent {
	private static final long serialVersionUID = 360669447596005364L;

	public AnnotationPanel(OperatorGraph parent, GraphWrapper gw, String annotationText) {
		super(parent, gw, false);

		JLabel textLabel = new JLabel(annotationText);
		textLabel.setFont(parent.getFONT());
		textLabel.setVerticalAlignment(JLabel.BOTTOM);

		textLabel.setPreferredSize(new Dimension(textLabel.getPreferredSize().width, textLabel.getPreferredSize().height));

		this.add(textLabel);

		this.setPreferredSize(new Dimension(textLabel.getPreferredSize().width + (int) (2 * parent.PADDING), textLabel.getPreferredSize().height + (int) (2 * parent.PADDING)));
	}
}