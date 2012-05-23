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
package lupos.engine.evaluators;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.trie.SuperTrie;
import lupos.engine.evaluators.CommonCoreQueryEvaluator.DISTINCT;
import lupos.engine.evaluators.CommonCoreQueryEvaluator.JOIN;
import lupos.engine.evaluators.CommonCoreQueryEvaluator.MERGE_JOIN_OPTIONAL;
import lupos.engine.evaluators.CommonCoreQueryEvaluator.PARALLELOPERANDS;
import lupos.engine.evaluators.CommonCoreQueryEvaluator.RDFS;
import lupos.engine.evaluators.CommonCoreQueryEvaluator.SORT;
import lupos.engine.evaluators.CommonCoreQueryEvaluator.STORAGE;
import lupos.engine.evaluators.JenaQueryEvaluator.FORMAT;
import lupos.engine.evaluators.JenaQueryEvaluator.ONTOLOGY;
import lupos.engine.evaluators.QueryEvaluator.DEBUG;
import lupos.engine.evaluators.QueryEvaluator.compareEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator.Optimizations;
import lupos.engine.evaluators.StreamQueryEvaluator.MATCHER;
import lupos.engine.operators.index.Dataset;
import xpref.XPref;
import xpref.datatypes.BooleanDatatype;
import xpref.datatypes.CollectionDatatype;
import xpref.datatypes.EnumDatatype;
import xpref.datatypes.FileChooseDatatype;
import xpref.datatypes.IntegerDatatype;
import xpref.datatypes.StringDatatype;

public final class EvaluatorCreator {

	public enum EVALUATORS{
		RDF3X {
			public QueryEvaluator create(XPref xpref) throws Exception{
				return EvaluatorCreator.createRDF3XQueryEvaluator(xpref);
			}
		}, Memory {
			public QueryEvaluator create(XPref xpref) throws Exception{
				return EvaluatorCreator.createMemoryIndexQueryEvaluator(xpref);
			}
		}, Stream {
			public QueryEvaluator create(XPref xpref) throws Exception{
				return EvaluatorCreator.createStreamQueryEvaluator(xpref);
			}			
		}, Jena {
			public QueryEvaluator create(XPref xpref) throws Exception{
				return EvaluatorCreator.createJenaQueryEvaluator(xpref);
			}
		}, Sesame {
			public QueryEvaluator create(XPref xpref) throws Exception{
				return EvaluatorCreator.createSesameQueryEvaluator(xpref);
			}
		};
		
		public abstract QueryEvaluator create(XPref xpref) throws Exception;
	};
	
