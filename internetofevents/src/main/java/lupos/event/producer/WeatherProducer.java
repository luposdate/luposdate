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
package lupos.event.producer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.communication.SerializingMessageService;
import lupos.event.util.Literals;
import lupos.event.util.Utils;

import org.json.JSONObject;

/**
 * Produces events which contain current weather-related information gathered from http://www.wunderground.com.
 *
 * @version $Id: $Id
 */
public class WeatherProducer extends ProducerBaseNoDuplicates {

	// Defines the name of the event
	/** Constant <code>NAMESPACE="http://localhost/events/Weather/"</code> */
	public static final String NAMESPACE = "http://localhost/events/Weather/";
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
		public static final URILiteral TYPE = Literals.createURI(NAMESPACE, "WeatherEvent");	
		
		// information about the current weather
		/** current weather, e.g. clear */
		public static final URILiteral CURRENT_WEATHER = Literals.createURI(NAMESPACE, "currentWeather");
		/** current temperature in °C */
		public static final URILiteral TEMPERATURE = Literals.createURI(NAMESPACE, "temperature");
		/** how the current temperature feels like in °C */
		public static final URILiteral FEELSLIKE = Literals.createURI(NAMESPACE, "feelslike");
		/** relative humidity in percent */
		public static final URILiteral HUMIDITY = Literals.createURI(NAMESPACE, "humidity");
		/** wind force in miles per hour and the wind direction as e.g. ESE (East South East) */
		public static final URILiteral WIND = Literals.createURI(NAMESPACE, "wind");
		/** last actual observation time */
		public static final URILiteral OBSERVATION_TIME = Literals.createURI(NAMESPACE, "observation_time");
		/** time of the request */
		public static final URILiteral CURRENT_TIME = Literals.createURI(NAMESPACE, "current_time");
		/** short form of the local time zone, e.g. CET */
		public static final URILiteral ZT_SHORT = Literals.createURI(NAMESPACE, "zt_short");
		/** long form of the local time zone, e.g. Europe/Berlin */
		public static final URILiteral ZT_LONG = Literals.createURI(NAMESPACE, "zt_long");
		/** wind degree in ° */
		public static final URILiteral WIND_DEGREE = Literals.createURI(NAMESPACE, "windDegree");
		/** wind force in kilometer per hour */
		public static final URILiteral WIND_KPH = Literals.createURI(NAMESPACE, "windKPH");
		/** wind gust force in kilometer per hour */
		public static final URILiteral WIND_GUST_KPH = Literals.createURI(NAMESPACE, "windGustKPH");
		/** air pressure in MB */
		public static final URILiteral PRESSURE_MB = Literals.createURI(NAMESPACE, "pressureMB");
		/** air pressure in IN */
		public static final URILiteral PRESSURE_IN = Literals.createURI(NAMESPACE, "pressureIN");
		/** dew point in °C */
		public static final URILiteral DEWPOINT = Literals.createURI(NAMESPACE, "dewpoint");
		/** heat index in °C */
		public static final URILiteral HEAT_INDEX = Literals.createURI(NAMESPACE, "heatIndex");
		/** windchill temperature in °C */
		public static final URILiteral WINDCHILL = Literals.createURI(NAMESPACE, "windchill");
		/** how far one can see in miles */
		public static final URILiteral VISIBILITY_MI = Literals.createURI(NAMESPACE, "visibilityMI");
		/** how far one can see in kilometer */
		public static final URILiteral VISIBILITY_KM = Literals.createURI(NAMESPACE, "visibilityKM");
		/** solar radiation in watts/m² */
		public static final URILiteral SOLARRADIATION = Literals.createURI(NAMESPACE, "solarRadiation");
		/** ultraviolet radiation */
		public static final URILiteral UV = Literals.createURI(NAMESPACE, "uv");
		/** precipitation per hour */
		public static final URILiteral PRECIP_1HR = Literals.createURI(NAMESPACE, "precip1hr");
		/** precipitation per day */
		public static final URILiteral PRECIP_TODAY = Literals.createURI(NAMESPACE, "precipToday");
		/** icon of the current weather */
		public static final URILiteral ICON = Literals.createURI(NAMESPACE, "icon");
		/** URL of the icon of the current weather */
		public static final URILiteral ICON_URL = Literals.createURI(NAMESPACE, "iconUrl");
		/** URL of the weather forecast */
		public static final URILiteral FORECAST_URL = Literals.createURI(NAMESPACE, "forecastUrl");
		/** URL of the weather history of the searched city */
		public static final URILiteral HISTORY_URL = Literals.createURI(NAMESPACE, "historyUrl");
		/** URL of the weather forecast of another weather station */
		public static final URILiteral OB_URL = Literals.createURI(NAMESPACE, "obUrl");

