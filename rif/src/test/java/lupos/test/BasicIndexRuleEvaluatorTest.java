package lupos.test;

import java.util.Collection;

import junit.framework.TestCase;
import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.rif.BasicIndexRuleEvaluator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by sebers on 3/11/14.
 */
public class BasicIndexRuleEvaluatorTest extends TestCase {


	private CommonCoreQueryEvaluator evaluator;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		final MemoryIndexQueryEvaluator memoryIndexQueryEvaluator = new MemoryIndexQueryEvaluator();
		memoryIndexQueryEvaluator.setupArguments();
		memoryIndexQueryEvaluator.getArgs().set("result", QueryResult.TYPE.MEMORY);
		memoryIndexQueryEvaluator.getArgs().set("codemap", LiteralFactory.MapType.TRIEMAP);
		memoryIndexQueryEvaluator.getArgs().set("distinct", CommonCoreQueryEvaluator.DISTINCT.HASHSET);
		memoryIndexQueryEvaluator.getArgs().set("join",CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);
		memoryIndexQueryEvaluator.getArgs().set("optional",CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);
		memoryIndexQueryEvaluator.getArgs().set("type", "Turtle");
		memoryIndexQueryEvaluator.getArgs().set("datastructure", Indices.DATA_STRUCT.HASHMAP);

		final BasicIndexRuleEvaluator basicIndexRuleEvaluator = new BasicIndexRuleEvaluator(memoryIndexQueryEvaluator);
		basicIndexRuleEvaluator.prepareInputData("C:/luposdate-master/rif/src/main/resources/query-driven-data-clouds.ttl");
		this.evaluator = basicIndexRuleEvaluator.getEvaluator();

	}

	@Override
	@After
	public void tearDown() throws Exception {

	}


	@Test
	public void testInsertPurgeAndInsert() throws Exception {
		this.printInformation();
		{
			final QueryResult selectResult = this.evaluator.getResult(
					"SELECT ?s ?p ?o " +
							"WHERE {?s ?p ?o.}");
			assertEquals(436, selectResult.size());
		}
		this.insertTriple();
		this.printInformation();
		{
			final QueryResult selectResult = this.evaluator.getResult(
					"SELECT ?s ?p ?o " +
							"WHERE {?s ?p ?o.}");
			assertEquals(437, selectResult.size());
		}
		this.deleteTriple();
		this.printInformation();
		{
			final QueryResult selectResult = this.evaluator.getResult(
					"SELECT ?s ?p ?o " +
							"WHERE {?s ?p ?o.}");
			assertEquals(436, selectResult.size());
		}
		this.insertTriple();
		this.printInformation();
		{
			final QueryResult selectResult = this.evaluator.getResult(
					"SELECT ?s ?p ?o " +
							"WHERE {?s ?p ?o.}");
			assertEquals(437, selectResult.size());
		}

	}

	public void insertTriple() throws Exception {
		final String query = "INSERT DATA {<http://www.auto-nomos.de/ontologies/query-driven-semantic-data-cloud-examples#equippedcarRight2>\n" +
				"      <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.auto-nomos.de/ontologies/query-driven-data-clouds#EquippedVehicle> }";
		this.evaluator.getResult(query);
	}

	public void deleteTriple() throws Exception {
		final String query = "" +
				"DELETE DATA {<http://www.auto-nomos.de/ontologies/query-driven-semantic-data-cloud-examples#equippedcarRight2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.auto-nomos.de/ontologies/query-driven-data-clouds#EquippedVehicle>} ";
		this.evaluator.getResult(query);
	}

	private void printInformation() throws Exception {

		final QueryResult selectResult = this.evaluator.getResult(
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
						"PREFIX hdc: <http://www.auto-nomos.de/ontologies/query-driven-data-clouds#>\n" +
						"SELECT ?s ?p ?o " +
						"WHERE {" +
						"?s ?p ?o. " +
						"FILTER ( (?s = <http://www.auto-nomos.de/ontologies/query-driven-semantic-data-cloud-examples#equippedcarRight2>) ) }");

		final Collection<Bindings> vehicleInformation =selectResult.getCollection();
		for (final Bindings bindings : vehicleInformation) {
			System.out.println(bindings.toString());
		}

		System.out.println(this.evaluator.getResult("ASK {<http://www.auto-nomos.de/ontologies/query-driven-semantic-data-cloud-examples#equippedcarRight2><http://www.w3.org/1999/02/22-rdf-syntax-ns#type><http://www.auto-nomos.de/ontologies/query-driven-data-clouds#EquippedVehicle>}"));
	}
}