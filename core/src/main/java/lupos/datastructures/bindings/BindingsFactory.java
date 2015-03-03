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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lupos.datastructures.items.Variable;

/**
 * This is a factory class to create new instances of Bindings
 *
 * @author groppe
 * @version $Id: $Id
 */
public class BindingsFactory {

	/** The hashmap storing the variables + position in literals for BindinsArray class */
	protected Map<Variable, Integer> posVariables = null;

	/**
	 * <p>Constructor for BindingsFactory.</p>
	 */
	protected BindingsFactory(){
	}

	/**
	 * <p>Constructor for BindingsFactory.</p>
	 *
	 * @param variables a {@link java.util.Collection} object.
	 */
	protected BindingsFactory(final Collection<Variable> variables){
		this.setVariables(variables);
	}

	/**
	 * <p>Constructor for BindingsFactory.</p>
	 *
	 * @param variables an array of {@link lupos.datastructures.items.Variable} objects.
	 */
	protected BindingsFactory(final Variable[] variables){
		this.setVariables(variables);
	}

	/**
	 * <p>Constructor for BindingsFactory.</p>
	 *
	 * @param posVariables a {@link java.util.Map} object.
	 */
	protected BindingsFactory(final Map<Variable, Integer> posVariables){
		this.setPosVariables(posVariables);
	}

	/**
	 * <p>createBindingsFactory.</p>
	 *
	 * @return a {@link lupos.datastructures.bindings.BindingsFactory} object.
	 */
	public static BindingsFactory createBindingsFactory(){
		if(Bindings.instanceClass==BindingsArray.class){
			return new ArrayBindingsFactory();
		} else if(Bindings.instanceClass==BindingsArrayReadTriples.class){
			return new ArrayReadTriplesBindingsFactory();
		} else if(Bindings.instanceClass==BindingsArrayVarMinMax.class){
			return new ArrayVarMinMaxBindingsFactory();
		} else if(Bindings.instanceClass==BindingsArrayPresortingNumbers.class){
			return new ArrayPresortingNumbersBindingsFactory();
		} else {
			return new BindingsFactory();
		}
	}

	/**
	 * <p>createBindingsFactory.</p>
	 *
	 * @param variables a {@link java.util.Collection} object.
	 * @return a {@link lupos.datastructures.bindings.BindingsFactory} object.
	 */
	public static BindingsFactory createBindingsFactory(final Collection<Variable> variables){
		if(Bindings.instanceClass==BindingsArray.class){
			return new ArrayBindingsFactory(variables);
		} else if(Bindings.instanceClass==BindingsArrayReadTriples.class){
			return new ArrayReadTriplesBindingsFactory(variables);
		} else if(Bindings.instanceClass==BindingsArrayVarMinMax.class){
			return new ArrayVarMinMaxBindingsFactory(variables);
		} else if(Bindings.instanceClass==BindingsArrayPresortingNumbers.class){
			return new ArrayPresortingNumbersBindingsFactory(variables);
		} else {
			return new BindingsFactory(variables);
		}
	}

	/**
	 * <p>createBindingsFactory.</p>
	 *
	 * @param variables an array of {@link lupos.datastructures.items.Variable} objects.
	 * @return a {@link lupos.datastructures.bindings.BindingsFactory} object.
	 */
	public static BindingsFactory createBindingsFactory(final Variable[] variables){
		if(Bindings.instanceClass==BindingsArray.class){
			return new ArrayBindingsFactory(variables);
		} else if(Bindings.instanceClass==BindingsArrayReadTriples.class){
			return new ArrayReadTriplesBindingsFactory(variables);
		} else if(Bindings.instanceClass==BindingsArrayVarMinMax.class){
			return new ArrayVarMinMaxBindingsFactory(variables);
		} else if(Bindings.instanceClass==BindingsArrayPresortingNumbers.class){
			return new ArrayPresortingNumbersBindingsFactory(variables);
		} else {
			return new BindingsFactory(variables);
		}
	}

	/**
	 * <p>createBindingsFactory.</p>
	 *
	 * @param posVariables a {@link java.util.Map} object.
	 * @return a {@link lupos.datastructures.bindings.BindingsFactory} object.
	 */
	public static BindingsFactory createBindingsFactory(final Map<Variable, Integer> posVariables){
		if(Bindings.instanceClass==BindingsArray.class){
			return new ArrayBindingsFactory(posVariables);
		} else if(Bindings.instanceClass==BindingsArrayReadTriples.class){
			return new ArrayReadTriplesBindingsFactory(posVariables);
		} else if(Bindings.instanceClass==BindingsArrayVarMinMax.class){
			return new ArrayVarMinMaxBindingsFactory(posVariables);
		} else if(Bindings.instanceClass==BindingsArrayPresortingNumbers.class){
			return new ArrayPresortingNumbersBindingsFactory(posVariables);
		} else {
			return new BindingsFactory(posVariables);
		}
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @return a {@link lupos.datastructures.bindings.Bindings} object.
	 */
	public Bindings createInstance(){
		try {
			return Bindings.instanceClass.newInstance();
		} catch (final InstantiationException e) {
			System.err.println(e);
		} catch (final IllegalAccessException e) {
			System.err.println(e);
		}
		return new BindingsMap();
	}

	/**
	 * <p>setVariables.</p>
	 *
	 * @param variables an array of {@link lupos.datastructures.items.Variable} objects.
	 */
	public void setVariables(final Variable[] variables){
		this.posVariables = new HashMap<Variable, Integer>();
		for (int i = 0; i < variables.length; i++) {
			this.posVariables.put(variables[i], i);
		}
	}

	/**
	 * <p>setVariables.</p>
	 *
	 * @param variables a {@link java.util.Collection} object.
	 */
	public void setVariables(final Collection<Variable> variables){
		this.posVariables = new HashMap<Variable, Integer>();
		final Iterator<Variable> varIt = variables.iterator();
		for (int i = 0; i < variables.size(); i++) {
			this.posVariables.put(varIt.next(), i);
		}
	}

	/**
	 * <p>Setter for the field <code>posVariables</code>.</p>
	 *
	 * @param posVariables a {@link java.util.Map} object.
	 */
	public void setPosVariables(final Map<Variable, Integer> posVariables){
		this.posVariables = posVariables;
	}

	/**
	 * <p>Getter for the field <code>posVariables</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<Variable, Integer> getPosVariables(){
		return this.posVariables;
	}

	/**
	 * <p>getVariables.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Variable> getVariables(){
		return this.posVariables.keySet();
	}

	/**
	 * <p>getVariablesInOrder.</p>
	 *
	 * @return an array of {@link lupos.datastructures.items.Variable} objects.
	 */
	public Variable[] getVariablesInOrder(){
		final Variable[] vars = new Variable[this.posVariables.size()];
		for(final Entry<Variable, Integer> entry: this.posVariables.entrySet()){
			vars[entry.getValue()] = entry.getKey();
		}
		return vars;
	}
}
