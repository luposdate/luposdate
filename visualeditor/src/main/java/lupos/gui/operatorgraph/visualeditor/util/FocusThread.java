package lupos.gui.operatorgraph.visualeditor.util;

import javax.swing.JTextField;

public class FocusThread extends Thread {
	private JTextField jtf;

	public FocusThread(JTextField jtf) {
		this.jtf = jtf;
	}

	public void run() {
		try {
			Thread.sleep(100);
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}

		this.jtf.grabFocus();
	}
}