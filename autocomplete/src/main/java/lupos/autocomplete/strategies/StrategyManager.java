
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
package lupos.autocomplete.strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;

import lupos.autocomplete.misc.Item;
import lupos.autocomplete.misc.RIFParserHelper;
import lupos.autocomplete.strategies.aos.AlphabeticOrderStrategy;
import lupos.autocomplete.strategies.aos.AlphabeticOrderStrategyRDF;
import lupos.autocomplete.strategies.aos.AlphabeticOrderStrategyRIF;
import lupos.autocomplete.strategies.aos.AlphabeticOrderStrategySPARQL;
import lupos.autocomplete.strategies.diao.DocumentInputAndAlphabeticOrderStrategy;
import lupos.autocomplete.strategies.diao.DocumentInputAndAlphabeticOrderStrategyRDF;
import lupos.autocomplete.strategies.diao.DocumentInputAndAlphabeticOrderStrategyRIF;
import lupos.autocomplete.strategies.diao.DocumentInputAndAlphabeticOrderStrategySPARQL;
import lupos.autocomplete.strategies.diof.DocumentInputAndOrderByFrequencyStrategy;
import lupos.autocomplete.strategies.diof.DocumentInputAndOrderByFrequencyStrategyRDF;
import lupos.autocomplete.strategies.diof.DocumentInputAndOrderByFrequencyStrategyRIF;
import lupos.autocomplete.strategies.diof.DocumentInputAndOrderByFrequencyStrategySPARQL;
import lupos.autocomplete.strategies.ps.ParserIdentificationStrategy;
import lupos.autocomplete.strategies.ps.ParserIdentificationStrategyRDF;
import lupos.autocomplete.strategies.ps.ParserIdentificationStrategyRIF;
import lupos.autocomplete.strategies.ps.ParserIdentificationStrategySPARQL;
import lupos.gui.anotherSyntaxHighlighting.ILuposParser;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.javacc.RIFParser;
import lupos.gui.anotherSyntaxHighlighting.javacc.SPARQLParser;
import lupos.gui.anotherSyntaxHighlighting.javacc.TurtleParser;
public class StrategyManager {

	//Standardmaessig ist hybride Strategie 5 eingestellt ( diof + ps = true)
	protected boolean aos = false;
	protected boolean diao = false;
	protected boolean diof = true;
	protected boolean ps = true;

	public enum LANGUAGE {
		SPARQL {
			@Override
			public AlphabeticOrderStrategy createAlphabeticOrderStrategy() {
				return new AlphabeticOrderStrategySPARQL();
			}

			@Override
			public DocumentInputAndAlphabeticOrderStrategy createDocumentInputAndAlphabeticOrderStrategy(final LuposDocumentReader r, final ILuposParser p) {
				return new DocumentInputAndAlphabeticOrderStrategySPARQL(r, p);
			}

			@Override
			public DocumentInputAndOrderByFrequencyStrategy createDocumentInputAndOrderByFrequencyStrategy(final LuposDocumentReader r, final ILuposParser p) {
				return new DocumentInputAndOrderByFrequencyStrategySPARQL(r, p);
			}

			@Override
			public ParserIdentificationStrategy createParserIdentificationStrategy(final LuposDocumentReader r, final ILuposParser p) {
				return new ParserIdentificationStrategySPARQL(r, p);
			}

			@Override
			public ILuposParser createLuposParser(final LuposDocumentReader reader) {
				return SPARQLParser.createILuposParser(reader);
			}
		},
		RIF {
			@Override
			public AlphabeticOrderStrategy createAlphabeticOrderStrategy() {
				return new AlphabeticOrderStrategyRIF();
			}

			@Override
			public DocumentInputAndAlphabeticOrderStrategy createDocumentInputAndAlphabeticOrderStrategy(final LuposDocumentReader r, final ILuposParser p) {
				return new DocumentInputAndAlphabeticOrderStrategyRIF(r, p);
			}

			@Override
			public DocumentInputAndOrderByFrequencyStrategy createDocumentInputAndOrderByFrequencyStrategy(final LuposDocumentReader r, final ILuposParser p) {
				return new DocumentInputAndOrderByFrequencyStrategyRIF(r, p);
			}

			@Override
			public ParserIdentificationStrategy createParserIdentificationStrategy(final LuposDocumentReader r, final ILuposParser p) {
				return new ParserIdentificationStrategyRIF(r, p);
			}

			@Override
			public ILuposParser createLuposParser(final LuposDocumentReader reader) {
				return new RIFParserHelper(RIFParser.createILuposParser(reader));
			}
		},
		RDF {
			@Override
			public AlphabeticOrderStrategy createAlphabeticOrderStrategy() {
				return new AlphabeticOrderStrategyRDF();
			}

			@Override
			public DocumentInputAndAlphabeticOrderStrategy createDocumentInputAndAlphabeticOrderStrategy(final LuposDocumentReader r, final ILuposParser p) {
				return new DocumentInputAndAlphabeticOrderStrategyRDF(r, p);
			}

			@Override
			public DocumentInputAndOrderByFrequencyStrategy createDocumentInputAndOrderByFrequencyStrategy(final LuposDocumentReader r, final ILuposParser p) {
				return new DocumentInputAndOrderByFrequencyStrategyRDF(r, p);
			}

			@Override
			public ParserIdentificationStrategy createParserIdentificationStrategy(final LuposDocumentReader r, final ILuposParser p) {
				return new ParserIdentificationStrategyRDF(r, p);
			}

			@Override
			public ILuposParser createLuposParser(final LuposDocumentReader reader) {
				return TurtleParser.createILuposParser(reader);
			}
		};

