package lupos.gui.operatorgraph.visualeditor.util;

import lupos.gui.operatorgraph.visualeditor.operators.Operator;

public class ModificationException extends Exception {
	private static final long serialVersionUID = 1L;
	private int line;
	private int column;
	private Operator operator;

	public ModificationException(String msg, Operator operator) {
		this(msg, 0, 0, operator);
	}

	public ModificationException(String msg, int line, int column, Operator operator) {
		super(msg);

		this.line = line;
		this.column = column;
		this.operator = operator;
	}

	public int getLine() {
		return this.line;
	}

	public int getColumn() {
		return this.column;
	}

	public Operator getOperator() {
		return this.operator;
	}
}