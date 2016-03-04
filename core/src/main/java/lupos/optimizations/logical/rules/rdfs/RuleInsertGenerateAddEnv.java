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
package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.ReplaceLit;
import lupos.engine.operators.singleinput.generate.GenerateAddEnv;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;
public class RuleInsertGenerateAddEnv extends Rule {

	/** {@inheritDoc} */
	@Override
	protected void init() {
		final TriplePattern pat1 = new TriplePattern();
		final ReplaceLit repLit = new ReplaceLit();
		final TriplePattern pat2 = new TriplePattern();
		final BasicOperator succ = new BasicOperator();

		pat1.setSucceedingOperator(new OperatorIDTuple(repLit, 0));

		repLit.setPrecedingOperator(pat1);
		repLit.setSucceedingOperator(new OperatorIDTuple(succ, -1));

		pat2.addSucceedingOperator(new OperatorIDTuple(succ, -1));

		succ.setPrecedingOperator(repLit);
		succ.addPrecedingOperator(pat2);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(pat1, "pat1");
		subGraphMap.put(repLit, "repLit");
		subGraphMap.put(pat2, "pat2");
		subGraphMap.put(succ, "succ");

		startNode = repLit;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final ReplaceLit repLit = (ReplaceLit) mso.get("repLit");

		if (repLit.getPrecedingOperators().size() > 1) {
			System.out
					.println("ReplaceLit has more than one precessor => Change RuleInsertGenerateAddEnv!!!");
			return false;
		}

		final TriplePattern pat1 = (TriplePattern) mso.get("pat1");
		final TriplePattern pat2 = (TriplePattern) mso.get("pat2");
		if (!isSpecialCase(pat2.getItems(), pat1.getItems())) {
			return false;
		}

		// ReplaceLit should add the minimum of the extras from general to
		// special TriplePattern
		if (!replaceLitAddsExtras(pat2.getItems(), pat1.getItems(), repLit)) {
			return false;
		}

		final LinkedList<OperatorIDTuple> succsRepLit = (LinkedList<OperatorIDTuple>) repLit
				.getSucceedingOperators();
		final LinkedList<OperatorIDTuple> succsPat2 = (LinkedList<OperatorIDTuple>) pat2
				.getSucceedingOperators();
		// And they have got only the same successors with same operand-IDs
		return (equal(succsRepLit, succsPat2));
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final TriplePattern pat1 = (TriplePattern) mso.get("pat1");
		final ReplaceLit repLit = (ReplaceLit) mso.get("repLit");
		final TriplePattern pat2 = (TriplePattern) mso.get("pat2");

		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) repLit
				.getSucceedingOperators();

		final HashMap<Variable, Literal> constants = new HashMap<Variable, Literal>();

		final LinkedList<Variable> repLitLeft = repLit
				.getSubstitutionsLiteralLeft();
		final LinkedList<Literal> repLitRight = repLit
				.getSubstitutionsLiteralRight();
		for (int i = 0; i < repLitLeft.size(); i++) {
			constants.put(repLitLeft.get(i), repLitRight.get(i));
		}

		final HashMap<Variable, Literal> conditions = new HashMap<Variable, Literal>();
		final Item[] pat1Items = pat1.getItems();
		final Item[] pat2Items = pat2.getItems();
		Item i1;
		Item i2;
		for (int i = 0; i < 3; i++) {
			i1 = pat1Items[i];
			i2 = pat2Items[i];
			if ((!i1.isVariable()) && (i2.isVariable())) {
				conditions.put((Variable) i2, (Literal) i1);
			}
		}

		final GenerateAddEnv genAdd = new GenerateAddEnv(conditions, constants);

		pat1.removeSucceedingOperator(repLit);

		pat2.setSucceedingOperator(new OperatorIDTuple(genAdd, 0));

		genAdd.setPrecedingOperator(pat2);
		genAdd.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(genAdd);
			succ.removePrecedingOperator(pat2);
			succ.removePrecedingOperator(repLit);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		added.add(genAdd);
		deleted.add(repLit);
		if (!deleted.isEmpty() || !added.isEmpty())
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	private boolean isSpecialCase(final Item[] general, final Item[] special) {
		Item ig;
		Item is;
		for (int i = 0; i < 3; i++) {
			ig = general[i];
			is = special[i];
			if (!ig.equals(is)) {
				if (!(ig.isVariable() && !is.isVariable())) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean replaceLitAddsExtras(final Item[] general,
			final Item[] special, final ReplaceLit replaceLit) {
		Item ig;
		Item is;
		for (int i = 0; i < 3; i++) {
			ig = general[i];
			is = special[i];
			if (!ig.equals(is)) {
				return replaceLit.contains(ig, is);
			}
		}
		return true;
	}

	/**
	 * <p>equal.</p>
	 *
	 * @param list1 a {@link java.util.LinkedList} object.
	 * @param list2 a {@link java.util.LinkedList} object.
	 * @return a boolean.
	 */
	public boolean equal(final LinkedList<OperatorIDTuple> list1,
			final LinkedList<OperatorIDTuple> list2) {
		final LinkedList<String> l1 = new LinkedList<String>();
		for (int i = 0; i < list1.size(); i++) {
			l1.add(list1.get(i).toString());
		}

		final LinkedList<String> l2 = new LinkedList<String>();
		for (int i = 0; i < list2.size(); i++) {
			l2.add(list2.get(i).toString());
		}

		return (l1.containsAll(l2) && l2.containsAll(l1));
	}
}
