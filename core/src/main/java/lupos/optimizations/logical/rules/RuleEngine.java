
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.optimizations.logical.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.rdf.Prefix;
public abstract class RuleEngine {

	private final static boolean checkNodeMap = false;

	protected Rule[] rules;

	/**
	 * <p>Constructor for RuleEngine.</p>
	 */
	public RuleEngine() {
	}

	/**
	 * <p>applyRules.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 */
	public void applyRules(final BasicOperator op) {
		applyRules(op, RuleEngine.createStartNodeMap(op), this.rules, null);
	}

	/**
	 * <p>applyRules.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 * @param mapStartNodes a {@link java.util.Map} object.
	 * @param rules an array of {@link lupos.optimizations.logical.rules.Rule} objects.
	 * @param untilRule a {@link lupos.optimizations.logical.rules.Rule} object.
	 */
	public void applyRules(
			final BasicOperator op,
			final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes,
			final Rule[] rules, final Rule untilRule) {
		for (final Rule r : rules) {
			if (untilRule != null && r.equals(untilRule))
				break;
			while (r.apply(op, mapStartNodes)) {
				Rule[] rulesToApply = r.getRulesToApply(this);
				if (rulesToApply == null)
					rulesToApply = this.rules;
				applyRules(op, mapStartNodes, rulesToApply, r);
			}
		}
	}
	
	/**
	 * <p>applyRulesDebugByteArray.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 * @param prefixInstance a {@link lupos.rdf.Prefix} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<DebugContainer<BasicOperatorByteArray>> applyRulesDebugByteArray(
			final BasicOperator op, final Prefix prefixInstance) {
		return applyRulesDebugByteArray(op, RuleEngine.createStartNodeMap(op),
				this.rules, null, prefixInstance);
	}
	
	/**
	 * <p>applyRulesDebugByteArray.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 * @param mapStartNodes a {@link java.util.Map} object.
	 * @param rules an array of {@link lupos.optimizations.logical.rules.Rule} objects.
	 * @param untilRule a {@link lupos.optimizations.logical.rules.Rule} object.
	 * @param prefixInstance a {@link lupos.rdf.Prefix} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<DebugContainer<BasicOperatorByteArray>> applyRulesDebugByteArray(
			final BasicOperator op,
			final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes,
			final Rule[] rules, final Rule untilRule,
			final Prefix prefixInstance) {
		final List<DebugContainer<BasicOperatorByteArray>> debug = new LinkedList<DebugContainer<BasicOperatorByteArray>>();
		for (final Rule r : rules) {
			if (untilRule != null && r.equals(untilRule))
				break;
			while (r.applyDebug(op, mapStartNodes)) {
				debug.add(new DebugContainer<BasicOperatorByteArray>(r
						.getName(), r.getDescription(), BasicOperatorByteArray
						.getBasicOperatorByteArray(op.deepClone(),
								prefixInstance)));
				// mapStartNodes = RuleEngine.createStartNodeMap(op);
				if (checkNodeMap) {
					// check if all nodes of the operatorgraph are in the
					// map:
					op.visit(new SimpleOperatorGraphVisitor() {
						public Object visit(final BasicOperator basicOperator) {
							if (!checkIfInNodeMap(basicOperator, mapStartNodes))
								System.err
								.println("The following node is not in mapStartNodes:"
										+ basicOperator);
							return null;
						}
					});
					// check if all nodes of the map are in the
					// operatorgraph:
					for (final Map.Entry<Class<? extends BasicOperator>, Set<BasicOperator>> entry : mapStartNodes
							.entrySet()) {
						for (final BasicOperator bo : entry.getValue()) {
							final FindInOperatorGraph findInOperatorGraph = new FindInOperatorGraph(
									bo);
							op.visit(findInOperatorGraph);
							if (!findInOperatorGraph.getFlag())
								System.err
								.println("The following node for class "
										+ entry.getKey()
										+ " is not in the operatorgraph:"
										+ bo);
						}
					}
				}
				Rule[] rulesToApply = r.getRulesToApply(this);
				if (rulesToApply == null)
					rulesToApply = this.rules;
				debug.addAll(applyRulesDebugByteArray(op, mapStartNodes,
						rulesToApply, r, prefixInstance));
			}
		}
		return debug;
	}


	public class FindInOperatorGraph implements SimpleOperatorGraphVisitor {
		private boolean flag;
		private final BasicOperator toFind;

		public FindInOperatorGraph(final BasicOperator toFind) {
			this.flag = false;
			this.toFind = toFind;
		}

		public Object visit(final BasicOperator basicOperator) {
			if (basicOperator.equals(toFind))
				flag = true;
			return null;
		}

		public boolean getFlag() {
			return flag;
		}
	}

	/**
	 * <p>createRules.</p>
	 */
	protected abstract void createRules();

