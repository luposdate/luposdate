package lupos.misc;

import java.io.File;
import java.io.FilenameFilter;
/**
 * FilenameFilter implementation that selects all files matching a regex
 */
public class RegexpMatcher implements FilenameFilter {
	private String regexp;
	
	public RegexpMatcher(String regexp) {
		this.regexp = regexp;
	}

	public boolean accept(File dir, String filename) {
		return filename.matches(regexp);
	}

}
