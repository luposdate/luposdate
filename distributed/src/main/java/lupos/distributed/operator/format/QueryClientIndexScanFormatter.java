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
package lupos.distributed.operator.format;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.distributed.query.operator.QueryClientIndexScan;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implements the formatter for the index scan operator
 */
public class QueryClientIndexScanFormatter implements OperatorFormatter {

		/** The index collection. */
		private Root root;

		/**
		 * Instantiates a new index scan formatter.
		 */
		public QueryClientIndexScanFormatter() {
		}

		/**
		 * Instantiates a new index scan formatter.
		 *
		 * @param root
		 *            the index collection
		 */
		public QueryClientIndexScanFormatter(final Root root) {
			this.setRoot(root);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * luposdate.operators.formatter.OperatorFormatter#serialize(lupos.engine
		 * .operators.BasicOperator, int)
		 */
		@Override
		public JSONObject serialize(final BasicOperator operator, final int node_id) {
			final JSONObject json = new JSONObject();

			final BasicIndexScan indexScan = (BasicIndexScan) operator;
			try {
				json.put("type", operator.getClass().getName());
				json.put("node_id", node_id);

				final Collection<JSONObject> triplePatterns = new LinkedList<JSONObject>();

				for (final TriplePattern triplePattern : indexScan.getTriplePattern()) {
					final JSONObject tripleJson = new JSONObject();
					final Collection<JSONObject> tripleItems = this.createTriplePatternItemsArray(triplePattern);

					tripleJson.put("items", tripleItems);

					triplePatterns.add(tripleJson);
				}

				json.put("triple_pattern", triplePatterns);
			} catch (final JSONException e) {
				e.printStackTrace();
			}

			return json;
		}

		/**
		 * Creates the triple pattern items array.
		 *
		 * @param triplePattern
		 *            the triple pattern
		 * @return the collection
		 * @throws JSONException
		 *             the jSON exception
		 */
		private Collection<JSONObject> createTriplePatternItemsArray(
				final TriplePattern triplePattern) throws JSONException {
			final Collection<JSONObject> tripleItems = new LinkedList<JSONObject>();

			for (final Item item : triplePattern.getItems()) {
				final JSONObject itemJson = this.createTriplePatternItemAsJsonString(item);
				tripleItems.add(itemJson);
			}
			return tripleItems;
		}

		/**
		 * Creates the triple pattern item as json string.
		 *
		 * @param item
		 *            the item
		 * @return the jSON object
		 * @throws JSONException
		 *             the jSON exception
		 */
		private JSONObject createTriplePatternItemAsJsonString(final Item item)
				throws JSONException {
			final JSONObject itemJson = new JSONObject();

			if (item.isVariable()) {
				itemJson.put("type", "variable");
				itemJson.put("name", item.getName());
			} else {
				itemJson.put("type", "literal");
				itemJson.put("value", item.getName());
				// item.getName().substring(1, item.getName().length() - 1));
			}
			return itemJson;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * luposdate.operators.formatter.OperatorFormatter#deserialize(org.json.
		 * JSONObject)
		 */
		@Override
		public BasicOperator deserialize(final JSONObject serializedOperator) throws JSONException {
			final QueryClientIndexScan indexScan = new QueryClientIndexScan(this.root);

			final Collection<TriplePattern> triplePatterns = this.createTriplePatternsListFromJSON(serializedOperator);
			indexScan.setTriplePatterns(triplePatterns);

			return indexScan;

		}

		/**
		 * Creates the triple patterns list from json.
		 *
		 * @param json
		 *            the json
		 * @return the collection
		 */
		private Collection<TriplePattern> createTriplePatternsListFromJSON(final JSONObject json) {
			final Collection<TriplePattern> result = new LinkedList<TriplePattern>();
			try {
				final JSONArray triplePatternsJson = (JSONArray) json.get("triple_pattern");
				for (int i = 0; i < triplePatternsJson.length(); i++) {
					final JSONObject triplePatternJson = triplePatternsJson.getJSONObject(i);
					final JSONArray itemsJson = (JSONArray) triplePatternJson.get("items");
					final Item[] items = new Item[3];
					for (int h = 0; h < 3; h++) {
						final JSONObject itemJson = itemsJson.getJSONObject(h);
						if (itemJson.getString("type").equals("variable")) {
							items[h] = new Variable(itemJson.getString("name"));
						} else {
							items[h] = LazyLiteral.getLiteral(itemJson.getString("value"));
						}
					}
					final TriplePattern triplePattern = new TriplePattern(items[0], items[1], items[2]);
					result.add(triplePattern);
				}
			} catch (final JSONException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			return result;
		}

		/**
		 * Sets the index collection.
		 *
		 * @param root
		 *            the new index collection
		 */
		public void setRoot(final Root root) {
			this.root = root;
		}
}
