package lupos.engine.indexconstruction.implementation;

import java.net.URISyntaxException;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.engine.indexconstruction.interfaces.ITripleConsumerWithEndNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringShortener implements ITripleConsumerWithEndNotification {

	private static final Logger log = LoggerFactory.getLogger(StringShortener.class);

	public static int LENGTH_LIMIT_OF_STRINGS = 8000;

	private final ITripleConsumerWithEndNotification tripleConsumer;

	public StringShortener(final ITripleConsumerWithEndNotification tripleConsumer){
		this.tripleConsumer = tripleConsumer;
	}

	@Override
	public void consume(final Triple triple) {
		for(int i=0; i<3; i++){
			if(triple.getPos(i).toString().length()>StringShortener.LENGTH_LIMIT_OF_STRINGS){
				final Literal literal = triple.getPos(i);
				if(literal instanceof StringURILiteral){
					final StringURILiteral sLiteral = (StringURILiteral) literal;
					try {
						triple.setPos(i, LiteralFactory.createURILiteral("<"+sLiteral.getString().substring(0, StringShortener.LENGTH_LIMIT_OF_STRINGS-2)+">"));
					} catch (final URISyntaxException e) {
						log.error(e.getMessage(), e);
					}
				} else if(literal instanceof TypedLiteral){
					final TypedLiteral typedLiteral = (TypedLiteral) literal;
					final int newlength = StringShortener.LENGTH_LIMIT_OF_STRINGS-typedLiteral.getType().length()-1;
					try {
						if(newlength<0){
							triple.setPos(i, LiteralFactory.createTypedLiteral(typedLiteral.getContent().substring(0, Math.min(typedLiteral.getContent().length()-1, StringShortener.LENGTH_LIMIT_OF_STRINGS/2))+"\"", typedLiteral.getType().substring(0, StringShortener.LENGTH_LIMIT_OF_STRINGS/2)));
						} else {
							triple.setPos(i, LiteralFactory.createTypedLiteral(typedLiteral.getContent().substring(0, newlength)+"\"", typedLiteral.getType()));
						}
					} catch (final URISyntaxException e) {
						log.error(e.getMessage(), e);
					}
				} else if(literal.isSimpleLiteral()){
					triple.setPos(i, LiteralFactory.createStringLiteral(triple.getPos(i).toString().substring(0, StringShortener.LENGTH_LIMIT_OF_STRINGS - 1)+"\""));
				} else if(literal.isLanguageTaggedLiteral()){
					final LanguageTaggedLiteral ltl = (LanguageTaggedLiteral) literal;
					final int newLength = StringShortener.LENGTH_LIMIT_OF_STRINGS-ltl.getLanguage().length()-1;
					if(newLength<0){
						triple.setPos(i, LiteralFactory.createLanguageTaggedLiteral(ltl.getContent().substring(0, Math.min(ltl.getContent().length()-1, StringShortener.LENGTH_LIMIT_OF_STRINGS/2))+"\"", ltl.getLanguage().substring(0,  StringShortener.LENGTH_LIMIT_OF_STRINGS/2 - 1)));
					} else {
						triple.setPos(i, LiteralFactory.createLanguageTaggedLiteral(ltl.getContent().substring(0, newLength)+"\"", ltl.getLanguage()));
					}
				}
			}
		}
		this.tripleConsumer.consume(triple);
	}

	@Override
	public void notifyEndOfProcessing() {
		this.tripleConsumer.notifyEndOfProcessing();
	}
}
