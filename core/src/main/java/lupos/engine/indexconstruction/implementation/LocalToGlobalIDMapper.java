package lupos.engine.indexconstruction.implementation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.FileHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the initial runs with their local ids to the global ids of the global dictionary
 */
public class LocalToGlobalIDMapper extends Thread {

	private static final Logger log = LoggerFactory.getLogger(LocalToGlobalIDMapper.class);

	private final String filename;
	private final int[] mapping;

	public LocalToGlobalIDMapper(final String filename, final int[] mapping) {
		this.filename = filename;
		this.mapping = mapping;
	}

	@Override
	public void run() {
		try (final InputStream in = new BufferedInputStream(new FileInputStream(this.filename));
			 final OutputStream out = new BufferedOutputStream(new FileOutputStream(this.filename + "_mapped"))) {

			int previousPrimaryCode = 0;
			int previousMappedPrimaryCode = 0;
			Integer primaryCode;
			while((primaryCode = InputHelper.readLuposIntVariableBytes(in)) != null){
				primaryCode+=previousPrimaryCode;
				final int primaryMappedCode = this.mapping[primaryCode];
				OutHelper.writeLuposIntVariableBytes(primaryMappedCode - previousMappedPrimaryCode, out);

				final int repetitions = InputHelper.readLuposIntVariableBytes(in);
				OutHelper.writeLuposIntVariableBytes(repetitions, out);

				int previousSecondaryCode = 0;
				int previousMappedSecondaryCode = 0;
				int previousTertiaryCode = 0;
				int previousMappedTertiaryCode = 0;

				for(int i=0; i<repetitions; i++) {
					final int secondaryCode = InputHelper.readLuposIntVariableBytes(in) + previousSecondaryCode;
					final int secondaryMappedCode = this.mapping[secondaryCode];
					OutHelper.writeLuposIntVariableBytes(secondaryMappedCode - previousMappedSecondaryCode, out);
					if(secondaryMappedCode != previousMappedSecondaryCode) {
						previousTertiaryCode = 0;
						previousMappedTertiaryCode = 0;
					}

					final int tertiaryCode = InputHelper.readLuposIntVariableBytes(in) + previousTertiaryCode;
					final int tertiaryMappedCode = this.mapping[tertiaryCode];
					OutHelper.writeLuposIntVariableBytes(tertiaryMappedCode - previousMappedTertiaryCode, out);

					previousMappedSecondaryCode = secondaryMappedCode;
					previousSecondaryCode = secondaryCode;
					previousMappedTertiaryCode = tertiaryMappedCode;
					previousTertiaryCode = tertiaryCode;
				}

				previousPrimaryCode = primaryCode;
				previousMappedPrimaryCode = primaryMappedCode;
			}

			in.close();
			out.close();
			FileHelper.deleteFile(this.filename);
		} catch (final FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (final IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}

