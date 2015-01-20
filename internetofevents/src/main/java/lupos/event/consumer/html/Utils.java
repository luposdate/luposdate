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
package lupos.event.consumer.html;

import java.awt.GridBagConstraints;

import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Class for handling File operations.
 */
public class Utils {
	
	/**
	 * Writes a files with a certain path with content.
	 * 
	 * @param filename the path of the file
	 * @param content the content of the file
	 */
	public static void writeFile(String filename, String content) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(content.getBytes());			
			fos.close();
		} catch (Exception e) {
			System.err.println(e);

			e.printStackTrace();
		}
	}

	/**
	 * Reads a certain file of a path.
	 * 
	 * @param filename the path the file to read
	 * @return content as the read file
	 */
	public static String readFile(String filename) {
		StringBuilder content = new StringBuilder();

		try {
			String NL = System.getProperty("line.separator");
			Scanner scanner = new Scanner(new FileInputStream(filename));

			try {
				while (scanner.hasNextLine()) {
					content.append(scanner.nextLine() + NL);
				}
			} finally {
				scanner.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return content.toString();
	}

	/**
	 * Creates a HTML file.
	 */
	public static void createHTMLFile(String outPutFolder, String filename, boolean mainFrame){
		File file;
		try{
			if(mainFrame){
				file = new File(outPutFolder + filename);
			}else{
				file = new File(outPutFolder + "HTML" + "/" + filename);
			}
			
			File dir = new File(outPutFolder + "HTML");
			if (dir.mkdir()) {
	           file.createNewFile();
	        } else {
	            //Nothing
	        }
			} catch (IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
	}
	
	/**
	 * Handles the data for the options of a GridBagLayout.
	 * 
	 * @param gridx x position
	 * @param gridy y position
	 * @param fill the option to fill
	 * @param anchor the layout direction
	 * @return c as set GridBagConstraints
	 */
	public static GridBagConstraints createGBC(int gridx, int gridy, int fill,
			int anchor) {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = fill;
		c.anchor = anchor;
		c.gridx = gridx;
		c.gridy = gridy;
		return c;
	}

}
