/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
