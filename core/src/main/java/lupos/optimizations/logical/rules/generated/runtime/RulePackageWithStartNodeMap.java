package lupos.optimizations.logical.rules.generated.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.rdf.Prefix;

public class RulePackageWithStartNodeMap extends AbstractRulePackage {
	private RulePackageWithStartNodeMap that = this;
	private HashMap<Class<?>, HashSet<BasicOperator>> startNodes = new HashMap<Class<?>, HashSet<BasicOperator>>();

	public void applyRules(BasicOperator rootOp) {
		rootOp.visit(new SimpleOperatorGraphVisitor() {
			private static final long serialVersionUID = 8365441598651188658L;

			public Object visit(BasicOperator op) {
				that.classifyNode(op);

				return null;
			}
		});

		while(true) {
			boolean end = true;

			for(Rule rule : this.rules) {
				if(rule.apply(this.startNodes)) {
					end = false;

					break;
				}
			}

			if(end) {
				break;
			}
		}
	}

	public List<DebugContainer<BasicOperatorByteArray>> applyRulesDebugByteArray(BasicOperator rootOp, Prefix prefixInstance) {
		List<DebugContainer<BasicOperatorByteArray>> debug = new LinkedList<DebugContainer<BasicOperatorByteArray>>();

		rootOp.visit(new SimpleOperatorGraphVisitor() {
			private static final long serialVersionUID = 8365441598651188658L;

			public Object visit(BasicOperator op) {
				that.classifyNode(op);

				return null;
			}
		});

		while(true) {
			boolean end = true;

			for(Rule rule : this.rules) {
				if(rule.apply(this.startNodes)) {
					debug.add(new DebugContainer<BasicOperatorByteArray>(rule.toString(), rule.getClass().getSimpleName().replace(" ", "").toLowerCase() + "Rule", BasicOperatorByteArray.getBasicOperatorByteArray(rootOp.deepClone(), prefixInstance)));

					end = false;

					break;
				}
			}
			if(end) {
				break;
			}
		}

		return debug;
	}

	private void classifyNode(BasicOperator op) {
		Class<? extends BasicOperator> clazz = op.getClass();

		HashSet<BasicOperator> set = this.getStartNodesSet(clazz);
		set.add(op);


		Class<?> superClazz = clazz.getSuperclass();

		while(superClazz != Object.class) {
			set = this.getStartNodesSet(superClazz);
			set.add(op);

			superClazz = superClazz.getSuperclass();
		}
	}

	private HashSet<BasicOperator> getStartNodesSet(Class<?> clazz) {
		HashSet<BasicOperator> set = this.startNodes.get(clazz);

		if(set == null) {
			set = new HashSet<BasicOperator>();

			this.startNodes.put(clazz, set);
		}

		return set;
	}
}