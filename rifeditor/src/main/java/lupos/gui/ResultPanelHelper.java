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
package lupos.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.TurtleParser;
import lupos.gui.debug.ShowResult;
import lupos.gui.operatorgraph.viewer.ViewerPrefix;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.rif.BasicIndexRuleEvaluator;
import lupos.rif.datatypes.RuleResult;
import lupos.sparql1_1.ASTAs;
import lupos.sparql1_1.ASTSelectQuery;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;

public final class ResultPanelHelper {
	/**
	 * This is just for external use an easy way to display the result of a RIF rules application... 
	 */
	public static void appyRIFRules(final String ruleset, final JPanel resultpanel, final BooleanReference usePrefixes,  final ViewerPrefix prefixInstance) throws Exception {		
		final BasicIndexRuleEvaluator ruleEvaluator = new BasicIndexRuleEvaluator();
		ruleEvaluator.prepareInputData(new LinkedList<URILiteral>(), new LinkedList<URILiteral>());
		ruleEvaluator.compileQuery(ruleset);
		ruleEvaluator.logicalOptimization();
		ruleEvaluator.physicalOptimization();
		QueryResult[] resultQueryEvaluator = ruleEvaluator.getResults();
		
		ResultPanelHelper.setupResultPanel(resultpanel, resultQueryEvaluator, null, null, null, null, null, usePrefixes, prefixInstance, resultpanel);
	}

	public static void setupResultPanel(final JPanel resultpanel, final QueryResult[] resultQueryEvaluator, final DebugViewerCreator debugViewerCreator, final DebugViewerCreator materializationInfo, final String inferenceRules,  final List<DebugContainer<BasicOperatorByteArray>> ruleApplicationsForMaterialization, final RuleResult errorsInOntology, final BooleanReference usePrefixes,  final ViewerPrefix prefixInstance, final Container contentPane) throws Exception {
		resultpanel.removeAll();
		final FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 0, 0);
		final JPanel buttonpanel = new JPanel(layout);

		final JButton bt_AST = (debugViewerCreator!=null)? debugViewerCreator.createASTButton(): null;

		final JButton bt_coreQuery = (debugViewerCreator!=null)? debugViewerCreator.createCoreSPARQLQueryButton(): null;

		final JButton bt_coreAST = (debugViewerCreator!=null)? debugViewerCreator.createASTCoreSPARQLButton(): null;

		final List<String> resultOrder = new LinkedList<String>();

		// if AST exists...
		if (debugViewerCreator!=null && debugViewerCreator instanceof SPARQLDebugViewerCreator
				&& ((SPARQLDebugViewerCreator) debugViewerCreator).getAST() != null) {

			final Node ast = ((SPARQLDebugViewerCreator) debugViewerCreator).getAST(); // get AST

			// walk through first level children of AST...
			for (int i = 0; i < ast.jjtGetNumChildren(); ++i) {
				final Node child = ast.jjtGetChild(i); // get current child

				if (child instanceof ASTSelectQuery) {
					final ASTSelectQuery selectChild = (ASTSelectQuery) child;

					// SELECT is not the wildcard *...
					if (!selectChild.isSelectAll()) {
						// walk through select children...
						for (int j = 0; j < selectChild.jjtGetNumChildren(); ++j) {
							final Node selectChildChild = selectChild
							.jjtGetChild(j);

							// child of select is variable...
							if (selectChildChild instanceof ASTVar) {
								final ASTVar var = (ASTVar) selectChildChild;

								// add name of variable to order...
								if (!resultOrder.contains(var.getName())) {
									resultOrder.add(var.getName());
								}
							} else if (selectChildChild instanceof ASTAs) {
								for (int j1 = 0; j1 < selectChildChild
								.jjtGetNumChildren(); ++j1) {
									final Node selectChildChildChild = selectChildChild
									.jjtGetChild(j1);
									if (selectChildChildChild instanceof ASTVar) {
										final ASTVar var = (ASTVar) selectChildChildChild;

										// add name of variable to order...
										if (!resultOrder.contains(var
												.getName())) {
											resultOrder.add(var
													.getName());
										}
									}
								}
							}
						}
					}
				}
			}
		}

		final JButton bt_opgraph = (debugViewerCreator!=null)? debugViewerCreator.createOperatorGraphButton(): null;
		if(bt_AST!=null){
			buttonpanel.add(bt_AST);
		}
		if(bt_coreQuery!=null){
			buttonpanel.add(bt_coreQuery);
		}
		if(bt_coreAST!=null){
			buttonpanel.add(bt_coreAST);
		}
		if(bt_opgraph!=null){
			buttonpanel.add(bt_opgraph);
		}

