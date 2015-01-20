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
 */
package lupos.distributed.p2p.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.NumberFormatter;

import lupos.datastructures.items.Triple;
import lupos.distributed.p2p.network.AbstractP2PNetwork;
import lupos.distributed.query.QueryClient;

/**
 * This class asks when instanciating a p2p network, how to configure or uses an
 * already created evaluator.
 *
 */
public abstract class P2PConfigFrame {

	/*
	 * Static model of all started evaluators
	 */
	private static P2PInstanceTableModel model = new P2PInstanceTableModel();

	/*
	 * Returns the table model
	 */
	private static P2PInstanceTableModel getTableModel() {
		return model;
	}

	/*
	 * Adds a new evaluator to the stored list
	 */
	private static void addRunningEvaluator(final PeerItem item) {
		getTableModel().addRow(item);
	}

	/**
	 * This is an item storing information about the instantiated evaluator and
	 * p2p-net
	 */
	public class PeerItem {
		public Date instanciationTime;
		public int instanceCount;
		public String networkName;
		public String distributionStrategy;
		public int port;
		public boolean isMaster = true;
		public String masterName;
		public int masterPort;
		public QueryClient queryEvaluator;
		public AbstractP2PNetwork<Triple> network;
		public boolean useSubgraphSubmission = true;
	}

	/**
	 * This is the {@link TableModel} that is used to visualize the data
	 */
	private static class P2PInstanceTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -1706077155171337906L;
		/*
		 * Headings
		 */
		private final String[] tableHeaders = { "", "Network", "Distribution", "SubgraphSubm.",
				"Port", "Connected with", "Creation Time" };
		private final List<PeerItem> items = new ArrayList<PeerItem>();
		private final AtomicInteger counter = new AtomicInteger(0);

		/**
		 * Returns the item on the given row
		 *
		 * @param row
		 *            the row
		 * @return the item which is in the given row
		 */
		public PeerItem getItem(final int row) {
			if (row >= this.getRowCount()) {
				return null;
			}
			return this.items.get(row);
		}

		/**
		 * Adds a new item to the row
		 *
		 * @param pc
		 *            the item to be added
		 *
		 */
		public void addRow(final PeerItem pc) {
			if (this.items.contains(pc)) {
				return;
			}
			/*
			 * just use a counter to numberate the evaluators
			 */
			pc.instanceCount = this.counter.incrementAndGet();
			pc.instanciationTime = new Date();
			this.items.add(pc);
			this.fireTableRowsInserted(this.getRowCount() - 1, this.getRowCount() - 1);
		}

		/**
		 * Removes an item out of the row
		 *
		 * @param item
		 *            the item to be removed
		 */
		public void deleteRow(final PeerItem item) {
			for (int rowIndex = this.items.size() - 1; rowIndex >= 0; rowIndex--) {
				if (this.items.get(rowIndex).equals(item)) {
					// kill item
					if (item.network != null) {
						item.network.shutdown();
					}
					item.queryEvaluator = null;
					this.items.remove(rowIndex);
				}
			}
			this.fireTableDataChanged();
		}

		@Override
		public String getColumnName(final int columnIndex) {
			return this.tableHeaders[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return 7;
		}

		@Override
		public int getRowCount() {
			return this.items.size();
		}

		@Override
		public Object getValueAt(final int row, final int column) {
			if (row < this.getRowCount()) {
				// return the information of the selected item and column of the
				// table
				final PeerItem pc = this.items.get(row);
				switch (column) {
				case 0:
					return pc.instanceCount;
				case 1:
					return pc.networkName;
				case 2:
					return pc.distributionStrategy;
				case 3:
					return pc.useSubgraphSubmission ;
				case 4:
					return pc.port;
				case 5:
					if (pc.isMaster) {
						return "";
					} else {
						/* display master-ip and port the peer is connected to */
						return String.format("%s:%d", pc.masterName, pc.masterPort);
					}
				case 6:
					return pc.instanciationTime;
				}
			}
			return null;
		}

	};

	/**
	 * New instance
	 */
	public P2PConfigFrame() {
	}

