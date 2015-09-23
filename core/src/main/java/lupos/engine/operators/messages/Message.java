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
package lupos.engine.operators.messages;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import lupos.engine.operators.BasicOperator;
import lupos.misc.debug.DebugStep;
public abstract class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3183344099291306589L;

	/** Constant <code>lastId=</code> */
	protected static int lastId;
	protected int id;
	protected HashSet<BasicOperator> visited = new HashSet<BasicOperator>();

	/**
	 * <p>hasVisited.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 * @return a boolean.
	 */
	public boolean hasVisited(final BasicOperator op) {
		return visited.contains(op);
	}

	/**
	 * <p>Setter for the field <code>visited</code>.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 */
	public void setVisited(final BasicOperator op) {
		visited.add(op);
	}

	/**
	 * <p>Constructor for Message.</p>
	 */
	public Message() {
		id = ++lastId;
	}

	/**
	 * <p>Constructor for Message.</p>
	 *
	 * @param msg a {@link lupos.engine.operators.messages.Message} object.
	 */
	public Message(final Message msg) {
		id = msg.id;
		visited = (HashSet<BasicOperator>) msg.visited.clone();
	}

	/**
	 * <p>postProcess.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 * @return a {@link lupos.engine.operators.messages.Message} object.
	 */
	public abstract Message postProcess(BasicOperator op);

	/**
	 * <p>preProcess.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 * @return a {@link lupos.engine.operators.messages.Message} object.
	 */
	public abstract Message preProcess(BasicOperator op);
	
	/**
	 * <p>merge.</p>
	 *
	 * @param msgs a {@link java.util.Collection} object.
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 * @return a {@link lupos.engine.operators.messages.Message} object.
	 */
	public abstract Message merge(Collection<Message> msgs, BasicOperator op);

	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() {
		return this.getClass().getSimpleName() + " (Message ID " + id + ")";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getId();
	}

	/** {@inheritDoc} */
	@Override
	public abstract Message clone();
	
	/**
	 * <p>postProcessDebug.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 * @param debugstep a {@link lupos.misc.debug.DebugStep} object.
	 * @return a {@link lupos.engine.operators.messages.Message} object.
	 */
	public Message postProcessDebug(final BasicOperator op,
			final DebugStep debugstep) {
		return postProcess(op);
	}

	/**
	 * <p>preProcessDebug.</p>
	 *
	 * @param op a {@link lupos.engine.operators.BasicOperator} object.
	 * @param debugstep a {@link lupos.misc.debug.DebugStep} object.
	 * @return a {@link lupos.engine.operators.messages.Message} object.
	 */
	public Message preProcessDebug(final BasicOperator op,
			final DebugStep debugstep) {
		return preProcess(op);
	}
}
