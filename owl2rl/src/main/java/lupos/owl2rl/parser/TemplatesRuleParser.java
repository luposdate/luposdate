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
package lupos.owl2rl.parser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import lupos.owl2rl.owlToRif.TemplateRule;
public class TemplatesRuleParser extends DefaultHandler{


	private TemplateRule tempTemplateRule= new TemplateRule(); //Das Objekt das während des Parsens gefüllt und dann in die Liste übergeben wird
	private String prefixes=""; //Dieser String hält die RIF-Prefixe, die im XML-Dokument auftauchen
	private StringBuilder content=new StringBuilder(); //StringBuilder, der die gelesenen Zeichen zwischen den Tags speichert, bis sie der templateRule übergeben werden.
	private boolean finishedParsing=false;
	private LinkedList<String> prefixnames= new LinkedList<String>();
	private String fixed="";
	private HashMap<String, TemplateRule> templateRulesmap= new HashMap<String, TemplateRule>();
	private LinkedList<String> nameList= new LinkedList<String>();

	private String[]vars;
	private LinkedList<String> variablesTemp=new  LinkedList<String>();

	private ParserResults results= new ParserResults();

	/**
	 * <p>Getter for the field <code>results</code>.</p>
	 *
	 * @return a {@link lupos.owl2rl.parser.ParserResults} object.
	 */
	public ParserResults getResults(){
		return this.results;
	}

	/**
	 * <p>Constructor for TemplatesRuleParser.</p>
	 */
	public TemplatesRuleParser(){
		super();
	}

	/*
	 * The tags used in the XML files
	 * 
	 */
	final static private String NAME="name", //name des Attributes von Prefixes und name des Templates
								PREFIX="prefix", //Prefix für RIF
								PATTERN="pattern", //Das zu ersetzende Pattern in OWL
								TEMPLATESTARTEND="templaterule", // Beginn eines neuen Tupels aus Pattern und Template
								VARIABLE="variable", //Eine Variable, die im Template mit matches aus dem Pattern zu ersetzen ist
								TEMPLATE="template", //Das Template für die RIF-Regeln
								PATTERNQUERY = "rifquery",
								BASE = "base",
								TYPE = "type",
								METHOD = "methodname",
								CLASS ="classname",
								FIXED = "fixedrules";

	/**
	 * <p>start.</p>
	 *
	 * @param rules a {@link java.io.InputStream} object.
	 */
	public void start(InputStream rules) {
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			parser.setContentHandler(this);		
			parser.parse(new org.xml.sax.InputSource(rules));
		} catch (Exception e1) {
			System.err.println("error: Unable to instantiate parser ("+"org.apache.xerces.parsers.SAXParser"+")");
			e1.printStackTrace();
		}
		this.finishedParsing=false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Wird am Anfang des Dokuments aufgerufen, definiert im Interface ContentHandler.
	 */
	@Override
	public void startDocument() throws SAXException {
		// ignore
	}



	/** {@inheritDoc} */
	@Override
	public void endDocument() throws SAXException {
		//        System.out.println("++++++++++Ende des Dokuments+++++++++++");

		this.results.setFixedrules(this.fixed);
		this.results.setPrefixes(this.prefixes);
		this.results.setTemplateRulesmap(this.templateRulesmap);
		this.results.setNames(this.nameList.toArray(new String[]{}));
		this.finishedParsing=true;
	}

	/**
	 * <p>isFinished.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isFinished(){
		return this.finishedParsing;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Wird bei jedem öffnenden Tag aufgerufen, definiert im Interface ContentHandler.
	 * Bei leeren Tags wie zum Beispiel &lt;img /&gt; wird startElement und
	 * endElement direkt hintereinander aufgerufen.
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		if(qName.equals(TEMPLATESTARTEND)){
			this.tempTemplateRule= new TemplateRule();
		} else if(qName.equals(PREFIX)){
			// a prefix tag has been found
			if(atts.getQName(0).equals(NAME)){
				this.prefixnames.add(atts.getValue(0));
				this.prefixes+="Prefix("+atts.getValue(0)+" <";
			}	
		} else if(qName.equals(PATTERNQUERY)){
			if(atts.getQName(0).equals(TYPE)){
				this.tempTemplateRule.queryType=getPatternQueryEnum(atts.getValue(0));
			}	
		} else if(qName.equals(BASE)){
			this.prefixes+="Base(<";
		}
	}

	private PatternQueryType getPatternQueryEnum(String type) {
		if(type.equals("xml")){
			return PatternQueryType.FROMXMLDATA;
		} else if(type.equals("rdftype")){
			return PatternQueryType.RDFTYPE;
		} else if(type.equals("p1-prp-p2")){
			return PatternQueryType.P1HASPROPERTYP2;
		} else if(type.equals("withforall")){
			return PatternQueryType.WITHFORALL;
		}
		return null;
	}


	/**
	 * {@inheritDoc}
	 *
	 * Wird bei jedem schließenden Tag aufgerufen, definiert im Interface ContentHandler.
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName){
		final String trimmedContent = this.content.toString().trim();

		//    	System.out.println("Name:"+qName + "\nContent: "+ content);
		
		if(qName.equals(PATTERN)){
			this.tempTemplateRule.setPattern(trimmedContent);
		} else if(qName.equals(NAME)){
			this.tempTemplateRule.setName(trimmedContent.toString());
			this.nameList.add(trimmedContent.toString());

		} else if(qName.equals(METHOD)){
			this.tempTemplateRule.setMethodName(trimmedContent.toString());

		} else if(qName.equals(CLASS)){
			this.tempTemplateRule.setClassName(trimmedContent.toString());

		} else if(qName.equals(PATTERNQUERY)){
			this.tempTemplateRule.setPatternQuery(trimmedContent.toString(), this.prefixes);

		} else if(qName.equals(TEMPLATE)){
			this.tempTemplateRule.setTemplate(trimmedContent.toString());

		} else if(qName.equals(VARIABLE)){
			addVariable(trimmedContent.toString());

		} else if(qName.equals(TEMPLATESTARTEND)){
			// Ein Tupel in die Liste packen
			this.tempTemplateRule.setVariableArray(getVariablesTempArray());
			this.tempTemplateRule.create();
			this.templateRulesmap.put(this.tempTemplateRule.getName(), this.tempTemplateRule);
		}

		//Prefixes werden geladen
		else if(qName.equals(PREFIX)){
			this.prefixes+=trimmedContent +">) \n";
			//     		prefixURIs.add(content.toString());
		} else if(qName.equals(BASE)){
			this.prefixes+=trimmedContent.toString() +">) \n";
		} else if(qName.equals(FIXED)){
			this.fixed+=trimmedContent.toString()+"\n";	
		}
		this.content.setLength(0);//cheapest way of deleting a StringBuilder?!
	}





	private String[] getVariablesTempArray() {
		this.vars = this.variablesTemp.toArray(new String[]{});	
		this.variablesTemp.clear();		
		return this.vars;
	}



	private void addVariable(String string) {
		this.variablesTemp.add(string);
	}


	/**
	 * {@inheritDoc}
	 *
	 * Wird immer aufgerufen, wenn Zeichen im Dokument auftauchen.
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		if (this.content!=null) {
			for (int i=start; i<start+length; i++) {
				this.content.append(ch[i]);
			}
		}   
	}
}
