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

import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;

/**
 * Extension of the KeyContainer for holding a set of alternative key's where the same
 * data is stored, so that in optimization the best matching KeyContainer can be chosen.
 * 
 * @author Bjoern
 *
 * @param <T> The type of the {@link KeyContainer}
 */
public class AlternativeKeyContainer<T> extends KeyContainer<T>{
	private static final long serialVersionUID = -8293383650978266404L;

	/**
	 * Create a new KeyContainer with additional functionality as storing an alternate {@link KeyContainer}
	 * @param type the type
	 * @param key the key
	 */
	public AlternativeKeyContainer(String type, T key) {
		super(type, key);
	}
	
	/**
	 * Create a new KeyContainer with additional functionality as storing an alternate {@link KeyContainer}
	 * @param type the type
	 * @param key the key
	 * @param alternative the alternate, redundant {@link KeyContainer}
	 */
	public AlternativeKeyContainer(String type, T key, KeyContainer<T> alternative) {
		super(type, key);
		addAlternative(alternative);
	}
	
	@SuppressWarnings("unchecked")
	private KeyContainer<T>[] alternative = new KeyContainer[0];
	
	/**
	 * Adds an alternative to this {@link KeyContainer} which is redundant and has same data.
	 * @param alternative the alternative {@link KeyContainer}
	 * @return {@code this}
	 */
	@SuppressWarnings("unchecked")
	public synchronized AlternativeKeyContainer<T>  addAlternative(KeyContainer<T> alternative) {
		KeyContainer<T>[] copyButOneMore = new KeyContainer[this.alternative.length +1];
		System.arraycopy(this.alternative, 0, copyButOneMore, 0, this.alternative.length);
		copyButOneMore[this.alternative.length] = alternative;
		this.alternative = copyButOneMore;
		return this;
	}
	
	/**
	 * Is any alternative {@link KeyContainer} set?
	 * @return yes, or no, if none is set via {@link #addAlternative(KeyContainer)}
	 */
	public boolean hasAlternative() {
		return this.alternative != null && this.alternative.length != 0;
	}
	
	/**
	 * Returns the first alternative redundant {@link KeyContainer}
	 * @return the {@link KeyContainer} or {@code null}, if none set!
	 */
	public KeyContainer<T> getAlternative() {
		if (this.alternative.length == 0) return null;
		else return this.alternative[0];
	}
	
	/**
	 * Returns all alternatives
	 * @return all alternative redundant {@link KeyContainer}
	 */
	public KeyContainer<T>[] getAlternatives() {
		 return this.alternative;
	}

	/**
	 * Removes all alternatives
	 */
	@SuppressWarnings("unchecked")
	public void removeAlternatives() {
		this.alternative = new KeyContainer[0];
	}

}
