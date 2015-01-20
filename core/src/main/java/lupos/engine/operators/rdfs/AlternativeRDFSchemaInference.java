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
package lupos.engine.operators.rdfs;

import lupos.datastructures.items.Item;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class AlternativeRDFSchemaInference extends
		RudimentaryRDFSchemaInference {

	public AlternativeRDFSchemaInference() {
	}

	public static void addInferenceRules(final Root ic,
			final TripleOperator tp) {
		addInferenceRules(ic, tp, null);
	}

	public static void addInferenceRulesForInstanceData(
			final Root ic, final TripleOperator tp) {
		addInferenceRulesForInstanceData(ic, tp, null);
	}

	public static void addInferenceRulesForExternalOntology(
			final Root ic, final TripleOperator tp) {
		addInferenceRulesForExternalOntology(ic, tp, null);
	}

	public static void addInferenceRulesForExternalOntology(
			final Root ic, final TripleOperator tp, final Item data) {
		RudimentaryRDFSchemaInference.addInferenceRulesForExternalOntology(ic,
				tp, data);
		try {
			TriplePattern tp_1;
			// TriplePattern tp_2;

			// rules (I) and (II) already defined in
			// RudimentaryRDFSchemaInference

			// (III) are axiomatic triples

			// (IV)

			tp_1 = tp(v("uuu"), SUBPROPERTY, v("www"));
			addTpsFilter(data, ic, tp, v("uuu"), SUBPROPERTY, v("uuu"),
					"FILTER(?uuu!=?www)", tp_1);

			tp_1 = tp(v("uuu"), SUBPROPERTY, v("www"));
			addTpsFilter(data, ic, tp, v("www"), SUBPROPERTY, v("www"),
					"FILTER(?uuu!=?www)", tp_1);

			// (V)

			tp_1 = tp(v("uuu"), DOMAIN, v("www"));
			addTps(data, ic, tp, v("uuu"), SUBPROPERTY, v("uuu"), tp_1);

			tp_1 = tp(v("uuu"), RANGE, v("www"));
			addTps(data, ic, tp, v("uuu"), SUBPROPERTY, v("uuu"), tp_1);

			// (VI)

			tp_1 = tp(v("uuu"), SUBCLASS, v("www"));
			addTpsFilter(data, ic, tp, v("uuu"), SUBCLASS, v("uuu"),
					"FILTER(?uuu!=?www)", tp_1);

			// (VII)

			tp_1 = tp(v("uuu"), DOMAIN, v("www"));
			addTps(data, ic, tp, v("www"), SUBCLASS, v("www"), tp_1);

			tp_1 = tp(v("uuu"), RANGE, v("www"));
			addTps(data, ic, tp, v("www"), SUBCLASS, v("www"), tp_1);

		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public static void addInferenceRulesForInstanceData(
			final Root ic, final TripleOperator tp, final Item data) {
		RudimentaryRDFSchemaInference.addInferenceRulesForInstanceData(ic, tp,
				data);
		try {
			// (VIII)
			final TriplePattern tp_1_1 = tp(v("aaa"), DOMAIN, v("xxx"));
			final TriplePattern tp_1_2 = tp(v("uuu"), v("aaa"), v("yyy"));
			addTps(data, ic, tp, v("uuu"), TYPE, v("xxx"), tp_1_1, tp_1_2);

			// ((IX)
			final TriplePattern tp_2_1 = new TriplePattern(v("aaa"), RANGE,
					v("xxx"));
			final TriplePattern tp_2_2 = new TriplePattern(v("uuu"), v("aaa"),
					v("vvv"));
			addTps(data, ic, tp, v("vvv"), TYPE, v("xxx"), tp_2_1, tp_2_2);

			// (X)
			// already in RudimentaryRDFSchemaInference

			// (XI)
			final TriplePattern tp_rdfs11_1 = new TriplePattern(v("c1"),
					SUBCLASS, v("c2"));
			final TriplePattern tp_rdfs11_2 = new TriplePattern(v("s"), TYPE,
					v("c1"));
			addTpsFilter(data, ic, tp, v("s"), TYPE, v("c2"),
					"Filter(?c1!=?c2)", tp_rdfs11_1, tp_rdfs11_2);

			// (XII)
			final TriplePattern tp_rdfs12_1 = new TriplePattern(v("p1"),
					DOMAIN, v("c"));
			final TriplePattern tp_rdfs12_2 = new TriplePattern(v("p2"),
					SUBPROPERTY, v("p1"));
			final TriplePattern tp_rdfs12_3 = new TriplePattern(v("s"),
					v("p2"), v("o"));
			addTps(data, ic, tp, v("s"), TYPE, v("c"), tp_rdfs12_1,
					tp_rdfs12_2, tp_rdfs12_3);

			// (XIII)
			final TriplePattern tp_rdfs13_1 = new TriplePattern(v("p1"), RANGE,
					v("c"));
			final TriplePattern tp_rdfs13_2 = new TriplePattern(v("p2"),
					SUBPROPERTY, v("p1"));
			final TriplePattern tp_rdfs13_3 = new TriplePattern(v("s"),
					v("p2"), v("o"));
			addTps(data, ic, tp, v("o"), TYPE, v("c"), tp_rdfs13_1,
					tp_rdfs13_2, tp_rdfs13_3);

			// (XIV)
			final TriplePattern tp_14 = tp(v("s"), v("p"), v("o"));
			addTps(data, ic, tp, v("p"), SUBPROPERTY, v("p"), tp_14);

			// (XV)
			final TriplePattern tp_15 = tp(v("s"), v("p"), v("o"));
			addTps(data, ic, tp, v("o"), SUBCLASS, v("o"), tp_15);

		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public static void addInferenceRules(final Root ic,
			final TripleOperator tp, final Item data) {
		addInferenceRulesForExternalOntology(ic, tp, data);
		addInferenceRulesForInstanceData(ic, tp, data);
	}
}