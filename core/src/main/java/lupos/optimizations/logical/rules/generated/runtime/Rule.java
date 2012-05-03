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