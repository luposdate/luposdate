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
package lupos.event.producer;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import com.jezhumble.javasysmon.*;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.communication.*;
import lupos.event.util.Literals;


/**
 * Creates events which contain the computers current CPU usage and uptime.
 *
 */
public class SysMonProducer extends ProducerBaseNoDuplicates {
	
	public static final String NAMESPACE = "http://localhost/events/SysMon/";
	private static final int INTERVAL = 1000;
	
	private final JavaSysMon monitor = new JavaSysMon();
	private CpuTimes previousCpuTimes = this.monitor.cpuTimes();
	
	public final static URILiteral TYPE = Literals.createURI(SysMonProducer.NAMESPACE, "SysMonEvent");
	
	public static class Predicates {
		public static final URILiteral UPTIME = Literals.createURI(NAMESPACE, "upTimeInSeconds");
		public static final URILiteral CPU_USAGE = Literals.createURI(NAMESPACE, "cpuUsage");
		public static final URILiteral CPU_FREQUENCY = Literals.createURI(NAMESPACE, "cpuFrequencyInHz");
		public static final URILiteral PID = Literals.createURI(NAMESPACE, "currentPID");
		public static final URILiteral CPUS = Literals.createURI(NAMESPACE, "numberOfCPUs");
		public static final URILiteral OS = Literals.createURI(NAMESPACE, "osName");
		public static final URILiteral TOTAL_PHYSICAL_BYTES = Literals.createURI(NAMESPACE, "totalPhysicalBytes");
		public static final URILiteral FREE_PHYSICAL_BYTES = Literals.createURI(NAMESPACE, "freePhysicalBytes");
		public static final URILiteral TOTAL_SWAP_BYTES = Literals.createURI(NAMESPACE, "totalSwapBytes");
		public static final URILiteral FREE_SWAP_BYTES = Literals.createURI(NAMESPACE, "freeSwapBytes");
	}


	public SysMonProducer(SerializingMessageService msgService) {
		super(msgService, INTERVAL);
	}
	
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		try {			
			// get data from sysmon library
			long uptime = this.monitor.uptimeInSeconds();
			long cpuFrequencyInHz = this.monitor.cpuFrequencyInHz();
			int currentPID = this.monitor.currentPid();
			int numberOfCPUs = this.monitor.numCpus();
			String osName = this.monitor.osName();
			long totalPhysicalBytes = this.monitor.physical().getTotalBytes();
			long freePhysicalBytes = this.monitor.physical().getFreeBytes();
			long totalSwapBytes = this.monitor.swap().getTotalBytes();
			long freeSwapBytes = this.monitor.swap().getFreeBytes();

			CpuTimes currentCpuTimes = this.monitor.cpuTimes();
			float usage = currentCpuTimes.getCpuUsage(this.previousCpuTimes);
			this.previousCpuTimes = currentCpuTimes;

			// build triples
			Triple typeTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Literals.RDF.TYPE, SysMonProducer.TYPE);
			
			Literal uptimeObj = Literals.createTyped(uptime+"", Literals.XSD.LONG);
			Triple uptimeTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.UPTIME, uptimeObj);
			
			Literal usageObj = Literals.createTyped(usage+"", Literals.XSD.FLOAT);
			Triple usageTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CPU_USAGE, usageObj);
			
			Triple cpuFrequencyInHzTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CPU_FREQUENCY, Literals.createTyped(cpuFrequencyInHz+"", Literals.XSD.LONG));
			
			Triple currentPIDTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.PID, Literals.createTyped(currentPID+"", Literals.XSD.INT));
			Triple numberOfCPUsTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.CPUS, Literals.createTyped(numberOfCPUs+"", Literals.XSD.INT));
			Triple osNameTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.OS, Literals.createTyped(osName+"", Literals.XSD.String));
			Triple totalPhysicalBytesTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.TOTAL_PHYSICAL_BYTES, Literals.createTyped(totalPhysicalBytes+"", Literals.XSD.LONG));
			Triple freePhysicalBytesTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.FREE_PHYSICAL_BYTES, Literals.createTyped(freePhysicalBytes+"", Literals.XSD.LONG));
			Triple totalSwapBytesTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.TOTAL_SWAP_BYTES, Literals.createTyped(totalSwapBytes+"", Literals.XSD.LONG));
			Triple freeSwapBytesTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.FREE_SWAP_BYTES, Literals.createTyped(freeSwapBytes+"", Literals.XSD.LONG));

			return ProducerBase.fold(Arrays.asList(typeTriple, uptimeTriple, usageTriple, cpuFrequencyInHzTriple, currentPIDTriple, numberOfCPUsTriple, osNameTriple, totalPhysicalBytesTriple, freePhysicalBytesTriple, totalSwapBytesTriple, freeSwapBytesTriple));
		} catch (URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		// start producer
		new SysMonProducer(msgService).start();
	}
}
