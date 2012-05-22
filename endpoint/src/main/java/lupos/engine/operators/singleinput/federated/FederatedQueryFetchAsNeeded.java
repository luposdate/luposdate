package lupos.engine.operators.singleinput.federated;

import java.io.IOException;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.Client;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;

public class FederatedQueryFetchAsNeeded extends FederatedQueryWithoutSucceedingJoin {

	public FederatedQueryFetchAsNeeded(Node federatedQuery) {
		super(federatedQuery);
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		return QueryResult.createInstance(new Iterator<Bindings>(){

			private Iterator<Bindings> bindingsIterator = bindings.iterator();
			private Iterator<Bindings> currentIteratorQueryResult = nextIteratorQueryResult();
			
			@Override
			public boolean hasNext() {
				if(this.currentIteratorQueryResult==null){
					return false;
				}
				while(!this.currentIteratorQueryResult.hasNext()){
					this.currentIteratorQueryResult = nextIteratorQueryResult();
					if(this.currentIteratorQueryResult==null){
						return false;
					}
				}
				return true;					
			}

			@Override
			public Bindings next() {
				if(hasNext()){
					return this.currentIteratorQueryResult.next();
				} else {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			private Iterator<Bindings> nextIteratorQueryResult(){
				try {
					if(!this.bindingsIterator.hasNext()){
						return null;
					}
					Bindings bindingsTemp = this.bindingsIterator.next();
					final String fQuery = toStringQuery(bindingsTemp);
					if (!FederatedQueryFetchAsNeeded.this.endpoint.isVariable()) {
						return new IteratorQueryResultAndOneBindings(Client.submitQuery(((URILiteral)FederatedQueryFetchAsNeeded.this.endpoint).getString(), fQuery), bindingsTemp);
					} else {
						Literal endpointURI = bindingsTemp.get((Variable) FederatedQueryFetchAsNeeded.this.endpoint);
						if (endpointURI instanceof LazyLiteral)
							endpointURI = ((LazyLiteral) endpointURI).getLiteral();
						if (endpointURI instanceof URILiteral) {
							return new IteratorQueryResultAndOneBindings(Client.submitQuery(((URILiteral) endpointURI).getString(), fQuery), bindingsTemp);
						} else {
							// ignore or error message?
						}
					}
				} catch(IOException e){
					System.err.println(e);
					e.printStackTrace();
				}
				// in case of error try next one:
				return nextIteratorQueryResult();
			}
		});	
	}

	public String toStringQuery(final Bindings bindings) {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper() {
			@Override
			public String visit(final ASTVar node) {
				Variable var = new Variable(node.getName());
				if ((bindings.get(var) != null)) {
					return bindings.get(var).toString();
				} else
					return "?" + node.getName();
			}
		};

		return "SELECT * " + this.federatedQuery.jjtGetChild(1).accept(dumper);
	}
	
	public class IteratorQueryResultAndOneBindings implements ParallelIterator<Bindings>{
		
		private final Iterator<Bindings> it;
		private final Bindings bindings;
		
		public IteratorQueryResultAndOneBindings(final QueryResult queryResult, final Bindings bindings){
			this.it = queryResult.oneTimeIterator();
			this.bindings = bindings;
		}

		@Override
		public boolean hasNext() {
			return this.it.hasNext();
		}

		@Override
		public Bindings next() {
			if(!hasNext()){
				return null;
			}
			Bindings result = this.it.next();
			result.addAll(this.bindings);
			return result;
		}

		@Override
		public void remove() {
			this.it.remove();
		}

		@Override
		public void close() {
			if(this.it instanceof ParallelIterator){
				((ParallelIterator<Bindings>)this.it).close();
			}
		}
		
	}
}
