/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.event.action;

import java.util.Collection;
import java.util.Set;
import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.action.send.Send;
import lupos.event.action.send.SlidingWindow;

public class SlideOutAction extends Action {

	private Send slidingWindow = new SlidingWindow();

	/**
	 * CONSTRUCTOR!
	 */
	public SlideOutAction() {
		super("SlideOutAction");
		this.slidingWindow.init();
	}

	protected SlideOutAction(String name) {
		super(name);
		this.slidingWindow.init();
	}

	@Override
	public void execute(QueryResult queryResult) {
		String msg = getMessage(queryResult);
		this.slidingWindow.sendContent(msg);
	}

	protected String getMessage(QueryResult qr) {
		String msg = new String();

		Set<Variable> vars = qr.getVariableSet();
		Collection<Bindings> bindings = qr.getCollection();

		// This looks awful. I know. Don't know how to do this properly with
		// generic Collections/Sets though ...
		Object[] bArray = bindings.toArray();
		Object[] vArray = vars.toArray();
		for (int i = 0; i < bArray.length; i++) {
			Bindings b = (Bindings) bArray[i];
			
			// separate bindings by newlines
			if (i > 0) {
				msg += '\n';
			}

			for (int j = 0; j < vArray.length; j++) {
				Variable v = (Variable) vArray[j];

				// separate variables in a binding by semicolon
				if (j > 0) {
					msg += "; ";
				}
				msg += b.get(v).originalString();
			}
		}

		return msg;
	}
}