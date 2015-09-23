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

import lupos.io.Registration.DeSerializerConsideringSubClasses;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;
import lupos.optimizations.logical.statistics.VarBucket;
public class VARBUCKETARRAY extends DeSerializerConsideringSubClasses<VarBucket[]> {
	/** {@inheritDoc} */
	@Override
	public boolean instanceofTest(final Object o) {
		return o instanceof VarBucket[];
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public Class<VarBucket[]>[] getRegisteredClasses() {
		return new Class[] { VarBucket[].class };
	}

	/** {@inheritDoc} */
	@Override
	public int length(final VarBucket[] t) {
		return LengthHelper.lengthLuposVarBucketArray(t);
	}

	/** {@inheritDoc} */
	@Override
	public void serialize(final VarBucket[] t, final OutputStream out) throws IOException {
		OutHelper.writeLuposVarBucketArray(t, out);
	}

	/** {@inheritDoc} */
	@Override
	public VarBucket[] deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
		return InputHelper.readLuposVarBucketArray(in);
	}
}
