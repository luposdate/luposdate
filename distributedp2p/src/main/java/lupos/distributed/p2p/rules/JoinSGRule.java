/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.distributed.p2p.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import lupos.distributed.operator.ISubgraphExecutor;
import lupos.distributed.operator.SubgraphContainer;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.rules.generated.runtime.Rule;

import org.apache.log4j.Logger;
import org.json.JSONException;


/**
 * This is a {@link Rule} that optimizes joins, where subgraphs with the same keys, are only asked once via
 * a new subgraph request, where both information is put into.
 * 
 * @author Bjoern
 *
 */
public class JoinSGRule extends Rule {

	private lupos.engine.operators.multiinput.Union u2 = null;
	private lupos.engine.operators.multiinput.Union u1 = null;
	@SuppressWarnings("rawtypes")
	private lupos.distributed.operator.SubgraphContainer sg1 = null;
	@SuppressWarnings("rawtypes")
	private lupos.distributed.operator.SubgraphContainer sg2 = null;
	private lupos.engine.operators.multiinput.join.Join join = null;
	private Object keySg1 = null;
	private Object keySg2 = null;
	private Logger log = Logger.getLogger(getClass());

	/*
	 * Searching for: SG1 (key A) ------ Union (-> Join) -------- SG2 (key A)
	 * Result would end in: SG1 (key A) with TriplePattern of Join of SG1 AND SG2 
	 */
	private boolean _checkPrivate0(BasicOperator _op) {
		/*
		 * Only process on SubgraphContainer
		 */
		if (_op.getClass() != lupos.distributed.operator.SubgraphContainer.class) {
			return false;
		}
		/* store the first subgraphContainer and its distribution key */
		this.sg1 = (lupos.distributed.operator.SubgraphContainer<?>) _op;
		this.keySg1 = this.sg1.getKey();

		/*
		 * now search for an "union" operator
		 */
		List<OperatorIDTuple> _succOfFirstSGContainer = _op
				.getSucceedingOperators();
		for (OperatorIDTuple _eachSucceedingOfFirstSGContainer : _succOfFirstSGContainer) {
			if (!(_eachSucceedingOfFirstSGContainer.getOperator() instanceof lupos.engine.operators.multiinput.Union)) {
				continue;
			}
			/*
			 * store "union"
			 */
			this.u1 = (lupos.engine.operators.multiinput.Union) _eachSucceedingOfFirstSGContainer
					.getOperator();
			/*
			 * get succeeding operators of this "union" and search for a "join"
			 */
			List<OperatorIDTuple> _succeedingsOfUnion = new LinkedList<>();
			_succeedingsOfUnion.addAll(this.u1.getSucceedingOperators());
			/*
			 * we iterate throug a ListIterator because , if this "level" of the operator graph
			 * has no join found, the next "level" will be added to this iterator!
			 */
			ListIterator<OperatorIDTuple> _succeedingsOfUnionIt = _succeedingsOfUnion.listIterator();
			while (true) {
				if (!_succeedingsOfUnionIt.hasNext()) break;
				OperatorIDTuple _eachSucceedingsOfUnion = _succeedingsOfUnionIt.next();
				/*
				 * Search for a join as succeding of "union"
				 */
				if (!(_eachSucceedingsOfUnion.getOperator() instanceof lupos.engine.operators.multiinput.join.Join)) {
					continue;
				}
				
				
				this.join = (lupos.engine.operators.multiinput.join.Join) _eachSucceedingsOfUnion
						.getOperator();

				
				/*
				 * Now search in join's preceding operators for a new "union" operator (so ignore
				 * the same way back to the already stored union)
				 */
				List<BasicOperator> _precedingsOfJoin = this.join.getPrecedingOperators();
				for (BasicOperator _eachPrecedingsOfJoin : _precedingsOfJoin) {
					/*
					 * search for an union
					 */
					if (!(_eachPrecedingsOfJoin instanceof lupos.engine.operators.multiinput.Union)) {
						continue;
					}
					/*
					 * that is not equal to our already stored "union", because if, we walk the tree back
					 */
					if (((lupos.engine.operators.multiinput.Union) _eachPrecedingsOfJoin)
							.equals(this.u1))
						continue;

					/*
					 * store the found second union, as 2nd union
					 */
					this.u2 = (lupos.engine.operators.multiinput.Union) _eachPrecedingsOfJoin;

					/*
					 * Now search for a SubgraphContainer with the same key, as our already 
					 * stored SubgraphContainer, in the precedings of the 2nd "union"-operator.
					 */
					List<BasicOperator> _precedingOf2ndUnion = this.u2.getPrecedingOperators();
					for (BasicOperator _eachPrecedingOf2ndUnion : _precedingOf2ndUnion) {
						if (_eachPrecedingOf2ndUnion.getClass() != lupos.distributed.operator.SubgraphContainer.class) {
							continue;
						}
						/*
						 * we have to avoid, that the two SubgraphContainer are equal (so we walked back in tree)
						 */
						if (((lupos.distributed.operator.SubgraphContainer<?>) _eachPrecedingOf2ndUnion)
								.equals(this.sg1)) {
							
							continue;
						}
						/*
						 * store the key of the 2nd SubgraphContainer
						 */
						keySg2 = ((lupos.distributed.operator.SubgraphContainer<?>) _eachPrecedingOf2ndUnion)
								.getKey();
						/*
						 * now: both keys have to be same!
						 */
						if (!keySg1.equals(keySg2)) {
							/*
							 * we didn't found any useful "join" at this level of the operator graph,
							 * so we add next level to the iterator (deep-search-algorithm)
							 */
							for (OperatorIDTuple nextDeep :  _eachSucceedingsOfUnion.getOperator().getSucceedingOperators()) {
								/*
								 * add and rewind, so that this would be the next item in iterator
								 */
								_succeedingsOfUnionIt.add(nextDeep);
								_succeedingsOfUnionIt.previous();
							}
							continue;
						}
						this.sg2 = (lupos.distributed.operator.SubgraphContainer<?>) _eachPrecedingOf2ndUnion;
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Constructor of the Join SubgraphContainer rule
	 */
	public JoinSGRule() {
		this.startOpClass = lupos.distributed.operator.SubgraphContainer.class;
		this.ruleName = "Join Subgraphcontainer with same key";
	}

	@Override
	protected boolean check(BasicOperator _op) {
		return this._checkPrivate0(_op);
	}

	
	public static ISubgraphExecutor<?> subgraphExecutor;

	
	@Override
	protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
		/*
		 * Get the root of the first SubgraphContainer
		 */
		Root root = sg1.getRootOfSubgraph();
		try {
			/*
			 * get the both BasicIndexScan's
			 */
			BasicIndexScan bis1 = getIndexScan(sg1.getRootOfSubgraph());
			BasicIndexScan bis2 = getIndexScan(sg2.getRootOfSubgraph());
			if (bis1 == null | bis2==null) return;
			
			log .debug(String.format("Rule %s: Move %s into %s",this.ruleName,sg2,sg1));
			
			/*
			 * Join both TriplePattern and store them in the first SubgraphContainer
			 */
			Collection<TriplePattern> patterns = bis1.getTriplePattern();
			patterns.addAll(bis2.getTriplePattern());
			bis1.setTriplePatterns(patterns);
			/*
			 * remove the 2nd SubgraphContainer
			 */
			root.removeSucceedingOperator(sg2);
			sg2.removeFromOperatorGraph();
			Root r = bis1.getRoot();
			r.removeSucceedingOperator(sg2);
				
			//remove the second sg-container
			u2.removePrecedingOperator(sg2);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		// delete unreachable operators...
		 this.deleteOperatorWithoutParentsRecursive(this.sg2, _startNodes);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void replace2(
			HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
		// remove obsolete connections...

		// remove connection from preds
		List<BasicOperator> pred = this.sg1.getPrecedingOperators();
		pred.addAll(this.sg2.getPrecedingOperators());

		for (BasicOperator ops : pred) {
			ops.removeSucceedingOperator(sg1);
			ops.removeSucceedingOperator(sg2);
		}

		Root root = sg1.getRootOfSubgraph();
		SubgraphContainer sgc;
		try {
			sgc = new SubgraphContainer(root, keySg1, subgraphExecutor);

			/*
			 * connect subgraph preceding
			 */
			for (BasicOperator p : pred) {
				p.addSucceedingOperator(sgc);
			}

			/*
			 * connect sg2 root's precedings to sg1's root as succeding operator
			 */
			for (OperatorIDTuple t : sg2.getRootOfSubgraph()
					.getSucceedingOperators()) {
				root.addSucceedingOperator(t);
			}

			/*
			 * get the two results, remove them, and add the join
			 */
			Result r1 = getResult(root);
			for (BasicOperator p : r1.getPrecedingOperators()) {
				p.removeSucceedingOperator(r1);
				// p.addSucceedingOperator(join);
			}
			Result r2 = getResult(root);
			for (BasicOperator p : r2.getPrecedingOperators()) {
				p.removeSucceedingOperator(r2);
				// p.addSucceedingOperator(join);
			}

			for (BasicOperator p : r1.getPrecedingOperators()) {
				p.addSucceedingOperator(join);
			}
			for (BasicOperator p : r2.getPrecedingOperators()) {
				p.addSucceedingOperator(join);
			}

			/*
			 * succedings of the join will be the succedings of the subgraph
			 * container
			 */
			sgc.setSucceedingOperators(join.getSucceedingOperators());
			/*
			 * the join in the subgraph container will return the result
			 */
			join.setSucceedingOperator(new OperatorIDTuple(new Result(), 0));

			root.removeSucceedingOperator(sg1);
			root.removeSucceedingOperator(sg2);

			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Search for a Result-Operator in succeeding's list of root
	 */
	@SuppressWarnings("serial")
	private Result getResult(final BasicOperator root) {
		//We could do with SimpleOperatorGraphVisitor, too:
		SimpleOperatorGraphVisitor sov = new SimpleOperatorGraphVisitor() {
			@Override
			public Object visit(BasicOperator basicOperator) {
				if (basicOperator instanceof Result) return basicOperator;
				return null;
			}
		};
		return (Result) root.visit(sov);
		
		/*final List<OperatorIDTuple> succs = root.getSucceedingOperators();
		if (succs == null | succs.size() == 0)
			return null;
		for (final OperatorIDTuple succ : succs) {
			final BasicOperator op = succ.getOperator();
			if (op instanceof Result) {
				return (Result) op;
			} else {
				Result res = null;
				if ((res = getResult(op)) != null)
					return res;
			}
		}
		return null;*/
	}

	/*
	 * returns the first found BasicIndexScan in succeeding's list of root
	 */
	private BasicIndexScan getIndexScan(final BasicOperator root) {
		final List<OperatorIDTuple> succs = root.getSucceedingOperators();
		if (succs == null | succs.size() == 0)
			return null;
		for (final OperatorIDTuple succ : succs) {
			final BasicOperator op = succ.getOperator();
			if (op instanceof BasicIndexScan) {
				return (BasicIndexScan) op;
			} else {
				BasicIndexScan res = null;
				/*
				 * recursive call
				 */
				if ((res = getIndexScan(op)) != null)
					return res;
			}
		}
		return null;
	}
}
