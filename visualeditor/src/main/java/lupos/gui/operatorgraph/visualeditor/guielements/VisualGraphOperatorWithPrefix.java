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
package lupos.gui.operatorgraph.visualeditor.guielements;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.DummyItem;
import lupos.gui.operatorgraph.visualeditor.util.VEPrefix;
public abstract class VisualGraphOperatorWithPrefix extends VisualGraphOperator {
	private static final long serialVersionUID = 8089073150957057614L;

	/**
	 * Instance of the prefix class which handles the prefixes and the
	 * name-spaces.
	 */
	public Prefix prefix = null;

	/**
	 * <p>Constructor for VisualGraphOperatorWithPrefix.</p>
	 *
	 * @param visualEditor a {@link lupos.gui.operatorgraph.visualeditor.VisualEditor} object.
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 */
	public VisualGraphOperatorWithPrefix(VisualEditor<Operator> visualEditor, Prefix prefix) {
		super(visualEditor);

		this.prefix = prefix;

		this.construct();
	}
	
	/**
	 * <p>newInstance.</p>
	 *
	 * @param visualEditor a {@link lupos.gui.operatorgraph.visualeditor.VisualEditor} object.
	 * @param prefix a {@link lupos.gui.operatorgraph.prefix.Prefix} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix} object.
	 */
	public abstract VisualGraphOperatorWithPrefix newInstance(VisualEditor<Operator> visualEditor, Prefix prefix);

	/**
	 * <p>clearAll.</p>
	 */
	public void clearAll() {
		this.prefix = new VEPrefix(true);

		this.clear();
	}

	/** {@inheritDoc} */
	protected Operator createOperator(Class<? extends Operator> clazz, Item content) throws Exception {
		// get some class names...
		String newClassName = clazz.getSimpleName();

		// get the chosen operator...
		Operator newOp = null;

		try {
			if(newClassName.endsWith("RDFTerm") && !(content instanceof DummyItem)) {
				newOp = clazz.getDeclaredConstructor(Prefix.class, Item.class).newInstance(this.prefix, content);
			}
			else {
				newOp = clazz.getDeclaredConstructor(Prefix.class).newInstance(this.prefix);
			}
		}
		catch(NoSuchMethodException nsme) {
			newOp = clazz.newInstance();
		}

		return newOp;
	}
}
