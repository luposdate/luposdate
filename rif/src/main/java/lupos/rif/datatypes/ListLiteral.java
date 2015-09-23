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
package lupos.rif.datatypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import lupos.datastructures.items.literal.Literal;
import lupos.rdf.Prefix;

/**
 * <p>ListLiteral class.</p>
 *
 * @author groppe
 * @version $Id: $Id
 */
public class ListLiteral extends Literal {

	/**
	 * The entries of this list
	 */
	private final List<Literal> entries;

	/**
	 * Gets the entries of this list...
	 *
	 * @return the entries of this list
	 */
	public List<Literal> getEntries(){
		return this.entries;
	}

	/**
	 * Constructor, which expects the entries of this list as parameter
	 *
	 * @param entries the entries of this list...
	 */
	public ListLiteral(final List<Literal> entries){
		this.entries = entries;
	}

	/** {@inheritDoc} */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public String[] getUsedStringRepresentations() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public Literal createThisLiteralNew() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public String toString(){
		final StringBuilder sa = new StringBuilder();
		sa.append("[");
		boolean flag = false;
		for(final Literal l: this.entries){
			if(flag){
				sa.append(", ");
			}
			flag = true;
			sa.append(l.toString());
		}
		sa.append("]");
		return sa.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final Prefix prefix){
		final StringBuilder sa = new StringBuilder();
		sa.append("[");
		boolean flag = false;
		for(final Literal l: this.entries){
			if(flag){
				sa.append(", ");
			}
			flag = true;
			sa.append(l.toString(prefix));
		}
		sa.append("]");
		return sa.toString();
	}

	/** {@inheritDoc} */
	@Override
	public ListLiteral clone(){
		return new ListLiteral(new ArrayList<Literal>(this.entries));
	}
}
