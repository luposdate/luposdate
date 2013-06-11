/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.endpoint.client.formatreader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.misc.util.ImmutableIterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONFormatReader extends DefaultMIMEFormatReader {

	public final static String MIMETYPE = "application/sparql-results+json";

	private final boolean writeQueryTriples;

	public JSONFormatReader(final boolean writeQueryTriples) {
		super("JSON", JSONFormatReader.MIMETYPE+(writeQueryTriples?"+querytriples":""));
		this.writeQueryTriples = writeQueryTriples;
	}

	public JSONFormatReader(){
		this(false);
	}

	@Override
	public String getMIMEType() {
		return JSONFormatReader.MIMETYPE+(this.writeQueryTriples?"+querytriples":"");
	}

	@Override
	public QueryResult getQueryResult(final InputStream inputStream) {
		try {
			final JSONObject object = new JSONObject(new JSONTokener(new InputStreamReader(inputStream)));
			if(object.has("boolean")){
				final boolean b=object.getBoolean("boolean");
				final BooleanResult br = new BooleanResult();
				if(b){
					br.add(Bindings.createNewInstance());
				}
				return br;
			} else {
				return QueryResult.createInstance(new ImmutableIterator<Bindings>(){

					protected int index = 0;
					protected JSONArray bindings = object.getJSONObject("results").getJSONArray("bindings");

					@Override
					public boolean hasNext() {
						return this.index<this.bindings.length();
					}

					@Override
					public Bindings next() {
						if(this.hasNext()){
							try {
								final Bindings result = getBindings(this.bindings.getJSONObject(this.index));
								this.index++;
								return result;
							} catch (final JSONException e) {
								System.err.println(e);
								e.printStackTrace();
								return null;
							}
						} else {
							return null;
						}
					}
				});
			}
		} catch (final JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	public static Bindings getBindings(final JSONObject oneResult) throws JSONException{
		final Bindings luposResult = Bindings.createNewInstance();
		final Iterator<String> keysIt = oneResult.keys();
		while(keysIt.hasNext()){
			final String var = keysIt.next();
			if(var.compareTo("<Query-Triples>")!=0){
				luposResult.add(new Variable(var), getLiteral(oneResult.getJSONObject(var)));
			} else {
				// This JSONObject contains the query-triples!
				// This is no standard and a proprietary feature of LUPOSDATE!
				final JSONArray triples = oneResult.getJSONArray(var);
				for(int i=0; i<triples.length(); i++){
					final JSONObject jsonTriple = triples.getJSONObject(i);
					luposResult.addTriple(new Triple(getLiteral(jsonTriple.getJSONObject("subject")), getLiteral(jsonTriple.getJSONObject("predicate")), getLiteral(jsonTriple.getJSONObject("object"))));
				}
			}
		}
		return luposResult;
	}

	public static Literal getLiteral(final JSONObject literal) throws JSONException {
		final String type = literal.getString("type");
		if(type.compareTo("uri")==0){
			try {
				return LiteralFactory.createURILiteral("<"+literal.getString("value")+">");
			} catch (final URISyntaxException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		} else if(type.compareTo("bnode")==0){
			return LiteralFactory.createAnonymousLiteral("_:"+literal.getString("value"));
		} else if(type.compareTo("typed-literal")==0){
			try {
				return LiteralFactory.createTypedLiteral("\""+literal.getString("value")+"\"", "<"+literal.getString("datatype")+">");
			} catch (final URISyntaxException e) {
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
