/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.RIFParser;
import lupos.gui.anotherSyntaxHighlighting.javacc.SPARQLParser;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.viewer.Viewer;
import lupos.gui.operatorgraph.viewer.ViewerPrefix;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;

public abstract class DebugViewerCreator {
	
	private final boolean fromJar;
	private final ViewerPrefix viewerPrefix;
	private final BooleanReference usePrefixes;
	private final RulesGetter rulesGetter;
	private final Image icon;
	
	public DebugViewerCreator(final boolean fromJar, final ViewerPrefix viewerPrefix, final BooleanReference usePrefixes, final RulesGetter rulesGetter, final Image icon){
		this.fromJar = fromJar;
		this.viewerPrefix = viewerPrefix;
		this.usePrefixes = usePrefixes; 
		this.rulesGetter = rulesGetter;
		this.icon = icon;
	}
	
	public abstract GraphWrapper getASTGraphWrapper();

	public abstract GraphWrapper getASTCoreGraphWrapper();

	public abstract String getCore();

	public abstract String queryOrRule();

	/**
	 * create the button to show the operator graph.
	 */
	public JButton createASTButton() {
		// create operatorgraph-button, add actionListener and add it to
		// Applet...
		final GraphWrapper graphWrapper = getASTGraphWrapper();
		final JButton bt_AST = new JButton("Show AST of " + queryOrRule());
		bt_AST.setMargin(new Insets(0, 0, 0, 0));
		bt_AST.setEnabled(graphWrapper != null);

		if (graphWrapper != null) {
			bt_AST.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent arg0) {
					new Viewer(graphWrapper, "Abstract syntax tree of the "
							+ queryOrRule(), false,
							DebugViewerCreator.this.fromJar);
				}
			});
		}

		return bt_AST;
	}

	public JButton createCoreSPARQLQueryButton() {
		// create coreSPARQLQuery-button, add actionListener and add it to
		// Applet...
		final String core = getCore();
		if(core==null){
			return null;
		}
		final JButton bt_coreSPARQLQuery = new JButton("Show Core "
				+ queryOrRule());
		bt_coreSPARQLQuery.setEnabled(true);
		bt_coreSPARQLQuery.setMargin(new Insets(0, 0, 0, 0));

		bt_coreSPARQLQuery.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				final JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());

				final LuposDocument document = new LuposDocument();
				final JTextPane tp_coreSPARQLQuery = new LuposJTextPane(document);
				document.init(SPARQLParser.createILuposParser(new LuposDocumentReader(document)), false);

				tp_coreSPARQLQuery.setFont(new Font("Courier New",
						Font.PLAIN, 12));
				tp_coreSPARQLQuery.setText(core);
				tp_coreSPARQLQuery.setEditable(false);

				final JScrollPane scroll = new JScrollPane(
						tp_coreSPARQLQuery);

				panel.add(scroll);

				final JFrame frame1 = new JFrame("Core " + queryOrRule());

				frame1.setIconImage(icon);

				frame1.setSize(794, 200);
				frame1.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
				frame1.getContentPane().add(panel);
				frame1.pack();
				frame1.setLocationRelativeTo(null);
				frame1.setVisible(true);
			}
		});

		return bt_coreSPARQLQuery;
	}
	
	public JButton createInferenceRulesButton(final String inferenceRulesParameter) {
		// create coreSPARQLQuery-button, add actionListener and add it to Applet...
		final JButton bt_InferenceRules = new JButton("Show Rules");
		bt_InferenceRules.setEnabled(inferenceRulesParameter != null);
		bt_InferenceRules.setMargin(new Insets(0, 0, 0, 0));

		if (inferenceRulesParameter != null) {
			bt_InferenceRules.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent arg0) {
					final JPanel panel = new JPanel();
					panel.setLayout(new BorderLayout());

					final LuposDocument document = new LuposDocument();
					final JTextPane tp_coreSPARQLQuery = new LuposJTextPane(document);
					document.init(RIFParser.createILuposParser(new LuposDocumentReader(document)), false);

					tp_coreSPARQLQuery.setFont(new Font("Courier New", Font.PLAIN, 12));
					tp_coreSPARQLQuery.setText(inferenceRulesParameter);
					tp_coreSPARQLQuery.setEditable(false);

					final JScrollPane scroll = new JScrollPane(tp_coreSPARQLQuery);

					panel.add(scroll);

					final JFrame frame1 = new JFrame("Inference Rules");

					frame1.setIconImage(icon);

					frame1.setSize(794, 200);
					frame1.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
					frame1.getContentPane().add(panel);
					frame1.pack();
					frame1.setLocationRelativeTo(null);
					frame1.setVisible(true);
				}
			});
		}

		return bt_InferenceRules;
	}

	/**
	 * create the button to show the operator graph.
	 */
	public JButton createASTCoreSPARQLButton() {
		// create operatorgraph-button, add actionListener and add it to
		// Applet...
		final GraphWrapper graphWrapper = getASTCoreGraphWrapper();
		final JButton bt_coreAST = new JButton("Show AST Core "
				+ queryOrRule());
		bt_coreAST.setMargin(new Insets(0, 0, 0, 0));
		bt_coreAST.setEnabled(graphWrapper != null);

		if (graphWrapper != null) {
			bt_coreAST.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent ae) {
					new Viewer(graphWrapper, "Abstract syntax tree of the Core " + queryOrRule(), false, DebugViewerCreator.this.fromJar);
				}
			});
		}

		return bt_coreAST;
	}

	/**
	 * create the button to show the operator graph.
	 */
	public JButton createOperatorGraphButton() {
		return this.createOperatorGraphButton(this.rulesGetter.getRuleApplications());
	}

	public JButton createOperatorGraphButton(final List<DebugContainer<BasicOperatorByteArray>> ruleApplicationsParameter) {
		// create OperatorGraph-button, add actionListener and add it to
		// Applet...
		final JButton bt_opgraph = new JButton("Show Operator Graph");
		bt_opgraph.setMargin(new Insets(0, 0, 0, 0));
		bt_opgraph.setEnabled(ruleApplicationsParameter != null);
		if (ruleApplicationsParameter != null) {
			bt_opgraph.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent ae) {
					ViewerPrefix vp = (DebugViewerCreator.this.viewerPrefix==null)? new ViewerPrefix(DebugViewerCreator.this.usePrefixes.isTrue(), null) : DebugViewerCreator.this.viewerPrefix;
					new Viewer(ruleApplicationsParameter, "OperatorGraph", false,
							DebugViewerCreator.this.fromJar, vp);
				}
			});
		}

		return bt_opgraph;
	}

	public List<DebugContainer<BasicOperatorByteArray>> getCorrectOperatorGraphRules() {
		return null;
	}
	
	public static interface RulesGetter {
		public List<DebugContainer<BasicOperatorByteArray>> getRuleApplications(); 
	}
}
