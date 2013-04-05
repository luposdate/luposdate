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
import lupos.sparql1_1.Node;
import lupos.misc.Helper;

import xpref.datatypes.BooleanDatatype;
import xpref.datatypes.CollectionDatatype;
import xpref.datatypes.EnumDatatype;
import xpref.datatypes.FileChooseDatatype;
import xpref.datatypes.IntegerDatatype;
import xpref.datatypes.StringDatatype;

public final class EvaluatorCreator {

	public enum EVALUATORS{
		RDF3X {
			@Override
			public QueryEvaluator<Node> create() throws Exception{
				return EvaluatorCreator.createRDF3XQueryEvaluator();
			}
		}, Memory {
			@Override
			public QueryEvaluator<Node> create() throws Exception{
				return EvaluatorCreator.createMemoryIndexQueryEvaluator();
			}
		}, Stream {
			@Override
			public QueryEvaluator<Node> create() throws Exception{
				return EvaluatorCreator.createStreamQueryEvaluator();
			}			
		}, Jena {
			@Override
			public QueryEvaluator create() throws Exception{
				return EvaluatorCreator.createJenaQueryEvaluator();
			}
		}, Sesame {
			@Override
			public QueryEvaluator create() throws Exception{
				return EvaluatorCreator.createSesameQueryEvaluator();
			}
		};
		
		public abstract QueryEvaluator create() throws Exception;
	}
	
	public static RDF3XQueryEvaluator createRDF3XQueryEvaluator() throws XPathExpressionException, SecurityException, IllegalArgumentException, ClassNotFoundException, SAXException, ParserConfigurationException, IOException, NoSuchFieldException, NoSuchMethodException, IllegalClassFormatException, IllegalAccessException, InvocationTargetException{
		return new RDF3XQueryEvaluator(
				(DEBUG) Helper.castEnum(EnumDatatype.getValues("debug").get(0)),
				BooleanDatatype.getValues("multiplequeries").get(0),
				(compareEvaluator) Helper.castEnum(EnumDatatype.getValues("compare").get(0)),
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
						(RDFS) Helper.castEnum(EnumDatatype.getValues("rdfsLUPOSDATE").get(0)),
						(LiteralFactory.MapType) Helper.castEnum(EnumDatatype.getValues("codemap").get(0)),
						FileChooseDatatype.getValues("tmpdirs")
								.toArray(new String[] {}),
						BooleanDatatype.getValues("loadindexinfo").get(0),
						(PARALLELOPERANDS) Helper.castEnum(EnumDatatype.getValues("paralleloperands.type").get(0)),
						BooleanDatatype.getValues("paralleloperands.blockwise").get(0),
						BooleanDatatype.getValues("paralleloperands.boundedQueue").get(
						0) ? IntegerDatatype
						.getValues("paralleloperands.limit").get(0) : 0,
						IntegerDatatype.getValues("jointhreads").get(0),
						IntegerDatatype.getValues("joinbuffer").get(0),
						(Heap.HEAPTYPE) Helper.castEnum(EnumDatatype.getValues("heap").get(0)),
						(ToSort.TOSORT) Helper.castEnum(EnumDatatype.getValues("tosort").get(0)),
						IntegerDatatype.getValues("indexheap").get(0),
						IntegerDatatype.getValues("mergeheapheight").get(0),
						(Heap.HEAPTYPE) Helper.castEnum(EnumDatatype.getValues("mergeheaptype").get(0)),
						IntegerDatatype.getValues("chunk").get(0),
						IntegerDatatype.getValues("mergethreads").get(0),
						(BooleanDatatype.getValues("yagomax.enableYagoMax").get(0)) ? IntegerDatatype
								.getValues("yagomax.yagomaxNumber").get(0) : -1,
						(QueryResult.TYPE) Helper.castEnum(EnumDatatype.getValues("result").get(0)),
						(STORAGE) Helper.castEnum(EnumDatatype.getValues("storage").get(0)),
						(JOIN) Helper.castEnum(EnumDatatype.getValues("join").get(0)),
						(JOIN) Helper.castEnum(EnumDatatype.getValues("optional").get(0)),
						(SORT) Helper.castEnum(EnumDatatype.getValues("sort").get(0)),
						(DISTINCT) Helper.castEnum(EnumDatatype.getValues("distinct").get(0)),
						(MERGE_JOIN_OPTIONAL) Helper.castEnum(EnumDatatype.getValues("merge_join_optional").get(0)),
						CollectionDatatype.getFirstValue("encoding"),
				(lupos.engine.operators.index.Indices.DATA_STRUCT) Helper.castEnum(EnumDatatype.getValues("datastructure").get(0)),
				(Dataset.SORT) Helper.castEnum(EnumDatatype.getValues("sortduringindexconstruction").get(0)),
				FileChooseDatatype.getFirstValue("writeindexinfo"),
				 (Optimizations) Helper.castEnum(EnumDatatype.getValues("optimizationRDF3X").get(0)));
	}
	
