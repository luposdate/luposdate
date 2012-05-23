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
package lupos.gui.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.join.IndexJoin;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.viewer.Viewer;
import lupos.gui.operatorgraph.util.VEImageIcon;
import lupos.misc.Tuple;
import lupos.misc.debug.DebugStep;
import lupos.misc.debug.DebugStepRIF;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;
import xpref.datatypes.BooleanDatatype;
import xpref.datatypes.FontDatatype;

public class EvaluationDemoToolBar extends JPanel implements DebugStepRIF {
	private static final long serialVersionUID = 2228126871144675159L;
	/**
	 * The operatorGraphViewer is the frame, which displays the operator graph
	 */
	protected Viewer operatorGraphViewer;
	/**
	 * This lock is used to synchronize the GUI thread with the evaluator thread
	 */
	protected ReentrantLock lock = new ReentrantLock();
	/**
	 * This lock is used to synchronize the EventHandler thread with the
	 * evaluator thread
	 */
	protected ReentrantLock eventLock = new ReentrantLock();
	/**
	 * These conditions are used to synchronize the GUI thread with the
	 * evaluator thread
	 */
	protected Condition condition = lock.newCondition();
	protected Condition conditionEnableAction = lock.newCondition();
	/**
	 * This variable is used to synchronize the GUI thread with the evaluator
	 * thread
	 */
	protected volatile boolean nextstep = false;
	/**
	 * This variable is used to determine whether there is any more data
	 * available
	 */
	protected volatile boolean hasEnded = false;
	/**
	 * resume = true lets the evaluator thread processing the query without
	 * stopping at each step
	 */
	protected boolean resume = false;
	/**
	 * evaluationThread contains the thread for evaluation for a clean end of
	 * the evaluation demo frame
	 */
	protected Thread evaluationThread = null;
	/**
	 * commentLabelElement contains the currently displayed label, which is
	 * removed when displaying a new label
	 */
	protected CommentLabelElement commentLabelElement = null;
	/**
	 * stepButton contains the button "Next step"
	 */
	protected JButton stepButton;
	/**
	 * stepBackButton contains the button "Previous step"
	 */
	protected JButton stepBackButton;
	/**
	 * the button which lets the user jump to the end
	 */
	protected JButton gotoEndButton;
	/**
	 * the button which lets the user jump to the beginning
	 */
	protected JButton gotoBeginButton;
	/**
	 * the button either titled "Play" or "Pause", allowing the user to play the
	 * animation
	 */
	protected JButton playPauseButton;
	/**
	 * infoText contains information about the current status
	 */
	protected JLabel infoArea;
	/**
	 * The ringBuffer
	 */
	private final RingBuffer ringBuffer;
	/**
	 * To indicate if the next step will be really displayed or not...
	 */
	private volatile boolean displayCommentLabel = true;
	/**
	 * Enable Buttons during steps?
	 */
	private volatile boolean enableButtons = true;

	/**
	 * for disabling context menus during playing the animation!
	 */
	private LinkedList<JMenuItem> menuItems;

	public volatile static boolean fromJar;

	/**
	 * The constructor of this tool bar generating and adding the "Next step"
	 * button
	 */
	public EvaluationDemoToolBar(final boolean fromJar) {
		this(fromJar, false);
	}

	public EvaluationDemoToolBar(final boolean fromJar,
			final boolean infiniteStreams) {
		super();

		EvaluationDemoToolBar.fromJar = fromJar;

		// generate the navigation buttons
		this.generateBeginButton();
		this.generateStepBackButton();
		this.generatePlayPauseButton();
		this.generateStepButton();
		this.generateEndButton();

		if (infiniteStreams) {
			gotoEndButton.setVisible(false);
		}

		this.generateSpeedComboBox();

		// create the buffer
		ringBuffer = new RingBuffer();
	}

