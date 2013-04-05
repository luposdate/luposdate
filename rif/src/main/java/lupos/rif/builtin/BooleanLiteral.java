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
package lupos.rif.builtin;

import java.net.URISyntaxException;

import lupos.datastructures.items.literal.TypedLiteral;
import lupos.rif.RIFException;

public class BooleanLiteral extends TypedLiteral {

	public final static BooleanLiteral FALSE;
	public final static BooleanLiteral TRUE;
	final public boolean value;

	static {
		FALSE = BooleanLiteral.create(false);
		TRUE = BooleanLiteral.create(true);
	}

	private BooleanLiteral(boolean value) throws URISyntaxException {
		super("\"" + Boolean.toString(value) + "\"",
				"<http://www.w3.org/2001/XMLSchema#boolean>");
		this.value = value;
	}

	public static BooleanLiteral create(boolean value) {
		try {
			return new BooleanLiteral(value);
		} catch (URISyntaxException e) {
			throw new RIFException(e.getMessage());
		}
	}

	public static BooleanLiteral not(BooleanLiteral bl) {
		return create(!bl.value);
	}
}
