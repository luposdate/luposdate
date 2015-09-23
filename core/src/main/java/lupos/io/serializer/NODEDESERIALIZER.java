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
package lupos.io.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.IntArrayDBBPTreeStatisticsNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.IntArrayNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.LazyLiteralDBBPTreeStatisticsNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.LazyLiteralNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.NodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StandardNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StringIntegerNodeDeSerializer;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.io.Registration;
import lupos.io.Registration.DeSerializerConsideringSubClasses;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;

@SuppressWarnings("rawtypes")
public class NODEDESERIALIZER<K, V> extends DeSerializerConsideringSubClasses<NodeDeSerializer<K, V>>{

	private static HashMap<Class<?>, DSNodeDeSerializer<?, ?>> registeredDeSerializer = new HashMap<Class<?>, DSNodeDeSerializer<?, ?>>();

	private static ArrayList<DSNodeDeSerializer<?, ?>> listOfDeSerializer = new ArrayList<DSNodeDeSerializer<?, ?>>();

	/**
	 * <p>registerDeSerializer.</p>
	 *
	 * @param deSerializers a {@link lupos.io.serializer.NODEDESERIALIZER.DSNodeDeSerializer} object.
	 */
	public static void registerDeSerializer(final DSNodeDeSerializer<?, ?>... deSerializers){
		for(final DSNodeDeSerializer<?, ?> deSerializer: deSerializers){
			NODEDESERIALIZER.listOfDeSerializer.add(deSerializer);
			for(final Class<?> c: deSerializer.getRegisteredClasses()){
				NODEDESERIALIZER.registeredDeSerializer.put(c, deSerializer);
			}
		}
	}

	private static<K, V> void error(final NodeDeSerializer<K, V> t){
		final String errorText = (t==null)? "Unknown NodeDeSerializer code" : "No DeSerializer found for NodeDeSerializer of type " + t.getClass();
		System.err.println(errorText);
		throw new UnsupportedOperationException(errorText);
	}

	static {
		NODEDESERIALIZER.registerDeSerializer(
				new DSStandardNodeDeSerializer(),
				new DSLazyLiteralNodeDeSerializer(),
				new DSLazyLiteralDBBPTreeStatisticsNodeDeSerializer(),
				new DSStringIntegerNodeDeSerializer(),
				new DSIntArrayNodeDeSerializer(),
				new DSIntArrayDBBPTreeStatisticsNodeDeSerializer());
	}

