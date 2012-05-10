package lupos.gui.operatorgraph.visualeditor.guielements;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.DummyItem;
import lupos.gui.operatorgraph.visualeditor.util.VEPrefix;

public abstract class VisualGraphOperatorWithPrefix extends VisualGraphOperator {
	private static final long serialVersionUID = 8089073150957057614L;

	/**
	 * Instance of the prefix class which handles the prefixes and the
	 * name-spaces.
	 */
	public Prefix prefix = null;

	public VisualGraphOperatorWithPrefix(VisualEditor<Operator> visualEditor, Prefix prefix) {
		super(visualEditor);

		this.prefix = prefix;

		this.construct();
	}
	
	public abstract VisualGraphOperatorWithPrefix newInstance(VisualEditor<Operator> visualEditor, Prefix prefix);

	public void clearAll() {
		this.prefix = new VEPrefix(true);

		this.clear();
	}

	protected Operator createOperator(Class<? extends Operator> clazz, Item content) throws Exception {
		// get some class names...
		String newClassName = clazz.getSimpleName();

		// get the chosen operator...
		Operator newOp = null;

		try {
			if(newClassName.endsWith("RDFTerm") && !(content instanceof DummyItem)) {
				newOp = clazz.getDeclaredConstructor(Prefix.class, Item.class).newInstance(this.prefix, content);
			}
			else {
				newOp = clazz.getDeclaredConstructor(Prefix.class).newInstance(this.prefix);
			}
		}
		catch(NoSuchMethodException nsme) {
			newOp = clazz.newInstance();
		}

		return newOp;
	}
}