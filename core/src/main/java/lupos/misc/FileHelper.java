package lupos.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class FileHelper {
	static public boolean deleteDirectory(final File path) {
		try {
			if (path.exists()) {
				final File[] files = path.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
			return (path.delete());
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
			return false;
		}
	}

	static public boolean deleteFile(final File path) {
		try {
			if (path.exists()) {
				return path.delete();
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return false;
	}

	static public boolean deleteFile(final String path) {
		return deleteFile(new File(path));
	}

	static public boolean deleteFilesStartingWithPattern(final String dir, final String pattern){
		File dirFile = new File(dir);
		String filesToDelete[] = dirFile.list(new FilenameFilter(){
			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith(pattern);
			}
		});
		boolean flag=true;
		for(String fileName: filesToDelete)
			flag=deleteFile(dir+fileName) && flag;
		return flag;
	}

	/**
	 * Opens a file.
	 * 
	 * @param filename
	 *            filename of file to open
	 * 
	 * @return file as string
	 */
	public static String readFile(final String filename) {
		return readFile(filename, new GetReader() {

			public Reader getReader(final String filename)
			throws FileNotFoundException {
				return new FileReader(new File(filename));
			}

		});
	}

	public static String readFile(final String filename, final boolean fromJar) {
		return FileHelper.readFile(filename, new FileHelper.GetReader() {

			public Reader getReader(final String filename)
			throws FileNotFoundException {
				if (fromJar) {
					InputStream stream = null;

					stream = this.getClass().getResourceAsStream(filename);
					return new java.io.InputStreamReader(stream);
				} else
					return new FileReader(new File(filename));
			}
		});
	}

	public static String readFile(final String filename,
			final GetReader getReader) {
		System.out.println("read file:" + filename);
		try {
			// read one time to determine the size, then a
			// StringBuffer can be initialized already with the final size!
			Reader r = getReader.getReader(filename);
			BufferedReader in = new BufferedReader(r);
			final int lengthSeparator = System.getProperty("line.separator")
			.length();
			String tmp = in.readLine();
			int size = 0;
			while (tmp != null) {
				size += tmp.length() + lengthSeparator;
				tmp = in.readLine();
			}
			in.close();

			r = getReader.getReader(filename);
			final StringBuilder b = new StringBuilder(size);
			in = new BufferedReader(r);
			tmp = in.readLine();

			while (tmp != null) {
				b.append(tmp + System.getProperty("line.separator"));
				tmp = in.readLine();
			}
			in.close();
			return b.toString();
		} catch (final Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}

		return null;
	}

	public static String fastReadFile(final String filename) {
		String s = "";
		FileChannel fc = null;
		try {
			fc = new FileInputStream(filename).getChannel();

			final MappedByteBuffer byteBuffer = fc.map(
					FileChannel.MapMode.READ_ONLY, 0, fc.size());

			final int size = byteBuffer.capacity();
			if (size > 0) {
				// Retrieve all bytes in the buffer
				byteBuffer.clear();
				final byte[] bytes = new byte[size];
				byteBuffer.get(bytes, 0, bytes.length);
				s = new String(bytes);
			}

			fc.close();
		} catch (final FileNotFoundException fnfx) {
			System.err.println("File not found: " + fnfx);
		} catch (final IOException iox) {
			System.err.println("I/O problems: " + iox);
		} finally {
			if (fc != null) {
				try {
					fc.close();
				} catch (final IOException ignore) {
					// ignore
				}
			}
		}
		return s;
	}

	public interface GetReader {
		Reader getReader(String filename) throws FileNotFoundException;
	}

	public static List<String> readInputStreamToCollection(
			final InputStream inputStream) {
		final List<String> content = new LinkedList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;

			while ((line = reader.readLine()) != null) {
				// let lines with comments away...
				if (!line.startsWith("#"))
					content.add(line);
			}
		} catch (final IOException ioe) {
			ioe.printStackTrace();
			return null;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return content;
	}

	public static List<String> readFileToCollection(final String filename) {
		final List<String> content = new LinkedList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line;

			while ((line = reader.readLine()) != null) {
				// let lines with comments away...
				if (!line.startsWith("#"))
					content.add(line);
			}
		} catch (final FileNotFoundException fnfe) {
			System.out.println("File " + filename + " not found, exiting.");
			return null;
		} catch (final IOException ioe) {
			ioe.printStackTrace();
			return null;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return content;
	}

	public static void printFileContent(final String filename,
			final int lineNumber, final boolean removeLine) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				if (removeLine) {
					if (i != lineNumber)
						System.out.println(line);
				} else {
					if (i >= lineNumber - 5 && i <= lineNumber + 5)
						System.out.println(i + ":" + line);
					else if (i > lineNumber + 5)
						break;
				}
				i++;
			}
		} catch (final FileNotFoundException fnfe) {
			System.out.println("File " + filename + " not found, exiting.");
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void writeFile(final String fileName, final String content) {
		try {
			final BufferedWriter writer = new BufferedWriter(new FileWriter(
					fileName));
			writer.write(content);
			writer.close();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public static void main(final String args[]) {
		if (args.length < 2)
			System.out
			.println("Usage: java lupos.misc.FileHelper [-r] Filename linenumber");
		else {
			if (args[0].compareTo("-r") == 0) {
				printFileContent(args[1], new Integer(args[2]), true);
			} else
				printFileContent(args[0], new Integer(args[1]), false);
		}
	}
}
