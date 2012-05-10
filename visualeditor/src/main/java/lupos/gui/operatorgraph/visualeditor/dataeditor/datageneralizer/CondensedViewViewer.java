package lupos.gui.operatorgraph.visualeditor.dataeditor.datageneralizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.dataeditor.guielements.DataGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.StatusBar;

public class CondensedViewViewer extends VisualEditor<Operator> {
	private static final long serialVersionUID = 7155983501843091279L;
	private JButton copyButton;
	private JFrame frame;

	public CondensedViewViewer(final Prefix prefix, final boolean standAlone,
			final Image image, final JPanel toolbar) {
		super(standAlone);

		this.visualGraphs.add(new DataGraph(this, prefix));
		this.frame = this.createMainWindowSingleGraph("LUPOSDATE-Condensed Data", true,
				image);

		this.frame.getContentPane().add(toolbar, BorderLayout.SOUTH);

		this.finalizeFrame(toolbar);

		this.frame.setVisible(true);
	}

	public StatusBar getStatusBar() {
		return null;
	}

	private void finalizeFrame(final JPanel... panels) {
		int minWidth = 0;
		int minHeight = 0;

		for (int i = 0; i < panels.length; i += 1) {
			final JPanel panel = panels[i];

			minWidth = Math.max(minWidth, panel.getPreferredSize().width);
			minHeight += panel.getPreferredSize().height;
		}

		this.frame.setMinimumSize(new Dimension(minWidth + 30, minHeight + 10));
		this.frame.setSize(1000, 600);
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
	}

	public JMenuBar buildMenuBar() {
		return null;
	}

	public JPanel createTopToolBar() {
		final JPanel toolBar = super.createTopToolBar();
		
		final JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(toolBar, BorderLayout.NORTH);
		JPanel panel2=new JPanel(new FlowLayout());
		panel2.add(this.createCopyButton());
		panel.add(panel2, BorderLayout.SOUTH);
		
		return panel;
	}

	/**
	 * Internal method to create the button to arrange the current graph.
	 * 
	 * @return the arrange button
	 */
	private JButton createCopyButton() {
		this.copyButton = new JButton("copy");
		this.copyButton.setEnabled(false);
		this.copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				copyElements();
			}
		});
		return this.copyButton;
	}

	protected void manageMenuItems() {
		super.manageMenuItems();

		if (this.selectedOperatorsList.size() > 0) {
			this.copyButton.setEnabled(true);
		} else {
			this.copyButton.setEnabled(false);
		}
	}

	protected void pasteElements(final String content) {
		System.err.println("Nothing can be pasted into the Condensed View!");
	}
}