	/** {@inheritDoc} */
	@Override
	public int length(final NodeDeSerializer<K, V> t) {
		@SuppressWarnings("unchecked")
		final DSNodeDeSerializer<K, V> deSerializer = (DSNodeDeSerializer<K, V>) NODEDESERIALIZER.registeredDeSerializer.get(t.getClass());
		if(deSerializer == null){
			NODEDESERIALIZER.error(t);
			return 0;
		} else {
			return LengthHelper.lengthLuposByte() + deSerializer.length(t);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void serialize(final NodeDeSerializer<K, V> t, final OutputStream out) throws IOException {
		@SuppressWarnings("unchecked")
		final DSNodeDeSerializer<K, V> deSerializer = (DSNodeDeSerializer<K, V>) NODEDESERIALIZER.registeredDeSerializer.get(t.getClass());
		if(deSerializer == null){
			NODEDESERIALIZER.error(t);
		} else {
			OutHelper.writeLuposByte((byte) NODEDESERIALIZER.listOfDeSerializer.indexOf(deSerializer), out);
			deSerializer.serialize(t, out);
		}
	}

	/** {@inheritDoc} */
	@Override
	public NodeDeSerializer<K, V> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		final DSNodeDeSerializer<K, V> deSerializer = (DSNodeDeSerializer<K, V>) NODEDESERIALIZER.listOfDeSerializer.get(InputHelper.readLuposByte(in));
		if(deSerializer == null){
			NODEDESERIALIZER.error(null);
			return null;
		} else {
			return deSerializer.deserialize(in);
		}
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends NodeDeSerializer<K, V>>[] getRegisteredClasses() {
		final Object[] array = NODEDESERIALIZER.registeredDeSerializer.keySet().toArray();
		final Class<? extends NodeDeSerializer<K, V>>[] result = new Class[array.length];
		System.arraycopy(array, 0, result, 0, array.length);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean instanceofTest(final Object o) {
		return (o instanceof Comparator);
	}

	public static interface DSNodeDeSerializer<K, V>{
		public int length(NodeDeSerializer<K, V> t);

		public void serialize(NodeDeSerializer<K, V> t, OutputStream out) throws IOException;

		public NodeDeSerializer<K, V> deserialize(InputStream in) throws IOException, URISyntaxException, ClassNotFoundException;

		public Class<? extends NodeDeSerializer<K, V>>[] getRegisteredClasses();
	}

	public static class DSStandardNodeDeSerializer<K, V> implements DSNodeDeSerializer<K, V>{

		@Override
		public int length(final NodeDeSerializer<K, V> t) {
			return 2*Registration.lengthSerializeId();
		}

		@Override
		public void serialize(final NodeDeSerializer<K, V> t, final OutputStream out) throws IOException {
			final StandardNodeDeSerializer<K, V> snds = (StandardNodeDeSerializer<K, V>) t;
			Registration.serializeClass(snds.getKeyClass(), out);
			Registration.serializeClass(snds.getValueClass(), out);
		}

		@SuppressWarnings("unchecked")
		@Override
		public NodeDeSerializer<K, V> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return new StandardNodeDeSerializer<K, V>((Class<? extends K>) Registration.deserializeId(in)[0], (Class<? extends V>) Registration.deserializeId(in)[0]);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends NodeDeSerializer<K, V>>[] getRegisteredClasses() {
			return new Class[]{ NodeDeSerializer.class, StandardNodeDeSerializer.class };
		}
	}

	public static class DSLazyLiteralNodeDeSerializer implements DSNodeDeSerializer<TripleKey, Triple>{

		@Override
		public int length(final NodeDeSerializer<TripleKey, Triple> t) {
			return LengthHelper.lengthLuposByte();
		}

		@Override
		public void serialize(final NodeDeSerializer<TripleKey, Triple> t, final OutputStream out) throws IOException {
			final LazyLiteralNodeDeSerializer tc = (LazyLiteralNodeDeSerializer) t;
			OutHelper.writeLuposByte((byte) tc.getCollationOrder().ordinal(), out);
		}

		@Override
		public  NodeDeSerializer<TripleKey, Triple> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return new LazyLiteralNodeDeSerializer(RDF3XIndexScan.CollationOrder.values()[InputHelper.readLuposByte(in)]);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends NodeDeSerializer<TripleKey, Triple>>[] getRegisteredClasses() {
			return new Class[]{ LazyLiteralNodeDeSerializer.class };
		}
	}

	public static class DSLazyLiteralDBBPTreeStatisticsNodeDeSerializer implements DSNodeDeSerializer<TripleKey, Triple>{

		@Override
		public int length(final NodeDeSerializer<TripleKey, Triple> t) {
			return LengthHelper.lengthLuposByte();
		}

		@Override
		public void serialize(final NodeDeSerializer<TripleKey, Triple> t, final OutputStream out) throws IOException {
			final LazyLiteralDBBPTreeStatisticsNodeDeSerializer tc = (LazyLiteralDBBPTreeStatisticsNodeDeSerializer) t;
			OutHelper.writeLuposByte((byte) tc.getCollationOrder().ordinal(), out);
		}

		@Override
		public  NodeDeSerializer<TripleKey, Triple> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return new LazyLiteralDBBPTreeStatisticsNodeDeSerializer(RDF3XIndexScan.CollationOrder.values()[InputHelper.readLuposByte(in)]);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends NodeDeSerializer<TripleKey, Triple>>[] getRegisteredClasses() {
			return new Class[]{ LazyLiteralDBBPTreeStatisticsNodeDeSerializer.class };
		}
	}

	public static class DSIntArrayNodeDeSerializer implements DSNodeDeSerializer<int[], int[]>{

		@Override
		public int length(final NodeDeSerializer<int[], int[]> t) {
			return LengthHelper.lengthLuposByte();
		}

		@Override
		public void serialize(final NodeDeSerializer<int[], int[]> t, final OutputStream out) throws IOException {
			final IntArrayNodeDeSerializer tc = (IntArrayNodeDeSerializer) t;
			OutHelper.writeLuposByte((byte) tc.getCollationOrder().ordinal(), out);
		}

		@Override
		public  NodeDeSerializer<int[], int[]> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return new IntArrayNodeDeSerializer(RDF3XIndexScan.CollationOrder.values()[InputHelper.readLuposByte(in)]);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends NodeDeSerializer<int[], int[]>>[] getRegisteredClasses() {
			return new Class[]{ IntArrayNodeDeSerializer.class };
		}
	}

	public static class DSIntArrayDBBPTreeStatisticsNodeDeSerializer implements DSNodeDeSerializer<int[], int[]>{

		@Override
		public int length(final NodeDeSerializer<int[], int[]> t) {
			return LengthHelper.lengthLuposByte();
		}

		@Override
		public void serialize(final NodeDeSerializer<int[], int[]> t, final OutputStream out) throws IOException {
			final IntArrayDBBPTreeStatisticsNodeDeSerializer tc = (IntArrayDBBPTreeStatisticsNodeDeSerializer) t;
			OutHelper.writeLuposByte((byte) tc.getCollationOrder().ordinal(), out);
		}

		@Override
		public  NodeDeSerializer<int[], int[]> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return new IntArrayDBBPTreeStatisticsNodeDeSerializer(RDF3XIndexScan.CollationOrder.values()[InputHelper.readLuposByte(in)]);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends NodeDeSerializer<int[], int[]>>[] getRegisteredClasses() {
			return new Class[]{ IntArrayDBBPTreeStatisticsNodeDeSerializer.class };
		}
	}

	public static class DSStringIntegerNodeDeSerializer implements DSNodeDeSerializer<String, Integer>{

		@Override
		public int length(final NodeDeSerializer<String, Integer> t) {
			return 0;
		}

		@Override
		public void serialize(final NodeDeSerializer<String, Integer> t, final OutputStream out) throws IOException {
		}

		@Override
		public NodeDeSerializer<String, Integer> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return new StringIntegerNodeDeSerializer();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends NodeDeSerializer<String, Integer>>[] getRegisteredClasses() {
			return new Class[]{ StringIntegerNodeDeSerializer.class };
		}
	}

}
