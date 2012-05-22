package lupos.endpoint.client.formatreader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.QueryResult;

public class JSONFormatReader extends MIMEFormatReader {

	public final static String MIMETYPE = "application/sparql-results+json";

	public JSONFormatReader() {
		super("JSON", JSONFormatReader.MIMETYPE);
	}

	@Override
	public String getMIMEType() {
		return JSONFormatReader.MIMETYPE;
	}

	@Override
	public QueryResult getQueryResult(final InputStream inputStream) {
		try {
			final JSONObject object = new JSONObject(new JSONTokener(new InputStreamReader(inputStream)));
			if(object.has("boolean")){
				boolean b=object.getBoolean("boolean");
				BooleanResult br = new BooleanResult();
				if(b){
					br.add(Bindings.createNewInstance());
				}
				return br;
			} else {
				return QueryResult.createInstance(new Iterator<Bindings>(){
					
					protected int index = 0;
					protected JSONArray bindings = object.getJSONObject("results").getJSONArray("bindings");

					@Override
					public boolean hasNext() {
						return this.index<this.bindings.length();
					}

					@Override
					public Bindings next() {
						if(hasNext()){							
							try {
								Bindings result = getBindings(this.bindings.getJSONObject(this.index));
								this.index++;
								return result;
							} catch (JSONException e) {
								System.err.println(e);
								e.printStackTrace();
								return null;
							}
						} else {
							return null;
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}					
				}); 				
			}
		} catch (JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}		
		return null;
	}

	public static Bindings getBindings(JSONObject oneResult) throws JSONException{
		Bindings luposResult = Bindings.createNewInstance();
		Iterator<String> keysIt = oneResult.keys();
		while(keysIt.hasNext()){
			String var = keysIt.next();
			luposResult.add(new Variable(var), getLiteral(oneResult.getJSONObject(var)));			
		}
		return luposResult;
	}

	public static Literal getLiteral(JSONObject literal) throws JSONException {
		String type = literal.getString("type");
		if(type.compareTo("uri")==0){
			try {
				return LiteralFactory.createURILiteral("<"+literal.getString("value")+">");
			} catch (URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		} else if(type.compareTo("bnode")==0){
			return LiteralFactory.createAnonymousLiteral("_:"+literal.getString("value"));
		} else if(type.compareTo("typed-literal")==0){
			try {
				return LiteralFactory.createTypedLiteral("\""+literal.getString("value")+"\"", "<"+literal.getString("datatype")+">");
			} catch (URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		} else if(type.compareTo("literal")==0){
			if(literal.has("xml:lang")){
				return LiteralFactory.createLanguageTaggedLiteral("\""+literal.getString("value")+"\"", literal.getString("xml:lang"));
			} else {
				return LiteralFactory.createLiteral("\""+literal.getString("value")+"\"");
			}
		} else {
			System.err.println("lupos.testcases.TransformatorQueryResultAndJSON: type of literal unknown: "+type);
		}
		return null;
	}
	
}
