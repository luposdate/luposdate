/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.optimizations.logical.rules.generated.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;

public abstract class Rule {
	private Rule that = this;
	protected Class<?> startOpClass;
	protected String ruleName = this.getClass().getSimpleName();

	protected abstract boolean check(BasicOperator _rootOp);
	protected abstract void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes);

	public String toString() {
		return this.ruleName;
	}

	public boolean apply(BasicOperator rootOp) {
		// System.out.println("applying rule '" + this.getClass().getSimpleName() + "'...");

		Object result = rootOp.visitAndStop(new SimpleOperatorGraphVisitor() {
			private static final long serialVersionUID = 8365441598651188658L;

			public Object visit(BasicOperator op) {
				if(that.check(op)) {
					that.replace(null);

					return new Boolean(true);
				}

				return null;
			}
		});

		return result != null;
	}

	public boolean apply(final HashMap<Class<?>, HashSet<BasicOperator>> startNodes) {
		// System.out.println("applying rule '" + this.getClass().getSimpleName() + "'...");

		HashSet<BasicOperator> ruleStartNodes = startNodes.get(this.startOpClass);

		if(ruleStartNodes != null) {
			for(BasicOperator startNode : ruleStartNodes) {
				if(this.check(startNode)) {
					this.replace(startNodes);

					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Method to delete an operator and its children if it has no preceding operators.
	 * Also update the start nodes map if it is present.
	 * 
	 * @param op
	 */
	@SuppressWarnings("unchecked")
	protected void deleteOperatorWithoutParentsRecursive(BasicOperator op, HashMap<Class<?>, HashSet<BasicOperator>> startNodes) {
		// operator has still preceding operator => don't do anything
		if(op.getPrecedingOperators().size() > 0) {
			return;
		}

		// get children list and clone it once...
		LinkedList<OperatorIDTuple> succeedingOperators = (LinkedList<OperatorIDTuple>) op.getSucceedingOperators();
		LinkedList<OperatorIDTuple> clonedSucceedingOperators = (LinkedList<OperatorIDTuple>) succeedingOperators.clone();

		// remove this operator from its children...
		for(OperatorIDTuple opIDT : clonedSucceedingOperators) {
			op.removeSucceedingOperator(opIDT);
			opIDT.getOperator().removePrecedingOperator(op);
		}

		if(startNodes != null) {
			// update start node map...
			this.deleteNodeFromStartNodeMap(op, startNodes);
		}

		// call this method recursively for all children of this operator...
		for(OperatorIDTuple opIDT : clonedSucceedingOperators) {
			this.deleteOperatorWithoutParentsRecursive(opIDT.getOperator(), startNodes);
		}
	}

	/**
	 * This method deletes the path in the operator graph in which the operator op is...
	 * @param op
	 * @param startNodes
	 */
	protected void deleteOperatorWithParentsAndChildren(BasicOperator op, HashMap<Class<?>, HashSet<BasicOperator>> startNodes){
		this.deletePrecedingOperators(op, startNodes);
		
		op.getPrecedingOperators().clear();
		deleteOperatorWithoutParentsRecursive(op, startNodes);
	}
	
	/**
	 * This method is used within deleteOperatorWithParentsAndChildren(...)
	 */
	private void deletePrecedingOperators(BasicOperator op, HashMap<Class<?>, HashSet<BasicOperator>> startNodes){
		if(op.getPrecedingOperators().size()==0){
			return;
		}
		
		for(BasicOperator prec: new HashSet<BasicOperator>(op.getPrecedingOperators())){
			if(prec.getSucceedingOperators().size()<=1){
				deletePrecedingOperators(prec, startNodes);
				
				if(startNodes != null) {
					// update start node map...
					this.deleteNodeFromStartNodeMap(prec, startNodes);
				}
			}
			op.removePrecedingOperator(prec);
			prec.removeSucceedingOperator(op);
		}		
	}

	protected void deleteNodeFromStartNodeMapNullCheck(BasicOperator op, HashMap<Class<?>, HashSet<BasicOperator>> startNodes) {
		if(startNodes != null) {
			this.deleteNodeFromStartNodeMap(op, startNodes);
		}
	}

	protected void deleteNodeFromStartNodeMap(BasicOperator op, HashMap<Class<?>, HashSet<BasicOperator>> startNodes) {
		Class<?> clazz = op.getClass();

		while(clazz != Object.class) {
			startNodes.get(clazz).remove(op);

			clazz = clazz.getSuperclass();
		}
	}

	protected void addNodeToStartNodeMapNullCheck(BasicOperator op, HashMap<Class<?>, HashSet<BasicOperator>> startNodes) {
		if(startNodes != null) {
			this.addNodeToStartNodeMap(op, startNodes);
		}
	}

	protected void addNodeToStartNodeMap(BasicOperator op, HashMap<Class<?>, HashSet<BasicOperator>> startNodes) {
		Class<?> clazz = op.getClass();

		while(clazz != Object.class) {
			HashSet<BasicOperator> list = startNodes.get(clazz);

			if(list == null) {
				list = new HashSet<BasicOperator>();

				startNodes.put(clazz, list);
			}

			list.add(op);

			clazz = clazz.getSuperclass();
		}
	}

	protected void deleteOperator(BasicOperator op, HashMap<Class<?>, HashSet<BasicOperator>> startNodes) {
		op.removeFromOperatorGraph();

		this.deleteNodeFromStartNodeMapNullCheck(op, startNodes);
	}
}