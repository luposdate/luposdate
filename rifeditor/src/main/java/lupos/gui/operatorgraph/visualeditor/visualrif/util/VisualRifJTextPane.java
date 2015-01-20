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
 */
package lupos.gui.operatorgraph.visualeditor.visualrif.util;


import java.awt.FontMetrics;
import java.util.LinkedList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;

public class VisualRifJTextPane extends LuposJTextPane {
	private static final long serialVersionUID = 1L;
	private LuposDocument doc = null;
	private int tabWidth;


	/**
	 * Constructor for LuposJTextPane
	 */
	public VisualRifJTextPane() {
		super();
	}

	/**
	 * Constructor for LuposJTextPane
	 * @param doc StyledDocument
	 */
	public VisualRifJTextPane(final StyledDocument doc) {
		super(doc);
	}

	/**
	 * Constructor for LuposJTextPane
	 * @param doc {@link LuposDocument}
	 */
	public VisualRifJTextPane(final LuposDocument doc) {
		super(doc);

		this.doc = doc;

		this.doc.setLuposJTextPane(this);
	}

	public void setTabWidth(int charactersPerTab) {
		this.tabWidth = charactersPerTab;
		FontMetrics fm = getFontMetrics(getFont());
		int charWidth = fm.charWidth('w');
		int tabWidth = charWidth * charactersPerTab;

		TabStop[] tabs = new TabStop[50];

		for(int j = 0; j < tabs.length; j++) {
			int tab = j + 1;
			tabs[j] = new TabStop(tab * tabWidth);
		}

		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);
		int length = getDocument().getLength();
		getStyledDocument().setParagraphAttributes(0, length, attributes, false);
	}
	
	public void setText(String text){

		final StringBuffer sb = new StringBuffer();
		char tab = '\t'; 
		sb.append(text);
 
		
		
		
		LinkedList<MatchResult> results = findMatches( "\n", sb );
		

		for (int i = 1; i <= results.size(); i++) {
			int tabCNT = calculateTab(results.get(results.size()-i).end(),sb);
			for (int j = 0; j < tabCNT; j++) {
				sb.insert(results.get(results.size()-(i)).end(), tab);
			}
	
		}
		
		
		super.setText(sb.toString());
	}
	
	private int calculateTab(int start, StringBuffer sb) {
		String subString = sb.substring(0, start);
		Pattern bracketOpenPattern = Pattern.compile( "\\(" ); 
		Pattern bracketClosePattern = Pattern.compile( "\\)" );
		Matcher matcherOpen = bracketOpenPattern.matcher(subString);
		Matcher matcherClose = bracketClosePattern.matcher(subString);
		int openbracket = 0;
		int closebracket = 0;
		
		
		while(matcherOpen.find())openbracket++;
		while(matcherClose.find())closebracket = closebracket+1;
		
		return (openbracket - closebracket);
	}
	
	static LinkedList<MatchResult> findMatches( String pattern, CharSequence s ) 
	{ 
	  LinkedList<MatchResult> results = new LinkedList<MatchResult>(); 
	 
	  for ( Matcher m = Pattern.compile(pattern).matcher(s); m.find(); ) 
	    results.add( m.toMatchResult() ); 
	 
	  return results; 
	}
	
}