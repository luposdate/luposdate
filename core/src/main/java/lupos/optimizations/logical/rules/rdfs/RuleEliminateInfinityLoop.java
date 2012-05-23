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
package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.Filter;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleEliminateInfinityLoop extends Rule {

	private LinkedList<BasicOperator> disconnect;
	private LinkedList<BasicOperator> instead;

	@Override
	protected void init() {
		final Generate generate = new Generate();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(generate, "generate");

		startNode = generate;
	}

	protected LinkedList<LinkedList<BasicOperator>> checkInfinityLoop(
			final BasicOperator start) {
		final LinkedList<LinkedList<BasicOperator>> resultList = new LinkedList<LinkedList<BasicOperator>>();
		findCircle(start, start, new LinkedList<BasicOperator>(), resultList,
				new HashSet<BasicOperator>());
		return resultList;
	}

	protected boolean simulate(final LinkedList<BasicOperator> path) {
		// simulate an execution on the given path in order to find out if
		// a constant triple will be generated somewhere...
		HashMap<Variable, Item> bindings = new HashMap<Variable, Item>();
		Item[] triple = null;
		for (final BasicOperator bo : path) {
			if (bo instanceof Generate) {
				triple = new Item[3];
				final Generate generate = (Generate) bo;
				for (int i = 0; i < 3; i++) {
					Item item = generate.getValueOrVariable()[i];
					if (item.isVariable()) {
						if (bindings.get(item) != null) {
							item = bindings.get(item);
						}
					}
					triple[i] = item;
				}
				// is a constant triple generated?
				if (!triple[0].isVariable() && !triple[1].isVariable()
						&& !triple[2].isVariable()) {
					disconnect.add(path.get(1));
					final Generate generateInstead = new Generate(triple);
					final LinkedList<OperatorIDTuple> llo = new LinkedList<OperatorIDTuple>();
					llo.addAll(bo.getSucceedingOperators());
					// delete path from the successors:
					final int index = path.indexOf(new OperatorIDTuple(bo, -1));
					final BasicOperator afterInPath = (index == path.size() - 1) ? path
							.get(0)
							: path.get(index + 1);
					llo.remove(new OperatorIDTuple(afterInPath, -1));
					generateInstead.setSucceedingOperators(llo);
					instead.add(generateInstead);
					return true;
				}
			}
			if (bo instanceof TriplePattern) {
				bindings = new HashMap<Variable, Item>();
				final Item[] items = ((TriplePattern) bo).getItems();
				int i = 0;
				for (final Item item : items) {
					if (item.isVariable()) {
						bindings.put((Variable) item, triple[i]);
					} else {
						if (!((Literal) item).equals(triple[i])) {
							if (!triple[i].isVariable()) {
								System.out
										.println("RuleEliminateInfinityLoop: The generated triple will not be consumed by the succeeding triple pattern!");
								return false;
							}
						}
					}
					i++;
				}
			}
		}
		// is again the same triple generated as by the first call of the first
		// generate?
		if (path.get(0) instanceof Generate) {
			triple = new Item[3];
			final Generate generate = (Generate) path.get(0);
			for (int i = 0; i < 3; i++) {
				Item item = generate.getValueOrVariable()[i];
				if (item.isVariable()) {
					if (bindings.get(item) != null) {
						item = bindings.get(item);
					}
				}
				triple[i] = item;
			}
			// is a constant triple generated or the same triple as by the first
			// call of this generate?
			if ((!triple[0].isVariable() || triple[0].equals(generate
					.getValueOrVariable()[0]))
					&& (!triple[1].isVariable() || triple[0].equals(generate
							.getValueOrVariable()[1]))
					&& (!triple[2].isVariable() || triple[2].equals(generate
							.getValueOrVariable()[0]))) {
				disconnect.add(path.get(1));
				final Generate generateInstead = new Generate(triple);
				final LinkedList<OperatorIDTuple> llo = new LinkedList<OperatorIDTuple>();
				llo.addAll(path.get(0).getSucceedingOperators());
				// delete path from the successors:
				llo.remove(new OperatorIDTuple(path.get(1), -1));
				generateInstead.setSucceedingOperators(llo);
				instead.add(generateInstead);
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Generate generate = (Generate) mso.get("generate");
		// this check is simplified! It is not considered that on the path are
		// many succeeding nodes,
		// such the infinity loop cannot be broken in the simple way implemented
		// here...
		final LinkedList<LinkedList<BasicOperator>> resultList = checkInfinityLoop(generate);
		if (resultList == null || resultList.size() == 0)
			return false;
		disconnect = new LinkedList<BasicOperator>();
		instead = new LinkedList<BasicOperator>();
		boolean transform = false;
		for (final LinkedList<BasicOperator> pathToCheck : resultList) {
			if (pathToCheck.size() == 2
					|| onlyOneGenerateAndTriplePattern(pathToCheck)) {
				// short paths without joins are considered to be infinity loops
				// (must be checked for OWL more carefully!)
				disconnect.add(pathToCheck.get(1));
				transform = true;
			}
			// simulate the path!
			else
				transform = transform || simulate(pathToCheck);
		}
		return transform;
	}

	protected boolean onlyOneGenerateAndTriplePattern(
			final LinkedList<BasicOperator> pathToCheck) {
		int generates = 0;
		int triplepatterns = 0;
		for (final BasicOperator bo : pathToCheck) {
			if (bo instanceof TriplePattern)
				triplepatterns++;
			else if (bo instanceof Generate)
				generates++;
		}
		if (generates <= 1 && triplepatterns <= 1)
			return true;
		else
			return false;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		// (new OperatorGraphNew(rootOperator.deepClone(), -1, false))
		// .displayOperatorGraph("Before...", null);
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Generate generate = (Generate) mso.get("generate");

		for (final BasicOperator d : disconnect) {
			generate.removeSucceedingOperator(d);
			d.removePrecedingOperator(generate);
		}
		for (final BasicOperator i : instead) {
			// ??????????????? the following is questionable ???????????????????
			for (final BasicOperator d : disconnect) {
				i.removeSucceedingOperator(d);
			}
			// ??????????????? the previous is questionable ???????????????????
			for (final BasicOperator previous : generate
					.getPrecedingOperators()) {
				previous.addSucceedingOperator(new OperatorIDTuple(i, 0));
				i.addPrecedingOperator(previous);
			}
			added.add(i);
		}
		recursiveDelete(generate, deleted);
		// (new OperatorGraphNew(rootOperator.deepClone(), -1, false))
		// .displayOperatorGraph("After...", null);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	protected void recursiveDelete(final BasicOperator bo,
			final Collection<BasicOperator> deleted) {
		if (bo.getSucceedingOperators() == null
				|| bo.getSucceedingOperators().size() == 0) {
			for (final BasicOperator po : bo.getPrecedingOperators()) {
				deleted.add(bo);
				po.removeSucceedingOperator(bo);
				recursiveDelete(po, deleted);
			}
		}
	}

	protected void findCircle(final BasicOperator start,
			final BasicOperator basicOperator,
			final LinkedList<BasicOperator> currentPath,
			final LinkedList<LinkedList<BasicOperator>> resultList,
			final HashSet<BasicOperator> alreadyVisited) {
		if (alreadyVisited.contains(basicOperator)
				|| !(basicOperator instanceof Generate
						|| basicOperator instanceof Filter || basicOperator instanceof TriplePattern)) {
			return;
		}
		currentPath.addLast(basicOperator);
		alreadyVisited.add(basicOperator);
		for (final OperatorIDTuple opid : basicOperator
				.getSucceedingOperators()) {
			if (opid.getOperator().equals(start)) {
				final LinkedList<BasicOperator> cpnew = (LinkedList<BasicOperator>) currentPath
						.clone();
				resultList.add(cpnew);
			} else
				findCircle(start, opid.getOperator(), currentPath, resultList,
						alreadyVisited);
		}
		currentPath.removeLast();
	}
}
