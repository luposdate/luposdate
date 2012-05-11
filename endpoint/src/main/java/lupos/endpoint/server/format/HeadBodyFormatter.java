package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

public abstract class HeadBodyFormatter extends Formatter {
	
	public HeadBodyFormatter(final String formatName){
		super(formatName);
	}
	
	public HeadBodyFormatter(final String formatName, final String key){
		super(formatName, key);
	}
	
	public abstract void writeBooleanResult(final OutputStream os, final boolean result) throws IOException;
	
	public abstract void writeStartHead(final OutputStream os) throws IOException;
	
	public void writeFirstVariableInHead(final OutputStream os, final Variable v) throws IOException{
		this.writeVariableInHead(os, v);
	}

	public abstract void writeVariableInHead(final OutputStream os, final Variable v) throws IOException;
	
	public abstract void writeEndHead(final OutputStream os) throws IOException;
	
	public void writeFirstStartResult(final OutputStream os) throws IOException{
		this.writeStartResult(os);
	}
	
	public abstract void writeStartResult(final OutputStream os) throws IOException;

	public abstract void writeEndResult(final OutputStream os) throws IOException;
	
	public abstract void writeEpilogue(final OutputStream os) throws IOException;
	
	public void writeFirstStartBinding(final OutputStream os, final Variable v) throws IOException{
		this.writeStartBinding(os, v);
	}
	
	public abstract void writeStartBinding(final OutputStream os, final Variable v) throws IOException;
	
	public abstract void writeEndBinding(final OutputStream os) throws IOException;
	
	public abstract void writeBlankNode(final OutputStream os, AnonymousLiteral blankNode) throws IOException;
	
	public abstract void writeURI(final OutputStream os, URILiteral uri) throws IOException;
	
	public abstract void writeSimpleLiteral(final OutputStream os, Literal literal) throws IOException;
	
	public abstract void writeTypedLiteral(final OutputStream os, TypedLiteral literal) throws IOException;
	
	public abstract void writeLanguageTaggedLiteral(final OutputStream os, LanguageTaggedLiteral literal) throws IOException;
	
	public Collection<Variable> getVariablesToIterateOnForOneBindings(Set<Variable> variables, Bindings bindings){
		return bindings.getVariableSet();
	}

	@Override
	public void writeResult(OutputStream os, Set<Variable> variables, QueryResult queryResult) throws IOException {
		if(queryResult instanceof GraphResult){
			// TODO RDF/XML, Turtle...
		} else if(queryResult instanceof BooleanResult){
			this.writeBooleanResult(os, ((BooleanResult) queryResult).isTrue());
		} else {
			this.writeStartHead(os);
			boolean firstTime = true;
			for(Variable v: variables){
				if(firstTime){
					this.writeFirstVariableInHead(os, v);
					firstTime = false;
				} else {
					this.writeVariableInHead(os, v);
				}
			}
			this.writeEndHead(os);
			firstTime = true;
			Iterator<Bindings> it = queryResult.oneTimeIterator();
			while(it.hasNext()){
				if(firstTime){
					this.writeFirstStartResult(os);
					firstTime = false;
				} else {
					this.writeStartResult(os);
				}
				Bindings bindings = it.next();
				boolean firstTimeBinding = true;
				for(Variable v: this.getVariablesToIterateOnForOneBindings(variables, bindings)){
					if(firstTimeBinding){
						this.writeFirstStartBinding(os, v);
						firstTimeBinding = false;
					} else {
						this.writeStartBinding(os, v);
					}
					
					Literal l=bindings.get(v);					
					if(l!=null){
						if(l instanceof LazyLiteral){
							l=((LazyLiteral)l).getLiteral();
						}

						if (l.isBlank()){
							this.writeBlankNode(os, (AnonymousLiteral) l);
						} else if(l.isURI()){
							this.writeURI(os, (URILiteral) l);
						} else {
							// literal => <literal>
							if(l instanceof TypedLiteral){
								this.writeTypedLiteral(os, (TypedLiteral)l);
							} else 
								if(l instanceof LanguageTaggedLiteral){
									this.writeLanguageTaggedLiteral(os, (LanguageTaggedLiteral)l);
								} else {
									this.writeSimpleLiteral(os, l);
								}
						}
					}
					this.writeEndBinding(os);
				}
				this.writeEndResult(os);
			}
			this.writeEpilogue(os);		
		}
	}

}
