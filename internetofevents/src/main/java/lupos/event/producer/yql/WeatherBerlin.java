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
package lupos.event.producer.yql;

import lupos.datastructures.items.literal.URILiteral;
import lupos.event.communication.SerializingMessageService;
import lupos.event.producer.ProducerBase;
import lupos.event.util.Literals;

/**
 * 
 * @author Christopher Gudat, Guillaume Assaud
 * 
 * YQLSpecializer can change into every Producer, YQL-Query is necessary
 *
 */

public class WeatherBerlin extends YQLQueryProducer {
	//Change these values to create an other Producer
	public final static String NAMESPACE_WEATHER = "http://localhost/events/weatherBerlin/";
	private final static String NAME = "WeatherBerlin";
	private final static String QUERY = "select * from weather.forecast where location in (select id from weather.search where query=\"berlin, germany\")";
	public final static URILiteral TYPE = Literals.createURI(NAMESPACE_WEATHER, NAME);
	
	public WeatherBerlin(SerializingMessageService msgService){
		super(msgService, NAMESPACE_WEATHER, TYPE, QUERY);
	}
	
	public static void main(String[] args) throws Exception {
		//Create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		WeatherBerlin tsp = new WeatherBerlin(msgService);
		
		// start producer
		tsp.start();
	}
}