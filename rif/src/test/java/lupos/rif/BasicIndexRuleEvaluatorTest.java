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
 */
public class BasicIndexRuleEvaluatorTest extends TestCase {


	private CommonCoreQueryEvaluator evaluator;

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
		basicIndexRuleEvaluator.prepareInputData("gui/src/main/resources/data/sp2b_demo.n3");
		evaluator = basicIndexRuleEvaluator.getEvaluator();

	}

	@After
	public void tearDown() throws Exception {

	}

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

	public void insertTriple() throws Exception {
		String query = "INSERT DATA {<http://www.auto-nomos.de/ontologies/query-driven-semantic-data-cloud-examples#equippedcarRight2>\n" +
				"      <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.auto-nomos.de/ontologies/query-driven-data-clouds#EquippedVehicle> }";
		evaluator.getResult(query);
	}

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
