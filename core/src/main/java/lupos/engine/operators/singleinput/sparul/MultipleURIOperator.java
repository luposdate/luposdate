package lupos.engine.operators.singleinput.sparul;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.operators.singleinput.SingleInputOperator;

public abstract class MultipleURIOperator extends SingleInputOperator {
	protected Collection<URILiteral> cu;
	
	public MultipleURIOperator(){		
	}
	
	public MultipleURIOperator(Collection<URILiteral> cu){
		super();
		this.cu=cu;
	}
	
	public Collection<URILiteral> getURIs(){
		return cu;
	}

	public void setURIs(Collection<URILiteral> cu){
		this.cu=cu;
	}
	
	public void setURI(final URILiteral uri){
		this.cu=new LinkedList<URILiteral>();
		this.cu.add(uri);
	}

	public String toString(){
		String result = super.toString();
		if(cu!=null && cu.size()>0){
			result+=" "+cu;
		}
		return result;
	}

}