	public static RDF3XQueryEvaluator createRDF3XQueryEvaluator(XPref xpref) throws XPathExpressionException, SecurityException, IllegalArgumentException, ClassNotFoundException, SAXException, ParserConfigurationException, IOException, NoSuchFieldException, NoSuchMethodException, IllegalClassFormatException, IllegalAccessException, InvocationTargetException{
		return new RDF3XQueryEvaluator(
				(DEBUG) EnumDatatype.getValues("debug").get(0),
				BooleanDatatype.getValues("multiplequeries").get(0),
				(compareEvaluator) EnumDatatype.getValues("compare").get(0),
				StringDatatype.getValues("compareoptions").get(0),
				BooleanDatatype.getFirstValue("times.enableRepetitions") ? IntegerDatatype
						.getValues("times.repetitionNumber").get(0)
						: 0,
				StringDatatype.getValues("dataset").get(0),
				((BooleanDatatype.getValues("formatLUPOSDATE.multiple").get(0)) ? "MULTIPLE"
						: "")
						+ EnumDatatype.getValues("formatLUPOSDATE.format").get(0),
						(BooleanDatatype
								.getValues("externalontology.enableExternalOntology")
								.get(0)) ? (EnumDatatype
								.getValues("externalontology.formatExternalOntology")
								.get(0))
								+ ","
								+ FileChooseDatatype
										.getFirstValue("externalontology.fileExternalOntology")
								: null,
						BooleanDatatype.getValues(
								"externalontology.inmemoryexternalontologyinference")
								.get(0),
						(RDFS) EnumDatatype.getValues("rdfsLUPOSDATE").get(0),
						(LiteralFactory.MapType) EnumDatatype.getValues("codemap").get(
								0),
						FileChooseDatatype.getValues("tmpdirs")
								.toArray(new String[] {}),
						BooleanDatatype.getValues("loadindexinfo").get(0),
						(PARALLELOPERANDS) EnumDatatype.getValues("paralleloperands.type").get(0),
						BooleanDatatype.getValues("paralleloperands.blockwise").get(0),
						BooleanDatatype.getValues("paralleloperands.boundedQueue").get(
						0) ? IntegerDatatype
						.getValues("paralleloperands.limit").get(0) : 0,
						IntegerDatatype.getValues("jointhreads").get(0),
						IntegerDatatype.getValues("joinbuffer").get(0),
						(Heap.HEAPTYPE) EnumDatatype.getValues("heap").get(0),
						(ToSort.TOSORT) EnumDatatype.getValues("tosort").get(0),
						IntegerDatatype.getValues("indexheap").get(0),
						IntegerDatatype.getValues("mergeheapheight").get(0),
						(Heap.HEAPTYPE) EnumDatatype.getValues("mergeheaptype").get(0),
						IntegerDatatype.getValues("chunk").get(0),
						IntegerDatatype.getValues("mergethreads").get(0),
						(BooleanDatatype.getValues("yagomax.enableYagoMax").get(0)) ? IntegerDatatype
								.getValues("yagomax.yagomaxNumber").get(0) : -1,
						(SuperTrie.TRIETYPE) EnumDatatype.getValues("stringsearch")
								.get(0),
						(QueryResult.TYPE) EnumDatatype.getValues("result")
								.get(0),
						(STORAGE) EnumDatatype.getValues("storage").get(0),
						(JOIN) EnumDatatype.getValues("join").get(0),
						(JOIN) EnumDatatype.getValues("optional").get(0),
						(SORT) EnumDatatype.getValues("sort").get(0),
						(DISTINCT) EnumDatatype.getValues("distinct").get(0),
						(MERGE_JOIN_OPTIONAL) EnumDatatype.getValues(
								"merge_join_optional").get(0),
						CollectionDatatype.getFirstValue("encoding"),
				(lupos.engine.operators.index.Indices.DATA_STRUCT) EnumDatatype.getValues("datastructure").get(0),
				(Dataset.SORT) EnumDatatype.getValues("sortduringindexconstruction").get(0),
				FileChooseDatatype.getFirstValue("writeindexinfo"),
				 (Optimizations) EnumDatatype.getValues(
						"optimizationRDF3X").get(0));
	}
	