	/**
	 * is executed, if clicked on an already started evaluator.<br>
	 * <br>
	 * <B>Note:</B>If evaluator can be used, the returned item is the given
	 * argument, otherwise an error is to throw.<br>
	 *
	 * @param evaluator
	 *            the item to be clicked on
	 * @return the peer item to be used
	 * @throws Exception
	 *             error, if the given evaluator cannot be used, because
	 *             different distribution strategy or wrong P2P network
	 *             implementation
	 */
	public abstract PeerItem onQueryEvaluator(PeerItem evaluator)
			throws Exception;

	/**
	 * a new instance of an evaluator and p2p-network is to be instantiated by
	 * the implementation and a new {@link PeerItem} is to return
	 *
	 * @param port
	 *            the port, the network should listen to
	 */
	public abstract PeerItem onLocalInstance(int port);

	/**
	 * a new instance of an evaluator and p2p-network is to be instantiated by
	 * the implementation and a new {@link PeerItem} is to return
	 *
	 * @param localPort
	 *            the port, the network should listen to
	 * @param masterPort
	 *            the port of the master's peer
	 * @param masterAddress
	 *            the ip/server-name of the master's peer
	 */
	public abstract PeerItem onMasterInstance(int localPort, int masterPort,
			String masterAddress);

	/**
	 * the dialog was cancelled.
	 */
	public abstract void onCancel();

