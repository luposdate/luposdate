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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.runtime;

import java.util.LinkedList;
import java.util.List;

import lupos.engine.operators.BasicOperator;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.optimizations.logical.rules.DebugContainer;

public class RulePackage extends AbstractRulePackage {
	public void applyRules(BasicOperator rootOp) {
		while(true) {
			boolean end = true;

			for(Rule rule : this.rules) {
				if(rule.apply(rootOp)) {
					end = false;

					break;
				}
			}

			if(end) {
				break;
			}
		}
	}

	public List<DebugContainer<BasicOperatorByteArray>> applyRulesDebugByteArray(BasicOperator rootOp, Prefix prefixInstance) {
		List<DebugContainer<BasicOperatorByteArray>> debug = new LinkedList<DebugContainer<BasicOperatorByteArray>>();

		while(true) {
			boolean end = true;
			for(Rule rule : this.rules) {
				if(rule.apply(rootOp)) {
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
}