/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.distributed.p2p.network.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import lupos.distributed.p2p.network.impl.TomP2P.ITomP2PLog;

/**
 * This is a special logger for the TomP2P network, which stores all access in a
 * special log file.
 * 
 * @author Bjoern
 * 
 */
public class TomP2PLog implements ITomP2PLog {

	private static FileLogger logger;
	public static boolean LOG = true;
	public static String FILENAME = "tomP2P.txt";

	static {
		logger = new TomP2PLog.FileLogger(FILENAME);
	}

	public void log(String type, String logString, int level) {
		if (LOG)
			logger.logMsg(composeLogMsg(type, logString, level));
	}

	private String composeLogMsg(String type, String msg, int level) {
		return String.format("%d:\t%s\t-\t%s", level, msg, type);
	}

	/**
	 * File logger which writes to a log file
	 * 
	 * @author Bjoern
	 * 
	 */
	static class FileLogger {
		private PrintStream fileout;

		/**
		 * Constructor of the file logger, which stores all data in the given
		 * file
		 * 
		 * @param filename
		 *            the file to be used as logging-file
		 */
		public FileLogger(String filename)

		{

			try {
				fileout = new PrintStream(new FileOutputStream(filename), true);
			} catch (FileNotFoundException e) {
				System.err.println(e.toString());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see AbstractLogger#logMsg(java.lang.String, int)
		 */
		protected void logMsg(String s) {
			fileout.println(s);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see Logger#close()
		 */
		public void close() {
			fileout.close();
		}
	}
}
