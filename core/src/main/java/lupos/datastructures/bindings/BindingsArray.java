/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;


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

	/** The hashmap storing the variables + position in literals */
	protected static Map<Variable, Integer> posVariables = null;
	/** The array storing the literals */
	protected Literal[] literals;

	private static ReentrantLock lock = new ReentrantLock();

	/**
	 * Constructor
	 * 
	 * @throws UnsupportedOperationException
	 *             will be thrown if this constructor is used and the variables
	 *             have not been initialized previously
	 */
	public BindingsArray() throws UnsupportedOperationException {
		if (posVariables == null) {
			throw new UnsupportedOperationException(
					"When using Bindings with arrays, the variables have to be known");
		}
		init();
	}

	/**
	 * Constructor
	 * 
	 * @param variables
	 *            the variables to be set
	 * @throws UnsupportedOperationException
	 *             will be thrown if the variables have already been set and the
	 *             order of the new variables do not match the one of the
	 *             already initialized ones
	 */
	public BindingsArray(final Variable[] variables)
			throws UnsupportedOperationException {
		if (posVariables != null
				&& !isMatchVariablePosition(Arrays.asList(variables))) {
			throw new UnsupportedOperationException(
					"The array of variables has already been initialized");
		}
		setVariables(variables);
	}

	/**
	 * Constructor
	 * 
	 * @param variables
	 *            the variables to be set
	 * @throws UnsupportedOperationException
	 *             will be thrown if the variables have already been set and the
	 *             order of the new variables do not match the one of the
	 *             already initialized ones
	 */
	public BindingsArray(final Set<Variable> variables)
			throws UnsupportedOperationException {
		if (posVariables != null && !isMatchVariablePosition(variables)) {
			throw new UnsupportedOperationException(
					"The array of variables has already been initialized");
		}
		setVariables(variables);
	}

	private void setVariables(final Variable[] variables) {
		initPosVars(variables);
		init();
	}

	/**
	 * @return the literals
	 */
	public Literal[] getLiterals() {
		return literals;
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
		System.arraycopy(otherLiterals, 0, this.literals, 0,
				otherLiterals.length);
	}

	/**
	 * Sets the variables maintained by this data structure even if they have
	 * been initialized previously.<br>
	 * If the order of the new variables does not match the previous ones,
	 * inconsistency errors may occur!
	 * 
	 * @param variables
	 *            the variables to be set
	 * @deprecated if the variables have been initialized previously,
	 *             inconsistency errors may occur.
	 */
	@Deprecated
	public static void forceVariables(final Set<Variable> variables) {
		// log.warn(
		// "A static of the data structure's variables was forced which might lead to problems!"
		// );
		final Variable[] variables2 = new Variable[variables.size()];
		int i = 0;
		for (final Variable var : variables) {
			variables2[i++] = var;
		}
		initPosVars(variables2);
	}

	/**
	 * Sets the variables maintained by this data structure even if they have
	 * been initialized previously.<br>
	 * If the order of the new variables does not match the previous ones,
	 * inconsistency errors may occur!
	 * 
	 * @param variables
	 *            the variables to be set
	 * @deprecated if the variables have been initialized previously,
	 *             inconsistency errors may occur.
	 */
	@Deprecated
	public static void forceVariables(final Map<Variable, Integer> variables) {
		lock.lock();
		try {
			BindingsArray.posVariables = variables;
		} finally {
			lock.unlock();
		}
	}
	
	
	/**
	 * Sets the variables maintained by this data structure
	 * 
	 * @param variables
	 *            the variables to be set
	 */
	private void setVariables(final Set<Variable> variables) {
		final Variable[] variables2 = new Variable[variables.size()];
		int i = 0;
		for (final Variable var : variables) {
			variables2[i++] = var;
		}
		setVariables(variables2);
	}

	/**
	 * Returns a map providing information about variables and their positions
	 * in this data structure
	 * 
	 * @return a map providing information about variables and their positions
	 *         in this data structure
	 */
	public static Map<Variable, Integer> getPosVariables() {
		return posVariables;
	}

	private static void initPosVars(final Variable[] variables) {
		lock.lock();
		try {
			posVariables = new HashMap<Variable, Integer>();
			for (int i = 0; i < variables.length; i++) {
				posVariables.put(variables[i], i);
			}
		} finally {
			lock.unlock();
		}
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
			if (i++ != posVariables.get(variable)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public BindingsArray clone() {
		final BindingsArray other = new BindingsArray();
		other.cloneLiterals(getLiterals());
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
		final Integer i = posVariables.get(var);
		if (i != null)
			literals[i] = literal;
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
		final Integer i = posVariables.get(var);
		if (i != null)
			return literals[i];
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
		for (final Variable var : posVariables.keySet()) {
			if (get(var) != null)
				hs.add(var);
		}
		return hs;
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
			literals = new Literal[posVariables.size()];
		} finally {
			lock.unlock();
		}
	}
}