		public abstract AlphabeticOrderStrategy createAlphabeticOrderStrategy();
		public abstract DocumentInputAndAlphabeticOrderStrategy createDocumentInputAndAlphabeticOrderStrategy(final LuposDocumentReader r, final ILuposParser p);
		public abstract DocumentInputAndOrderByFrequencyStrategy createDocumentInputAndOrderByFrequencyStrategy(final LuposDocumentReader r, final ILuposParser p);
		public abstract ParserIdentificationStrategy createParserIdentificationStrategy(final LuposDocumentReader r, final ILuposParser p);
		public abstract ILuposParser createLuposParser(LuposDocumentReader reader);
	};

	protected final LuposDocument document;
	protected final LuposDocumentReader reader;
	protected final ILuposParser parser;
	protected final AlphabeticOrderStrategy aosStrategy;
	protected final DocumentInputAndAlphabeticOrderStrategy diaoStrategy;
	protected final DocumentInputAndOrderByFrequencyStrategy diofStrategy;
	protected final ParserIdentificationStrategy psStrategy;

	/**
	 * <p>Constructor for StrategyManager.</p>
	 *
	 * @param language a {@link lupos.autocomplete.strategies.StrategyManager.LANGUAGE} object.
	 * @param d a {@link lupos.gui.anotherSyntaxHighlighting.LuposDocument} object.
	 */
	public StrategyManager(final LANGUAGE language, final LuposDocument d) {
		this.document = d;
		this.reader = new LuposDocumentReader(this.document);
		this.parser = language.createLuposParser(this.reader);
		this.aosStrategy = language.createAlphabeticOrderStrategy();
		this.diaoStrategy = language.createDocumentInputAndAlphabeticOrderStrategy(this.reader, this.parser);
		this.diofStrategy = language.createDocumentInputAndOrderByFrequencyStrategy(this.reader, this.parser);
		this.psStrategy = language.createParserIdentificationStrategy(this.reader, this.parser);
	}

	/*
	 * Funktion zum setzen der Strategie booleans,
	 * wird aufgerufen in GuiWindow
	 */
	/**
	 * <p>setStrategyChoices.</p>
	 *
	 * @param aos a boolean.
	 * @param diao a boolean.
	 * @param diof a boolean.
	 * @param ps a boolean.
	 */
	public void setStrategyChoices(final boolean aos, final boolean diao, final boolean diof, final boolean ps){
		this.aos = aos;
		this.diao = diao;
		this.diof = diof;
		this.ps = ps;
	}

