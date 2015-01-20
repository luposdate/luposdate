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
/**
 *
 */
package lupos.engine.operators.singleinput.sort;

import java.util.Collection;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings;

/**
 * This is almost an abstract class, but as it needs to be
 * instantiated in operatorPipe it is not. Nevertheless it should not be
 * instantiated, as no useful results will be created. 
 * DO ONLY USE EXTENDING CLASSES
 */
public class Sort extends SingleInputOperator {

	protected ComparatorBindings comparator; // Comparator which compares in order to sort

	public Sort() {
	}

	/**
	 * Contructor
	 * 
	 * @param node
	 *            the current sort node. From this node all other informations
	 *            like variables to sort after will be extracted.
	 */
	public Sort(final lupos.sparql1_1.Node node) {
		comparator = new ComparatorAST(node);
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
		comparator = ((Sort) op).getComparator();
	}

	public ComparatorBindings getComparator() {
		return comparator;
	}

	public void setComparator(ComparatorBindings comparator) {
		this.comparator=comparator;
	}

	@Override
	public String toString() {
		return super.toString() + "\nSortcriterium:" + getSortCriterium();
	}

	public Collection<Variable> getSortCriterium() {
		return getComparator().getSortCriterium();
	}
}
