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
package lupos.event.producers.webpage;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.ProducerBaseNoDuplicates;
import lupos.event.communication.SerializingMessageService;
import lupos.event.util.Literals;


import org.htmlcleaner.*;
import org.w3c.dom.NodeList;


/**
 * This class can be used to extract data from any html based website.
 * Sub-classes call this instructor for extracting explicit data
 * 
 * @author Team 4
 *
 */
public class GeneralProducer extends ProducerBaseNoDuplicates {

	public String NAMESPACE;
	private Literal TYPE;
	private static final int INTERVAL = 10000;
	private URL url;
	private List<Literal> literalList;
	protected List<String> xpathList;
	protected List<URILiteral> dataString;
	protected List<String> regexString;

	/**
	 * 
	 * @param msgService for TCP-transport
	 * @param namespace the namespace of the Producer
	 * @param type literaltype of the calling sub-class
	 * @param url2 link to website
	 * @param xpathlist list of xpath queries for data extraction
	 * @param literallist list of literals where each entry corresponds to each entry in xpathlist
	 * @param datastring type of literal for each node value
	 * @param regexstring optional for selecting specific data out of xpathNode (regular expressions)
	 * @param interval the interval (in milliseconds) for checking the web page for updates
	 * 
	 * The constructor just sets the parameter
	 * 
	 */
	public GeneralProducer(SerializingMessageService msgService,
			String namespace, Literal type, URL url2, List<String> xpathlist,
			List<Literal> literallist, List<URILiteral> datastring, List<String> regexstring,
			final int interval) {
		super(msgService, interval);
		this.NAMESPACE = namespace;
		this.TYPE = type;
		this.url = url2;
		this.xpathList = xpathlist;
		this.literalList = literallist;
		this.dataString = datastring;
		this.regexString = regexstring;
	}

	/**
	 * 
	 * @param msgService for TCP-transport
	 * @param namespace the namespace of the Producer
	 * @param type literaltype of the calling sub-class
	 * @param url2 link to website
	 * @param xpathlist list of xpath queries for data extraction
	 * @param literallist list of literals where each entry corresponds to each entry in xpathlist
	 * @param datastring type of literal for each node value
	 * @param regexstring optional for selecting specific data out of xpathNode (regular expressions)
	 * 
	 * The constructor just sets the parameter
	 * 
	 */
	public GeneralProducer(SerializingMessageService msgService,
			String namespace, Literal type, URL url2, List<String> xpathlist,
			List<Literal> literallist, List<URILiteral> datastring, List<String> regexstring) {
		this(msgService, namespace, type, url2, xpathlist, literallist, datastring, regexstring, INTERVAL);
	}
	
	/**
	 * This method parses the website and executes every xpath expression.
	 * The result for each node in each query is put into a specific triple-list.
	 * @return result as List of triple-lists
	 */
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		try {
			
			//Configure HTML Cleaner
			// This cleaner cleans dirty websites by editing tags
			 
			HtmlCleaner cleaner = new HtmlCleaner();
			CleanerProperties props = cleaner.getProperties();
			props.setAllowHtmlInsideAttributes(true);
			props.setAllowMultiWordAttributes(true);
			props.setRecognizeUnicodeChars(true);
			props.setOmitComments(true);

			// open a connection to the desired URL
			URLConnection conn = this.url.openConnection();
			// clean html page
			TagNode tagNode = new HtmlCleaner().clean(new InputStreamReader(
					conn.getInputStream()));
			//Convert HTML cleaner TagNode into DOM Document
			//so that xpath 2.0 queries can be done (HTMLCleaner just supports XPath 1.0)
			org.w3c.dom.Document doc = new DomSerializer(
					new CleanerProperties()).createDOM(tagNode);
		
			List<NodeList> queryList = new ArrayList<NodeList>();
			
			// evaluate XPath expressions which are stored in xpathList 
			// and save result nodes in queryList
			for (int j = 0; j < this.xpathList.size(); j++) {
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				XPathExpression expr = xpath.compile(this.xpathList.get(j));
				Object results = expr.evaluate(doc, XPathConstants.NODESET);
				NodeList nodes = (NodeList) results;
				queryList.add(nodes);
			}

			List<List<Triple>> result = new ArrayList<List<Triple>>();
			List<Triple> res = new ArrayList<Triple>();
			
			//this id is intended to create unique subject values
			Long id = 0l;
			
			//check how many entries  are in the first result of the xpathQuery and 
			//go through each result
			for (int k = 0; k < queryList.get(0).getLength(); k++) {
				Literal subject = LiteralFactory.createAnonymousLiteral("<" + id + ">");
				Triple typeTriple = new Triple(subject, Literals.RDF.TYPE, this.TYPE);	
				res.add(typeTriple);

				//check how many XPath expressions were submitted and go trough each result
				//add all entries to the res = (intermediate) result
				for (int i = 0; i < queryList.size(); i++) {
					//get data out of the XPath result node list
					String data = queryList.get(i).item(k).getTextContent()
							.trim();
					
					// evaluate regex for information selection
					boolean addValues=true;
					// if regex given
					if (this.regexString.get(i).length()>0)
					{
						final Pattern pattern = Pattern.compile(this.regexString.get(i));
						final Matcher matcher = pattern.matcher(data);
						// if regex valid, extract information
						if (matcher.find() == true) {
							System.out.println("Executing regex");
							data = matcher.group(0);
						}
						// if regex invalid ignore data
						else
						{
							System.out.println("error in regular expression");
							addValues = false;
						}	
					}
					// create triple out of selected data
					if (addValues){
						System.out.println(data);
						Literal obj = Literals.createTyped(data, this.dataString.get(i));
						Triple genTriple = new Triple(subject, this.literalList.get(i), obj);
						res.add(genTriple);
					}				
				}
				//increase counter for individual subject generation
				id++;
				
				//add intermediate results to main result
				result.add(res);
				//clear the intermediate result variable
				res = new ArrayList<Triple>();				
			}
			//return main result
			return result;

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}
}
