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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;

public class RuleMouseListener implements MouseListener {
	private AbstractGuiComponent<Operator> parent;
	private MouseListener[] listeners;

	public RuleMouseListener(AbstractGuiComponent<Operator> parent, JComponent comp) {
		this.parent = parent;

		this.listeners = comp.getMouseListeners();

		for(MouseListener l : this.listeners) {
			comp.removeMouseListener(l);
		}
	}

	public void mouseClicked(MouseEvent me) {
		boolean ret = this.parent.handleConnectionMode();

		if(!ret) {
			for(MouseListener l : this.listeners) {
				l.mouseClicked(me);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
		if(!this.parent.inConnectionMode()) {
			for(MouseListener l : this.listeners) {
				l.mouseEntered(e);
			}
		}
	}

	public void mouseExited(MouseEvent e) {
		if(!this.parent.inConnectionMode()) {
			for(MouseListener l : this.listeners) {
				l.mouseExited(e);
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		if(!this.parent.inConnectionMode()) {
			for(MouseListener l : this.listeners) {
				l.mousePressed(e);
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if(!this.parent.inConnectionMode()) {
			for(MouseListener l : this.listeners) {
				l.mouseReleased(e);
			}
		}
	}
}