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

	public Collection<TriplePattern> getTemplates() {
		return ctp;
	}

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

	@Override
	public String toString() {
		return super.toString() + ctp;
	}
	
	@Override
	public String toString(lupos.rdf.Prefix prefixInstance) {
		StringBuffer result = new StringBuffer("Construct: ");

		for(TriplePattern tp : this.ctp) {
			result.append(tp.toString(prefixInstance)).append("\n");
		}

		return result.toString();
	}
}
