package lupos.rdf;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import lupos.engine.operators.tripleoperator.TripleConsumer;

public class JenaTurtleTripleConsumerPipeRememberingPrefixes extends
		JenaTurtleTripleConsumerPipe {

	private final HashMap<String, String> map = new HashMap<String, String>();

	public JenaTurtleTripleConsumerPipeRememberingPrefixes(final Reader arg0,
			final TripleConsumer tc) {
		super(arg0, tc);
		this.setEventHandler(new TripleConsumerHandler(tc) {

			@Override
			public void prefix(final int arg0, final int arg1,
					final String arg2, final String arg3) {
				map.put(arg2, arg3);
			}

		});
	}

	public Map<String, String> getPrefixMap() {
		return map;
	}
}
