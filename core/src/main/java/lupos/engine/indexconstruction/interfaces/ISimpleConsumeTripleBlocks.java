package lupos.engine.indexconstruction.interfaces;

public interface ISimpleConsumeTripleBlocks extends IEndOfProcessingNotification {
	public void consumeTriplesBlocks(int[][] triples, int index);
}
