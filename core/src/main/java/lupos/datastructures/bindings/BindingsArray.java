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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.misc.util.ImmutableIterator;

/**
 * Instances of this class store bindings in arrays.<br>
 * A binding is an association between a variable and an actual value called
 * literal.<br>
 * In this object, the variables and literals are stored in two different arrays
 * and can be associated by their indexes in each array:a variable at
 * position'i' in the variables' array is associated to the literal at position
 * 'i' in the literals' array.<br>
 * <br>
 *
 * The available variables have to be processed previously since it is no good
 * idea to use variable size when using arrays. The variables have to be
 * provided when initializing a new instance of this class for the first time.
 * These variables are stored statically and in the same order they have been
 * provided.
 *
 */
public class BindingsArray extends Bindings {

	private static final long serialVersionUID = 5900739377315263485L;

	/**
	 * The BindingsFactory, which contains the variables + position in literals
	 */
	protected final BindingsFactory bindingsFactory;

	/** The array storing the literals */
	protected Literal[] literals;

	private static ReentrantLock lock = new ReentrantLock();

	/**
	 * Constructor
	 */
	protected BindingsArray(final BindingsFactory bindingsFactory){
		this.bindingsFactory = bindingsFactory;
		this.literals = new Literal[this.bindingsFactory.posVariables.size()];
	}

	/**
	 * Constructor. Be careful: only use this constructor if you know what you are doing!
	 */
	public BindingsArray(final BindingsFactory bindingsFactory, final Literal[] literals){
		this.bindingsFactory = bindingsFactory;
		this.literals = literals;
	}

	/**
	 * @return the literals
	 */
	public Literal[] getLiterals() {
		return this.literals;
	}

	/**
	 * Sets the literals.<br>
	 * In fact, provided array of literals copied by an
	 * {@link System#arraycopy(Object, int, Object, int, int)} call with the
	 * array of literals from this class as target and the provided array as
	 * source.
	 *
	 * @param literals
	 *            the literals to set
	 */
	protected void cloneLiterals(final Literal[] otherLiterals) {
		System.arraycopy(otherLiterals, 0, this.literals, 0, otherLiterals.length);
	}


	/**
	 * Returns if the order of the provided variables equals the order of a
	 * collection of variables which are currently stored
	 *
	 * @param variables
	 *            the collection of variables
	 * @return <code>true</code> if the order of the variables matches
	 */
	private boolean isMatchVariablePosition(final Collection<Variable> variables) {
		int i = 0;
		for (final Variable variable : variables) {
			if (i++ != this.bindingsFactory.posVariables.get(variable)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public BindingsArray clone() {
		final BindingsArray other = new BindingsArray(this.bindingsFactory);
		other.cloneLiterals(this.getLiterals());
		return other;
	}

	/**
	 * Adds a new binding to the collection.<br>
	 * If the variable is already bound, the old value will be dismissed. If the
	 * old value is still needed, the collection of bindings has to be cloned
	 * previously.
	 *
	 * @param varname
	 *            the variable's name
	 * @param literal
	 *            the literal
	 */
	@Override
	public void add(final Variable var, final Literal literal) {
		final Integer i = this.bindingsFactory.posVariables.get(var);
		if (i != null) {
			this.literals[i] = literal;
		}
	}

	/**
	 * Returns the literal a variable is bound to.
	 *
	 * @param varname
	 *            the variable's name
	 * @return the literal a variable is bound to
	 */
	@Override
	public Literal get(final Variable var) {
		final Integer i = this.bindingsFactory.posVariables.get(var);
		if (i != null) {
			return this.literals[i];
		}
		return null;
	}

	/**
	 * Returns the set of bound variables
	 *
	 * @return the set of bound variables
	 */
	@Override
	public Set<Variable> getVariableSet() {
		final HashSet<Variable> hs = new HashSet<Variable>();
		for (final Variable var :this.bindingsFactory.posVariables.keySet()) {
			if(this.get(var) != null){
				hs.add(var);
			}
		}
		return hs;
	}

	@Override
	public Iterator<Variable> getVariables() {
		// this method is overridden because of performance issues...
		final Iterator<Variable> it_var = this.bindingsFactory.posVariables.keySet().iterator();
		return new ImmutableIterator<Variable>(){

			Variable var = null;

			@Override
			public boolean hasNext() {
				if(this.var == null){
					this.var = this.next();
				}
				return (this.var!=null);
			}

			@Override
			public Variable next() {
				if(this.var != null){
					final Variable result = this.var;
					this.var = null;
					return result;
				}
				while(it_var.hasNext()){
					final Variable result = it_var.next();
					if(BindingsArray.this.literals[BindingsArray.this.bindingsFactory.posVariables.get(result)] != null){
						return result;
					}
				}
				return null;
			}
		};
	}

	/**
	 * Adds all bindings of another collection to this one.<br>
	 * If the bindings of the other collections conflict with the bindings of
	 * this collection, the old bindings of this one will be dismissed.
	 *
	 * @param other
	 */
	public void addAll(final BindingsArray other) {
		// add or overwrite all values of the other bindings object
		for (int i = 0; i < other.literals.length; i++) {
			// a null value must not be added (it could overwrite)
			// a valid value
			if (other.literals[i] != null) {
				this.literals[i] = other.literals[i];
			}
		}
	}

	@Override
	public void init() {
		lock.lock();
		try {
			this.literals = new Literal[this.bindingsFactory.posVariables.size()];
		} finally {
			lock.unlock();
		}
	}

	@Override
	public BindingsArray createInstance(){
		return new BindingsArray(this.bindingsFactory);
	}

	public BindingsFactory getBindingsFactory() {
		return this.bindingsFactory;
	}
}