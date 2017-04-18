package lupos.engine.indexconstruction.implementation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import lupos.compression.Compression;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.indexconstruction.interfaces.IEndOfProcessingNotification;
import lupos.engine.indexconstruction.interfaces.IReadTriples;
import lupos.engine.indexconstruction.interfaces.ITripleConsumerWithEndNotification;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.misc.FileHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTriples implements IReadTriples {

	private static final Logger log = LoggerFactory.getLogger(ReadTriples.class);

	protected final String dataFormat;
	protected final Collection<URILiteral> defaultGraphs;
	protected final int numberOfParallelInput;
	protected final int numberOfParallelTripleConsumers;

	protected final ITripleConsumerWithEndNotification[] tripleConsumers;
	protected final IEndOfProcessingNotification secondPhase;

	/**
	 * Constructor
	 *
	 * @param numberOfParallelInput number of parsers per target
	 * @param numberOfParallelTripleConsumers number of targets, which consume triples
	 * @param compressor the used compression scheme of the input files
	 * @param dataFormat the data format of the input files (Prefix MULTIPLE, if the given files contain a list of other files to be parsed. E.g. N3 for N3 format.)
	 * @param files the files containing the triples, which are parsed
	 * @throws URISyntaxException
	 */
	public ReadTriples(final ITripleConsumerWithEndNotification[] tripleConsumers, final IEndOfProcessingNotification secondPhase, final int numberOfParallelInput, final int numberOfParallelTripleConsumers, final String compressor, final String dataFormat, final String... files) throws URISyntaxException{
		if(compressor.compareTo("BZIP2")==0){
			SortConfiguration.setDEFAULT_COMPRESSION(Compression.BZIP2);
		} else if(compressor.compareTo("HUFFMAN")==0){
			SortConfiguration.setDEFAULT_COMPRESSION(Compression.HUFFMAN);
		} else if(compressor.compareTo("GZIP")==0){
			SortConfiguration.setDEFAULT_COMPRESSION(Compression.GZIP);
		} else {
			SortConfiguration.setDEFAULT_COMPRESSION(Compression.NONE);
		}
		this.dataFormat = dataFormat;
		this.defaultGraphs = new LinkedList<URILiteral>();
		for(int i=0; i<files.length; i++) {
			this.defaultGraphs.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + files[i]+ ">"));
		}
		this.numberOfParallelInput = numberOfParallelInput;
		this.numberOfParallelTripleConsumers = numberOfParallelTripleConsumers;
		this.tripleConsumers = tripleConsumers;
		this.secondPhase = secondPhase;
	}

	@Override
	public void readTriples() {
		for(final URILiteral uri: this.defaultGraphs) {
			try {
				if(this.dataFormat.startsWith("MULTIPLE")){ // read input in parallel!
					// Parallel parsers, but each directly feeding its "private" CreateLocalDictionaryAndLocalIds => Overhead of bounded buffer (in an alternative approach) is avoided!

					final String typeWithoutMultiple = this.dataFormat.substring("MULTIPLE".length());
					final Collection<String> filenames = FileHelper.readInputStreamToCollection(uri.openStream());
					final BoundedBuffer<String> filenamesBB = new BoundedBuffer<String>(filenames.size());
					for (final String filename : filenames) {
						try {
							filenamesBB.put(filename);
						} catch (final InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
					filenamesBB.endOfData();
					final Thread[] threads = new Thread[this.numberOfParallelInput*this.numberOfParallelTripleConsumers];
					for(int j = 0; j<this.numberOfParallelTripleConsumers; j++){
						final int index = j;
						final TripleConsumer synchronizedTC = (this.numberOfParallelInput==1)? // synchronized triple consumer is not necessary for CreateParallelLocalDictionaryAndLocalIds (synchronization is done in its bounded buffer!)
								this.tripleConsumers[index]:
								new TripleConsumer() {
									@Override
									public synchronized void consume(final Triple triple) {
										ReadTriples.this.tripleConsumers[index].consume(triple);
									}
								};
						for (int i = 0; i < this.numberOfParallelInput; i++) {
							threads[j*this.numberOfParallelTripleConsumers+i] = new Thread() {
								@Override
								public void run() {
									try {
										while (filenamesBB.hasNext()) {
											final String filename = filenamesBB.get();
											if (filename == null) {
												break;
											}
											log.debug("Reading data from file: {}", filename);
											String type2;
											if(typeWithoutMultiple.compareTo("DETECT") == 0) {
												final int index = filename.lastIndexOf('.');
												if (index == -1) {
													log.error("Type of {} ould not be automatically detected! ", filename);
												}
												type2 = filename.substring(index + 1).toUpperCase();
											} else {
												type2 = typeWithoutMultiple;
											}
											try {
												CommonCoreQueryEvaluator.readTriplesWithoutMultipleFiles(type2, new BufferedInputStream(new FileInputStream(filename)), synchronizedTC);
											} catch (final Throwable e) {
												log.error(e.getMessage(), e);
											}
										}
									} catch (final InterruptedException e) {
										log.error(e.getMessage(), e);
									}
								}
							};
							threads[j*this.numberOfParallelTripleConsumers+i].start();
						}
					}
					for (int i = 0; i < threads.length; i++) {
						try {
							threads[i].join();
						} catch (final InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
				} else {
					CommonCoreQueryEvaluator.readTriples(this.dataFormat, new BufferedInputStream(uri.openStream()), this.tripleConsumers[0]);
				}
			} catch (final Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		for(final ITripleConsumerWithEndNotification tripleConsumer: this.tripleConsumers){
			tripleConsumer.notifyEndOfProcessing();
		}
		this.secondPhase.notifyEndOfProcessing();
	}
}
