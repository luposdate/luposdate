
/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
 *
 * @author groppe
 * @version $Id: $Id
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
import lupos.gui.debug.ShowResult.GetOperatorGraphViewer;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.util.VEImageIcon;
import lupos.gui.operatorgraph.viewer.Viewer;
import lupos.misc.Tuple;
import lupos.misc.debug.DebugStepRIF;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;
import xpref.datatypes.BooleanDatatype;
import xpref.datatypes.FontDatatype;
public class EvaluationDemoToolBar extends JPanel implements DebugStepRIF, GetOperatorGraphViewer {
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
	protected Condition condition = this.lock.newCondition();
	protected Condition conditionEnableAction = this.lock.newCondition();
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

	/** Constant <code>fromJar=</code> */
	public volatile static boolean fromJar;

	/**
	 * The constructor of this tool bar generating and adding the "Next step"
	 * button
	 *
	 * @param fromJar a boolean.
	 */
	public EvaluationDemoToolBar(final boolean fromJar) {
		this(fromJar, false);
	}

	/**
	 * <p>Constructor for EvaluationDemoToolBar.</p>
	 *
	 * @param fromJar a boolean.
	 * @param infiniteStreams a boolean.
	 */
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
			this.gotoEndButton.setVisible(false);
		}

		this.generateSpeedComboBox();

		// create the buffer
		this.ringBuffer = new RingBuffer();
	}

	private void generateSpeedComboBox() {

		final int max = 300;
		final JSlider jslider = new JSlider();

		jslider.setMinimum(1);
		jslider.setMaximum(max);
		jslider.setValue(max + 1
				- (int) CommentLabelElement.getPercentageSteps());
		jslider.addChangeListener(new ChangeListener() {

			@Override
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
		this.gotoBeginButton = new JButton("<<");
		this.gotoBeginButton
		.setToolTipText("Return to the beginning of the ring buffer!");
		this.gotoBeginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				EvaluationDemoToolBar.this.eventLock.lock();

				try {
					final StepContainer firstStep = EvaluationDemoToolBar.this.ringBuffer.goBackToStart();

					if (firstStep != null) {
						EvaluationDemoToolBar.this.displayCommentLabel(firstStep);
					}

					EvaluationDemoToolBar.this.gotoBeginButton.setEnabled(false);
					EvaluationDemoToolBar.this.stepBackButton.setEnabled(false);
					EvaluationDemoToolBar.this.playPauseButton.setEnabled(true);
					EvaluationDemoToolBar.this.stepButton.setEnabled(true);
					EvaluationDemoToolBar.this.gotoEndButton.setEnabled(true);
				} finally {
					EvaluationDemoToolBar.this.eventLock.unlock();
				}
			}
		});

		this.gotoBeginButton.setEnabled(false);
		this.add(this.gotoBeginButton);
	}

	/**
	 * generates the end button
	 *
	 */
	private void generateEndButton() {
		this.gotoEndButton = new JButton(">>");
		this.gotoEndButton.setToolTipText("process all steps until end!");
		this.gotoEndButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				EvaluationDemoToolBar.this.eventLock.lock();
				try {
					EvaluationDemoToolBar.this.gotoBeginButton.setEnabled(false);
					EvaluationDemoToolBar.this.stepBackButton.setEnabled(false);
					EvaluationDemoToolBar.this.gotoEndButton.setEnabled(false);
					EvaluationDemoToolBar.this.stepButton.setEnabled(false);
					EvaluationDemoToolBar.this.playPauseButton.setEnabled(false);
					EvaluationDemoToolBar.this.displayCommentLabel = false;
					while (!EvaluationDemoToolBar.this.hasEnded || EvaluationDemoToolBar.this.ringBuffer.hasNext()) {
						if (EvaluationDemoToolBar.this.ringBuffer.hasNext()) {
							EvaluationDemoToolBar.this.ringBuffer.next();
						} else {
							EvaluationDemoToolBar.this.lock.lock();
							try {
								EvaluationDemoToolBar.this.nextstep = true;
								EvaluationDemoToolBar.this.condition.signalAll();
							} finally {
								EvaluationDemoToolBar.this.lock.unlock();
							}
							EvaluationDemoToolBar.this.lock.lock();
							try {
								while (EvaluationDemoToolBar.this.nextstep == true && !EvaluationDemoToolBar.this.hasEnded && !EvaluationDemoToolBar.this.resume) {
									try {
										EvaluationDemoToolBar.this.conditionEnableAction.await();
									} catch (final InterruptedException e1) {
										System.err.println(e1);
										e1.printStackTrace();
									}
								}
							} finally {
								EvaluationDemoToolBar.this.lock.unlock();
							}
						}
					}
					EvaluationDemoToolBar.this.displayCommentLabel = true;
					if (EvaluationDemoToolBar.this.ringBuffer.getCurrentStepContainer() != null) {
						EvaluationDemoToolBar.this.displayCommentLabel(EvaluationDemoToolBar.this.ringBuffer
								.getCurrentStepContainer());
					}
					EvaluationDemoToolBar.this.gotoBeginButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
					EvaluationDemoToolBar.this.stepBackButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
				} finally {
					EvaluationDemoToolBar.this.eventLock.unlock();
				}
			}
		});
		this.add(this.gotoEndButton);
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
		this.playPauseButton.setText(" Play");
		final int size = this.getStandardFont().getSize();
		this.playPauseButton.setIcon(VEImageIcon.getPlayIcon(size, Color.GREEN
				.darker()));
		this.playPauseButton.setDisabledIcon(VEImageIcon.getPlayIcon(size,
				Color.GRAY));
		this.playPauseButton.setToolTipText("Play/pause the animation!");
	}

	/**
	 * generates a play button, that switches to "pause" when pressed
	 *
	 */
	private void generatePlayPauseButton() {
		this.playPauseButton = new JButton();
		this.setPlayButton();
		this.playPauseButton.addActionListener(new ActionListener() {
			/**
			 * the playThread invoked when the user presses the play-button
			 */
			protected PlayThread playThread;

			@Override
			public synchronized void actionPerformed(final ActionEvent arg0) {
				EvaluationDemoToolBar.this.eventLock.lock();
				try {
					if (this.playThread != null && this.playThread.isAlive()) {
						this.playThread.pausePlayAnimation();
						try {
							this.playThread.join();
						} catch (final InterruptedException e) {
							System.err.println(e);
							e.printStackTrace();
						}
						EvaluationDemoToolBar.this.setEnabledContextMenu(true);
						this.playThread = null;
						EvaluationDemoToolBar.this.enableButtons = true;
						EvaluationDemoToolBar.this.setPlayButton();
						EvaluationDemoToolBar.this.stepButton
						.setEnabled(!EvaluationDemoToolBar.this.hasEnded || EvaluationDemoToolBar.this.ringBuffer.hasNext());
						EvaluationDemoToolBar.this.stepBackButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
						EvaluationDemoToolBar.this.gotoBeginButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
						EvaluationDemoToolBar.this.gotoEndButton.setEnabled(!EvaluationDemoToolBar.this.hasEnded
								|| EvaluationDemoToolBar.this.ringBuffer.hasNext());
						EvaluationDemoToolBar.this.playPauseButton.setEnabled(!EvaluationDemoToolBar.this.hasEnded
								|| EvaluationDemoToolBar.this.ringBuffer.hasNext());
					} else {

						this.playThread = new PlayThread();
						EvaluationDemoToolBar.this.setEnabledContextMenu(false);
						EvaluationDemoToolBar.this.stepButton.setEnabled(false);
						EvaluationDemoToolBar.this.stepBackButton.setEnabled(false);
						EvaluationDemoToolBar.this.gotoBeginButton.setEnabled(false);
						EvaluationDemoToolBar.this.gotoEndButton.setEnabled(false);
						// start the thread
						EvaluationDemoToolBar.this.playPauseButton.setText(" Pause");
						final int size = EvaluationDemoToolBar.this.getStandardFont().getSize();
						EvaluationDemoToolBar.this.playPauseButton.setIcon(VEImageIcon.getPauseIcon(size,
								Color.YELLOW.darker()));
						EvaluationDemoToolBar.this.playPauseButton.setDisabledIcon(VEImageIcon
								.getPauseIcon(size, Color.GRAY));
						this.playThread.start();
					}
				} finally {
					EvaluationDemoToolBar.this.eventLock.unlock();
				}
			}
		});
		this.add(this.playPauseButton);
	}

	private class PlayThread extends Thread {
		private volatile boolean pausedPlayAnimation = false;

		public synchronized void pausePlayAnimation() {
			this.pausedPlayAnimation = true;
		}

		@Override
		public void run() {
			EvaluationDemoToolBar.this.enableButtons = false;
			while (!EvaluationDemoToolBar.this.hasEnded || EvaluationDemoToolBar.this.ringBuffer.hasNext()) {
				// only display the next step, if the user did not pause the
				// animation
				if (EvaluationDemoToolBar.this.commentLabelElement != null) {
					while (EvaluationDemoToolBar.this.commentLabelElement.getAnimationthread().isAlive()) {
						if (this.pausedPlayAnimation) {
							return;
						}
					}
				}
				if (this.pausedPlayAnimation) {
					return;
				}
				EvaluationDemoToolBar.this.nextButtonClick();
			}
			// the loop has ended so we change the button
			EvaluationDemoToolBar.this.eventLock.lock();
			try {
				EvaluationDemoToolBar.this.enableButtons = true;
				EvaluationDemoToolBar.this.playPauseButton.setText(" Play");
				EvaluationDemoToolBar.this.playPauseButton.setIcon(VEImageIcon.getPlayIcon(EvaluationDemoToolBar.this.getFont()
						.getSize(), Color.GREEN.darker()));
				EvaluationDemoToolBar.this.playPauseButton.setDisabledIcon(VEImageIcon.getPlayIcon(
						EvaluationDemoToolBar.this.getFont().getSize(), Color.GRAY));
				EvaluationDemoToolBar.this.stepButton.setEnabled(false);
				EvaluationDemoToolBar.this.stepBackButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
				EvaluationDemoToolBar.this.gotoBeginButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
				EvaluationDemoToolBar.this.gotoEndButton.setEnabled(false);
				EvaluationDemoToolBar.this.playPauseButton.setEnabled(false);
				EvaluationDemoToolBar.this.setEnabledContextMenu(true);
			} finally {
				EvaluationDemoToolBar.this.eventLock.unlock();
			}
		}
	}

	private void setEnabledContextMenu(final boolean enabled) {
		for (final JMenuItem menuItem : this.menuItems) {
			menuItem.setEnabled(enabled);
		}
	}

	/**
	 * This method generates and adds the "Next step" button
	 */
	private void generateStepButton() {
		this.stepButton = new JButton(">");
		this.stepButton.setToolTipText("process the next step!");
		this.stepButton.addActionListener(new ActionListener() {
			@Override
			public synchronized void actionPerformed(final ActionEvent e) {
				EvaluationDemoToolBar.this.eventLock.lock();
				try {
					if (EvaluationDemoToolBar.this.commentLabelElement != null
							&& EvaluationDemoToolBar.this.commentLabelElement.getAnimationthread() != null) {
						EvaluationDemoToolBar.this.commentLabelElement.stopAnimation = true;
						try {
							EvaluationDemoToolBar.this.commentLabelElement.getAnimationthread().join();
						} catch (final InterruptedException e1) {
							System.err.println(e1);
							e1.printStackTrace();
						}
					}
					EvaluationDemoToolBar.this.nextButtonClick();
					EvaluationDemoToolBar.this.stepBackButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
					EvaluationDemoToolBar.this.gotoBeginButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
				} finally {
					EvaluationDemoToolBar.this.eventLock.unlock();
				}
			}
		});
		this.add(this.stepButton);
	}

	/**
	 * the action which is performed when the next-step button is clicked
	 *
	 */
	private void nextButtonClick() {
		if (this.ringBuffer.hasNext()) {
			final StepContainer resultStepContainer = this.ringBuffer.next();
			this.displayCommentLabel(resultStepContainer);
			if (!this.ringBuffer.hasNext() && this.hasEnded) {
				this.stepButton.setEnabled(false);
				this.playPauseButton.setEnabled(false);
				this.gotoEndButton.setEnabled(false);
			}
		} else {
			this.lock.lock();
			try {
				this.nextstep = true;
				this.condition.signalAll();
			} finally {
				this.lock.unlock();
			}
			this.lock.lock();
			try {
				while (this.nextstep == true && !this.hasEnded && !this.resume) {
					try {
						this.conditionEnableAction.await();
					} catch (final InterruptedException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			} finally {
				this.lock.unlock();
			}
		}
	}

	/**
	 * This method generates and adds the "Previous step" button
	 */
	private void generateStepBackButton() {
		this.stepBackButton = new JButton("<");
		this.stepBackButton.setToolTipText("process the previous step!");
		this.stepBackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				EvaluationDemoToolBar.this.eventLock.lock();
				try {
					EvaluationDemoToolBar.this.stepButton.setEnabled(true);
					EvaluationDemoToolBar.this.playPauseButton.setEnabled(true);
					EvaluationDemoToolBar.this.gotoEndButton.setEnabled(true);
					final StepContainer prevStepContainer = EvaluationDemoToolBar.this.ringBuffer
					.previous();
					if (EvaluationDemoToolBar.this.commentLabelElement != null
							&& EvaluationDemoToolBar.this.commentLabelElement.getAnimationthread() != null) {
						EvaluationDemoToolBar.this.commentLabelElement.stopAnimation = true;
						try {
							EvaluationDemoToolBar.this.commentLabelElement.getAnimationthread().join();
						} catch (final InterruptedException e1) {
							System.err.println(e1);
							e1.printStackTrace();
						}
					}
					// show the comment label box
					if (prevStepContainer != null) {
						EvaluationDemoToolBar.this.displayCommentLabel(prevStepContainer);
					}
					EvaluationDemoToolBar.this.stepBackButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
					EvaluationDemoToolBar.this.gotoBeginButton.setEnabled(EvaluationDemoToolBar.this.ringBuffer.hasPrevious());
				} finally {
					EvaluationDemoToolBar.this.eventLock.unlock();
				}
			}
		});
		this.stepBackButton.setEnabled(false);
		this.add(this.stepBackButton);
	}

	/** {@inheritDoc} */
	@Override
	public Viewer getOperatorGraphViewer() {
		return this.operatorGraphViewer;
	}

	/**
	 * This method sets the operatorGraphViewer
	 *
	 * @param operatorGraphViewer a {@link lupos.gui.operatorgraph.viewer.Viewer} object.
	 */
	public void setOperatorGraphViewer(final Viewer operatorGraphViewer) {
		this.operatorGraphViewer = operatorGraphViewer;
		this.operatorGraphViewer.getRootPane().setDoubleBuffered(true);
		this.operatorGraphViewer.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				EvaluationDemoToolBar.this.displayCommentLabel = false;
				if (EvaluationDemoToolBar.this.commentLabelElement != null
						&& EvaluationDemoToolBar.this.commentLabelElement.getAnimationthread() != null) {
					EvaluationDemoToolBar.this.commentLabelElement.stopAnimation = true;
				}
				operatorGraphViewer.setVisible(false);
				EvaluationDemoToolBar.this.close();
				operatorGraphViewer.dispose();
			}
		});

		this.setUpContextMenus();
	}

	/**
	 * This method sets up the context menus, which should be called after
	 * setting the operatorGraphViewer...
	 */
	public void setUpContextMenus() {
		// --- context menu example - begin ---
		// get the OperatorGraph...
		final OperatorGraph operatorGraph = this.operatorGraphViewer.getOperatorGraph();

		this.menuItems = new LinkedList<JMenuItem>();

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
			@Override
			public boolean check(final StepContainer stepContainer) {
				return true;
			}
		};

		menuItemNext.addActionListener(new ContextMenuNextActionListener(
				graphWrapper, checkStepAll));
		menuItemPrev.addActionListener(new ContextMenuBackActionListener(
				graphWrapper, checkStepAll));

		final CheckStep checkStepBindings = new CheckStep() {
			@Override
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
			@Override
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
			@Override
			public boolean check(final StepContainer stepContainer) {
				return stepContainer.getObject() instanceof Triple;
			}
		};

		menuItemNextTriple.addActionListener(new ContextMenuNextActionListener(
				graphWrapper, checkStepTriple));
		menuItemPrevTriple.addActionListener(new ContextMenuBackActionListener(
				graphWrapper, checkStepTriple));

		final CheckStep checkStepMessage = new CheckStep() {
			@Override
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
			@Override
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
			@Override
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
		this.menuItems.add(menuItemNext);
		this.menuItems.add(menuItemPrev);
		this.menuItems.add(menuItemNextBindings);
		this.menuItems.add(menuItemPrevBindings);
		this.menuItems.add(menuItemNextTriple);
		this.menuItems.add(menuItemPrevTriple);
		this.menuItems.add(menuItemNextMessage);
		this.menuItems.add(menuItemPrevMessage);
		this.menuItems.add(menuItemNextAddStep);
		this.menuItems.add(menuItemPrevAddStep);
		this.menuItems.add(menuItemNextDeleteStep);
		this.menuItems.add(menuItemPrevDeleteStep);

		if (graphWrapper.getElement() instanceof IndexJoin) {
			final JMenuItem menuItemIndices = new JMenuItem(
					"Show internal indices");
			menuItemIndices.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					EvaluationDemoToolBar.this.eventLock.lock();
					try {
						final IndexJoin indexJoin = (IndexJoin) graphWrapper
						.getElement();

						final Tuple<Vector<String>, Vector<Vector<String>>> left = EvaluationDemoToolBar.this.getTable(indexJoin
								.getLba()[0]);
						final Tuple<Vector<String>, Vector<Vector<String>>> right = EvaluationDemoToolBar.this.getTable(indexJoin
								.getLba()[1]);

						final JTable leftTable = CommentLabelElement.getTable(
								left.getSecond(), left.getFirst(),
								EvaluationDemoToolBar.this.operatorGraphViewer.getOperatorGraph());
						// final JTable leftTable = new JTable(left.getSecond(),
						// left.getFirst());
						// leftTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
						leftTable.setBackground(Color.WHITE);

						final JTable rightTable = CommentLabelElement.getTable(
								right.getSecond(), right.getFirst(),
								EvaluationDemoToolBar.this.operatorGraphViewer.getOperatorGraph());
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
						EvaluationDemoToolBar.this.eventLock.unlock();
					}
				}
			});
			contextMenu.addSeparator();
			contextMenu.add(menuItemIndices);
			this.menuItems.add(menuItemIndices);
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
					if (value == null) {
						columns.add("");
					} else {
						columns.add(value.toString(this
								.getOperatorGraphViewer().getOperatorGraph()
								.getPrefix()));
					}
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

		@Override
		public void actionPerformed(final ActionEvent ae) {
			EvaluationDemoToolBar.this.eventLock.lock();
			try {
				Tuple<Boolean, StepContainer> resultNextStep = EvaluationDemoToolBar.this.nextStep(this.graphWrapper);
				while (resultNextStep != null && resultNextStep.getFirst()
						&& !this.checkStep.check(resultNextStep.getSecond())) {
					resultNextStep = EvaluationDemoToolBar.this.nextStep(this.graphWrapper);
				}

				EvaluationDemoToolBar.this.displayCommentLabel = true;
				if (resultNextStep != null && resultNextStep.getFirst()
						&& resultNextStep.getSecond() != null) {
					EvaluationDemoToolBar.this.displayCommentLabel(resultNextStep.getSecond());
				}

				EvaluationDemoToolBar.this.actualizeButtons();
			} finally {
				EvaluationDemoToolBar.this.eventLock.unlock();
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

		@Override
		public void actionPerformed(final ActionEvent ae) {
			EvaluationDemoToolBar.this.eventLock.lock();
			try {
				if (this.graphWrapper != null) {
					StepContainer searchedStepContainer = EvaluationDemoToolBar.this.ringBuffer
					.goBackTo((BasicOperator) this.graphWrapper.getElement());
					while (searchedStepContainer != null
							&& !this.checkStep.check(searchedStepContainer)) {
						searchedStepContainer = EvaluationDemoToolBar.this.ringBuffer
						.goBackTo((BasicOperator) this.graphWrapper
								.getElement());
					}
					if (searchedStepContainer != null) {
						EvaluationDemoToolBar.this.displayCommentLabel(searchedStepContainer);
					} else {
						JOptionPane.showMessageDialog(null, "No more elements");
					}
					EvaluationDemoToolBar.this.actualizeButtons();
				}
			} finally {
				EvaluationDemoToolBar.this.eventLock.unlock();
			}
		}
	}

	private interface CheckStep {
		public boolean check(StepContainer stepContainer);
	}

	private Tuple<Boolean, StepContainer> nextStep(
			final GraphWrapper graphWrapper) {
		if (this.ringBuffer.hasNext() || !this.hasEnded) {
			// very important! stop the animation if it's running
			if (this.commentLabelElement != null
					&& this.commentLabelElement.getAnimationthread() != null) {
				this.commentLabelElement.stopAnimation = true;
				try {
					this.commentLabelElement.getAnimationthread().join();
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
			// set lastOperator to further comparison

			final BasicOperator lastOperator = (BasicOperator) graphWrapper
			.getElement();
			boolean found = false;
			this.displayCommentLabel = false;
			BasicOperator lastOperatorFrom;
			while (!found && (!this.hasEnded || this.ringBuffer.hasNext())) {
				if (this.ringBuffer.hasNext()) {
					final StepContainer resultStepContainer = this.ringBuffer.next();
					lastOperatorFrom = resultStepContainer.getFrom();
				} else {
					this.lock.lock();
					try {
						this.nextstep = true;
						this.condition.signalAll();
					} finally {
						this.lock.unlock();
					}
					this.lock.lock();
					try {
						while (this.nextstep == true && !this.hasEnded && !this.resume) {
							try {
								this.conditionEnableAction.await();
							} catch (final InterruptedException e) {
								System.err.println(e);
								e.printStackTrace();
							}
						}
					} finally {
						this.lock.unlock();
					}
					lastOperatorFrom = this.ringBuffer.getCurrentStepContainer()
					.getFrom();
				}
				// check if we found the correct operator and then
				// end
				// the loop
				if (lastOperator.equals(lastOperatorFrom)) {
					found = true;
				}
			} // end while
			final StepContainer resultStepContainer = this.ringBuffer
			.getCurrentStepContainer();
			if (!found) {
				this.displayCommentLabel = true;
				this.displayCommentLabel(resultStepContainer);
				JOptionPane.showMessageDialog(null, "No more elements");
			}
			return new Tuple<Boolean, StepContainer>(found, resultStepContainer);
		} else {
			this.displayCommentLabel = true;
			this.displayCommentLabel(this.ringBuffer.getCurrentStepContainer());
			JOptionPane.showMessageDialog(null, "No more elements");
		}
		return null;
	}

	private void actualizeButtons() {
		if (!this.ringBuffer.hasNext() && this.hasEnded) {
			this.stepButton.setEnabled(false);
			this.playPauseButton.setEnabled(false);
			this.gotoEndButton.setEnabled(false);
		} else {
			this.stepButton.setEnabled(true);
			this.playPauseButton.setEnabled(true);
			this.gotoEndButton.setEnabled(true);
		}
		if (this.ringBuffer.hasPrevious()) {
			this.stepBackButton.setEnabled(true);
			this.gotoBeginButton.setEnabled(true);
		} else {
			this.stepBackButton.setEnabled(false);
			this.gotoBeginButton.setEnabled(false);
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
	 * {@inheritDoc}
	 *
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
			this.conditionEnableAction.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	private void displayCommentLabel(final StepContainer stepContainer) {
		// don't do anything if the user clicked "Next from here" or
		// "Back to this"
		if (!this.displayCommentLabel) {
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
				for (int i = 1; i <= max; i++) {
					columnNames.add("Arg. " + i);
				}
				for (final Predicate p : rr.getPredicateResults()) {
					final Vector<String> columns = new Vector<String>();
					columns.add(p.getName().toString(
							this.operatorGraphViewer.getOperatorGraph()
							.getPrefix()));
					for (final Literal l : p.getParameters()) {
						columns.add(l.toString(this.operatorGraphViewer.getOperatorGraph().getPrefix()));
					}
					for (int i = p.getParameters().size(); i <= max; i++) {
						columns.add("");
					}
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
	 * {@inheritDoc}
	 *
	 * This method is called whenever an intermediate result is transmitted
	 * between two operators
	 */
	@Override
	public synchronized void step(final BasicOperator from,
			final BasicOperator to, final Bindings bindings) {
		this.waitForAction();
		this.endOfStep(new StepContainer(from, to, bindings, false));
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is called whenever a triple is transmitted between two
	 * operators
	 */
	@Override
	public synchronized void step(final BasicOperator from,
			final BasicOperator to, final Triple triple) {
		this.waitForAction();
		this.endOfStep(new StepContainer(from, to, triple, false));
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is called whenever an intermediate result to be deleted is
	 * transmitted between two operators
	 */
	@Override
	public synchronized void stepDelete(final BasicOperator from,
			final BasicOperator to, final Bindings bindings) {
		this.waitForAction();
		this.endOfStep(new StepContainer(from, to, bindings, true));
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is called whenever a triple to be deleted is transmitted
	 * between two operators
	 */
	@Override
	public synchronized void stepDelete(final BasicOperator from,
			final BasicOperator to, final Triple triple) {
		this.waitForAction();
		this.endOfStep(new StepContainer(from, to, triple, true));
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is called whenever an event for deleting all intermediate
	 * results is transmitted between two operators
	 */
	@Override
	public synchronized void stepDeleteAll(final BasicOperator from,
			final BasicOperator to) {
		this.waitForAction();
		this.endOfStep(new StepContainer(from, to, "Delete all solutions", true));
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is called whenever a message is transmitted between two
	 * operators
	 */
	@Override
	public synchronized void stepMessage(final BasicOperator from,
			final BasicOperator to, final Message msg) {
		this.waitForAction();
		this.endOfStep(new StepContainer(from, to, msg, false));
	}

	private void endOfStep(final StepContainer stepContainer) {
		this.ringBuffer.next(stepContainer);
		if (this.enableButtons) {
			this.stepBackButton.setEnabled(this.ringBuffer.hasPrevious());
			this.gotoBeginButton.setEnabled(this.ringBuffer.hasPrevious());
		}
		this.displayCommentLabel(stepContainer);
		this.enableAction();
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is called after the evaluation of the query has ended
	 */
	@Override
	public synchronized void endOfEvaluation() {
		// there is no next step any more => disable the step button!
		this.stepButton.setEnabled(false);
		this.playPauseButton.setEnabled(false);
		this.gotoEndButton.setEnabled(false);
		this.lock.lock();
		try {
			this.hasEnded = true;
			this.conditionEnableAction.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void step(final BasicOperator from, final BasicOperator to, final RuleResult rr) {
		this.waitForAction();
		this.endOfStep(new StepContainer(from, to, rr, false));
	}

	/** {@inheritDoc} */
	@Override
	public void stepDelete(final BasicOperator from, final BasicOperator to, final RuleResult rr) {
		this.waitForAction();
		this.endOfStep(new StepContainer(from, to, rr, false));
	}
}
