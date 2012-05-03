package lupos.rif.visitor;

import java.util.ArrayList;
import java.util.HashSet;

import lupos.rif.IRuleNode;
import lupos.rif.RIFException;
import lupos.rif.SimpleRuleVisitor;
import lupos.rif.model.Document;
import lupos.rif.model.Rule;

public class RuleFilteringVisitor extends SimpleRuleVisitor {

	@Override
	public IRuleNode visit(Document obj, IRuleNode arg) throws RIFException {
		for (final Rule rule : new ArrayList<Rule>(obj.getRules()))
			if (rule.isImplication() && obj.getConclusion() != null) {
				// Falls Regel nicht gebraucht wird zur Auswertung der
				// Conclusion, dann Ÿberspringen
				// Annahme, Conclusion ist nur ein RulePredicate
				if (!rule.containsRecursion(obj.getConclusion(),
						new HashSet<Rule>()))
					obj.getRules().remove(rule);
			}
		return obj;
	}

}
