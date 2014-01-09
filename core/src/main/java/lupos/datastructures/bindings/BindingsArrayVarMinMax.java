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
package lupos.datastructures.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lupos.datastructures.items.Variable;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;

public class BindingsArrayVarMinMax extends BindingsArray {
	protected int[] minArray = new int[this.literals.length];
	protected int[] maxArray = new int[this.literals.length];

	public BindingsArrayVarMinMax(final BindingsFactory bindingsFactory) {
		super(bindingsFactory);
		for (int i = 0; i < this.literals.length; i++) {
			this.minArray[i] = -1;
			this.maxArray[i] = -1;
		}
	}

	public void addMinMax(final Variable v, final int min, final int max) {
		final int pos = this.bindingsFactory.posVariables.get(v);
		this.minArray[pos] = min;
		this.maxArray[pos] = max;
	}

	public int getMin(final Variable v) {
		return this.minArray[this.bindingsFactory.posVariables.get(v)];
	}

	public int getMax(final Variable v) {
		return this.maxArray[this.bindingsFactory.posVariables.get(v)];
	}

	public int getMin(final int varCode) {
		return this.minArray[varCode];
	}

	public int getMax(final int varCode) {
		return this.maxArray[varCode];
	}

	@Override
	public BindingsArrayVarMinMax clone() {
		final BindingsArrayVarMinMax other = new BindingsArrayVarMinMax(this.bindingsFactory);
		// System.arraycopy(this.literals, 0, other.literals, 0,
		// this.literals.length);
		other.cloneLiterals(this.getLiterals());
		System.arraycopy(this.minArray, 0, other.minArray, 0, this.minArray.length);
		System.arraycopy(this.maxArray, 0, other.maxArray, 0, this.maxArray.length);

		return other;
	}

	@Override
	public void addAllPresortingNumbers(final Bindings bindings) {
		if (!(bindings instanceof BindingsArrayVarMinMax)) {
			return;
		}
		final BindingsArrayVarMinMax bavmm = (BindingsArrayVarMinMax) bindings;
		for (int i = 0; i < this.minArray.length; i++) {
			if (bavmm.minArray[i] > -1) {
				// if (minArray[i] < 0) {
				this.minArray[i] = bavmm.minArray[i];
				this.maxArray[i] = bavmm.maxArray[i];
				// } else {
				// minArray[i] = Math.max(minArray[i], bavmm.minArray[i]);
				// maxArray[i] = Math.min(maxArray[i], bavmm.maxArray[i]);
				// }
			}
		}
	}

	public void writePresortingNumbers(final OutputStream out)
			throws IOException {
		for (int i = 0; i < this.minArray.length; i++) {
			OutHelper.writeLuposInt(this.minArray[i], out);
			OutHelper.writeLuposInt(this.maxArray[i], out);
		}
	}

	public void readPresortingNumbers(final InputStream in)
			throws IOException {
		for (int i = 0; i < this.minArray.length; i++) {
			this.minArray[i] = InputHelper.readLuposInt(in);
			this.maxArray[i] = InputHelper.readLuposInt(in);
		}
	}

	public int lengthPresortingNumbers() {
		int result = 0;
		for (int i = 0; i < this.minArray.length; i++) {
			result += 	LengthHelper.lengthLuposInt(this.minArray[i]) +
						LengthHelper.lengthLuposInt(this.maxArray[i]);
		}
		return result;
	}

	@Override
	public BindingsArrayVarMinMax createInstance(){
		return new BindingsArrayVarMinMax(this.bindingsFactory);
	}
}
