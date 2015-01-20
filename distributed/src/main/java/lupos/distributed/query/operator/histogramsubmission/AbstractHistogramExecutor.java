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
 */
package lupos.distributed.query.operator.histogramsubmission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.distributed.operator.format.Helper;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.statistics.VarBucket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractHistogramExecutor implements IHistogramExecutor {

	@Override
	public Map<Variable, Tuple<Literal, Literal>> getMinMax(final TriplePattern triplePattern, final Collection<Variable> variables) {
		try {
			final JSONObject mainJSON = Helper.createVariablesJSONObject(variables);
			final JSONObject triplePatternJSON = Helper.createTriplePatternJSONObject(triplePattern);
			mainJSON.put("triple_pattern", triplePatternJSON.get("triple_pattern"));

			final JSONObject json = new JSONObject();
			json.put("min_max_request", mainJSON);

			final String[] jsonResults = this.sendJSONRequests(json.toString(), triplePattern);

			final Map<Variable, Tuple<Literal, Literal>> result = new HashMap<Variable, Tuple<Literal, Literal>>();

			for(final String jsonResult: jsonResults){

				final JSONArray array = new JSONObject(jsonResult).getJSONArray("result");
				for(int i=0; i<array.length(); i++){
					final JSONObject entry = array.getJSONObject(i);

					final Variable var = Helper.createVariableFromJSON(entry.getJSONObject("variable"));
					Literal min = Helper.createLiteralFromJSON(entry.getJSONObject("minimum"));
					Literal max = Helper.createLiteralFromJSON(entry.getJSONObject("maximum"));

					// compare new min/max with possibly existing ones...

					boolean modified = false;
					final Tuple<Literal, Literal> minMax = result.get(var);
					if(minMax==null){
						modified = true;
					} else {
						final Literal otherMin = minMax.getFirst();
						if(min.compareToNotNecessarilySPARQLSpecificationConform(otherMin)<0){
							modified = true;
						} else {
							min = otherMin;
						}
						final Literal otherMax = minMax.getSecond();
						if(max.compareToNotNecessarilySPARQLSpecificationConform(otherMax)>0){
							modified = true;
						} else {
							max = otherMax;
						}
					}
					if(modified){
						// new min or/and max!
						result.put(var, new Tuple<Literal, Literal>(min, max));
					}
				}
			}

			return result;
		} catch (final JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Map<Variable, VarBucket> getHistograms(
			final TriplePattern triplePattern,
			final Collection<Variable> variables,
			final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima) {
		try {
			final JSONObject mainJSON = Helper.createVariablesJSONObject(variables);
			final JSONObject triplePatternJSON = Helper.createTriplePatternJSONObject(triplePattern);
			mainJSON.put("triple_pattern", triplePatternJSON.get("triple_pattern"));
			mainJSON.put("minima", Helper.createMapJSONObject(minima));
			mainJSON.put("maxima", Helper.createMapJSONObject(maxima));

			final JSONObject json = new JSONObject();
			json.put("histogram_request", mainJSON);

			final String[] jsonResults = this.sendJSONRequests(json.toString(), triplePattern);

			if(jsonResults.length==1) {
				// only one histogram => just return!
				return Helper.createMapFromJSON(new JSONObject(jsonResults[0]));
			}

			//TODO deal with several histograms coming from different nodes => union of histograms
			// in the moment just the one is chosen with the most entries
			Map<Variable, VarBucket> result = null;

			for(final String jsonResult: jsonResults) {
				final Map<Variable, VarBucket> map = Helper.createMapFromJSON(new JSONObject(jsonResult));

				if(result == null){
					// the first histogram is just taken over
					result = map;
				} else {
					// look, which histogram is based on more elements and just choose this one
					for(final Entry<Variable, VarBucket> entry: map.entrySet()){
						final Variable var = entry.getKey();
						final VarBucket histogram = result.get(var);
						final VarBucket otherHistogram = entry.getValue();
						if(histogram == null || otherHistogram.getSum()>histogram.getSum()) {
							result.put(var, otherHistogram);
						}
					}
				}
			}

			return result;
		} catch (final JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Must be overridden to implement that the request (for histogram or min/max computations) is transmitted over network
	 *
	 * @param request the request for histograms or min/max computations serialized as json string
	 * @param triplePattern can be used for determining the key of the node holding data relevant to this triple pattern
	 * @return the result strings (serialized as json string) from the queried nodes
	 */
	public abstract String[] sendJSONRequests(String request, TriplePattern triplePattern);

	/**
	 * just for creating the json request string for rebuilding the statistics to be sent over network
	 *
	 * @return the json request string for rebuilding the statistics
	 */
	public static String createRebuildStatisticsRequestString(){
		final JSONObject json = new JSONObject();
		try {
			json.put("rebuild_statistics_request", "now");
		} catch (final JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return json.toString();
	}
}
