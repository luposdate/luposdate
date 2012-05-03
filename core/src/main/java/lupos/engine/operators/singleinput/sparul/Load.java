package lupos.engine.operators.singleinput.sparul;

import java.util.Collection;

import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Indices;

public class Load extends MultipleURIOperator {

	// the URI into which the data is loaded, is null in the case of the default
	// graph...
	protected URILiteral into;
	protected final Dataset dataset;
	protected final boolean isSilent;

	public Load(final Collection<URILiteral> cu, final URILiteral into, Dataset dataset, final boolean isSilent) {
		super(cu);
		this.into = into;
		this.dataset=dataset;
		this.isSilent = isSilent;
	}

	public void setInto(final URILiteral into) {
		this.into = into;
	}

	public URILiteral getInto() {
		return into;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (into != null)
			System.err.println("Named graphs currently not supported!");
		else {
			try {
				for (final URILiteral u : cu) {
					final Indices indices = dataset.getIndicesFactory().createIndices((into==null)?u:into);
					dataset.indexingRDFGraph(u, indices, false, false);
					if (into == null) {
						// default graph!
						dataset.putIntoDefaultGraphs(u, indices);
					} else {
						dataset.putIntoNamedGraphs(into, indices);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
				System.err.println(e);
				if(!isSilent)
					throw new Error("Error while loading: "+e.getMessage());
			}
		}
		this.dataset.buildCompletelyAllIndices();
		return null;
	}

	@Override
	public String toString() {
		String s = super.toString() + cu;
		if (into != null)
			s += " into " + into;
		return s;
	}
}
