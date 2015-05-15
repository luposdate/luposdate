package lupos.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

public class IOCostsOutputStream extends OutputStream {

	public static boolean LogIOCosts = false;
	private static long numberOfWrittenBytes = 0;
	private static ReentrantLock lock = new ReentrantLock();

	public static OutputStream createIOCostsOutputStream(final OutputStream inferior){
		if(IOCostsOutputStream.LogIOCosts) {
			return new IOCostsOutputStream(inferior);
		} else {
			return inferior;
		}
	}

	private final OutputStream inferior;

	public IOCostsOutputStream(final OutputStream inferior){
		this.inferior = inferior;
	}

	@Override
	public void write(final int b) throws IOException {
		try {
			IOCostsOutputStream.lock.lock();
			IOCostsOutputStream.numberOfWrittenBytes++;
		} finally {
			IOCostsOutputStream.lock.unlock();
		}
		this.inferior.write(b);
	}

	@Override
	public void close() throws IOException{
		this.inferior.close();
	}

	public static long getNumberOfWrittenBytes(){
		return IOCostsOutputStream.numberOfWrittenBytes;
	}

	public static void resetNumberOfWrittenBytes(){
		IOCostsOutputStream.numberOfWrittenBytes = 0;
	}
}
