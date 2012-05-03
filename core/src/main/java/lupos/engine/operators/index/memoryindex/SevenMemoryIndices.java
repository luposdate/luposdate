package lupos.engine.operators.index.memoryindex;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

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
		init(usedDatastructure);
	}

	public SevenMemoryIndices(final URILiteral rdf) {
		rdfName = rdf;
		init(usedDatastructure);
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
		init(ds);
	}

	@Override
	public void init(final DATA_STRUCT ds) {
		SevenMemoryIndices.usedDatastructure = ds;
		switch (usedDatastructure) {
		case DEFAULT:
		case HASHMAP:
			initMapsInMainMemory();
			break;
		}
	}

	/**
	 * Initializes the maps to store the triple elements.<br>
	 * These maps will be stored and queried in the main memory.
	 * 
	 * @see DiskBasedHashMap
	 */
	@SuppressWarnings("unchecked")
	private void initMapsInMainMemory() {
		subjectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				(Class<Collection<Triple>>) (new java.util.LinkedList<Triple>()
						.getClass()));
		predicateMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				(Class<Collection<Triple>>) (new java.util.LinkedList<Triple>()
						.getClass()));
		objectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				(Class<Collection<Triple>>) (new java.util.LinkedList<Triple>()
						.getClass()));
		subjectPredicateMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				(Class<Collection<Triple>>) (new java.util.LinkedList<Triple>()
						.getClass()));
		subjectObjectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				(Class<Collection<Triple>>) (new java.util.LinkedList<Triple>()
						.getClass()));
		predicateObjectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				(Class<Collection<Triple>>) (new java.util.LinkedList<Triple>()
						.getClass()));
		subjectPredicateObjectMap = new MapOfCollectionsImplementation<String, Triple, Collection<Triple>>(
				new HashMap<String, Collection<Triple>>(),
				(Class<Collection<Triple>>) (new java.util.LinkedList<Triple>()
						.getClass()));
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
	public boolean add(final Triple e) {
		try {

			final StringBuffer subject = new StringBuffer(e.getSubject()
					.toString());
			final StringBuffer predicate = new StringBuffer(e.getPredicate()
					.toString());
			final StringBuffer object = new StringBuffer(e.getObject()
					.toString());

			String key = subject.toString();
			subjectMap.putToCollection(key, e);

			key = predicate.toString();
			predicateMap.putToCollection(key, e);

			key = object.toString();
			objectMap.putToCollection(key, e);

			// subject object
			key = subject.append(object).toString();
			subjectObjectMap.putToCollection(key, e);

			// subject predicate
			final StringBuffer subjectPredicate = new StringBuffer(e
					.getSubject().toString()).append(predicate);
			key = subjectPredicate.toString();
			subjectPredicateMap.putToCollection(key, e);

			// predicate object
			key = predicate.append(object).toString();
			predicateObjectMap.putToCollection(key, e);

			// subject predicate object
			key = subjectPredicate.append(object).toString();
			subjectPredicateObjectMap.putToCollection(key, e);

			// System.out.println("SM: "+subjectMap.size()+" PM: "+predicateMap.
			// size
			// ()+" OM: "+objectMap.size()+" SPM: "+subjectPredicateMap.size(
			// )+" SOM: "
			// +subjectObjectMap.size()+" SPOM: "+subjectPredicateObjectMap
			// .size());

			return true;
		} catch (final Exception ex) {
			ex.printStackTrace();
			System.err.println(
							"Error while inserting a tripleElement into the \"subject\" -map which"+ ex);
			return false;
		}
	}

	public Collection<Triple> getFromMap(final MAP_PATTERN mapPattern,
			final String keyString) {

		switch (mapPattern) {
		case SMAP:
			return subjectMap.get(keyString);
		case PMAP:
			return predicateMap.get(keyString);
		case OMAP:
			return objectMap.get(keyString);
		case SPMAP:
			return subjectPredicateMap.get(keyString);
		case SOMAP:
			return subjectObjectMap.get(keyString);
		case POMAP:
			return predicateObjectMap.get(keyString);
		case SPOMAP:
			return subjectPredicateObjectMap.get(keyString);
		default:
			return subjectPredicateObjectMap.valuesInCollections();

		}
	}

	/**
	 * Debugging method which "prints" the size of each map
	 * 
	 * @see LuposLogger
	 */
	public void printMapSizes() {
		System.err.println("subjectMap: " + subjectMap.size());
		System.err.println("predicateMap: " + predicateMap.size());
		System.err.println("objectMap: " + objectMap.size());
		System.err.println("subjectPredicateMap: " + subjectPredicateMap.size());
		System.err.println("subjectObjectMap: " + subjectObjectMap.size());
		System.err.println("predicateObjectMap: " + predicateObjectMap.size());
		System.err.println("subjectPredicateObjectMap: "
				+ subjectPredicateObjectMap.size());
	}

	@Override
	public boolean contains(final Triple t) {
		return subjectPredicateObjectMap.containsKey(t.getSubject().toString()
				+ t.getPredicate().toString() + t.getObject().toString());
	}

	@Override
	public boolean remove(final Triple t) {
		try {

			final StringBuffer subject = new StringBuffer(t.getSubject()
					.toString());
			final StringBuffer predicate = new StringBuffer(t.getPredicate()
					.toString());
			final StringBuffer object = new StringBuffer(t.getObject()
					.toString());

			String key = subject.toString();
			subjectMap.removeFromCollection(key, t);

			key = predicate.toString();
			predicateMap.removeFromCollection(key, t);

			key = object.toString();
			objectMap.removeFromCollection(key, t);

			// subject object
			key = subject.append(object).toString();
			subjectObjectMap.removeFromCollection(key, t);

			// subject predicate
			final StringBuffer subjectPredicate = new StringBuffer(t
					.getSubject().toString()).append(predicate);
			key = subjectPredicate.toString();
			subjectPredicateMap.removeFromCollection(key, t);

			// predicate object
			key = predicate.append(object).toString();
			predicateObjectMap.removeFromCollection(key, t);

			// subject predicate object
			key = subjectPredicate.append(object).toString();
			subjectPredicateObjectMap.removeFromCollection(key, t);

			// System.out.println("SM: "+subjectMap.size()+" PM: "+predicateMap.
			// size
			// ()+" OM: "+objectMap.size()+" SPM: "+subjectPredicateMap.size(
			// )+" SOM: "
			// +subjectObjectMap.size()+" SPOM: "+subjectPredicateObjectMap
			// .size());

			return true;
		} catch (final Exception ex) {
			ex.printStackTrace();
			System.err.println("Error while deleting a tripleElement from the maps:"+ ex);
			return false;
		}
	}

	@Override
	public void constructCompletely() {
	}
	
	public void writeOutAllModifiedPages() throws IOException {
	}
}
