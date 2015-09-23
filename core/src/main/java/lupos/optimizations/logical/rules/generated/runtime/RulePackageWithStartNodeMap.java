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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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
