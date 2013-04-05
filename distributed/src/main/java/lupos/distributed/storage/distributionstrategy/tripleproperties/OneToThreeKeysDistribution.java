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
package lupos.distributed.storage.distributionstrategy.tripleproperties;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class implements the distribution strategy, where the
 * triples are distributed according to one and two keys distribution strategies as well as
 * to the subject - predicate - object
 * (to seven different nodes).
 */
public class OneToThreeKeysDistribution implements IDistributionKeyContainer<String> {

	// just for avoiding redundant code...
	private final OneKeyDistribution oneKeyDistribution = new OneKeyDistribution();
	private final TwoKeysDistribution twoKeysDistribution = new TwoKeysDistribution();

	protected final static String TYPE_SPO = "SPO";

	@Override
	public KeyContainer<String>[] getKeysForStoring(final Triple triple) {
		@SuppressWarnings("unchecked")
		final KeyContainer<String>[] keys = new KeyContainer[7];
		System.arraycopy(this.oneKeyDistribution.getKeysForStoring(triple), 0, keys, 0, 3);
		System.arraycopy(this.twoKeysDistribution.getKeysForStoring(triple), 0, keys, 3, 3);
		keys[6] = new KeyContainer<String>(TYPE_SPO,
				triple.getSubject().originalString() +
				triple.getPredicate().originalString() +
				triple.getObject().originalString());
		return keys;
	}

	@SuppressWarnings("unchecked")
	@Override
	public KeyContainer<String>[] getKeysForQuerying(final TriplePattern triplePattern) throws TriplePatternNotSupportedError {
		int numberOfLiterals = 0;
		// count the number of literals in a triple pattern...
		for(final Item item: triplePattern){
			if(!item.isVariable()){
				numberOfLiterals++;
			}
		}
		switch(numberOfLiterals){
			case 1:
				return this.oneKeyDistribution.getKeysForQuerying(triplePattern);
			case 2:
				return this.twoKeysDistribution.getKeysForQuerying(triplePattern);
			case 3:
				return new KeyContainer[]{ new KeyContainer<String>(TYPE_SPO, ((Literal)triplePattern.getSubject()).originalString() + ((Literal)triplePattern.getPredicate()).originalString() + ((Literal)triplePattern.getObject()).originalString()) };
			default:
				throw new TriplePatternNotSupportedError(this, triplePattern);
		}
	}

	@Override
	public String toString(){
		return "One to three keys distribution strategy (triple (s, p, o) has keys { 'S' + s, 'P' + p, 'O' + o, 'SP' + s + p, 'PO' + p + o, 'SO' + s + o, 'SPO' + s + p + o })";
	}

	@Override
	public String[] getKeyTypes() {
		return OneToThreeKeysDistribution.getPossibleKeyTypes();
	}

	public static String[] getPossibleKeyTypes(){
		return new String[]{ OneKeyDistribution.TYPE_S, OneKeyDistribution.TYPE_P,OneKeyDistribution.TYPE_O,
				TwoKeysDistribution.TYPE_SP, TwoKeysDistribution.TYPE_SO, TwoKeysDistribution.TYPE_PO,
				TYPE_SPO };
	}
}
