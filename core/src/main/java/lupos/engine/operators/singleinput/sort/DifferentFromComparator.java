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
package lupos.engine.operators.singleinput.sort;

import java.util.Comparator;
public class DifferentFromComparator<E extends Comparable<E>> implements Comparator<E>{

	private Comparator<E> comp;
	
	/**
	 * Compares two given bindings as specified in SPARQL-specification
	 *
	 * @param comp a {@link java.util.Comparator} object.
	 */
	public DifferentFromComparator( Comparator<E> comp ){
		this.comp=comp;
	}

	/**
	 * Compares two bindings considering SPARQL-specifications
	 *
	 * @param arg0 first Bindings to compare
	 * @param arg1 second Bindings to compare
	 * @return simlar to any other integer based compare method: <br>
	 * -1 if l0 < l1<br>
	 *  1 if l0 > l1<br>
	 *  0 if l0 = l1<br>
	 *  but modified, as result will be multiplicated by -1 if descending order has been chosen.
	 */
	public int compare(E arg0, E arg1) {
		
		int compare=comp.compare(arg0, arg1);
		if(compare==0) {
			int compare2=arg0.compareTo(arg1);
			return compare2;
		}
		else return compare;
	}
}
