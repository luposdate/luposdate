package lupos.misc;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

public class IOCostsInputStream extends InputStream {

	public static boolean LogIOCosts = false;
	private static long numberOfReadBytes = 0;
	private static ReentrantLock lock = new ReentrantLock();

	public static InputStream createIOCostsInputStream(final InputStream inferior){
		if(IOCostsInputStream.LogIOCosts) {
			return new IOCostsInputStream(inferior);
		} else {
			return inferior;
		}
	}

	private final InputStream inferior;

	public IOCostsInputStream(final InputStream inferior){
		this.inferior = inferior;
	}

	@Override
	public int read() throws IOException {
		final int result = this.inferior.read();
		if(result>=0){ // do not count in case of eof!
			try {
				IOCostsInputStream.lock.lock();
				IOCostsInputStream.numberOfReadBytes++;
			} finally {
				IOCostsInputStream.lock.unlock();
			}
		}
		return result;
	}

	@Override
	public void close() throws IOException{
		this.inferior.close();
	}

	public static long getNumberOfReadBytes(){
		return IOCostsInputStream.numberOfReadBytes;
	}

	public static void resetNumberOfReadBytes(){
		IOCostsInputStream.numberOfReadBytes = 0;
	}
}
