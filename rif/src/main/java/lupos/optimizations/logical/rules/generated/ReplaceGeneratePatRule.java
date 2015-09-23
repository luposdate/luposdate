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
package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
public class ReplaceGeneratePatRule extends Rule {

    private lupos.engine.operators.singleinput.generate.Generate g = null;
    private lupos.engine.operators.tripleoperator.TriplePattern t = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.engine.operators.singleinput.generate.Generate.class) {
            return false;
        }

        this.g = (lupos.engine.operators.singleinput.generate.Generate) _op;

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

        if(_succedingOperators_1_0.size() != 1) {
            return false;
        }

        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(_sucOpIDTup_1_0.getOperator().getClass() != lupos.engine.operators.tripleoperator.TriplePattern.class) {
                continue;
            }

            this.t = (lupos.engine.operators.tripleoperator.TriplePattern) _sucOpIDTup_1_0.getOperator();

            return true;
        }

        return false;
    }


    /**
     * <p>Constructor for ReplaceGeneratePatRule.</p>
     */
    public ReplaceGeneratePatRule() {
        this.startOpClass = lupos.engine.operators.singleinput.generate.Generate.class;
        this.ruleName = "Replace Generate Pat";
    }

    /** {@inheritDoc} */
    protected boolean check(BasicOperator _op) {
        return this._checkPrivate0(_op);
    }

    /** {@inheritDoc} */
    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        final lupos.datastructures.items.Item[] patItems = this.t.getItems();
        		final lupos.datastructures.items.Item[] generateItems = this.g.getValueOrVariable();
        
        		lupos.engine.operators.singleinput.filter.Filter filter = null;
        		final lupos.engine.operators.singleinput.ReplaceVar replaceVar = new lupos.engine.operators.singleinput.ReplaceVar();
        		replaceVar.setIntersectionVariables(new java.util.HashSet<lupos.datastructures.items.Variable>());
        		replaceVar.setUnionVariables(replaceVar.getIntersectionVariables());
        
        		final java.util.LinkedList<lupos.datastructures.items.Variable> addBindingsVar = new java.util.LinkedList<lupos.datastructures.items.Variable>();
        		final java.util.LinkedList<lupos.datastructures.items.literal.Literal> addBindingsLit = new java.util.LinkedList<lupos.datastructures.items.literal.Literal>();
        
        		String filterConstraint = "Filter( ";
        		for (int i = 0; i < 3; i++) {
        			final lupos.datastructures.items.Item patItem = patItems[i];
        			final lupos.datastructures.items.Item generateItem = generateItems[i];
        			if ((!patItem.isVariable()) && generateItem.isVariable()) {
        				filterConstraint += generateItems[i].toString() + " = "
        						+ patItems[i].toString() + " && ";
        			} else if (patItem.isVariable() && generateItem.isVariable()) {
        				replaceVar.addSubstitution((lupos.datastructures.items.Variable) patItem,
        						(lupos.datastructures.items.Variable) generateItem);
        				replaceVar.getIntersectionVariables().add((lupos.datastructures.items.Variable) patItem);
        			} else if (patItem.isVariable() && (!generateItem.isVariable())) {
        				addBindingsVar.add((lupos.datastructures.items.Variable) patItem);
        				addBindingsLit.add((lupos.datastructures.items.literal.Literal) generateItem);
        			} else if (!patItem.isVariable() && !generateItem.isVariable()
        					&& !generateItem.equals(patItem)) {
        				// cannot match, remove generate.
        				for (final BasicOperator parent : this.g.getPrecedingOperators())
        					parent.removeSucceedingOperator(this.g);
        				this.g.getPrecedingOperators().clear();
        				this.g.removeFromOperatorGraph();
        				return;
        			}
        		}
        
        		// If (?x = ?a) and (?x = ?b) then (valueOf(?a) = value(?b)) must be
        		// fulfilled
        		for (int i = 0; i < 2; i++) {
        			for (int x = i + 1; x < 3; x++) {
        				if (patItems[i].equals(patItems[x])) {
        					filterConstraint += generateItems[i].toString() + " = "
        							+ generateItems[x].toString() + " && ";
        				}
        			}
        		}
        
        		if (!filterConstraint.equals("Filter( ")) {
        			filterConstraint = filterConstraint.substring(0,
        					filterConstraint.length() - 3)
        					+ ") ";
        
        			try {
        				final lupos.sparql1_1.ASTFilterConstraint ASTfilter = (lupos.sparql1_1.ASTFilterConstraint) lupos.sparql1_1.SPARQL1_1Parser
        						.parseFilter(filterConstraint);
        				filter = new lupos.engine.operators.singleinput.filter.Filter(ASTfilter);
        			} catch (final Exception e) {
        				System.err
        						.println("This should never happen in RuleReplaceGenPat!");
        				System.err.println(e);
        				e.printStackTrace();
        			}
        		}
        
        		// Only Operators with a not empty definition are put into the
        		// operatorgraph
        		final java.util.LinkedList<BasicOperator> order = new java.util.LinkedList<BasicOperator>();
        		if (filter != null) {
        			order.add(filter);
        		}
        
        		final int substVar = replaceVar.getSubstitutionsVariableLeft().size();
        
        		if (substVar > 0) {
        			order.add(replaceVar);
        		} else {
        			final lupos.engine.operators.singleinput.Projection p = new lupos.engine.operators.singleinput.Projection();
        			p.setIntersectionVariables(new java.util.HashSet<lupos.datastructures.items.Variable>());
        			p.setUnionVariables(p.getUnionVariables());
        			order.add(p);
        		}
        		if (addBindingsVar.size() > 0) {
        			final java.util.Iterator<lupos.datastructures.items.literal.Literal> lit_it = addBindingsLit.iterator();
        			final java.util.HashSet<lupos.datastructures.items.Variable> hsv = new java.util.HashSet<lupos.datastructures.items.Variable>();
        			hsv.addAll(replaceVar.getUnionVariables());
        			for (final lupos.datastructures.items.Variable v : addBindingsVar) {
        				final lupos.engine.operators.singleinput.AddBinding ab = new lupos.engine.operators.singleinput.AddBinding(v, lit_it.next());
        				hsv.add(v);
        				ab.setIntersectionVariables((java.util.HashSet<lupos.datastructures.items.Variable>) hsv.clone());
        				ab.setUnionVariables(ab.getIntersectionVariables());
        				order.add(ab);
        			}
        		}
        
        		// In case that Generate or TriplePattern has minimum one variable, than
        		// minimum one operator has to be inserted
        		if (order.size() > 0) {
        			final java.util.List<BasicOperator> pres = (java.util.List<BasicOperator>) this.g.getPrecedingOperators();
        			BasicOperator pre;
        			for (int i = 0; i < pres.size(); i++) {
        				pre = pres.get(i);
        				pre.addSucceedingOperator(new OperatorIDTuple(order.getFirst(),
        						0));
        				if (filter != null) {
        					java.util.Collection<lupos.datastructures.items.Variable> vars = filter
        							.getIntersectionVariables();
        					if (vars == null)
        						vars = new java.util.HashSet<lupos.datastructures.items.Variable>();
        					vars.addAll(pre.getIntersectionVariables());
        					filter.setIntersectionVariables(vars);
        					filter.setUnionVariables(vars);
        				}
        				pre.removeSucceedingOperator(this.g);
        				order.getFirst().addPrecedingOperator(pre);
        			}
        
        			for (int i = 0; i < order.size() - 1; i++) {
        				order.get(i + 1).setPrecedingOperator(order.get(i));
        				order.get(i).setSucceedingOperator(
        						new OperatorIDTuple(order.get(i + 1), 0));
        			}
        
        			final java.util.LinkedList<OperatorIDTuple> succs = (java.util.LinkedList<OperatorIDTuple>) this.t.getSucceedingOperators();
        			for (int i = 0; i < succs.size(); i++) {
        				succs.get(i).getOperator()
        						.addPrecedingOperator(order.getLast());
        			}
        
        			final java.util.LinkedList<OperatorIDTuple> sops = new java.util.LinkedList<OperatorIDTuple>();
        			sops.addAll(this.t.getSucceedingOperators());
        			order.getLast().setSucceedingOperators(sops);
        		} else {
        			final java.util.LinkedList<BasicOperator> pres = (java.util.LinkedList<BasicOperator>) this.g.getPrecedingOperators();
        			final java.util.LinkedList<OperatorIDTuple> succs = (java.util.LinkedList<OperatorIDTuple>) this.t.getSucceedingOperators();
        			BasicOperator pre;
        			BasicOperator succ;
        			for (int i = 0; i < pres.size(); i++) {
        				pre = pres.get(i);
        				pre.removeSucceedingOperator(this.g);
        				for (int x = 0; x < succs.size(); x++) {
        					pre.addSucceedingOperator(succs.get(x));
        					succ = succs.get(x).getOperator();
        					succ.removePrecedingOperator(this.t);
        					succ.addPrecedingOperator(pre);
        				}
        			}
        		}
    }
}
