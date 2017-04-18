package lupos.engine.indexconstruction.interfaces;


public interface IIndicesGenerator {
	public void generateIndicesAndWriteOut(final int size) throws Exception;
	public void notifyAllIndicesConstructed();
}
