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
package lupos.rif;

import junit.framework.TestCase;
import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.operators.index.Indices;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by sebers on 3/11/14.
 *
 * @author groppe
 * @version $Id: $Id
 * @since 1.0
 */
public class BasicIndexRuleEvaluatorTest extends TestCase {


	private CommonCoreQueryEvaluator evaluator;

	/**
	 * <p>setUp.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		MemoryIndexQueryEvaluator memoryIndexQueryEvaluator = new MemoryIndexQueryEvaluator();
		memoryIndexQueryEvaluator.setupArguments();
		memoryIndexQueryEvaluator.getArgs().set("result", QueryResult.TYPE.MEMORY);
		memoryIndexQueryEvaluator.getArgs().set("codemap", LiteralFactory.MapType.TRIEMAP);
		memoryIndexQueryEvaluator.getArgs().set("distinct", CommonCoreQueryEvaluator.DISTINCT.HASHSET);
		memoryIndexQueryEvaluator.getArgs().set("join",CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);
		memoryIndexQueryEvaluator.getArgs().set("optional",CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);
		memoryIndexQueryEvaluator.getArgs().set("type", "Turtle");
		memoryIndexQueryEvaluator.getArgs().set("datastructure", Indices.DATA_STRUCT.HASHMAP);

		BasicIndexRuleEvaluator basicIndexRuleEvaluator = new BasicIndexRuleEvaluator(memoryIndexQueryEvaluator);
		basicIndexRuleEvaluator.prepareInputData("../gui/src/main/resources/data/sp2b_demo.n3");
		evaluator = basicIndexRuleEvaluator.getEvaluator();

	}

	/**
	 * <p>tearDown.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	@After
	public void tearDown() throws Exception {

	}

	/**
	 * <p>testInsertPurgeAndInsert.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	@Test
	public void testInsertPurgeAndInsert() throws Exception {
		{
			QueryResult selectResult = evaluator.getResult(
					"SELECT ?s ?p ?o " +
							"WHERE {?s ?p ?o.}");
			assertEquals(700, selectResult.size());
		}
		insertTriple();
		{
			QueryResult selectResult = evaluator.getResult(
					"SELECT ?s ?p ?o " +
							"WHERE {?s ?p ?o.}");
			assertEquals(701, selectResult.size());
		}
		printInformation();
		deleteTriple();
		{
			QueryResult selectResult = evaluator.getResult(
					"SELECT ?s ?p ?o " +
							"WHERE {?s ?p ?o.}");
			assertEquals(700, selectResult.size());
		}
		insertTriple();
		{
			QueryResult selectResult = evaluator.getResult(
					"SELECT ?s ?p ?o " +
							"WHERE {?s ?p ?o.}");
			assertEquals(701, selectResult.size());
		}

	}

	/**
	 * <p>insertTriple.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void insertTriple() throws Exception {
		String query = "INSERT DATA {<http://www.auto-nomos.de/ontologies/query-driven-semantic-data-cloud-examples#equippedcarRight2>\n" +
				"      <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.auto-nomos.de/ontologies/query-driven-data-clouds#EquippedVehicle> }";
		evaluator.getResult(query);
	}

	/**
	 * <p>deleteTriple.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void deleteTriple() throws Exception {
		String query = "" +
				"DELETE {?s ?p ?o} " +
				"WHERE " +
				"{" +
				"  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.auto-nomos.de/ontologies/query-driven-data-clouds#EquippedVehicle> ." +
				"FILTER ( (?s = <http://www.auto-nomos.de/ontologies/query-driven-semantic-data-cloud-examples#equippedcarRight2>) ). " +
				" ?s ?p ?o. "+
				"}";
		evaluator.getResult(query);
	}

	private void printInformation() throws Exception {

		QueryResult selectResult = evaluator.getResult(
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
						"PREFIX hdc: <http://www.auto-nomos.de/ontologies/query-driven-data-clouds#>\n" +
						"SELECT ?s ?p ?o " +
						"WHERE {" +
						"?s ?p ?o. " +
						"FILTER ( (?s = <http://www.auto-nomos.de/ontologies/query-driven-semantic-data-cloud-examples#equippedcarRight2>) ) }");

		final Collection<Bindings> vehicleInformation =selectResult.getCollection();
		for (Bindings bindings : vehicleInformation) {
			System.out.println(bindings.toString());
		}
	}
}
