package lupos.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import lupos.datastructures.items.Triple;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.tripleoperator.TripleConsumer;

public class TestParser {

	public static class StringConsumer {
		public void consumeString(final String str){
			System.out.println(str);
		}
	}

	public static void main(final String[] args) throws Exception {
		final String filename = (args.length>0)? args[0] : "../gui/src/main/resources/data/sp2b.n3";
		final InputStream in = new BufferedInputStream(new FileInputStream(filename));
		final StringConsumer consumer = new StringConsumer();
		CommonCoreQueryEvaluator.readTriples("N3", in, new TripleConsumer(){
			@Override
			public void consume(final Triple triple) {
				consumer.consumeString(triple.getSubject().toString());
				consumer.consumeString(triple.getPredicate().toString());
				consumer.consumeString(triple.getObject().toString());
			}

		});
	}
}
