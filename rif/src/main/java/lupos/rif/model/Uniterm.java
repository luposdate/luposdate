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
package lupos.rif.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.rdf.Prefix;
import lupos.rif.IExpression;

public abstract class Uniterm extends AbstractRuleNode implements IExpression {
	public IExpression termName;
	public List<IExpression> termParams = new ArrayList<IExpression>();

	public Uniterm() {
		super();
	}

	public boolean containsOnlyVariables() {
		return false;
	}

	public Set<RuleVariable> getVariables() {
		Set<RuleVariable> vars = new HashSet<RuleVariable>();
		vars.addAll(termName.getVariables());
		for (IExpression expr : termParams)
			vars.addAll(expr.getVariables());
		return vars;
	}

	public List<Uniterm> getPredicates() {
		return Arrays.asList(this);
	}

	public String getLabel() {
		final StringBuffer str = new StringBuffer();
		str.append(termName.toString()).append("(");
		for (int idx = 0; idx < termParams.size(); idx++) {
			str.append(termParams.get(idx).toString());
			if (idx < termParams.size() - 1)
				str.append(", ");
			else
				str.append(")");
		}
		if (str.substring(str.length()) != ")")
			str.append(")");
		return str.toString();
	}

	public String toString(Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer();
		str.append(termName.toString(prefixInstance)).append("(");
		for (int idx = 0; idx < termParams.size(); idx++) {
			str.append(termParams.get(idx).toString(prefixInstance));
			if (idx < termParams.size() - 1)
				str.append(", ");
			else
				str.append(")");
		}
		if (str.substring(str.length()) != ")")
			str.append(")");
		return str.toString();
	}

	public abstract boolean equalsDataStructure(Object obj);
}
