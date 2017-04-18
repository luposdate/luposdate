package lupos.engine.indexconstruction.interfaces;

import java.util.Map;

public interface ICreateReadTriplesFactory {
	public IReadTriples createReadTriples(Map<String, Object> configuration, ITripleConsumerWithEndNotification[] tripleConsumers, IEndOfProcessingNotification secondPhase) throws Exception;
}
