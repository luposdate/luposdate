/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.datastructures.bindings;

import java.io.IOException;

import lupos.datastructures.items.Variable;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class BindingsArrayVarMinMax extends BindingsArray {
	protected int[] minArray = new int[literals.length];
	protected int[] maxArray = new int[literals.length];

	public BindingsArrayVarMinMax() {
		super();
		for (int i = 0; i < literals.length; i++) {
			minArray[i] = -1;
			maxArray[i] = -1;
		}
	}

	public void addMinMax(final Variable v, final int min, final int max) {
		final int pos = posVariables.get(v);
		minArray[pos] = min;
		maxArray[pos] = max;
	}

	public int getMin(final Variable v) {
		return minArray[posVariables.get(v)];
	}

	public int getMax(final Variable v) {
		return maxArray[posVariables.get(v)];
	}

	public int getMin(final int varCode) {
		return minArray[varCode];
	}

	public int getMax(final int varCode) {
		return maxArray[varCode];
	}

	@Override
	public BindingsArrayVarMinMax clone() {
		final BindingsArrayVarMinMax other = new BindingsArrayVarMinMax();
		// System.arraycopy(this.literals, 0, other.literals, 0,
		// this.literals.length);
		other.cloneLiterals(getLiterals());
		System.arraycopy(minArray, 0, other.minArray, 0, minArray.length);
		System.arraycopy(maxArray, 0, other.maxArray, 0, maxArray.length);

		return other;
	}

	@Override
	public void addAllPresortingNumbers(final Bindings bindings) {
		if (!(bindings instanceof BindingsArrayVarMinMax))
			return;
		final BindingsArrayVarMinMax bavmm = (BindingsArrayVarMinMax) bindings;
		for (int i = 0; i < minArray.length; i++) {
			if (bavmm.minArray[i] > -1) {
				// if (minArray[i] < 0) {
				minArray[i] = bavmm.minArray[i];
				maxArray[i] = bavmm.maxArray[i];
				// } else {
				// minArray[i] = Math.max(minArray[i], bavmm.minArray[i]);
				// maxArray[i] = Math.min(maxArray[i], bavmm.maxArray[i]);
				// }
			}
		}
	}

	public void writePresortingNumbers(final LuposObjectOutputStream out)
			throws IOException {
		for (int i = 0; i < minArray.length; i++) {
			out.writeLuposInt(minArray[i]);
			out.writeLuposInt(maxArray[i]);
		}
	}

	public void readPresortingNumbers(final LuposObjectInputStream in)
			throws IOException {
		for (int i = 0; i < minArray.length; i++) {
			minArray[i] = in.readLuposInt();
			maxArray[i] = in.readLuposInt();
		}
	}
}
