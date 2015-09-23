/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
