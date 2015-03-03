
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.operatorgraph.visualeditor.visualrif.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
public class File extends java.io.File {
	private static final long serialVersionUID = 2423924049861542568L;
	private String fileExtension = "";

	/**
	 * <p>Constructor for File.</p>
	 *
	 * @param pathname a {@link java.lang.String} object.
	 */
	public File(String pathname) {
		super(pathname);

		String[] tmp = pathname.split("\\.");

		this.fileExtension = tmp[tmp.length-1];
	}

	/**
	 * <p>copyFileTo.</p>
	 *
	 * @param targetFileName a {@link java.lang.String} object.
	 */
	public void copyFileTo(String targetFileName) {
		try {
			File f2 = new File(targetFileName);

			InputStream in = new FileInputStream(this);
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;

			while((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * <p>Getter for the field <code>fileExtension</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFileExtension() {
		return this.fileExtension;
	}

	/**
	 * <p>readFile.</p>
	 *
	 * @param filename a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String readFile(String filename) {
		StringBuilder content = new StringBuilder();

		try {
			String NL = System.getProperty("line.separator");
			Scanner scanner = new Scanner(new FileInputStream(filename));

			try {
				while(scanner.hasNextLine()) {
					content.append(scanner.nextLine() + NL);
				}
			}
			finally {
				scanner.close();
			}
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}

		return content.toString();
	}

	/**
	 * <p>writeFile.</p>
	 *
	 * @param filename a {@link java.lang.String} object.
	 * @param content a {@link java.lang.String} object.
	 */
	public static void writeFile(String filename, String content) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(content.getBytes());
			fos.close();
		}
		catch(Exception e) {
			System.err.println(e);

			e.printStackTrace();
		}
	}
}
