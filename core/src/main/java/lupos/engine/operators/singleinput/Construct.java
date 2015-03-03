
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
package lupos.engine.operators.singleinput;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.tripleoperator.TriplePattern;
public class Construct extends SingleInputOperator {
	private static final long serialVersionUID = 1L;
	protected Collection<TriplePattern> ctp;
	
	protected int id=0; // used for reification of blank nodes...
	protected boolean containsBlankNodes = false;

	/**
	 * <p>setTemplates.</p>
	 *
	 * @param ctp a {@link java.util.Collection} object.
	 */
	public void setTemplates(final Collection<TriplePattern> ctp) {
		this.ctp = ctp;
		for(TriplePattern tp: ctp){
			for(Item item: tp){
				if(item instanceof AnonymousLiteral){
					containsBlankNodes=true;
				} else if(item instanceof LazyLiteral){
					if(((LazyLiteral)item).getLiteral() instanceof LazyLiteral){
						containsBlankNodes=true;
					}
				}
			}
		}
	}

	/**
	 * <p>getTemplates.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<TriplePattern> getTemplates() {
		return ctp;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult qr, final int operandID) {
		if(this.containsBlankNodes){
			// do reification of blank nodes!
			final GraphResult gr = new GraphResult();
			for(Bindings b: qr){
				Collection<TriplePattern> ctp_new = new LinkedList<TriplePattern>();
				for(TriplePattern tp: ctp){
					TriplePattern tp_new=new TriplePattern(reificate(tp.getItems()[0]), reificate(tp.getItems()[1]), reificate(tp.getItems()[2]));
					ctp_new.add(tp_new);
				}
				gr.setTemplate(ctp_new);
				gr.add(b);
				id++;
			}
			qr.release();
			return gr;
		} else {
			final GraphResult gr = new GraphResult(ctp);
			gr.add(qr);
			qr.release();
			return gr;
		}
	}

	private Item reificate(Item item) {
		if(item instanceof LazyLiteral)
			item=((LazyLiteral)item).getLiteral();
		if(item instanceof AnonymousLiteral){
			return LiteralFactory.createAnonymousLiteral(((AnonymousLiteral)item).toString()+"_reificated_"+id);
		} else return item;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString() + ctp;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString(lupos.rdf.Prefix prefixInstance) {
		StringBuffer result = new StringBuffer("Construct: ");

		for(TriplePattern tp : this.ctp) {
			result.append(tp.toString(prefixInstance)).append("\n");
		}

		return result.toString();
	}
}
