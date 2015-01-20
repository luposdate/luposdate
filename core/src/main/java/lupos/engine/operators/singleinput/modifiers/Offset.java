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
package lupos.engine.operators.singleinput.modifiers;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class Offset extends SingleInputOperator {

	// the offset to use
	private int offset;
	private int pos = 0;

	/**
	 * constructs an offset-operator with given offset
	 *
	 * @param offset
	 *            to use
	 */
	public Offset(final int offset) {
		this.offset = offset;
	}

	/**
	 * changes offset to given value
	 *
	 * @param offset
	 *            the new offset
	 */
	public void setOffset(final int offset) {
		this.offset = offset;
		if (offset < 0) {
			System.out
					.println("Error: OFFSET has to be either positive or zero!");
		}
	}

	public int getOffset() {
		return this.offset;
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		this.offset = ((Offset) op).offset;
	}

	/**
	 * overrides process method from OperatorInterface
	 *
	 * @return the BindingsList cut to offset:bindings.length;
	 */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (this.pos >= this.offset){
			return bindings;
		}

		final Iterator<Bindings> itb = bindings.oneTimeIterator();

		while (itb.hasNext() && this.pos < this.offset) {
			itb.next();
			this.pos++;
		}

		return QueryResult.createInstance(itb);
	}

	@Override
	public String toString() {
		return super.toString() + " " + this.offset;
	}
}
