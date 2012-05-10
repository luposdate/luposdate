package lupos.gui.anotherSyntaxHighlighting;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;

public class LuposDocument extends DefaultStyledDocument {

	protected LuposJTextPane text;
	protected Colorer colorer;
	protected volatile boolean ignoreColoring = false;
	protected final static int WAITINGTIME = 1000;
	
	/**
	 * 
	 * @param parser
	 *            The parser fitting to the language used in the document (text
	 *            area).
	 */
	public LuposDocument() {
		super();
		init();
	}
	
	public void init(final ILuposParser parser, final boolean startColorerThread){
		this.colorer = new Colorer(this, parser, LuposDocument.WAITINGTIME, startColorerThread);
	}
	
	public void init(final ILuposParser parser, final boolean startColorerThread, final int WAITINGTIME){
		this.colorer = new Colorer(this, parser, WAITINGTIME, startColorerThread);
	}
	
	/**
	 * Creates a listener which is receiving ranges to be worked with.
	 */
	private void init() {

		// initialize thread which invokes highlighting/parsing periodically.
		this.addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(final DocumentEvent arg0) {
				if(!ignoreColoring){
					final int offset = arg0.getOffset();
					colorer.transmitRemoveEvent(offset, offset + arg0.getLength());
				}
			}

			@Override
			public void insertUpdate(final DocumentEvent arg0) {
				if(!ignoreColoring){
					final int offset = arg0.getOffset();
					final int end = offset + arg0.getLength();
					colorer.transmitInsertEvent(offset, end);
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
	}	

	/**
	 * setting the LuposJTextPane
	 * @param luposJTextPane the {@link LuposJTextPane} to set
	 */
	public void setLuposJTextPane(LuposJTextPane luposJTextPane) {
		this.text = luposJTextPane;		
	}
	
	public void colorOneTimeAll(){
		this.colorer.colorOneTime();
	}

	public void setIgnoreColoring(boolean ignoreColoring) {
		this.ignoreColoring = ignoreColoring; 
	}
}
