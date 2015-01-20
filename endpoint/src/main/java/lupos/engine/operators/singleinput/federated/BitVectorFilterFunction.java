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
package lupos.engine.operators.singleinput.federated;

import java.math.BigInteger;

import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.filter.expressionevaluation.EvaluationVisitorImplementation;
import lupos.engine.operators.singleinput.filter.expressionevaluation.ExternalFunction;
import lupos.engine.operators.singleinput.filter.expressionevaluation.Helper;
import lupos.misc.BitVector;

public class BitVectorFilterFunction implements ExternalFunction {

	@Override
	public Object evaluate(Object[] args) throws TypeErrorException {
		if(args.length==3){
			String value = args[0].toString();
			BigInteger bitVector = Helper.getInteger(args[1]);
			int bitVectorSize = Helper.getInteger(args[2]).intValue();
			BitVector bv = BitVector.getBitVector(bitVector, bitVectorSize);
			if(bv.get(Math.abs(value.hashCode() % bitVectorSize))){
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		} else {
			throw new TypeErrorException("BitVectorFilter funtion expects exactly 3 arguments: the value, the bitvector (as integer) and the size of the bitvector");
		}
	}


	public static void register(){
		EvaluationVisitorImplementation.registerExternalFunction(LiteralFactory.createURILiteralWithoutLazyLiteralWithoutException("<http://www.ifis.uni-luebeck.de/functions/BitVectorFilter>"), new BitVectorFilterFunction());
	}
}
