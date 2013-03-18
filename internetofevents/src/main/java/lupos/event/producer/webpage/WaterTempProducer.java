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
package lupos.event.producer.webpage;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.communication.SerializingMessageService;
import lupos.event.producer.ProducerBase;
import lupos.event.util.Literals;


/**
 * Searches bsh with a keyword and calls the GeneralProducer and forwards the website and search data
 * @author Team 4
 *
 */
public class WaterTempProducer extends GeneralProducer{

	/**
	 * sets the namespace of this producer
	 */
	public static final String WATERTEMPPRODUCER_NAMESPACE = "http://localhost/events/WaterTemp/";
	/**
	 * creates the RDF-Type of this producer
	 */
	public final static URILiteral TYPE = Literals.createURI(WaterTempProducer.WATERTEMPPRODUCER_NAMESPACE, "WaterTempProducer");
	
	// the other predicates...
	public final static URILiteral TIME = Literals.createURI(WATERTEMPPRODUCER_NAMESPACE, "time");
	public final static URILiteral TEMP = Literals.createURI(WATERTEMPPRODUCER_NAMESPACE, "temperature_in_Ã‚Â°C");
	/**
	 * the page of the referred provider
	 */
	private static final String WEATHER_PROVIDER = "http://www.bsh.de/aktdat/bm/";
	/**
	 * the data for the chosen location
	 */
	private static String defaultlocation = "Travemuende.htm";
	
	/**
	 * The constructor just calls the super class and sets its parameter.
	 * Use WaterTempProducer.createWaterTempProducer(...) for creating a WaterTempProducer.
	 */
	private WaterTempProducer(SerializingMessageService msgService,
			URL url, List<String> xpathlist,
			List<Literal> literalList, List<URILiteral> infoString, List<String> regexString) {
		super(msgService, WATERTEMPPRODUCER_NAMESPACE, TYPE, url, xpathlist, literalList, infoString, regexString);
	}

	/**
	 * For creating a WaterTempProducer
	 * @param msgService for TCP-transport
	 * @param location the location for the water temperature
	 * @return the created WaterTempProducer
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public static WaterTempProducer createWaterTempProducer(final SerializingMessageService msgService, final String location) throws MalformedURLException, UnsupportedEncodingException{
		/**
		 * URL data
		 */
		URL url = new URL(WEATHER_PROVIDER + URLEncoder.encode(location, "UTF-8"));
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

		
		xPathList.add("//th[contains(@id,'h') and contains(@class,'zelle6')]|//th[contains(@id,'m') and contains(@class,'zelle6')]");
		infoString.add(Literals.XSD.String);
		literalList.add(TIME);
		regexString.add("");
		
		xPathList.add("//td[contains(@headers,'wtemp progh')]|//td[contains(@headers,'wtemp progm')]");
		infoString.add(Literals.XSD.INT);
		literalList.add(TEMP);
		regexString.add("");
		
		return new WaterTempProducer(msgService, url, xPathList, literalList, infoString, regexString);
	}

	/**
	 * 	sets the xPath commands and corresponding regular expressions, literaltypes and namespaces.
	 *  After that it starts the producer and with that calls constructor of WeatherProducer.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		String location = JOptionPane.showInputDialog("Enter location to get weather:", defaultlocation);
		
		WaterTempProducer wtp = WaterTempProducer.createWaterTempProducer(msgService, location);
		// start producer
		wtp.start();
		
	}
}