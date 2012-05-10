package lupos.gui.operatorgraph.visualeditor.dataeditor.util;

import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.Connection;

public class DataConnection extends Connection<Operator> {
	public DataConnection(VisualEditor<Operator> visualEditor) {
		super(visualEditor);
	}

	protected String validateConnection() {
		return "";
	}
}