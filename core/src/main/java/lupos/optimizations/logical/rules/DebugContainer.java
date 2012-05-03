package lupos.optimizations.logical.rules;


public class DebugContainer<T> {

	private String ruleName;
	private String description;
	private T root;

	/**
	 * @param description
	 * @param root
	 * @param ruleName
	 */
	public DebugContainer(final String ruleName, final String description,
			final T root) {
		this.description = description;
		this.root = root;
		this.ruleName = ruleName;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(final String ruleName) {
		this.ruleName = ruleName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public T getRoot() {
		return root;
	}

	public void setRoot(final T root) {
		this.root = root;
	}

}
