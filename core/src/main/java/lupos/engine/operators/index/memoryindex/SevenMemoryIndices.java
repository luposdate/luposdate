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
package lupos.engine.operators.index.memoryindex;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.sorteddata.MapOfCollections;
import lupos.datastructures.sorteddata.MapOfCollectionsImplementation;
import lupos.engine.operators.index.Indices;

public class SevenMemoryIndices extends Indices {

	/**
	 * Map storing a collection of {@link Triple}s grouped by their subject
	 * literal
	 */
	// the map's pattern is '1'
	public MapOfCollections<String, Triple, Collection<Triple>> subjectMap;

	/**
	 * Map storing a collection of {@link Triple}s grouped by their predicate
	 * literal
	 */
	// the map's pattern is '2'
	public MapOfCollections<String, Triple, Collection<Triple>> predicateMap;

	/**
	 * Map storing a collection of {@link Triple}s grouped by their object
	 * literal
	 */
	// the map's pattern is '4'
	public MapOfCollections<String, Triple, Collection<Triple>> objectMap;

	/**
	 * Map storing a collection of {@link Triple}s grouped by their subject and
	 * predicate literals. The literals are concatenated and used as keys.
	 */
	// the map's pattern is '3'
	public MapOfCollections<String, Triple, Collection<Triple>> subjectPredicateMap;

	/**
	 * Map storing a collection of {@link Triple}s grouped by their subject and
	 * object literals. The literals are concatenated and used as keys.
	 */
	// the map's pattern is '5'
	public MapOfCollections<String, Triple, Collection<Triple>> subjectObjectMap;

	/**
	 * Map storing a collection of {@link Triple}s grouped by their predicate
	 * and object literals. The literals are concatenated and used as keys.
	 */
	// the map's pattern is '6'
	public MapOfCollections<String, Triple, Collection<Triple>> predicateObjectMap;

	/**
	 * Map storing a collection of {@link Triple}s grouped by their subject,
	 * predicate and object literals. The literals are concatenated and used as
	 * keys.
	 */
	// the map's pattern is '7'
	public MapOfCollections<String, Triple, Collection<Triple>> subjectPredicateObjectMap;

	/**
	 * Constructor initializing the Maps according to the set Data structure
	 * (default is HashMap)
	 */
	public SevenMemoryIndices() {
		this.init(usedDatastructure);
	}

	public SevenMemoryIndices(final URILiteral rdf) {
		this.rdfName = rdf;
		this.init(usedDatastructure);
	}

	/**
	 * Constructor initializing the Maps in either in the main memory or on the
	 * local hard disk
	 *
	 * @param memorybased
	 *            indicates whether the maps should be initialized in the main
	 *            memory (<code>true</code>) or not (<code>false</code>)
	 */
	public SevenMemoryIndices(final DATA_STRUCT ds) {
		this.init(ds);
	}

	@Override
	public void init(final DATA_STRUCT ds) {
		SevenMemoryIndices.usedDatastructure = ds;
		switch (usedDatastructure) {
		case DEFAULT:
		case HASHMAP:
			this.initMapsInMainMemory();
			break;
		}
	}

