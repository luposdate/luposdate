/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.event.producer.rsssemantics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Retrieves stoplist words from stoplist.txt (language sensitive, provisional
 * stoplist words for english and german texts included so far) and removes
 * these words from feed description string, if contained.
 */
public class StoplistReader {
	@SuppressWarnings("unchecked")
	public ArrayList<String>[] readStoplist(String path) {
		ArrayList<String> germanstoplist = new ArrayList<String>();
		ArrayList<String> englishstoplist = new ArrayList<String>();
		try {
			BufferedReader in;
			try {
				in = new BufferedReader(new InputStreamReader(StoplistReader.class.getResource(path).openStream()));
			} catch(Exception e){
				in = new BufferedReader(new FileReader(new File(StoplistReader.class.getResource(path).getFile())));
			}
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				if (zeile.equals("[de]")) {
					while ((!((zeile = in.readLine())).equals("[/de]"))) {
						germanstoplist.add(zeile);
					}
				} else if (zeile.equals("[en]")) {
					while ((!((zeile = in.readLine())).equals("[/en]"))) {
						englishstoplist.add(zeile);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		@SuppressWarnings("rawtypes")
		ArrayList[] stoplist = { germanstoplist, englishstoplist };
		return stoplist;
	}
}
