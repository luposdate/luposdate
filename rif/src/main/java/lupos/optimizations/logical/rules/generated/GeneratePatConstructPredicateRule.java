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
package lupos.optimizations.logical.rules.generated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;




public class GeneratePatConstructPredicateRule extends Rule {

    private lupos.rif.operator.ConstructPredicate c = null;
    private lupos.rif.operator.PredicatePattern p = null;

    private boolean _checkPrivate0(BasicOperator _op) {
        if(_op.getClass() != lupos.rif.operator.ConstructPredicate.class) {
            return false;
        }

        this.c = (lupos.rif.operator.ConstructPredicate) _op;

        List<OperatorIDTuple> _succedingOperators_1_0 = _op.getSucceedingOperators();

        if(_succedingOperators_1_0.size() != 1) {
            return false;
        }

        for(OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
            if(_sucOpIDTup_1_0.getOperator().getClass() != lupos.rif.operator.PredicatePattern.class) {
                continue;
            }

            this.p = (lupos.rif.operator.PredicatePattern) _sucOpIDTup_1_0.getOperator();

            return true;
        }

        return false;
    }


    public GeneratePatConstructPredicateRule() {
        this.startOpClass = lupos.rif.operator.ConstructPredicate.class;
        this.ruleName = "Generate Pat ConstructPredicate";
    }

    protected boolean check(BasicOperator _op) {
        boolean _result = this._checkPrivate0(_op);

        if(_result) {
            // additional check method code...
            return this.c.getPredicatePattern().size() == 1;
        }

        return _result;
    }

    protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
        final lupos.datastructures.items.literal.URILiteral patURI = this.p.getPredicateName();
        		final lupos.datastructures.items.Item[] patItems = this.p.getPatternItems().toArray(new lupos.datastructures.items.Item[] {});
        
        		final lupos.datastructures.items.literal.URILiteral constructURI = this.c.getPredicatePattern().get(0)
        				.getFirst();
        		final lupos.datastructures.items.Item[] constructItems = this.c.getPredicatePattern().get(0)
        				.getSecond().toArray(new lupos.datastructures.items.Item[] {});
        
        		lupos.rif.operator.RuleFilter filter = null;
        		final lupos.engine.operators.singleinput.ReplaceVar replaceVar = new lupos.engine.operators.singleinput.ReplaceVar();
        		replaceVar.setIntersectionVariables(new java.util.HashSet<lupos.datastructures.items.Variable>());
        		replaceVar.setUnionVariables(replaceVar.getIntersectionVariables());
        
        		final java.util.LinkedList<lupos.datastructures.items.Variable> addBindingsVar = new java.util.LinkedList<lupos.datastructures.items.Variable>();
        		final java.util.LinkedList<lupos.datastructures.items.literal.Literal> addBindingsLit = new java.util.LinkedList<lupos.datastructures.items.literal.Literal>();
        
        		if (!patURI.equals(constructURI)
        				|| patItems.length != constructItems.length) {
        			for (final BasicOperator parent : this.c.getPrecedingOperators())
        				parent.removeSucceedingOperator(this.c);
        			this.c.getPrecedingOperators().clear();
        			this.c.removeFromOperatorGraph();
        			return;
        		}
        
