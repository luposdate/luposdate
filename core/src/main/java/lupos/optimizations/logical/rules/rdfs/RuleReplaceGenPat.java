/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.AddBinding;
import lupos.engine.operators.singleinput.Filter;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
import lupos.sparql1_1.ASTFilterConstraint;
import lupos.sparql1_1.SPARQL1_1Parser;

public class RuleReplaceGenPat extends Rule {

	@Override
	protected void init() {
		final Generate generate = new Generate();
		final TriplePattern pat = new TriplePattern();

		generate.setSucceedingOperator(new OperatorIDTuple(pat, -1));

		pat.setPrecedingOperator(generate);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(generate, "generate");
		subGraphMap.put(pat, "pat");

		startNode = generate;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Generate generate = (Generate) mso.get("generate");

		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) generate
				.getSucceedingOperators();
		// First finish RuleSplitGenerates
		return ((succs.size() == 1) && (succs.get(0).getOperator() instanceof TriplePattern));
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Generate generate = (Generate) mso.get("generate");
		final TriplePattern pat = (TriplePattern) mso.get("pat");
		final Item[] patItems = pat.getItems();
		final Item[] generateItems = generate.getValueOrVariable();

		// System.out.println(generate.toString() + "---" + pat.toString());

		Filter filter = null;
		final ReplaceVar replaceVar = new ReplaceVar();
		replaceVar.setIntersectionVariables(new HashSet<Variable>());
		replaceVar.setUnionVariables(replaceVar.getIntersectionVariables());

		final LinkedList<Variable> addBindingsVar = new LinkedList<Variable>();
		final LinkedList<Literal> addBindingsLit = new LinkedList<Literal>();

		String filterConstraint = "Filter( ";
		for (int i = 0; i < 3; i++) {
			final Item patItem = patItems[i];
			final Item generateItem = generateItems[i];
			if ((!patItem.isVariable()) && generateItem.isVariable()) {
				filterConstraint += generateItems[i].toString() + " = "
						+ patItems[i].toString() + " && ";
			} else if (patItem.isVariable() && generateItem.isVariable()) {
				replaceVar.addSubstitution((Variable) patItem,
						(Variable) generateItem);
				replaceVar.getIntersectionVariables().add((Variable) patItem);
			} else if (patItem.isVariable() && (!generateItem.isVariable())) {
				addBindingsVar.add((Variable) patItem);
				addBindingsLit.add((Literal) generateItem);
			} else if (!patItem.isVariable() && !generateItem.isVariable()
					&& !generateItem.equals(patItem)) {
				// cannot match, remove generate.
				for (final BasicOperator parent : generate
						.getPrecedingOperators())
					parent.removeSucceedingOperator(generate);
				generate.getPrecedingOperators().clear();
				generate.removeFromOperatorGraph();
				return null;
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
				final ASTFilterConstraint ASTfilter = (ASTFilterConstraint) SPARQL1_1Parser
						.parseFilter(filterConstraint);
				filter = new Filter(ASTfilter);
			} catch (final Exception e) {
				System.err
						.println("This should never happen in RuleReplaceGenPat!");
				System.err.println(e);
				e.printStackTrace();
			}
		}

		// Only Operators with a not empty definition are put into the
		// operatorgraph
		final LinkedList<BasicOperator> order = new LinkedList<BasicOperator>();
		if (filter != null) {
			order.add(filter);
			added.add(filter);
		}

		final int substVar = replaceVar.getSubstitutionsVariableLeft().size();

		if (substVar > 0) {
			order.add(replaceVar);
			added.add(replaceVar);
		} else {
			final Projection p = new Projection();
			p.setIntersectionVariables(new HashSet<Variable>());
			p.setUnionVariables(p.getUnionVariables());
			order.add(p);
			added.add(p);
		}
		if (addBindingsVar.size() > 0) {
			final Iterator<Literal> lit_it = addBindingsLit.iterator();
			final HashSet<Variable> hsv = new HashSet<Variable>();
			hsv.addAll(replaceVar.getUnionVariables());
			for (final Variable v : addBindingsVar) {
				final AddBinding ab = new AddBinding(v, lit_it.next());
				hsv.add(v);
				ab.setIntersectionVariables((HashSet<Variable>) hsv.clone());
				ab.setUnionVariables(ab.getIntersectionVariables());
				order.add(ab);
				added.add(ab);
			}
		}

		// In case that Generate or TriplePattern has minimum one variable, than
		// minimum one operator has to be inserted
		if (order.size() > 0) {
			final List<BasicOperator> pres = (List<BasicOperator>) generate
					.getPrecedingOperators();
			BasicOperator pre;
			for (int i = 0; i < pres.size(); i++) {
				pre = pres.get(i);
				pre.addSucceedingOperator(new OperatorIDTuple(order.getFirst(),
						0));
				if (filter != null) {
					Collection<Variable> vars = filter
							.getIntersectionVariables();
					if (vars == null)
						vars = new HashSet<Variable>();
					vars.addAll(pre.getIntersectionVariables());
					filter.setIntersectionVariables(vars);
					filter.setUnionVariables(vars);
				}
				pre.removeSucceedingOperator(generate);
				order.getFirst().addPrecedingOperator(pre);
			}

			for (int i = 0; i < order.size() - 1; i++) {
				order.get(i + 1).setPrecedingOperator(order.get(i));
				order.get(i).setSucceedingOperator(
						new OperatorIDTuple(order.get(i + 1), 0));
			}

			final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) pat
					.getSucceedingOperators();
			for (int i = 0; i < succs.size(); i++) {
				succs.get(i).getOperator()
						.addPrecedingOperator(order.getLast());
			}

			final LinkedList<OperatorIDTuple> sops = new LinkedList<OperatorIDTuple>();
			sops.addAll(pat.getSucceedingOperators());
			order.getLast().setSucceedingOperators(sops);
		} else {
			final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) generate
					.getPrecedingOperators();
			final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) pat
					.getSucceedingOperators();
			BasicOperator pre;
			BasicOperator succ;
			for (int i = 0; i < pres.size(); i++) {
				pre = pres.get(i);
				pre.removeSucceedingOperator(generate);
				for (int x = 0; x < succs.size(); x++) {
					pre.addSucceedingOperator(succs.get(x));
					succ = succs.get(x).getOperator();
					succ.removePrecedingOperator(pat);
					succ.addPrecedingOperator(pre);
				}
			}
		}

		// TriplePattern can have more predecessors then Generate..
		pat.removePrecedingOperator(generate);
		if (pat.getPrecedingOperators().size() == 0)
			deleted.add(pat);
		// System.out.println(pat.getPrecedingOperators());
		deleted.add(generate);

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		// has been done manually: rootOperator.sendMessage(new
		// BoundVariablesMessage());
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
