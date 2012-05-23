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
package lupos.engine.operators.index.adaptedRDF3X;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.dbmergesortedds.DBMergeSortedMap;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.datastructures.items.TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.TripleKeyComparator;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.LazyLiteralNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.NodeDeSerializer;
import lupos.datastructures.paged_dbbptree.OptimizedDBBPTreeGeneration;
import lupos.datastructures.paged_dbbptree.PrefixSearchMinMax;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.datastructures.sorteddata.PrefixSearch;
import lupos.datastructures.sorteddata.PrefixSearchFromSortedMap;
import lupos.datastructures.trie.SuperTrie;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex.CollationOrder;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class SixIndices extends Indices {

    protected PrefixSearchMinMax<TripleKey, Triple> SPO;
    protected PrefixSearchMinMax<TripleKey, Triple> SOP;
    protected PrefixSearchMinMax<TripleKey, Triple> OSP;
    protected PrefixSearchMinMax<TripleKey, Triple> OPS;
    protected PrefixSearchMinMax<TripleKey, Triple> PSO;
    protected PrefixSearchMinMax<TripleKey, Triple> POS;

    protected lupos.datastructures.paged_dbbptree.LazyLiteralTripleKeyDBBPTreeStatistics[] statisticsIndicesForFastHistogramComputation = null;

    protected static final int k = 1000;
    protected static final int k_ = 500;

    public SixIndices() {
        init(SixIndices.usedDatastructure);
    }

    public lupos.datastructures.paged_dbbptree.LazyLiteralTripleKeyDBBPTreeStatistics getDBBPTreeStatistics(final CollationOrder order) {
        return statisticsIndicesForFastHistogramComputation[order.ordinal()];
    }

    public PrefixSearchMinMax<TripleKey, Triple> getDatastructure(final CollationOrder order) {
        try {
            if (SixIndices.usedDatastructure == DATA_STRUCT.DBBPTREE) {

                NodeDeSerializer<TripleKey, Triple> nodeDeSerializer = (LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERAL || LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) ? new LazyLiteralNodeDeSerializer(order) : new StandardNodeDeSerializer<TripleKey, Triple>(TripleKey.class, Triple.class);

                DBBPTree<TripleKey, Triple> dbbptree = new DBBPTree<TripleKey, Triple>(null, k, k_, nodeDeSerializer);

                dbbptree.setName(order.toString());

                return new OptimizedDBBPTreeGeneration<TripleKey, Triple>(new DBMergeSortedMap<TripleKey, Triple>(HEAPHEIGHT, (Class<lupos.datastructures.dbmergesortedds.MapEntry<TripleKey, Triple>>) (new lupos.datastructures.dbmergesortedds.MapEntry<TripleKey, Triple>(null, null)).getClass()), dbbptree);
            } else
                return new PrefixSearchFromSortedMap<TripleKey, Triple>(new TreeMap<TripleKey, Triple>(new TripleKeyComparator(new TripleComparator(order))));

        } catch (final IOException e) {
            System.err.println(e);
            e.printStackTrace();
            return null;
        }
    }

    public SixIndices(final URILiteral uriLiteral) {
        rdfName = uriLiteral;
        init(SixIndices.usedDatastructure);
    }

    public SixIndices(final URILiteral uriLiteral, final boolean initialize) {
        rdfName = uriLiteral;
        if (initialize)
            init(SixIndices.usedDatastructure);
    }

    @Override
    public boolean add(final Triple t) {
        addTriple(t);
        return true;
    }

    protected Adder[] adders = null;
    protected BoundedBuffer<Triple>[] boundedBuffersForAdders = null;
    protected final static int MAXBUFFER = 1000;
    protected final static boolean parallel = true;

    private void addTriple(final Triple t) {
        if (parallel) {
            if (boundedBuffersForAdders == null) {
                boundedBuffersForAdders = new BoundedBuffer[6];
                boundedBuffersForAdders[0] = new BoundedBuffer<Triple>(MAXBUFFER);
                boundedBuffersForAdders[1] = new BoundedBuffer<Triple>(MAXBUFFER);
                boundedBuffersForAdders[2] = new BoundedBuffer<Triple>(MAXBUFFER);
                boundedBuffersForAdders[3] = new BoundedBuffer<Triple>(MAXBUFFER);
                boundedBuffersForAdders[4] = new BoundedBuffer<Triple>(MAXBUFFER);
                boundedBuffersForAdders[5] = new BoundedBuffer<Triple>(MAXBUFFER);
                adders = new Adder[6];
                adders[0] = new Adder(boundedBuffersForAdders[0], CollationOrder.SPO, SPO);
                adders[1] = new Adder(boundedBuffersForAdders[1], CollationOrder.SOP, SOP);
                adders[2] = new Adder(boundedBuffersForAdders[2], CollationOrder.PSO, PSO);
                adders[3] = new Adder(boundedBuffersForAdders[3], CollationOrder.POS, POS);
                adders[4] = new Adder(boundedBuffersForAdders[4], CollationOrder.OSP, OSP);
                adders[5] = new Adder(boundedBuffersForAdders[5], CollationOrder.OPS, OPS);
                for (final Thread thread : adders) {
                    thread.start();
                }
            }
            for (int i = 0; i < boundedBuffersForAdders.length; i++) {
                try {
                    boundedBuffersForAdders[i].put(t);
                } catch (final InterruptedException e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        } else {
            SPO.put(new TripleKey(t, new TripleComparator(CollationOrder.SPO)), t);
            SOP.put(new TripleKey(t, new TripleComparator(CollationOrder.SOP)), t);
            PSO.put(new TripleKey(t, new TripleComparator(CollationOrder.PSO)), t);
            POS.put(new TripleKey(t, new TripleComparator(CollationOrder.POS)), t);
            OSP.put(new TripleKey(t, new TripleComparator(CollationOrder.OSP)), t);
            OPS.put(new TripleKey(t, new TripleComparator(CollationOrder.OPS)), t);
        }
    }

    private void waitForAdderThreads() {
        if (adders != null) {
            for (int i = 0; i < 6; i++)
                adders[i].getBoundedBuffer().endOfData();
            for (int i = 0; i < 6; i++) {
                try {
                    adders[i].join();
                } catch (final InterruptedException e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
            adders = null;
            boundedBuffersForAdders = null;
        }
    }

    @Override
    public void build() {
        waitForAdderThreads();
    }

    @Override
    public boolean contains(final Triple t) {
        return (SPO.get(new TripleKey(t, new TripleComparator(CollationOrder.SPO))) != null);
    }
    
    public int size(){
    	return SPO.size();
    }

    @Override
    public void init(final DATA_STRUCT ds) {
        SixIndices.usedDatastructure = ds;
        // the initialization for PREPHASE.INDEPENDANTBAGS must be done after
        // the prephase!
        SPO = getDatastructure(CollationOrder.SPO);
        SOP = getDatastructure(CollationOrder.SOP);
        PSO = getDatastructure(CollationOrder.PSO);
        POS = getDatastructure(CollationOrder.POS);
        OSP = getDatastructure(CollationOrder.OSP);
        OPS = getDatastructure(CollationOrder.OPS);
    }

    @Override
    public boolean remove(final Triple t) {

        SPO.remove(new TripleKey(t, new TripleComparator(CollationOrder.SPO)));
        SOP.remove(new TripleKey(t, new TripleComparator(CollationOrder.SOP)));
        PSO.remove(new TripleKey(t, new TripleComparator(CollationOrder.PSO)));
        POS.remove(new TripleKey(t, new TripleComparator(CollationOrder.POS)));
        OSP.remove(new TripleKey(t, new TripleComparator(CollationOrder.OSP)));
        OPS.remove(new TripleKey(t, new TripleComparator(CollationOrder.OPS)));
        return true;
    }

    public Iterator<Triple> evaluateTriplePattern(final TriplePattern tp) {
        return RDF3XIndex.getIterator(this, RDF3XIndex.getKey(tp, null), RDF3XIndex.getCollationOrder(tp, null), null, null);
    }

    @Override
    public void constructCompletely() {
        this.waitForAdderThreads();
        if (SPO instanceof OptimizedDBBPTreeGeneration) {
            if (((OptimizedDBBPTreeGeneration) SPO).generatedCompletely() && ((OptimizedDBBPTreeGeneration) SOP).generatedCompletely() && ((OptimizedDBBPTreeGeneration) PSO).generatedCompletely() && ((OptimizedDBBPTreeGeneration) POS).generatedCompletely() && ((OptimizedDBBPTreeGeneration) OSP).generatedCompletely() && ((OptimizedDBBPTreeGeneration) OPS).generatedCompletely())
                return;
            if (Indices.usedDatastructure == Indices.DATA_STRUCT.DBBPTREE && (LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERAL || LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
                this.statisticsIndicesForFastHistogramComputation = new lupos.datastructures.paged_dbbptree.LazyLiteralTripleKeyDBBPTreeStatistics[CollationOrder.values().length];
                // new
                // LazyLiteralTripleKeyDBBPTreeStatistics[CollationOrder.values
                // ().length];
                for (int i = 0; i < statisticsIndicesForFastHistogramComputation.length; i++) {
                    try {
                        this.statisticsIndicesForFastHistogramComputation[i] = new lupos.datastructures.paged_dbbptree.LazyLiteralTripleKeyDBBPTreeStatistics(null, 1500, 1500, CollationOrder.values()[i]);
                    } catch (final IOException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                    // new LazyLiteralTripleKeyDBBPTreeStatistics(null, 1500,
                    // 1500, SortedTripleElement.ORDER_PATTERN.values()[i]);
                }
            }
            final Thread[] threads = new Thread[6];
            threads[0] = new GenerateCompletelyRunner2((OptimizedDBBPTreeGeneration) SPO, CollationOrder.SPO);
            threads[0].start();
            threads[1] = new GenerateCompletelyRunner2((OptimizedDBBPTreeGeneration) SOP, CollationOrder.SOP);
            threads[1].start();
            threads[2] = new GenerateCompletelyRunner2((OptimizedDBBPTreeGeneration) PSO, CollationOrder.PSO);
            threads[2].start();
            threads[3] = new GenerateCompletelyRunner2((OptimizedDBBPTreeGeneration) POS, CollationOrder.POS);
            threads[3].start();
            threads[4] = new GenerateCompletelyRunner2((OptimizedDBBPTreeGeneration) OSP, CollationOrder.OSP);
            threads[4].start();
            threads[5] = new GenerateCompletelyRunner2((OptimizedDBBPTreeGeneration) OPS, CollationOrder.OPS);
            threads[5].start();
            try {
                threads[0].join();
            } catch (final InterruptedException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            try {
                threads[1].join();
            } catch (final InterruptedException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            try {
                threads[2].join();
            } catch (final InterruptedException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            try {
                threads[3].join();
            } catch (final InterruptedException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            try {
                threads[4].join();
            } catch (final InterruptedException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            try {
                threads[5].join();
            } catch (final InterruptedException e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
    }

    private class GenerateCompletelyRunner2 extends Thread {

        private final OptimizedDBBPTreeGeneration index;
        private final CollationOrder order;

        public GenerateCompletelyRunner2(final OptimizedDBBPTreeGeneration index, final CollationOrder order) {
            this.index = index;
            this.order = order;
        }

        @Override
        public void run() {
            index.generateCompletely();
            if (Indices.usedDatastructure == Indices.DATA_STRUCT.DBBPTREE && (LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERAL || LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP)) {
                try {
                    getDBBPTreeStatistics(order).generateDBBPTree(index.getDBBPTree().entrySet());
                } catch (IOException e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        }
    }

    private class Adder extends Thread {

        protected final BoundedBuffer<Triple> bbt;
        protected final CollationOrder order;
        protected final PrefixSearch ps;

        public Adder(final BoundedBuffer<Triple> bbt, final CollationOrder order, final PrefixSearch ps) {
            this.bbt = bbt;
            this.order = order;
            this.ps = ps;
        }

        public BoundedBuffer<Triple> getBoundedBuffer() {
            return bbt;
        }

        @Override
        public void run() {
            try {
                while (bbt.hasNext()) {
                    final Triple t = bbt.get();
                    ps.put(new TripleKey(t, new TripleComparator(order)), t);
                }
            } catch (final InterruptedException e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
    }

    private void makeLazyLiteral(final int pos, final SortedSet<Triple> dsst_current, final SortedSet<Triple> dsst_next) {
        makeLazyLiteral(pos, dsst_current, new TripleConsumer() {

            @Override
            public void consume(final Triple triple) {
                dsst_next.add(triple);
            }

        });
    }

    private void makeLazyLiteral(final int pos, final SortedSet<Triple> dsst_current, final TripleConsumer next) {
        final Iterator<java.util.Map.Entry<String, Integer>> iterator = ((StringIntegerMapJava) LazyLiteral.getHm()).getMap().entrySet().iterator();
        java.util.Map.Entry<String, Integer> current = iterator.next();
        for (final Triple t : dsst_current) {
            try {
                if (iterator instanceof SIPParallelIterator) {
                    while (t.getPos(pos).toString().compareTo(current.getKey()) != 0) {
                        current = ((SIPParallelIterator<java.util.Map.Entry<String, Integer>, String>) iterator).next(t.getPos(pos).toString());
                    }
                } else {
                    while (t.getPos(pos).toString().compareTo(current.getKey()) != 0) {
                        current = iterator.next();
                    }
                }
                // if (current == null)
                // System.err
                // .println("SixIndices: current reached the end for key:"
                // + t.getPos(pos).toString());
                // else
                // System.out.println(current.getKey());
                if (t.getPos(pos).originalStringDiffers())
                    t.setPos(pos, new LazyLiteralOriginalContent(current.getValue(), t.getPos(pos).originalString()));
                else
                    t.setPos(pos, new LazyLiteral(current.getValue()));
                next.consume(t);
            } catch (final NullPointerException e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
        if (iterator instanceof ParallelIterator)
            ((ParallelIterator) iterator).close();
    }

    protected class GenerateIDTriplesUsingStringSearch {

        ReentrantLock lock = new ReentrantLock();
        Condition waitForMapper = lock.newCondition();
        Condition waitForDictionaryBuilder = lock.newCondition();
        boolean mapped = false;
        volatile boolean dictionaryBuilt = false;
        int[] map = null;
        volatile int tripleNumberDictionaryBuilder = 0;

        protected GenerateIDTriplesUsingStringSearch(final URILiteral graphURI, final String dataFormat, final Dataset dataset, final TripleConsumer tc) throws Exception {
            dataset.waitForCodeMapConstruction();
            CommonCoreQueryEvaluator.setMultipleDataThreads(1); // necessary to
                                                                // have a
                                                                // deterministic
                                                                // sequence of
                                                                // read triples
                                                                // for both
                                                                // threads
                                                                // threadDictionaryBuilder
                                                                // and
                                                                // threadMapper!
            final SuperTrie searchtree = SuperTrie.createInstance();

            final Thread threadDictionaryBuilder = new Thread() {
                @Override
                public void run() {
                    try {
                        CommonCoreQueryEvaluator.readTriples(dataFormat, graphURI.openStream(), new TripleConsumer() {

                            @Override
                            public void consume(final Triple triple) {
                                for (final Literal l : triple) {
                                    // rdftermsRepresentations.add(l.
                                    // originalString());
                                    searchtree.add(l.toString());
                                    if (l.originalStringDiffers())
                                        searchtree.add(l.originalString());
                                }
                                tripleNumberDictionaryBuilder++;
                                if (searchtree.isFull()) {
                                    handleRun(searchtree);
                                }
                            }

                        });
                        if (searchtree.size() > 0) {
                            handleRun(searchtree);
                        }
                    } catch (final Exception e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                }
            };
            threadDictionaryBuilder.start();

            final Thread threadMapper = new Thread() {
                @Override
                public void run() {
                    try {
                        lock.lock();
                        try {
                            while (!dictionaryBuilt)
                                try {
                                    waitForDictionaryBuilder.await();
                                } catch (final InterruptedException e) {
                                    System.err.println(e);
                                    e.printStackTrace();
                                }
                            dictionaryBuilt = false;
                        } finally {
                            lock.unlock();
                        }
                        CommonCoreQueryEvaluator.readTriples(dataFormat, graphURI.openStream(), new TripleConsumer() {
                            int tripleNumberMapper = 0;

                            @Override
                            public void consume(final Triple triple) {
                                try {
                                    for (int pos = 0; pos < 3; pos++) {
                                        if (triple.getPos(pos).originalStringDiffers())
                                            triple.setPos(pos, new LazyLiteralOriginalContent(map[searchtree.getIndex(triple.getPos(pos).toString())], map[searchtree.getIndex(triple.getPos(pos).originalString())]));
                                        else
                                            triple.setPos(pos, new LazyLiteral(map[searchtree.getIndex(triple.getPos(pos).toString())]));
                                    }
                                    tc.consume(triple);
                                    tripleNumberMapper++;
                                    if (tripleNumberMapper == tripleNumberDictionaryBuilder) {
                                        mapped = true;
                                        lock.lock();
                                        try {
                                            waitForMapper.signalAll();
                                            while (!dictionaryBuilt)
                                                try {
                                                    waitForDictionaryBuilder.await();
                                                } catch (final InterruptedException e) {
                                                    System.err.println(e);
                                                    e.printStackTrace();
                                                }
                                            dictionaryBuilt = false;
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
                                } catch (final RuntimeException e) {
                                    System.err.println("Error for mapping triple:" + triple);
                                    System.err.println("tripleNumberMapper:" + tripleNumberMapper + "<-> tripleNumberDictionaryBuilder:" + tripleNumberDictionaryBuilder);
                                    System.err.println(e);
                                    e.printStackTrace();
                                    throw e;
                                }
                            }
                        });
                    } catch (final Exception e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                }
            };
            threadMapper.start();

            threadDictionaryBuilder.join();

            // handle remaining triples...
            if (searchtree.size() > 0)
                handleRun(searchtree);
            // give the mapper thread chance to terminate!
            dictionaryBuilt = true;
            lock.lock();
            try {
                waitForDictionaryBuilder.signalAll();
            } finally {
                lock.unlock();
            }
        }

        private void handleRun(final SuperTrie searchtree) {
            // new "Run"
            lock.lock();
            try {
                map = getMap(searchtree);
                dictionaryBuilt = true;
                // now read the next triples,
                // transform the ids
                // concerning the global
                // dictionary and transmit
                // them to the SixIndices Object
                waitForDictionaryBuilder.signalAll();
                // wait until the previous step
                // is ready...
                while (!mapped)
                    try {
                        waitForMapper.await();
                    } catch (final InterruptedException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                mapped = false;
                // clear the searchtree for the next
                // "Run"
                searchtree.clear();
            } finally {
                lock.unlock();
            }
        }

        private int[] getMap(final SuperTrie searchtree) {
            // build map from local dictionary to global
            // dictionary...
            final int[] map = new int[searchtree.size()];

            // get global map:
            final Iterator<java.util.Map.Entry<String, Integer>> iterator = ((StringIntegerMapJava) LazyLiteral.getHm()).getMap().entrySet().iterator();
            java.util.Map.Entry<String, Integer> current = iterator.next();

            int index = 0;
            for (final String s : searchtree) {
                if (iterator instanceof SIPParallelIterator) {
                    while (s.compareTo(current.getKey()) != 0) {
                        current = ((SIPParallelIterator<java.util.Map.Entry<String, Integer>, String>) iterator).next(s);
                    }
                } else {
                    while (s.compareTo(current.getKey()) != 0) {
                        current = iterator.next();
                    }
                }
                map[index++] = current.getValue();
            }
            return map;
        }
    }

    protected class GenerateIDTriplesUsingStringSearch2 {

        protected GenerateIDTriplesUsingStringSearch2(final URILiteral graphURI, final String dataFormat, final Dataset dataset, final TripleConsumer tc) throws Exception {
            dataset.waitForCodeMapConstruction();

            final SuperTrie searchtree = SuperTrie.createInstance();

            final DiskCollection<Triple> triples = new DiskCollection<Triple>(Triple.class);

            try {
                CommonCoreQueryEvaluator.readTriples(dataFormat, graphURI.openStream(), new TripleConsumer() {

                    @Override
                    public void consume(final Triple triple) {
                        for (final Literal l : triple) {
                            // rdftermsRepresentations.add(l.
                            // originalString());
                            searchtree.add(l.toString());
                            if (l.originalStringDiffers())
                                searchtree.add(l.originalString());
                        }
                        triples.add(triple);
                        if (searchtree.isFull()) {
                            handleRun(searchtree, triples, tc);
                        }
                    }

                });
                if (searchtree.size() > 0) {
                    handleRun(searchtree, triples, tc);
                    triples.release();
                }
            } catch (final IOException e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }

        private void handleRun(final SuperTrie searchtree, Collection<Triple> triples, final TripleConsumer tc) {
            int[] map = getMap(searchtree);

            for (Triple triple : triples) {
                Triple dummy = new Triple(triple.getPos(0), triple.getPos(1), triple.getPos(2));
                for (int pos = 0; pos < 3; pos++) {
                    if (triple.getPos(pos).originalStringDiffers())
                        dummy.setPos(pos, new LazyLiteralOriginalContent(map[searchtree.getIndex(triple.getPos(pos).toString())], map[searchtree.getIndex(triple.getPos(pos).originalString())]));
                    else
                        dummy.setPos(pos, new LazyLiteral(map[searchtree.getIndex(triple.getPos(pos).toString())]));
                }
                tc.consume(dummy);
            }

            // clear the searchtree and the triples collection for the next
            // "Run"
            searchtree.clear();
            triples.clear();
        }

        private int[] getMap(final SuperTrie searchtree) {
            // build map from local dictionary to global
            // dictionary...
            final int[] map = new int[searchtree.size()];

            // get global map:
            final Iterator<java.util.Map.Entry<String, Integer>> iterator = ((StringIntegerMapJava) LazyLiteral.getHm()).getMap().entrySet().iterator();
            java.util.Map.Entry<String, Integer> current = iterator.next();

            int index = 0;
            for (final String s : searchtree) {
                if (iterator instanceof SIPParallelIterator) {
                    while (s.compareTo(current.getKey()) != 0) {
                        current = ((SIPParallelIterator<java.util.Map.Entry<String, Integer>, String>) iterator).next(s);
                    }
                } else {
                    while (s.compareTo(current.getKey()) != 0) {
                        current = iterator.next();
                    }
                }
                map[index++] = current.getValue();
            }
            return map;
        }
    }

    @Override
    protected void loadDataWithoutConsideringOntoloy(final URILiteral graphURI, final String dataFormat, final Dataset dataset) throws Exception {
        if (LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERAL || LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
                if (Dataset.getSortingApproach() == Dataset.SORT.STRINGSEARCHTREE || Dataset.getSortingApproach() == Dataset.SORT.STRINGSEARCHTREEREPLACEMENTSELECTION) {
                    // TODO check
                    // new GenerateIDTriplesUsingStringSearch(graphURI,
                    // dataFormat, dataset, this);
                    new GenerateIDTriplesUsingStringSearch2(graphURI, dataFormat, dataset, this);
                } else {
                    final SortedSet<Triple> dsst_s;
                    dsst_s = new DBMergeSortedSet<Triple>(HEAPHEIGHT, new TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(CollationOrder.SPO), Triple.class);
                    try {
                        CommonCoreQueryEvaluator.readTriples(dataFormat, graphURI.openStream(), new TripleConsumer() {

                            @Override
                            public void consume(final Triple triple) {
                                dsst_s.add(triple);
                            }

                        });
                    } catch (final IOException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                    final SortedSet<Triple> dsst_p;
                    dsst_p = new DBMergeSortedSet<Triple>(HEAPHEIGHT, new TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(CollationOrder.PSO), Triple.class);
                    ((DBMergeSortedSet<Triple>) dsst_s).sort();
                    dataset.waitForCodeMapConstruction();
                    makeLazyLiteral(0, dsst_s, dsst_p);
                    if (dsst_s instanceof DBMergeSortedSet)
                        ((DBMergeSortedSet) dsst_s).release();
                    final SortedSet<Triple> dsst_o;
                    dsst_o = new DBMergeSortedSet<Triple>(HEAPHEIGHT, new TripleComparatorCompareStringRepresentationsOrCodeOfLazyLiteral(CollationOrder.OPS), Triple.class);
                    makeLazyLiteral(1, dsst_p, dsst_o);
                    if (dsst_p instanceof DBMergeSortedSet)
                        ((DBMergeSortedSet) dsst_p).release();
                    makeLazyLiteral(2, dsst_o, this);
                    if (dsst_o instanceof DBMergeSortedSet)
                        ((DBMergeSortedSet) dsst_o).release();
                }
        } else
        	super.loadDataWithoutConsideringOntoloy(graphURI, dataFormat, dataset);
    }

    @Override
    public void readIndexInfo(final LuposObjectInputStream in) throws IOException, ClassNotFoundException {
        SPO = DBBPTree.readLuposObject(in);
        ((DBBPTree) SPO).setName("SPO");
        SOP = DBBPTree.readLuposObject(in);
        ((DBBPTree) SOP).setName("SOP");
        PSO = DBBPTree.readLuposObject(in);
        ((DBBPTree) PSO).setName("PSO");
        POS = DBBPTree.readLuposObject(in);
        ((DBBPTree) POS).setName("POS");
        OSP = DBBPTree.readLuposObject(in);
        ((DBBPTree) OSP).setName("OSP");
        OPS = DBBPTree.readLuposObject(in);
        ((DBBPTree) OPS).setName("OPS");
        if (LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERAL || LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
            this.statisticsIndicesForFastHistogramComputation = new lupos.datastructures.paged_dbbptree.LazyLiteralTripleKeyDBBPTreeStatistics[CollationOrder.values().length];
            for (int i = 0; i < CollationOrder.values().length; i++) {
                this.statisticsIndicesForFastHistogramComputation[i] = lupos.datastructures.paged_dbbptree.LazyLiteralTripleKeyDBBPTreeStatistics.readLuposObject(in);
            }
        }
    }

    @Override
    public void writeIndexInfo(final LuposObjectOutputStream out) throws IOException {
        if (SixIndices.usedDatastructure == DATA_STRUCT.DBBPTREE) {
            if (SPO instanceof OptimizedDBBPTreeGeneration) {
                (((OptimizedDBBPTreeGeneration) SPO).getDBBPTree()).writeLuposObject(out);
                (((OptimizedDBBPTreeGeneration) SOP).getDBBPTree()).writeLuposObject(out);
                (((OptimizedDBBPTreeGeneration) PSO).getDBBPTree()).writeLuposObject(out);
                (((OptimizedDBBPTreeGeneration) POS).getDBBPTree()).writeLuposObject(out);
                (((OptimizedDBBPTreeGeneration) OSP).getDBBPTree()).writeLuposObject(out);
                (((OptimizedDBBPTreeGeneration) OPS).getDBBPTree()).writeLuposObject(out);
            } else if(SPO instanceof DBBPTree){
            	((DBBPTree) SPO).writeLuposObject(out);
            	((DBBPTree) SOP).writeLuposObject(out);
            	((DBBPTree) PSO).writeLuposObject(out);
            	((DBBPTree) POS).writeLuposObject(out);
            	((DBBPTree) OSP).writeLuposObject(out);
            	((DBBPTree) OPS).writeLuposObject(out);
            }
            if (LiteralFactory.getMapType() == LiteralFactory.MapType.LAZYLITERAL || LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP) {
                for (int i = 0; i < statisticsIndicesForFastHistogramComputation.length; i++) {
                    statisticsIndicesForFastHistogramComputation[i].writeLuposObject(out);
                }
            }
        } else
            System.err.println("Cannot write the index info: It is only a main memory index!");
    }

    @Override
    public void writeOutAllModifiedPages() throws IOException {
        if (SixIndices.usedDatastructure == DATA_STRUCT.DBBPTREE) {
        	if (SPO instanceof OptimizedDBBPTreeGeneration) {
                (((OptimizedDBBPTreeGeneration) SPO).getDBBPTree()).writeAllModifiedPages();
                (((OptimizedDBBPTreeGeneration) SOP).getDBBPTree()).writeAllModifiedPages();
                (((OptimizedDBBPTreeGeneration) PSO).getDBBPTree()).writeAllModifiedPages();
                (((OptimizedDBBPTreeGeneration) POS).getDBBPTree()).writeAllModifiedPages();
                (((OptimizedDBBPTreeGeneration) OSP).getDBBPTree()).writeAllModifiedPages();
                (((OptimizedDBBPTreeGeneration) OPS).getDBBPTree()).writeAllModifiedPages();
            } else if (SPO instanceof DBBPTree) {
            	((DBBPTree) SPO).writeAllModifiedPages();
            	((DBBPTree) SOP).writeAllModifiedPages();
            	((DBBPTree) PSO).writeAllModifiedPages();
            	((DBBPTree) POS).writeAllModifiedPages();
            	((DBBPTree) OSP).writeAllModifiedPages();
            	((DBBPTree) OPS).writeAllModifiedPages();
            }
        }
    }
}
