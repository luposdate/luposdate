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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoin;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoinNonStandardSPARQL;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.operatorgraph.ServiceApproaches;

public class RDF3X_Client extends Demo_Applet {

	protected final JFileChooser fc;

	public static void main(final String args[]) {
		final Demo_Applet applet = new RDF3X_Client();
		if (args.length > 0) {
			Demo_Applet.startDemoAsApplication(args[0], applet);
		} else {
			Demo_Applet.startDemoAsApplication(applet);
		}
	}

	public RDF3X_Client(){
		 this.fc = new JFileChooser();
		 this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		 this.fc.setMultiSelectionEnabled(false);
	}


	@Override
	protected JPanel generateDataTab() {
		// just for initialization of some variables in the super class
		// -> quick and dirty, to be worked over!
		super.generateDataTab();

		// now the new code...
		final JPanel result = new JPanel(new FlowLayout());
		final JButton button = new JButton("Choose indices directory");
		button.addActionListener(new
				ActionListener(){
					@Override
					public void actionPerformed(final ActionEvent e) {
						final File oldFile = RDF3X_Client.this.fc.getSelectedFile();
						final int returnCode = RDF3X_Client.this.fc.showOpenDialog(RDF3X_Client.this);
						if(returnCode != JFileChooser.APPROVE_OPTION){
							RDF3X_Client.this.fc.setCurrentDirectory(oldFile);
						} else {
							RDF3X_Client.this.fc.setCurrentDirectory(RDF3X_Client.this.fc.getSelectedFile());
						}
					}
		});
		result.add(button);
		return result;
	}

	@Override
	protected void generateEvaluatorChooseAndPreferences() {
		// just for initialization of some variables in the super class
		// -> quick and dirty, to be worked over!
		this.cobo_evaluator = new JComboBox(this.getEvaluators());

		// now the new code for this class
		final JPanel rowpanel = new JPanel(new BorderLayout());

		rowpanel.add(this.generatePreferencesButton(), BorderLayout.EAST);

		this.masterpanel.add(rowpanel, BorderLayout.NORTH);
	}

	@Override
	public QueryEvaluator<Node> setupEvaluator(final EvaluationMode mode) throws Throwable {
		final ServiceApproaches serviceApproach = xpref.datatypes.EnumDatatype.getFirstValue("serviceCallApproach");
		final FederatedQueryBitVectorJoin.APPROACH bitVectorApproach = xpref.datatypes.EnumDatatype.getFirstValue("serviceCallBitVectorApproach");
		bitVectorApproach.setup();
		serviceApproach.setup();
		FederatedQueryBitVectorJoin.substringSize = xpref.datatypes.IntegerDatatype.getFirstValue("serviceCallBitVectorSize");
		FederatedQueryBitVectorJoinNonStandardSPARQL.bitvectorSize = FederatedQueryBitVectorJoin.substringSize;
		LiteralFactory.semanticInterpretationOfLiterals = xpref.datatypes.BooleanDatatype.getFirstValue("semanticInterpretationOfDatatypes");

		final RDF3XQueryEvaluator evaluator = new RDF3XQueryEvaluator();
		try {
			evaluator.loadLargeScaleIndices(this.fc.getSelectedFile().getCanonicalPath());
		} catch(final Exception e){
			this.displayDataErrorMessage(e.getMessage(), false);
		}
		return evaluator;
	}

	@Override
	protected void evaluate(final Evaluation evaluation, final EvaluationMode mode) {
		super.evaluate(new Evaluation_DisablePrepareInputData(evaluation), mode);
	}

	protected class Evaluation_DisablePrepareInputData extends Evaluation {

		protected final Evaluation evaluation;

		public Evaluation_DisablePrepareInputData(final Evaluation evaluation){
			this.evaluation = evaluation;
		}

		@Override
		public String getQuery() {
			return this.evaluation.getQuery();
		}

		@Override
		public long compileQuery(final String queryParameter) throws Exception {
			return this.evaluation.compileQuery(queryParameter);
		}

		@Override
		public DebugViewerCreator compileQueryDebugByteArray(final String queryParameter) throws Exception {
			return this.evaluation.compileQueryDebugByteArray(queryParameter);
		}

		@Override
		public JButton getButtonEvaluate() {
			return this.evaluation.getButtonEvaluate();
		}

		@Override
		public JButton getButtonEvalDemo() {
			return this.evaluation.getButtonEvalDemo();
		}

		@Override
		public JButton getButtonMeasureExecutionTimes() {
			return this.evaluation.getButtonMeasureExecutionTimes();
		}

		@Override
		public QueryEvaluator<Node> getEvaluator() {
			return this.evaluation.getEvaluator();
		}

		@Override
		public boolean evaluatorSuitableForDemo(final String s) {
			// RDF3X is suitable!
			return true;
		}

		@Override
		public boolean evaluatorSuitableForEvaluation(final String s) {
			// RDF3X is suitable!
			return true;
		}

		@Override
		public long prepareInputData(
				final Collection<URILiteral> defaultGraphsParameter,
				final LinkedList<URILiteral> namedGraphs) throws Exception {
			// already indices imported!
			return 0;
		}
	}
}
