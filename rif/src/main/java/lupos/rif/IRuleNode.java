
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

import java.util.List;
public interface IRuleNode {
	/**
	 * <p>getParent.</p>
	 *
	 * @return a {@link lupos.rif.IRuleNode} object.
	 */
	IRuleNode getParent();

	/**
	 * <p>setParent.</p>
	 *
	 * @param parent a {@link lupos.rif.IRuleNode} object.
	 */
	void setParent(IRuleNode parent);

	/**
	 * <p>getChildren.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	List<IRuleNode> getChildren();
	
	/**
	 * <p>getLabel.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	String getLabel();

	/**
	 * <p>accept.</p>
	 *
	 * @param visitor a {@link lupos.rif.IRuleVisitor} object.
	 * @param arg a A object.
	 * @param <R> a R object.
	 * @param <A> a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	<R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException;
}
