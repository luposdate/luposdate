package lupos.gui.operatorgraph.visualeditor.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class SaveDialog extends JFileChooser {
	private static final long serialVersionUID = 1L;

	public SaveDialog(String path) {
		super(path);
	}

	public void approveSelection() {
		// if chosen file name already exists...
		if(new File(this.getSelectedFile().getAbsolutePath()).exists()) {
			// create dialog to warn user and get return value...
			int ret = JOptionPane.showOptionDialog(this,
					"An element with the choosen name already exists!\nWould you like to overwrite it?",
					"Overwrite?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					new Object[] { "Overwrite", "Cancel" }, 0);

			if(ret == JOptionPane.OK_OPTION) { // if user choose to overwrite...
				super.approveSelection(); // go on and save image
			}
		}
		else { // chosen file name does not exist...
			super.approveSelection(); // go on and save image
		}
	}
}