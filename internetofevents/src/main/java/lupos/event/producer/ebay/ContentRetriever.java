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
package lupos.event.producer.ebay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


import lupos.datastructures.parallel.BoundedBuffer;
import lupos.event.producer.ebay.parser.JSONParser;
import lupos.event.producer.ebay.parser.JSObject;


/**
 * Content retriever for auction data from eBay
 */
public class ContentRetriever extends Thread {

	/**
	 * Current configuration set
	 */
	private final Configuration config;
	
	/**
	 * Indicates whether the retriever thread is running or not
	 */
	private volatile boolean loop;
	
	/**
	 * Bounded buffer that stores the retrieved auctions
	 */
	private final BoundedBuffer<Auction> buffer;

	/**
	 * Constructor
	 * 
	 * @param	config		Current configuration set
	 * @param	buffer		Bounded buffer that stores the retrieved auctions
	 */
	public ContentRetriever(Configuration config, BoundedBuffer<Auction> buffer) {
		this.config = config;
		this.buffer = buffer;
		this.loop = true;
	}

	/**
	 * Retrieves the content from the host name given in the constructor
	 * 
	 * @param	pageNumber		Current page number 
	 * 
	 * @return	String source code of the requested page
	 * 
	 * @throws	IOException	if no connection to eBay could be established
	 */
	public String getContent(final int pageNumber) throws IOException {
		final StringBuilder getStr = new StringBuilder(this.config.toString());
		final StringBuilder retVal = new StringBuilder();
		final Socket s = new Socket(Configuration.HOST, Configuration.PORT);
		final PrintWriter out = new PrintWriter(s.getOutputStream(), true);
		final BufferedReader in =
				new BufferedReader(new InputStreamReader(s.getInputStream()));

		String currentLine = null;
		boolean headerEnd = false;
		boolean output = false;
		
		getStr.append("&paginationInput.pageNumber=");
		getStr.append(pageNumber);

		// HTTP header
		out.println("GET /" + getStr.toString() + " HTTP/1.1");
		out.println("Host: " + Configuration.HOST + ":" + Configuration.PORT);
		out.println("Connection: close");

		// End of header
		out.println("");

		// While there is incoming data read from the input stream
		while ((currentLine = in.readLine()) != null) {
			if (headerEnd) {
				if (output) {
					retVal.append(currentLine).append("\n");
				}
				output = !output;
			}
			else if ("".equals(currentLine)) {
				headerEnd = true;
			}
			
		}

		s.close();

		return retVal.toString();
	}
	
	/**
	 * Contacts the ebay server, retrieves current auctions and returns them...
	 * @return the retrieved auctions after contacting the ebay server 
	 */
	public List<Auction> retrieveAuctions() {
		List<Auction> auctions = new ArrayList<Auction>();
		int counter = 0;
		
		try {
			for (int page = 1, max = (int) Math.ceil((double) this.config.resultCount / Configuration.ENTRIES_PER_PAGE); this.loop && page <= max; page++) {
				final String content = this.getContent(page);
				final JSObject model = JSONParser.parse(content);
				
				try {
					JSObject searchResult =
							model.get(this.config.opName + "Response").get("searchResult");
					final int count = (page == max)
							? ((this.config.resultCount - 1) % Configuration.ENTRIES_PER_PAGE) + 1
							: Integer.parseInt(searchResult.get("@count").toString());
		
					for (int i = 0; this.loop && i < count; i++) {
						counter++;
						JSObject item = searchResult.get("item").get(i);		
						auctions.add(new Auction(item));					
					}
				}
				catch (NullPointerException e) {
					// ignore...
				}
			}
		}
		catch (IOException e) {
			return null; 
		}
		
		return auctions;
	}
	
	@Override
	public void run() {
		int counter = 0;
		
		while (this.loop) {
			try {
				for (int page = 1, max = (int) Math.ceil((double) this.config.resultCount / Configuration.ENTRIES_PER_PAGE); this.loop && page <= max; page++) {
					final String content = this.getContent(page);
					final JSObject model = JSONParser.parse(content);
					
					try {
						JSObject searchResult =
								model.get(this.config.opName + "Response").get("searchResult");
						final int count = (page == max)
								? ((this.config.resultCount - 1) % Configuration.ENTRIES_PER_PAGE) + 1
								: Integer.parseInt(searchResult.get("@count").toString());
			
						for (int i = 0; this.loop && i < count; i++) {
							counter++;
							JSObject item = searchResult.get("item").get(i);		
							Auction auction = new Auction(item);
							
							try {
								this.buffer.put(auction);
							}
							catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					catch (NullPointerException e) {
						// ignore...
					}
				}
			}
			catch (IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stops the thread
	 */
	public void shutdown() {
		this.loop = false;
	}
}