	/**
	 * <p>addToNodeMap.</p>
	 *
	 * @param basicOperator a {@link lupos.engine.operators.BasicOperator} object.
	 * @param mapStartNodes a {@link java.util.Map} object.
	 */
	public static void addToNodeMap(
			final BasicOperator basicOperator,
			final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes) {
		Set<BasicOperator> set = mapStartNodes.get(basicOperator.getClass());
		if (set == null) {
			set = new HashSet<BasicOperator>();
		}
		set.add(basicOperator);
		mapStartNodes.put(basicOperator.getClass(), set);

		set = mapStartNodes.get(BasicOperator.class);
		if (set == null) {
			set = new HashSet<BasicOperator>();
		}
		set.add(basicOperator);
		mapStartNodes.put(BasicOperator.class, set);

		if (basicOperator instanceof Join &&
				// exclude special case:
				basicOperator.getClass().getPackage().getName().compareTo(
				"lupos.engine.operators.multiinput.join.parallel") != 0) {
			set = mapStartNodes.get(Join.class);
			if (set == null) {
				set = new HashSet<BasicOperator>();
			}
			set.add(basicOperator);
			mapStartNodes.put(Join.class, set);

		}

		if (basicOperator instanceof Optional ||
				// include special case:
				basicOperator.getClass().getPackage().getName().compareTo(
				"lupos.engine.operators.multiinput.optional.parallel") == 0) {
			set = mapStartNodes.get(Optional.class);
			if (set == null) {
				set = new HashSet<BasicOperator>();
			}
			set.add(basicOperator);
			mapStartNodes.put(Optional.class, set);
		}
	}

	/**
	 * <p>deleteFromNodeMap.</p>
	 *
	 * @param basicOperator a {@link lupos.engine.operators.BasicOperator} object.
	 * @param mapStartNodes a {@link java.util.Map} object.
	 */
	public static void deleteFromNodeMap(
			final BasicOperator basicOperator,
			final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes) {
		Set<BasicOperator> set = mapStartNodes.get(basicOperator.getClass());
		if (set != null) {
			set.remove(basicOperator);
			if (set.size() > 0)
				mapStartNodes.put(basicOperator.getClass(), set);
			else
				mapStartNodes.remove(basicOperator.getClass());
		}

		set = mapStartNodes.get(BasicOperator.class);
		if (set != null) {
			set.remove(basicOperator);
			mapStartNodes.put(BasicOperator.class, set);
		}

		if (basicOperator instanceof Join &&
				// exclude special case:
				basicOperator.getClass().getPackage().getName().compareTo(
				"lupos.engine.operators.multiinput.optional.parallel") != 0) {
			set = mapStartNodes.get(Join.class);
			if (set != null) {
				set.remove(basicOperator);
				if (set.size() > 0)
					mapStartNodes.put(Join.class, set);
				else
					mapStartNodes.remove(Join.class);
			}
		}

		if (basicOperator instanceof Optional ||
				// include special case:
				basicOperator.getClass().getPackage().getName().compareTo(
				"lupos.engine.operators.multiinput.optional.parallel") == 0) {
			set = mapStartNodes.get(Optional.class);
			if (set != null) {
				set.remove(basicOperator);
				if (set.size() > 0)
					mapStartNodes.put(Optional.class, set);
				else
					mapStartNodes.remove(Optional.class);
			}
		}
	}

	/**
	 * <p>checkIfInNodeMap.</p>
	 *
	 * @param basicOperator a {@link lupos.engine.operators.BasicOperator} object.
	 * @param mapStartNodes a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	public static boolean checkIfInNodeMap(
			final BasicOperator basicOperator,
			final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes) {
		Set<BasicOperator> set = mapStartNodes.get(basicOperator.getClass());
		if (set == null) {
			return false;
		}
		if (!set.contains(basicOperator))
			return false;

		set = mapStartNodes.get(BasicOperator.class);
		if (set == null) {
			return false;
		}
		if (!set.contains(basicOperator))
			return false;

		if (basicOperator instanceof Join &&
				// exclude special case:
				basicOperator.getClass().getPackage().getName().compareTo(
				"lupos.engine.operators.multiinput.optional.parallel") != 0) {
			set = mapStartNodes.get(Join.class);
			if (set == null) {
				return false;
			}
			if (!set.contains(basicOperator))
				return false;
		}

		if (basicOperator instanceof Optional ||
				// include special case:
				basicOperator.getClass().getPackage().getName().compareTo(
				"lupos.engine.operators.multiinput.optional.parallel") == 0) {
			set = mapStartNodes.get(Optional.class);
			if (set == null) {
				return false;
			}
			if (!set.contains(basicOperator))
				return false;
		}
		return true;
	}

	private static Map<Class<? extends BasicOperator>, Set<BasicOperator>> createStartNodeMap(
			final BasicOperator op) {
		final Map<Class<? extends BasicOperator>, Set<BasicOperator>> mapStartNodes = new HashMap<Class<? extends BasicOperator>, Set<BasicOperator>>();

		op.visit(new SimpleOperatorGraphVisitor() {

			public Object visit(final BasicOperator basicOperator) {
				RuleEngine.addToNodeMap(basicOperator, mapStartNodes);
				return null;
			}

		});

		return mapStartNodes;
	}

//	public class JProgressBarDemo extends JFrame {
//		JProgressBar bar = new JProgressBar(0, 1000000);
//
//		public JProgressBarDemo() {
//			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			final JButton but = new JButton("Start z�hlen");
//			but.addActionListener(new ButtonActionListener());
//			add(bar, BorderLayout.PAGE_START);
//			add(but, BorderLayout.PAGE_END);
//			pack();
//			bar.setEnabled(false);
//		}
//
//		private class ButtonActionListener implements ActionListener {
//			public void actionPerformed(final ActionEvent e) {
//				new Thread() {
//					@Override
//					public void run() {
//						for (int i = 1; i <= bar.getMaximum(); ++i) {
//							final int j = i;
//							SwingUtilities.invokeLater(new Runnable() {
//								public void run() {
//									bar.setValue(j);
//								}
//							});
//						}
//					}
//				}.start();
//			}
//		}
//	}

}
