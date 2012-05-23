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