/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.event.producer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONObject;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.communication.SerializingMessageService;
import lupos.event.util.Literals;
import lupos.event.util.Utils;


/**
 * Produces events which contain current moon-related information gathered from http://www.wunderground.com.
 * 
 * @author  Anderson, Kutzner
 *
 */
public class MoonProducer extends ProducerBaseNoDuplicates {
	
	// Defines the name of the event
	public static final String NAMESPACE = "http://localhost/events/Moon/";
	// Defines the interval 
	private static final int INTERVAL = 10000;
	// Defines the base of the API URL
	private static final String API_BASE_URL = "http://api.wunderground.com/api/";

	// API-Key, Country- and City-Name used to create the search address with offering: Germany, Lubeck
	private String PROJECTCODE = "5f028a996895c1d6";
	private String LANDNAME = "Germany";
	private String CITYNAME = "Lubeck";
	
	
	/**
	 * 	Class that creates URIs for the search queries.
	 */
	public static class Predicates {
		public static final URILiteral TYPE = Literals.createURI(NAMESPACE, "MoonEvent");

		// information about the current moon
		/** percentage of the moon thats illuminated */
		public static final URILiteral PERCENT_ILLUMINATED = Literals.createURI(NAMESPACE, "percent_illuminated");
		/** days since the last new moon */
		public static final URILiteral AGE_OF_MOON = Literals.createURI(NAMESPACE, "age_of_moon");
		/** hour of the request */
		public static final URILiteral CURRENT_HOUR = Literals.createURI(NAMESPACE, "current_hour");
		/** minute of the request */
		public static final URILiteral CURRENT_MINUTE = Literals.createURI(NAMESPACE, "current_minute");
		/** hour of the sunrise */
		public static final URILiteral SUNRISE_HOUR = Literals.createURI(NAMESPACE, "sunrise_hour");
		/** minute of the sunrise */
		public static final URILiteral SUNRISE_MINUTE = Literals.createURI(NAMESPACE, "sunrice_minute");
		/** hour of the sunset */
		public static final URILiteral SUNSET_HOUR = Literals.createURI(NAMESPACE, "sunset_hour");
		/** minute of the sunset */
		public static final URILiteral SUNSET_MINUTE = Literals.createURI(NAMESPACE, "sunset_minute");
	}
	
	
	/**
	 * Constructor of the MoonProducer.
	 * 
	 * @param msgService The message service that the producer should use to communicate.
	 * @throws Exception
	 */
	public MoonProducer(SerializingMessageService msgService) throws Exception {
		super(msgService, INTERVAL);
	}
	
	/**
	 * Creates and returns triples with the moon information.
	 */
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		try {		
				// search for query via wunderground API
				// http://api.wunderground.com/api/5f028a996895c1d6/astronomy/q/Germany/Lubeck.json
				String jsonStr = Utils.httpGet(API_BASE_URL + this.PROJECTCODE + "/astronomy/q/" + this.LANDNAME + "/" + this.CITYNAME + ".json");

				// get results list from json-response
				JSONObject rootObj = new JSONObject(jsonStr);
				
				
				JSONObject returnObj = rootObj.getJSONObject("moon_phase");

				String percentIlluminatedValue = returnObj.getString("percentIlluminated");
				String ageOfMoonValue = returnObj.getString("ageOfMoon");
				
				
				JSONObject returnObj2 = rootObj.getJSONObject("moon_phase").getJSONObject("current_time");
				
				String currentHourValue = returnObj2.getString("hour");
				String currentMinuteValue = returnObj2.getString("minute");
				
				
				JSONObject returnObj3 = rootObj.getJSONObject("moon_phase").getJSONObject("sunrise");
				
				String sunriseHourValue = returnObj3.getString("hour");
				String sunriseMinuteValue = returnObj3.getString("minute");
				
				
				JSONObject returnObj4 = rootObj.getJSONObject("moon_phase").getJSONObject("sunset");
				
				String sunsetHourValue = returnObj4.getString("hour");
				String sunsetMinuteValue = returnObj4.getString("minute");
				
				
				// creates an array list
				List<Triple> triples = new ArrayList<Triple>();
				
				// create an event instance for found weather
				// create triples
				final Literal percentIlluminatedObject = Literals.createTyped(percentIlluminatedValue, Literals.XSD.DECIMAL);
				final Literal ageOfMoonObject = Literals.createTyped(ageOfMoonValue, Literals.XSD.DECIMAL);				
				final Literal currentHourObject = Literals.createTyped(currentHourValue, Literals.XSD.DECIMAL);
				final Literal currentMinuteObject = Literals.createTyped(currentMinuteValue, Literals.XSD.DECIMAL);
				final Literal sunriseHourObject = Literals.createTyped(sunriseHourValue, Literals.XSD.DECIMAL);
				final Literal sunriseMinuteObject = Literals.createTyped(sunriseMinuteValue, Literals.XSD.DECIMAL);				
				final Literal sunsetHourObject = Literals.createTyped(sunsetHourValue, Literals.XSD.DECIMAL);
				final Literal sunsetMinuteObject = Literals.createTyped(sunsetMinuteValue, Literals.XSD.DECIMAL);
				
	
				// Adds triples (subject, predicate, object) to the array list
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Literals.RDF.TYPE, Predicates.TYPE));
				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.PERCENT_ILLUMINATED, percentIlluminatedObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.AGE_OF_MOON, ageOfMoonObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CURRENT_HOUR, currentHourObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CURRENT_MINUTE, currentMinuteObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.SUNRISE_HOUR, sunriseHourObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.SUNRISE_MINUTE, sunriseMinuteObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.SUNSET_HOUR, sunsetHourObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.SUNSET_MINUTE, sunsetMinuteObject));
				
				// returns the array list with all the requested information
				return ProducerBase.fold(triples);

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Main-method that starts the MoonProducer and asks for the country and city to get information about.
	 * 
	 * @param args command line parameter
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		// create new WeatherProducer
		MoonProducer mp = new MoonProducer(msgService);
		
		// create windows to choose the country and city you want to search 
		mp.LANDNAME = JOptionPane.showInputDialog("Enter a country to be searched for on wunderground:", mp.LANDNAME);
		mp.CITYNAME = JOptionPane.showInputDialog("Enter a city name to be searched for on wunderground:", mp.CITYNAME);		
				
		// start producer
		mp.start();		
	}
}