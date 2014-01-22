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
package lupos.distributed.p2p.distributionstrategy;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.distributed.storage.distributionstrategy.TriplePatternNotSupportedError;
import lupos.distributed.storage.distributionstrategy.tripleproperties.IDistributionKeyContainer;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.engine.operators.tripleoperator.TriplePattern;

/**
 * This class implements the distribution strategy, where the hierarchy of
 * distribution is set via hash function.
 */
public class SimplePartitionDistribution implements
		IDistributionKeyContainer<String> {

	/*
	 * The needed keys
	 */
	protected final static String TYPE_SP = "SP";
	protected final static String TYPE_PO = "PO";
	protected final static String TYPE_SO = "SO";
	protected final static String TYPE_PS = "PS";
	protected final static String TYPE_OP = "OP";
	protected final static String TYPE_OS = "OS";

	/**
	 * the size of bags for distribution, so for: 4, the keys are hashed modulo
	 * 4.
	 */
	private final int bagSize = 2;

	/**
	 * Returns the bag size
	 *
	 * @return the bag size
	 */
	public int getBagsize() {
		return this.bagSize;
	}

	/*
	 * hashes the {index}-component of triple with the hash function
	 */
	private int getHashedComponent(final int component, final TriplePattern t) {
		if (component < 3 && component >= 0) {
			return Math.abs(((Literal) t.getPos(component)).originalString()
					.hashCode()) % this.getBagsize();
		} else {
			throw new RuntimeException(String.format(
					"Component %d not found in %s", component, t));
		}
	}

	/*
	 * hashes the {index}-component of triple with the hash function
	 */
	private int getHashedComponent(final int component, final Triple t) {
		if (component < 3 && component >= 0) {
			return Math.abs(t.getPos(component).originalString().hashCode()) % this.getBagsize();
		} else {
			throw new RuntimeException(String.format("Component %d not found in %s", component, t));
		}
	}

	/*
	 * returns the first char of the string, because for hashing this is used in
	 * front of the new key used.
	 */
	private String f(final String s) {
		if (s == null || s.length() == 0){
			return "";
		}
		return s.charAt(0) + "";
	}

	/*
	 * returns the second char of the string, because for hashing this is used
	 * in front of the new key used.
	 */
	private String s(final String s) {
		if (s == null || s.length() == 0 || s.length() < 2){
			return "";
		}
		return s.charAt(1) + "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public KeyContainer<String>[] getKeysForStoring(final Triple triple) {
		/*
		 * will return
		 * [FirstKey]<ElementOfFirstKey>[SecondKey][hash(<ElementOf2ndKey>)] so
		 * for example: S<subject>P1 where 1 =: hash(<predicate>)
		 */
		return new KeyContainer[] {
				new KeyContainer<String>(this.f(TYPE_OS), triple.getObject()
						.originalString()
						+ this.s(TYPE_OS)
						+ this.getHashedComponent(0, triple)),
				/*
				 * new KeyContainer<String>(f(TYPE_OP),
				 * triple.getObject().originalString() + s(TYPE_OP) +
				 * getHashedComponent(1,triple)),
				 */
				new KeyContainer<String>(this.f(TYPE_PO), triple.getPredicate()
						.originalString()
						+ this.s(TYPE_PO)
						+ this.getHashedComponent(2, triple)),
				/*
				 * new KeyContainer<String>(f(TYPE_PS),
				 * triple.getPredicate().originalString() + s(TYPE_PS) +
				 * getHashedComponent(0,triple)),
				 */
				new KeyContainer<String>(this.f(TYPE_SP), triple.getSubject()
						.originalString()
						+ this.s(TYPE_SP)
						+ this.getHashedComponent(1, triple))
		/*
		 * , new KeyContainer<String>(f(TYPE_SO),
		 * triple.getSubject().originalString() + s(TYPE_SO) +
		 * getHashedComponent(2,triple))
		 */
		};
		// Because of use of a deterministic calculation, the remaining 3 keys
		// are uncommented
	}

	/*
	 * Returns an random element of array
	 *
	 * @param o the array
	 *
	 * @return the item in array, choosen randomly
	 */
	private Object getRandomItem(final Object... o) {
		// see documentation: we need deterministic here, so return first
		if (o.length == 0) {
			return null;
		}
		return o[0];
		/*
		 * for later implementation with no need of deterministic calculation
		 * this old way is to be used: return o[new Random().nextInt(o.length)];
		 */
	}



	/*
	 * this is the old method, which was not deterministic in its resulting
	 * keycontainer
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public KeyContainer<String>[] getKeysForQueryingNonDeterministic(
			final TriplePattern triplePattern)
			throws TriplePatternNotSupportedError {
		/*
		 * all in all we have: <S><P><O> <?><P><O> <?><?><O> <?><P><?> <S><?><?>
		 * <S><P><?> <S><?><O>
		 */

		// <S><P><O>
		if (!triplePattern.getSubject().isVariable()
				&& !triplePattern.getPredicate().isVariable()
				&& !triplePattern.getObject().isVariable()) {
			// here we can decide between 6 methods:
			final KeyContainer[] choices = new KeyContainer[] {
					new KeyContainer<String>(this.f(TYPE_SP),
							(triplePattern.getSubject()).toString()
									+ this.s(TYPE_SP)
									+ this.getHashedComponent(1, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_PO),
							(triplePattern.getPredicate()).toString()
									+ this.s(TYPE_PO)
									+ this.getHashedComponent(2, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_PS),
							(triplePattern.getPredicate()).toString()
									+ this.s(TYPE_PS)
									+ this.getHashedComponent(0, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_SO),
							(triplePattern.getSubject()).toString()
									+ this.s(TYPE_SO)
									+ this.getHashedComponent(2, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_OS),
							(triplePattern.getObject()).toString() + this.s(TYPE_OS)
									+ this.getHashedComponent(0, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_OP),
							(triplePattern.getObject()).toString() + this.s(TYPE_OP)
									+ this.getHashedComponent(1, triplePattern)) };
			//with use of non-deterministic we could choose the best
			//key here, or use static analysis via histrogram support
			final KeyContainer type = (KeyContainer) this.getRandomItem(choices);
			return new KeyContainer[] { type };
		}

		if (triplePattern.getSubject().isVariable()) {
			// <?><P><O>
			if (!triplePattern.getPredicate().isVariable()
					&& !triplePattern.getObject().isVariable()) {
				return new KeyContainer[] { new KeyContainer<String>(
						this.f(TYPE_PO), (triplePattern.getPredicate()).toString()
								+ this.s(TYPE_PO)
								+ this.getHashedComponent(2, triplePattern)) };
			} // <?><?><O>
			else if (triplePattern.getPredicate().isVariable()) {
				// choices: all in OP or all in OS
				final String type = (String) this.getRandomItem(new String[] { TYPE_OS,
						TYPE_OP });
				final KeyContainer[] result = new KeyContainer[this.getBagsize()];
				for (int i = 0; i < this.getBagsize(); i++) {
					result[i] = new KeyContainer<String>(this.f(type),
							(triplePattern.getObject()).toString() + this.s(type)
									+ i);
				}
				return result;

			} // <?><P><?>
			else if (triplePattern.getObject().isVariable()) {
				// choices: all in PS or all in PO
				final String type = (String) this.getRandomItem(new String[] { TYPE_PO,
						TYPE_PS });
				final KeyContainer[] result = new KeyContainer[this.getBagsize()];
				for (int i = 0; i < this.getBagsize(); i++) {
					result[i] = new KeyContainer<String>(this.f(type),
							(triplePattern.getPredicate()).toString() + this.s(type)
									+ i);
				}
				return result;
			}
		} else {
			// subject is not variable
			// <S><?><?>
			if (triplePattern.getPredicate().isVariable()
					&& triplePattern.getObject().isVariable()) {
				// choices: all in SP or all in SO
				final String type = (String) this.getRandomItem(new String[] { TYPE_SP,
						TYPE_SO });
				final KeyContainer[] result = new KeyContainer[this.getBagsize()];
				for (int i = 0; i < this.getBagsize(); i++) {
					result[i] = new KeyContainer<String>(this.f(type),
							(triplePattern.getSubject()).toString() + this.s(type)
									+ i);
				}
				return result;
			}
			// <S><P><?>
			else if (!triplePattern.getPredicate().isVariable()) {
				return new KeyContainer[] { new KeyContainer<String>(
						this.f(TYPE_SP), (triplePattern.getSubject()).toString()
								+ this.s(TYPE_SP)
								+ this.getHashedComponent(1, triplePattern)) };
			} // <S><?><O>
			else if (!triplePattern.getObject().isVariable()) {
				return new KeyContainer[] { new KeyContainer<String>(
						this.f(TYPE_OS), (triplePattern.getObject()).toString()
								+ this.s(TYPE_OS)
								+ this.getHashedComponent(0, triplePattern)) };
			}
		}
		throw new TriplePatternNotSupportedError(this, triplePattern);
	}

	@SuppressWarnings("unchecked")
	@Override
	public KeyContainer<String>[] getKeysForQuerying(
			final TriplePattern triplePattern)
			throws TriplePatternNotSupportedError {
		/*
		 * all in all we have: <S><P><O> <?><P><O> <?><?><O> <?><P><?> <S><?><?>
		 * <S><P><?> <S><?><O>
		 */

		if (triplePattern.getSubject().isVariable()
				&& triplePattern.getPredicate().isVariable()
				&& triplePattern.getObject().isVariable()) {
			throw new RuntimeException(String.format(
					"Triple %s is only containings variables.", triplePattern));
		}

		// <S><P><O>
		if (!triplePattern.getSubject().isVariable()
				&& !triplePattern.getPredicate().isVariable()
				&& !triplePattern.getObject().isVariable()) {
			// here we can decide between 6 methods:
			final KeyContainer[] choices = new KeyContainer[] {
					new KeyContainer<String>(this.f(TYPE_SP),
							((Literal) triplePattern.getSubject())
									.originalString()
									+ this.s(TYPE_SP)
									+ this.getHashedComponent(1, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_PO),
							((Literal) triplePattern.getPredicate())
									.originalString()
									+ this.s(TYPE_PO)
									+ this.getHashedComponent(2, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_PS),
							((Literal) triplePattern.getPredicate())
									.originalString()
									+ this.s(TYPE_PS)
									+ this.getHashedComponent(0, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_SO),
							((Literal) triplePattern.getSubject())
									.originalString()
									+ this.s(TYPE_SO)
									+ this.getHashedComponent(2, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_OS),
							((Literal) triplePattern.getObject())
									.originalString()
									+ this.s(TYPE_OS)
									+ this.getHashedComponent(0, triplePattern)),
					new KeyContainer<String>(this.f(TYPE_OP),
							((Literal) triplePattern.getObject())
									.originalString()
									+ this.s(TYPE_OP)
									+ this.getHashedComponent(1, triplePattern)) };
			final KeyContainer type = (KeyContainer) this.getRandomItem(choices);
			return new KeyContainer[] { type };
		}

		if (triplePattern.getSubject().isVariable()) {
			// <?><P><O>
			if (!triplePattern.getPredicate().isVariable()
					&& !triplePattern.getObject().isVariable()) {
				return new KeyContainer[] { new KeyContainer<String>(
						this.f(TYPE_PO),
						((Literal) triplePattern.getPredicate())
								.originalString()
								+ this.s(TYPE_PO)
								+ this.getHashedComponent(2, triplePattern)) };
			} // <?><?><O>
			else if (triplePattern.getPredicate().isVariable()) {
				// choices: all in OP or all in OS
				final String type = (String) this.getRandomItem(new String[] { TYPE_OS,
						TYPE_OP });
				final KeyContainer[] result = new KeyContainer[this.getBagsize()];
				for (int i = 0; i < this.getBagsize(); i++) {
					result[i] = new KeyContainer<String>(this.f(type),
							((Literal) triplePattern.getObject())
									.originalString() + this.s(type) + i);
				}
				return result;

			} // <?><P><?>
			else if (triplePattern.getObject().isVariable()) {
				// choices: all in PS or all in PO
				final String type = (String) this.getRandomItem(new String[] { TYPE_PO,
						TYPE_PS });
				final KeyContainer[] result = new KeyContainer[this.getBagsize()];
				for (int i = 0; i < this.getBagsize(); i++) {
					result[i] = new KeyContainer<String>(this.f(type),
							((Literal) triplePattern.getPredicate())
									.originalString() + this.s(type) + i);
				}
				return result;
			}
		} else {
			// subject is not variable
			// <S><?><?>
			if (triplePattern.getPredicate().isVariable()
					&& triplePattern.getObject().isVariable()) {
				// choices: all in SP or all in SO
				final String type = (String) this.getRandomItem(new String[] { TYPE_SP,
						TYPE_SO });
				final KeyContainer[] result = new KeyContainer[this.getBagsize()];
				for (int i = 0; i < this.getBagsize(); i++) {
					result[i] = new KeyContainer<String>(this.f(type),
							((Literal) triplePattern.getSubject())
									.originalString() + this.s(type) + i);
				}
				return result;
			}
			// <S><P><?>
			else if (!triplePattern.getPredicate().isVariable()) {
				return new KeyContainer[] { new KeyContainer<String>(
						this.f(TYPE_SP),
						((Literal) triplePattern.getSubject()).originalString()
								+ this.s(TYPE_SP)
								+ this.getHashedComponent(1, triplePattern)) };
			} // <S><?><O>
			else if (!triplePattern.getObject().isVariable()) {
				return new KeyContainer[] { new KeyContainer<String>(
						this.f(TYPE_OS),
						((Literal) triplePattern.getObject()).originalString()
								+ this.s(TYPE_OS)
								+ this.getHashedComponent(0, triplePattern)) };
			}
		}
		throw new TriplePatternNotSupportedError(this, triplePattern);
	}

	@Override
	public String toString() {
		return "P2P hierarchy distribution strategy (triple (s, p, o) has keys { 'SP' , 'PO', 'SO' , 'OS' , 'OP' , 'PS' })";
	}

	@Override
	public String[] getKeyTypes() {
		return SimplePartitionDistribution.getPossibleKeyTypes();
	}

	public static String[] getPossibleKeyTypes() {
		return new String[] { TYPE_SP, TYPE_SO, TYPE_PO, TYPE_OP, TYPE_OS,
				TYPE_PS };
	}
}
