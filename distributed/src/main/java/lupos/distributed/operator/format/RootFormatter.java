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

import lupos.distributed.operator.format.operatorcreator.IOperatorCreator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Root;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implements the formatter for the root operator
 */
public class RootFormatter implements OperatorFormatter {

		/** The dataset. */
		private Dataset	dataset;

		/**
		 * the operator creator for creating the root operator
		 */
		private IOperatorCreator operatorCreator;

		/**
		 * Gets the dataset.
		 *
		 * @return the dataset
		 */
		public Dataset getDataset() {
			return this.dataset;
		}

		/**
		 * Instantiates a new root formatter.
		 *
		 * @param dataset
		 *            the dataset
		 */
		public RootFormatter(final Dataset dataset, final IOperatorCreator operatorCreator) {
			this.dataset = dataset;
			this.operatorCreator = operatorCreator;
		}

		/**
		 * Instantiates a new root formatter.
		 */
		public RootFormatter() {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * luposdate.operators.formatter.OperatorFormatter#serialize(lupos.engine
		 * .operators.BasicOperator, int)
		 */
		@Override
		public JSONObject serialize(final BasicOperator operator, final int node_id)
				throws JSONException {
			final JSONObject json = new JSONObject();

			json.put("type", Root.class.getName());
			json.put("node_id", node_id);
			json.put("root", true);

			return json;
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
			return this.operatorCreator.createRoot(this.dataset);
		}
}
