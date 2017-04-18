package lupos.engine.indexconstruction.implementation.sorter;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is just for writing the final run
 */
public class FinalRunWriter {

	private static final Logger log = LoggerFactory.getLogger(FinalRunWriter.class);

	private final static int MAX = 8 * 1024;
	private final int[][] block = new int[FinalRunWriter.MAX][];;
	private int current = 0;
	private int previousPrimaryCode = 0;
	private final OutputStream out;

	public FinalRunWriter(final String filename) throws FileNotFoundException, IOException {
		this.out = new BufferedOutputStream(new FileOutputStream(filename));
	}

	public void write(final int[] triple) {
		if(this.current==FinalRunWriter.MAX || (this.current>0 && this.block[this.current-1][0]!=triple[0])) {
			this.writeBlock();
		}
		this.block[this.current] = triple;
		this.current++;
	}

	private void writeBlock() {
		if(this.current>0) {
			try {
				this.previousPrimaryCode = SecondaryConditionSorter.writeBlock(this.block, 0, this.current, 0, 1, 2, this.previousPrimaryCode, this.out);
			} catch (final IOException e) {
				log.error(e.getMessage(), e);
			}
			this.current = 0;
		}
	}

	public void close() throws IOException {
		this.writeBlock();
		this.out.close();
	}
}