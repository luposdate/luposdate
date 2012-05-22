package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;

import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

public class CSVFormatter extends SeparatorFormatter {

	public CSVFormatter() {
		super("CSV", "text/csv");
	}
	
	@Override
	public void writeSeparator(OutputStream os) throws IOException{
		os.write(",".getBytes());		
	}

	@Override
	public String getMIMEType(QueryResult queryResult) {
		if (queryResult instanceof GraphResult) {
			return super.getMIMEType(queryResult);
		} else {
			return "text/csv";
		}
	}
}