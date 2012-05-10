package lupos.gui.operatorgraph.visualeditor;

import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import lupos.gui.operatorgraph.visualeditor.util.StatusBar;

public class VisualEditorFrame<T> extends JFrame {
	private static final long serialVersionUID = -2348397969234212569L;

	public VisualEditorFrame(VisualEditor<T> editor, String title, Image image, boolean standAlone) {
		this(editor, title, image, standAlone, true);
	}

	public VisualEditorFrame(VisualEditor<T> editor, String title, Image image, boolean standAlone, boolean showMenuBar) {
		if(standAlone) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		}
		else {
			this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		}

		if(image != null) {
			this.setIconImage(image);
		}

		if(showMenuBar) {
			this.setJMenuBar(editor.buildMenuBar());
		}

		StatusBar statusBar = editor.getStatusBar();

		if(statusBar != null) {
			this.getContentPane().add(statusBar, BorderLayout.SOUTH);
		}

		this.getContentPane().add(editor, BorderLayout.CENTER);
		this.setTitle(title);
		this.setSize(1000, 600);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}