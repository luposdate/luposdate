package lupos.optimizations.logical.rules.externalontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.VariableInInferenceRule;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.AddBinding;
import lupos.engine.operators.singleinput.Filter;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
import lupos.optimizations.logical.rules.RuleEngine;
import lupos.optimizations.logical.rules.RuleEngine.FindInOperatorGraph;

public class RuleEliminateUnsatisfiableFilterAfterAdd extends Rule {
	
	private AddBinding add;

	@Override
	protected void init() {
		final AddBinding add = new AddBinding(null, null);
		final Filter filter = new Filter();

		add.setSucceedingOperator(new OperatorIDTuple(filter, -1));
		filter.setPrecedingOperator(add);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(add, "add");
		subGraphMap.put(filter, "filter");

		startNode = add;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		add = (AddBinding) mso.get("add");
		final Filter filter = (Filter) mso.get("filter");
		
		if(filter.getPrecedingOperators().size()>1)
			return false;
		
		do {
		
			Variable v=add.getVar();
			Literal l=add.getLiteral();
			
			lupos.sparql1_1.Node n = filter.getNodePointer();
			if (n.jjtGetNumChildren() > 0) {
				n = n.jjtGetChild(0);
				if (n instanceof lupos.sparql1_1.ASTEqualsNode) {
					lupos.sparql1_1.Node left = n.jjtGetChild(0);
					lupos.sparql1_1.Node right = n.jjtGetChild(1);
					if (right instanceof lupos.sparql1_1.ASTVar) {
						final lupos.sparql1_1.Node tmp = left;
						left = right;
						right = tmp;
					}
					if (left instanceof lupos.sparql1_1.ASTVar) {
						final String varname = ((lupos.sparql1_1.ASTVar) left)
								.getName();
						Variable var = new Variable(varname);
						VariableInInferenceRule varInference = new VariableInInferenceRule(varname);
	
						if (right instanceof lupos.sparql1_1.ASTQName
								|| right instanceof lupos.sparql1_1.ASTQuotedURIRef
								|| right instanceof lupos.sparql1_1.ASTFloatingPoint
								|| right instanceof lupos.sparql1_1.ASTInteger
								|| right instanceof lupos.sparql1_1.ASTStringLiteral
								|| right instanceof lupos.sparql1_1.ASTDoubleCircumflex) {
							Literal constant = LazyLiteral.getLiteral(right);
							
							if(var.equals(v) || varInference.equals(v)){
								if(!l.equals(constant))
									return true;
							}
							
						} else return false;
					} else return false;
				} else return false;
			} else return false;
			
			BasicOperator bo=add.getPrecedingOperators().get(0);
			if(bo instanceof AddBinding)
				add=(AddBinding)bo;
			else return false;
		
		} while(add.getPrecedingOperators().size()==1);
		
		return false;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		
		// remove addBinding
		for (BasicOperator op: add.getPrecedingOperators()) {
			op.removeSucceedingOperator(add);
		}
		
		// perform depth first search to remove filter and following operators 
		deleteSubGraph(add, deleted);
		
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
	
	private void deleteSubGraph(final BasicOperator currentOp, final Collection<BasicOperator> deleted)  {
		
		// current op was deleted from graph
		deleted.add(currentOp);
		
		// as long as there are successors, detach the first successor of current operator
		while (currentOp.getSucceedingOperators().size() > 0) {
			final BasicOperator nextOp = currentOp.getSucceedingOperators().get(0).getOperator();
			currentOp.removeSucceedingOperator(nextOp);
			nextOp.removePrecedingOperator(currentOp);
			
			if (nextOp.getPrecedingOperators().size() == 0) {
				
				// walk the graph recursively if no predecessors are left
				deleteSubGraph(nextOp, deleted);
			} else if (nextOp instanceof Join) {
				
				// nextOp is a join: check if all remaining predecessors have same operand id
				int opId = -1;
				for (BasicOperator pred: nextOp.getPrecedingOperators()) {
					for (OperatorIDTuple tuple: pred.getSucceedingOperators()) {
						if (tuple.getOperator() == nextOp) {
							if (opId == -1) {
								opId = tuple.getId();
							} else if (opId != tuple.getId()) {
								return;
							}
						}
					}
				}
				
				// all predecessors have same id: delete the join
				for (BasicOperator pred: nextOp.getPrecedingOperators()) {
					pred.removeSucceedingOperator(nextOp);
					pred.addSucceedingOperators(nextOp.getSucceedingOperators());
					for (OperatorIDTuple tuple: nextOp.getSucceedingOperators()) {
						tuple.getOperator().addPrecedingOperator(pred);
					}
				}
				for (OperatorIDTuple tuple: nextOp.getSucceedingOperators()) {
					tuple.getOperator().removePrecedingOperator(nextOp);
				}
				deleted.add(nextOp);
			}
		}
		
	}
}
