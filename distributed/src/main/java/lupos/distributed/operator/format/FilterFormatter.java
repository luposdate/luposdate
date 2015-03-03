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
package lupos.distributed.operator.format;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.filter.Filter;
import lupos.sparql1_1.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implements the formatter for the Filter Operator.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class FilterFormatter implements OperatorFormatter {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * luposdate.operators.formatter.OperatorFormatter#serialize(lupos.engine
	 * .operators.BasicOperator, int)
	 */
	/** {@inheritDoc} */
	@Override
	public JSONObject serialize(final BasicOperator operator, final int node_id)
			throws JSONException {
		final JSONObject json = new JSONObject();
		json.put("type", Filter.class.getName());
		json.put("node_id", node_id);
		json.put("expression", operator.toString());
		return json;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * luposdate.operators.formatter.OperatorFormatter#deserialize(org.json.JSONObject)
	 */
	/** {@inheritDoc} */
	@Override
	public BasicOperator deserialize(final JSONObject serializedOperator) throws JSONException {
		final JSONObject json = serializedOperator;
		String filtername =  json.getString("expression");
		filtername = filtername.substring(0,filtername.length()-3);
		try {
			return new Filter(filtername);
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
