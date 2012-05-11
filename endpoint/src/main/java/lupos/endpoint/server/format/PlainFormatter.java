package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;

public class PlainFormatter extends Formatter {

	public PlainFormatter() {
		super("Plain", "text/plain");
	}

	@Override
	public void writeResult(OutputStream os, Set<Variable> variables, QueryResult queryResult) throws IOException {
		os.write(queryResult.toString().getBytes());
	}

	@Override
	public String getMIMEType(QueryResult queryResult) {
		return "text/plain";
	}		
}
