package lupos.endpoint.client.formatreader;

import java.io.InputStream;

import lupos.datastructures.queryresult.QueryResult;

public abstract class MIMEFormatReader {
	private final String formatName;
	private final String key;
	
	public MIMEFormatReader(final String formatName){
		this(formatName, formatName);
	}
	
	public MIMEFormatReader(final String formatName, final String key){
		this.formatName = formatName;
		this.key = key.toLowerCase();
	}
	
	public abstract String getMIMEType();
	
	public String getName(){
		return this.formatName;
	}
	
	public String getKey(){
		return this.key;
	}

	public abstract QueryResult getQueryResult(final InputStream inputStream);
}
