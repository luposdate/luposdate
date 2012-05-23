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
package lupos.endpoint.client.formatreader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;

public class XMLFormatReader extends MIMEFormatReader {
	
	public final static String MIMETYPE = "application/sparql-results+xml";

	// the type of state used for sax parsing
	private static enum TYPE {
		uri, literal, typedliteral, languagetaggedliteral, blanknode, undefined
	}

	final ReentrantLock lock = new ReentrantLock();
	final Condition decisionMade = this.lock.newCondition();
	private volatile boolean decision = false;
	private volatile boolean booleanResult = false;
	private volatile boolean resultOfBooleanResult;

	public XMLFormatReader() {
		super("XML", XMLFormatReader.MIMETYPE);
	}

	@Override
	public String getMIMEType() {
		return XMLFormatReader.MIMETYPE;
	}

	@Override
	public QueryResult getQueryResult(final InputStream inputStream) {
		final BoundedBuffer<Bindings> boundedBuffer = new BoundedBuffer<Bindings>();
		
		final ParseThread parseThread = new ParseThread(inputStream, boundedBuffer);
		parseThread.start();
				
		this.lock.lock();
		try {
			// wait until it is clear whether it is a boolean result or a "normal" queryresult
			while(!this.decision){
				this.decisionMade.await();
			}
		} catch (InterruptedException e) {
			System.err.println();
			e.printStackTrace();
		} finally {
			this.lock.unlock();
		}
		
		if(this.booleanResult){
			BooleanResult result = new BooleanResult();
			if(this.resultOfBooleanResult){
				result.add(Bindings.createNewInstance());
			}
			return result;
		} else {		
			return QueryResult.createInstance(new ParallelIterator<Bindings>(){
	
				@Override
				public boolean hasNext() {
					try {
						return boundedBuffer.hasNext();
					} catch (InterruptedException e) {
						System.err.println(e);
						e.printStackTrace();
					}
					return false;
				}
	
				@Override
				public Bindings next() {
					try {
						return boundedBuffer.get();
					} catch (InterruptedException e) {
						System.err.println(e);
						e.printStackTrace();
					}
					return null;
				}
	
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
	
				@Override
				public void close() {
					boundedBuffer.stopIt();
				}			
			});
		}
	}
	
	public class ParseThread extends Thread {
		
		private final InputStream inputStream;
		private final BoundedBuffer<Bindings> boundedBuffer;
		
		public ParseThread(final InputStream inputStream, final BoundedBuffer<Bindings> boundedBuffer){
			this.inputStream = inputStream;
			this.boundedBuffer = boundedBuffer; 
		}
		
		@Override
		public void run(){
			SAXParserFactory factory = SAXParserFactory.newInstance();
			try {
				SAXParser saxParser = factory.newSAXParser();
				ResultsHandler resultsHandler = new ResultsHandler(this.boundedBuffer);
				saxParser.parse(this.inputStream, resultsHandler);
				this.boundedBuffer.endOfData();
			} catch (ParserConfigurationException e) {
				System.err.println(e);
				e.printStackTrace();
			} catch (SAXException e) {
				System.err.println(e);
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}		
		}
	}

	public class ResultsHandler extends DefaultHandler {
		
		
		private final BoundedBuffer<Bindings> boundedBuffer;
		
		private boolean booleanResultInHandler = false;
		
		private Bindings currentBindings;
		private Variable currentVariable;
		private TYPE currentType = TYPE.undefined;
		private String currentAttribute = null;
		
		public ResultsHandler(final BoundedBuffer<Bindings> boundedBuffer){
			this.boundedBuffer = boundedBuffer;
		}
		
		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
			this.currentType = TYPE.undefined;
			if(qName.equalsIgnoreCase("boolean")){
				this.booleanResultInHandler = true;
			} else if(qName.equalsIgnoreCase("results")){
				XMLFormatReader.this.lock.lock();
				try {
					XMLFormatReader.this.decision=true;
					XMLFormatReader.this.decisionMade.signalAll();
				} finally {
					XMLFormatReader.this.lock.unlock();
				}
			}  else if(qName.equalsIgnoreCase("result")){
				this.currentBindings = Bindings.createNewInstance();
			} else if(qName.equalsIgnoreCase("binding")){
				String variableName = attributes.getValue("name");
				if(variableName==null){
					System.err.println("There is no name element given in a bindings-tag!");
					Thread.dumpStack();
				} else {
					this.currentVariable = new Variable(variableName);
				}			
			} else if(qName.equalsIgnoreCase("uri")){
				this.currentType = TYPE.uri; 
			} else if(qName.equalsIgnoreCase("bnode")){
				this.currentType = TYPE.blanknode; 
			} else if(qName.equalsIgnoreCase("literal")){
				String datatype = attributes.getValue("datatype");
				if(datatype!=null){
					this.currentType = TYPE.typedliteral;
					this.currentAttribute = "<"+datatype+">";
					return;
				}
				String lang = attributes.getValue("xml:lang");
				if(lang!=null){
					this.currentType = TYPE.languagetaggedliteral;
					this.currentAttribute = lang;
					return;
				}				
				this.currentType = TYPE.literal; 
			} 
		}
		
		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			this.currentType = TYPE.undefined;
			if(qName.equalsIgnoreCase("result")){
				try {
					this.boundedBuffer.put(this.currentBindings);
				} catch (InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void characters(final char ch[], final int start, final int length) throws SAXException {
			final String content = new String(ch, start, length);
			if(this.booleanResultInHandler){
				XMLFormatReader.this.lock.lock();
				try {
					XMLFormatReader.this.booleanResult=true;
					XMLFormatReader.this.resultOfBooleanResult = content.equals("true");
					XMLFormatReader.this.decision=true;
					XMLFormatReader.this.decisionMade.signalAll();
				} finally {
					XMLFormatReader.this.lock.unlock();
				}
				
			}			
			Literal literal = null;
			switch(this.currentType){
			default:
			case undefined:
				break;
			case uri:
				literal = LiteralFactory.createURILiteralWithoutException("<"+content+">");
				break;
			case literal:
				literal = LiteralFactory.createLiteral("\""+content+"\"");
				break;
			case typedliteral:
				literal = LiteralFactory.createTypedLiteralWithoutException("\""+content+"\"", this.currentAttribute);
				break;
			case languagetaggedliteral:
				literal = LiteralFactory.createLanguageTaggedLiteral("\""+content+"\"", this.currentAttribute);
				break;
			case blanknode:
				literal = LiteralFactory.createAnonymousLiteral("_:"+content);
				break;
			}
			if(literal!=null){
				this.currentBindings.add(this.currentVariable, literal);
			}
		}
	}
}
