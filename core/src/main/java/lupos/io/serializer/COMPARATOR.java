
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.io.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import lupos.datastructures.dbmergesortedds.StandardComparator;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.io.Registration.DeSerializerConsideringSubClasses;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;

@SuppressWarnings("rawtypes")
public class COMPARATOR<T> extends DeSerializerConsideringSubClasses<Comparator<T>>{

	private static HashMap<Class<?>, ComparatorDeSerializer<?>> registeredDeSerializer = new HashMap<Class<?>, ComparatorDeSerializer<?>>();

	private static ArrayList<ComparatorDeSerializer<?>> listOfDeSerializer = new ArrayList<ComparatorDeSerializer<?>>();

	/**
	 * <p>registerDeSerializer.</p>
	 *
	 * @param deSerializers a {@link lupos.io.serializer.COMPARATOR.ComparatorDeSerializer} object.
	 */
	public static void registerDeSerializer(final ComparatorDeSerializer<?>... deSerializers){
		for(final ComparatorDeSerializer<?> deSerializer: deSerializers){
			COMPARATOR.listOfDeSerializer.add(deSerializer);
			for(final Class<?> c: deSerializer.getRegisteredClasses()){
				COMPARATOR.registeredDeSerializer.put(c, deSerializer);
			}
		}
	}

	private static<T> void error(final Comparator<T> t){
		final String errorText = (t==null)? "Unknown Comparator code" : "No DeSerializer found for comparator of type " + t.getClass();
		System.err.println(errorText);
		throw new UnsupportedOperationException(errorText);
	}

	static {
		COMPARATOR.registerDeSerializer(
				new StandardComparatorDeSerializer(),
				new TripleComparatorDeSerializer());
	}

	/** {@inheritDoc} */
	@Override
	public int length(final Comparator<T> t) {
		@SuppressWarnings("unchecked")
		final
		ComparatorDeSerializer<T> deSerializer = (ComparatorDeSerializer<T>) COMPARATOR.registeredDeSerializer.get(t.getClass());
		if(deSerializer == null){
			COMPARATOR.error(t);
			return 0;
		} else {
			return LengthHelper.lengthLuposByte() + deSerializer.length(t);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void serialize(final Comparator<T> t, final OutputStream out) throws IOException {
		@SuppressWarnings("unchecked")
		final
		ComparatorDeSerializer<T> deSerializer = (ComparatorDeSerializer<T>) COMPARATOR.registeredDeSerializer.get(t.getClass());
		if(deSerializer == null){
			COMPARATOR.error(t);
		} else {
			OutHelper.writeLuposByte((byte) COMPARATOR.listOfDeSerializer.indexOf(deSerializer), out);
			deSerializer.serialize(t, out);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Comparator<T> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		final
		ComparatorDeSerializer<T> deSerializer = (ComparatorDeSerializer<T>) COMPARATOR.listOfDeSerializer.get(InputHelper.readLuposByte(in));
		if(deSerializer == null){
			COMPARATOR.error(null);
			return null;
		} else {
			return deSerializer.deserialize(in);
		}
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Comparator<T>>[] getRegisteredClasses() {
		final Object[] array = COMPARATOR.registeredDeSerializer.keySet().toArray();
		final Class<? extends Comparator<T>>[] result = new Class[array.length];
		System.arraycopy(array, 0, result, 0, array.length);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean instanceofTest(final Object o) {
		return (o instanceof Comparator);
	}

	public static interface ComparatorDeSerializer<T>{
		public int length(Comparator<T> t);

		public void serialize(Comparator<T> t, OutputStream out) throws IOException;

		public Comparator<T> deserialize(InputStream in) throws IOException, URISyntaxException, ClassNotFoundException;

		public Class<? extends Comparator<T>>[] getRegisteredClasses();
	}

	public static class StandardComparatorDeSerializer<T> implements ComparatorDeSerializer<T>{

		@Override
		public int length(final Comparator<T> t) {
			return 0;
		}

		@Override
		public void serialize(final Comparator<T> t, final OutputStream out) throws IOException {
		}

		@Override
		public Comparator<T> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return new StandardComparator<T>();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Comparator<T>>[] getRegisteredClasses() {
			return new Class[]{ Comparator.class, StandardComparator.class };
		}
	}

	public static class TripleComparatorDeSerializer implements ComparatorDeSerializer<Triple>{

		@Override
		public int length(final Comparator<Triple> t) {
			return LengthHelper.lengthLuposByte();
		}

		@Override
		public void serialize(final Comparator<Triple> t, final OutputStream out) throws IOException {
			final TripleComparator tc = (TripleComparator) t;
			OutHelper.writeLuposByte(tc.getBytePattern(), out);
		}

		@Override
		public Comparator<Triple> deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return new TripleComparator(InputHelper.readLuposByte(in));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Comparator<Triple>>[] getRegisteredClasses() {
			return new Class[]{ TripleComparator.class };
		}
	}

}
