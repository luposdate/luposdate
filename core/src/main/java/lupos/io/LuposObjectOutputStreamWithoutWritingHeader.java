package lupos.io;

import java.io.IOException;
import java.io.OutputStream;

public class LuposObjectOutputStreamWithoutWritingHeader extends
		LuposObjectOutputStream {

	public LuposObjectOutputStreamWithoutWritingHeader(final OutputStream arg0)
			throws IOException {
		os = arg0;
	}

	@Override
	public void close() throws IOException {
		try {
			os.close();
		} catch (final IOException e) {
			System.err.println("Warning: " + e);
			e.printStackTrace();
		}
	}
}
