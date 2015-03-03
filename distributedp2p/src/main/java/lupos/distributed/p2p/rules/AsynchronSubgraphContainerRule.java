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
package lupos.distributed.p2p.rules;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import lupos.distributed.operator.AsynchronSubgraphContainer;
import lupos.distributed.operator.SubgraphContainer;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Root;

/**
 * This is a {@link lupos.optimizations.logical.rules.generated.runtime.Rule},
 * that replaces all {@link lupos.distributed.operator.SubgraphContainer} with the asynchron working
 * {@link lupos.distributed.operator.AsynchronSubgraphContainer}. Note: This class replaces the
 * {@link lupos.distributed.operator.SubgraphContainer} which are already packed into another
 * {@link lupos.distributed.operator.SubgraphContainer}, too.
 *
 * @author Bjoern
 * @version $Id: $Id
 */
public class AsynchronSubgraphContainerRule extends
		lupos.optimizations.logical.rules.generated.runtime.Rule {
	
	private BasicOperator subgraph;
	private Root globalRoot;
	private Logger log = Logger.getLogger(getClass());

	/**
	 * Creates an instance of that rule
	 */
	public AsynchronSubgraphContainerRule() {
		this.startOpClass = lupos.distributed.operator.SubgraphContainer.class;
		this.ruleName = "ReplaceSubgraphContainerWithAsynchronSubgraphContainer";
	}
	
	/** {@inheritDoc} */
	@Override
	protected boolean check(BasicOperator _rootOp) {
		/*
		 * store the root, if it is given the first time (necessary for subgraph container
		 * that don't have a preceding operator)
		 */
		if (_rootOp instanceof Root && globalRoot == null)
			globalRoot = (Root) _rootOp;
		/*
		 * check whether it is an SubgraphContainer that is not replaced yet!
		 */
		this.subgraph = _rootOp;
		return (_rootOp != null && _rootOp instanceof SubgraphContainer && _rootOp
				.getClass() != AsynchronSubgraphContainer.class);
	}

	/** {@inheritDoc} */
	@Override
	protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
		/*
		 * for better processing, rename the container
		 */
		SubgraphContainer<?> sg = (SubgraphContainer<?>) this.subgraph;
		SubgraphContainer<?> newSG = AsynchronSubgraphContainer.cloneFrom(sg);
		
		log.debug(String.format("Rule %s: Replacing %s to %s",this.ruleName,sg,newSG));

		/*
		 * if the old subgraph container has no parent, maybe the root is its
		 * parent?
		 */
		if (sg.getPrecedingOperators().size() == 0) {
			for (OperatorIDTuple op : globalRoot.getSucceedingOperators()) {
				if (op.getOperator().equals(sg)) {
					/*
					 * we have found our root as parent, so temporarily add it!
					 */
					sg.addPrecedingOperator(globalRoot);
					break;
				}
			}
		}
		/*
		 * get the preceding operator's of the SG ( ? -> SG and replace with ?
		 * -> NEW SG)
		 */
		for (BasicOperator bo : sg.getPrecedingOperators()) {
			bo.removeSucceedingOperator(sg);
			bo.addSucceedingOperator(newSG);
			newSG.addPrecedingOperator(bo);
		}
		/*
		 * now remove all preceding's of the old SG
		 */
		for (BasicOperator bo : sg.getPrecedingOperators()) {
			sg.removePrecedingOperator(bo);
		}
		/*
		 * get the list of succeeding operator's from the old SG ( SG->? and
		 * replace with NEW SG -> ?)
		 */
		for (OperatorIDTuple op : sg.getSucceedingOperators()) {
			op.getOperator().removePrecedingOperator(sg);
			newSG.addSucceedingOperator(op);
			op.getOperator().addPrecedingOperator(newSG);
		}
		/*
		 * now remove all succeeding's of the old SG
		 */
		for (OperatorIDTuple op : sg.getSucceedingOperators()) {
			sg.removeSucceedingOperator(op);
		}

		sg.removeFromOperatorGraph();
		
		// now check the internal subgraph container ....
		AsynchronSubgraphContainerRule rule = new AsynchronSubgraphContainerRule();
		// ... until there is no SubgraphContainer left, that is not replaced!
		while (rule.apply(newSG.getRootOfSubgraph())) {
		};
	}
}
