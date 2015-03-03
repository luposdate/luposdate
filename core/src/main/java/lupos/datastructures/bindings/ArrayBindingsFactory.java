
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.datastructures.bindings;

import java.util.Collection;
import java.util.Map;

import lupos.datastructures.items.Variable;
public class ArrayBindingsFactory extends BindingsFactory {

	/**
	 * <p>Constructor for ArrayBindingsFactory.</p>
	 */
	protected ArrayBindingsFactory(){
		super();
	}

	/**
	 * <p>Constructor for ArrayBindingsFactory.</p>
	 *
	 * @param variables a {@link java.util.Collection} object.
	 */
	protected ArrayBindingsFactory(final Collection<Variable> variables){
		super(variables);
	}


	/**
	 * <p>Constructor for ArrayBindingsFactory.</p>
	 *
	 * @param variables an array of {@link lupos.datastructures.items.Variable} objects.
	 */
	protected ArrayBindingsFactory(final Variable[] variables){
		super(variables);
	}

	/**
	 * <p>Constructor for ArrayBindingsFactory.</p>
	 *
	 * @param posVariables a {@link java.util.Map} object.
	 */
	protected ArrayBindingsFactory(final Map<Variable, Integer> posVariables){
		super(posVariables);
	}

	/** {@inheritDoc} */
	@Override
	public Bindings createInstance(){
		return new BindingsArray(this);
	}

}