	/**
	 * Initializes the maps to store the triple elements.<br>
	 * These maps will be stored and queried in the main memory.
	 *
	 * @see DiskBasedHashMap
	 */
	private void initMapsInMainMemory() {
		this.subjectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(), LinkedList.class);
		this.predicateMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				LinkedList.class);
		this.objectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				LinkedList.class);
		this.subjectPredicateMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				LinkedList.class);
		this.subjectObjectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				LinkedList.class);
		this.predicateObjectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				LinkedList.class);
		this.subjectPredicateObjectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				LinkedList.class);
	}

	/**
	 * Inserts an Element into the map which keys are the subject of the triples
	 * which are stored in a collection
	 *
	 * @param e
	 *            the {@link Triple}
	 * @return indicates if the operation was performed successfully
	 */

	@Override
	public void add(final Triple e) {
		try {

			if(this.contains(e)) {
				// already inside => do not need to insert!
				return;
			}

			final StringBuffer subject = new StringBuffer(e.getSubject()
					.toString());
			final StringBuffer predicate = new StringBuffer(e.getPredicate()
					.toString());
			final StringBuffer object = new StringBuffer(e.getObject()
					.toString());

			String key = subject.toString();
			this.subjectMap.putToCollection(key, e);

			key = predicate.toString();
			this.predicateMap.putToCollection(key, e);

			key = object.toString();
			this.objectMap.putToCollection(key, e);

			// subject object
			key = subject.append(object).toString();
			this.subjectObjectMap.putToCollection(key, e);

			// subject predicate
			final StringBuffer subjectPredicate = new StringBuffer(e
					.getSubject().toString()).append(predicate);
			key = subjectPredicate.toString();
			this.subjectPredicateMap.putToCollection(key, e);

			// predicate object
			key = predicate.append(object).toString();
			this.predicateObjectMap.putToCollection(key, e);

			// subject predicate object
			key = subjectPredicate.append(object).toString();
			this.subjectPredicateObjectMap.putToCollection(key, e);

		} catch (final Exception ex) {
			ex.printStackTrace();
			System.err.println("Error while inserting a tripleElement into the \"subject\" -map which"+ ex);
		}
	}

	public Collection<Triple> getFromMap(final MAP_PATTERN mapPattern,
			final String keyString) {

		switch (mapPattern) {
		case SMAP:
			return this.subjectMap.get(keyString);
		case PMAP:
			return this.predicateMap.get(keyString);
		case OMAP:
			return this.objectMap.get(keyString);
		case SPMAP:
			return this.subjectPredicateMap.get(keyString);
		case SOMAP:
			return this.subjectObjectMap.get(keyString);
		case POMAP:
			return this.predicateObjectMap.get(keyString);
		case SPOMAP:
			return this.subjectPredicateObjectMap.get(keyString);
		default:
			return this.subjectPredicateObjectMap.valuesInCollections();

		}
	}

	/**
	 * Debugging method which "prints" the size of each map
	 *
	 * @see LuposLogger
	 */
	public void printMapSizes() {
		System.err.println("subjectMap: " + this.subjectMap.size());
		System.err.println("predicateMap: " + this.predicateMap.size());
		System.err.println("objectMap: " + this.objectMap.size());
		System.err.println("subjectPredicateMap: " + this.subjectPredicateMap.size());
		System.err.println("subjectObjectMap: " + this.subjectObjectMap.size());
		System.err.println("predicateObjectMap: " + this.predicateObjectMap.size());
		System.err.println("subjectPredicateObjectMap: "
				+ this.subjectPredicateObjectMap.size());
	}

	@Override
	public boolean contains(final Triple t) {
		return this.subjectPredicateObjectMap.containsKey(t.getSubject().toString()
				+ t.getPredicate().toString() + t.getObject().toString());
	}

	private final void removeTriple(final String key, final Triple t, final MapOfCollections<String, Triple, Collection<Triple>> map){
		if(map.removeFromCollection(key, t)){
			if(map.get(key).size()==0){ // there must be a collection, otherwise removeFromCollection(...) would not have returned true!
				map.remove(key);
			}
		}
	}

	@Override
	public void remove(final Triple t) {
		try {

			if(!this.contains(t)) {
				// triple to be removed not inside => do not need to remove!
				return;
			}

			final StringBuffer subject = new StringBuffer(t.getSubject()
					.toString());
			final StringBuffer predicate = new StringBuffer(t.getPredicate()
					.toString());
			final StringBuffer object = new StringBuffer(t.getObject()
					.toString());

			String key = subject.toString();
			this.removeTriple(key, t, this.subjectMap);

			key = predicate.toString();
			this.removeTriple(key, t, this.predicateMap);

			key = object.toString();
			this.removeTriple(key, t, this.objectMap);

			// subject object
			key = subject.append(object).toString();
			this.removeTriple(key, t, this.subjectObjectMap);

			// subject predicate
			final StringBuffer subjectPredicate = new StringBuffer(t
					.getSubject().toString()).append(predicate);
			key = subjectPredicate.toString();
			this.removeTriple(key, t, this.subjectPredicateMap);

			// predicate object
			key = predicate.append(object).toString();
			this.removeTriple(key, t, this.predicateObjectMap);

			// subject predicate object
			key = subjectPredicate.append(object).toString();
			this.removeTriple(key, t, this.subjectPredicateObjectMap);

		} catch (final Exception ex) {
			ex.printStackTrace();
			System.err.println("Error while deleting a tripleElement from the maps:"+ ex);
		}
	}

	@Override
	public void constructCompletely() {
	}

	@Override
	public void writeOutAllModifiedPages() throws IOException {
	}
}
