package lupos.distributed.gui;

import lupos.distributed.query.QueryClient;

public class Demo_Applet {

	public static void main(String[] args){
		lupos.gui.Demo_Applet.registerEvaluator("Distributed Evaluator", QueryClient.class);
		lupos.gui.Demo_Applet.main(args);
	}
}
