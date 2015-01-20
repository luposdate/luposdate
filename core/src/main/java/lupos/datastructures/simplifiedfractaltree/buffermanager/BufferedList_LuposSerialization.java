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
package lupos.datastructures.simplifiedfractaltree.buffermanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import lupos.datastructures.buffermanager.BufferManager.PageAddress;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class BufferedList_LuposSerialization<E> extends BufferedList<E> {
	/**
	 * Serial Version ID.
	 */
	private static final long serialVersionUID = 4988155911642704072L;

	public BufferedList_LuposSerialization(final Object arg0) {
		super(arg0);
	}

	public BufferedList_LuposSerialization(final int pageSize, final File file, final Object arg0) {
		super(pageSize, file, arg0);
	}

	@Override
	protected byte[] serialize(final E element) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final BufferedOutputStream bos = new BufferedOutputStream(os);
		byte[] array = null;

		try {
			final LuposObjectOutputStream loos = new LuposObjectOutputStream(bos);
			loos.writeLuposObject(element);
			loos.flush();
			loos.close();
			os.close();
			bos.close();
			array = os.toByteArray();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return array;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	protected E deserialize(final byte[] array, final Object arg0) {
		final ByteArrayInputStream bais = new ByteArrayInputStream(array);
		final BufferedInputStream bis = new BufferedInputStream(bais);
		LuposObjectInputStream<E> loin = null;
		E element = null;

		try {
			loin = new LuposObjectInputStream<E>(bis, (Class<E>) arg0.getClass());
			element = loin.readLuposObject();
			bais.close();
			loin.close();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		} catch (final URISyntaxException e1) {
			e1.printStackTrace();
		}

		return element;
	}

	@Override
	public E get(final int index) {
		this.rangeCheck(index);

		return this.getElement(this.getPointers(index), this.instance);
	}

	@SuppressWarnings("unchecked")
	protected E getElement(final Pointer pointer, final Object arg0) {
		ByteArrayOutputStream baos = null;
		ByteArrayInputStream bais = null;
		LuposObjectInputStream<E> loin = null;
		E element = null;

		final int rightPage = this.calcRightPage(pointer.leftPage, pointer.leftBound, pointer.size);
		final int rightBound = this.calcRightBound(pointer.leftBound, pointer.size);

		for (int i = pointer.leftPage; i <= rightPage; i++) {
			try {
				final byte[] page = this.bufferManager.getPage(this.pageSize, new PageAddress(i, this.path1));
				if (pointer.leftPage == rightPage) {
					baos = new ByteArrayOutputStream();
					baos.write(page, pointer.leftBound, pointer.size);
					bais = new ByteArrayInputStream(baos.toByteArray());

					loin = new LuposObjectInputStream<E>(bais, (Class<E>) arg0.getClass());
					element = loin.readLuposObject();
				} else if (i == pointer.leftPage) {
					baos = new ByteArrayOutputStream();
					baos.write(page, pointer.leftBound, this.pageSize - pointer.leftBound);
				} else if (i != pointer.leftPage && i != rightPage) {
					baos.write(page, 0, this.pageSize);
				} else if (i == rightPage) {
					baos.write(page, 0, rightBound);
					bais = new ByteArrayInputStream(baos.toByteArray());

					loin = new LuposObjectInputStream<E>(bais, (Class<E>) arg0.getClass());
					element = loin.readLuposObject();
				}
			} catch (IOException | ClassNotFoundException | URISyntaxException e) {
				e.printStackTrace();
			}
		}

		try {
			baos.close();
			bais.close();
			loin.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return element;
	}
}