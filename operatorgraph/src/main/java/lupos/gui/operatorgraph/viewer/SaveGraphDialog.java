package lupos.gui.operatorgraph.viewer;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Class to create the FileChooser for saving the graph.
 * 
 */
public class SaveGraphDialog extends JFileChooser {
	private static final long serialVersionUID = -9182650514741475066L;

	public SaveGraphDialog() {
		this.setDialogTitle("Save as... PNG, JPG or GIF");
	}

	public void approveSelection() {
		// if choosen file name already exists...
		if(new File(this.getSelectedFile().getAbsolutePath()).exists()) {
			// specify button lables for dialog...
			final Object[] buttonLables = { "Overwrite", "Cancel" };

			// create dialog to warn user and get return value...
			final int ret = JOptionPane.showOptionDialog(
					this,
					"A file with the choosen file name already exists!\nWould you like to overwrite it?",
					"", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null,
					buttonLables, buttonLables[1]);

			if(ret == JOptionPane.OK_OPTION) { // if user choose to overwrite...
				super.approveSelection(); // go on and save image
			}
		}
		else { // choosen file name does not exist...
			super.approveSelection(); // go on and save image
		}
	}
}