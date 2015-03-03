
/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.distributed.operator.format;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.VarBucket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class Helper {

	/**
	 * Creates a json object from a given object
	 *
	 * @param t the object to be transformed into an json object
	 * @return the json object representing t, or an empty JSON object if the type of t is not supported
	 * @throws org.json.JSONException if any.
	 * @param <T> a T object.
	 */
	public static<T> JSONObject createJSONObject(final T t) throws JSONException {
		if(t instanceof Item) {
			return Helper.createItemAsJSONObject((Item) t);
		} else if(t instanceof TriplePattern) {
			return Helper.createTriplePatternJSONObject((TriplePattern) t);
		} else if(t instanceof VarBucket) {
			return Helper.createVarBucketJSONObject((VarBucket) t);
		} else {
			System.err.println("lupos.distributed.operator.format.Helper.createJSONObject(...): Unknown type, returning an empty JSON object!");
			return new JSONObject();
		}
	}

	/**
	 * Creates a json object from a map
	 *
	 * @param map the map to be represented by a json object
	 * @return the json object representing the map
	 * @throws org.json.JSONException if any.
	 * @param <K> a K object.
	 * @param <V> a V object.
	 */
	public static<K, V> JSONObject createMapJSONObject(final Map<K, V> map) throws JSONException {
		final JSONObject json = new JSONObject();

		final Collection<JSONObject> entriesJson = new LinkedList<JSONObject>();
		if(map!=null){
			for(final Entry<K, V> entry: map.entrySet()){
				final JSONObject entryJson = new JSONObject();
				entryJson.put("key", Helper.createJSONObject(entry.getKey()));
				entryJson.put("value", Helper.createJSONObject(entry.getValue()));
				entriesJson.add(entryJson);
			}
		}

		json.put("map", entriesJson);

		return json;
	}

	/**
	 * Creates a json object from a VarBucket (containing a histogram)
	 *
	 * @param varBucket the histogram
	 * @return the json object representing the histogram
	 * @throws org.json.JSONException if any.
	 */
	public static JSONObject createVarBucketJSONObject(final VarBucket varBucket) throws JSONException {
		final JSONObject json = new JSONObject();

		json.put("min", Helper.createLiteralAsJSONObject(varBucket.minimum));
		json.put("max", Helper.createLiteralAsJSONObject(varBucket.maximum));

		final Collection<JSONObject> entriesJson = new LinkedList<JSONObject>();

		for(final lupos.optimizations.logical.statistics.Entry entry: varBucket.selectivityOfInterval){
			final JSONObject entryJson = new JSONObject();
			entryJson.put("selectivity", entry.selectivity);
			entryJson.put("numberOfDistinctLiterals", entry.distinctLiterals);
			entryJson.put("border", Helper.createLiteralAsJSONObject(entry.literal));
			entriesJson.add(entryJson);
		}

		json.put("histogram", entriesJson);

		return json;
	}

	/**
	 * Creates a JSON object that represents a collection of variables
	 *
	 * @param variables the variables
	 * @return a JSON object that represents a collection of variables
	 * @throws org.json.JSONException if any.
	 */
	public static JSONObject createVariablesJSONObject(final Collection<Variable> variables) throws JSONException {
		final JSONObject json = new JSONObject();
		final Collection<JSONObject> varsJSON = new LinkedList<JSONObject>();
		for (final Variable var : variables) {
			varsJSON.add(Helper.createVarAsJSONObject(var));
		}
		json.put("variables", varsJSON);
		return json;
	}

	/**
	 * Creates a JSONObject that represents a collection of triple patterns
	 *
	 * @param triplePatterns the triple patterns collection
	 * @return A JSONObject representing the given collection of triple patterns
	 * @throws org.json.JSONException if any.
	 */
	public static JSONObject createTriplePatternsJSONObject(final Collection<TriplePattern> triplePatterns) throws JSONException {
		final JSONObject json = new JSONObject();
		final Collection<JSONObject> triplePatternsJSON = new LinkedList<JSONObject>();
		for (final TriplePattern triplePattern : triplePatterns) {
			triplePatternsJSON.add(Helper.createTriplePatternJSONObject(triplePattern));
		}
		json.put("triple_patterns", triplePatternsJSON);
		return json;
	}

	/**
	 * Creates the triple pattern JSON object.
	 *
	 * @param triplePattern
	 *            the triple pattern
	 * @return the json object
	 * @throws org.json.JSONException
	 *             the jSON exception
	 */
	public static JSONObject createTriplePatternJSONObject(final TriplePattern triplePattern) throws JSONException {
		final Collection<JSONObject> tripleItems = Helper.createTriplePatternItemsArray(triplePattern);
		final JSONObject tripleJson = new JSONObject();
		tripleJson.put("triple_pattern", tripleItems);
		return tripleJson;
	}

	/**
	 * Creates the triple pattern items array.
	 *
	 * @param triplePattern
	 *            the triple pattern
	 * @return the collection
	 * @throws org.json.JSONException
	 *             the jSON exception
	 */
	public static Collection<JSONObject> createTriplePatternItemsArray(final TriplePattern triplePattern) throws JSONException {
		final Collection<JSONObject> tripleItems = new LinkedList<JSONObject>();
		for (final Item item : triplePattern.getItems()) {
			final JSONObject itemJson = Helper.createItemAsJSONObject(item);
			tripleItems.add(itemJson);
		}
		return tripleItems;
	}

	/**
	 * Creates the triple pattern item as json object.
	 *
	 * @param item
	 *            the item
	 * @return the jSON object
	 * @throws org.json.JSONException
	 *             the jSON exception
	 */
	public static JSONObject createItemAsJSONObject(final Item item) throws JSONException {
		if (item.isVariable()) {
			return Helper.createVarAsJSONObject((Variable) item);
		} else {
			return Helper.createLiteralAsJSONObject((Literal) item);
		}
	}

	/**
	 * Creates a JSONObject from a variable
	 *
	 * @param var the variable
	 * @return the JSON object representing the variable
	 * @throws org.json.JSONException if any.
	 */
	public final static JSONObject createVarAsJSONObject(final Variable var) throws JSONException {
		final JSONObject varJson = new JSONObject();
		varJson.put("type", "variable");
		varJson.put("name", var.getName());
		return varJson;
	}

	/**
	 * Creates a JSONObject from a literal
	 *
	 * @param literal the literal
	 * @return the JSONObject representing the literal
	 * @throws org.json.JSONException if any.
	 */
	public final static JSONObject createLiteralAsJSONObject(final Literal literal) throws JSONException {
		final JSONObject literalJson = new JSONObject();
		literalJson.put("type", "literal");
		literalJson.put("value", literal.originalString());
		return literalJson;
	}

	/**
	 * Creates an object from a JSON object. It tries to identify the object type from the attributes given in the json object
	 *
	 * @param json the json object
	 * @return the object represented by the json object, null if the object type has not been identified
	 * @throws org.json.JSONException if any.
	 * @param <T> a T object.
	 */
	@SuppressWarnings("unchecked")
	public static<T> T createObjectFromJSON(final JSONObject json) throws JSONException {
		if(json.has("type")) {
			return (T) Helper.createItemFromJSON(json);
		} else if(json.has("triple_pattern")) {
			return (T) Helper.createTriplePatternFromJSON(json);
		} else if(json.has("histogram")) {
			return (T) Helper.createVarBucketFromJSON(json);
		} else {
			System.err.println("lupos.distributed.operator.format.Helper.createObjectFromJSON(...): Unknown type stored in JSON object, returning null!");
			return null;
		}
	}

	/**
	 * Creates a map from a JSON object
	 *
	 * @param mapJson the json object
	 * @return the map
	 * @throws org.json.JSONException if any.
	 * @param <K> a K object.
	 * @param <V> a V object.
	 */
	public static<K, V> Map<K, V> createMapFromJSON(final JSONObject mapJson) throws JSONException {
		final Map<K, V> result = new HashMap<K, V>();

		final JSONArray entriesJson = (JSONArray) mapJson.get("map");
		for(int i=0; i < entriesJson.length(); i++){
			final JSONObject entryJson = entriesJson.getJSONObject(i);
			final K key = Helper.createObjectFromJSON(entryJson.getJSONObject("key"));
			final V value = Helper.createObjectFromJSON(entryJson.getJSONObject("value"));
			result.put(key, value);
		}

		return result;
	}

	/**
	 * Creates an object representing a histogram from a JSON object
	 *
	 * @param varBucketJson the json object containing the histogram
	 * @return the histogram object
	 * @throws org.json.JSONException if any.
	 */
	public static VarBucket createVarBucketFromJSON(final JSONObject varBucketJson) throws JSONException {
		final VarBucket result = new VarBucket();

		result.minimum = Helper.createLiteralFromJSON(varBucketJson.getJSONObject("min"));
		result.maximum = Helper.createLiteralFromJSON(varBucketJson.getJSONObject("max"));

		final JSONArray entriesJson = (JSONArray) varBucketJson.get("histogram");

		for(int i=0; i < entriesJson.length(); i++){
			final JSONObject entryJson = entriesJson.getJSONObject(i);
			final lupos.optimizations.logical.statistics.Entry entry = new lupos.optimizations.logical.statistics.Entry();

			entry.selectivity = entryJson.getDouble("selectivity");
			entry.distinctLiterals = entryJson.getDouble("numberOfDistinctLiterals");
			entry.literal = Helper.createLiteralFromJSON(entryJson.getJSONObject("border"));

			result.selectivityOfInterval.add(entry);
		}

		return result;
	}

	/**
	 * Creates a list of variables from a JSON object
	 *
	 * @param varsJson the JSON object
	 * @return a list of variables
	 * @throws org.json.JSONException if any.
	 */
	public static List<Variable> createVariablesFromJSON(final JSONObject varsJson) throws JSONException {
		final List<Variable> result = new LinkedList<Variable>();
		final JSONArray triplePatternsJsonArray = (JSONArray) varsJson.get("variables");
		for (int i = 0; i < triplePatternsJsonArray.length(); i++) {
			result.add(Helper.createVariableFromJSON(triplePatternsJsonArray.getJSONObject(i)));
		}
		return result;
	}

	/**
	 * Creates a list of triple patterns from a JSON object
	 *
	 * @param triplePatternsJson the JSON object
	 * @return a list of triple patterns
	 * @throws org.json.JSONException if any.
	 */
	public static List<TriplePattern> createTriplePatternsFromJSON(final JSONObject triplePatternsJson) throws JSONException {
		final List<TriplePattern> result = new LinkedList<TriplePattern>();
		final JSONArray triplePatternsJsonArray = (JSONArray) triplePatternsJson.get("triple_patterns");
		for (int i = 0; i < triplePatternsJsonArray.length(); i++) {
			result.add(Helper.createTriplePatternFromJSON(triplePatternsJsonArray.getJSONObject(i)));
		}
		return result;
	}

	/**
	 * Creates a triple pattern from a JSON object
	 *
	 * @param triplePatternJson the JSON object
	 * @return a triple pattern
	 * @throws org.json.JSONException if any.
	 */
	public static TriplePattern createTriplePatternFromJSON(final JSONObject triplePatternJson) throws JSONException {
		final JSONArray itemsJson = (JSONArray) triplePatternJson.get("triple_pattern");
		final Item[] items = new Item[3];
		for (int h = 0; h < 3; h++) {
			items[h] = Helper.createItemFromJSON(itemsJson.getJSONObject(h));
		}
		return new TriplePattern(items[0], items[1], items[2]);
	}

	/**
	 * Creates an item (i. e., a variable or literal) from the JSON object
	 *
	 * @param itemJson the JSON object
	 * @return the item
	 * @throws org.json.JSONException if any.
	 */
	public static Item createItemFromJSON(final JSONObject itemJson) throws JSONException {
		if (itemJson.getString("type").equals("variable")) {
			return Helper.createVariableFromJSON(itemJson);
		} else {
			return Helper.createLiteralFromJSON(itemJson);
		}
	}

	/**
	 * Creates a variable from a given JSON object
	 *
	 * @param varJson the JSON object
	 * @return a variable
	 * @throws org.json.JSONException if any.
	 */
	public final static Variable createVariableFromJSON(final JSONObject varJson) throws JSONException {
		return new Variable(varJson.getString("name"));
	}

	/**
	 * Creates a literal from a given JSOM object
	 *
	 * @param literalJson the JSON object
	 * @return a literal
	 * @throws org.json.JSONException if any.
	 */
	public final static Literal createLiteralFromJSON(final JSONObject literalJson) throws JSONException {
		return LazyLiteral.getLiteral(literalJson.getString("value"), true);
	}
}
