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

import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel;
public class GroupContainer implements ITree {

	private VisualRifEditor visualRifEditor;
	private GroupPanel activeGroup;
	private HashMap<String,GroupPanel> groups = new HashMap<String,GroupPanel>();
	private LinkedList<GroupIdentifier> groupPanelList;

	//Constructor
	/**
	 * <p>Constructor for GroupContainer.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public GroupContainer(final VisualRifEditor visualRifEditor){
		this.visualRifEditor = visualRifEditor;
		this.setGroupPanelList(new LinkedList<GroupIdentifier>());
	}

	/**
	 * <p>showGroup.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void showGroup(final String ruleName) {
		this.activeGroup = this.groups.get(ruleName);
		this.visualRifEditor.setRightComponent(this.activeGroup);
	}

	/**
	 * <p>getGroupByName.</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 */
	public GroupPanel getGroupByName(final String groupName){
		return this.groups.get(groupName);
	}

	/**
	 * <p>createNewGroup.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 */
	public GroupPanel createNewGroup(){
		final String name = this.checkName("Group", "Group", 0);
		this.activeGroup = new GroupPanel(this.visualRifEditor, name);
		this.groups.put(name, this.activeGroup);
		this.visualRifEditor.getTreePane().addNewGroup(this.activeGroup,this.visualRifEditor.getDocumentContainer().getNameOfActiveElement());
		this.printGroups();
		final TreePath path = this.activeGroup.getGroupPath();
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleNameInVisualGraphsComponentArray(name, name);
		return this.activeGroup;
	}

	/**
	 * <p>deleteGroup.</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 */
	public void deleteGroup(final String groupName){
		//delete visual Component on Canvas
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().deleteGroup(groupName);
		this.groups.remove(groupName);
		// delete GroupPanel
		for (int i = 0; i < this.groupPanelList.size(); i++) {
			if (this.groupPanelList.get(i).getGroupName().equals(groupName)){
				this.groupPanelList.remove(i);
			}
		}
	}

	/**
	 * Checks whether the name of
	 * the new rule is already used.
	 *
	 * @param basename a {@link java.lang.String} object.
	 * @param newname a {@link java.lang.String} object.
	 * @param index a int.
	 * @return a new auto-generated name for the new rule
	 */
	public String checkName(final String basename, String newname, int index) {
		boolean exists = false;
		for(final String documentName : this.groups.keySet()) {
			if(newname.equalsIgnoreCase(documentName)) {
				newname = basename + index;
				index += 1;
				exists = true;

				break;
			}
		}
		if(exists) {
			newname = this.checkName(basename, newname, index);
		}
		return newname;
	}

	/**
	 * Loads the RulePanel and shows it on the right side of the GUI
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void showDocument(final String ruleName){
		this.activeGroup = this.groups.get(ruleName);
		this.visualRifEditor.setRightComponent(this.activeGroup);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public void removeElement(final String elem, final TreeNode parentNode) {
		final GroupPanel ret = this.groups.remove(elem);
		this.activeGroup = null;
		this.visualRifEditor.setRightComponent(new JPanel());
	}

	/** {@inheritDoc} */
	@Override
	public boolean nameChanged(final TypeEnum e, final String oldName, final String newName) {
		String tmpName = this.checkName(newName, newName, 0);
		if(!tmpName.equalsIgnoreCase(newName)) {
			this.activeGroup.setGroupName(oldName);
			return false;
		}
		tmpName = this.visualRifEditor.getDocumentContainer().checkName(newName, newName, 0);
		if(!tmpName.equalsIgnoreCase(newName)) {
			return false;
		}
		this.activeGroup = this.groups.get(oldName);
		this.activeGroup.setGroupName(newName);
		this.groups.remove(oldName);
		this.groups.put(newName, this.activeGroup);
		this.updateNameInGroupPanelList(oldName, newName);
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateGroupNameInVisualGraphsComponentArray(oldName, newName);
		this.visualRifEditor.getTreePane().updateTopComponent(oldName, newName);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String getNameOfActiveElement() {
		return this.activeGroup.toString();
	}

	private void updateNameInGroupPanelList(final String oldName, final String newName){
		for(int i = 0 ; i < this.groupPanelList.size() ; i++){
			if(this.groupPanelList.get(i).getGroupName().equals(oldName)){
				this.groupPanelList.get(i).setGroupName(newName);
			}
		}
	}

	/**
	 * <p>cancelModi.</p>
	 */
	public void cancelModi() {
		if(this.activeGroup != null) {
			this.activeGroup.cancelModi();
		}
	}

	/**
	 * Test
	 */
	private void printGroups(){
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		for(final String name : this.groups.keySet()) {
			System.out.println(name);
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}

	/* *************** **
	 * Getter + Setter **
	 * *************** */
	/**
	 * <p>Getter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	/**
	 * <p>Setter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	/**
	 * <p>Getter for the field <code>activeGroup</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 */
	public GroupPanel getActiveGroup() {
		return this.activeGroup;
	}

	/**
	 * <p>Setter for the field <code>activeGroup</code>.</p>
	 *
	 * @param activeGroup a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 */
	public void setActiveGroup(final GroupPanel activeGroup) {
		this.activeGroup = activeGroup;
	}

	/**
	 * <p>Getter for the field <code>groups</code>.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<String, GroupPanel> getGroups() {
		return this.groups;
	}

	/**
	 * <p>Setter for the field <code>groups</code>.</p>
	 *
	 * @param groups a {@link java.util.HashMap} object.
	 */
	public void setGroups(final HashMap<String, GroupPanel> groups) {
		this.groups = groups;
	}

	/**
	 * <p>Getter for the field <code>groupPanelList</code>.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<GroupIdentifier> getGroupPanelList() {
		return this.groupPanelList;
	}

	/**
	 * <p>Setter for the field <code>groupPanelList</code>.</p>
	 *
	 * @param groupPanelList a {@link java.util.LinkedList} object.
	 */
	public void setGroupPanelList(final LinkedList<GroupIdentifier> groupPanelList) {
		this.groupPanelList = groupPanelList;
	}
}
