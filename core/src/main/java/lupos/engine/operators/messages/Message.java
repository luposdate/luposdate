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

	protected static int lastId;
	protected int id;
	protected HashSet<BasicOperator> visited = new HashSet<BasicOperator>();

	public boolean hasVisited(final BasicOperator op) {
		return visited.contains(op);
	}

	public void setVisited(final BasicOperator op) {
		visited.add(op);
	}

	public Message() {
		id = ++lastId;
	}

	public Message(final Message msg) {
		id = msg.id;
		visited = (HashSet<BasicOperator>) msg.visited.clone();
	}

	public abstract Message postProcess(BasicOperator op);

	public abstract Message preProcess(BasicOperator op);
	
	public abstract Message merge(Collection<Message> msgs, BasicOperator op);

	public String getId() {
		return this.getClass().getSimpleName() + " (Message ID " + id + ")";
	}

	@Override
	public String toString() {
		return getId();
	}

	@Override
	public abstract Message clone();
	
	public Message postProcessDebug(final BasicOperator op,
			final DebugStep debugstep) {
		return postProcess(op);
	}

	public Message preProcessDebug(final BasicOperator op,
			final DebugStep debugstep) {
		return preProcess(op);
	}
}
