/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.event.producers.webpage;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.ProducerBase;
import lupos.event.communication.SerializingMessageService;
import lupos.event.communication.TcpConnectInfo;
import lupos.event.communication.TcpMessageTransport;
import lupos.event.util.Literals;

/**
 * Searches german lotto page  with a keyword and calls the GeneralProducer and forwards the website and search data
 * @author Team 4
 *
 */
public class LottoProducer extends GeneralProducer{

	/**
	 * sets the namespace of this producer
	 */
	public static final String LOTTOPRODUCER_NAMESPACE = "http://localhost/events/Lotto/";
	
	/**
	 * creates the RDF-Type of this producer
	 */
	public final static URILiteral TYPE = Literals.createURI(LottoProducer.LOTTOPRODUCER_NAMESPACE, "LottoProducer");
	
	// the other predicates
	public final static URILiteral SIX_FROM_FOURTYNINE = Literals.createURI(LOTTOPRODUCER_NAMESPACE, "6_aus_49");
	public final static URILiteral ZUSATZZAHL = Literals.createURI(LOTTOPRODUCER_NAMESPACE, "Zusatzzahl");
	public final static URILiteral SUPERZAHL = Literals.createURI(LOTTOPRODUCER_NAMESPACE, "Superzahl");
	/**
	 * the page of the referred provider
	 */
	private static final String LOTTO_PROVIDER = "http://www.dielottozahlende.net/lotto/6aus49/6aus49.html";

	
	/**
	 * The constructor just calls the super class and sets its parameter
	 * 
	 * 
	 * @param msgService for TCP-transport
	 * 
	 */
	private LottoProducer(SerializingMessageService msgService,
			URL url, List<String> xpathlist,
			List<Literal> literalList, List<URILiteral> infoString, List<String> regexString) {
		super(msgService, LOTTOPRODUCER_NAMESPACE, TYPE, url, xpathlist, literalList, infoString, regexString );
	}

	
	public static LottoProducer createLottoProducer(SerializingMessageService msgService) throws MalformedURLException{
		/**
		 * URL data
		 */
		URL url = new URL(LOTTO_PROVIDER);
		/**
		 * list containing every created XPath query
		 */
		List<String> xPathList = new ArrayList<String>();
		/**
		 * list of Literals for a broker/client
		 */
		List<Literal> literalList = new ArrayList<Literal>();
		/**
		 * list for XSD-types of data
		 */
		List<URILiteral> infoString = new ArrayList<URILiteral>();
		/**
		 * regular expression can be set for a query, in this case decimal values
		 */
		List<String> regexString = new ArrayList<String>();
		
		xPathList.add("//p[@class='lz'][position()=1]");
		infoString.add(Literals.XSD.String);
		literalList.add(SIX_FROM_FOURTYNINE);
		regexString.add("");
		
		xPathList.add("//p[@class='lzzz'][position()=1]");
		infoString.add(Literals.XSD.String);
		literalList.add(ZUSATZZAHL);
		regexString.add("\\d+");
		
		xPathList.add("//p[@class='lzsz'][position()=1]");
		infoString.add(Literals.XSD.String);
		literalList.add(SUPERZAHL);
		regexString.add("\\d+");
		
		return new LottoProducer(msgService, url, xPathList, literalList, infoString, regexString);
	}
	
	/**
	 * 	sets the xPath commands and corresponding regular expressions, literaltypes and namespaces.
	 *  After that it starts the producer and with that calls constructor of WeatherProducer.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {		

		// create communication channel
		SerializingMessageService msgService = new SerializingMessageService(TcpMessageTransport.class);
		msgService.connect(new TcpConnectInfo(ProducerBase.askForHostOfBroker(), 4444));

		
		LottoProducer wtp = LottoProducer.createLottoProducer(msgService);
		// start producer
		wtp.start();
		
	}
}