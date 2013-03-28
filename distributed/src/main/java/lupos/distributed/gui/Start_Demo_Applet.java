/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.distributed.gui;

import lupos.engine.evaluators.QueryEvaluator;
import lupos.sparql1_1.Node;

/**
 * This class is for starting the Demo_Applet with a distributed query evaluator.
 */
public class Start_Demo_Applet {

	/**
	 * This method starts the Demo_Applet. It is assumed that the first command line argument contains
	 * the class name of the distributed query evaluator.
	 * @param args command line arguments
	 * @throws ClassNotFoundException
	 */
	public static void start(final String[] args) throws ClassNotFoundException {
		String[] argsWithoutFirst = new String[args.length-1];
		System.arraycopy(args, 1, argsWithoutFirst, 0, args.length-1);
		Start_Demo_Applet.start(args[0], argsWithoutFirst);
	}

	/**
	 * This method starts the Demo_Applet.
	 * @param evaluator the class name of the distributed query evaluator
	 * @param args command line arguments
	 * @throws ClassNotFoundException
	 */
	public static void start(final String evaluator, final String[] args) throws ClassNotFoundException {
		@SuppressWarnings("unchecked")
		Class<? extends QueryEvaluator<Node>> evaluatorClass = (Class<? extends QueryEvaluator<Node>>) Class.forName(evaluator);
		Start_Demo_Applet.start(evaluatorClass, args);
	}
	
	/**
	 * This method starts the Demo_Applet.
	 * @param evaluator the distributed query evaluator class
	 * @param args the command line arguments
	 */
	public static void start(final Class<? extends QueryEvaluator<Node>> evaluator, final String[] args) {
		lupos.gui.Demo_Applet.registerEvaluator("Distributed Evaluator", evaluator);
		lupos.gui.Demo_Applet.main(args);
	}

	/**
	 * Main method to start the Demo_Applet. It is assumed that the first command line argument contains
	 * the class name of the distributed query evaluator.
	 * @param args command line arguments
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		Start_Demo_Applet.start(args);
	}
}
