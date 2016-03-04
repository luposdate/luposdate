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
package lupos.engine.indexconstruction;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.items.IntArrayComparator;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.paged_dbbptree.IntArrayDBBPTreeStatistics;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.IntArrayNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.NodeDeSerializer;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.indexconstruction.FastRDF3XIndexConstruction.IteratorFromRun;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.io.helper.OutHelper;
import lupos.misc.util.ImmutableIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastRDF3XIntArrayIndexConstruction {

	private static final Logger log = LoggerFactory.getLogger(FastRDF3XIndexConstruction.class);

	private FastRDF3XIntArrayIndexConstruction() {
	}

	public static void main(final String[] args) {

		log.info("Starting program to construct an RDF3X Index for LUPOSDATE...");
		log.debug("[help is printed when using less than 5 command line arguments]");
		log.debug("_______________________________________________________________");

		if (args.length < 5) {
			log.error("Usage: java -Xmx768M lupos.engine.indexconstruction.FastRDF3XIntArrayIndexConstruction <datafile> <dataformat> <encoding> <NONE|BZIP2|HUFFMAN|GZIP> <directory for indices> [LIMIT_TRIPLES_IN_MEMORY [<datafile2> [<datafile3> ...]]]");
			log.error("Example: java -Xmx768M lupos.engine.indexconstruction.FastRDF3XIntArrayIndexConstruction data.n3 N3 UTF-8 NONE /luposdateindex 500000");
			return;
		}

		FastRDF3XIndexConstruction.main(FastRDF3XIntArrayIndexConstruction::generateIndicesAndWriteOut, args);
	}

	@SuppressWarnings("rawtypes")
	protected static int generateIndicesAndWriteOut(final Collection<URILiteral> defaultGraphs, final int size, final String dir, final String writeindexinfo) throws IOException{
		// generate indices (evaluation indices plus histogram indices)
		@SuppressWarnings({ "unchecked" })
		final DBBPTree<int[], int[]>[] evaluationIndices = new DBBPTree[CollationOrder.values().length];
		final IntArrayDBBPTreeStatistics[] histogramIndices = new IntArrayDBBPTreeStatistics[CollationOrder.values().length];

		int i=0;
		for(int primaryPos=0; primaryPos<3; primaryPos++) {
			final int other_condition1 = (primaryPos==0)?1:0;
			final int other_condition2 = (primaryPos==2)?1:2;
			final CollationOrder order1 = CollationOrder.valueOf(FastRDF3XIndexConstruction.map[primaryPos] + FastRDF3XIndexConstruction.map[other_condition1] + FastRDF3XIndexConstruction.map[other_condition2]);
			final String prefixFilename = dir + FastRDF3XIndexConstruction.map[primaryPos] + "_Final_Run_";
			final Comparator<int[]> comparator1 = new IntArrayComparator(order1);
			evaluationIndices[i] = FastRDF3XIntArrayIndexConstruction.generateEvaluationIndex(comparator1, order1, new GeneratorFromFinalRun(prefixFilename + FastRDF3XIndexConstruction.map[other_condition1] + FastRDF3XIndexConstruction.map[other_condition2], size, primaryPos, other_condition1, other_condition2));
			histogramIndices[i] = FastRDF3XIntArrayIndexConstruction.generateHistogramIndex(comparator1, order1, evaluationIndices[i]);

			i++;
			final CollationOrder order2 = CollationOrder.valueOf(FastRDF3XIndexConstruction.map[primaryPos] + FastRDF3XIndexConstruction.map[other_condition2] + FastRDF3XIndexConstruction.map[other_condition1]);
			final Comparator<int[]> comparator2 = new IntArrayComparator(order2);
			evaluationIndices[i] = FastRDF3XIntArrayIndexConstruction.generateEvaluationIndex(comparator2, order2, new GeneratorFromFinalRun(prefixFilename + FastRDF3XIndexConstruction.map[other_condition2] + FastRDF3XIndexConstruction.map[other_condition1], size, primaryPos, other_condition2, other_condition1));
			histogramIndices[i] = FastRDF3XIntArrayIndexConstruction.generateHistogramIndex(comparator2, order2, evaluationIndices[i]);
			i++;
		}

		// write out index info

		final OutputStream out = new BufferedOutputStream(new FileOutputStream(writeindexinfo));

		BufferManager.getBufferManager().writeAllModifiedPages();

		OutHelper.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree.getCurrentFileID(), out);

		// write out dictionary
		((lupos.datastructures.paged_dbbptree.DBBPTree) ((StringIntegerMapJava) LazyLiteral.getHm()).getOriginalMap()).writeLuposObject(out);
		((StringArray) LazyLiteral.getV()).writeLuposStringArray(out);

		// write out default graphs
		OutHelper.writeLuposInt(1, out); // only one default graph
		LiteralFactory.writeLuposLiteral(defaultGraphs.iterator().next(), out);

		// write out evaluation indices
		for(int j=0; j<evaluationIndices.length; j++){
			evaluationIndices[j].writeLuposObject(out);
		}


		// write out histogram indices
		for(int j=0; j<histogramIndices.length; j++){
			histogramIndices[j].writeLuposObject(out);
		}

		//write out named graphs
		OutHelper.writeLuposInt(0, out); // no named graphs!
		out.close();
		return evaluationIndices[0].size();
	}

	 public static DBBPTree<int[], int[]> generateEvaluationIndex(final  Comparator<int[]> comparator, final CollationOrder order, final Generator<int[], int[]> generator) throws IOException {
     	final NodeDeSerializer<int[], int[]> nodeDeSerializer = new IntArrayNodeDeSerializer(order);
     	final DBBPTree<int[], int[]> tree = new DBBPTree<int[], int[]>(comparator, FastRDF3XIndexConstruction.k, FastRDF3XIndexConstruction.k_, nodeDeSerializer, int[].class, int[].class);
     	tree.generateDBBPTree(generator);
     	return tree;
	 }

	 public static IntArrayDBBPTreeStatistics generateHistogramIndex(final  Comparator<int[]> comparator, final CollationOrder order, final DBBPTree<int[], int[]> evaluationIndex) throws IOException {
		 final IntArrayDBBPTreeStatistics tree = new IntArrayDBBPTreeStatistics(comparator, FastRDF3XIndexConstruction.k, FastRDF3XIndexConstruction.k_, order);
		 tree.generateDBBPTree(evaluationIndex);
		 return tree;
	 }

	 /**
	  * This is just for generating B+-trees from the final run
	  */
	 public static class GeneratorFromFinalRun implements Generator<int[], int[]>{

		 private final String filename;
		 private final int size;
		 private int primaryMap;
		 private int secondaryMap;
		 private int tertiaryMap;
		 private final CollationOrder order;

		 public GeneratorFromFinalRun(final String filename, final int size, final int primaryPos, final int secondaryPos, final int tertiaryPos) {
			 this.filename = filename;
			 this.size = size;
			 switch(primaryPos){
			 default:
			 case 0:
				 this.primaryMap = 0;
				 break;
			 case 1:
				 this.secondaryMap = 0;
				 break;
			 case 2:
				 this.tertiaryMap = 0;
				 break;
			 }
			 switch(secondaryPos){
			 default:
			 case 0:
				 this.primaryMap = 1;
				 break;
			 case 1:
				 this.secondaryMap = 1;
				 break;
			 case 2:
				 this.tertiaryMap = 1;
				 break;
			 }
			 switch(tertiaryPos){
			 default:
			 case 0:
				 this.primaryMap = 2;
				 break;
			 case 1:
				 this.secondaryMap = 2;
				 break;
			 case 2:
				 this.tertiaryMap = 2;
				 break;
			 }
			 this.order = CollationOrder.createCollationOrder(primaryPos, secondaryPos);
		 }

		 @Override
		 public int size() {
			 return this.size;
		 }

		 @Override
		 public Iterator<Entry<int[], int[]>> iterator() {
			 try {
				 return new ImmutableIterator<Entry<int[], int[]>>(){

					 IteratorFromRun it = new IteratorFromRun(GeneratorFromFinalRun.this.filename);
					 int[] triple = this.it.next();

					 @Override
					 public boolean hasNext() {
						 return this.triple!=null;
					 }

					 @Override
					 public Entry<int[], int[]> next() {
						 if(this.triple==null){
							 return null;
						 }
						 final int[] t = new int[]{
								 this.triple[GeneratorFromFinalRun.this.primaryMap],
								 this.triple[GeneratorFromFinalRun.this.secondaryMap],
								 this.triple[GeneratorFromFinalRun.this.tertiaryMap]};


						 try {
							 this.triple = this.it.next();

						 } catch (final IOException e) {
							 log.error(e.getMessage(), e);
							 this.triple = null;
						 }

						 return new MapEntry<int[], int[]>(t, t);
					 }
				 };
			 } catch (final IOException e) {
				 log.error(e.getMessage(), e);
			 }
			 return null;
		 }

	 }
}
