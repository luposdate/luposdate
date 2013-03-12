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
package lupos.event.producers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.ProducerBase;
import lupos.event.ProducerBaseNoDuplicates;
import lupos.event.communication.SerializingMessageService;
import lupos.event.communication.TcpConnectInfo;
import lupos.event.communication.TcpMessageTransport;
import lupos.event.util.Literals;

/**
 * Producer to report maximum delays for (G-G-G-German) interregional trains,
 * obtained from Zugmonitor API.
 */
public class DBDelayProducer extends ProducerBaseNoDuplicates {

	// Zugmonitor API URLs.
	private static final String TRAIN_URL_BASE = "http://zugmonitor.sz.de/api/trains/";
	private static final String STATION_URL = "http://zugmonitor.sueddeutsche.de/api/stations";

	// DBDelayProducer namespace and type
	public static final String NAMESPACE = "http://localhost/events/DB/";
	public final static URILiteral TYPE = Literals.createURI(NAMESPACE, "TrainDBEvent");

	// event predicates
	public static final URILiteral TRAIN_NAME = Literals.createURI(NAMESPACE, "train_name");
	public static final URILiteral STATION_NAME = Literals.createURI(NAMESPACE, "station_name");
	public static final URILiteral LATITUDE = Literals.createURI(NAMESPACE, "latitude");
	public static final URILiteral LONGITUDE = Literals.createURI(NAMESPACE, "longitude");
	public static final URILiteral DELAY = Literals.createURI(NAMESPACE, "delay");
	public static final URILiteral DELAY_CAUSE = Literals.createURI(NAMESPACE, "delay_cause");

	/**
	 * Station list: <station id, station info>
	 */
	private Map<Integer, StationInfo> stations = new HashMap<Integer, StationInfo>();

	/**
	 * Station informations. Obviously.
	 */
	private class StationInfo {
		public int id = -1;
		public String name = new String();
		public double latitude;
		public double longitude;
	}

	/**
	 * Storing the delay in minutes and the delay cause for a station (only
	 * useful in conjunction with a train).
	 */
	private class StationDelayInfo {
		public StationInfo station = null;
		public int delayMinutes;
		public String delayCause = new String();
	}

	/**
	 * Train informations we're interested in.
	 */
	private class TrainInfo {
		public int id = -1;
		public String name = new String();
		public int maxDelay = 0;
		public List<StationDelayInfo> delayPerStation = new ArrayList<StationDelayInfo>();
	}

