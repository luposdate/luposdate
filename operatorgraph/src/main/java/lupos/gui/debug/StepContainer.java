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
package lupos.gui.debug;

import lupos.engine.operators.BasicOperator;

/**
 * The class StepContainer just contains all neccessary values of a single step
 * in order to reproduce it through the use of the RingBuffer
 *
 * @author Markus & Peter
 * @version $Id: $Id
 */
public class StepContainer {
	private final BasicOperator from;
	private final BasicOperator to;
	private final Object object;
	private final boolean deleteStep;

	/**
	 * Constructor
	 *
	 * @param from a {@link lupos.engine.operators.BasicOperator} object.
	 * @param to a {@link lupos.engine.operators.BasicOperator} object.
	 * @param object a {@link java.lang.Object} object.
	 * @param deleteStep a boolean.
	 */
	public StepContainer(final BasicOperator from, final BasicOperator to,
			final Object object, final boolean deleteStep) {
		super();
		this.from = from;
		this.to = to;
		this.object = object;
		this.deleteStep = deleteStep;
	}

	/**
	 * <p>Getter for the field <code>from</code>.</p>
	 *
	 * @return a {@link lupos.engine.operators.BasicOperator} object.
	 */
	public BasicOperator getFrom() {
		return from;
	}

	/**
	 * <p>Getter for the field <code>to</code>.</p>
	 *
	 * @return a {@link lupos.engine.operators.BasicOperator} object.
	 */
	public BasicOperator getTo() {
		return to;
	}

	/**
	 * <p>Getter for the field <code>object</code>.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * <p>isDeleteStep.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDeleteStep() {
		return deleteStep;
	}
}
