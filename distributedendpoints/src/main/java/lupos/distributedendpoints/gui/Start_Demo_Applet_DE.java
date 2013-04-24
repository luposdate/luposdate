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
package lupos.distributedendpoints.gui;

import javax.swing.JOptionPane;

import lupos.distributedendpoints.query.withoutsubgraphsubmission.QueryClient_DE;
import lupos.distributedendpoints.query.withoutsubgraphsubmission.QueryClient_DE_OneKeyDistribution;
import lupos.distributedendpoints.query.withoutsubgraphsubmission.QueryClient_DE_OneToThreeKeysDistribution;
import lupos.distributedendpoints.query.withoutsubgraphsubmission.QueryClient_DE_TwoKeysDistribution;
import lupos.distributedendpoints.query.withsubgraphsubmission.QueryClient_DE_SG_OneKeyDistribution;
import lupos.distributedendpoints.query.withsubgraphsubmission.QueryClient_DE_SG_OneToThreeKeysDistribution;
import lupos.distributedendpoints.query.withsubgraphsubmission.QueryClient_DE_SG_TwoKeysDistribution;
import lupos.gui.Demo_Applet;

/**
 * This class is just for starting the Demo_Applet with our new distributed query evaluators (without and with different distribution strategies)...
 */
public class Start_Demo_Applet_DE {

	public static void main(final String[] args) throws ClassNotFoundException {
		Demo_Applet.registerEvaluator("Distributed Evaluator (without distribution strategy)", QueryClient_DE.class);
		Demo_Applet.registerEvaluator("Distributed Evaluator (one key distribution)", QueryClient_DE_OneKeyDistribution.class);
		Demo_Applet.registerEvaluator("Distributed Evaluator (two keys distribution)", QueryClient_DE_TwoKeysDistribution.class);
		Demo_Applet.registerEvaluator("Distributed Evaluator (one to three keys distribution)", QueryClient_DE_OneToThreeKeysDistribution.class);
		Demo_Applet.registerEvaluator("Distributed Evaluator with subgraph submission (one key distribution)", QueryClient_DE_SG_OneKeyDistribution.class);
		Demo_Applet.registerEvaluator("Distributed Evaluator with subgraph submission (two keys distribution)", QueryClient_DE_SG_TwoKeysDistribution.class);
		Demo_Applet.registerEvaluator("Distributed Evaluator with subgraph submission (one to three keys distribution)", QueryClient_DE_SG_OneToThreeKeysDistribution.class);
		Demo_Applet.main(args);
	}

	public static boolean askForHistogramRequests(){
		final Object[] options = {"Histogram Requests", "Static Analysis"};
		final int ret = JOptionPane.showOptionDialog(
				null,
				"Use histogram requests during join order optimization\n(leads to better optimization results, but also to more messages)\nor a static analysis (no extra messages)",
				"Histogram requests or static analysis",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options, options[0]);

		return (ret == JOptionPane.YES_OPTION);
	}
}
