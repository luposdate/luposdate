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

import lupos.datastructures.queryresult.QueryResult;

/**
 * Builds SPARQL 1.1 query strings and queries the database.
 * 
 */
public class QueryBuilder {

	public QueryResult query(String item, String filterBy) {
		String startstring = "PREFIX : <http://dbpedia.org/resource/>"
				+ "PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
				+ "PREFIX dcterms: <http://purl.org/dc/terms/> ";
		String where = " WHERE { SERVICE <http://dbpedia.org/sparql/> { ";
		String askPersonString = "ASK where { SERVICE <http://dbpedia.org/sparql/> { ?person foaf:surname '"
				+ item
				+ "'@en ."
				+ "?person dbo:thumbnail ?thumbnail;"
				+ "rdfs:label ?label."
				+ "FILTER(LANG(?label) = 'de')."
				+ "?person dbo:birthDate ?birthDate;"
				+ "dbo:birthPlace ?birthPlace ."
				+ "?person dcterms:subject ?subject;"
				+ "foaf:isPrimaryTopicOf ?wikiarticle;"
				+ "rdfs:comment ?comment."
				+ "FILTER (LANG(?comment) = 'de').}}";
		String personString1 = "SELECT ?name ?thumbnail ?label ?birthDate ?birthPlace ?deathDate ?subject ?wikiarticle ?comment";
		String personString2 = "?person foaf:surname '" + item + "'@en;"
				+ "foaf:name ?name."
				+ "OPTIONAL{ ?person dbo:thumbnail ?thumbnail} "
				+ "?person rdfs:label ?label." + "FILTER(LANG(?label) = 'de')."
				+ "?person dbo:birthDate ?birthDate;"
				+ "dbo:birthPlace ?birthPlace ."
				+ "OPTIONAL { ?person dbo:deathDate ?deathDate}"
				+ "?person dcterms:subject ?subject;"
				+ "foaf:isPrimaryTopicOf ?wikiarticle;"
				+ "rdfs:comment ?comment." + "FILTER (LANG(?comment) = 'de').";
		String askPlaceString = "ASK WHERE { SERVICE <http://dbpedia.org/sparql/> { :"
				+ item
				+ " rdfs:label ?label."
				+ "FILTER(lang(?label) = 'de'). "
				+ ":"
				+ item
				+ " rdfs:comment ?comment."
				+ "FILTER(lang(?comment) = 'de')."
				+ ":" + item + "  foaf:isPrimaryTopicOf ?wikiarticle. }}";
		String placeString1 = "SELECT ?label ?leader ?leadername ?populationTotal ?areaTotal ?country ?comment ?wikiarticle";
		String placeString2 = ":" + item + " rdfs:label ?label."
				+ "FILTER(lang(?label) = 'de')." + "OPTIONAL { " + ":" + item
				+ " dbo:leader ?leader }" + "OPTIONAL { " + ":" + item
				+ " dbo:leaderName ?leadername }" + "OPTIONAL { :" + item
				+ " dbo:populationTotal ?populationTotal }" + "OPTIONAL { :"
				+ item + " dbo:areaTotal ?areaTotal }" + "OPTIONAL { :" + item
				+ " dbo:country ?country } :" + item
				+ " rdfs:comment ?comment."
				+ "FILTER(lang(?comment) = 'de'). :" + item
				+ " foaf:isPrimaryTopicOf ?wikiarticle.";
		String askThingString = "ASK WHERE { :" + item
				+ " rdfs:label ?label. :" + item + " rdfs:comment ?comment."
				+ "FILTER(LANG(?comment) = 'de'). }";
		String thingString1 = "SELECT ?label ?comment";
		String thingString2 = ":" + item + " rdfs:label ?label."
				+ "FILTER(LANG(?label) = 'de'). :" + item
				+ " rdfs:comment ?comment."
				+ "FILTER(LANG(?comment) = 'de'). :" + item
				+ " foaf:isPrimaryTopicOf ?wikiarticle.";
		String limit = " }} LIMIT 1";
		String outputString = startstring;

		/**
		 * Check if there is a person (place) in the dbpedia database whose
		 * surname (label) contains the search string
		 */
		if (filterBy.equals("askperson")) {
			outputString = outputString + askPersonString;
		} else if (filterBy.equals("askplace")) {
			outputString = outputString + askPlaceString;
		} else if (filterBy.equals("askthing")) {
			outputString = outputString + askThingString;
		} else if (filterBy.equals("person")) {
			outputString = outputString + personString1 + where + personString2
					+ limit;
		} else if (filterBy.equals("place")) {
			outputString = outputString + placeString1 + where + placeString2
					+ limit;
		} else if (filterBy.equals("thing")) {
			outputString = outputString + thingString1 + where + thingString2
					+ limit;
		} else
			return null;
		System.out.println(outputString);

		// niftier, but requires Java v1.7
		// switch(filterBy) {
		// case "askperson":
		// outputString = outputString + askPersonString;
		// break;
		// case "askplace":
		// outputString = outputString + askPlaceString;
		// break;
		// case "askThingString":
		// outputString = outputString + askThingString;
		// break;
		// case "person":
		// outputString = outputString + personString1 + where + personString2
		// + limit;
		// break;
		// case "place":
		// outputString = outputString + placeString1 + where + placeString2
		// + limit;
		// break;
		// case "thing":
		// outputString = outputString + thingString1 + where + thingString2
		// + limit;
		// break;
		// default:
		// outputString = null;
		// break;
		// }

		QueryDB querymaker = new QueryDB(outputString);
		QueryResult answer = querymaker.query();
		return answer;
	}
}
