/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.gui.operatorgraph.arrange;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

public final class LayeredDrawing {

	public static void updateX(final OperatorGraph operatorgraph,
			final GraphBox box,
			final HashMap<GraphWrapper, Integer> levels, final int level,
			final int x,
			final HashSet<GraphBox> visited) {
		if (visited.contains(box)) {
			return;
		}

		visited.add(box);

		final int offset = box.getX() - x;

		box.setX(x);

		// --- update position of subtree - begin ---
		for (final Object o : box.getOp().getSucceedingElements()) {
			final GraphWrapperIDTuple gwIDt = (GraphWrapperIDTuple) o;
			final GraphBox box2 = operatorgraph.getBoxes().get(
					gwIDt.getOperator());
			if (levels.get(box2.getOp()) > level) {
				updateX(operatorgraph, box2, levels, level, box2.getX()
						+ offset, visited);
			}
		}
		// --- update position of subtree - end ---
	}

	public static void updateXWithoutUpdatingParentsSize(
			final OperatorGraph operatorgraph, final GraphBox box,
			final HashMap<GraphWrapper, Integer> levels, final int level,
			final int x, final HashSet<GraphBox> visited) {
		if (visited.contains(box)) {
			return;
		}

		visited.add(box);

		final int offset = box.getX() - x;

		box.setXWithoutUpdatingParentsSize(x);

		// --- update position of subtree - begin ---
		for (final Object o : box.getOp().getSucceedingElements()) {
			final GraphWrapperIDTuple gwIDt = (GraphWrapperIDTuple) o;
			final GraphBox box2 = operatorgraph.getBoxes().get(
					gwIDt.getOperator());
			if (levels.get(box2.getOp()) > level) {
				updateXWithoutUpdatingParentsSize(operatorgraph, box2, levels,
						level, box2.getX() + offset, visited);
			}
		}
		// --- update position of subtree - end ---
	}

	protected static HashMap<GraphWrapper, Integer> generateGraph(
			final OperatorGraph operatorgraph) {
		final HashMap<GraphWrapper, Integer> levels = new HashMap<GraphWrapper, Integer>();
		// walk through the list of root elements...
		for (final GraphWrapper gw : operatorgraph.getRootList(false)) {
			depthFirstDetermineLevel(new HashSet<GraphWrapper>(),
					new HashSet<GraphWrapper>(), gw, 0, operatorgraph, levels);
			// build level tree of current root element
		}
		return levels;
	}

	@SuppressWarnings("unchecked")
	protected static void depthFirstDetermineLevel(
			final HashSet<GraphWrapper> visited,
			final HashSet<GraphWrapper> visitedNotCloned,
			final GraphWrapper op, final int level,
			final OperatorGraph operatorgraph,
			final HashMap<GraphWrapper, Integer> levels) {
		if (visited.contains(op)) { // if current operator was visited before...
			return; // abort
		}

		visited.add(op); // add current operator the list of visited

		// all operatorss have already a GraphBox...
		// if (boxes.containsKey(op)) {
			if (!visitedNotCloned.contains(op)) {
				levels.put(op, level);
			} else {
				// if the current level is higher than the level of the existing
				// GraphBox...
				if (levels.get(op) < level) {
					levels.put(op, level); // update the level of the
					// GraphBox
				} else {
					return;
				}
			}

		visitedNotCloned.add(op);

		// walk trough the children of the current operator and add them
		// recursively...
		for (final GraphWrapperIDTuple child : op.getSucceedingElements()) {
			depthFirstDetermineLevel((HashSet<GraphWrapper>) visited.clone(),
					visitedNotCloned, child.getOperator(), level + 1,
					operatorgraph, levels);
		}
	}

