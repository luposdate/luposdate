%s

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;

%s

public class %s extends Rule {
%s

    public %s() {
        this.startOpClass = %s.class;
        this.ruleName = "%s";
    }

    protected boolean check(BasicOperator _op) {
%s
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
%s
    }
}