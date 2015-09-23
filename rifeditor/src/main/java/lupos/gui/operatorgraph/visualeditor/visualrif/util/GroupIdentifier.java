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
package lupos.gui.operatorgraph.visualeditor.visualrif.util;

import java.awt.Component;

import javax.swing.tree.TreePath;

import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel;
public class GroupIdentifier {


	
	private String groupName;

	private Component component;
	
	private TreePath path;
	
	private GroupPanel groupPanel;
	
	
	/**
	 * <p>Constructor for GroupIdentifier.</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 * @param groupPanel a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 * @param component a {@link java.awt.Component} object.
	 * @param path a {@link javax.swing.tree.TreePath} object.
	 */
	public GroupIdentifier(String groupName, GroupPanel groupPanel,  Component component, TreePath path){
		
		this.setComponent(component);
		this.setGroupName(groupName);
		this.setGroupPanel(groupPanel);
		this.setPath(path);


	}




	/**
	 * <p>Getter for the field <code>component</code>.</p>
	 *
	 * @return a {@link java.awt.Component} object.
	 */
	public Component getComponent() {
		return component;
	}


	/**
	 * <p>Setter for the field <code>component</code>.</p>
	 *
	 * @param component a {@link java.awt.Component} object.
	 */
	public void setComponent(Component component) {
		this.component = component;
	}


	/**
	 * <p>Getter for the field <code>path</code>.</p>
	 *
	 * @return a {@link javax.swing.tree.TreePath} object.
	 */
	public TreePath getPath() {
		return path;
	}


	/**
	 * <p>Setter for the field <code>path</code>.</p>
	 *
	 * @param path a {@link javax.swing.tree.TreePath} object.
	 */
	public void setPath(TreePath path) {
		this.path = path;
	}




	/**
	 * <p>Getter for the field <code>groupName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGroupName() {
		return groupName;
	}




	/**
	 * <p>Setter for the field <code>groupName</code>.</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}




	/**
	 * <p>Getter for the field <code>groupPanel</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 */
	public GroupPanel getGroupPanel() {
		return groupPanel;
	}




	/**
	 * <p>Setter for the field <code>groupPanel</code>.</p>
	 *
	 * @param groupPanel a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 */
	public void setGroupPanel(GroupPanel groupPanel) {
		this.groupPanel = groupPanel;
	}






	
}

