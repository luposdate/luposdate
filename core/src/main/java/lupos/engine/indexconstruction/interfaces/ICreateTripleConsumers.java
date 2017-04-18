package lupos.engine.indexconstruction.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lupos.misc.Tuple;

public interface ICreateTripleConsumers {
	public Tuple<ITripleConsumerWithEndNotification[], IEndOfProcessingNotification> createTripleConsumers(final Map<String, Object> configuration, final List<Tuple<String, Long>> times) throws IOException;
}
