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
package lupos.gui.operatorgraph.viewer;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperPrefixNonEditable;
import lupos.gui.operatorgraph.prefix.Prefix;
import xpref.datatypes.BooleanDatatype;

public class OperatorGraphWithPrefix extends OperatorGraph {
	private static final long serialVersionUID = 902876317608720839L;

	private Prefix prefix = null;

	public OperatorGraphWithPrefix() {
		super();
		try {
			this.prefix = new ViewerPrefix(BooleanDatatype.getValues(
					"operatorGraph_usePrefixes").get(0).booleanValue());
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public OperatorGraphWithPrefix(final ViewerPrefix prefix) {
		super();
		try {
			this.prefix = prefix;
			this.prefix.setStatus(BooleanDatatype.getValues(
					"operatorGraph_usePrefixes").get(0).booleanValue());
		} catch (final Exception e) {
			this.prefix.setStatus(true);
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public void setPrefixStatus(final boolean status) {
		this.prefix.setStatus(status);
	}

	public Prefix getPrefix() {
		return this.prefix;
	}

	@Override
	public synchronized void arrange(final Arrange arrange) {
		final GraphWrapper prefixGW = new GraphWrapperPrefixNonEditable(this.prefix);

		if (this.boxes.containsKey(prefixGW)) {
			final GraphBox oldBox = this.boxes.remove(prefixGW);
			this.remove(oldBox.getElement());
		}

		super.arrange(arrange);

		if (this.prefix == null
				|| !this.prefix.isActive()
				|| (this.rootList.size() > 0 && !this.rootList.get(0).usePrefixesActive())) {
			return;
		}

		final GraphBox prefixBox = this.graphBoxCreator.createGraphBox(this, prefixGW);
		prefixBox.setX(2 * (int) Math.ceil(this.PADDING));
		prefixBox.setY(this.getMaxY()
				+ (int) Math.ceil(this.SPACING_Y));
		prefixBox.arrange(arrange);

		this.boxes.put(prefixGW, prefixBox);

		this.updateSize();
	}
}