package lupos.engine.indexconstruction.implementation.indices;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.indexconstruction.interfaces.IIndexContainer;
import lupos.io.helper.OutHelper;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndicesWriter {

	private static final Logger log = LoggerFactory.getLogger(IndicesWriter.class);

	private final IIndexContainer<String, Integer> dictionary;
	private final IIndexContainer<int[], int[]>[] evaluationIndices;
	private final StringArray stringArray;
	private final String writeindexinfo;
	private URILiteral defaultGraph;
	private boolean determineSpace = false;
	private final List<Tuple<String, Long>> times;

	public IndicesWriter(final Map<String, Object> configuration, final IIndexContainer<String, Integer> dictionary, final StringArray stringArray, final IIndexContainer<int[], int[]>[] evaluationIndices, final List<Tuple<String, Long>> times){
		this.dictionary = dictionary;
		this.evaluationIndices = evaluationIndices;
		this.stringArray = stringArray;
		this.writeindexinfo = (String) configuration.get("writeindexinfo");
		final String firstfile = ((String[])configuration.get("files"))[0];
		try {
			this.defaultGraph = LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + firstfile+ ">");
		} catch (final URISyntaxException e) {
			log.error(e.getMessage(), e);
			this.defaultGraph = null;
		}
		if(configuration.get("space")!=null){
			this.determineSpace = true;
		}
		this.times = times;
	}

	public void writeOut() throws Exception {
		final long start = System.currentTimeMillis();
		// write out index info
		final OutputStream out = new BufferedOutputStream(new FileOutputStream(this.writeindexinfo));

		this.dictionary.writeHeader(out);

		// write out dictionary
		this.dictionary.writeLuposObject(out);

		this.stringArray.writeLuposStringArray(out);

		// write out default graphs
		OutHelper.writeLuposInt(1, out); // only one default graph
		LiteralFactory.writeLuposLiteral(this.defaultGraph, out);

		// write out evaluation indices
		for(int j=0; j<this.evaluationIndices.length; j++){
			this.evaluationIndices[j].writeLuposObject(out);
		}

		//write out named graphs
		OutHelper.writeLuposInt(0, out); // no named graphs!
		out.close();
		BufferManager.getBufferManager().writeAllModifiedPages();
		final long end = System.currentTimeMillis();
		this.times.add(new Tuple<String, Long>("Write out modified pages and write index info file", end-start));
		if(this.determineSpace){
			this.dictionary.logProperties(this.times);
			for(int j=0; j<this.evaluationIndices.length; j++){
				this.evaluationIndices[j].logProperties(this.times);
			}
		}
	}
}