	public static MemoryIndexQueryEvaluator createMemoryIndexQueryEvaluator(XPref xpref) throws XPathExpressionException, SecurityException, IllegalArgumentException, ClassNotFoundException, SAXException, ParserConfigurationException, IOException, NoSuchFieldException, NoSuchMethodException, IllegalClassFormatException, IllegalAccessException, InvocationTargetException{
		return new MemoryIndexQueryEvaluator((DEBUG) EnumDatatype.getValues("debug").get(0),
				BooleanDatatype.getValues("multiplequeries").get(0),
				(compareEvaluator) EnumDatatype.getValues("compare").get(0),
				StringDatatype.getValues("compareoptions").get(0),
				BooleanDatatype.getFirstValue("times.enableRepetitions") ? IntegerDatatype
						.getValues("times.repetitionNumber").get(0)
						: 0,
				StringDatatype.getValues("dataset").get(0),
				((BooleanDatatype.getValues("formatLUPOSDATE.multiple").get(0)) ? "MULTIPLE"
						: "")
						+ EnumDatatype.getValues("formatLUPOSDATE.format").get(0),
						(BooleanDatatype
								.getValues("externalontology.enableExternalOntology")
								.get(0)) ? (EnumDatatype
								.getValues("externalontology.formatExternalOntology")
								.get(0))
								+ ","
								+ FileChooseDatatype
										.getFirstValue("externalontology.fileExternalOntology")
								: null,
						BooleanDatatype.getValues(
								"externalontology.inmemoryexternalontologyinference")
								.get(0),
						(RDFS) EnumDatatype.getValues("rdfsLUPOSDATE").get(0),
						(LiteralFactory.MapType) EnumDatatype.getValues("codemap").get(
								0),
						FileChooseDatatype.getValues("tmpdirs")
								.toArray(new String[] {}),
						BooleanDatatype.getValues("loadindexinfo").get(0),
						(PARALLELOPERANDS) EnumDatatype.getValues(
						"paralleloperands.type").get(0),
						BooleanDatatype.getValues("paralleloperands.blockwise").get(0),
						BooleanDatatype.getValues("paralleloperands.boundedQueue").get(
						0) ? IntegerDatatype
						.getValues("paralleloperands.limit").get(0) : 0,
						IntegerDatatype.getValues("jointhreads").get(0),
						IntegerDatatype.getValues("joinbuffer").get(0),
						(Heap.HEAPTYPE) EnumDatatype.getValues("heap").get(0),
						(ToSort.TOSORT) EnumDatatype.getValues("tosort").get(0),
						IntegerDatatype.getValues("indexheap").get(0),
						IntegerDatatype.getValues("mergeheapheight").get(0),
						(Heap.HEAPTYPE) EnumDatatype.getValues("mergeheaptype").get(0),
						IntegerDatatype.getValues("chunk").get(0),
						IntegerDatatype.getValues("mergethreads").get(0),
						(BooleanDatatype.getValues("yagomax.enableYagoMax").get(0)) ? IntegerDatatype
								.getValues("yagomax.yagomaxNumber").get(0) : -1,
						(SuperTrie.TRIETYPE) EnumDatatype.getValues("stringsearch")
								.get(0),
						(QueryResult.TYPE) EnumDatatype.getValues("result")
								.get(0),
						(STORAGE) EnumDatatype.getValues("storage").get(0),
						(JOIN) EnumDatatype.getValues("join").get(0),
						(JOIN) EnumDatatype.getValues("optional").get(0),
						(SORT) EnumDatatype.getValues("sort").get(0),
						(DISTINCT) EnumDatatype.getValues("distinct").get(0),
						(MERGE_JOIN_OPTIONAL) EnumDatatype.getValues(
								"merge_join_optional").get(0),
						CollectionDatatype.getFirstValue("encoding"),
				(lupos.engine.operators.index.Indices.DATA_STRUCT) EnumDatatype.getValues("datastructure").get(0),
				(Dataset.SORT) EnumDatatype.getValues("sortduringindexconstruction").get(0),
				(MemoryIndexQueryEvaluator.Optimizations) EnumDatatype.getValues("optimizationInMemory").get(0)
				);
	}
	
