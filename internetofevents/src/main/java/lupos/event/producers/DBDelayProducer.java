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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
	private static final String TRAIN_URL_BASE = "http://zugmonitor.sz.de/api/trains/";

	public static final String NAMESPACE = "http://localhost/events/DB/";
	public final static URILiteral TYPE = Literals.createURI(NAMESPACE, "TrainDBEvent");

	public static final URILiteral NAME = Literals.createURI(NAMESPACE, "name");
	public static final URILiteral MAXDELAY = Literals.createURI(NAMESPACE, "maxdelay");

	/**
	 * Train informations we're interested in.
	 */
	private class TrainInfo {
		public int id = -1;
		public String name;
		public int maxDelay = 0;
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				result += line;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Parse JSON-encoded train data from Zugmonitor API.
	 * 
	 * @param jsonData
	 *            JSON-encoded train information data (from Zugmonitor API).
	 * @return List of TrainInfos, one for each train.
	 */
	private List<TrainInfo> parseTrainDataSets(String jsonData) {
		System.out.println(jsonData);
		List<TrainInfo> result = new LinkedList<TrainInfo>();
		try {
			// parse JSON-encoded string
			JSONObject obj = new JSONObject(jsonData);

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
				result.maxDelay = getMaxDelayFromStations(stationArray);
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Iterate over a JSONArray of station infos (extracted from the info
	 * obtained from Zugmonitor API) to compute the maximum delay over all
	 * stations.
	 * 
	 * @param stationArray
	 *            JSONArray of station infos (from Zugmonitor API).
	 * @return Maximum delay over all contained stations.
	 */
	private int getMaxDelayFromStations(JSONArray stationArray) {
		int result = 0;

		try {
			// iterate over all stations
			for (int i = 0; i < stationArray.length(); i++) {
				JSONObject stationObj = stationArray.getJSONObject(i);
				// if there is a delay given and if it's greater than the
				// current maximum, store it
				if (stationObj.has("delay")) {
					result = Math.max(result, stationObj.getInt("delay"));
				}
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return result;
	}

	public DBDelayProducer(SerializingMessageService msgService, int interval) {
		super(msgService, interval);
	}

	/**
	 * Construct a SerializingMessageService, connect it to host:4444 and
	 * create a DBDelayProducer instance with interval 3000.
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = new SerializingMessageService(TcpMessageTransport.class);
		msgService.connect(new TcpConnectInfo(ProducerBase.askForHostOfBroker(), 4444));

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
		String url = TRAIN_URL_BASE + df.format(new Date());

		// retrieve json-encoded content (http GET)
		String jsonContent = getHttp(url);

		// parse it
		List<TrainInfo> trainInfos = parseTrainDataSets(jsonContent);

		// encode to triples
		List<List<Triple>> triplelist = new ArrayList<List<Triple>>();

		// add to list
		for (TrainInfo ti : trainInfos) {
			triplelist.add(trainToTriple(ti));
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
	protected List<Triple> trainToTriple(TrainInfo train) {
		List<Triple> triplelist = new ArrayList<Triple>();

		try {
			// the subject
			Literal subj = LiteralFactory.createAnonymousLiteral("_:t" + train.id);
			triplelist.add(new Triple(subj, Literals.RDF.TYPE, TYPE));

			// the train's name (string)
			Literal obj;
			obj = LiteralFactory.createLiteral("\"" + train.name + "\"");
			triplelist.add(new Triple(subj, NAME, obj));

			// the train's maximum delay:
			int maxDelay = train.maxDelay;

			int days = maxDelay / 1440;
			maxDelay -= days * 1440;

			int hours = maxDelay / 60;
			maxDelay -= hours * 60;

			int minutes = maxDelay;

			obj = Literals.createDurationLiteral(0, 0, days, hours, minutes, 0);
			triplelist.add(new Triple(subj, MAXDELAY, obj));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return triplelist;
	}
}