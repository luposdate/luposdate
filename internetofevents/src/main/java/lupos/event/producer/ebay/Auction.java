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
package lupos.event.producer.ebay;

import lupos.event.producer.ebay.parser.JSObject;


/**
 * Contains all relevant information about an eBay auction
 */
public class Auction {
	
	/**
	 * Article id
	 */
	public final String id;
	
	/**
	 * Remaining time until the auction ends
	 */
	public final Duration timeLeft;
	
	/**
	 * Name of the auction
	 */
	public final String title;
	
	/**
	 * Highest bid
	 */
	public final Integer currentPrice;
	
	/**
	 * Price for immediate purchase
	 */
	public final Integer buyItNowPrice;
	
	/**
	 * Shipping fee
	 */
	public final Integer shippingServiceCost;
	
	
	/**
	 * Constructor
	 * 
	 * @param	id
	 * 				Article id
	 * @param	remainingTime
	 * 				Remaining time until the auction ends
	 * @param	name
	 * 				Name of the auction
	 * @param	bid
	 * 				Highest bid
	 * @param	price
	 * 				Price for immediate purchase
	 * @param	shippingFee
	 * 				Shipping fee
	 */
	public Auction(String id, String remainingTime, String name, int bid,
			int price, int shippingFee) {
		this.id = id;
		this.timeLeft = new Duration(remainingTime);
		this.title = name;
		this.currentPrice = bid;
		this.buyItNowPrice = price;
		this.shippingServiceCost = shippingFee;
	}

	/**
	 * Constructor
	 * 
	 * @param	item
	 * 				JSObject that contains the information about this auction
	 */
	public Auction(JSObject item) {
		String shippingInfo = item.access("shippingInfo.shippingServiceCost.__value__");
		String bid = item.access("sellingStatus.currentPrice.__value__");
		String remainingTime = item.access("sellingStatus.timeLeft.0");
		String buyItNow = item.access("listingInfo.buyItNowPrice.__value__");
		
		String id_local = null;
		Duration timeLeft_local = null;
		String title_local = null;
		Integer currentPrice_local = null;
		Integer buyItNowPrice_local = null;
		Integer shippingServiceCost_local = null;
		
		try {
			id_local = item.access("itemId.0");
			timeLeft_local = (remainingTime != null) ? new Duration(remainingTime.replaceAll("\\s+", "")) : null;
			title_local = item.access("title.0");
			currentPrice_local = (bid != null) ? (int) (Double.parseDouble(bid.replaceAll("\\s+", "")) * 100.0) : null;
			buyItNowPrice_local = (buyItNow != null) ? (int) (Double.parseDouble(buyItNow.replaceAll("\\s+", "")) * 100.0) : null;
			shippingServiceCost_local = (shippingInfo != null) ? (int) (Double.parseDouble(shippingInfo.replaceAll("\\s+", "")) * 100.0) : null;
		}
		catch (Exception e) { 
			System.err.println("P: "+item.toString());
			e.printStackTrace();
			System.exit(1);
		}
		
		this.id = id_local;
		this.timeLeft = timeLeft_local;
		this.title = title_local;
		this.currentPrice = currentPrice_local;
		this.buyItNowPrice = buyItNowPrice_local;
		this.shippingServiceCost = shippingServiceCost_local;
	}
	
	@Override
	public String toString() {
		return new StringBuilder("Auction(id = ").append(this.id)
				.append(", remainingTime = ").append(this.timeLeft)
				.append(", name = ").append(this.title)
				.append(", bid = ").append(this.currentPrice)
				.append(", price = ").append(this.buyItNowPrice)
				.append(", shippingFee = ").append(this.shippingServiceCost)
				.append(')').toString();
	}
}