        		final lupos.rif.model.Conjunction conj = new lupos.rif.model.Conjunction();
        		for (int i = 0; i < patItems.length; i++) {
        			final lupos.datastructures.items.Item patItem = patItems[i];
        			final lupos.datastructures.items.Item constructItem = constructItems[i];
        			if ((!patItem.isVariable()) && constructItem.isVariable()) {
        				{
        					final lupos.rif.model.Equality eq = new lupos.rif.model.Equality();
        					eq.leftExpr = new lupos.rif.model.RuleVariable(constructItem.getName());
        					eq.rightExpr = new lupos.rif.model.Constant((lupos.datastructures.items.literal.Literal) patItem, eq);
        					conj.addExpr(eq);
        				}
        			} else if (patItem.isVariable() && constructItem.isVariable()) {
        				replaceVar.addSubstitution((lupos.datastructures.items.Variable) patItem,
        						(lupos.datastructures.items.Variable) constructItem);
        				replaceVar.getIntersectionVariables().add((lupos.datastructures.items.Variable) patItem);
        			} else if (patItem.isVariable() && (!constructItem.isVariable())) {
        				addBindingsVar.add((lupos.datastructures.items.Variable) patItem);
        				addBindingsLit.add((lupos.datastructures.items.literal.Literal) constructItem);
        			} else if (!patItem.isVariable() && !constructItem.isVariable()
        					&& !constructItem.equals(patItem)) {
        				// cannot match, remove generate.
        				for (final BasicOperator parent : this.c.getPrecedingOperators())
        					parent.removeSucceedingOperator(this.c);
        				this.c.getPrecedingOperators().clear();
        				this.p.removePrecedingOperator(this.c);
        				return;
        			}
        		}
        
        		for (int i = 0; i < 2; i++) {
        			for (int x = i + 1; x < patItems.length; x++) {
        				if (patItems[i].equals(patItems[x])) {
        					final lupos.rif.model.Equality eq = new lupos.rif.model.Equality();
        					if (constructItems[i].isVariable())
        						eq.leftExpr = new lupos.rif.model.RuleVariable(
        								constructItems[i].getName());
        					else
        						eq.leftExpr = new lupos.rif.model.Constant((lupos.datastructures.items.literal.Literal) constructItems[i],
        								eq);
        					if (constructItems[x].isVariable())
        						eq.rightExpr = new lupos.rif.model.RuleVariable(
        								constructItems[x].getName());
        					else
        						eq.rightExpr = new lupos.rif.model.Constant(
        								(lupos.datastructures.items.literal.Literal) constructItems[x], eq);
        					conj.addExpr(eq);
        				}
        			}
        		}
        
        		if (!conj.isEmpty())
        			filter = new lupos.rif.operator.RuleFilter(conj, null);
        
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
        			final java.util.LinkedList<BasicOperator> pres = (java.util.LinkedList<BasicOperator>) this.c.getPrecedingOperators();
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
        				pre.removeSucceedingOperator(this.c);
        				order.getFirst().addPrecedingOperator(pre);
        			}
        
        			for (int i = 0; i < order.size() - 1; i++) {
        				order.get(i + 1).setPrecedingOperator(order.get(i));
        				order.get(i).setSucceedingOperator(
        						new OperatorIDTuple(order.get(i + 1), 0));
        			}
        
        			final java.util.List<OperatorIDTuple> succs = (java.util.List<OperatorIDTuple>) this.p.getSucceedingOperators();
        			for (int i = 0; i < succs.size(); i++) {
        				succs.get(i).getOperator()
        						.addPrecedingOperator(order.getLast());
        			}
        
        			final java.util.LinkedList<OperatorIDTuple> sops = new java.util.LinkedList<OperatorIDTuple>();
        			sops.addAll(this.p.getSucceedingOperators());
        			order.getLast().setSucceedingOperators(sops);
        		} else {
        			final java.util.LinkedList<BasicOperator> pres = (java.util.LinkedList<BasicOperator>) this.c.getPrecedingOperators();
        			final java.util.LinkedList<OperatorIDTuple> succs = (java.util.LinkedList<OperatorIDTuple>) this.p.getSucceedingOperators();
        			BasicOperator pre;
        			BasicOperator succ;
        			for (int i = 0; i < pres.size(); i++) {
        				pre = pres.get(i);
        				pre.removeSucceedingOperator(this.c);
        				for (int x = 0; x < succs.size(); x++) {
        					pre.addSucceedingOperator(succs.get(x));
        					succ = succs.get(x).getOperator();
        					succ.removePrecedingOperator(this.p);
        					succ.addPrecedingOperator(pre);
        				}
        			}
        		}
    }
}
