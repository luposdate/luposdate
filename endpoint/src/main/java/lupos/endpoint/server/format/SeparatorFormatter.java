package lupos.endpoint.server.format;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.URILiteral;

public abstract class SeparatorFormatter extends HeadBodyFormatter{
	
	public SeparatorFormatter(String formatName) {
		super(formatName);
	}
	
	public abstract void writeSeparator(OutputStream os) throws IOException;

	@Override
	public void writeBooleanResult(OutputStream os, boolean result)
			throws IOException {
		os.write(Boolean.toString(result).getBytes());
	}

	@Override
	public void writeStartHead(OutputStream os) throws IOException {		
	}

	@Override
	public void writeFirstVariableInHead(final OutputStream os, final Variable v)
			throws IOException {
		os.write(v.getName().getBytes());
	}
	
	@Override
	public void writeVariableInHead(OutputStream os, Variable v)
			throws IOException {
		this.writeSeparator(os);
		this.writeFirstVariableInHead(os, v);
	}

	@Override
	public void writeEndHead(OutputStream os) throws IOException {
		os.write("\n".getBytes());
	}

	@Override
	public void writeStartResult(OutputStream os) throws IOException {
	}

	@Override
	public void writeEndResult(OutputStream os) throws IOException {
		os.write("\n".getBytes());
	}

	@Override
	public void writeEpilogue(OutputStream os) throws IOException {
	}

	@Override
	public void writeFirstStartBinding(final OutputStream os, Variable v) throws IOException {		
	}
	
	@Override
	public void writeStartBinding(OutputStream os, Variable v) throws IOException {
		this.writeSeparator(os);
	}

	@Override
	public void writeEndBinding(OutputStream os) throws IOException {
	}

	@Override
	public void writeBlankNode(OutputStream os, AnonymousLiteral blankNode) throws IOException {
		os.write(blankNode.originalString().getBytes());
	}

	@Override
	public void writeURI(OutputStream os, URILiteral uri) throws IOException {
		os.write(uri.originalString().getBytes());
	}

	@Override
	public void writeSimpleLiteral(OutputStream os, Literal literal) throws IOException {
		os.write(literal.originalString().getBytes());
	}

	@Override
	public void writeTypedLiteral(OutputStream os, TypedLiteral literal) throws IOException {
		os.write(literal.originalString().getBytes());
	}

	@Override
	public void writeLanguageTaggedLiteral(OutputStream os, LanguageTaggedLiteral literal) throws IOException {
		os.write(literal.originalString().getBytes());
	}
	
	public Collection<Variable> getVariablesToIterateOnForOneBindings(Set<Variable> variables, Bindings bindings){
		return variables;
	}
}
