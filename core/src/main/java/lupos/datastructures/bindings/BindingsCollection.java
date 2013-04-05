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
package lupos.datastructures.bindings;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;

/**
 * Instances of this class store bindings in collections.<br>
 * A binding is an association between a variable and an
 * actual value called literal.<br>
 * In this collection, the variables and literals are stored
 * in two different sorted collections and can be associated
 * by their indexes in each collection: a variable at position
 * 'i' in the variables' collection is associated to the
 * literal at position 'i' in the literals' collection
 *
 * @author Sebastian Ebers
 *
 */
public class BindingsCollection extends Bindings{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8695527841805068857L;

	/** The collection storing the variables */
	private Vector<Variable> variables;

	/** The collection storing the literals */
	private Vector<Literal> literals;
	
	@Override
	public void init(){
		variables = new Vector<Variable>();
		literals = new Vector<Literal>();		
	}

	/** Constructor	 */
	public BindingsCollection(){
		init();
	}

	@Override
	public BindingsCollection clone(){
		final BindingsCollection other=new BindingsCollection();
		other.variables = new Vector<Variable>(this.variables);
		other.literals = new Vector<Literal>(this.literals);
		return other;
	}

	/**
	 * Adds a new binding to the collection.<br>
	 * If the variable is already bound, the old value
	 * will be dismissed. If the old value is still needed,
	 * the collection of bindings has to be cloned previously.
	 * @param varname  the variable's name
	 * @param literal  the literal
	 */
	@Override
	public void add(final Variable var, final Literal literal){
		final int index = variables.indexOf(var);
		if (index != -1){
			literals.setElementAt(literal, index);
		}else{
			variables.add(var);
			literals.add(literal);
		}
	}

	/**
	 * Returns the literal a variable is bound to.
	 * @param varname  the variable's name
	 * @return the literal a variable is bound to
	 */
	@Override
	public Literal get(final Variable var){
		final int i = variables.indexOf(var);
		if (i != -1){
			return literals.get(i);
		}
		return null;
	}

	/**
	 * Returns the set of bound variables
	 * @return the set of bound variables
	 */
	@Override
	public Set<Variable> getVariableSet(){
		final Set<Variable> keySet=new HashSet<Variable>();
		for(final Variable var: variables)
			if(get(var)!=null) keySet.add(var);
		return keySet;
	}

	/**
	 * Adds all bindings of another collection to this one.<br>
	 * If the bindings of the other collections conflict with
	 * the bindings of this collection, the old bindings of
	 * this one will be dismissed.
	 * @param other
	 */
	public void addAll(final BindingsCollection other){
		for (final Variable variable : other.variables) {
			add(variable,other.get(variable));
		}
	}

	@Override
	public boolean equals(final Object other) {

		// if the other instance is a BindingsCollection, too
		if (other instanceof BindingsCollection){
			final BindingsCollection otherBC = (BindingsCollection)other;
			return this.variables.equals(otherBC.variables) && this.literals.equals(otherBC.literals);
		} else return super.equals(other);
	}
}