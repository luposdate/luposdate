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
package lupos.event.producer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lupos.datastructures.items.Triple;
import lupos.event.communication.SerializingMessageService;

/**
 * Alternative base class for producers, automatically eliminating all duplicate
 * triples in a new data set (comparing to the previous one). Subclasses must
 * implement produceNoDuplicates() instead of produce()!
 *
 */
public abstract class ProducerBaseNoDuplicates extends ProducerBase {

	/**
	 * Compares Triples, by predicate as primary and object as secondary
	 * comparison index. Subjects are ignored.
	 */
	private class TripleComparator implements Comparator<Triple> {
		@Override
		public int compare(final Triple o1, final Triple o2) {
			int res = o1.getPredicate().compareTo(o2.getPredicate());
			if (res == 0) {
				res = o1.getObject().compareTo(o2.getObject());
			}
			return res;
		}
	}

	/**
	 * Hashes for the last data set produced.
	 */
	private List<Integer> prevHashes = new ArrayList<Integer>();

	public ProducerBaseNoDuplicates(final SerializingMessageService msgService,
			final int interval) {
		super(msgService, interval);
	}

	/**
	 * NOT to be overridden by subclasses anymore!
	 *
	 * see #produceNoDuplicates()
	 */
	@Override
	public List<List<Triple>> produce() {
		final List<List<Triple>> curDataSet = this.produceWithDuplicates();
		if(curDataSet!=null){
			final List<List<Triple>> newDataSet = this.eliminateDuplicates(curDataSet);
			return newDataSet;
		} else {
			return null;
		}
	}

	private List<List<Triple>> eliminateDuplicates(
			final List<List<Triple>> curDataSet) {
		final List<List<Triple>> cleanDataSet = new ArrayList<List<Triple>>();

		// compute hashes for all current events
		final List<Integer> curHashes = this.computeEventHashes(curDataSet);

		// for each event, check if it was contained in the previous data set
		// (comparison by hash values)
		// if it was not, add it to the resulting "cleanDataSet"
		for (int i = 0; i < curHashes.size(); i++) {
			if (!this.prevHashes.contains(curHashes.get(i))) {
				cleanDataSet.add(curDataSet.get(i));
			}
		}

		// remember last list of hashes
		// TODO: move this somewhere more adequate (?)
		this.prevHashes = curHashes;

		return cleanDataSet;
	}

	/**
	 * Compute hashes for a data set, i.e., a List of events (where an event is
	 * a List of Triples).
	 *
	 * @param curDataSet
	 *            Data set to compute hashes for.
	 * @return List of hashes of all events contained in curDataSet, in the same
	 *         order as the corresponding events occur in curDataSet.
	 */
	private List<Integer> computeEventHashes(final List<List<Triple>> curDataSet) {
		final List<Integer> hashes = new ArrayList<Integer>();
		for (final List<Triple> event : curDataSet) {
			hashes.add(this.computeEventHash(event));
		}
		return hashes;
	}

	/**
	 * Compute an event's hash by sorting it first (primary sort index:
	 * predicate; secondary sort index: object), then concatenating all
	 * predicate/object pairs in order into a huge string and returning its hash
	 * code.
	 *
	 * @param event
	 *            Event to be hashed.
	 * @return The event's hash value.
	 */
	private int computeEventHash(final List<Triple> event) {
		Collections.sort(event, new TripleComparator());
		String hashStr = new String();
		for (final Triple t : event) {
			hashStr += t.getPredicate().toString() + " "
					+ t.getObject().toString() + " ";
		}
		return hashStr.hashCode();
	}

	/**
	 * To be overridden by subclasses for event production.
	 *
	 * @return List of produced events, where an event is a List of Triples itself.
	 */
	public abstract List<List<Triple>> produceWithDuplicates();
}
