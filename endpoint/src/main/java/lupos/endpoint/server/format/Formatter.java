package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;

public abstract class Formatter {
	private final String formatName;
	private final String key;
	
	public Formatter(final String formatName){
		this(formatName, formatName);
	}
	
	public Formatter(final String formatName, final String key){
		this.formatName = formatName;
		this.key = key.toLowerCase();
	}
	
	public abstract String getMIMEType(QueryResult queryResult);
	
	public String getName(){
		return this.formatName;
	}
	
	public String getKey(){
		return this.key;
	}
	
	public abstract void writeResult(final OutputStream os, Set<Variable> variables, final QueryResult queryResult) throws IOException;
}