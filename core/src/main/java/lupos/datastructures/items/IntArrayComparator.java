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
package lupos.datastructures.items;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;

public class IntArrayComparator implements Comparator<int[]>, Externalizable {

	private static final int MAX = 3;

	protected int[] criteria;

	public IntArrayComparator(final CollationOrder collationOrder){
		switch(collationOrder){
			default:
			case SPO:
				this.criteria = new int[]{0, 1, 2};
				break;
			case SOP:
				this.criteria = new int[]{0, 2, 1};
				break;
			case PSO:
				this.criteria = new int[]{1, 0, 2};
				break;
			case POS:
				this.criteria = new int[]{1, 2, 0};
				break;
			case OSP:
				this.criteria = new int[]{2, 0, 1};
				break;
			case OPS:
				this.criteria = new int[]{2, 1, 0};
				break;
		}
	}

	public IntArrayComparator(final int... criteria){
		this.criteria = criteria;
	}

	public IntArrayComparator(final byte readByte) {
		this.criteria = new int[3];
		this.criteria[0] = readByte % IntArrayComparator.MAX;
		this.criteria[1] = (readByte / IntArrayComparator.MAX) % IntArrayComparator.MAX;
		this.criteria[2] = (readByte / (IntArrayComparator.MAX * IntArrayComparator.MAX)) % IntArrayComparator.MAX;
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeInt(this.criteria.length);
		for(final int i: this.criteria){
			out.writeInt(i);
		}
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		final int length = in.readInt();
		this.criteria = new int[length];
		for(int i=0; i<length; i++){
			this.criteria[i] = in.readInt();
		}
	}

	@Override
	public int compare(final int[] o1, final int[] o2) {
		for(final int i: this.criteria){ // continue with primary, secondary, tertiary, ... criteria
			if(i>=0){
				if(o1[i]<0 || o2[i]<0){
					// this case happens for prefix search keys
					return 0;
				}
				final int compare = o1[i] - o2[i];
				if(compare != 0){
					return compare;
				}
			}
		}
		return 0;
	}

	public int getCriteria(final int number){
		return this.criteria[number];
	}

	public byte getBytePattern(){
		return (byte) ((byte) this.criteria[0] + IntArrayComparator.MAX
				* (this.criteria[1] + IntArrayComparator.MAX
						* this.criteria[2]));
	}
}
