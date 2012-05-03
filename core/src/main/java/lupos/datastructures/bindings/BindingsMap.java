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
		hashMap=new HashMap<Variable, Literal>();
	}
	
	public BindingsMap(){
	}

	@Override
	@SuppressWarnings("unchecked")
	public Bindings clone()
	{
		final BindingsMap bnew=new BindingsMap();
		bnew.hashMap=(HashMap<Variable, Literal>)hashMap.clone();
		return bnew;
	}

	@Override
	public void add(final Variable var, final Literal lit)
	{
		hashMap.put(var, lit);
	}

	@Override
	public Literal get(final Variable var)
	{
		return hashMap.get(var);
	}

	/**
	 * Returns the set of bound variables
	 * @return the set of bound variables
	 */
	@Override
	public Set<Variable> getVariableSet(){
		final Set<Variable> keySet=new HashSet<Variable>();
		for(final Variable var: hashMap.keySet())
			if(hashMap.get(var)!=null) keySet.add(var);
		return keySet;
	}

}