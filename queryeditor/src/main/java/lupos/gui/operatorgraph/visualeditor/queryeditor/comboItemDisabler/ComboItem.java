package lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler;

public class ComboItem {
	private Object obj;
	public boolean isEnabled;

	public ComboItem(Object obj, boolean isEnabled) {
		this.obj = obj;
		this.isEnabled = isEnabled;
	}

	public ComboItem(Object obj) {
		this(obj, true);
	}

	public String toString() {
		return this.obj.toString();
	}

	public boolean equals(Object o) {
		if(o instanceof ComboItem) {
			ComboItem co = (ComboItem) o;

			return this.obj.equals(co.obj);
		}
		else
			return false;
	}

	public Object getObject() {
		return this.obj;
	}
}