	public static MemoryIndexQueryEvaluator createMemoryIndexQueryEvaluator() throws XPathExpressionException, SecurityException, IllegalArgumentException, ClassNotFoundException, SAXException, ParserConfigurationException, IOException, NoSuchFieldException, NoSuchMethodException, IllegalClassFormatException, IllegalAccessException, InvocationTargetException{
		return new MemoryIndexQueryEvaluator((DEBUG) Helper.castEnum(EnumDatatype.getValues("debug").get(0)),
				BooleanDatatype.getValues("multiplequeries").get(0),
				(compareEvaluator) Helper.castEnum(EnumDatatype.getValues("compare").get(0)),
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
						(RDFS) Helper.castEnum(EnumDatatype.getValues("rdfsLUPOSDATE").get(0)),
						(LiteralFactory.MapType) Helper.castEnum(EnumDatatype.getValues("codemap").get(0)),
						FileChooseDatatype.getValues("tmpdirs").toArray(new String[] {}),
						BooleanDatatype.getValues("loadindexinfo").get(0),
						(PARALLELOPERANDS) Helper.castEnum(EnumDatatype.getValues("paralleloperands.type").get(0)),
						BooleanDatatype.getValues("paralleloperands.blockwise").get(0),
						BooleanDatatype.getValues("paralleloperands.boundedQueue").get(
						0) ? IntegerDatatype
						.getValues("paralleloperands.limit").get(0) : 0,
						IntegerDatatype.getValues("jointhreads").get(0),
						IntegerDatatype.getValues("joinbuffer").get(0),
						(Heap.HEAPTYPE) Helper.castEnum(EnumDatatype.getValues("heap").get(0)),
						(ToSort.TOSORT) Helper.castEnum(EnumDatatype.getValues("tosort").get(0)),
						IntegerDatatype.getValues("indexheap").get(0),
						IntegerDatatype.getValues("mergeheapheight").get(0),
						(Heap.HEAPTYPE) Helper.castEnum(EnumDatatype.getValues("mergeheaptype").get(0)),
						IntegerDatatype.getValues("chunk").get(0),
						IntegerDatatype.getValues("mergethreads").get(0),
						(BooleanDatatype.getValues("yagomax.enableYagoMax").get(0)) ? IntegerDatatype
								.getValues("yagomax.yagomaxNumber").get(0) : -1,
						(QueryResult.TYPE) Helper.castEnum(EnumDatatype.getValues("result").get(0)),
						(STORAGE) Helper.castEnum(EnumDatatype.getValues("storage").get(0)),
						(JOIN) Helper.castEnum(EnumDatatype.getValues("join").get(0)),
						(JOIN) Helper.castEnum(EnumDatatype.getValues("optional").get(0)),
						(SORT) Helper.castEnum(EnumDatatype.getValues("sort").get(0)),
						(DISTINCT) Helper.castEnum(EnumDatatype.getValues("distinct").get(0)),
						(MERGE_JOIN_OPTIONAL) Helper.castEnum(EnumDatatype.getValues("merge_join_optional").get(0)),
						CollectionDatatype.getFirstValue("encoding"),
				(lupos.engine.operators.index.Indices.DATA_STRUCT) Helper.castEnum(EnumDatatype.getValues("datastructure").get(0)),
				(Dataset.SORT) Helper.castEnum(EnumDatatype.getValues("sortduringindexconstruction").get(0)),
				(MemoryIndexQueryEvaluator.Optimizations) Helper.castEnum(EnumDatatype.getValues("optimizationInMemory").get(0))
				);
	}
	
