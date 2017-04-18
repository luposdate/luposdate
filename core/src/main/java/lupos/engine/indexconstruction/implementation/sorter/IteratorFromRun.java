package lupos.engine.indexconstruction.implementation.sorter;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import lupos.io.helper.InputHelper;

/**
 * Just for iterating through a run
 */
public class IteratorFromRun {

	private final InputStream in;

	int previousPrimaryCode = 0;
	int previousSecondaryCode = 0;
	int previousTertiaryCode = 0;
	int leftWithSamePrimaryCode = 0;

	public IteratorFromRun(final String filename) throws EOFException, FileNotFoundException, IOException {
		this.in = new BufferedInputStream(new FileInputStream(filename));
	}

	public int[] next() throws IOException {
		if(this.leftWithSamePrimaryCode==0) {
			final Integer code = InputHelper.readLuposIntVariableBytes(this.in);
			if(code == null){
				this.in.close();
				return null;
			}
			this.previousPrimaryCode += code;
			this.previousSecondaryCode = 0;
			this.previousTertiaryCode = 0;
			this.leftWithSamePrimaryCode = InputHelper.readLuposIntVariableBytes(this.in);
		}
		final int code = InputHelper.readLuposIntVariableBytes(this.in);
		if(code > 0) {
			this.previousTertiaryCode = 0;
		}
		this.previousSecondaryCode += code;
		this.previousTertiaryCode += InputHelper.readLuposIntVariableBytes(this.in);
		this.leftWithSamePrimaryCode--;
		return new int[] { this.previousPrimaryCode, this.previousSecondaryCode, this.previousTertiaryCode};
	}
}