	public static void arrange(final OperatorGraph operatorgraph) {

		final HashMap<GraphWrapper, Integer> levels = generateGraph(operatorgraph);

		final LinkedList<GraphRow> rows = new LinkedList<GraphRow>();

		// create a HashMap which holds a List of GraphWrapper objects for each
		// level...
		final LinkedHashMap<Integer, LinkedList<GraphWrapper>> graphWrapperOfLevel = new LinkedHashMap<Integer, LinkedList<GraphWrapper>>();
		final HashMap<GraphWrapper, GraphBox> boxes = operatorgraph.getBoxes();

		// walk trough all GraphWrapper objects that have a GraphBox...
		for (final GraphWrapper gw : boxes.keySet()) {
			// get the GraphBox to the GraphWrapper...
			final int currentLevel = levels.get(gw);

			// get list of GraphWrapper objects for the level of the GraphBox...
			LinkedList<GraphWrapper> lgw = graphWrapperOfLevel
					.get(currentLevel);

			if (lgw == null) { // if list of the level is null...
				lgw = new LinkedList<GraphWrapper>(); // create list
			}

			lgw.add(gw); // add current GraphWrapper object to list

			// add list of GraphWrappers to the HashMap for the level...
			graphWrapperOfLevel.put(currentLevel, lgw);
		}

		// initialize some more variables...
		int level = 0;
		int y = (int) Math.ceil(operatorgraph.PADDING);

		// get GraphWrapper elements for the current level...
		while (graphWrapperOfLevel.get(level) != null) {
			// create a row for this level...
			final GraphRow row = new GraphRow(operatorgraph, y,
					graphWrapperOfLevel.get(
					level).size());

			rows.add(row); // add row to rows list

			// define order!
			final TreeSet<GraphWrapper> toSort = new TreeSet<GraphWrapper>(
					new Comparator<GraphWrapper>() {
						@Override
						public int compare(final GraphWrapper arg0,
								final GraphWrapper arg1) {
							if (arg0.getPrecedingElements() == null
									|| arg0.getPrecedingElements().size() == 0) {
								return -1;
							}

							if (arg1.getPrecedingElements() == null
									|| arg1.getPrecedingElements().size() == 0) {
								return 1;
							}

							final GraphWrapper parent0 = arg0
									.getPrecedingElements().get(0);
							final GraphWrapper parent1 = arg1
									.getPrecedingElements().get(0);

							if (parent0.equals(parent1)) {
								final GraphWrapperIDTuple gwidT0 = parent0
										.getGraphWrapperIDTuple(arg0);
								final GraphWrapperIDTuple gwidT1 = parent1
										.getGraphWrapperIDTuple(arg1);

								if (gwidT0.getId() == gwidT1.getId()) { // siblings
																		// !
									for (final GraphWrapperIDTuple gwidtuple : parent0
											.getSucceedingElements()) {
										if (gwidtuple.getOperator()
												.equals(arg0)) {
											return -1;
										} else if (gwidtuple.getOperator()
												.equals(arg1)) {
											return 1;
										}
									}
								} else if (gwidT0.getId() < gwidT1.getId()) {
									return -1;
								} else if (gwidT0.getId() > gwidT1.getId()) {
									return 1;
								}

								System.err
										.println("QueryGraph: GraphWrapper object not found!");

								return -1;
							} else {
								final GraphBox b0 = boxes.get(parent0);
								final GraphBox b1 = boxes.get(parent1);

								if (b0.getX() < b1.getX()) {
									return -1;
								} else {
									return 1;
								}
							}
						}
					});

			toSort.addAll(graphWrapperOfLevel.get(level));

			// add the GraphBoxes of all GraphWrappers of this level to the
			// row...
			row.addAllWithoutUpdatingParentsSize(toSort, boxes);

			// add row height and spacing height to y...
			y += row.getHeight() + operatorgraph.SPACING_Y;

			level++; // increment level
		}

		int maxRowId = 0;

		// move each box under it's preceding box / boxes...
		for (int r = 0; r < rows.size(); ++r) { // walk through rows...
			final GraphRow row = rows.get(r); // get current row

			// move first row to most left corner...
			if (levels.get(row.getBoxes().get(0).getOp()) == 0) {
				// define x value for far left box...
				int localX = (int) Math.ceil(operatorgraph.PADDING);

				// walk through boxes of the first row...
				for (final GraphBox localBox : row.getBoxes()) {
					updateXWithoutUpdatingParentsSize(operatorgraph, localBox,
							levels, level,
							localX,
							new HashSet<GraphBox>());

					// update the x value for the next box...
					localX += localBox.width + operatorgraph.PADDING;
				}
			} else {
				// center boxes under preceding boxes...

				// walk through boxes...
				for (final GraphBox box : row.getBoxes()) {
					// get preceding OPs of current box...
					final LinkedList<GraphWrapper> precOps = box.getOp()
							.getPrecedingElements();

					// remove preceding OPs from higher levels...
					for (int j = 0; j < precOps.size(); ++j) {
						if (levels.get(boxes.get(precOps.get(j)).getOp()) > levels
								.get(box.getOp())) {
							precOps.remove(j);
						}
					}

					if (precOps.size() == 0) {
						continue;
					}

					int center = 0;

					// switch over number of preceding OPs...
					if (precOps.size() == 1) { // one preceding OP...
						// move the box under it's preceding box...

						// get preceding box...
						final GraphBox precBox = boxes.get(precOps.get(0));

						// determine center of preceding box...
						center = precBox.getX() + (precBox.width / 2);

						// move box so that center of both boxes are equal...
						updateXWithoutUpdatingParentsSize(operatorgraph, box,
								levels, level,
								center
								- (box.width / 2), new HashSet<GraphBox>());
					} else {
						// center box under preceding boxes...

						// get first preceding box...
						GraphBox preBox = boxes.get(precOps.get(0));

						// determine center of first preceding box...
						center = preBox.getX() + (preBox.width / 2);

						// walk through preceding operators...
						for (int j = 1; j < precOps.size(); ++j) {
							// get preceding box...
							preBox = boxes.get(precOps.get(j));

							// determine center of preceding box...
							final int preBoxCenter = preBox.getX()
									+ (preBox.width / 2);

							// determine new center...
							center = (center + preBoxCenter) / 2;
						}

						// move box to center of preceding boxes...
						updateXWithoutUpdatingParentsSize(operatorgraph, box,
								levels, level,
								center
								- (box.width / 2), new HashSet<GraphBox>());
					}
				}
			}

			// resolve box overlapping...
			GraphBox box = row.getBoxes().get(0); // get first box

			// move left box if not at the right position...
			if (box.getX() < (int) Math.ceil(operatorgraph.PADDING)) {
				updateXWithoutUpdatingParentsSize(operatorgraph, box, levels,
						level,
						(int) Math.ceil(operatorgraph.PADDING),
						new HashSet<GraphBox>());
			}

			// walk through boxes...
			for (int i = 1; i < row.getBoxes().size(); ++i) {
				// get previous box...
				final GraphBox preBox = row.getBoxes().get(i - 1);

				box = row.getBoxes().get(i); // get current box

				// if previous box and current box overlap...
				if (preBox.getX() + preBox.width
						+ (int) Math.ceil(operatorgraph.SPACING_X) > box.getX()) {
					// move current box...
					updateXWithoutUpdatingParentsSize(operatorgraph,
							box,
							levels,
							level,
							preBox.getX() + preBox.width
									+ (int) Math.ceil(operatorgraph.SPACING_X),
							new HashSet<GraphBox>());
				}
			}

			// determine ID of longest row...
			if (row.getWidth() > rows.get(maxRowId).getWidth()) {
				maxRowId = r;
			}
		}

		if (rows.size() > 0) {
			final int maxWidth = rows.get(maxRowId).getWidth();

			// walk through all rows previous to maxRowId...
			for (int r = 0; r < maxRowId; ++r) {
				// center the current row...
				rows.get(r).centerWithoutUpdatingParentsSize(maxWidth / 2);
			}

			// --- resolve annotation overlapping - begin ---
			for (final GraphRow row : rows) { // walk through all rows...
				// walk through all boxes of the row...
				for (final GraphBox box : row.getBoxes()) {
					final Hashtable<GraphWrapper, AbstractSuperGuiComponent> lineAnnotations = box
							.getLineAnnotations();
					final Iterator<GraphWrapper> lineAnnotationsGWsIt = lineAnnotations
							.keySet().iterator();

					// box has less than two line annotations...
					if (lineAnnotations.size() < 2) {
						continue;
					}

					GraphWrapper previousAnnotationGW = lineAnnotationsGWsIt
							.next();
					AbstractSuperGuiComponent previousAnnotation = lineAnnotations
							.get(previousAnnotationGW);

					// walk through all but the first annotation...
					while (lineAnnotationsGWsIt.hasNext()) {
						// get current GW...
						final GraphWrapper lineAnnotationGW = lineAnnotationsGWsIt
								.next();

						// get current annotation....
						final AbstractSuperGuiComponent annotation = lineAnnotations
								.get(lineAnnotationGW);

						// check for overlapping...
						if (previousAnnotation.isOverlapping(annotation)) {
							final GraphBox previousAnnotationTargetBox = boxes
									.get(previousAnnotationGW);
							final GraphBox annotationTargetBox = boxes
									.get(lineAnnotationGW);

							// move annotation target box...
							if (previousAnnotationTargetBox.getX() <= annotationTargetBox
									.getX()) {
								final int annotationDistance = (previousAnnotation
										.getLocation().x + previousAnnotation
										.getPreferredSize().width)
										- annotation.getLocation().x;
								final int newX = annotationTargetBox.getX()
										+ annotationTargetBox.width
										+ (int) Math
												.ceil(operatorgraph.PADDING)
										+ annotationDistance;

								annotationTargetBox
										.setXWithoutUpdatingParentsSize(newX);
							}
							// move previousAnnotation target box...
							else if (previousAnnotationTargetBox.getX() > annotationTargetBox
									.getX()) {
								final int annotationDistance = (annotation
										.getLocation().x + annotation
										.getPreferredSize().width)
										- previousAnnotation.getLocation().x;
								final int newX = previousAnnotationTargetBox
										.getX()
										+ previousAnnotationTargetBox.width
										+ (int) Math
												.ceil(operatorgraph.PADDING)
										+ annotationDistance;

								previousAnnotationTargetBox
										.setXWithoutUpdatingParentsSize(newX);
							}
						}

						// set current annotation GW as previous annotation GW
						// for he next run...
						previousAnnotationGW = lineAnnotationGW;

						// set current annotation as previous annotation for the
						// next run...
						previousAnnotation = annotation;
					}
				}
			}
			// --- resolve annotation overlapping - end ---
		}

	}
}