	public static StreamQueryEvaluator createStreamQueryEvaluator(XPref xpref) throws Exception{
		return new StreamQueryEvaluator((DEBUG) EnumDatatype.getValues("debug").get(0),
				BooleanDatatype.getValues("multiplequeries").get(0),
				(compareEvaluator) EnumDatatype.getValues("compare").get(0),
				StringDatatype.getValues("compareoptions").get(0),
				BooleanDatatype.getFirstValue("times.enableRepetitions") ? IntegerDatatype
						.getValues("times.repetitionNumber").get(0)
						: 0,
				StringDatatype.getValues("dataset").get(0),
				((BooleanDatatype.getValues("formatLUPOSDATE.multiple").get(0)) ? "MULTIPLE"
						: "")
						+ EnumDatatype.getValues("formatLUPOSDATE.format").get(0),
						(BooleanDatatype
								.getValues("externalontology.enableExternalOntology")
								.get(0)) ? (EnumDatatype
								.getValues("externalontology.formatExternalOntology")
								.get(0))
								+ ","
								+ FileChooseDatatype
										.getFirstValue("externalontology.fileExternalOntology")
								: null,
						BooleanDatatype.getValues(
								"externalontology.inmemoryexternalontologyinference")
								.get(0),
						(RDFS) EnumDatatype.getValues("rdfsLUPOSDATE").get(0),
						(LiteralFactory.MapType) EnumDatatype.getValues("codemap").get(
								0),
						FileChooseDatatype.getValues("tmpdirs")
								.toArray(new String[] {}),
						BooleanDatatype.getValues("loadindexinfo").get(0),
						(PARALLELOPERANDS) EnumDatatype.getValues(
						"paralleloperands.type").get(0),
						BooleanDatatype.getValues("paralleloperands.blockwise").get(0),
						BooleanDatatype.getValues("paralleloperands.boundedQueue").get(
						0) ? IntegerDatatype
						.getValues("paralleloperands.limit").get(0) : 0,
						IntegerDatatype.getValues("jointhreads").get(0),
						IntegerDatatype.getValues("joinbuffer").get(0),
						(Heap.HEAPTYPE) EnumDatatype.getValues("heap").get(0),
						(ToSort.TOSORT) EnumDatatype.getValues("tosort").get(0),
						IntegerDatatype.getValues("indexheap").get(0),
						IntegerDatatype.getValues("mergeheapheight").get(0),
						(Heap.HEAPTYPE) EnumDatatype.getValues("mergeheaptype").get(0),
						IntegerDatatype.getValues("chunk").get(0),
						IntegerDatatype.getValues("mergethreads").get(0),
						(BooleanDatatype.getValues("yagomax.enableYagoMax").get(0)) ? IntegerDatatype
								.getValues("yagomax.yagomaxNumber").get(0) : -1,
						(SuperTrie.TRIETYPE) EnumDatatype.getValues("stringsearch")
								.get(0),
						(QueryResult.TYPE) EnumDatatype.getValues("result")
								.get(0),
						(STORAGE) EnumDatatype.getValues("storage").get(0),
						(JOIN) EnumDatatype.getValues("join").get(0),
						(JOIN) EnumDatatype.getValues("optional").get(0),
						(SORT) EnumDatatype.getValues("sort").get(0),
						(DISTINCT) EnumDatatype.getValues("distinct").get(0),
						(MERGE_JOIN_OPTIONAL) EnumDatatype.getValues(
								"merge_join_optional").get(0),
						CollectionDatatype.getFirstValue("encoding"),
						(MATCHER) EnumDatatype.getValues("matcher").get(0));
	}

	public static JenaQueryEvaluator createJenaQueryEvaluator(XPref xpref) throws Exception{
		return new JenaQueryEvaluator((DEBUG) EnumDatatype.getValues("debug").get(0),
		BooleanDatatype.getValues("multiplequeries").get(0),
		(compareEvaluator) EnumDatatype.getValues("compare").get(0),
		StringDatatype.getValues("compareoptions").get(0),
		BooleanDatatype.getFirstValue("times.enableRepetitions") ? IntegerDatatype
				.getValues("times.repetitionNumber").get(0)
				: 0, StringDatatype.getValues("dataset").get(0),
				(ONTOLOGY)EnumDatatype.getFirstValue("ontologyJena"),
				(FORMAT)EnumDatatype.getFirstValue("formatJena"));
	}
	
	public static SesameQueryEvaluator createSesameQueryEvaluator(XPref xpref) throws Exception{
		return new SesameQueryEvaluator((DEBUG) EnumDatatype.getValues("debug").get(0),
				BooleanDatatype.getValues("multiplequeries").get(0),
				(compareEvaluator) EnumDatatype.getValues("compare").get(0),
				StringDatatype.getValues("compareoptions").get(0),
				BooleanDatatype.getFirstValue("times.enableRepetitions") ? IntegerDatatype
						.getValues("times.repetitionNumber").get(0)
						: 0,
				StringDatatype.getValues("dataset").get(0),
				(SesameQueryEvaluator.ONTOLOGY) EnumDatatype.getValues("ontologySesame").get(0),
				(SesameQueryEvaluator.FORMAT) EnumDatatype.getValues("formatSesame").get(0));
	}
}
