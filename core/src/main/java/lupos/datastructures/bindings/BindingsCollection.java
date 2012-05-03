package lupos.datastructures.bindings;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.rdf.Prefix;



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