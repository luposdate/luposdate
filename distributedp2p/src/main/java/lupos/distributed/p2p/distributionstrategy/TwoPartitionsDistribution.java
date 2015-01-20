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
package lupos.distributed.p2p.distributionstrategy;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.distributed.storage.distributionstrategy.tripleproperties.IDistributionKeyContainer;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This is the distribution with twice usage of partitions with the last and
 * pre-last component of the KeyCombination
 */
public class TwoPartitionsDistribution implements
		IDistributionKeyContainer<String> {

	/*
	 * The needed key combinations
	 */
	protected final static String TYPE_SPO = "SPO";
	protected final static String TYPE_SOP = "SOP";
	protected final static String TYPE_PSO = "PSO";
	protected final static String TYPE_POS = "POS";
	protected final static String TYPE_OPS = "OPS";
	protected final static String TYPE_OSP = "OSP";

	/**
	 * the size of bags for distribution, so for: 4, the keys are hashed modulo
	 * 4. (first bag) 
	 * see in: {@link #getFirstBagsize()}
	 */
	private int bagSize = 4;

	/**
	 * the size of bags for distribution (second bag)
	 * see in: {@link #getSecondBagsize()}
	 */
	private int sndBagSize = 4;

	/**
	 * Returns the bag size (for the first component)
	 * 
	 * @return the bag size
	 */
	public int getFirstBagsize() {
		return bagSize;
	}

	/**
	 * Returns the bag size (for the 2nd component)
	 * 
	 * @return the bag size
	 */
	public int getSecondBagsize() {
		return sndBagSize;
	}

	/*
	 * hashes the {index}-component of triple with the hash function
	 */
	private int getHashedComponent(int hashPosition, int component,
			TriplePattern t) {
		//get the bag-size, needed for this calculation
		int bagSize = (hashPosition == 0) ? getFirstBagsize()
				: getSecondBagsize();
		if (component < 3 && component >= 0)
			return Math.abs(((Literal) t.getPos(component)).originalString()
					.hashCode()) % bagSize;
		else
			throw new RuntimeException(String.format(
					"Component %d not found in %s", component, t));
	}

	/*
	 * hashes the {index}-component of triple with the hash function
	 */
	private int getHashedComponent(int hashPosition, int component, Triple t) {
		//get the bag-size, needed for this calculation
		int bagSize = (hashPosition == 0) ? getFirstBagsize()
				: getSecondBagsize();
		if (component < 3 && component >= 0)
			return Math.abs(t.getPos(component).originalString().hashCode())
					% bagSize;
		else
			throw new RuntimeException(String.format(
					"Component %d not found in %s", component, t));
	}

	/*
	 * returns the first char of the string, because for hashing this is used in
	 * front of the new key used.
	 */
	private String f(String s) {
		if (s == null || s.length() == 0)
			return "";
		return s.charAt(0) + "";
	}

	/*
	 * returns the second char of the string, because for hashing this is used
	 * in front of the new key used.
	 */
	private String s(String s) {
		if (s == null || s.length() == 0 || s.length() < 3)
			return "";
		return s.charAt(1) + "";
	}

	/*
	 * returns the second char of the string, because for hashing this is used
	 * in front of the new key used.
	 */
	private String t(String s) {
		if (s == null || s.length() == 0 || s.length() < 3)
			return "";
		return s.charAt(2) + "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public KeyContainer<String>[] getKeysForStoring(final Triple triple) {
		/*
		 * will return
		 * [FirstKey]<ElementOfFirstKey>[SecondKey][hash(<ElementOf2ndKey>)][ThirdKey][hash(<ElementOf3ndKey>)] so
		 * for example: S<subject>P1O2 where 1 =: hash(<predicate>) and 2 =: hash(<object>)
		 */
		return new KeyContainer[] {
				new KeyContainer<String>(f(TYPE_SPO), triple.getSubject()
						.originalString()
						+ s(TYPE_SPO)
						+ getHashedComponent(0, 1, triple)
						+ t(TYPE_SPO)
						+ getHashedComponent(1, 2, triple)),
				new KeyContainer<String>(f(TYPE_PSO), triple.getPredicate()
						.originalString()
						+ s(TYPE_PSO)
						+ getHashedComponent(0, 0, triple)
						+ t(TYPE_PSO)
						+ getHashedComponent(1, 2, triple)),
				new KeyContainer<String>(f(TYPE_OPS), triple.getObject()
						.originalString()
						+ s(TYPE_OPS)
						+ getHashedComponent(0, 1, triple)
						+ t(TYPE_OPS)
						+ getHashedComponent(1, 0, triple)) };

	}

	/*
	 * Returns an random element of array
	 * 
	 * @param o the array
	 * 
	 * @return the item in array, choosen randomly
	 */
	private Object getRandomItem(Object... o) {
		// see documentation: we need deterministic here, so return first
		if (o.length == 0)
			return null;
		return o[0];

		/*
		 * for later implementation with histogramm static analysis this can be used or improved
		 *  (not deterministic): return o[new Random().nextInt(o.length)];
		 */
	}

	@SuppressWarnings("unchecked")
	public KeyContainer<String>[] getKeysForQuerying(
			final TriplePattern triplePattern)
			throws TriplePatternNotSupportedError {
		/*
		 * all in all we have: <S><P><O> <?><P><O> <?><?><O> <?><P><?> <S><?><?>
		 * <S><P><?> <S><?><O>
		 */

		if (triplePattern.getSubject().isVariable()
				&& triplePattern.getPredicate().isVariable()
				&& triplePattern.getObject().isVariable())
			throw new RuntimeException(String.format(
					"Triple %s is only containings variables.", triplePattern));

		// <S><P><O>
		if (!triplePattern.getSubject().isVariable()
				&& !triplePattern.getPredicate().isVariable()
				&& !triplePattern.getObject().isVariable()) {
			return new KeyContainer[] { new KeyContainer<String>(f(TYPE_SPO),
					((Literal) triplePattern.getSubject()).originalString()
							+ s(TYPE_SPO)
							+ getHashedComponent(0, 1, triplePattern)
							+ t(TYPE_SPO)
							+ getHashedComponent(1, 2, triplePattern)) };
		}

		if (triplePattern.getSubject().isVariable()) {
			// <?><P><O>
			if (!triplePattern.getPredicate().isVariable()
					&& !triplePattern.getObject().isVariable()) {
				KeyContainer[] result = new KeyContainer[getFirstBagsize()];
				for (int i = 0; i < getFirstBagsize(); i++) {
					result[i] = new KeyContainer<String>(f(TYPE_PSO),
							((Literal) triplePattern.getPredicate())
									.originalString()
									+ s(TYPE_PSO)
									+ i
									+ t(TYPE_PSO)
									+ getHashedComponent(1, 2, triplePattern));
				}
				return result;
			} // <?><?><O>
			else if (triplePattern.getPredicate().isVariable()) {
				KeyContainer[] result = new KeyContainer[getFirstBagsize()
						* getSecondBagsize()];
				int cnt = 0;
				for (int i = 0; i < getFirstBagsize(); i++) {
					for (int j = 0; j < getSecondBagsize(); j++) {
						result[cnt++] = new KeyContainer<String>(f(TYPE_OPS),
								((Literal) triplePattern.getObject())
										.originalString()
										+ s(TYPE_OPS)
										+ i
										+ t(TYPE_OPS) + j);
					}
				}
				return result;
			} // <?><P><?>
			else if (triplePattern.getObject().isVariable()) {
				KeyContainer[] result = new KeyContainer[getFirstBagsize()
						* getSecondBagsize()];
				int cnt = 0;
				//here: iterate over both partitions (so n*m keys)
				for (int i = 0; i < getFirstBagsize(); i++) {
					for (int j = 0; j < getSecondBagsize(); j++) {
						result[cnt++] = new KeyContainer<String>(f(TYPE_PSO),
								((Literal) triplePattern.getPredicate())
										.originalString()
										+ s(TYPE_PSO)
										+ i
										+ t(TYPE_PSO) + j);
					}
				}
				return result;
			}
		} else {
			// subject is not variable
			// <S><?><?>
			if (triplePattern.getPredicate().isVariable()
					&& triplePattern.getObject().isVariable()) {
				KeyContainer[] result = new KeyContainer[getFirstBagsize()
						* getSecondBagsize()];
				int cnt = 0;
				for (int i = 0; i < getFirstBagsize(); i++) {
					for (int j = 0; j < getSecondBagsize(); j++) {
						result[cnt++] = new KeyContainer<String>(f(TYPE_SPO),
								((Literal) triplePattern.getSubject())
										.originalString()
										+ s(TYPE_SPO)
										+ i
										+ t(TYPE_SPO) + j);
					}
				}
				return result;
			}
			// <S><P><?>
			else if (!triplePattern.getPredicate().isVariable()) {
				KeyContainer[] result = new KeyContainer[getFirstBagsize()];
				for (int i = 0; i < getFirstBagsize(); i++) {
					result[i] = new KeyContainer<String>(f(TYPE_SPO),
							((Literal) triplePattern.getSubject())
									.originalString()
									+ s(TYPE_SPO)
									+ getHashedComponent(0, 1, triplePattern)
									+ t(TYPE_SPO) + i);
				}
				return result;
			} // <S><?><O>
			else if (!triplePattern.getObject().isVariable()) {

				// use SPO
				KeyContainer[] result = new KeyContainer[getFirstBagsize()];
				for (int i = 0; i < getFirstBagsize(); i++) {
					result[i] = new KeyContainer<String>(f(TYPE_SPO),
							((Literal) triplePattern.getSubject())
									.originalString()
									+ s(TYPE_SPO)
									+ i
									+ t(TYPE_SPO)
									+ getHashedComponent(1, 2, triplePattern));
				}
				return result;
			}
		}
		throw new TriplePatternNotSupportedError(this, triplePattern);
	}

	@Override
	public String toString() {
		return "P2P double hierarchy distribution strategy (triple (s, p, o) has keys { 'SP' , 'PO', 'SO' , 'OS' , 'OP' , 'PS' })";
	}

	@Override
	public String[] getKeyTypes() {
		return TwoPartitionsDistribution.getPossibleKeyTypes();
	}

	public static String[] getPossibleKeyTypes() {
		return new String[] { TYPE_SPO, TYPE_PSO, TYPE_OPS };
	}

}
