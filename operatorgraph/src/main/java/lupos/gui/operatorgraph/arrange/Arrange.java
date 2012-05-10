package lupos.gui.operatorgraph.arrange;

import lupos.gui.operatorgraph.OperatorGraph;

public enum Arrange {
	LAYERED() {

		@Override
		public void arrange(final OperatorGraph operatorgraph,
				final boolean flipX, final boolean flipY, final boolean rotate) {
			LayeredDrawing.arrange(operatorgraph, flipX, flipY, rotate);
		}

		@Override
		public String toString() {
			return "Layered Drawing";
		}

	};

	public abstract void arrange(final OperatorGraph operatorgraph,
			final boolean flipX, final boolean flipY, final boolean rotate);
}
