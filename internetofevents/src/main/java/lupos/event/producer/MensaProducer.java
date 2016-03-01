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
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.communication.SerializingMessageService;
import lupos.event.communication.TcpConnectInfo;
import lupos.event.communication.TcpMessageTransport;
import lupos.event.util.Literals;

/**
 * Produce the daily meal info from the refectory of university of luebeck. The
 * Casino is not supported!
 *
 * @author groppe
 * @version $Id: $Id
 */
public class MensaProducer extends ProducerBaseNoDuplicates {

	// event predicates
	private static final String MENSA_URL_BASE = "http://www.studentenwerk-s-h.de/seiten_essen/plan_mensa_luebeck.html";

	// MensaProducer namespace and type
	/** Constant <code>NAMESPACE="http://localhost/events/Mensa/"</code> */
	public static final String NAMESPACE = "http://localhost/events/Mensa/";
	/** Constant <code>TYPE</code> */
	public final static URILiteral TYPE = Literals.createURI(NAMESPACE, "MensaEvent");

	// event predicates
	/** Constant <code>NAME</code> */
	public static final URILiteral NAME = Literals.createURI(NAMESPACE, "name");
	/** Constant <code>PRICE</code> */
	public static final URILiteral PRICE = Literals.createURI(NAMESPACE, "price");

	/**
	 * A class for a meal with a meal-id, name and price of the meal.
	 * 
	 */
	private class MealInfo {
		public int id;
		public String name;
		public double price;
	}

	/**
	 * @return List of MealInfos extracted from the Mensa's web page. Like a
	 *         boss. True story. No shit.
	 */
	private List<MealInfo> parseMensaSite() {
		List<MealInfo> result = null;

		try {
			Document doc = Jsoup.connect(MENSA_URL_BASE).get();
			Elements e = doc.getElementsByTag("div");
			Elements divInhaltElements = e.select("#inhalt");
			if (divInhaltElements == null || divInhaltElements.isEmpty()) {
				System.out.println("no div with id=\"inhalt\"!");
			} else {
				Element divInhalt = divInhaltElements.first();
				result = parseContent(divInhalt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Parses the <div id="inhalt"> element of the mensa's web page for meal
	 * info.
	 * 
	 * @param divInhalt
	 *            div element with id="inhalt".
	 * @return List of found MealInfos.
	 */
	private List<MealInfo> parseContent(Element divInhalt) {
		List<MealInfo> result = new LinkedList<MensaProducer.MealInfo>();

		// 0-based day, 0: Monday
		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;

		// we only work Monday through Friday
		if (day < 0 || day > 4)
			return result;

		try {
			Elements allTD = divInhalt.getElementsByTag("td");
			Elements allMeals = allTD
					.select("[class~=(schrift_gerichte|schrift_spezial)]");

			int mealDay = 0;
			int line = 1;
			int id = 0;
			for (int i = 0; i < allMeals.size(); i++) {
				if (allMeals.get(i).text().startsWith("="))
					break;

				if (mealDay == day) {
					System.out
							.print((line % 2) == 1 ? (allMeals.get(i).text() + ": ")
									: (allMeals.get(i).text() + "\n"));

					if ((line % 2) == 1) {
						MealInfo meal = new MealInfo();
						meal.id = id++;
						meal.name = allMeals.get(i).text();
						result.add(meal);
					} else {
						double price = searchDouble(allMeals.get(i).text(), 0);
						result.get(result.size() - 1).price = price;
					}
				}

				if (mealDay == 4)
					line++;

				mealDay = (mealDay + 1) % 5;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Extract the first double-precision floating point value after a given
	 * start index from a string.
	 * 
	 * @param str
	 *            String to be searched for a double value.
	 * @param startIndex
	 *            Index in str to start searching from.
	 * @return The first double-precision floating point value found in str
	 *         after or at startindex.
	 */
	private Double searchDouble(String str, int startIndex) {
		double ret = 0;
		String str2 = str.replace(",", ".");

		int s = 0;
		for (; s < str.length(); s++) {
			if ((str2.charAt(s) >= '0' && str2.charAt(s) <= '9')) {
				break;
			}
		}

		for (int i = s; i < str2.length(); i++) {
			try {
				ret = Double.parseDouble(str2.substring(s, i + 1));
			} catch (Exception e) {
				break;
			}
		}

		return ret;
	}

	/**
	 * Constructor!
	 *
	 * @param msgService a {@link lupos.event.communication.SerializingMessageService} object.
	 * @param interval a int.
	 */
	public MensaProducer(SerializingMessageService msgService, int interval) {
		super(msgService, interval);
	}

	/**
	 * This is {@link #main(String[])}. It does {@link #main(String[])}.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = new SerializingMessageService(
				TcpMessageTransport.class);
		msgService.connect(new TcpConnectInfo("localhost", 4444));

		// start producer
		new MensaProducer(msgService, 30000).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.event.ProducerBase#produce()
	 */
	/** {@inheritDoc} */
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		List<MealInfo> mealinfolist = parseMensaSite();

		List<List<Triple>> triplelist = new ArrayList<List<Triple>>();

		for (MealInfo m : mealinfolist) {
			triplelist.add(mealinfoToTriple(m));
		}

		for (List<Triple> l : triplelist) {
			for (Triple t : l) {
				System.out.println(t);
			}
		}

		return (triplelist.isEmpty()) ? null : triplelist;
	}

	/**
	 * Encode a {@link MealInfo} into an event.
	 * 
	 * @param meal
	 *            MealInfo to be encoded into triples
	 * @return list of triples. encoding of meal.
	 */
	private List<Triple> mealinfoToTriple(MealInfo meal) {
		List<Triple> triples = new ArrayList<Triple>();

		try {
			Literal subj = LiteralFactory.createAnonymousLiteral("_:t"
					+ meal.id);
			triples.add(new Triple(subj, Literals.RDF.TYPE, TYPE));

			Literal obj;
			obj = LiteralFactory.createLiteral("\"" + meal.name + "\"");
			triples.add(new Triple(subj, NAME, obj));

			obj = LiteralFactory.createLiteral("\"" + meal.price + "\"");
			triples.add(new Triple(subj, PRICE, obj));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return triples;
	}

}
