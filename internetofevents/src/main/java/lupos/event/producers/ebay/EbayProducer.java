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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.event.ProducerBase;
import lupos.event.communication.SerializingMessageService;
import lupos.event.communication.TcpConnectInfo;
import lupos.event.communication.TcpMessageTransport;
import lupos.event.util.Literals;

public class EbayProducer extends ProducerBase {
	
	public static final String NAMESPACE = "http://localhost/events/Ebay/";
	private static final int INTERVAL = 20000;
	
	private final static Literal TYPE = Literals.createURI(NAMESPACE, "EbayAuctionEvent");
	
	static class Predicates {
		public static final Literal TITLE = Literals.createURI(NAMESPACE, "title");
		public static final Literal TIMELEFT = Literals.createURI(NAMESPACE, "timeLeft");
		public static final Literal CURRENTPRICE = Literals.createURI(NAMESPACE, "currentPrice");
		public static final Literal BUYITNOWPRICE = Literals.createURI(NAMESPACE, "buyItNowPrice");
		public static final Literal SHIPPINGCOSTS = Literals.createURI(NAMESPACE, "shippingCosts");
	}
	
	private int subjectCounter = 0;
	private ContentRetriever retriever;
	
	
	public EbayProducer(SerializingMessageService msgService) {
		super(msgService, INTERVAL);
		
		Configuration.SECURITY_APPNAME = JOptionPane.showInputDialog("Join at http://developer.ebay.com/join to get your security app name.\nEnter your security app name here:", Configuration.SECURITY_APPNAME);
		
		String keyword;
		try {
			keyword = URLEncoder.encode(JOptionPane.showInputDialog("Enter a keyword to be searched for:", "keyword"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println(e);
			e.printStackTrace();
			keyword="";
		}
		
		this.retriever = new ContentRetriever(new Configuration(
				keyword, 
				0, 1, Configuration.ENTRIES_PER_PAGE), null);
	}

	@Override
	public List<List<Triple>> produce() {

		List<Auction> auctions = this.retriever.retrieveAuctions();
		if(auctions == null) {
			System.err.println("Error while retrieving auctions!");
			return null;
		}
		
		List<List<Triple>> t = new ArrayList<List<Triple>>();
		
		for(Auction a : auctions) {
			System.out.println("title: " + a.title);
			t.add(auctionToTriple(a));
		}
		
		return (t.size()==0)? null : t;
	}

	protected List<Triple> auctionToTriple(Auction auction) {
		List<Triple> t = new ArrayList<Triple>();
		
		Literal subj = LiteralFactory.createAnonymousLiteral("_:s" + (this.subjectCounter++));
		
		if (auction.title != null) {
			Literal obj = LiteralFactory.createLiteral("\""+auction.title+"\"");
			t.add(new Triple(subj, Predicates.TITLE, obj));
		}
		
		if (auction.timeLeft != null) {
			Literal obj = LiteralFactory.createLiteral("\""+auction.timeLeft+"\"");
			t.add(new Triple(subj, Predicates.TITLE, obj));
		}
		
		if (auction.currentPrice != null) {
			Literal obj = LiteralFactory.createLiteral("\""+auction.currentPrice+"\"");
			t.add(new Triple(subj, Predicates.CURRENTPRICE, obj));		
		}

		if (auction.buyItNowPrice != null) {
			Literal obj = LiteralFactory.createLiteral("\""+auction.buyItNowPrice+"\"");
			t.add(new Triple(subj, Predicates.BUYITNOWPRICE, obj));	
		}

		if (auction.shippingServiceCost != null) {
			Literal obj = LiteralFactory.createLiteral("\""+auction.shippingServiceCost+"\"");
			t.add(new Triple(subj, Predicates.SHIPPINGCOSTS, obj));	
		}
		
		t.add(new Triple(subj, Literals.RDF.TYPE, EbayProducer.TYPE));
		
		return t;
	}
	
	
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = new SerializingMessageService(TcpMessageTransport.class);
		msgService.connect(new TcpConnectInfo("localhost", 4444));
		
		// start producer
		new EbayProducer(msgService).start();
	}
}
