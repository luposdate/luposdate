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

import lupos.gui.Demo_Applet;

/**
 * This class is just for starting the Demo_Applet with our new distributed
 * query evaluators (without and with different distribution strategies)...
 *
 * @author groppe
 * @version $Id: $Id
 */
public class Start_Demo_Applet_DE {

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public static void main(final String[] args) throws ClassNotFoundException {

		/*
		 * if the distribution strategy is choosen in UI seperatly, this can be used:
		 * now the default distribution strategy is NinefoldInsertion
		 */
		Demo_Applet
		.registerEvaluator(
				"TomP2P",
				lupos.distributed.p2p.query.withsubgraph.TomP2P_QueryClient.class);

		Demo_Applet
		.registerEvaluator(
				"Chordless",
				lupos.distributed.p2p.query.withsubgraph.Chordless_QueryClient.class);

		/* the old ones */

		Demo_Applet
		.registerEvaluator(
				"TomP2P without SubgraphSubmission / SimplePartitionDistribution",
				lupos.distributed.p2p.query.TomP2P_WithSimplePartitionDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"TomP2P without SubgraphSubmission / TwoKeysDistribution",
				lupos.distributed.p2p.query.TomP2P_WithTwoKeysDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"TomP2P without SubgraphSubmission / OneToThreeKeysDistribution",
				lupos.distributed.p2p.query.TomP2P_WithOneToThreeKeysDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"TomP2P without SubgraphSubmission / OneKeyDistribution",
				lupos.distributed.p2p.query.TomP2P_WithOneKeyDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"TomP2P without SubgraphSubmission / NinefoldDistribution",
				lupos.distributed.p2p.query.TomP2P_NinefoldInsertionDistribution.class);

		Demo_Applet
		.registerEvaluator(
				"Chordless without SubgraphSubmission / SimplePartitionDistribution",
				lupos.distributed.p2p.query.Chordless_WithSimplePartitionDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"Chordless without SubgraphSubmission / TwoKeysDistribution",
				lupos.distributed.p2p.query.Chordless_WithTwoKeysDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"Chordless without SubgraphSubmission / OneToThreeKeysDistribution",
				lupos.distributed.p2p.query.Chordless_WithOneToThreeKeysDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"Chordless without SubgraphSubmission / OneKeyDistribution",
				lupos.distributed.p2p.query.Chordless_WithOneKeyDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"Chordless without SubgraphSubmission / NinefoldDistribution",
				lupos.distributed.p2p.query.Chordless_NinefoldInsertionDistribution.class);



		Demo_Applet
		.registerEvaluator(
				"TomP2P with SubgraphSubmission / SimplePartitionDistribution",
				lupos.distributed.p2p.query.withsubgraph.TomP2P_WithSimplePartitionDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"TomP2P with SubgraphSubmission / TwoKeysDistribution",
				lupos.distributed.p2p.query.withsubgraph.TomP2P_WithTwoKeysDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"TomP2P with SubgraphSubmission / OneToThreeKeysDistribution",
				lupos.distributed.p2p.query.withsubgraph.TomP2P_WithOneToThreeKeysDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"TomP2P with SubgraphSubmission / OneKeyDistribution",
				lupos.distributed.p2p.query.withsubgraph.TomP2P_WithOneKeyDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"TomP2P with SubgraphSubmission / NinefoldDistribution",
				lupos.distributed.p2p.query.withsubgraph.TomP2P_NinefoldInsertionDistribution.class);

		Demo_Applet
		.registerEvaluator(
				"Chordless with SubgraphSubmission / SimplePartitionDistribution",
				lupos.distributed.p2p.query.withsubgraph.Chordless_WithSimplePartitionDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"Chordless with SubgraphSubmission / TwoKeysDistribution",
				lupos.distributed.p2p.query.withsubgraph.Chordless_WithTwoKeysDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"Chordless with SubgraphSubmission / OneToThreeKeysDistribution",
				lupos.distributed.p2p.query.withsubgraph.Chordless_WithOneToThreeKeysDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"Chordless with SubgraphSubmission / OneKeyDistribution",
				lupos.distributed.p2p.query.withsubgraph.Chordless_WithOneKeyDistribution.class);
		Demo_Applet
		.registerEvaluator(
				"Chordless with SubgraphSubmission / NinefoldDistribution",
				lupos.distributed.p2p.query.withsubgraph.Chordless_NinefoldInsertionDistribution.class);

		Demo_Applet.main(args);
	}

}