	/**
	 * Shows the dialog to select the P2P-network / evaluator which is to be
	 * used.
	 */
	public PeerItem showDialog() {
		//store the result here
		final PeerItem[] result = new PeerItem[1];

		//create modal dialog
		final JDialog frame = new JDialog();
		frame.setLayout(new BorderLayout());

		/* Panel on top */
		final Border borderTop = new TitledBorder(BorderFactory.createEtchedBorder(),
				"Create new evalator", TitledBorder.LEADING, TitledBorder.TOP);
		final JPanel createEvalatorPanel = new JPanel();
		createEvalatorPanel.setBorder(borderTop);
		createEvalatorPanel.setLayout(new BoxLayout(createEvalatorPanel,
				BoxLayout.PAGE_AXIS));
		final JPanel pnlLocalPeer = new JPanel();
		pnlLocalPeer
				.setLayout(new BoxLayout(pnlLocalPeer, BoxLayout.PAGE_AXIS));
		pnlLocalPeer.add(new JLabel("Select port where to start peer:"));
		final JFormattedTextField localPort = new JFormattedTextField(
				new NumberFormatter());
		localPort.setValue(11111);
		pnlLocalPeer.add(localPort);
		final JPanel pnlMasterPeer = new JPanel();
		pnlMasterPeer.setLayout(new BoxLayout(pnlMasterPeer,
				BoxLayout.PAGE_AXIS));
		final JCheckBox connectMaster = new JCheckBox(
				"Should be connected with master peer?");
		pnlMasterPeer.add(connectMaster);
		final JPanel masterConfiguration = new JPanel();
		masterConfiguration.setLayout(new BoxLayout(masterConfiguration,
				BoxLayout.PAGE_AXIS));
		masterConfiguration.setVisible(false);
		pnlMasterPeer.add(masterConfiguration);
		connectMaster.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				//show master-configuration only if is to be connected to master peer
				masterConfiguration.setVisible(connectMaster.isSelected());
			}
		});
		masterConfiguration
				.add(new JLabel("Select master-port to connect to:"));
		final JFormattedTextField masterPort = new JFormattedTextField(
				new NumberFormatter());
		masterPort.setValue(11111);
		masterConfiguration.add(masterPort);
		masterConfiguration.add(new JLabel(
				"Select master-address/ip to connect to:"));
		final JTextField masterAddress = new JTextField();
		masterAddress.setText("127.0.0.1");
		masterConfiguration.add(masterAddress);
		createEvalatorPanel.add(pnlLocalPeer);
		createEvalatorPanel.add(pnlMasterPeer);

		/* Panel with JTable in the center */
		final JTable tbl = new JTable(model);
		final Border border = new TitledBorder(BorderFactory.createEtchedBorder(),
				"Select allready running evaluators", TitledBorder.LEADING,
				TitledBorder.TOP);
		final JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.setBorder(border);
		tablePanel.add(new JScrollPane(tbl), BorderLayout.CENTER);
		tablePanel.add(new JLabel(
				"Double-click on an evaluator, to use this instance."),
				BorderLayout.SOUTH);

		/* Panel on southern */
		final JPanel pnlControls = new JPanel();
		pnlControls.setLayout(new BoxLayout(pnlControls, BoxLayout.LINE_AXIS));
		pnlControls.setAlignmentX(Component.RIGHT_ALIGNMENT);
		final JButton ok = new JButton("OK");
		final JButton cancel = new JButton("Cancel");
		final JLabel label = new JLabel();
		label.setVisible(false);

		/*
		 * listeners
		 */
		tbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent me) {
				final JTable table = (JTable) me.getSource();
				final Point p = me.getPoint();
				final int row = table.rowAtPoint(p);
				/*
				 * On double-click of the table item ...
				 */
				if (me.getClickCount() == 2) {
					final PeerItem eval = model.getItem(row);
					try {
						/*
						 * ask whether this evaluator can be used
						 */
						result[0] = P2PConfigFrame.this.onQueryEvaluator(eval);
					} catch (final Exception exception) {
						label.setText("Error: " + exception.getLocalizedMessage());
						label.setVisible(true);
						return;
					}
					//hide dialog
					frame.setVisible(false);
				}
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				/*
				 * inform and close dialog
				 */
				P2PConfigFrame.this.onCancel();
				frame.setVisible(false);
			}
		});
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				int _localPort;
				int _masterPort;
				String _masterAddress;

				/* is input integer? */
				Object val = localPort.getValue();
				try {
					if (val instanceof Long) {
						_localPort = ((Long) val).intValue();
					} else if (val instanceof Integer) {
						_localPort = (Integer) val;
					} else {
						_localPort = Integer.parseInt((String) val);
					}
					if (_localPort > 65535 || _localPort < 0) {
						throw new NumberFormatException("Ports must be in [0,65535]");
					}
				} catch (final NumberFormatException exception) {
					label.setText("Error: " + exception.getLocalizedMessage());
					label.setVisible(true);
					return;
				}
				if (connectMaster.isSelected()) {
					/* is input integer? */
					val = masterPort.getValue();
					try {
						if (val instanceof Long) {
							_masterPort = ((Long) val).intValue();
						} else if (val instanceof Integer) {
							_masterPort = (Integer) val;
						} else {
							_masterPort = Integer.parseInt((String) val);
						}
						if (_localPort > 65535 || _localPort < 0) {
							throw new NumberFormatException("Ports must be in [0,65535]");
						}
					} catch (final NumberFormatException exception) {
						label.setText("Error: " + exception.getLocalizedMessage());
						label.setVisible(true);
						return;
					}
					if (masterAddress.getText().isEmpty()) {
						label.setText("Error: Unknown master address.");
						label.setVisible(true);
						return;
					} else {
						_masterAddress = masterAddress.getText();
					}
					//create new instance by implementation
					result[0] = P2PConfigFrame.this.onMasterInstance(
							_localPort, _masterPort, _masterAddress);
				} else {
					//create new instance by implementation
					result[0] = P2PConfigFrame.this.onLocalInstance(_localPort);
				}
				frame.setVisible(false);
			}
		});
		pnlControls.add(label);
		pnlControls.add(ok);
		pnlControls.add(cancel);

		frame.setModal(true);
		frame.add(createEvalatorPanel, BorderLayout.NORTH);
		frame.add(tablePanel, BorderLayout.CENTER);
		frame.add(pnlControls, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
		/*
		 * return the result query evaluator
		 */
		if (result[0] != null) {
			/* store new evaluator in our list */
			if (result[0].queryEvaluator != null) {
				P2PConfigFrame.addRunningEvaluator(result[0]);
			}
		}
		return result[0];
	}

}
