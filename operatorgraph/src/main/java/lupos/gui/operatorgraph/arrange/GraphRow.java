package lupos.gui.operatorgraph.arrange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

public class GraphRow {
	private final List<GraphBox> boxes; // holds the boxes of the row
	private final int y;
	private int height = 0;
	private final OperatorGraph parent;

	public GraphRow(final OperatorGraph parent, final int y) {
		this.parent = parent;
		this.y = y;
		this.boxes = new LinkedList<GraphBox>();
	}

	public GraphRow(final OperatorGraph parent, final int y, final int initialSize) {
		this.parent = parent;
		this.y = y;
		this.boxes = new ArrayList<GraphBox>(initialSize);
	}

	/**
	 * Adds a GraphBox to the current row.
	 * 
	 * @param b
	 *            box to add
	 */
	public void add(final GraphBox b) {

		// set new box start position...
		if(this.boxes.isEmpty()) { // no box in the row, yet...
			b.setX((int) Math.ceil(this.parent.PADDING));
		}
		else { // already boxes in the row...
			// get last box in row...
			final GraphBox last = this.boxes.get(this.boxes.size() - 1);

			b.setX(last.getX() + last.width + (int) Math.ceil(this.parent.getSPACING()));
		}

		b.setY(this.y); // move box in y-direction into the row

		this.boxes.add(b); // add new box to boxes list

		// set row height to highest height in the row...
		this.height = Math.max(this.height, b.height);
	}

	public void addAllWithoutUpdatingParentsSize(final Collection<GraphWrapper> cgw, final Map<GraphWrapper, GraphBox> boxesMap) {
		// last box in the row
		GraphBox last = (this.boxes.isEmpty()) ? null : this.boxes.get(this.boxes.size() - 1);

		for(final GraphWrapper gw : cgw) {
			final GraphBox b = boxesMap.get(gw);

			// set new box start position...
			if(last == null) { // no box in the row, yet...
				b.setXWithoutUpdatingParentsSize((int) Math.ceil(this.parent.PADDING));
			}
			else { // already boxes in the row...
				b.setXWithoutUpdatingParentsSize(last.getX() + last.width + (int) Math.ceil(this.parent.getSPACING()));
			}

			b.setYWithoutUpdatingParentsSize(this.y); // move box in y-direction into the row

			this.boxes.add(b); // add new box to boxes list

			// set row height to highest height in the row
			this.height = Math.max(this.height, b.height);

			last = b;
		}
	}

	/**
	 * Centers the current row.
	 * 
	 * @param center
	 *            the center of the largest row
	 */
	public void center(final int center) {
		final int offset = center - (this.getWidth() / 2);

		// walk through boxes of the row...
		for(final GraphBox box : this.boxes) {
			box.setX(box.getX() + offset);
		}
	}

	public void centerWithoutUpdatingParentsSize(final int center) {
		final int offset = center - (this.getWidth() / 2);

		// walk through boxes of the row...
		for(final GraphBox box : this.boxes) {
			box.setXWithoutUpdatingParentsSize(box.getX() + offset);
		}
	}

	public List<GraphBox> getBoxes() {
		return this.boxes;
	}

	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		GraphBox mostLeft = this.boxes.get(0);
		GraphBox mostRight = this.boxes.get(0);

		for(int i = 1; i < this.boxes.size(); ++i) {
			final GraphBox tmpBox = this.boxes.get(i);

			if(tmpBox.getX() < mostLeft.getX()) {
				mostLeft = tmpBox;
			}

			if(tmpBox.getX() + tmpBox.width > mostRight.getX() + mostRight.width) {
				mostRight = tmpBox;
			}
		}

		final int mostRightPosition = mostRight.getX() + mostRight.width;

		return mostRightPosition - mostLeft.getX() + 2 * (int) Math.ceil(this.parent.getSPACING());
	}

	public void removeBox(final GraphBox box) {
		this.boxes.remove(box);
	}

	public int getY() {
		return this.y;
	}
}