	/*
	 * Hier werden die Elemente der einzelnen Strategien zusammengefuehrt
	 * und nach gewichtungen sortiert
	 */
	/**
	 * <p>JoinStrategies.</p>
	 *
	 * @param textDocument a {@link java.lang.String} object.
	 * @param cursorPosition a int.
	 * @return a {@link java.util.ArrayList} object.
	 */
	public ArrayList<Entry<Item, int[]>> JoinStrategies(final String textDocument, final int cursorPosition){
		List<Entry<Item, Integer>> returnList = new ArrayList<Entry<Item,Integer>>();
		final HashMap<Item, int[]> hm = new HashMap<Item, int[]>();
		int[] array;
		if (this.diao) {
			returnList = this.diaoStrategy.createAutoCompletionList(textDocument, cursorPosition);
			for (int i = 0; i < returnList.size(); i++) {
				array = new int[]{	0,
									0,
									returnList.get(i).getValue()};
				hm.put(returnList.get(i).getKey(), array);
			}
		} else if (this.aos) {
			returnList = this.aosStrategy.createAutoCompletionList(textDocument, cursorPosition);
			for (int i = 0; i < returnList.size(); i++) {
				array = new int[]{0,0,returnList.get(i).getValue()};
				hm.put(returnList.get(i).getKey(), array);
			}
		}
		if (this.diof) {
			returnList = this.diofStrategy.createAutoCompletionList(textDocument, cursorPosition);
			for (int i = 0; i < returnList.size(); i++) {
				if (hm.containsKey(returnList.get(i).getKey())) {
					array = new int[]{	0,
										//Wert vom i-ten Element der aktuellen Strategie
										returnList.get(i).getValue(),
										//3. Stelle des Werte Arrays des i-ten Elements in hm
										hm.get(returnList.get(i).getKey())[2]};
				} else {
					array = new int[]{	0,
							//Wert vom i-ten Element der aktuellen Strategie
							returnList.get(i).getValue(),
							0};
				}
				hm.put(returnList.get(i).getKey(), array);
			}
		}
		if (this.ps) {
			returnList = this.psStrategy.createAutoCompletionList(textDocument, cursorPosition);
			for (int i = 0; i < returnList.size(); i++) {
				if (hm.containsKey(returnList.get(i).getKey())) {

					array = new int[]{
										//Wert vom i-ten Element der aktuellen Strategie
										returnList.get(i).getValue(),
										//2. und 3. Stelle des Werte Arrays des i-ten Elements in hm
										hm.get(returnList.get(i).getKey())[1],
										hm.get(returnList.get(i).getKey())[2]};
				} else {
					array = new int[]{
							//Wert vom i-ten Element der aktuellen Strategie
							returnList.get(i).getValue(),
							0,
							0};
				}
				hm.put(returnList.get(i).getKey(), array);
			}
		}


		final ArrayList<Entry<Item, int[]>> list = new ArrayList<Entry<Item, int[]>>(hm.entrySet());

		Collections.sort(list, new Comparator<Entry<Item, int[]>>(){

			/*
			 * spezielle compare funktion die durch die arrays geht
			 */
			@Override
			public int compare(final Entry<Item, int[]> arg0, final Entry<Item, int[]> arg1) {
				final int[] a0 = arg0.getValue();
				final int[] a1 = arg1.getValue();
				for(int i=0; i<a0.length; i++){
					final int diff = a1[i]-a0[i];
					if(diff!=0){
						return diff;
					}
				}
				return arg0.getKey().getValue().compareTo(arg1.getKey().getValue());
			}
		});
		return list;
	}

	/*
	 * fuegt die Elemente der Vorschlagsliste in ein fuer die JList lesbares Objekt ein
	 */
	/**
	 * <p>listToJList.</p>
	 *
	 * @param textDocument a {@link java.lang.String} object.
	 * @param cursorPosition a int.
	 * @return a {@link javax.swing.DefaultListModel} object.
	 */
	public DefaultListModel listToJList( final String textDocument, final int cursorPosition){
		final List<Entry<Item, int[]>> returnList = this.JoinStrategies(textDocument, cursorPosition);
		final DefaultListModel jl = new DefaultListModel();
		if (returnList!=null) {
			for (int i = 0; i < returnList.size(); i++) {

				jl.addElement(returnList.get(i).getKey().getValue());
			}
		}
		return jl;
	}

	/*
	 * gibt fuer die Analyse Klasse die Werte als Liste zurueck
	 */
	/**
	 * <p>listForAnalyse.</p>
	 *
	 * @param textDocument a {@link java.lang.String} object.
	 * @param cursorPosition a int.
	 * @return a {@link java.util.List} object.
	 */
	public List<String> listForAnalyse(final String textDocument, final int cursorPosition){
		final List<Entry<Item, int[]>> returnList = this.JoinStrategies(textDocument, cursorPosition);
		final List<String> list = new ArrayList<String>();
		if (returnList!=null) {
			for (int i = 0; i < returnList.size(); i++) {
				list.add(returnList.get(i).getKey().getValue());
			}
		}
		return list;
	}
}
