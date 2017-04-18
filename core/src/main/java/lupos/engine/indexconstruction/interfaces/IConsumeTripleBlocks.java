package lupos.engine.indexconstruction.interfaces;

public interface IConsumeTripleBlocks extends IJoiner, IEndOfProcessingNotification {
	public void consumeTriplesBlocks(int[][] triples, int index, int maxID, int runNumber);
}
