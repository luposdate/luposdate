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
package lupos.datastructures.bindings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;



public class BindingsMap extends Bindings
{
	/**
	 *
	 */
	private static final long serialVersionUID = -2283705193034764491L;

	protected HashMap<Variable, Literal> hashMap=new HashMap<Variable, Literal>();

	@Override
	public void init(){
		this.hashMap=new HashMap<Variable, Literal>();
	}

	public BindingsMap(){
	}

	@Override
	@SuppressWarnings("unchecked")
	public Bindings clone()
	{
		final BindingsMap bnew=new BindingsMap();
		bnew.hashMap=(HashMap<Variable, Literal>)this.hashMap.clone();
		return bnew;
	}

	@Override
	public void add(final Variable var, final Literal lit)
	{
		this.hashMap.put(var, lit);
	}

	@Override
	public Literal get(final Variable var)
	{
		return this.hashMap.get(var);
	}

	/**
	 * Returns the set of bound variables
	 * @return the set of bound variables
	 */
	@Override
	public Set<Variable> getVariableSet(){
		final Set<Variable> keySet=new HashSet<Variable>();
		for(final Variable var: this.hashMap.keySet()) {
			if(this.hashMap.get(var)!=null) {
				keySet.add(var);
			}
		}
		return keySet;
	}

	@Override
	public BindingsMap createInstance(){
		return new BindingsMap();
	}
}