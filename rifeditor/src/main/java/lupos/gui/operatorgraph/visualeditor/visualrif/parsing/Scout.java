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
package lupos.gui.operatorgraph.visualeditor.visualrif.parsing;

import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RuleList;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.RuleVariable;
public class Scout implements IRuleVisitor<Object, Object>{

	final static int DOCUMENT = 0;
	final static int RULE = 1;
	final static int EXISTEXPRESSION = 2;
	final static int CONJUNCTION = 3;
	final static int DISJUNCTION = 4;
	final static int RULEPREDICATE = 5;
	final static int EQUALITY = 6;
	final static int EXTERNAL = 7;
	final static int RULELIST = 8;
	final static int RULEVARIABLE = 9;
	final static int CONSTANT = 10;
	
	/** {@inheritDoc} */
	@Override
	public Object visit(Document obj, Object arg) throws RIFException {
		
		return  DOCUMENT;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(Rule obj, Object arg) throws RIFException {
		
		return RULE;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(ExistExpression obj, Object arg) throws RIFException {

		return EXISTEXPRESSION;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(Conjunction obj, Object arg) throws RIFException {
		
		return CONJUNCTION;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(Disjunction obj, Object arg) throws RIFException {
		
		return DISJUNCTION;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(RulePredicate obj, Object arg) throws RIFException {

		return RULEPREDICATE;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(Equality obj, Object arg) throws RIFException {

		return EQUALITY;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(External obj, Object arg) throws RIFException {

		return EXTERNAL;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(RuleList obj, Object arg) throws RIFException {

		return RULELIST;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(RuleVariable obj, Object arg) throws RIFException {

		return RULEVARIABLE;
	}

	/** {@inheritDoc} */
	@Override
	public Object visit(Constant obj, Object arg) throws RIFException {

		return CONSTANT;
	}

}
