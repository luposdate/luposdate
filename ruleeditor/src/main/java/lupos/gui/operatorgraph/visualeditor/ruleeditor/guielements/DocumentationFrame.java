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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;

public class DocumentationFrame extends JFrame {
	private static final long serialVersionUID = 7404279613683768486L;
	private JFrame that = this;

	public DocumentationFrame(final RuleEditor parent) {
		super();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.weightx = gbc.weighty = 1.0;
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);

		JPanel panel = new JPanel(new GridBagLayout());


		String path = System.getProperty("user.dir");

		final JTextField jTF_targetPath = new JTextField(path, 30);
		jTF_targetPath.setEditable(false);

		final JFileChooser chooser = new JFileChooser(path);

		JButton jB_targetPath = new JButton("Select");
		jB_targetPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int ret = chooser.showDialog(that, "Select");

				if(ret == JFileChooser.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					jTF_targetPath.setText(fileName);
				}
			}
		});


		final JTextField jTF_documentationTitle = new JTextField("LUPOSDATE Rule Documentation", 30);


		final JTextField jTF_ownImage = new JTextField(path, 30);
		jTF_ownImage.setEnabled(false);
		jTF_ownImage.setEditable(false);

		final JButton jB_ownImage = new JButton("Select");
		jB_ownImage.setEnabled(false);
		jB_ownImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int ret = chooser.showDialog(that, "Select");

				if(ret == JFileChooser.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					jTF_ownImage.setText(fileName);
				}
			}
		});


		final JRadioButton jRB_noImage = new JRadioButton("use no image");
		jRB_noImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				jTF_ownImage.setEnabled(false);
				jB_ownImage.setEnabled(false);
			}
		});

		final JRadioButton jRB_systemImage = new JRadioButton("use system image", true);
		jRB_systemImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				jTF_ownImage.setEnabled(false);
				jB_ownImage.setEnabled(false);
			}
		});

		final JRadioButton jRB_ownImage = new JRadioButton("use own image");
		jRB_ownImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				jTF_ownImage.setEnabled(true);
				jB_ownImage.setEnabled(true);
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(jRB_noImage);
		group.add(jRB_systemImage);
		group.add(jRB_ownImage);


		JButton jB_generate = new JButton("generate documentation");
		jB_generate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String imagePath = null;

				if(jRB_noImage.isSelected()) {
					imagePath = null;
				}
				else if(jRB_systemImage.isSelected()) {
					imagePath = "src/lupos/gui/operatorgraph/visualeditor/ruleeditor/images/logo.gif";
				}
				else if(jRB_ownImage.isSelected()) {
					imagePath = jTF_ownImage.getText();
				}

				parent.getDocumentationGenerator().generate(jTF_targetPath.getText(), jTF_documentationTitle.getText(), imagePath);

				that.setVisible(false);
			}
		});

		JButton jB_cancel = new JButton("cancel");
		jB_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				that.setVisible(false);
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(jB_generate);
		buttonPanel.add(jB_cancel);


		panel.add(new JLabel("Target directory:"), gbc);
		gbc.gridx++;
		panel.add(jTF_targetPath, gbc);
		gbc.gridx++;
		panel.add(jB_targetPath, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		panel.add(new JSeparator(), gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;
		panel.add(new JLabel("Documentation title:"), gbc);
		gbc.gridx++;
		gbc.gridwidth = 2;
		panel.add(jTF_documentationTitle, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		panel.add(new JSeparator(), gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 5, 0, 5);
		panel.add(jRB_noImage, gbc);
		gbc.gridy++;
		panel.add(jRB_systemImage, gbc);
		gbc.gridy++;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel.add(jRB_ownImage, gbc);
		gbc.gridx++;
		panel.add(jTF_ownImage, gbc);
		gbc.gridx++;
		panel.add(jB_ownImage, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.insets = new Insets(10, 5, 5, 5);
		panel.add(buttonPanel, gbc);


		this.setTitle("Documentation Generation");
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.getContentPane().add(panel);
		this.pack();
		this.setLocationRelativeTo(parent);
		this.setVisible(false);
	}
}