	public static StreamQueryEvaluator createStreamQueryEvaluator() throws Exception{
		return new StreamQueryEvaluator((DEBUG) Helper.castEnum(EnumDatatype.getValues("debug").get(0)),
				BooleanDatatype.getValues("multiplequeries").get(0),
				(compareEvaluator) Helper.castEnum(EnumDatatype.getValues("compare").get(0)),
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
						(RDFS) Helper.castEnum(EnumDatatype.getValues("rdfsLUPOSDATE").get(0)),
						(LiteralFactory.MapType) Helper.castEnum(EnumDatatype.getValues("codemap").get(0)),
						FileChooseDatatype.getValues("tmpdirs").toArray(new String[] {}),
						BooleanDatatype.getValues("loadindexinfo").get(0),
						(PARALLELOPERANDS) Helper.castEnum(EnumDatatype.getValues("paralleloperands.type").get(0)),
						BooleanDatatype.getValues("paralleloperands.blockwise").get(0),
						BooleanDatatype.getValues("paralleloperands.boundedQueue").get(
						0) ? IntegerDatatype
						.getValues("paralleloperands.limit").get(0) : 0,
						IntegerDatatype.getValues("jointhreads").get(0),
						IntegerDatatype.getValues("joinbuffer").get(0),
						(Heap.HEAPTYPE) Helper.castEnum(EnumDatatype.getValues("heap").get(0)),
						(ToSort.TOSORT) Helper.castEnum(EnumDatatype.getValues("tosort").get(0)),
						IntegerDatatype.getValues("indexheap").get(0),
						IntegerDatatype.getValues("mergeheapheight").get(0),
						(Heap.HEAPTYPE) Helper.castEnum(EnumDatatype.getValues("mergeheaptype").get(0)),
						IntegerDatatype.getValues("chunk").get(0),
						IntegerDatatype.getValues("mergethreads").get(0),
						(BooleanDatatype.getValues("yagomax.enableYagoMax").get(0)) ? IntegerDatatype.getValues("yagomax.yagomaxNumber").get(0) : -1,
						(QueryResult.TYPE) Helper.castEnum(EnumDatatype.getValues("result").get(0)),
						(STORAGE) Helper.castEnum(EnumDatatype.getValues("storage").get(0)),
						(JOIN) Helper.castEnum(EnumDatatype.getValues("join").get(0)),
						(JOIN) Helper.castEnum(EnumDatatype.getValues("optional").get(0)),
						(SORT) Helper.castEnum(EnumDatatype.getValues("sort").get(0)),
						(DISTINCT) Helper.castEnum(EnumDatatype.getValues("distinct").get(0)),
						(MERGE_JOIN_OPTIONAL) Helper.castEnum(EnumDatatype.getValues("merge_join_optional").get(0)),
						CollectionDatatype.getFirstValue("encoding"),
						(MATCHER) Helper.castEnum(EnumDatatype.getValues("matcher").get(0)));
	}

	public static JenaQueryEvaluator createJenaQueryEvaluator() throws Exception{
		return new JenaQueryEvaluator((DEBUG) Helper.castEnum(EnumDatatype.getValues("debug").get(0)),
		BooleanDatatype.getValues("multiplequeries").get(0),
		(compareEvaluator) Helper.castEnum(EnumDatatype.getValues("compare").get(0)),
		StringDatatype.getValues("compareoptions").get(0),
		BooleanDatatype.getFirstValue("times.enableRepetitions") ? IntegerDatatype
				.getValues("times.repetitionNumber").get(0)
				: 0, StringDatatype.getValues("dataset").get(0),
				(ONTOLOGY) Helper.castEnum(EnumDatatype.getFirstValue("ontologyJena")),
				(FORMAT) Helper.castEnum(EnumDatatype.getFirstValue("formatJena")));
	}
	
	public static SesameQueryEvaluator createSesameQueryEvaluator() throws Exception{
		return new SesameQueryEvaluator((DEBUG) Helper.castEnum(EnumDatatype.getValues("debug").get(0)),
				BooleanDatatype.getValues("multiplequeries").get(0),
				(compareEvaluator) Helper.castEnum(EnumDatatype.getValues("compare").get(0)),
				StringDatatype.getValues("compareoptions").get(0),
				BooleanDatatype.getFirstValue("times.enableRepetitions") ? IntegerDatatype.getValues("times.repetitionNumber").get(0) : 0,
				StringDatatype.getValues("dataset").get(0),
				(SesameQueryEvaluator.ONTOLOGY) Helper.castEnum(EnumDatatype.getValues("ontologySesame").get(0)),
				(SesameQueryEvaluator.FORMAT) Helper.castEnum(EnumDatatype.getValues("formatSesame").get(0)));
	}
}