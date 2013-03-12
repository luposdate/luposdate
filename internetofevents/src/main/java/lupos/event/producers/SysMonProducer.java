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
package lupos.event.producers;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import com.jezhumble.javasysmon.*;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.event.ProducerBase;
import lupos.event.communication.*;
import lupos.event.util.Literals;


/**
 * Creates events which contain the computers current CPU usage and uptime.
 *
 */
public class SysMonProducer extends ProducerBase {
	
	public static final String NAMESPACE = "http://localhost/events/SysMon/";
	private static final int INTERVAL = 1000;
	
	private final JavaSysMon monitor = new JavaSysMon();
	private CpuTimes previousCpuTimes = this.monitor.cpuTimes();
	
	private final static Literal TYPE = Literals.createURI(SysMonProducer.NAMESPACE, "SysMonEvent");
	
	static class Predicates {
		public static final Literal UPTIME = Literals.createURI(NAMESPACE, "upTime");
		public static final Literal CPU_USAGE = Literals.createURI(NAMESPACE, "cpuUsage");
	}


	public SysMonProducer(SerializingMessageService msgService) {
		super(msgService, INTERVAL);
	}
	
	@Override
	public List<List<Triple>> produce() {
		try {			
			// get data from sysmon library
			long uptime = this.monitor.uptimeInSeconds();

			CpuTimes currentCpuTimes = this.monitor.cpuTimes();
			float usage = currentCpuTimes.getCpuUsage(this.previousCpuTimes);
			this.previousCpuTimes = currentCpuTimes;

			// build triples
			Triple typeTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Literals.RDF.TYPE, SysMonProducer.TYPE);
			
			Literal uptimeObj = Literals.createTyped(uptime+"", Literals.XSD.LONG);
			Triple uptimeTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.UPTIME, uptimeObj);
			
			Literal usageObj = Literals.createTyped(usage+"", Literals.XSD.FLOAT);
			Triple usageTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CPU_USAGE, usageObj);

			return ProducerBase.fold(Arrays.asList(typeTriple, uptimeTriple, usageTriple));
		} catch (URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = new SerializingMessageService(TcpMessageTransport.class);
		msgService.connect(new TcpConnectInfo(ProducerBase.askForHostOfBroker(), 4444));
		
		// start producer
		new SysMonProducer(msgService).start();
	}
}
