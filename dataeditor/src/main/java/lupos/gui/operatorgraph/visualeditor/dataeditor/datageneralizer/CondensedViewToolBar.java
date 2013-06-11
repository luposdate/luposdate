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
package lupos.gui.operatorgraph.visualeditor.dataeditor.datageneralizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.misc.Tuple;
import lupos.misc.util.ImmutableIterator;
import lupos.misc.util.OperatorIDTuple;
import lupos.rdf.JenaTurtleTripleConsumerPipe;

@SuppressWarnings("unchecked")
public class CondensedViewToolBar extends JPanel {
	private static final long serialVersionUID = 2228126871144675159L;

	protected CondensedViewViewer condensedViewViewer;
	private final Prefix prefix;

	protected int level;
	protected final int maxLevel = 5;

	private int variableCount = 0;
	private final HashMap<HyperNode, Variable> variableMap = new HashMap<HyperNode, Variable>();

	final HashMap<Literal, Set<Triple>> subjects = new HashMap<Literal, Set<Triple>>();
	final HashMap<Literal, Set<Triple>> objects = new HashMap<Literal, Set<Triple>>();

	private int numberOfTriples = 0;
	private Collection<HyperTriple> originalresultlevel0 = null;
	private final Collection<HyperTriple>[] result = new Collection[this.maxLevel + 1];

	public CondensedViewToolBar(final String text, final Prefix viewerPrefix) {
		this.prefix = viewerPrefix;
		// try {
		// CommonCoreQueryEvaluator.readTriples("JENAN3",
		// new ByteArrayInputStream(text.getBytes()),
		// new TripleProcessor() {
		// public Bindings process(final Triple triple) {
		// Set<Triple> st = subjects.get(triple.getSubject());
		// if (st == null)
		// st = new HashSet<Triple>();
		// st.add(triple);
		// subjects.put(triple.getSubject(), st);
		// Set<Triple> st2 = objects.get(triple.getObject());
		// if (st2 == null)
		// st2 = new HashSet<Triple>();
		// st2.add(triple);
		// objects.put(triple.getObject(), st);
		// return null;
		// }
		// });
		// } catch (final Exception e1) {
		try {
			final URILiteral rdfURL = LiteralFactory
					.createURILiteralWithoutLazyLiteral("<inlinedata:" + text
							+ ">");

			// first just retrieve the prefixes:

			final Reader reader = new InputStreamReader(rdfURL.openStream());
			final Map<String, String> prefixMap = JenaTurtleTripleConsumerPipe
					.retrievePrefixes(reader);

			for (final String prefix : prefixMap.keySet()) {
				this.prefix.addEntry(prefix, "<"
 + prefixMap.get(prefix) + ">",
						false);
			}

			CommonCoreQueryEvaluator.readTriples("N3",
					new ByteArrayInputStream(text.getBytes()),
					new TripleConsumer() {
						@Override
						public void consume(final Triple triple) {
							Set<Triple> st = CondensedViewToolBar.this.subjects.get(triple.getSubject());
							if (st == null) {
								st = new HashSet<Triple>();
							}
							st.add(triple);
							CondensedViewToolBar.this.subjects.put(triple.getSubject(), st);
							Set<Triple> st2 = CondensedViewToolBar.this.objects.get(triple.getObject());
							if (st2 == null) {
								st2 = new HashSet<Triple>();
							}
							st2.add(triple);
							CondensedViewToolBar.this.objects.put(triple.getObject(), st);
						}
					});
		} catch (final Exception e) {
			JOptionPane.showOptionDialog(this, e.getMessage(), "Error",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
					null, null, null);
		}
		// }
		this.generateStepButtons();
		// how to determine maxLevel???
		this.level = 0;
	}

