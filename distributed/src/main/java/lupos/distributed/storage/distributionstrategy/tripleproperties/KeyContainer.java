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

/**
 * This class is a container for the key consisting of its type (e.g. "P" for a key consisting of the predicate of a triple)
 * and the real key.
 *
 * @param <K> the java type of the real key
 */
public class KeyContainer<K> {

	/**
	 * the real key
	 */
	public final K key;
	/**
	 * the type of the key
	 */
	public final String type;

	/**
	 * constructor to set the type and the key
	 * @param type the type of the key
	 * @param key the real key
	 */
	public KeyContainer(final String type, final K key){
		this.type = type;
		this.key = key;
	}

	// the following methods "int hashCode()" and "boolean equals(Object o)" are overridden such that KeyContainers can be put into hash sets for duplicate elimination

	@Override
	public int hashCode(){
		return this.key.hashCode() + this.type.hashCode();
	}

	@Override
	public boolean equals(final Object o){
		if(o instanceof KeyContainer){
			@SuppressWarnings("rawtypes")
			final
			KeyContainer otherContainer = (KeyContainer) o;
			return (this.type.compareTo(otherContainer.type)==0 && this.key.equals(otherContainer.key));
		} else {
			return false;
		}
	}
}
