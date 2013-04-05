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
package lupos.event.producer.rsssemantics;

import java.util.*;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.event.communication.SerializingMessageService;
import lupos.event.producer.ProducerBase;
import lupos.event.producer.ProducerBaseNoDuplicates;
import lupos.event.util.Literals;

/**
 * Main class
 */
public class RSSSemanticInterpretationProducer extends ProducerBaseNoDuplicates {
	private static final String feedUrl = "http://www.abendblatt.de/politik/?service=Rss";
	private static final int INTERVAL = 1000;
	private static final String NAMESPACE = "http://localhost/events/RSSSemantics/";
	static RSSReader reader = new RSSReader(feedUrl);
	static Feed feed = reader.readFeed();
	static StoplistReader stop = new StoplistReader();
	static String stoplistpath = "/stoplist.txt";
	static ArrayList<String>[] stoplist = stop.readStoplist(stoplistpath);
	static MessageParser mparse = new MessageParser(stoplist);

	static class Predicates {
		public static final Literal FEED = Literals.createURI(NAMESPACE,
				"FeedToInterpret");
		public static final Literal INTERPRETATION = Literals.createURI(
				NAMESPACE, "DBPediaResult");
	}

	public RSSSemanticInterpretationProducer(
			SerializingMessageService msgService) {
		super(msgService, INTERVAL);
	}

	public static void main(String[] args) throws Exception {
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		RSSSemanticInterpretationProducer rt = new RSSSemanticInterpretationProducer(msgService);
		rt.start();
		rt.produce();

		// Experimental: See Frequency.java for further information
		// ArrayList<String> subtemp = nextMessage.getSubstring();
		// ArrayList<Integer> freqimport = nextMessage.getFrequency();

	}

	/**
	 * Starts interpretation and database querying process for all messages in
	 * the feed, finally initiates triple creation and returns the list of
	 * triple lists to the broker.
	 */
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		List<List<Triple>> result = new LinkedList<List<Triple>>();
		for (FeedMessage message : feed.getMessages()) {
			try {
				DBAnswer nextMessage = mparse.parseMessage(message);
				if (!(message.getDescription() == "")) {
					ArrayList<Triple> preresult = message.generateTriples();
					ArrayList<Triple> dbares;
					if (!(nextMessage == null)) {
						dbares = nextMessage.generateTriples();
						if (!(dbares.isEmpty())) {
							preresult.addAll(dbares);
						}
					}
					result.add(preresult);
				} else {
					continue;
				}
			} catch (Exception e) {
				System.out.println("Fail.");
				e.printStackTrace();
			}
		}
		return result;
	}
}