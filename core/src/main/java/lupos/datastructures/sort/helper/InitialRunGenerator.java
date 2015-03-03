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
package lupos.datastructures.sort.helper;

import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.sort.run.Run;
import lupos.datastructures.sort.run.Runs;

/**
 * Thread for generating the initial runs...
 *
 * @author groppe
 * @version $Id: $Id
 */
public class InitialRunGenerator extends Thread {
	
	private final BoundedBuffer<String> buffer;
	private Run run;
	private final BoundedBuffer<Run> initialRunsLevel0;
	private final int NUMBER_ELEMENTS_IN_INITIAL_RUNS;
	private final Runs runs;
	
	/**
	 * <p>Constructor for InitialRunGenerator.</p>
	 *
	 * @param buffer a {@link lupos.datastructures.parallel.BoundedBuffer} object.
	 * @param initialRunsLevel0 a {@link lupos.datastructures.parallel.BoundedBuffer} object.
	 * @param NUMBER_ELEMENTS_IN_INITIAL_RUNS a int.
	 * @param runs a {@link lupos.datastructures.sort.run.Runs} object.
	 */
	public InitialRunGenerator(final BoundedBuffer<String> buffer, final BoundedBuffer<Run> initialRunsLevel0, final int NUMBER_ELEMENTS_IN_INITIAL_RUNS, final Runs runs){
		this.buffer = buffer;
		this.runs = runs;
		this.run = this.runs.createRun();
		this.initialRunsLevel0 = initialRunsLevel0;
		this.NUMBER_ELEMENTS_IN_INITIAL_RUNS = NUMBER_ELEMENTS_IN_INITIAL_RUNS;
	}
	
	/** {@inheritDoc} */
	@Override
	public void run(){
		try {
			int numberInRun = 0;
			while(true) {
				String item = this.buffer.get();
				if(item==null){
					break;
				}
				if(this.run.add(item)){
					numberInRun++;
				}
				if(numberInRun >= this.NUMBER_ELEMENTS_IN_INITIAL_RUNS){
					// this run exceeds the limit for number of elements and becomes therefore a new initial run
					this.finishInitialRun();
					numberInRun =0;
				}
			}
			// the (not full) run becomes an initial run because all RDF terms have been consumed
			this.finishInitialRun();
		} catch (InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>finishInitialRun.</p>
	 *
	 * @throws java.lang.InterruptedException if any.
	 */
	public void finishInitialRun() throws InterruptedException{
		if(!this.run.isEmpty()){
			Run sortedRun = this.run.sort();
			this.initialRunsLevel0.put(sortedRun);
			this.run = this.runs.createRun();
		}
	}
}
