package lupos.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class LuposObjectInputStreamWithoutReadingHeader<E> extends
		LuposObjectInputStream<E> {

	public LuposObjectInputStreamWithoutReadingHeader(final InputStream arg0,
			final Class<? extends E> classOfElements) throws IOException,
			EOFException {
		is = arg0;
		this.classOfElements = classOfElements;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}
}
