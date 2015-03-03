
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
package lupos.rif;

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
public interface IRuleVisitor<R, A> {
	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Document} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(Document obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Rule} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(Rule obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.ExistExpression} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(ExistExpression obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Conjunction} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(Conjunction obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Disjunction} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(Disjunction obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.RulePredicate} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(RulePredicate obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Equality} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(Equality obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.External} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(External obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.RuleList} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(RuleList obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.RuleVariable} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(RuleVariable obj, A arg) throws RIFException;

	/**
	 * <p>visit.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Constant} object.
	 * @param arg a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	R visit(Constant obj, A arg) throws RIFException;

}