	/**
	 * Download textual content from a given URL via HTTP GET.
	 * 
	 * @param strUrl
	 *            URL to download (textual) content from.
	 * @return Content downloaded from the given URL, obtained via HTTP GET.
	 */
	private String getHttp(String strUrl) {
		String result = "";
		try {
			URL url = new URL(strUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				result += line;
			}
			reader.close();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Parse JSON-encoded station data from Zugmonitor API.
	 * 
	 * @param jsonStr
	 *            JSON-encoded station information data (from Zugmonitor API).
	 * @return List of StationInfos, one for each station.
	 */
	private Map<Integer, StationInfo> parseStationDataSets(String jsonStr) {
		Map<Integer, StationInfo> result = new HashMap<Integer, StationInfo>();
		try {
			// parse JSON-encoded string
			JSONObject obj = new JSONObject(jsonStr);

			// process each key (keys = station IDs)
			@SuppressWarnings("unchecked")
			Iterator<String> it = obj.keys();
			while (it.hasNext()) {
				String key = it.next();
				StationInfo si = parseStationDataSet(obj, key);
				if (si != null) {
					result.put(si.id, si);
				}
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Parse a single station's information to a StationInfo object.
	 * 
	 * @param rootObj
	 *            (Root) JSONObject containing all the parsed information from
	 *            Zugmonitor API.
	 * @param trainKey
	 *            Station ID, i.e. the key of the corresponding station's JSON
	 *            object.
	 * @return StationInfo for the specified station.
	 */
	private StationInfo parseStationDataSet(JSONObject rootObj,
			String stationKey) {
		StationInfo result = new StationInfo();

		try {
			result.id = Integer.parseInt(stationKey);
		} catch (NumberFormatException e) {
			return null;
		}

		try {
			JSONObject stationObj = rootObj.getJSONObject(stationKey);

			// get the station's name
			if (stationObj.has("name")) {
				result.name = stationObj.getString("name");
			}

			// get latitude and longitude
			if (stationObj.has("lat")) {
				result.latitude = stationObj.getDouble("lat");
			}

			if (stationObj.has("lon")) {
				result.longitude = stationObj.getDouble("lon");
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Parse JSON-encoded train data from Zugmonitor API.
	 * 
	 * @param jsonStr
	 *            JSON-encoded train information data (from Zugmonitor API).
	 * @return List of TrainInfos, one for each train.
	 */
	private List<TrainInfo> parseTrainDataSets(String jsonStr) {
		// System.out.println(jsonData);
		List<TrainInfo> result = new ArrayList<TrainInfo>();
		try {
			// parse JSON-encoded string
			JSONObject obj = new JSONObject(jsonStr);

			// process each key (keys = train IDs)
			@SuppressWarnings("unchecked")
			Iterator<String> it = obj.keys();
			while (it.hasNext()) {
				String key = it.next();
				result.add(parseTrainDataSet(obj, key));
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Parse a single train's information to a TrainInfo object.
	 * 
	 * @param rootObj
	 *            (Root) JSONObject containing all the parsed information from
	 *            Zugmonitor API.
	 * @param trainKey
	 *            Train ID, i.e. the key of the corresponding train's JSON
	 *            object.
	 * @return TrainInfo for the specified train.
	 */
	private TrainInfo parseTrainDataSet(JSONObject rootObj, String trainKey) {
		TrainInfo result = new TrainInfo();
		result.id = Integer.parseInt(trainKey);

		try {
			JSONObject trainObj = rootObj.getJSONObject(trainKey);

			// get train_nr: the train's name
			if (trainObj.has("train_nr")) {
				result.name = trainObj.getString("train_nr");
			}

			// parse station infos to compute the train's maximum delay
			if (trainObj.has("stations")) {
				JSONArray stationArray = trainObj.getJSONArray("stations");
				for (int i = 0; i < stationArray.length(); i++) {
					StationDelayInfo sdi = getStationDelayInfo(stationArray
							.getJSONObject(i));
					result.maxDelay = Math.max(result.maxDelay,
							sdi.delayMinutes);
					result.delayPerStation.add(sdi);
				}
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Extract the StationDelayInfo from a station's JSONObject (parsed from
	 * Zugmonitor API data).
	 * 
	 * @param stationObj
	 *            The station's JSONObject.
	 * @return The corresponding StationDelayInfo.
	 */
	private StationDelayInfo getStationDelayInfo(JSONObject stationObj) {
		StationDelayInfo result = new StationDelayInfo();
		try {
			if (stationObj.has("station_id")) {
				int stationID = stationObj.getInt("station_id");

				if (this.stations.containsKey(stationID)) {
					result.station = this.stations.get(stationID);
				} else {
					System.err.println("Dammit! No station with id "
							+ stationID + " known!");
				}

				if (stationObj.has("delay")) {
					result.delayMinutes = stationObj.getInt("delay");

					if (stationObj.has("delay_cause")) {
						result.delayCause = stationObj.getString("delay_cause");
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Constructor.
	 */
	public DBDelayProducer(SerializingMessageService msgService, int interval) {
		super(msgService, interval);
	}

	/**
	 * Construct a SerializingMessageService, connect it to host:4444 and create
	 * a DBDelayProducer instance with interval 3000.
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = new SerializingMessageService(
				TcpMessageTransport.class);
		msgService.connect(new TcpConnectInfo(
				ProducerBase.askForHostOfBroker(), 4444));

		// start producer
		new DBDelayProducer(msgService, 30000).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.event.ProducerBase#produce()
	 */
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		// build Zugmonitor API url with current date
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String trainURL = TRAIN_URL_BASE + df.format(new Date());

		// retrieve json-encoded train infos (http GET)
		String jsonTrains = getHttp(trainURL);

		// retrieve json-encoded station infos
		String jsonStations = getHttp(STATION_URL);

		// parse json data
		this.stations = parseStationDataSets(jsonStations);
		List<TrainInfo> trainInfos = parseTrainDataSets(jsonTrains);

		// encode to triples
		List<List<Triple>> triplelist = new ArrayList<List<Triple>>();

		// add to list
		for (TrainInfo ti : trainInfos) {
			triplelist.addAll(trainToEvents(ti));
		}

		return (triplelist.size() == 0) ? null : triplelist;
	}

	/**
	 * Encode a TrainInfo object into triples.
	 * 
	 * @param train
	 *            TrainInfo to be encoded into triples.
	 * @return well ... guess what!
	 */
	private List<List<Triple>> trainToEvents(TrainInfo train) {
		List<List<Triple>> result = new ArrayList<List<Triple>>();

		try {
			// the train subject
			Literal subj = LiteralFactory.createAnonymousLiteral("_:t"
					+ train.id);

			// the train's name
			Literal nameObj = LiteralFactory.createLiteral("\"" + train.name
					+ "\"");

			// for each station, compose a new event
			for (StationDelayInfo sdi : train.delayPerStation) {
				List<Triple> event = stationDelayInfoToTriples(subj, sdi);

				// add the event type and the train's name
				event.add(0, new Triple(subj, Literals.RDF.TYPE, TYPE));
				event.add(1, new Triple(subj, TRAIN_NAME, nameObj));

				result.add(event);
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Construct an event (i.e., a List of Triples) with a given subject from a
	 * StationDelayInfo object.
	 * 
	 * @param trainSubj
	 *            Triples' subject; usually identifying the corresponding train.
	 * @param sdi
	 *            StationDelayInfo to be encoded into triples.
	 * @return List of Triples encoding the station's name, the delay at the
	 *         station and the cause of the delay.
	 */
	private List<Triple> stationDelayInfoToTriples(Literal trainSubj,
			StationDelayInfo sdi) {
		List<Triple> result = new ArrayList<Triple>();
		try {
			Literal obj;

			// station name
			obj = LiteralFactory.createLiteral("\"" + sdi.station.name + "\"");
			result.add(new Triple(trainSubj, STATION_NAME, obj));
			
			// latitude, longitude
			obj = Literals.createTyped(sdi.station.latitude + "", Literals.XSD.FLOAT);
			result.add(new Triple(trainSubj, LATITUDE, obj));
			
			obj = Literals.createTyped(sdi.station.longitude + "", Literals.XSD.FLOAT);
			result.add(new Triple(trainSubj, LONGITUDE, obj));

			// delay

			// split delay into days, hours, minutes
			int totalMinutes = sdi.delayMinutes;

			int days = totalMinutes / 1440;
			totalMinutes -= days * 1440;

			int hours = totalMinutes / 60;
			totalMinutes -= hours * 60;

			int minutes = totalMinutes;

			obj = Literals.createDurationLiteral(0, 0, days, hours, minutes, 0);
			result.add(new Triple(trainSubj, DELAY, obj));

			// delay cause
			obj = LiteralFactory.createLiteral("\"" + sdi.delayCause + "\"");
			result.add(new Triple(trainSubj, DELAY_CAUSE, obj));
		} catch (URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return result;
	}
}
