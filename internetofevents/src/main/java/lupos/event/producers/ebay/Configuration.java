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
package lupos.event.producers.ebay;

/**
 * Configuration set
 */
public class Configuration {
	
	/**
	 * Hostname to retrieve the auction data from
	 */
	public static final String HOST = "svcs.ebay.com";
	
	/**
	 * Port to connect to at the remote host
	 */
	public static final int PORT = 80;
	
	/**
	 * Path on the server
	 */
	public static final String PATH = "services/search/FindingService/v1";
	
	/**
	 * API calls supported by this application
	 */
	public static final String[] OPERATION_NAME = { "findItemsByKeywords" };
	
	/**
	 * eBay service version
	 */
	public static final String SERVICE_VERSION = "1.0.0";
	
	/**
	 * Security appname to identify this application at the eBay API
	 * 
	 * There are a few prerequisites for running this producer:
	 * 
     * 1) Join the eBay Developers Program and get your Access Keys:
     * http://developer.ebay.com/join
     * 
     * 2) Joining is free and you get 5,000 API calls a day just for joining! 
     * You have to generate your application keys from your My Account page:
     * https://developer.ebay.com/DevZone/account/Default.aspx
	 */
	public static String SECURITY_APPNAME = "Enter your security app name here!";
	
	/**
	 * eBay locations supported by this application
	 */
	public static final String[] GLOBAL_ID = { "EBAY-US", "EBAY-DE" };
	
	/**
	 * Number of entries to retrieve per page
	 */
	public static final int ENTRIES_PER_PAGE = 2;
	
	/**
	 * Response data format (only JSON is supported by this application)
	 */
	public static final String RESPONSE_DATA_FORMAT = "JSON";
	
	/**
	 * Number of pages to retrieve during each loop
	 */
	public final int resultCount;
	
	/**
	 * Keywords to search
	 */
	public final String keywordString;
	
	/**
	 * Currently selected OPERATION_NAME
	 */
	public final String opName;
	
	/**
	 * Currently selected eBay location
	 */
	public final String locale;
	
	/**
	 * Constructor
	 * 
	 * @param	keywordString	Keywords to search for
	 * @param	opName			Index of the OPERATION_NAME that has to be used
	 * @param	locale			Index of the GLOBAL_ID that has to be used
	 * @param 	resultCount 	Number of pages to retrieve during each loop
	 */
	public Configuration(String keywordString, int opName, int locale, int resultCount) {
		this.keywordString = keywordString;
		this.opName = Configuration.OPERATION_NAME[opName];
		this.locale = Configuration.GLOBAL_ID[locale];
		this.resultCount = resultCount;
	}
	
	@Override
	public String toString() {
		return (new StringBuilder(PATH)
			.append("?OPERATION-NAME=").append(this.opName)
			.append("&SERVICE-VERSION=").append(SERVICE_VERSION)
			.append("&SECURITY-APPNAME=").append(SECURITY_APPNAME)
			.append("&GLOBAL-ID=").append(this.locale)
			.append("&paginationInput.entriesPerPage=").append(ENTRIES_PER_PAGE)
			.append("&RESPONSE-DATA-FORMAT=").append(RESPONSE_DATA_FORMAT)
			.append("&keywords=").append(this.keywordString)).toString();
	}
	
}