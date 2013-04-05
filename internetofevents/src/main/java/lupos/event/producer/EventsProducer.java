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

import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.json.*;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.communication.SerializingMessageService;
import lupos.event.util.Literals;
import lupos.event.util.Literals.XSD;
import lupos.event.util.Utils;

/**
 * 
 * @author Christopher Gudat, Guillaume Assaud
 * Search events by city, country, distance or tag from Last.fm
 */

public class EventsProducer extends ProducerBaseNoDuplicates {
	//Query-String:
	//http://ws.audioscrobbler.com/2.0/?method=geo.getevents&location=madrid&api_key=86029e27ac8170ddcb028fbf0c2dc4cf&format=json
	public static final String NAMESPACE = "http://localhost/events/EventSearch/";
	private final static int INTERVAL = 3000;
	private static final String SEARCH_URL = "http://ws.audioscrobbler.com/2.0/?method=geo.getevents&location=";
	private String SEARCH_CITY = "hamburg";
	//API-Key for Last.fm
	private String API_KEY = "86029e27ac8170ddcb028fbf0c2dc4cf";
	//Search radius from current position
	private String SEARCH_DISTANCE = "0";
	//Limit of results
	private String SEARCH_LIMIT = "10";
	//Using a special tag
	private String SEARCH_TAG = "";
	
	private SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.UK);	
	
	public final static URILiteral EVENT_TYPE_OBJECT = Literals.createURI(NAMESPACE, "EventsProducer");
	public final static URILiteral HEADLINER_PREDICATE = Literals.createURI(NAMESPACE, "artist");
	public final static URILiteral ID_PREDICATE = Literals.createURI(NAMESPACE, "id");
	public final static URILiteral TITLE_PREDICATE = Literals.createURI(NAMESPACE, "title");
	public final static URILiteral START_PREDICATE = Literals.createURI(NAMESPACE, "startDate");
	public final static URILiteral NAME_PREDICATE = Literals.createURI(NAMESPACE, "location");
	public final static URILiteral STREET_PREDICATE = Literals.createURI(NAMESPACE, "street");
	public final static URILiteral POSTALCODE_PREDICATE = Literals.createURI(NAMESPACE, "postalcode");
	public final static URILiteral CITY_PREDICATE = Literals.createURI(NAMESPACE, "city");
	public final static URILiteral URL_PREDICATE = Literals.createURI(NAMESPACE, "url");
	public final static URILiteral DESCRIPTION_PREDICATE = Literals.createURI(NAMESPACE, "description");
	public final static URILiteral IMAGE_PREDICATE = Literals.createURI(NAMESPACE, "image");
	
	public EventsProducer(SerializingMessageService msgService) {
		super(msgService, INTERVAL);
	}
	
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		try {			
			//Search query URL for Last.fm
			String jsonStr = Utils.httpGet(SEARCH_URL + URLEncoder.encode(this.SEARCH_CITY, "UTF-8") + "&api_key="+this.API_KEY + "&format=json"+"&distance="+ this.SEARCH_DISTANCE + "&limit=" + this.SEARCH_LIMIT + "&tag=" + this.SEARCH_TAG);
			//Build object from query string
			JSONObject rootObj = new JSONObject(jsonStr);
			JSONObject resultsObject = rootObj.getJSONObject("events");
			JSONArray eventArray = resultsObject.getJSONArray("event");
			
			//Create list of triples
			List<List<Triple>> events = new ArrayList<List<Triple>>();
			
			//Create an instance for each event			
			for(int i=0; i<eventArray.length(); i++) {	
				List<Triple> triples = new ArrayList<Triple>();
				JSONObject jsonObj = eventArray.getJSONObject(i);
				
				//Get every parameter from the API
				long id = jsonObj.getLong("id");
				String title = jsonObj.getString("title");
				String startDate = jsonObj.getString("startDate");
				String description = jsonObj.getString("description");
				URL url = new URL(jsonObj.getString("url"));
				
				//convert string to date
				this.date.setLenient(false);
				Date date1 = this.date.parse(startDate);				

				//Large image
				JSONArray imageArray = jsonObj.getJSONArray("image");
				JSONObject imageObject = imageArray.getJSONObject(3);
				String image = imageObject.getString("#text");
				
				JSONObject artistsObject = jsonObj.optJSONObject("artists");
				String headliner = artistsObject.getString("headliner");
				
				JSONObject venueObject = jsonObj.optJSONObject("venue");
				String location = venueObject.getString("name");				
				
				JSONObject locationObject = venueObject.optJSONObject("location");
				String street = locationObject.getString("street");
				String postalcode = locationObject.getString("postalcode");
				String city = locationObject.getString("city");
				
				//Create an anonymous id
				Literal subject = LiteralFactory.createAnonymousLiteral("<"+id+">");

				//Create triples for each attribute
				triples.add(new Triple(subject, Literals.RDF.TYPE, EVENT_TYPE_OBJECT));
				triples.add(new Triple(subject, HEADLINER_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(headliner)+"\"")));
				triples.add(new Triple(subject, ID_PREDICATE, LiteralFactory.createTypedLiteral("\""+id+"\"", Literals.XSD.LONG)));
				triples.add(new Triple(subject, TITLE_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(title)+"\"")));
				triples.add(new Triple(subject, START_PREDICATE, LiteralFactory.createTypedLiteral("\""+date1+"\"", XSD.DATETIME)));
				triples.add(new Triple(subject, NAME_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(location)+"\"")));
				triples.add(new Triple(subject, STREET_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(street)+"\"")));
				triples.add(new Triple(subject, POSTALCODE_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(postalcode)+"\"")));
				triples.add(new Triple(subject, CITY_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(city)+"\"")));
				triples.add(new Triple(subject, DESCRIPTION_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(description)+"\"")));
				triples.add(new Triple(subject, URL_PREDICATE, LiteralFactory.createTypedLiteral("\""+url+"\"", XSD.ANYURI)));
				triples.add(new Triple(subject, IMAGE_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(image)+"\"")));
				
				events.add(triples);
			}			
			return events;			
		} catch (Exception e) {
			System.out.println("We could not find any upcoming events based on your specified location.");
		}
		return null;
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		//Create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		EventsProducer tsp = new EventsProducer(msgService);
		
		//Show dialog message boxes
		tsp.SEARCH_CITY = JOptionPane.showInputDialog("Enter keyword for city or country:", tsp.SEARCH_CITY);
		tsp.SEARCH_DISTANCE = JOptionPane.showInputDialog("Enter search radius:", tsp.SEARCH_DISTANCE);
		tsp.SEARCH_TAG = JOptionPane.showInputDialog("Enter search tag (genre):", tsp.SEARCH_TAG);
		tsp.API_KEY = JOptionPane.showInputDialog("Enter api-key:", tsp.API_KEY);		
		
		// start producer
		tsp.start();
	}
}
