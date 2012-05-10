package lupos.gui.operatorgraph.visualeditor.guielements;

public interface Suggester<T> {
	public void makeSuggestions(T operator);
	public boolean isInSuggestionMode(T operator);
}