	private void generateSpeedComboBox() {

		final int max = 300;
		final JSlider jslider = new JSlider();

		jslider.setMinimum(1);
		jslider.setMaximum(max);
		jslider.setValue(max + 1
				- (int) CommentLabelElement.getPercentageSteps());
		jslider.addChangeListener(new ChangeListener() {

			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting()) {
					final int speed = max + 1 - source.getValue();

					if (speed <= 100) {
						CommentLabelElement.setPercentageSteps(speed);
						CommentLabelElement.setPause(10);
					} else {
						CommentLabelElement.setPercentageSteps(100);
						CommentLabelElement.setPause(10 + speed - 100);
					}
				}
			}

		});

		this.add(new JLabel("  Speed:"));
		this.add(jslider);
	}

	/**
	 * generates the begin button
	 * 
	 */
	private void generateBeginButton() {
		gotoBeginButton = new JButton("<<");
		gotoBeginButton
		.setToolTipText("Return to the beginning of the ring buffer!");
		gotoBeginButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				eventLock.lock();

				try {
					final StepContainer firstStep = ringBuffer.goBackToStart();

					if (firstStep != null) {
						displayCommentLabel(firstStep);
					}

					gotoBeginButton.setEnabled(false);
					stepBackButton.setEnabled(false);
					playPauseButton.setEnabled(true);
					stepButton.setEnabled(true);
					gotoEndButton.setEnabled(true);
				} finally {
					eventLock.unlock();
				}
			}
		});

		gotoBeginButton.setEnabled(false);
		this.add(gotoBeginButton);
	}

	/**
	 * generates the end button
	 * 
	 */
	private void generateEndButton() {
		gotoEndButton = new JButton(">>");
		gotoEndButton.setToolTipText("process all steps until end!");
		gotoEndButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				eventLock.lock();
				try {
					gotoBeginButton.setEnabled(false);
					stepBackButton.setEnabled(false);
					gotoEndButton.setEnabled(false);
					stepButton.setEnabled(false);
					playPauseButton.setEnabled(false);
					displayCommentLabel = false;
					while (!hasEnded || ringBuffer.hasNext()) {
						if (ringBuffer.hasNext()) {
							ringBuffer.next();
						} else {
							lock.lock();
							try {
								nextstep = true;
								condition.signalAll();
							} finally {
								lock.unlock();
							}
							lock.lock();
							try {
								while (nextstep == true && !hasEnded && !resume)
									try {
										conditionEnableAction.await();
									} catch (final InterruptedException e1) {
										System.err.println(e1);
										e1.printStackTrace();
									}
							} finally {
								lock.unlock();
							}
						}
					}
					displayCommentLabel = true;
					if (ringBuffer.getCurrentStepContainer() != null)
						displayCommentLabel(ringBuffer
								.getCurrentStepContainer());
					gotoBeginButton.setEnabled(ringBuffer.hasPrevious());
					stepBackButton.setEnabled(ringBuffer.hasPrevious());
				} finally {
					eventLock.unlock();
				}
			}
		});
		this.add(gotoEndButton);
	}

	private Font getStandardFont() {
		try {
			if (BooleanDatatype.getValues("standardFont.fontEnable").get(0)
					.booleanValue()) {
				return FontDatatype.getValues("standardFont.font").get(0);
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return this.playPauseButton.getFont();
	}

	private void setPlayButton() {
		playPauseButton.setText(" Play");
		final int size = getStandardFont().getSize();
		playPauseButton.setIcon(VEImageIcon.getPlayIcon(size, Color.GREEN
				.darker()));
		playPauseButton.setDisabledIcon(VEImageIcon.getPlayIcon(size,
				Color.GRAY));
		playPauseButton.setToolTipText("Play/pause the animation!");
	}

	/**
	 * generates a play button, that switches to "pause" when pressed
	 * 
	 */
	private void generatePlayPauseButton() {
		playPauseButton = new JButton();
		setPlayButton();
		playPauseButton.addActionListener(new ActionListener() {
			/**
			 * the playThread invoked when the user presses the play-button
			 */
			protected PlayThread playThread;

			public synchronized void actionPerformed(final ActionEvent arg0) {
				eventLock.lock();
				try {
					if (playThread != null && playThread.isAlive()) {
						playThread.pausePlayAnimation();
						try {
							playThread.join();
						} catch (final InterruptedException e) {
							System.err.println(e);
							e.printStackTrace();
						}
						setEnabledContextMenu(true);
						playThread = null;
						enableButtons = true;
						setPlayButton();
						stepButton
						.setEnabled(!hasEnded || ringBuffer.hasNext());
						stepBackButton.setEnabled(ringBuffer.hasPrevious());
						gotoBeginButton.setEnabled(ringBuffer.hasPrevious());
						gotoEndButton.setEnabled(!hasEnded
								|| ringBuffer.hasNext());
						playPauseButton.setEnabled(!hasEnded
								|| ringBuffer.hasNext());
					} else {

						playThread = new PlayThread();
						setEnabledContextMenu(false);
						stepButton.setEnabled(false);
						stepBackButton.setEnabled(false);
						gotoBeginButton.setEnabled(false);
						gotoEndButton.setEnabled(false);
						// start the thread
						playPauseButton.setText(" Pause");
						final int size = getStandardFont().getSize();
						playPauseButton.setIcon(VEImageIcon.getPauseIcon(size,
								Color.YELLOW.darker()));
						playPauseButton.setDisabledIcon(VEImageIcon
								.getPauseIcon(size, Color.GRAY));
						playThread.start();
					}
				} finally {
					eventLock.unlock();
				}
			}
		});
		this.add(playPauseButton);
	}

	private class PlayThread extends Thread {
		private volatile boolean pausedPlayAnimation = false;

		public synchronized void pausePlayAnimation() {
			pausedPlayAnimation = true;
		}

		@Override
		public void run() {
			enableButtons = false;
			while (!hasEnded || ringBuffer.hasNext()) {
				// only display the next step, if the user did not pause the
				// animation
				if (commentLabelElement != null) {
					while (commentLabelElement.getAnimationthread().isAlive())
						if (pausedPlayAnimation)
							return;
				}
				if (pausedPlayAnimation)
					return;
				nextButtonClick();
			}
			// the loop has ended so we change the button
			eventLock.lock();
			try {
				enableButtons = true;
				playPauseButton.setText(" Play");
				playPauseButton.setIcon(VEImageIcon.getPlayIcon(getFont()
						.getSize(), Color.GREEN.darker()));
				playPauseButton.setDisabledIcon(VEImageIcon.getPlayIcon(
						getFont().getSize(), Color.GRAY));
				stepButton.setEnabled(false);
				stepBackButton.setEnabled(ringBuffer.hasPrevious());
				gotoBeginButton.setEnabled(ringBuffer.hasPrevious());
				gotoEndButton.setEnabled(false);
				playPauseButton.setEnabled(false);
				setEnabledContextMenu(true);
			} finally {
				eventLock.unlock();
			}
		}
	}

	private void setEnabledContextMenu(final boolean enabled) {
		for (final JMenuItem menuItem : menuItems)
			menuItem.setEnabled(enabled);
	}

	/**
	 * This method generates and adds the "Next step" button
	 */
	private void generateStepButton() {
		stepButton = new JButton(">");
		stepButton.setToolTipText("process the next step!");
		stepButton.addActionListener(new ActionListener() {
			public synchronized void actionPerformed(final ActionEvent e) {
				eventLock.lock();
				try {
					if (commentLabelElement != null
							&& commentLabelElement.getAnimationthread() != null) {
						commentLabelElement.stopAnimation = true;
						try {
							commentLabelElement.getAnimationthread().join();
						} catch (final InterruptedException e1) {
							System.err.println(e1);
							e1.printStackTrace();
						}
					}
					nextButtonClick();
					stepBackButton.setEnabled(ringBuffer.hasPrevious());
					gotoBeginButton.setEnabled(ringBuffer.hasPrevious());
				} finally {
					eventLock.unlock();
				}
			}
		});
		this.add(stepButton);
	}

	/**
	 * the action which is performed when the next-step button is clicked
	 * 
	 */
	private void nextButtonClick() {
		if (ringBuffer.hasNext()) {
			final StepContainer resultStepContainer = ringBuffer.next();
			displayCommentLabel(resultStepContainer);
			if (!ringBuffer.hasNext() && hasEnded) {
				stepButton.setEnabled(false);
				playPauseButton.setEnabled(false);
				gotoEndButton.setEnabled(false);
			}
		} else {
			lock.lock();
			try {
				nextstep = true;
				condition.signalAll();
			} finally {
				lock.unlock();
			}
			lock.lock();
			try {
				while (nextstep == true && !hasEnded && !resume)
					try {
						conditionEnableAction.await();
					} catch (final InterruptedException e) {
						System.err.println(e);
						e.printStackTrace();
					}
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * This method generates and adds the "Previous step" button
	 */
	private void generateStepBackButton() {
		stepBackButton = new JButton("<");
		stepBackButton.setToolTipText("process the previous step!");
		stepBackButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				eventLock.lock();
				try {
					stepButton.setEnabled(true);
					playPauseButton.setEnabled(true);
					gotoEndButton.setEnabled(true);
					final StepContainer prevStepContainer = ringBuffer
					.previous();
					if (commentLabelElement != null
							&& commentLabelElement.getAnimationthread() != null) {
						commentLabelElement.stopAnimation = true;
						try {
							commentLabelElement.getAnimationthread().join();
						} catch (final InterruptedException e1) {
							System.err.println(e1);
							e1.printStackTrace();
						}
					}
					// show the comment label box
					if (prevStepContainer != null) {
						displayCommentLabel(prevStepContainer);
					}
					stepBackButton.setEnabled(ringBuffer.hasPrevious());
					gotoBeginButton.setEnabled(ringBuffer.hasPrevious());
				} finally {
					eventLock.unlock();
				}
			}
		});
		stepBackButton.setEnabled(false);
		this.add(stepBackButton);
	}

	/**
	 * @return the OperatorGraphViewer
	 */
	public Viewer getOperatorGraphViewer() {
		return this.operatorGraphViewer;
	}

	/**
	 * This method sets the operatorGraphViewer
	 * 
	 * @param operatorGraphViewer
	 */
	public void setOperatorGraphViewer(final Viewer operatorGraphViewer) {
		this.operatorGraphViewer = operatorGraphViewer;
		this.operatorGraphViewer.getRootPane().setDoubleBuffered(true);
		this.operatorGraphViewer.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				displayCommentLabel = false;
				if (commentLabelElement != null
						&& commentLabelElement.getAnimationthread() != null) {
					commentLabelElement.stopAnimation = true;
				}
				operatorGraphViewer.setVisible(false);
				close();
				operatorGraphViewer.dispose();
			}
		});

		setUpContextMenus();
	}

	/**
	 * This method sets up the context menus, which should be called after
	 * setting the operatorGraphViewer...
	 */
	public void setUpContextMenus() {
		// --- context menu example - begin ---
		// get the OperatorGraph...
		final OperatorGraph operatorGraph = this.operatorGraphViewer
		.getOperatorGraph();

		menuItems = new LinkedList<JMenuItem>();

		// walk through the list of root GraphWrappers...
		final HashSet<GraphWrapper> visited = new HashSet<GraphWrapper>();
		for (final GraphWrapper rootGW : operatorGraph.getRootList(false)) {
			this.addContextMenusToGraphWrappers(operatorGraph, rootGW, visited);
		}

		// remove context menus only from the root GraphWrappers...
		// for (final GraphWrapper rootGW : operatorGraph.getRootList(false)) {
		// operatorGraph.unsetContextMenuOfOperator(rootGW);
		// }
		// --- context menu example - end ---
	}

	/**
	 * This method sets a context menu for the given GraphWrappers and all their
	 * children.
	 * 
	 * @param operatorGraph
	 *            instance of the OperatorGraph
	 * @param graphWrappers
	 *            list of GraphWrappers to set context menus to
	 * @param visited
	 *            set of already visited GraphWrappers, used to detect cycles
	 */
	private void addContextMenusToGraphWrappers(
			final OperatorGraph operatorGraph, final GraphWrapper graphWrapper,
			final HashSet<GraphWrapper> visited) {
		// in case of cycles: has this GraphWrapper already been visited?
		if (visited.contains(graphWrapper)) {
			return;
		}

		visited.add(graphWrapper);

		// add context menu to the current GraphWrapper...
		operatorGraph.setContextMenuOfOperator(graphWrapper, this
				.createContextMenu(graphWrapper));

		// walk through children of current GraphWrapper...
		for (final GraphWrapperIDTuple gwIDT : graphWrapper
				.getSucceedingElements()) {
			// add context menus to the child GraphWrapper and its children...
			this.addContextMenusToGraphWrappers(operatorGraph, gwIDT
					.getOperator(), visited);
		}
	}

	/**
	 * This method creates a context menu to a specific GraphWrapper
	 * 
	 * @param graphWrapper
	 *            The GraphWrapper to which the context menu is attached to
	 * @return the context menu
	 */
	private JPopupMenu createContextMenu(final GraphWrapper graphWrapper) {
		final JMenuItem menuItemNext = new JMenuItem("Next Step from here");
		final JMenuItem menuItemPrev = new JMenuItem("Previous Step from here");

		final JMenuItem menuItemNextBindings = new JMenuItem(
		"Next Solution from here");
		final JMenuItem menuItemPrevBindings = new JMenuItem(
		"Previous Solution from here");
		final JMenuItem menuItemNextPredicate = new JMenuItem(
		"Next Predicate Solution from here");
		final JMenuItem menuItemPrevPredicate = new JMenuItem(
		"Previous Predicate Solution from here");

		final JMenuItem menuItemNextTriple = new JMenuItem(
		"Next Triple from here");
		final JMenuItem menuItemPrevTriple = new JMenuItem(
		"Previous Triple from here");

		final JMenuItem menuItemNextMessage = new JMenuItem(
		"Next Message from here");
		final JMenuItem menuItemPrevMessage = new JMenuItem(
		"Previous Message from here");

		final JMenuItem menuItemNextDeleteStep = new JMenuItem(
		"Next Delete Step from here");
		final JMenuItem menuItemPrevDeleteStep = new JMenuItem(
		"Previous Delete Step from here");

		final JMenuItem menuItemNextAddStep = new JMenuItem(
		"Next Add Step from here");
		final JMenuItem menuItemPrevAddStep = new JMenuItem(
		"Previous Add Step from here");

		final CheckStep checkStepAll = new CheckStep() {
			public boolean check(final StepContainer stepContainer) {
				return true;
			}
		};

		menuItemNext.addActionListener(new ContextMenuNextActionListener(
				graphWrapper, checkStepAll));
		menuItemPrev.addActionListener(new ContextMenuBackActionListener(
				graphWrapper, checkStepAll));

		final CheckStep checkStepBindings = new CheckStep() {
			public boolean check(final StepContainer stepContainer) {
				return stepContainer.getObject() instanceof Bindings;
			}
		};

		menuItemNextBindings
		.addActionListener(new ContextMenuNextActionListener(
				graphWrapper, checkStepBindings));
		menuItemPrevBindings
		.addActionListener(new ContextMenuBackActionListener(
				graphWrapper, checkStepBindings));

		final CheckStep checkStepPredicates = new CheckStep() {
			public boolean check(final StepContainer stepContainer) {
				return stepContainer.getObject() instanceof RuleResult;
			}
		};

		menuItemNextPredicate
		.addActionListener(new ContextMenuNextActionListener(
				graphWrapper, checkStepPredicates));
		menuItemPrevPredicate
		.addActionListener(new ContextMenuBackActionListener(
				graphWrapper, checkStepPredicates));

		final CheckStep checkStepTriple = new CheckStep() {
			public boolean check(final StepContainer stepContainer) {
				return stepContainer.getObject() instanceof Triple;
			}
		};

		menuItemNextTriple.addActionListener(new ContextMenuNextActionListener(
				graphWrapper, checkStepTriple));
		menuItemPrevTriple.addActionListener(new ContextMenuBackActionListener(
				graphWrapper, checkStepTriple));

		final CheckStep checkStepMessage = new CheckStep() {
			public boolean check(final StepContainer stepContainer) {
				return stepContainer.getObject() instanceof Message;
			}
		};

		menuItemNextMessage
		.addActionListener(new ContextMenuNextActionListener(
				graphWrapper, checkStepMessage));
		menuItemPrevMessage
		.addActionListener(new ContextMenuBackActionListener(
				graphWrapper, checkStepMessage));

		final CheckStep checkStepAddStep = new CheckStep() {
			public boolean check(final StepContainer stepContainer) {
				return !stepContainer.isDeleteStep();
			}
		};

		menuItemNextAddStep
		.addActionListener(new ContextMenuNextActionListener(
				graphWrapper, checkStepAddStep));
		menuItemPrevAddStep
		.addActionListener(new ContextMenuBackActionListener(
				graphWrapper, checkStepAddStep));

		final CheckStep checkStepDeleteStep = new CheckStep() {
			public boolean check(final StepContainer stepContainer) {
				return stepContainer.isDeleteStep();
			}
		};

		menuItemNextDeleteStep
		.addActionListener(new ContextMenuNextActionListener(
				graphWrapper, checkStepDeleteStep));
		menuItemPrevDeleteStep
		.addActionListener(new ContextMenuBackActionListener(
				graphWrapper, checkStepDeleteStep));

		final JPopupMenu contextMenu = new JPopupMenu();
		contextMenu.add(menuItemNext);
		contextMenu.add(menuItemPrev);
		contextMenu.addSeparator();
		contextMenu.add(menuItemNextBindings);
		contextMenu.add(menuItemPrevBindings);
		contextMenu.addSeparator();
		contextMenu.add(menuItemNextTriple);
		contextMenu.add(menuItemPrevTriple);
		contextMenu.addSeparator();
		contextMenu.add(menuItemNextMessage);
		contextMenu.add(menuItemPrevMessage);
		contextMenu.addSeparator();
		contextMenu.add(menuItemNextAddStep);
		contextMenu.add(menuItemPrevAddStep);
		contextMenu.addSeparator();
		contextMenu.add(menuItemNextDeleteStep);
		contextMenu.add(menuItemPrevDeleteStep);
		menuItems.add(menuItemNext);
		menuItems.add(menuItemPrev);
		menuItems.add(menuItemNextBindings);
		menuItems.add(menuItemPrevBindings);
		menuItems.add(menuItemNextTriple);
		menuItems.add(menuItemPrevTriple);
		menuItems.add(menuItemNextMessage);
		menuItems.add(menuItemPrevMessage);
		menuItems.add(menuItemNextAddStep);
		menuItems.add(menuItemPrevAddStep);
		menuItems.add(menuItemNextDeleteStep);
		menuItems.add(menuItemPrevDeleteStep);

		if (graphWrapper.getElement() instanceof IndexJoin) {
			final JMenuItem menuItemIndices = new JMenuItem(
					"Show internal indices");
			menuItemIndices.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					eventLock.lock();
					try {
						final IndexJoin indexJoin = (IndexJoin) graphWrapper
						.getElement();

						final Tuple<Vector<String>, Vector<Vector<String>>> left = getTable(indexJoin
								.getLba()[0]);
						final Tuple<Vector<String>, Vector<Vector<String>>> right = getTable(indexJoin
								.getLba()[1]);

						final JTable leftTable = CommentLabelElement.getTable(
								left.getSecond(), left.getFirst(),
								operatorGraphViewer.getOperatorGraph());
						// final JTable leftTable = new JTable(left.getSecond(),
						// left.getFirst());
						// leftTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
						leftTable.setBackground(Color.WHITE);

						final JTable rightTable = CommentLabelElement.getTable(
								right.getSecond(), right.getFirst(),
								operatorGraphViewer.getOperatorGraph());
						// final JTable rightTable = new
						// JTable(right.getSecond(),
						// right.getFirst());
						// rightTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
						rightTable.setBackground(Color.WHITE);

						final JPanel panelLeftTable = new JPanel(
								new BorderLayout());
						final JPanel innerPanelLeftTable = new JPanel(
								new BorderLayout());

						innerPanelLeftTable.add(new JLabel("Left Operand"),
								BorderLayout.NORTH);
						innerPanelLeftTable.add(leftTable.getTableHeader(),
								BorderLayout.CENTER);
						panelLeftTable.add(innerPanelLeftTable,
								BorderLayout.NORTH);
						panelLeftTable.add(leftTable, BorderLayout.CENTER);

						final JPanel panelRightTable = new JPanel(
								new BorderLayout());
						final JPanel innerPanelRightTable = new JPanel(
								new BorderLayout());

						innerPanelRightTable.add(new JLabel("Right Operand"),
								BorderLayout.NORTH);
						innerPanelRightTable.add(rightTable.getTableHeader(),
								BorderLayout.CENTER);
						panelRightTable.add(innerPanelRightTable,
								BorderLayout.NORTH);
						panelRightTable.add(rightTable, BorderLayout.CENTER);

						final JSplitPane splitPane = new JSplitPane(
								JSplitPane.HORIZONTAL_SPLIT);
						splitPane.setOneTouchExpandable(true);
						splitPane.setTopComponent(new JScrollPane(
								panelLeftTable));
						splitPane.setBottomComponent(new JScrollPane(
								panelRightTable));

						final JFrame frame = new JFrame("Indices of "
								+ graphWrapper.getElement().getClass()
								.getSimpleName());
						frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
						// Add content to the window.
						frame.add(splitPane);

						// Display the window.
						frame.pack();
						frame.setVisible(true);
					} finally {
						eventLock.unlock();
					}
				}
			});
			contextMenu.addSeparator();
			contextMenu.add(menuItemIndices);
			menuItems.add(menuItemIndices);
		}

		return contextMenu;
	}

	private Tuple<Vector<String>, Vector<Vector<String>>> getTable(
			final Map<String, QueryResult> index) {
		final Vector<Vector<String>> data = new Vector<Vector<String>>();
		final Vector<String> columnNames = new Vector<String>();
		columnNames.add("Number");
		columnNames.add("Index Key");

		final HashSet<Variable> allVariables = new HashSet<Variable>();

		for (final QueryResult qr : index.values()) {
			for (final Variable v : qr.getVariableSet()) {
				allVariables.add(v);
			}
		}
		for (final Variable v : allVariables) {
			columnNames.add(v.getName());
		}
		int number = 1;
		for (final Map.Entry<String, QueryResult> entry : index.entrySet()) {
			for (final Bindings b : entry.getValue()) {
				final Vector<String> columns = new Vector<String>();
				columns.add(number + ".");
				columns.add(entry.getKey());
				for (final Variable v : allVariables) {
					final Literal value = b.get(v);
					if (value == null)
						columns.add("");
					else
						columns.add(value.toString(this
								.getOperatorGraphViewer().getOperatorGraph()
								.getPrefix()));
				}
				data.add(columns);
				number++;
			}
		}
		return new Tuple<Vector<String>, Vector<Vector<String>>>(columnNames,
				data);
	}

	private class ContextMenuNextActionListener implements ActionListener {

		private final GraphWrapper graphWrapper;
		private final CheckStep checkStep;

		public ContextMenuNextActionListener(final GraphWrapper graphWrapper,
				final CheckStep checkStep) {
			this.graphWrapper = graphWrapper;
			this.checkStep = checkStep;
		}

		public void actionPerformed(final ActionEvent ae) {
			eventLock.lock();
			try {
				Tuple<Boolean, StepContainer> resultNextStep = nextStep(graphWrapper);
				while (resultNextStep != null && resultNextStep.getFirst()
						&& !checkStep.check(resultNextStep.getSecond()))
					resultNextStep = nextStep(graphWrapper);

				displayCommentLabel = true;
				if (resultNextStep != null && resultNextStep.getFirst()
						&& resultNextStep.getSecond() != null)
					displayCommentLabel(resultNextStep.getSecond());

				actualizeButtons();
			} finally {
				eventLock.unlock();
			}
		}
	}

	private class ContextMenuBackActionListener implements ActionListener {

		private final GraphWrapper graphWrapper;
		private final CheckStep checkStep;

		public ContextMenuBackActionListener(final GraphWrapper graphWrapper,
				final CheckStep checkStep) {
			this.graphWrapper = graphWrapper;
			this.checkStep = checkStep;
		}

		public void actionPerformed(final ActionEvent ae) {
			eventLock.lock();
			try {
				if (graphWrapper != null) {
					StepContainer searchedStepContainer = ringBuffer
					.goBackTo((BasicOperator) graphWrapper.getElement());
					while (searchedStepContainer != null
							&& !checkStep.check(searchedStepContainer))
						searchedStepContainer = ringBuffer
						.goBackTo((BasicOperator) graphWrapper
								.getElement());
					if (searchedStepContainer != null) {
						displayCommentLabel(searchedStepContainer);
					} else {
						JOptionPane.showMessageDialog(null, "No more elements");
					}
					actualizeButtons();
				}
			} finally {
				eventLock.unlock();
			}
		}
	}

	private interface CheckStep {
		public boolean check(StepContainer stepContainer);
	}

	private Tuple<Boolean, StepContainer> nextStep(
			final GraphWrapper graphWrapper) {
		if (ringBuffer.hasNext() || !hasEnded) {
			// very important! stop the animation if it's running
			if (commentLabelElement != null
					&& commentLabelElement.getAnimationthread() != null) {
				commentLabelElement.stopAnimation = true;
				try {
					commentLabelElement.getAnimationthread().join();
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
			// set lastOperator to further comparison

			final BasicOperator lastOperator = (BasicOperator) graphWrapper
			.getElement();
			boolean found = false;
			displayCommentLabel = false;
			BasicOperator lastOperatorFrom;
			while (!found && (!hasEnded || ringBuffer.hasNext())) {
				if (ringBuffer.hasNext()) {
					final StepContainer resultStepContainer = ringBuffer.next();
					lastOperatorFrom = resultStepContainer.getFrom();
				} else {
					lock.lock();
					try {
						nextstep = true;
						condition.signalAll();
					} finally {
						lock.unlock();
					}
					lock.lock();
					try {
						while (nextstep == true && !hasEnded && !resume)
							try {
								conditionEnableAction.await();
							} catch (final InterruptedException e) {
								System.err.println(e);
								e.printStackTrace();
							}
					} finally {
						lock.unlock();
					}
					lastOperatorFrom = ringBuffer.getCurrentStepContainer()
					.getFrom();
				}
				// check if we found the correct operator and then
				// end
				// the loop
				if (lastOperator.equals(lastOperatorFrom)) {
					found = true;
				}
			} // end while
			final StepContainer resultStepContainer = ringBuffer
			.getCurrentStepContainer();
			if (!found) {
				displayCommentLabel = true;
				displayCommentLabel(resultStepContainer);
				JOptionPane.showMessageDialog(null, "No more elements");
			}
			return new Tuple<Boolean, StepContainer>(found, resultStepContainer);
		} else {
			displayCommentLabel = true;
			displayCommentLabel(ringBuffer.getCurrentStepContainer());
			JOptionPane.showMessageDialog(null, "No more elements");
		}
		return null;
	}

	private void actualizeButtons() {
		if (!ringBuffer.hasNext() && hasEnded) {
			stepButton.setEnabled(false);
			playPauseButton.setEnabled(false);
			gotoEndButton.setEnabled(false);
		} else {
			stepButton.setEnabled(true);
			playPauseButton.setEnabled(true);
			gotoEndButton.setEnabled(true);
		}
		if (ringBuffer.hasPrevious()) {
			stepBackButton.setEnabled(true);
			gotoBeginButton.setEnabled(true);
		} else {
			stepBackButton.setEnabled(false);
			gotoBeginButton.setEnabled(false);
		}
	}

	/**
	 * This method sets the thread for evaluation
	 * 
	 * @param evaluationThread
	 *            the thread for evaluation
	 */
	public void setEvaluationThread(final Thread evaluationThread) {
		this.evaluationThread = evaluationThread;
	}

	/**
	 * This method is called whenever the garbage collector removes this object
	 */
	@Override
	public void finalize() {
		this.close();
	}

	/**
	 * This method is called for a clean end of the OperatorGraphViewer frame
	 */
	public void close() {
		if (this.evaluationThread != null) {
			try {
				this.lock.lock();

				try {
					this.resume = true;
					this.condition.signalAll();
					this.conditionEnableAction.signalAll();
				} finally {
					this.lock.unlock();
				}

				System.out.println("EvaluationDemoToolBar-Finalizer");

				this.evaluationThread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method lets the evaluation thread sleep until the next step is
	 * required by the GUI e.g. by pressing the "Next step" button
	 */
	private void waitForAction() {
		this.lock.lock();
		try {
			while (!this.nextstep && !this.resume) {
				try {
					this.condition.await();
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			this.lock.unlock();
		}
	}

	private void enableAction() {
		this.lock.lock();
		try {
			this.nextstep = false;
			conditionEnableAction.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	private void displayCommentLabel(final StepContainer stepContainer) {
		// don't do anything if the user clicked "Next from here" or
		// "Back to this"
		if (!displayCommentLabel) {
			return;
		}
		if (stepContainer.getObject() != null) {
			// remove the old comment panel
			if (this.commentLabelElement != null) {
				this.commentLabelElement.remove();
			}
			final Vector<Vector<String>> data = new Vector<Vector<String>>();
			final Vector<String> columnNames = new Vector<String>();
			if (stepContainer.getObject() instanceof Bindings) {
				final Bindings b = (Bindings) stepContainer.getObject();
				final Vector<String> columns = new Vector<String>();
				for (final Variable v : b.getVariableSet()) {
					columnNames.add(v.getName());
					columns.add(b.get(v).toString(
							this.operatorGraphViewer.getOperatorGraph()
							.getPrefix()));
				}
				data.add(columns);
				this.commentLabelElement = new CommentLabelElement(
						this.operatorGraphViewer.getOperatorGraph(),
						stepContainer.getFrom(), stepContainer.getTo(), data,
						columnNames, stepContainer.isDeleteStep());
			} else if (stepContainer.getObject() instanceof Triple) {
				final Triple t = (Triple) stepContainer.getObject();
				final Vector<String> columns = new Vector<String>();
				columns.add(t.getSubject()
						.toString(
								this.operatorGraphViewer.getOperatorGraph()
								.getPrefix()));
				columns.add(t.getPredicate()
						.toString(
								this.operatorGraphViewer.getOperatorGraph()
								.getPrefix()));
				columns.add(t.getObject()
						.toString(
								this.operatorGraphViewer.getOperatorGraph()
								.getPrefix()));
				columnNames.add("Subject");
				columnNames.add("Predicate");
				columnNames.add("Object");
				data.add(columns);
				this.commentLabelElement = new CommentLabelElement(
						this.operatorGraphViewer.getOperatorGraph(),
						stepContainer.getFrom(), stepContainer.getTo(), data,
						columnNames, stepContainer.isDeleteStep());
			} else if (stepContainer.getObject() instanceof RuleResult) {
				final RuleResult rr = (RuleResult) stepContainer.getObject();
				int max = 0;
				for (final Predicate p : rr.getPredicateResults()) {
					max = Math.max(max, p.getParameters().size());
				}
				columnNames.add("Predicate");
				for (int i = 1; i <= max; i++)
					columnNames.add("Arg. " + i);
				for (final Predicate p : rr.getPredicateResults()) {
					final Vector<String> columns = new Vector<String>();
					columns.add(p.getName().toString(
							this.operatorGraphViewer.getOperatorGraph()
							.getPrefix()));
					for (final Literal l : p.getParameters()) {
						columns.add(l.toString(this.operatorGraphViewer
								.getOperatorGraph().getPrefix()));
					}
					for (int i = p.getParameters().size(); i <= max; i++)
						columns.add("");
					data.add(columns);
				}
				this.commentLabelElement = new CommentLabelElement(
						this.operatorGraphViewer.getOperatorGraph(),
						stepContainer.getFrom(), stepContainer.getTo(), data,
						columnNames, stepContainer.isDeleteStep());
			} else {
				this.commentLabelElement = new CommentLabelElement(
						this.operatorGraphViewer.getOperatorGraph(),
						stepContainer.getFrom(), stepContainer.getTo(),
						stepContainer.getObject().toString(), stepContainer
						.isDeleteStep());
			}
		}
	}

	/**
	 * This method is called whenever an intermediate result is transmitted
	 * between two operators
	 */
	public synchronized void step(final BasicOperator from,
			final BasicOperator to, final Bindings bindings) {
		this.waitForAction();
		endOfStep(new StepContainer(from, to, bindings, false));
	}

	/**
	 * This method is called whenever a triple is transmitted between two
	 * operators
	 */
	public synchronized void step(final BasicOperator from,
			final BasicOperator to, final Triple triple) {
		this.waitForAction();
		endOfStep(new StepContainer(from, to, triple, false));
	}

	/**
	 * This method is called whenever an intermediate result to be deleted is
	 * transmitted between two operators
	 */
	public synchronized void stepDelete(final BasicOperator from,
			final BasicOperator to, final Bindings bindings) {
		this.waitForAction();
		endOfStep(new StepContainer(from, to, bindings, true));
	}

	/**
	 * This method is called whenever a triple to be deleted is transmitted
	 * between two operators
	 */
	public synchronized void stepDelete(final BasicOperator from,
			final BasicOperator to, final Triple triple) {
		this.waitForAction();
		endOfStep(new StepContainer(from, to, triple, true));
	}

	/**
	 * This method is called whenever an event for deleting all intermediate
	 * results is transmitted between two operators
	 */
	public synchronized void stepDeleteAll(final BasicOperator from,
			final BasicOperator to) {
		this.waitForAction();
		endOfStep(new StepContainer(from, to, "Delete all solutions", true));
	}

	/**
	 * This method is called whenever a message is transmitted between two
	 * operators
	 */
	public synchronized void stepMessage(final BasicOperator from,
			final BasicOperator to, final Message msg) {
		this.waitForAction();
		endOfStep(new StepContainer(from, to, msg, false));
	}

	private void endOfStep(final StepContainer stepContainer) {
		ringBuffer.next(stepContainer);
		if (enableButtons) {
			stepBackButton.setEnabled(ringBuffer.hasPrevious());
			gotoBeginButton.setEnabled(ringBuffer.hasPrevious());
		}
		displayCommentLabel(stepContainer);
		this.enableAction();
	}

	/**
	 * This method is called after the evaluation of the query has ended
	 */
	public synchronized void endOfEvaluation() {
		// there is no next step any more => disable the step button!
		stepButton.setEnabled(false);
		playPauseButton.setEnabled(false);
		gotoEndButton.setEnabled(false);
		lock.lock();
		try {
			hasEnded = true;
			this.conditionEnableAction.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public void step(final BasicOperator from, final BasicOperator to, final RuleResult rr) {
		this.waitForAction();
		endOfStep(new StepContainer(from, to, rr, false));
	}

	public void stepDelete(final BasicOperator from, final BasicOperator to, final RuleResult rr) {
		this.waitForAction();
		endOfStep(new StepContainer(from, to, rr, false));
	}
}