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
 * Searches windfinder with a keyword and calls the GeneralProducer and forwards the website and search data
 * @author Team 4
 *
 */
public class WindFinderProducer extends GeneralProducer {
	
	/**
	 * sets the namespace of this producer
	 */
	public static final String WINDFINDERNAMESPACE = "http://localhost/events/WindFinder/";
	/**
	 * creates the RDF-Type of this producer
	 */
	public final static URILiteral TYPE = Literals.createURI(WindFinderProducer.WINDFINDERNAMESPACE, "WindFinderProducer");
	
	/**
	 * The different used predicates...
	 */
	public final static URILiteral TIME = Literals.createURI(WINDFINDERNAMESPACE, "time");
	public final static URILiteral WIND_DIRECTION = Literals.createURI(WINDFINDERNAMESPACE, "wind_direction");
	public final static URILiteral WIND_SPEED = Literals.createURI(WINDFINDERNAMESPACE, "windspeed_in_kts");
	public final static URILiteral PRECIPITATION = Literals.createURI(WINDFINDERNAMESPACE, "precipitation_in_mm");
	public final static URILiteral PRESSURE = Literals.createURI(WINDFINDERNAMESPACE, "pressure_in_hpa");
	public final static URILiteral TEMPERATURE = Literals.createURI(WINDFINDERNAMESPACE, "temperature_in_Ã‚Â°C");
	
	/**
	 * the page of the referred provider
	 */
	private static final String WEATHER_PROVIDER = "http://www.windfinder.com/forecast/";
	/**
	 * the data for the chosen location
	 */
	private static String defaultLocation = "marina_luebeck_werft_grell";

	/**
	 * The constructor just calls the super class and sets its parameter.
	 * Instances of WindFinderProducer should be created by calling 
	 * WindFinderProducer createWindFinderProducer(...).
	 */
	private WindFinderProducer(SerializingMessageService msgService,
			URL url, List<String> xpathlist,
			List<Literal> literalList, List<URILiteral> dataString, List<String> regexString) {
		super(msgService, WINDFINDERNAMESPACE, TYPE, url, xpathlist, literalList, dataString, regexString);
	}
	
	/**
	 * 
	 * @param msgService for TCP-transport
	 * @param location the location for the weather
	 * @return the WindFinderProducer
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public static WindFinderProducer createWindFinderProducer(SerializingMessageService msgService, String location) throws MalformedURLException, UnsupportedEncodingException{
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
		List<URILiteral> dataString = new ArrayList<URILiteral>();
		/**
		 * regular expression can be set for a query, in this case decimal values
		 */
		List<String> regexString = new ArrayList<String>();
		// list of xPath commands to find data on html webpage
		xPathList.add("//span[@class='forecast-time hide']");
		// list of datatypes that data from html page has
		dataString.add(Literals.XSD.TIME);
		// list of predicates for triples
		literalList.add(TIME);
		// list of regular expressions for selecting specific information out of xPath node
		regexString.add("");
		
		xPathList.add("//span[@class='units-wd units-wd-dir']");
		dataString.add(Literals.XSD.String);
		literalList.add(WIND_DIRECTION);
		regexString.add("");
		
		xPathList.add("//span[contains(@class,'windspeed')]");
		dataString.add(Literals.XSD.INT);
		literalList.add(WIND_SPEED);
		regexString.add("");
		
		xPathList.add("//div[contains(@class,'units-pr')]");
		dataString.add(Literals.XSD.INT);
		literalList.add(PRECIPITATION);
		regexString.add("");
		
		xPathList.add("//td[contains(@class,'pres')]");
		dataString.add(Literals.XSD.INT);
		literalList.add(PRESSURE);
		regexString.add("");
		
		xPathList.add("//div[contains(@class,'units-at')]");
		dataString.add(Literals.XSD.INT);
		literalList.add(TEMPERATURE);
		regexString.add("");
		
		URL url = new URL(WEATHER_PROVIDER + URLEncoder.encode(location, "UTF-8"));
		
		WindFinderProducer wp = new WindFinderProducer(msgService, url, xPathList, literalList, dataString, regexString);
		
		return wp;
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
		
		String location = JOptionPane.showInputDialog("Enter location to get weather:", defaultLocation); 
		WindFinderProducer wp = WindFinderProducer.createWindFinderProducer(msgService, location);
		// start producer
		wp.start();		
	}
}