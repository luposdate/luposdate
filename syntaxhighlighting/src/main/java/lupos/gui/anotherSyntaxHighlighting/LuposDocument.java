
/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.anotherSyntaxHighlighting;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
public class LuposDocument extends DefaultStyledDocument {

	protected LuposJTextPane text;
	protected Colorer colorer;
	protected volatile boolean ignoreColoring = false;
	/** Constant <code>WAITINGTIME=1000</code> */
	protected final static int WAITINGTIME = 1000;

	/**
	 * <p>Constructor for LuposDocument.</p>
	 */
	public LuposDocument() {
		super();
		this.init();
	}

	/**
	 * <p>init.</p>
	 *
	 * @param parser a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 * @param startColorerThread a boolean.
	 */
	public void init(final ILuposParser parser, final boolean startColorerThread){
		this.colorer = new Colorer(this, parser, LuposDocument.WAITINGTIME, startColorerThread);
	}

	/**
	 * <p>init.</p>
	 *
	 * @param parser a {@link lupos.gui.anotherSyntaxHighlighting.ILuposParser} object.
	 * @param startColorerThread a boolean.
	 * @param WAITINGTIME_Parameter a int.
	 */
	public void init(final ILuposParser parser, final boolean startColorerThread, final int WAITINGTIME_Parameter){
		this.colorer = new Colorer(this, parser, WAITINGTIME_Parameter, startColorerThread);
	}

	/**
	 * Creates a listener which is receiving ranges to be worked with.
	 */
	private void init() {

		this.addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(final DocumentEvent arg0) {
				if(!LuposDocument.this.ignoreColoring){
					final int offset = arg0.getOffset();
					LuposDocument.this.colorer.transmitRemoveEvent(offset, offset + arg0.getLength());
				}
			}

			@Override
			public void insertUpdate(final DocumentEvent arg0) {
				if(!LuposDocument.this.ignoreColoring){
					final int offset = arg0.getOffset();
					final int end = offset + arg0.getLength();
					LuposDocument.this.colorer.transmitInsertEvent(offset, end);
				}
			}

			@Override
			public void changedUpdate(final DocumentEvent arg0) {
				// should not occur
			}
		});
	}

	/**
	 * setting the LuposJTextPane
	 *
	 * @param luposJTextPane the {@link lupos.gui.anotherSyntaxHighlighting.LuposJTextPane} to set
	 */
	public void setLuposJTextPane(final LuposJTextPane luposJTextPane) {
		this.text = luposJTextPane;
	}

	/**
	 * <p>colorOneTimeAll.</p>
	 */
	public void colorOneTimeAll(){
		this.colorer.colorOneTime();
	}

	/**
	 * <p>Setter for the field <code>ignoreColoring</code>.</p>
	 *
	 * @param ignoreColoring a boolean.
	 */
	public void setIgnoreColoring(final boolean ignoreColoring) {
		this.ignoreColoring = ignoreColoring;
	}
}
