/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.VariableInInferenceRule;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Filter;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.sparql1_1.ParseException;

public class RudimentaryRDFSchemaInference {
	protected static Variable v(final String str) {
		return new VariableInInferenceRule(str);
	}

	protected static URILiteral u(final String str) {
		try {
			return LiteralFactory
					.createURILiteralWithoutLazyLiteral("<http://www.w3.org/"
							+ str + ">");
		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}
	}

	protected static Triple t(final String a, final String b, final String c) {
		return new Triple(u(a), u(b), u(c));
	}

	protected static TriplePattern tp(final Item a, final Item b, final Item c) {
		return new TriplePattern(a, b, c);
	}

	static Triple[] axiomaticRDFTriples = {
			t("1999/02/22-rdf-syntax-ns#type", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#subject",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#predicate",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#object",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#first",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#rest", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#value",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_1", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_2", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_3", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_4", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_5", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_6", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_7", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_8", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_9", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_10", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_11", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_12", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_13", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_14", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_15", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_16", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_17", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_18", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_19", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_20", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_21", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_22", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_23", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_24", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_25", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_26", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_27", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_28", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_29", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_30", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_31", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_32", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_33", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_34", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_35", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_36", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_37", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_38", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#_39", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("1999/02/22-rdf-syntax-ns#nil", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#List") };

	static Triple[] axiomaticRDFSTriples = {
			t("1999/02/22-rdf-syntax-ns#type", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("2000/01/rdf-schema#domain", "2000/01/rdf-schema#domain",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#range", "2000/01/rdf-schema#domain",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#subPropertyOf", "2000/01/rdf-schema#domain",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#subClassOf", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Class"),
			t("1999/02/22-rdf-syntax-ns#subject", "2000/01/rdf-schema#domain",
					"1999/02/22-rdf-syntax-ns#Statement"),
			t("1999/02/22-rdf-syntax-ns#predicate",
					"2000/01/rdf-schema#domain",
					"1999/02/22-rdf-syntax-ns#Statement"),
			t("1999/02/22-rdf-syntax-ns#object", "2000/01/rdf-schema#domain",
					"1999/02/22-rdf-syntax-ns#Statement"),
			t("2000/01/rdf-schema#member", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#first", "2000/01/rdf-schema#domain",
					"1999/02/22-rdf-syntax-ns#List"),
			t("1999/02/22-rdf-syntax-ns#rest", "2000/01/rdf-schema#domain",
					"1999/02/22-rdf-syntax-ns#List"),
			t("2000/01/rdf-schema#seeAlso", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("2000/01/rdf-schema#isDefinedBy", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("2000/01/rdf-schema#comment", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("2000/01/rdf-schema#label", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("2000/01/rdf-schema#value", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#type", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Class"),
			t("1999/02/22-rdf-syntax-ns#domain", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Class"),
			t("1999/02/22-rdf-syntax-ns#range", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Class"),
			t("2000/01/rdf-schema#subPropertyOf", "2000/01/rdf-schema#range",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#subClassOf", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Class"),
			t("1999/02/22-rdf-syntax-ns#subject", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#predicate", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#object", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),
			t("2000/01/rdf-schema#member", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#first", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#rest", "2000/01/rdf-schema#range",
					"1999/02/22-rdf-syntax-ns#List"),
			t("2000/01/rdf-schema#seeAlso", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),
			t("2000/01/rdf-schema#isDefinedBy", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),
			t("2000/01/rdf-schema#comment", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#URILiteral"),
			t("2000/01/rdf-schema#label", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#URILiteral"),
			t("1999/02/22-rdf-syntax-ns#value", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#Alt", "2000/01/rdf-schema#subClassOf",
					"2000/01/rdf-schema#Container"),
			t("1999/02/22-rdf-syntax-ns#Bag", "2000/01/rdf-schema#subClassOf",
					"2000/01/rdf-schema#Container"),
			t("1999/02/22-rdf-syntax-ns#Sequence",
					"2000/01/rdf-schema#subClassOf",
					"2000/01/rdf-schema#Container"),
			t("2000/01/rdf-schema#ContainerMembershipProperty",
					"2000/01/rdf-schema#subClassOf",
					"1999/02/22-rdf-syntax-ns#Property"),

			t("2000/01/rdf-schema#isDefinedBy",
					"2000/01/rdf-schema#subPropertyOf",
					"2000/01/rdf-schema#seeAlso"),

			t("1999/02/22-rdf-syntax-ns#XMLURILiteral",
					"1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#Datatype"),
			t("1999/02/22-rdf-syntax-ns#XMLURILiteral",
					"2000/01/rdf-schema#subClassOf",
					"2000/01/rdf-schema#URILiteral"),
			t("2000/01/rdf-schema#Datatype", "2000/01/rdf-schema#subClassOf",
					"2000/01/rdf-schema#Class"),

			t("1999/02/22-rdf-syntax-ns#_1", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_1", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_1", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_2", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_2", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_2", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_3", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_3", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_3", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_4", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_4", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_4", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_5", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_5", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_5", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_6", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_6", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_6", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_7", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_7", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_7", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_8", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_8", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_8", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_9", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_9", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_9", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_10", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_10", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_10", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_11", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_11", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_11", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_12", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_12", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_12", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_13", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_13", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_13", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_14", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_14", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_14", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_15", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_15", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_15", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_16", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_16", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_16", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_17", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_17", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_17", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_18", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_18", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_18", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_19", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_19", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_19", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_20", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_20", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_20", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_21", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_21", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_21", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_22", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_22", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_22", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_23", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_23", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_23", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_24", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_24", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_24", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_25", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_25", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_25", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_26", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_26", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_26", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_27", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_27", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_27", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_28", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_28", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_28", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_29", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_29", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_29", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_30", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_30", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_30", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_31", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_31", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_31", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_32", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_32", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_32", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_33", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_33", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_33", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_34", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_34", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_34", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_35", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_35", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_35", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_36", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_36", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_36", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_37", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_37", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_37", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_38", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_38", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_38", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource"),

			t("1999/02/22-rdf-syntax-ns#_39", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#ContainerMembershipProperty"),
			t("1999/02/22-rdf-syntax-ns#_39", "2000/01/rdf-schema#domain",
					"2000/01/rdf-schema#Resource"),
			t("1999/02/22-rdf-syntax-ns#_39", "2000/01/rdf-schema#range",
					"2000/01/rdf-schema#Resource") };

	static Triple[] RDFSvalidTriples = {
			t("2000/01/rdf-schema#Resource", "1999/02/22-rdf-syntax-ns#type",
					"2000/01/rdf-schema#Class"),
			t("2000/01/rdf-schema#Class", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("2000/01/rdf-schema#URILiteral", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("1999/02/22-rdf-syntax-ns#XMLURILiteral",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("2000/01/rdf-schema#Datatype", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("1999/02/22-rdf-syntax-ns#Seq", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("1999/02/22-rdf-syntax-ns#Bag", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("1999/02/22-rdf-syntax-ns#Alt", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("2000/01/rdf-schema#Container", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("1999/02/22-rdf-syntax-ns#List", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("2000/01/rdf-schema#ContainerMembershipProperty",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("1999/02/22-rdf-syntax-ns#Property",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),
			t("1999/02/22-rdf-syntax-ns#Statement",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Class"),

			t("2000/01/rdf-schema#domain", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#range", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#subPropertyOf",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#subClassOf", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#member", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#seeAlso", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#isDefinedBy",
					"1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#comment", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property"),
			t("2000/01/rdf-schema#label", "1999/02/22-rdf-syntax-ns#type",
					"1999/02/22-rdf-syntax-ns#Property") };

	public RudimentaryRDFSchemaInference() {
	}

	protected static void addTps(final Item data, final Root ic,
			final TripleOperator tp, final Item a, final Item b, final Item c,
			final TriplePattern... tps) {
		final Generate g = new Generate(tp, a, b, c);
		if (tps.length == 1) {
			final TriplePattern tpo = tps[0];
			tpo.setSucceedingOperator(new OperatorIDTuple(g, 0));
			final Collection<TriplePattern> ctp = new LinkedList<TriplePattern>();
			ctp.add(tpo);
			ic.addSucceedingOperator(new OperatorIDTuple(ic.newIndex(
					new OperatorIDTuple(g, 0), ctp, null), 0));
		} else {

			final Collection<TriplePattern> ctp = new LinkedList<TriplePattern>();
			for (int i = 0; i < tps.length; i++) {
				final TriplePattern tpo = tps[i];
				ctp.add(tpo);
			}
			ic.addSucceedingOperator(new OperatorIDTuple(ic.newIndex(
					new OperatorIDTuple(g, 0), ctp, null), 0));
		}
	}

	protected static void addTpsFilter(final Item data,
			final Root ic, final TripleOperator tp, final Item a,
			final Item b, final Item c, final String filter,
			final TriplePattern tp2) {
		try {
			final Filter f = new Filter(filter);
			final Generate g = new Generate(tp, a, b, c);
			f.addSucceedingOperator(new OperatorIDTuple(g, 0));
			tp2.setSucceedingOperator(new OperatorIDTuple(f, 0));
			final Collection<TriplePattern> ctp = new LinkedList<TriplePattern>();
			ctp.add(tp2);
			ic.addSucceedingOperator(new OperatorIDTuple(ic.newIndex(
					new OperatorIDTuple(f, 0), ctp, null), 0));
		} catch (final ParseException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	protected static void addTpsFilter(final Item data,
			final Root ic, final TripleOperator tp, final Item a,
			final Item b, final Item c, final String filter1,
			final String filter2, final TriplePattern tp1,
			final TriplePattern tp2) {
		try {
			final Filter f1 = new Filter(filter1);
			final Filter f2 = new Filter(filter2);
			final Generate g = new Generate(tp, a, b, c);
			final Join join = new Join();
			f1.addSucceedingOperator(new OperatorIDTuple(join, 0));
			f2.addSucceedingOperator(new OperatorIDTuple(join, 1));
			join.addSucceedingOperator(new OperatorIDTuple(g, 0));
			tp1.setSucceedingOperator(new OperatorIDTuple(f1, 0));
			tp2.setSucceedingOperator(new OperatorIDTuple(f2, 0));
			final Collection<TriplePattern> ctp1 = new LinkedList<TriplePattern>();
			ctp1.add(tp1);
			ic.addSucceedingOperator(new OperatorIDTuple(ic.newIndex(
					new OperatorIDTuple(f1, 0), ctp1, null), 0));
			final Collection<TriplePattern> ctp2 = new LinkedList<TriplePattern>();
			ctp2.add(tp2);
			ic.addSucceedingOperator(new OperatorIDTuple(ic.newIndex(
					new OperatorIDTuple(f2, 0), ctp2, null), 0));
		} catch (final ParseException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	protected static void addTpsFilter(final Item data,
			final Root ic, final TripleOperator tp, final Item a,
			final Item b, final Item c, final String filter1,
			final TriplePattern tp1, final TriplePattern tp2) {
		try {
			final Filter f1 = new Filter(filter1);
			final Generate g = new Generate(tp, a, b, c);
			final Join join = new Join();
			f1.addSucceedingOperator(new OperatorIDTuple(join, 0));
			join.addSucceedingOperator(new OperatorIDTuple(g, 0));
			tp1.setSucceedingOperator(new OperatorIDTuple(f1, 0));
			tp2.setSucceedingOperator(new OperatorIDTuple(join, 1));
			final Collection<TriplePattern> ctp1 = new LinkedList<TriplePattern>();
			ctp1.add(tp1);
			ic.addSucceedingOperator(new OperatorIDTuple(ic.newIndex(
					new OperatorIDTuple(f1, 0), ctp1, null), 0));
			final Collection<TriplePattern> ctp2 = new LinkedList<TriplePattern>();
			ctp2.add(tp2);
			ic.addSucceedingOperator(new OperatorIDTuple(ic.newIndex(
					new OperatorIDTuple(join, 1), ctp2, null), 0));
		} catch (final ParseException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	protected static final URILiteral TYPE = u("1999/02/22-rdf-syntax-ns#type");
	protected static final URILiteral DOMAIN = u("2000/01/rdf-schema#domain");
	protected static final URILiteral RANGE = u("2000/01/rdf-schema#range");
	protected static final URILiteral RESSOURCE = u("2000/01/rdf-schema#Resource");
	protected static final URILiteral PROPERTY = u("1999/02/22-rdf-syntax-ns#Property");
	protected static final URILiteral SUBPROPERTY = u("2000/01/rdf-schema#subPropertyOf");
	protected static final URILiteral CLASS = u("2000/01/rdf-schema#Class");
	protected static final URILiteral SUBCLASS = u("2000/01/rdf-schema#subClassOf");

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
		try {
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs5
			 * uuu rdfs:subPropertyOf vvv . vvv rdfs:subPropertyOf xxx . uuu
			 * rdfs:subPropertyOf xxx .
			 */
			final TriplePattern tp_rdfs5_1 = new TriplePattern(v("uuu"),
					SUBPROPERTY, v("vvv"));
			final TriplePattern tp_rdfs5_2 = new TriplePattern(v("vvv"),
					SUBPROPERTY, v("xxx"));
			addTpsFilter(data, ic, tp, v("uuu"), SUBPROPERTY, v("xxx"),
					"(?uuu!=?vvv)", "(?vvv!=?xxx)", tp_rdfs5_1,
					tp_rdfs5_2);

			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs11
			 * uuu rdfs:subClassOf vvv . vvv rdfs:subClassOf xxx . uuu
			 * rdfs:subClassOf xxx .
			 */
			final TriplePattern tp_rdfs11_1 = new TriplePattern(v("uuu"),
					SUBCLASS, v("vvv"));
			final TriplePattern tp_rdfs11_2 = new TriplePattern(v("vvv"),
					SUBCLASS, v("xxx"));
			addTpsFilter(data, ic, tp, v("uuu"), SUBCLASS, v("xxx"),
					"(?uuu!=?vvv)", "(?vvv!=?xxx)", tp_rdfs11_1,
					tp_rdfs11_2);
		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public static void addInferenceRulesForInstanceData(
			final Root ic, final TripleOperator tp, final Item data) {
		try {
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs9
			 * uuu rdfs:subClassOf xxx . vvv rdf:type uuu . vvv rdf:type xxx .
			 */
			final TriplePattern tp_rdfs9_1 = new TriplePattern(v("uuu"),
					SUBCLASS, v("xxx"));
			final TriplePattern tp_rdfs9_2 = new TriplePattern(v("vvv"), TYPE,
					v("uuu"));
			addTpsFilter(data, ic, tp, v("vvv"), TYPE, v("xxx"),
					"(?uuu!=?xxx)", tp_rdfs9_1, tp_rdfs9_2);
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs7
			 * aaa rdfs:subPropertyOf bbb . uuu aaa yyy . uuu bbb yyy .
			 */
			final TriplePattern tp_rdfs7_1 = new TriplePattern(v("aaa"),
					SUBPROPERTY, v("bbb"));
			final TriplePattern tp_rdfs7_2 = new TriplePattern(v("uuu"),
					v("aaa"), v("yyy"));
			addTpsFilter(data, ic, tp, v("uuu"), v("bbb"), v("yyy"),
					"(?aaa!=?bbb)", tp_rdfs7_1, tp_rdfs7_2);
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

	public static void evaluateAxiomaticAndRDFSValidTriples(
			final TripleConsumer tc) {
		for (int i = 0; i < axiomaticRDFTriples.length; i++) {
			// System.out.println("!!-->"+axiomaticRDFTriples[i]);
			tc.consume(axiomaticRDFTriples[i]);
		}
		for (int i = 0; i < axiomaticRDFSTriples.length; i++)
			tc.consume(axiomaticRDFSTriples[i]);
		for (int i = 0; i < RDFSvalidTriples.length; i++)
			tc.consume(RDFSvalidTriples[i]);
	}

}