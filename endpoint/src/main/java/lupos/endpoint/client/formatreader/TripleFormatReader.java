package lupos.endpoint.client.formatreader;

import java.io.InputStream;

import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.tripleoperator.TripleConsumer;

public class TripleFormatReader extends MIMEFormatReader {

	protected final String formatParameter;
	
	public TripleFormatReader(final String formatName, final String mimetype, final String formatParameter) {
		super(formatName, mimetype);
		this.formatParameter = formatParameter; 
	}

	@Override
	public String getMIMEType() {
		return this.getKey();
	}

	@Override
	public QueryResult getQueryResult(final InputStream inputStream) {
		
		final GraphResult result = new GraphResult();
		
		try {
			CommonCoreQueryEvaluator.readTriples(this.formatParameter, inputStream, 
					new TripleConsumer(){
						@Override
						public void consume(Triple triple) {
							result.addGraphResultTriple(triple);
						}			
			});
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		return result;
	}

}
