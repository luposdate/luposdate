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
 * This class implements the distribution strategy with ninefold insertion,
 * where the last item of the key is hashed, so that all triples are stored in
 * partition by key only and not by the p2p implementation.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class NinefoldInsertionDistribution implements
		IDistributionKeyContainer<String> {

	/*
	 * The needed keys
	 */
	/** Constant <code>TYPE_POS="POS"</code> */
	protected final static String TYPE_POS = "POS";
	/** Constant <code>TYPE_SOP="SOP"</code> */
	protected final static String TYPE_SOP = "SOP";
	/** Constant <code>TYPE_SPO="SPO"</code> */
	protected final static String TYPE_SPO = "SPO";
	/** Constant <code>TYPE_SP="SP"</code> */
	protected final static String TYPE_SP = "SP";
	/** Constant <code>TYPE_SO="SO"</code> */
	protected final static String TYPE_SO = "SO";
	/** Constant <code>TYPE_PS="PS"</code> */
	protected final static String TYPE_PS = "PS";
	/** Constant <code>TYPE_PO="PO"</code> */
	protected final static String TYPE_PO = "PO";
	/** Constant <code>TYPE_OS="OS"</code> */
	protected final static String TYPE_OS = "OS";
	/** Constant <code>TYPE_OP="OP"</code> */
	protected final static String TYPE_OP = "OP";

	/**
	 * New instance
	 */
	public NinefoldInsertionDistribution() {
	}

	/**
	 * New instance with given bagSize
	 *
	 * @param bagSize
	 *            the among of partitions to be used for distribution
	 */
	public NinefoldInsertionDistribution(int bagSize) {
		this.bagSize = bagSize;
	}

	/**
	 * the size of bags for distribution, so for: 4, the keys are hashed modulo
	 * 4.
	 */
	private int bagSize = 2;

	/**
	 * Returns the bag size
	 *
	 * @return the bag size
	 */
	public int getBagsize() {
		return bagSize;
	}

	/*
	 * hashes the {index}-component of triple with the hash function
	 */
	private int getHashedComponent(int component, TriplePattern t) {
		if (component < 3 && component >= 0)
			return Math.abs(((Literal) t.getPos(component)).originalString()
					.hashCode()) % getBagsize();
		else
			throw new RuntimeException(String.format(
					"Component %d not found in %s", component, t));
	}

	/*
	 * hashes the {index}-component of triple with the hash function
	 */
	private int getHashedComponent(int component, Triple t) {
		if (component < 3 && component >= 0) {
			Literal a = t.getPos(component);
			/* if components are null */
			String str = (a == null) ? "null" : a.originalString();
			return Math.abs(str.hashCode()) % getBagsize();
		} else
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
		if (s == null || s.length() == 0 || s.length() < 2)
			return "";
		return s.charAt(1) + "";
	}

	/*
	 * returns the second char of the string, because for hashing this is used
	 * in front of the new key used.
	 */
	private String t(String s) {
		if (s == null || s.length() == 0 || s.length() < 2)
			return "";
		return s.charAt(2) + "";
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public KeyContainer<String>[] getKeysForStoring(final Triple triple) {
		return new KeyContainer[] {
				/*
				 * All nine possibilities to be used as P2P distribution key
				 */
				new KeyContainer<String>(f(TYPE_SPO), triple.getSubject()
						.originalString()
						+ s(TYPE_SPO)
						+ triple.getPredicate()
						+ t(TYPE_SPO) + getHashedComponent(2, triple)),
				new KeyContainer<String>(f(TYPE_SOP), triple.getSubject()
						.originalString()
						+ s(TYPE_SOP)
						+ triple.getObject()
						+ t(TYPE_SOP) + getHashedComponent(1, triple)),
				new KeyContainer<String>(f(TYPE_POS), triple.getPredicate()
						.originalString()
						+ s(TYPE_POS)
						+ triple.getObject()
						+ t(TYPE_POS) + getHashedComponent(0, triple)),
				new KeyContainer<String>(f(TYPE_SP), triple.getSubject()
						.originalString()
						+ s(TYPE_SP)
						+ getHashedComponent(1, triple)),
				new KeyContainer<String>(f(TYPE_SO), triple.getSubject()
						.originalString()
						+ s(TYPE_SP)
						+ getHashedComponent(2, triple)),
				new KeyContainer<String>(f(TYPE_PS), triple.getPredicate()
						.originalString()
						+ s(TYPE_PS)
						+ getHashedComponent(0, triple)),
				new KeyContainer<String>(f(TYPE_PO), triple.getPredicate()
						.originalString()
						+ s(TYPE_PO)
						+ getHashedComponent(2, triple)),
				new KeyContainer<String>(f(TYPE_OP), triple.getObject()
						.originalString()
						+ s(TYPE_OP)
						+ getHashedComponent(1, triple)),
				new KeyContainer<String>(f(TYPE_OS), triple.getObject()
						.originalString()
						+ s(TYPE_OS)
						+ getHashedComponent(0, triple)) };
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
				&& triplePattern.getObject().isVariable())
			throw new RuntimeException(String.format(
					"Triple %s is only containings variables.", triplePattern));

		// <S><P><O>
		if (!triplePattern.getSubject().isVariable()
				&& !triplePattern.getPredicate().isVariable()
				&& !triplePattern.getObject().isVariable()) {
			// SPO used as main KeyContainer, you can also use: SOP, POS as
			// alternatives
			return new KeyContainer[] { new AlternativeKeyContainer<String>(
					f(TYPE_SPO),
					((Literal) triplePattern.getSubject()).originalString()
							+ s(TYPE_SPO)
							+ ((Literal) triplePattern.getPredicate())
									.originalString() + t(TYPE_SPO)
							+ getHashedComponent(2, triplePattern))
					.addAlternative(
							new KeyContainer<String>(f(TYPE_SOP),
									((Literal) triplePattern.getSubject())
											.originalString()
											+ s(TYPE_SOP)
											+ ((Literal) triplePattern
													.getObject())
													.originalString()
											+ t(TYPE_SOP)
											+ getHashedComponent(1,
													triplePattern)))
					.addAlternative(
							new KeyContainer<String>(f(TYPE_POS),
									((Literal) triplePattern.getPredicate())
											.originalString()
											+ s(TYPE_POS)
											+ ((Literal) triplePattern
													.getObject())
													.originalString()
											+ t(TYPE_POS)
											+ getHashedComponent(0,
													triplePattern))) };
		}

		if (triplePattern.getSubject().isVariable()) {
			// <?><P><O>
			if (!triplePattern.getPredicate().isVariable()
					&& !triplePattern.getObject().isVariable()) {
				KeyContainer[] result = new KeyContainer[getBagsize()];
				//iterate all partitions and use them as third component
				for (int i = 0; i < getBagsize(); i++) {
					result[i] = new KeyContainer<String>(f(TYPE_POS),
							((Literal) triplePattern.getPredicate())
									.originalString()
									+ s(TYPE_POS)
									+ ((Literal) triplePattern.getObject())
											.originalString() + t(TYPE_POS) + i);
				}
				return result;
			} // <?><?><O>
			else if (triplePattern.getPredicate().isVariable()) {
				// choices: all in OP or all in OS
				String type = TYPE_OS;
				String type2 = TYPE_OP;
				KeyContainer[] result = new AlternativeKeyContainer[getBagsize()];
				/*
				 * iterate over all partitions and use keys OP and OS
				 */
				for (int i = 0; i < getBagsize(); i++) {
					result[i] = new AlternativeKeyContainer<String>(f(type),
							((Literal) triplePattern.getObject())
									.originalString() + s(type) + i)
							.addAlternative(new KeyContainer<String>(f(type2),
									((Literal) triplePattern.getObject())
											.originalString() + s(type2) + i));
				}
				// now OP set as alternative
				return result;
			} // <?><P><?>
			else if (triplePattern.getObject().isVariable()) {
				// choices: all in PS or all in PO
				String type = TYPE_PO;
				String type2 = TYPE_PS;
				KeyContainer[] result = new KeyContainer[getBagsize()];
				/*
				 * iterate over all partitions ...
				 */
				for (int i = 0; i < getBagsize(); i++) {
					result[i] = new AlternativeKeyContainer<String>(f(type),
							((Literal) triplePattern.getPredicate())
									.originalString() + s(type) + i)
							.addAlternative(new KeyContainer<String>(f(type2),
									((Literal) triplePattern.getPredicate())
											.originalString() + s(type2) + i));
				}
				return result;
			}
		} else {
			// subject is not variable
			// <S><?><?>
			if (triplePattern.getPredicate().isVariable()
					&& triplePattern.getObject().isVariable()) {
				// choices: all in SP or all in SO
				String type = TYPE_SP;
				String type2 = TYPE_SO;
				AlternativeKeyContainer[] result = new AlternativeKeyContainer[getBagsize()];
				for (int i = 0; i < getBagsize(); i++) {
					result[i] = new AlternativeKeyContainer<String>(f(type),
							((Literal) triplePattern.getSubject())
									.originalString() + s(type) + i)
							.addAlternative(new KeyContainer<String>(f(type2),
									((Literal) triplePattern.getSubject())
											.originalString() + s(type2) + i));
				}
				return result;
			}
			// <S><P><?>
			else if (!triplePattern.getPredicate().isVariable()) {
				KeyContainer[] result = new AlternativeKeyContainer[getBagsize()];
				for (int i = 0; i < getBagsize(); i++) {
					result[i] = new AlternativeKeyContainer<String>(
							f(TYPE_SPO),
							((Literal) triplePattern.getSubject())
									.originalString()
									+ s(TYPE_SPO)
									+ ((Literal) triplePattern.getPredicate())
											.originalString() + t(TYPE_SPO) + i);
				}
				return result;
			} // <S><?><O>
			else if (!triplePattern.getObject().isVariable()) {
				KeyContainer[] result = new AlternativeKeyContainer[getBagsize()];
				for (int i = 0; i < getBagsize(); i++) {
					result[i] = new AlternativeKeyContainer<String>(
							f(TYPE_SOP),
							((Literal) triplePattern.getSubject())
									.originalString()
									+ s(TYPE_SOP)
									+ ((Literal) triplePattern.getObject())
											.originalString() + t(TYPE_SOP) + i);
				}
			}
		}
		throw new TriplePatternNotSupportedError(this, triplePattern);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "P2P distribution strategy with 9-fold insertion (triple (s, p, o) has keys { 'SP' , 'PO', 'SO' , 'OS' , 'OP' , 'PS', 'SPO', 'SOP', 'POS' }, where the last item is hashed.)";
	}

	/** {@inheritDoc} */
	@Override
	public String[] getKeyTypes() {
		return NinefoldInsertionDistribution.getPossibleKeyTypes();
	}

	/**
	 * <p>getPossibleKeyTypes.</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
	public static String[] getPossibleKeyTypes() {
		return new String[] { TYPE_SP, TYPE_SO, TYPE_PO, TYPE_OP, TYPE_OS,
				TYPE_PS, TYPE_SPO, TYPE_SOP, TYPE_POS };
	}
}
