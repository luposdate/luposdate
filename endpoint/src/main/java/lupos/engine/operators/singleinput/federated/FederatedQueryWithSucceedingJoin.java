package lupos.engine.operators.singleinput.federated;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.Client;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.sparql1_1.Node;

/**
 * For all those approaches, which must join the result of the sparql endpoint afterwards with the original queryresult (computed from the local data (and/or other service calls)) 
 */
public abstract class FederatedQueryWithSucceedingJoin extends FederatedQuery {

	public FederatedQueryWithSucceedingJoin(Node federatedQuery) {
		super(federatedQuery);
	}
	
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		this.intersectionVariables = new HashSet<Variable>();
		this.unionVariables = new HashSet<Variable>();
		this.checkVariables(this.federatedQuery, this.unionVariables);
		this.intersectionVariables.addAll(this.unionVariables);
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().addAll(this.unionVariables);
		return result;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		return FederatedQueryWithSucceedingJoin.process(bindings, this.endpoint, toStringQuery(bindings));
	}
	
	public static QueryResult process(final QueryResult bindings, final Item endpoint, final String fQuery){
		if (!endpoint.isVariable()) {
			try {
				return Client.submitQuery(((URILiteral)endpoint).getString(), fQuery);
			} catch (IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		} else {
			// service call with variable
			// it could be that several endpoints are asked, which we succeedingly ask for results...
			// the same endpoints are not asked again!
			return QueryResult.createInstance(new ParallelIterator<Bindings>(){
				
				private Iterator<Bindings> it = bindings.oneTimeIterator();
				private HashSet<URILiteral> alreadyAskedEndpoints = new HashSet<URILiteral>();
				private Iterator<Bindings> currentIteratorQueryResult = this.getNextIteratorQueryResult(); 

				@Override
				public boolean hasNext() {
					if(this.currentIteratorQueryResult==null){
						return false;
					}
					while(!this.currentIteratorQueryResult.hasNext()){
						this.currentIteratorQueryResult = this.getNextIteratorQueryResult();
						if(this.currentIteratorQueryResult==null){
							return false;
						}
					}
					return true;
				}

				@Override
				public Bindings next() {
					if(this.hasNext()){
						return this.currentIteratorQueryResult.next();
					} else {
						return null;
					}
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
				
				private Iterator<Bindings> getNextIteratorQueryResult(){
					URILiteral endpointURI = this.nextEndpoint();
					if(endpointURI==null){
						return null;
					}
					String uri = endpointURI.getString();
					try {
						return FederatedQueryWithSucceedingJoin.addEndpointVariable((Variable)endpoint, endpointURI, Client.submitQuery(uri, fQuery)).oneTimeIterator();
					} catch (IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}
					return null;
				}
				
				private URILiteral nextEndpoint(){
					while(this.it.hasNext()){
						Bindings service = this.it.next();
						Literal endpointURI = service.get((Variable) endpoint);
						if (endpointURI instanceof LazyLiteral)
							endpointURI = ((LazyLiteral) endpointURI).getLiteral();
						if (endpointURI instanceof URILiteral) {
							URILiteral result = (URILiteral) endpointURI;
							if(!this.alreadyAskedEndpoints.contains(result)){ // do not ask endpoints several times!
								this.alreadyAskedEndpoints.add(result);
								return result;
							}
						} else {
							// error message or maybe ignore?
						}
					}
					return null;
				}
			});	
		}
		return null;
	}

	protected abstract String toStringQuery(QueryResult bindings);
	
	public static QueryResult addEndpointVariable(final Variable endpointVariable, final Literal endpointURI, final QueryResult resultSetToBindingsList) {
		return QueryResult.createInstance(new ParallelIterator<Bindings>(){
			
			private Iterator<Bindings> it = resultSetToBindingsList.oneTimeIterator();

			@Override
			public boolean hasNext() {
				return this.it.hasNext();
			}

			@Override
			public Bindings next() {
				Bindings bindings = this.it.next();
				if(bindings==null){
					return null;
				}
				bindings.add(endpointVariable, endpointURI);
				return bindings;
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
		});		
	}
}
