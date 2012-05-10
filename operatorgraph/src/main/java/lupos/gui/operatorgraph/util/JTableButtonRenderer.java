package lupos.gui.operatorgraph.util;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class JTableButtonRenderer implements TableCellRenderer {
	private final TableCellRenderer __defaultRenderer;

	public JTableButtonRenderer(final TableCellRenderer renderer) {
		__defaultRenderer = renderer;
	}

	public Component getTableCellRendererComponent(final JTable table,
			final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		if (value instanceof Component) {
			return (Component) value;
		}
		return __defaultRenderer.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
	}
}
