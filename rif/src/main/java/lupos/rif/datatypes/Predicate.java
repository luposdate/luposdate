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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.rdf.Prefix;
import lupos.rif.RIFException;

public class Predicate implements Serializable {
	private static final long serialVersionUID = 253370338303245743L;
	protected URILiteral name;
	protected ArrayList<Literal> params = new ArrayList<Literal>();

	public Literal getName() {
		return this.name;
	}

	public void setName(final Literal name) {
		if (name instanceof URILiteral) {
			this.name = (URILiteral) name;
		} else {
			throw new RIFException("Predicatename can only be URILiteral!");
		}
	}

	public List<Literal> getParameters() {
		return this.params;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj != null && obj instanceof Predicate) {
			final Predicate pred = (Predicate) obj;
			if (!pred.name.equals(this.name)) {
				return false;
			}
			if (pred.params.size() != this.params.size()) {
				return false;
			}
			for (int i = 0; i < this.params.size(); i++) {
				final Literal param = pred.params.get(i);
				if (param==null || !param.equals(this.params.get(i))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer();
		str.append(this.name.toString()).append("(");
		for (int idx = 0; idx < this.params.size(); idx++) {
			str.append(this.params.get(idx));
			if (idx < this.params.size() - 1){
				str.append(", ");
			}
		}
		str.append(")");
		return str.toString();
	}

	public String toString(final Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer();
		str.append(this.name.toString(prefixInstance)).append("(");
		for (int idx = 0; idx < this.params.size(); idx++) {
			final Object current = this.params.get(idx);
			if(current instanceof Literal){
				str.append(((Literal)current).toString(prefixInstance));
			} else {
				str.append(current.toString());
			}
			if (idx < this.params.size() - 1){
				str.append(", ");
			}
		}
		str.append(")");
		return str.toString();
	}

}
