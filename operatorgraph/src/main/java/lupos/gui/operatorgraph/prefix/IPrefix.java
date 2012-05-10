package lupos.gui.operatorgraph.prefix;

public interface IPrefix {
	public void prefixRemoved(String prefix, String namespace);

	public void prefixAdded();

	public void prefixModified(String oldPrefix, String newPrefix);
}