		// information about the location of the weather station
		/** name of the city of the weather station */
		public static final URILiteral CITY_NAME = Literals.createURI(NAMESPACE, "city_name");
		/** name of the country of the weather station */
		public static final URILiteral COUNTRY_NAME = Literals.createURI(NAMESPACE, "country_name");
		/** code of the country of the weather station */
		public static final URILiteral COUNTRYCODE = Literals.createURI(NAMESPACE, "countryCode");
		/** latitude of the weather station */
		public static final URILiteral LATITUDE = Literals.createURI(NAMESPACE, "lati");
		/** longitude of the weather station */
		public static final URILiteral LONGITUDE = Literals.createURI(NAMESPACE, "longi");
		/** elevation of the weather station */
		public static final URILiteral ELEVATION = Literals.createURI(NAMESPACE, "elev");

		// information about the asked city
		/** name of the asked city */
		public static final URILiteral CURRENT_CITY = Literals.createURI(NAMESPACE, "current_city");
		/** code of the country of the asked country */
		public static final URILiteral COUNTRY2CODE = Literals.createURI(NAMESPACE, "country2Code");
		/** latitude of the asked city */
		public static final URILiteral LATITUDE2 = Literals.createURI(NAMESPACE, "lati2");
		/** longitude of the asked city */
		public static final URILiteral LONGITUDE2 = Literals.createURI(NAMESPACE, "longi2");
		/** elevation of the asked city */
		public static final URILiteral ELEVATION2 = Literals.createURI(NAMESPACE, "elev2");
	}

	
	/**
	 * Constructor of the WeatherProducer.
	 *
	 * @param msgService The message service that the producer should use to communicate.
	 * @throws java.lang.Exception if any.
	 */
	public WeatherProducer(SerializingMessageService msgService) throws Exception {
		super(msgService, INTERVAL);
	}

	
	/**
	 * {@inheritDoc}
	 *
	 * Creates and returns triples with the weather information.
	 */
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		try {		
				// search for query via wunderground API
				// http://api.wunderground.com/api/5f028a996895c1d6/conditions/q/Germany/Lubeck.json
				String jsonStr = Utils.httpGet(API_BASE_URL + this.PROJECTCODE + "/conditions/q/" + this.LANDNAME + "/" + this.CITYNAME + ".json");

				// get results list from json-response
				JSONObject rootObj = new JSONObject(jsonStr);
				
				
				JSONObject returnObj = rootObj.getJSONObject("current_observation");

				String currentWeatherValue = returnObj.getString("weather");
				String temperatureValue = returnObj.getString("temp_c");
				String feelslikeValue = returnObj.getString("feelslike_c");
				String humidityValue = returnObj.getString("relative_humidity");
				String windValue = returnObj.getString("wind_string");
				String observationTimeValue = returnObj.getString("observation_time_rfc822");
				String currentTimeValue = returnObj.getString("local_time_rfc822");
				String ztShortValue = returnObj.getString("local_tz_short");
				String ztLongValue = returnObj.getString("local_tz_long");
				String windDegreeValue = returnObj.getString("wind_degrees");
				String windKPHValue = returnObj.getString("wind_kph");
				String windGustKPHValue = returnObj.getString("wind_gust_kph");
				String pressureMBValue = returnObj.getString("pressure_mb");
				String pressureINValue = returnObj.getString("pressure_in");
				String dewpointValue = returnObj.getString("dewpoint_c");
				String heatIndexValue = returnObj.getString("heat_index_c");
				String windchillValue = returnObj.getString("windchill_c");
				String visibilityMIValue = returnObj.getString("visibility_mi");
				String visibilityKMValue = returnObj.getString("visibility_km");
				String solarRadiationValue = returnObj.getString("solarradiation");
				String uvValue = returnObj.getString("UV");
				String precip1hrValue = returnObj.getString("precip_1hr_metric");
				String precipTodayValue = returnObj.getString("precip_today_metric");
				String iconValue = returnObj.getString("icon");
				String iconUrlValue = "http://icons.wxug.com/i/c/a/" + iconValue +".gif";
				String forecastUrlValue = returnObj.getString("forecast_url");
				String historyUrlValue = returnObj.getString("history_url");
				String obUrlValue = returnObj.getString("ob_url");

				
				JSONObject returnObj2 = rootObj.getJSONObject("current_observation").getJSONObject("observation_location");

				String cityNameValue = returnObj2.getString("full");
				String countryNameValue = returnObj2.getString("country");
				String countryCodeValue = returnObj2.getString("country_iso3166");
				String latiValue = returnObj2.getString("latitude");
				String longiValue = returnObj2.getString("longitude");
				String elevValue = returnObj2.getString("elevation");

				
				JSONObject returnObj3 = rootObj.getJSONObject("current_observation").getJSONObject("display_location");
				
				String currentCityValue = returnObj3.getString("full");
				String country2CodeValue = returnObj3.getString("country_iso3166");
				String lati2Value = returnObj3.getString("latitude");
				String longi2Value = returnObj3.getString("longitude");
				String elev2Value = returnObj3.getString("elevation");
			
				
				// creates an array list
				List<Triple> triples = new ArrayList<Triple>();

				// create an event instance for found weather
				// create triples
				final Literal currentWeatherObject = Literals.createTyped(currentWeatherValue, Literals.XSD.String);
				final Literal temperatureObject = Literals.createTyped(temperatureValue, Literals.XSD.DECIMAL);
				final Literal feelslikeObject = Literals.createTyped(feelslikeValue, Literals.XSD.DECIMAL);
				final Literal humidityObject = Literals.createTyped(humidityValue, Literals.XSD.String);
				final Literal windObject = Literals.createTyped(windValue, Literals.XSD.String);
				final Literal observationTimeObject = Literals.createTyped(observationTimeValue, Literals.XSD.String);
				final Literal currentTimeObject = Literals.createTyped(currentTimeValue, Literals.XSD.String);
				final Literal ztShortObject = Literals.createTyped(ztShortValue, Literals.XSD.String);
				final Literal ztLongObject = Literals.createTyped(ztLongValue, Literals.XSD.String);
				final Literal windDegreeObject = Literals.createTyped(windDegreeValue, Literals.XSD.DECIMAL);
				final Literal windKPHObject = Literals.createTyped(windKPHValue, Literals.XSD.DECIMAL);
				final Literal windGustKPHObject = Literals.createTyped(windGustKPHValue, Literals.XSD.DECIMAL);
				final Literal pressureMBObject = Literals.createTyped(pressureMBValue, Literals.XSD.DECIMAL);
				final Literal pressureINObject = Literals.createTyped(pressureINValue, Literals.XSD.DECIMAL);
				final Literal dewpointObject = Literals.createTyped(dewpointValue, Literals.XSD.DECIMAL);
				final Literal heatIndexObject = Literals.createTyped(heatIndexValue, Literals.XSD.String);
				final Literal windchillObject = Literals.createTyped(windchillValue, Literals.XSD.DECIMAL);
				final Literal visibilityMIObject = Literals.createTyped(visibilityMIValue, Literals.XSD.String);
				final Literal visibilityKMObject = Literals.createTyped(visibilityKMValue, Literals.XSD.String);
				final Literal solarRadiationObject = Literals.createTyped(solarRadiationValue, Literals.XSD.DECIMAL);
				final Literal uvObject = Literals.createTyped(uvValue, Literals.XSD.DECIMAL);
				final Literal precip1hrObject = Literals.createTyped(precip1hrValue, Literals.XSD.DECIMAL);
				final Literal precipTodayObject = Literals.createTyped(precipTodayValue, Literals.XSD.DECIMAL);
				final Literal iconObject = Literals.createTyped(iconValue, Literals.XSD.String);
				final Literal iconUrlObject = Literals.createTyped(iconUrlValue, Literals.XSD.String);
				final Literal forecastUrlObject = Literals.createTyped(forecastUrlValue, Literals.XSD.String);
				final Literal historyUrlObject = Literals.createTyped(historyUrlValue, Literals.XSD.String);
				final Literal obUrlObject = Literals.createTyped(obUrlValue, Literals.XSD.String);

				final Literal cityNameObject = Literals.createTyped(cityNameValue, Literals.XSD.String);
				final Literal countryNameObject = Literals.createTyped(countryNameValue, Literals.XSD.String);
				final Literal countryCodeObject = Literals.createTyped(countryCodeValue, Literals.XSD.String);
				final Literal latiObject = Literals.createTyped(latiValue, Literals.XSD.DECIMAL);
				final Literal longiObject = Literals.createTyped(longiValue, Literals.XSD.DECIMAL);
				final Literal elevObject = Literals.createTyped(elevValue, Literals.XSD.String);

				final Literal currentCityObject = Literals.createTyped(currentCityValue, Literals.XSD.String);
				final Literal country2CodeObject = Literals.createTyped(country2CodeValue, Literals.XSD.String);
				final Literal lati2Object = Literals.createTyped(lati2Value, Literals.XSD.DECIMAL);
				final Literal longi2Object = Literals.createTyped(longi2Value, Literals.XSD.DECIMAL);
				final Literal elev2Object = Literals.createTyped(elev2Value, Literals.XSD.DECIMAL);
				
				
				// Adds triples (subject, predicate, object) to the array list
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Literals.RDF.TYPE, Predicates.TYPE));
				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CURRENT_CITY, currentCityObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.COUNTRY2CODE, country2CodeObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.LATITUDE2, lati2Object));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.LONGITUDE2, longi2Object));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.ELEVATION2, elev2Object));
				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CITY_NAME, cityNameObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.COUNTRY_NAME, countryNameObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.COUNTRYCODE, countryCodeObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.LATITUDE, latiObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.LONGITUDE, longiObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.ELEVATION, elevObject));
					
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CURRENT_TIME, currentTimeObject));	
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.OBSERVATION_TIME, observationTimeObject));	
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.ZT_SHORT, ztShortObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.ZT_LONG, ztLongObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CURRENT_WEATHER, currentWeatherObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.TEMPERATURE, temperatureObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.FEELSLIKE, feelslikeObject));
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.HUMIDITY, humidityObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.WIND, windObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.WIND_DEGREE, windDegreeObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.WIND_KPH, windKPHObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.WIND_GUST_KPH, windGustKPHObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.PRESSURE_MB, pressureMBObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.PRESSURE_IN, pressureINObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.DEWPOINT, dewpointObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.HEAT_INDEX, heatIndexObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.WINDCHILL, windchillObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.VISIBILITY_MI, visibilityMIObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.VISIBILITY_KM, visibilityKMObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.SOLARRADIATION, solarRadiationObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.UV, uvObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.PRECIP_1HR, precip1hrObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.PRECIP_TODAY, precipTodayObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.ICON, iconObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.ICON_URL, iconUrlObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.FORECAST_URL, forecastUrlObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.HISTORY_URL, historyUrlObject));				
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.OB_URL, obUrlObject));
				
				
				// returns the array list with all the requested information
				return ProducerBase.fold(triples);

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

		
	/**
	 * Main-method that starts the WeatherProducer and asks for the country and city to get information about.
	 *
	 * @param args command line parameter
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		// create new WeatherProducer
		WeatherProducer wp = new WeatherProducer(msgService);
		
		// create windows to choose the country and city you want to search 
		wp.LANDNAME = JOptionPane.showInputDialog("Enter a country to be searched for on wunderground:", wp.LANDNAME);
		wp.CITYNAME = JOptionPane.showInputDialog("Enter a city name to be searched for on wunderground:", wp.CITYNAME);		
				
		// start producer
		wp.start();		
	}
		
}
