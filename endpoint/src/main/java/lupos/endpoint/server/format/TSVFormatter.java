package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;

import lupos.datastructures.items.Variable;

import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

public class TSVFormatter extends SeparatorFormatter {

	public TSVFormatter() {
		super("TSV");
	}
	
	@Override
	public void writeSeparator(OutputStream os) throws IOException{
		os.write("\t".getBytes());		
	}
	
	@Override
	public void writeFirstVariableInHead(final OutputStream os, final Variable v)
			throws IOException {
		os.write(v.toString().getBytes());
	}

	@Override
	public String getMIMEType(QueryResult queryResult) {
		if (queryResult instanceof GraphResult) {
			return "application/rdf+xml";
		} else {
			return "text/csv";
		}
	}
}