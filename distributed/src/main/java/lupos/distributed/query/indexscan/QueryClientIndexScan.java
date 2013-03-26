package lupos.distributed.query.indexscan;

import java.util.Collection;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.IteratorQueryResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class QueryClientIndexScan extends BasicIndexScan {
	
	public QueryClientIndexScan(final Root root){
		super(root);
	}

	public QueryClientIndexScan(final OperatorIDTuple succeedingOperator,
			final Collection<TriplePattern> triplePatterns, final Item rdfGraph, final Root root) {
		super(succeedingOperator, triplePatterns, rdfGraph, root);
	}

	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		QueryClientIndices queryClientIndices = (QueryClientIndices) indices;
		QueryResult result = QueryResult.createInstance();
		result.add(bindings);
		
		for (TriplePattern pattern : this.triplePatterns) {
			QueryResult iResult = result;
			result = QueryResult.createInstance();
						
			for(Bindings b: iResult){
				TriplePattern tpWithReplacedVariables = this.determineTriplePatternToEvaluate(pattern, b);
				QueryResult resultOfTP = queryClientIndices.evaluateTriplePattern(tpWithReplacedVariables);
				if(resultOfTP!=null){
					result.addAll(this.addBindings(b, resultOfTP));
				}
			}
		}
		return result;
	}

	private IteratorQueryResult addBindings(final Bindings bindings, final QueryResult queryResult) {
		return new IteratorQueryResult(new Iterator<Bindings>(){

			Iterator<Bindings> it = queryResult.oneTimeIterator();
			
			@Override
			public boolean hasNext() {
				return this.it.hasNext();
			}

			@Override
			public Bindings next() {
				if(this.it.hasNext()){
					Bindings b = this.it.next().clone();
					b.addAll(bindings);
					return b;
				} else {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}			
		});
	}

	private TriplePattern determineTriplePatternToEvaluate(TriplePattern pattern, Bindings b) {		
		return new TriplePattern(getItem(pattern.getPos(0), b), getItem(pattern.getPos(1), b), getItem(pattern.getPos(2), b));
	}
	
	private Item getItem(Item item, Bindings bindings){
		if(item.isVariable()){
			Literal literal = bindings.get((Variable)item);
			if(literal!=null){
				return literal;
			}
		}
		return item;
	}
}