		final JPanel buttonPanelInference = (materializationInfo!=null)? new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) : null;
		
		if(materializationInfo!=null){			
			buttonPanelInference.add(new JLabel("Inference:"));
			buttonPanelInference.add(materializationInfo.createInferenceRulesButton(inferenceRules));	
			buttonPanelInference.add(materializationInfo.createASTButton());
			buttonPanelInference.add(materializationInfo.createASTCoreSPARQLButton());
			buttonPanelInference.add(materializationInfo.createOperatorGraphButton(ruleApplicationsForMaterialization));
		}

		resultpanel.addHierarchyBoundsListener(new HierarchyBoundsListener() {

			@Override
			public void ancestorMoved(final HierarchyEvent e) {
				ResultPanelHelper.updateButtonPanelSize(layout, resultpanel, contentPane.getSize(), buttonpanel, buttonPanelInference);
			}

			@Override
			public void ancestorResized(final HierarchyEvent e) {
				this.ancestorMoved(e);
			}
		});
		
		JPanel buttonSuperPanel = new JPanel(new BorderLayout());
		buttonSuperPanel.add(buttonpanel, BorderLayout.NORTH);
		
		if(buttonPanelInference!=null){
			buttonSuperPanel.add(buttonPanelInference, BorderLayout.CENTER);
		}
		resultpanel.add(buttonSuperPanel, BorderLayout.NORTH);

		boolean tablesOccur = false;
		for (final QueryResult qr : resultQueryEvaluator) {
			if (!(qr instanceof BooleanResult) && (qr.size() > 0))
				tablesOccur = true;
		}

		JSplitPane splitPane_result = null;

		if (tablesOccur) {

			final JCheckBox cb_prefixes = new JCheckBox("Use prefixes", true);
			cb_prefixes.setSelected(usePrefixes.isTrue());
			cb_prefixes.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					// set boolean flag...
					if (e.getStateChange() == ItemEvent.SELECTED) {
						usePrefixes.setValue(true);
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						usePrefixes.setValue(false);
					}
					if (prefixInstance != null)
						prefixInstance.setStatus(usePrefixes.isTrue());
					resultpanel.removeAll();
					try {
						ResultPanelHelper.setupResultPanel(resultpanel, resultQueryEvaluator, debugViewerCreator, materializationInfo, inferenceRules,  ruleApplicationsForMaterialization, errorsInOntology, usePrefixes,  prefixInstance, contentPane );
					} catch(Exception exception){
						System.err.println("Should only occurr if it already occurred before:\n"+exception);
						exception.printStackTrace();
					}
					contentPane.validate();
				}
			});

			final JPanel prefixesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
			prefixesPanel.add(cb_prefixes);
			
			if(buttonSuperPanel!=null){
				buttonSuperPanel.add(prefixesPanel, BorderLayout.SOUTH);
			} else {
				buttonpanel.add(prefixesPanel);
			}

			if (usePrefixes.isTrue()) {
				final JPanel pPanel = new JPanel(new BorderLayout());

				final JLabel info = new JLabel();
				info.setText("Prefixes:");

				final JPanel infoPanel = new JPanel(new FlowLayout(
						FlowLayout.LEFT));
				infoPanel.add(info);
				pPanel.add(infoPanel, BorderLayout.NORTH);

				final LuposDocument document = new LuposDocument();
				final LuposJTextPane ta_prefixes = new LuposJTextPane(document);
				document.init(TurtleParser.createILuposParser(new LuposDocumentReader(document)), false);

				// just to get all prefixes before displaying them...
				for(QueryResult qr: resultQueryEvaluator){
					qr.toString(prefixInstance);
				}
				if(errorsInOntology!=null){
					errorsInOntology.toString(prefixInstance);
				}
				
				ta_prefixes.setText(prefixInstance.getPrefixString("", "").toString());
				ta_prefixes.setEditable(false);
				final JScrollPane scroll = new JScrollPane(ta_prefixes);
				pPanel.add(scroll, BorderLayout.CENTER);

				splitPane_result = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				splitPane_result.setOneTouchExpandable(true);
				splitPane_result.setContinuousLayout(true);
				splitPane_result.setTopComponent(pPanel);
				splitPane_result.setResizeWeight(0.15);

				resultpanel.add(splitPane_result, BorderLayout.CENTER);
			}
		}

		// --- output result table - end ---
		// final JScrollPane sp_result = new JScrollPane(resultTable);
		final QueryResult[] toDisplay;
		if(errorsInOntology!=null){
			toDisplay = new QueryResult[1+resultQueryEvaluator.length];
			toDisplay[0] = errorsInOntology;
			System.arraycopy(resultQueryEvaluator, 0, toDisplay, 1, resultQueryEvaluator.length);
		} else {
			toDisplay = resultQueryEvaluator;
		}

		final JScrollPane scrollpane = new JScrollPane(ShowResult.getResultPanel(errorsInOntology!=null, toDisplay, prefixInstance, resultOrder, null));

		if (usePrefixes.isTrue() && tablesOccur) {
			splitPane_result.setBottomComponent(scrollpane);
		} else {
			resultpanel.add(scrollpane, BorderLayout.CENTER);
		}

		ResultPanelHelper.updateButtonPanelSize(layout, resultpanel, contentPane.getSize(), buttonpanel, buttonPanelInference);
	}
	
	private final static int DELTA = 5;
	
	private static void updateButtonPanelSize(final FlowLayout layout, final JPanel resultpanel, final Dimension contentPaneSize, final JPanel ... buttonpanels) {
		for(JPanel buttonpanel: buttonpanels){
			if (buttonpanel != null && resultpanel != null) {
				final Dimension d = layout.minimumLayoutSize(buttonpanel); // for retrieving the height...
				// calculating number of rows
				int rows = 1;
				int x = 0;
				for(Component component: buttonpanel.getComponents()){
					x += component.getWidth();
					if(x>contentPaneSize.width-ResultPanelHelper.DELTA){ // component will be already displayed in next row?
						rows++;
						x=component.getWidth();
					}
				}
				final Dimension n = new Dimension(contentPaneSize.width, rows * d.height);
				// set the size of the button panel...
				buttonpanel.setPreferredSize(n);
			}
		}
	}
}