	private void generateStepButtons() {
		final JButton stepButtonForward = new JButton(">");
		final JButton stepButtonBackward = new JButton("<");
		final JButton stepButtonLast = new JButton(">>");
		final JButton stepButtonStart = new JButton("<<");
		stepButtonForward.setToolTipText("condense more...");
		stepButtonForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (CondensedViewToolBar.this.level < CondensedViewToolBar.this.maxLevel) {
					CondensedViewToolBar.this.level++;
					stepButtonBackward.setEnabled(true);
					stepButtonStart.setEnabled(true);
					if (CondensedViewToolBar.this.level == CondensedViewToolBar.this.maxLevel) {
						stepButtonForward.setEnabled(false);
						stepButtonLast.setEnabled(false);
					}
					CondensedViewToolBar.this.drawNewCondensedView();
					CondensedViewToolBar.this.repaint();
				}
			}
		});
		stepButtonBackward.setToolTipText("condense less...");
		stepButtonBackward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (CondensedViewToolBar.this.level > 0) {
					CondensedViewToolBar.this.level--;
					stepButtonForward.setEnabled(true);
					stepButtonLast.setEnabled(true);
					if (CondensedViewToolBar.this.level == 0) {
						stepButtonBackward.setEnabled(false);
						stepButtonStart.setEnabled(false);
					}
					CondensedViewToolBar.this.drawNewCondensedView();
					CondensedViewToolBar.this.repaint();
				}
			}
		});
		stepButtonLast.setToolTipText("condense most...");
		stepButtonLast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				CondensedViewToolBar.this.level = CondensedViewToolBar.this.maxLevel;
				stepButtonBackward.setEnabled(true);
				stepButtonStart.setEnabled(true);
				stepButtonForward.setEnabled(false);
				stepButtonLast.setEnabled(false);
				CondensedViewToolBar.this.drawNewCondensedView();
				CondensedViewToolBar.this.repaint();
			}
		});
		stepButtonStart.setToolTipText("condense least...");
		stepButtonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				CondensedViewToolBar.this.level = 0;
				stepButtonForward.setEnabled(true);
				stepButtonLast.setEnabled(true);
				stepButtonBackward.setEnabled(false);
				stepButtonStart.setEnabled(false);
				CondensedViewToolBar.this.drawNewCondensedView();
				CondensedViewToolBar.this.repaint();
			}
		});

		this.add(stepButtonStart);
		this.add(stepButtonBackward);
		this.add(stepButtonForward);
		this.add(stepButtonLast);
		stepButtonForward.setEnabled(true);
		stepButtonLast.setEnabled(true);
		stepButtonBackward.setEnabled(false);
		stepButtonStart.setEnabled(false);
	}

	private void drawNewCondensedView() {
		this.displayHyperNodes(this.getRootList());
	}

	public LinkedList<GraphWrapper> getRootList() {
		if (this.result[0] == null) {
			// Level 0: put all objects ?o of (s, ?p, ?o) into one node, which
			// do
			// not have any succeeding nodes and no objects being URIs or blank
			// nodes,
			// which have several (>1) incoming edges!
			this.result[0] = new LinkedList<HyperTriple>();
			for (final Literal key : this.subjects.keySet()) {
				final Set<Triple> set = this.subjects.get(key);
				final LinkedList<Triple> hypernode = new LinkedList<Triple>();
				final LinkedList<Triple> remaining = new LinkedList<Triple>();
				for (final Triple triple : set) {
					this.numberOfTriples++;
					if (this.subjects.get(triple) != null) {
						remaining.add(triple);
					} else {
						if (((triple.getObject() instanceof AnonymousLiteral || triple
								.getObject() instanceof URILiteral) && this.objects
								.get(triple.getObject()).size() > 1)) {
							remaining.add(triple);
						} else {
							hypernode.add(triple);
						}
					}
				}
				if (hypernode.size() > 0) {
					System.out.println("Level 0: Combine " + hypernode.size());
					this.result[0].add(new HyperTriple(hypernode));
				}
				for (final Triple triple : remaining) {
					this.result[0].add(new HyperTriple(triple));
				}
			}
			this.originalresultlevel0 = this.result[0];
			this.result[0] = this.combineHyperNodes(this.result[0]);
		}
		if (this.level > 0 && this.result[1] == null) {
			// now condense all hypernodes with the same predicates into one
			// node
			// plus level 0 restrictions!
			final Collection<HyperTriple> result2 = new LinkedList<HyperTriple>();
			final HashMap<HyperNode, List<HyperTriple>> predicatesMap = new HashMap<HyperNode, List<HyperTriple>>();
			for (final HyperTriple ht : this.originalresultlevel0) {
				if (ht.getPos(0).size() == 1 && ht.getPos(1).size() == 1
						&& ht.getPos(2).size() == 1) {
					final Triple triple = new Triple(
							(Literal) ht.getPos(0).items.iterator().next(),
							(Literal) ht.getPos(1).items.iterator().next(),
							(Literal) ht.getPos(2).items.iterator().next());
					if (this.subjects.get(triple) != null) {
						result2.add(ht);
					} else {
						if (((triple.getObject() instanceof AnonymousLiteral || triple
								.getObject() instanceof URILiteral) && this.objects
								.get(triple.getObject()).size() > 1)) {
							result2.add(ht);
						} else {
							List<HyperTriple> lht = predicatesMap.get(ht
									.getPos(1));
							if (lht == null) {
								lht = new LinkedList<HyperTriple>();
							}
							lht.add(ht);
							predicatesMap.put(ht.getPos(1), lht);
						}
					}
				} else {
					List<HyperTriple> lht = predicatesMap.get(ht.getPos(1));
					if (lht == null) {
						lht = new LinkedList<HyperTriple>();
					}
					lht.add(ht);
					predicatesMap.put(ht.getPos(1), lht);
				}
			}
			for (final Entry<HyperNode, List<HyperTriple>> entry : predicatesMap
					.entrySet()) {
				if (entry.getValue().size() > 1) {
					System.out.println("Level 1: Condense "
							+ entry.getValue().size() + " hypertriples!");
				}
				// + entry.getValue());
				result2.add(new HyperTriple(entry.getValue()));
			}
			this.result[1] = this.combineHyperNodes(result2);
		}
		if (this.level > 1 && this.result[2] == null) {
			// level 1 plus condense all those, which have the same predicate
			// and object in a triple
			final Collection<HyperTriple> result2 = new LinkedList<HyperTriple>();
			final HashMap<Tuple<HyperNode, HyperNode>, List<HyperTriple>> predicatesObjectsMap = new HashMap<Tuple<HyperNode, HyperNode>, List<HyperTriple>>(
					this.numberOfTriples * 2);
			for (final HyperTriple ht : this.result[1]) {
				final Tuple<HyperNode, HyperNode> predicateObjectTuple = new Tuple<HyperNode, HyperNode>(
						ht.getPos(1), ht.getPos(2));
				List<HyperTriple> lht = predicatesObjectsMap
						.get(predicateObjectTuple);
				if (lht == null) {
					lht = new LinkedList<HyperTriple>();
				}
				lht.add(ht);
				predicatesObjectsMap.put(predicateObjectTuple, lht);
			}
			for (final Entry<Tuple<HyperNode, HyperNode>, List<HyperTriple>> entry : predicatesObjectsMap
					.entrySet()) {
				if (entry.getValue().size() > 1) {
					System.out.println("Level 2: Condense "
							+ entry.getValue().size() + " hypertriples!");
				}
				// + entry.getValue());
				final HyperTriple ht = new HyperTriple(entry.getValue());
				result2.add(ht);
			}
			this.result[2] = this.combineHyperNodes(result2);
		}
		if (this.level > 2 && this.result[3] == null) {
			// now do the same as in level 0 with the result of level 2, but
			// only combine those with common subjects and predicates
			final Collection<HyperTriple> result2 = new LinkedList<HyperTriple>();
			final HashMap<Tuple<HyperNode, HyperNode>, List<HyperTriple>> predicatesMap = new HashMap<Tuple<HyperNode, HyperNode>, List<HyperTriple>>();
			for (final HyperTriple ht : this.result[2]) {
				final Tuple<HyperNode, HyperNode> tuple = new Tuple<HyperNode, HyperNode>(
						ht.getPos(0), ht.getPos(1));
				List<HyperTriple> lht = predicatesMap.get(tuple);
				if (lht == null) {
					lht = new LinkedList<HyperTriple>();
				}
				lht.add(ht);
				predicatesMap.put(tuple, lht);
			}
			for (final Entry<Tuple<HyperNode, HyperNode>, List<HyperTriple>> entry : predicatesMap
					.entrySet()) {
				if (entry.getValue().size() > 1) {
					System.out.println("Level 3: Condense "
							+ entry.getValue().size() + " hypertriples!");
				}
				// + entry.getValue());
				result2.add(new HyperTriple(entry.getValue()));
			}
			this.result[3] = this.combineHyperNodes(result2);
		}
		if (this.level > 3 && this.result[4] == null) {
			// combine those with common subject and predicates, which have at
			// least one predicate in common...
			final Collection<HyperTriple> result2 = new HashSet<HyperTriple>();
			final HashMap<Tuple<HyperNode, Item>, Set<HyperTriple>> predicatesMap = new HashMap<Tuple<HyperNode, Item>, Set<HyperTriple>>();
			for (final HyperTriple ht : this.result[3]) {
				final HashSet<HyperTriple> hsht = new HashSet<HyperTriple>();
				for (final Item item : ht.getPos(1)) {
					final Tuple<HyperNode, Item> tuple = new Tuple<HyperNode, Item>(
							ht.getPos(0), item);
					final Set<HyperTriple> sht = predicatesMap.get(tuple);
					if (sht != null) {
						hsht.addAll(sht);
					}
				}
				hsht.add(ht);
				for (final HyperTriple ht2 : hsht) {
					for (final Item item : ht2.getPos(1)) {
						final Tuple<HyperNode, Item> tuple = new Tuple<HyperNode, Item>(
								ht.getPos(0), item);
						predicatesMap.put(tuple, hsht);
					}
				}
			}
			for (final Entry<Tuple<HyperNode, Item>, Set<HyperTriple>> entry : predicatesMap
					.entrySet()) {
				if (entry.getValue().size() > 1) {
					System.out.println("Level 4: Condense "
							+ entry.getValue().size() + " hypertriples!");
				}
				// + entry.getValue());
				result2.add(new HyperTriple(entry.getValue()));
			}
			this.result[4] = this.combineHyperNodes(result2);
		}
		if (this.level > 4 && this.result[5] == null) {
			// level 2 plus condense all those hypertriples, which have the same
			// predicates
			final Collection<HyperTriple> result2 = new LinkedList<HyperTriple>();
			final HashMap<HyperNode, List<HyperTriple>> predicatesMap = new HashMap<HyperNode, List<HyperTriple>>();
			for (final HyperTriple ht : this.result[2]) {
				List<HyperTriple> lht = predicatesMap.get(ht.getPos(1));
				if (lht == null) {
					lht = new LinkedList<HyperTriple>();
				}
				lht.add(ht);
				predicatesMap.put(ht.getPos(1), lht);
			}
			for (final Entry<HyperNode, List<HyperTriple>> entry : predicatesMap
					.entrySet()) {
				if (entry.getValue().size() > 1) {
					System.out.println("Level 5: Condense "
							+ entry.getValue().size() + " hypertriples!");
				}
				// + entry.getValue());
				result2.add(new HyperTriple(entry.getValue()));
			}
			this.result[5] = this.combineHyperNodes(result2);
		}
		return this.getRootList(this.result[this.level]);
	}

	private HashMap<Item, HyperNode> getItemMap(
			final Collection<HyperTriple> cht) {
		final HashMap<Item, HyperNode> map = new HashMap<Item, HyperNode>();
		for (final HyperTriple ht : cht) {
			for (int i = 0; i < 3; i++) {
				if (i == 1) {
					continue;
				}
				final HashSet<HyperNode> toMerge = new HashSet<HyperNode>();
				for (final Item item : ht.getPos(i)) {
					if (!item.isVariable()) {
						final HyperNode hn = map.get(item);
						if (hn != null) {
							toMerge.add(hn);
						}
					}
				}
				toMerge.add(ht.getPos(i));
				final HyperNode hn = new HyperNode(toMerge);
				for (final Item item : ht.getPos(i)) {
					if (!item.isVariable()) {
						map.put(item, hn);
					}
				}
				for (final HyperNode hnToMerge : toMerge) {
					for (final Item item : hnToMerge) {
						if (!item.isVariable()) {
							map.put(item, hn);
						}
					}
				}
			}
		}
		return map;
	}

	private Collection<HyperTriple> combineHyperNodes(
			final Collection<HyperTriple> cht) {
		final Collection<HyperTriple> result = new HashSet<HyperTriple>();
		final HashMap<Item, HyperNode> map = this.getItemMap(cht);

		for (final HyperTriple ht : cht) {
			final HyperNode[] hns = new HyperNode[3];
			for (int i = 0; i < 3; i++) {
				if (i == 1) {
					hns[i] = ht.getPos(i);
					continue;
				}
				for (final Item item : ht.getPos(i)) {
					if (!item.isVariable()) {
						hns[i] = map.get(item);
						if (hns[i] != null) {
							break;
						}
					}
				}
			}
			result.add(new HyperTriple(hns));
		}
		return result;
	}

	private LinkedList<GraphWrapper> getRootList(
			final Collection<HyperTriple> hypertriples) {
		// now really deal with the triples...
		final LinkedHashMap<Item, Operator> rdfHash = new LinkedHashMap<Item, Operator>();
		final LinkedHashSet<Operator> rdfTermToJoin = new LinkedHashSet<Operator>();

		for (final HyperTriple hypertriple : hypertriples) {
			CondensedRDFTerm rdfTermSubject = (CondensedRDFTerm) rdfHash
					.get(hypertriple.getPos(0).iterator().next());

			if (rdfTermSubject == null) {
				rdfTermSubject = new CondensedRDFTerm(this.prefix, hypertriple
						.getPos(0).getItems());
				rdfHash.put(hypertriple.getPos(0).iterator().next(),
						rdfTermSubject);
			}

			CondensedRDFTerm rdfTermObject = (CondensedRDFTerm) rdfHash
					.get(hypertriple.getPos(2).iterator().next());

			if (rdfTermObject == null) {
				rdfTermObject = new CondensedRDFTerm(this.prefix, hypertriple
						.getPos(2).getItems());
				rdfHash.put(hypertriple.getPos(2).iterator().next(),
						rdfTermObject);
			}
			final LinkedList<Item> lli = rdfTermSubject
					.getPredicates(rdfTermObject);
			for (final Item predicate : hypertriple.getPos(1)) {
				if (lli == null || !lli.contains(predicate)) {
					rdfTermSubject.addPredicate(rdfTermObject, predicate);
				}
			}
			final OperatorIDTuple<Operator> opIDT = new OperatorIDTuple<Operator>(
					rdfTermObject, 0);

			if (!rdfTermSubject.getSucceedingOperators().contains(opIDT)) {
				rdfTermSubject.addSucceedingOperator(opIDT);
			}

			rdfTermToJoin.add(rdfTermSubject);

		}
		final LinkedList<GraphWrapper> rootList = new LinkedList<GraphWrapper>();

		for (final Operator op : RDFTerm.findRootNodes(rdfTermToJoin)) {
			op.setParents();

			rootList.add(new GraphWrapperOperator(op));
		}
		return rootList;
	}

	private void displayHyperNodes(final LinkedList<GraphWrapper> gws) {

		System.out.println("Displaying data...");

		if (this.condensedViewViewer != null) {
			this.condensedViewViewer.getVisualGraphs().get(0).updateMainPanel(
					this.condensedViewViewer.getVisualGraphs().get(0).createGraph(
											gws,
											lupos.gui.operatorgraph.arrange.Arrange
													.values()[0]));
		}
	}

	public CondensedViewViewer getOperatorGraphViewer() {
		return this.condensedViewViewer;
	}

	public void setOperatorGraphViewer(
			final CondensedViewViewer operatorGraphViewer) {
		this.condensedViewViewer = operatorGraphViewer;
		this.drawNewCondensedView();
	}

	public Variable getVariable(final HyperNode hypernode) {
		Variable var = this.variableMap.get(hypernode);
		if (var == null) {
			var = new Variable("x" + this.variableCount++);
			this.variableMap.put(hypernode, var);
		}
		return var;
	}

	public class HyperTriple {
		private final HyperNode[] hypernodes = new HyperNode[3];

		public HyperTriple(final Triple triple) {
			this(new ImmutableIterator<Triple>() {
				boolean already = false;

				@Override
				public boolean hasNext() {
					return !this.already;
				}

				@Override
				public Triple next() {
					if (!this.already) {
						this.already = true;
						return triple;
					}
					return null;
				}
			});
		}

		public HyperTriple(final Collection<Triple> triples) {
			this(triples.iterator());
		}

		public HyperTriple(final Collection<Triple> triples,
				final HyperNode subject) {
			this.hypernodes[0] = subject;
			for (int i = 1; i < 3; i++) {
				this.hypernodes[i] = new HyperNode();
			}
			for (final Triple t : triples) {
				for (int i = 1; i < 3; i++) {
					if (!this.hypernodes[i].contains(t.getPos(i))) {
						this.hypernodes[i].add(t.getPos(i));
					}
				}
			}
			if (this.hypernodes[2].size() > 1) {
				final Variable varO = CondensedViewToolBar.this.getVariable(this.hypernodes[2]);
				this.hypernodes[2].addFirst(varO);
			}
		}

		public HyperTriple(final HyperNode[] hypernodes) {
			this.hypernodes[0] = hypernodes[0];
			this.hypernodes[1] = hypernodes[1];
			this.hypernodes[2] = hypernodes[2];
		}

		public HyperTriple(final HyperNode subject, final Literal predicate,
				final HyperNode object) {
			this.hypernodes[0] = subject;
			this.hypernodes[2] = object;
			this.hypernodes[1] = new HyperNode();
			this.hypernodes[1].add(predicate);
		}

		public HyperTriple(final Iterator<Triple> iterator) {
			for (int i = 0; i < 3; i++) {
				this.hypernodes[i] = new HyperNode();
			}
			while (iterator.hasNext()) {
				final Triple t = iterator.next();
				for (int i = 0; i < 3; i++) {
					if (!this.hypernodes[i].contains(t.getPos(i))) {
						this.hypernodes[i].add(t.getPos(i));
					}
				}
			}
			if (this.hypernodes[0].size() > 1) {
				final Variable varS = CondensedViewToolBar.this.getVariable(this.hypernodes[0]);
				this.hypernodes[0].addFirst(varS);
			}
			if (this.hypernodes[2].size() > 1) {
				final Variable varO = CondensedViewToolBar.this.getVariable(this.hypernodes[2]);
				this.hypernodes[2].addFirst(varO);
			}
		}

		public HyperTriple(final List<HyperTriple> listOfHyperTriples) {
			for (int i = 0; i < 3; i++) {
				int initialCapacity = 0;
				for (final HyperTriple ht : listOfHyperTriples) {
					initialCapacity += ht.getPos(i).size();
				}
				this.hypernodes[i] = new HyperNode((int) (initialCapacity * 1.25));

				for (final HyperTriple ht : listOfHyperTriples) {
					for (final Item item : ht.getPos(i)) {
						if (!item.isVariable()) {
							this.hypernodes[i].add(item);
						}
					}
				}
			}
			if (this.hypernodes[0].size() > 1) {
				final Variable varS = CondensedViewToolBar.this.getVariable(this.hypernodes[0]);
				this.hypernodes[0].addFirst(varS);
			}
			if (this.hypernodes[2].size() > 1) {
				final Variable varO = CondensedViewToolBar.this.getVariable(this.hypernodes[2]);
				this.hypernodes[2].addFirst(varO);
			}
		}

		public HyperTriple(final Set<HyperTriple> listOfHyperTriples) {
			for (int i = 0; i < 3; i++) {
				int initialCapacity = 0;
				for (final HyperTriple ht : listOfHyperTriples) {
					initialCapacity += ht.getPos(i).size();
				}
				this.hypernodes[i] = new HyperNode((int) (initialCapacity * 1.25));

				for (final HyperTriple ht : listOfHyperTriples) {
					for (final Item item : ht.getPos(i)) {
						if (!item.isVariable()) {
							this.hypernodes[i].add(item);
						}
					}
				}
			}
			if (this.hypernodes[0].size() > 1) {
				final Variable varS = CondensedViewToolBar.this.getVariable(this.hypernodes[0]);
				this.hypernodes[0].addFirst(varS);
			}
			if (this.hypernodes[2].size() > 1) {
				final Variable varO = CondensedViewToolBar.this.getVariable(this.hypernodes[2]);
				this.hypernodes[2].addFirst(varO);
			}
		}

		public HyperNode getPos(final int i) {
			return this.hypernodes[i];
		}

		@Override
		public String toString() {
			return "(" + this.hypernodes[0] + ", " + this.hypernodes[1] + ", "
					+ this.hypernodes[2] + ")";
		}
	}

	public class HyperNode implements Iterable<Item> {

		private LinkedHashSet<Item> items;

		public HyperNode() {
			this.items = new LinkedHashSet<Item>();
		}

		public HyperNode(final int initialCapacity) {
			this.items = new LinkedHashSet<Item>(initialCapacity);
		}

		public HyperNode(final HyperNode hyperNode,
				final boolean removeVariables) {
			if (removeVariables) {
				this.items = new LinkedHashSet<Item>();
				for (final Item item : hyperNode) {
					if (!item.isVariable()) {
						this.add(item);
					}
				}
			} else {
				this.items = hyperNode.items;
			}
		}

		public HyperNode(final Item item) {
			this();
			this.add(item);
		}

		public HyperNode(final LinkedHashSet<Item> items) {
			this.items = items;
		}

		public HyperNode(final HashSet<HyperNode> toMerge) {
			this();
			for (final HyperNode hn : toMerge) {
				for (final Item item : hn) {
					if (!item.isVariable()) {
						this.add(item);
					}
				}
			}
			if (this.size() > 1) {
				final Variable var = CondensedViewToolBar.this.getVariable(this);
				this.addFirst(var);
			}
		}

		public void add(final Item item) {
			this.items.add(item);
		}

		public int size() {
			return this.items.size();
		}

		public void addFirst(final Item item) {
			final LinkedHashSet<Item> zitems = new LinkedHashSet<Item>();
			zitems.add(item);
			zitems.addAll(this.items);
			this.items = zitems;
		}

		public boolean contains(final Item item) {
			return this.items.contains(item);
		}

		@Override
		public Iterator<Item> iterator() {
			return this.items.iterator();
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof HyperNode) {
				final HyperNode hn = (HyperNode) o;
				for (final Item item : this.items) {
					if (!item.isVariable() && !hn.items.contains(item)) {
						return false;
					}
				}
				for (final Item item : hn.items) {
					if (!item.isVariable() && !this.items.contains(item)) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			long hash = 0;
			for (final Item item : this.items) {
				if (!item.isVariable()) {
					hash = (hash + item.hashCode()) % Integer.MAX_VALUE;
				}
			}
			return (int) hash;
		}

		@Override
		public String toString() {
			return this.items.toString();
		}

		public LinkedHashSet<Item> getItems() {
			return this.items;
		}
	}
}