/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
/**
 * 
 */
package lupos.test;

import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.rif.BasicIndexRuleEvaluator;

/**
 * This class shows how to use the RIF Evaluator to process RIF rules
 * 
 * @author groppe
 */
public class TestRIFEngine {

	private final static String rules = "Document(\n" +
										"	Prefix(rdf     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>)\n" +
										"	Prefix(dc      <http://purl.org/dc/elements/1.1/>)\n" +
										"	Prefix(dcterms <http://purl.org/dc/terms/>)\n" +
										"	Prefix(bench   <http://localhost/vocabulary/bench/>)\n" +
										"	Prefix(xsd     <http://www.w3.org/2001/XMLSchema#>)\n" +
										"\n" +
										"	Group (\n" +
										"		Forall ?yr ?journal (\n" + 
										"			?journal[dcterms:published -> ?yr] :- And(?journal # bench:Journal\n" +
										"			?journal[dc:title -> \"Journal 1 (1940)\"^^xsd:string]\n" +
										"			?journal[dcterms:issued -> ?yr]\n" +
										"			)\n" +
										"  		)\n" +
										" 	)\n" +
										")\n";
	
	private final static String data = 
			"@prefix dc: <http://purl.org/dc/elements/1.1/> .\n" +
			"@prefix dcterms: <http://purl.org/dc/terms/> .\n" +
			"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
			"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
			"@prefix swrc: <http://swrc.ontoware.org/ontology#> .\n" +
			"@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n" +
			"@prefix bench: <http://localhost/vocabulary/bench/> .\n" +
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
			"@prefix person: <http://localhost/persons/> .\n" +
			"bench:Journal rdfs:subClassOf foaf:Document.\n" +
			"bench:Proceedings rdfs:subClassOf foaf:Document.\n" +
			"bench:Inproceedings rdfs:subClassOf foaf:Document.\n" +
			"bench:Article rdfs:subClassOf foaf:Document.\n" +
			"bench:Www rdfs:subClassOf foaf:Document.\n" +
			"bench:MastersThesis rdfs:subClassOf foaf:Document.\n" +
			"bench:PhDThesis rdfs:subClassOf foaf:Document.\n" +
			"bench:Incollection rdfs:subClassOf foaf:Document.\n" +
			"bench:Book rdfs:subClassOf foaf:Document.\n" +
			"<http://localhost/persons/Paul_Erdoes> rdf:type foaf:Person.\n" +
			"<http://localhost/persons/Paul_Erdoes> foaf:name \"Paul Erdoes\"^^xsd:string.\n" +
			"<http://localhost/misc/UnknownDocument> rdf:type foaf:Document.\n" +
			"<http://localhost/publications/journals/Journal1/1940> rdf:type bench:Journal.\n" +
			"<http://localhost/publications/journals/Journal1/1940> swrc:number \"1\"^^xsd:integer.\n" +
			"<http://localhost/publications/journals/Journal1/1940> dc:title \"Journal 1 (1940)\"^^xsd:string.\n" +
			"<http://localhost/publications/journals/Journal1/1940> swrc:volume \"1\"^^xsd:integer.\n" +
			"<http://localhost/publications/journals/Journal1/1940> dcterms:issued \"1940\"^^xsd:integer.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> rdf:type bench:Article.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> bench:abstract \"unmuzzling measles decentralizing hogfishes gantleted richer succories dwelling scrapped prat islanded burlily thanklessly swiveled polers oinked apnea maxillary dumpers bering evasiveness toto teashop reaccepts gunneries exorcises pirog desexes summable heliocentricity excretions recelebrating dually plateauing reoccupations embossers cerebrum gloves mohairs admiralties bewigged playgoers cheques batting waspishly stilbestrol villainousness miscalling firefanged skeins equalled sandwiching bewitchment cheaters riffled kerneling napoleons rifer splinting surmisers satisfying undamped sharpers forbearer anesthetization undermentioned outflanking funnyman commuted lachrymation floweret arcadian acridities unrealistic substituting surges preheats loggias reconciliating photocatalyst lenity tautological jambing sodality outcrop slipcases phenylketonuria grunts venturers valiantly unremorsefully extradites stollens ponderers conditione loathly cancels debiting parrots paraguayans resonates\"^^xsd:string.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> bench:cdrom \"http://www.hogfishes.tld/richer/succories.html\"^^xsd:string.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> rdfs:seeAlso \"http://www.gantleted.tld/succories/dwelling.html\"^^xsd:string.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> swrc:month \"4\"^^xsd:integer.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> swrc:note \"overbites terminals giros podgy vagus kinkiest xix recollected\"^^xsd:string.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> swrc:pages \"110\"^^xsd:integer.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> dc:title \"richer dwelling scrapped\"^^xsd:string.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> foaf:homepage \"http://www.succories.tld/scrapped/prat.html\"^^xsd:string.\n" +
			"<http://localhost/publications/articles/Journal1/1940/Article1> swrc:journal <http://localhost/publications/journals/Journal1/1940>.\n" +
			"_:Adamanta_Schlitt rdf:type foaf:Person.\n";

	public static void main(final String[] args) {
		try {
			
			// set up parameters of evaluators and initialize the evaluators...
			final MemoryIndexQueryEvaluator evaluator = new MemoryIndexQueryEvaluator();
			evaluator.setupArguments();
			evaluator.getArgs().set("result", QueryResult.TYPE.MEMORY);
			evaluator.getArgs().set("codemap", LiteralFactory.MapType.TRIEMAP);
			evaluator.getArgs().set("distinct",CommonCoreQueryEvaluator.DISTINCT.HASHSET);
			evaluator.getArgs().set("join",CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);
			evaluator.getArgs().set("optional",CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);
			evaluator.getArgs().set("datastructure",Indices.DATA_STRUCT.HASHMAP);
			evaluator.init();
			final BasicIndexRuleEvaluator rifEvaluator = new BasicIndexRuleEvaluator(evaluator);
			
			LinkedList<URILiteral> dataIRIs=new LinkedList<URILiteral>();
			dataIRIs.add(LiteralFactory.createStringURILiteral("<inlinedata:"+data+">"));
			rifEvaluator.prepareInputData(dataIRIs, new LinkedList<URILiteral>());
						
			System.out.println("Evaluate rules...");
			
			// evaluate rules and print out the result!
			evaluateQueryAndPrintOut(rifEvaluator, rules);
			
			
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private final static void evaluateQueryAndPrintOut(
			final QueryEvaluator evaluator, final String query)
			throws Exception {
		// evaluate query:
		final QueryResult qr = evaluator.getResult(query);
		
		System.out.println(qr);

		// use oneTimeIterator() whenever the result need to
		// be iterated only once, otherwise iterator()...
		final Iterator<Bindings> it_query = qr.oneTimeIterator();
		while (it_query.hasNext()) {
			// get next solution of the query...
			final Bindings bindings = it_query.next();
			// print out the bound values of all bindings!
			StringBuilder result=new StringBuilder("{");
			boolean first=true;
			for (final Variable v : bindings.getVariableSet()) {
				if(first){
					first=false;
				} else {
					result.append(", ");
				}
				result.append(v);
				result.append('=');
				result.append(bindings.get(v));
			}
			result.append("}");
			System.out.println(result.toString());
		}
	}
}
