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
package lupos.gui.operatorgraph.visualeditor.visualrif.util;

import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel;

public class GroupContainer implements ITree {
	
	
	private VisualRifEditor visualRifEditor;
	private GroupPanel activeGroup;
	private HashMap<String,GroupPanel> groups = new HashMap<String,GroupPanel>();
	private LinkedList<GroupIdentifier> groupPanelList;

	
	
	
	
	//Constructor
	public GroupContainer(VisualRifEditor visualRifEditor){
		this.visualRifEditor = visualRifEditor;
		this.setGroupPanelList(new LinkedList<GroupIdentifier>());
	}
	
	
	
	public void showGroup(String ruleName) {
		this.activeGroup = this.groups.get(ruleName);

		this.visualRifEditor.setRightComponent(this.activeGroup);
	}
	
	
	public GroupPanel getGroupByName(String groupName){
		return this.groups.get(groupName);
	}
	
	
	public GroupPanel createNewGroup(){

		String name = this.checkName("Group", "Group", 0);
		
		
		this.activeGroup = new GroupPanel(this.visualRifEditor, name);
		
		this.groups.put(name, this.activeGroup);
		visualRifEditor.getTreePane().addNewGroup(this.activeGroup,visualRifEditor.getDocumentContainer().getNameOfActiveElement());
		printGroups();

		TreePath path = this.activeGroup.getGroupPath();
//		visualRifEditor.getDocumentContainer().getActiveDocument().getListOfGroups().add(name);
		visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleNameInVisualGraphsComponentArray(name, name);
		
		return this.activeGroup;
	}
	
	
	public void deleteGroup(String groupName){
		//delete visual Component on Canvas
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().deleteGroup(groupName);
		
		this.groups.remove(groupName);
		
		// delete GroupPanel
		for (int i = 0; i < this.groupPanelList.size(); i++) {
			if (this.groupPanelList.get(i).getGroupName().equals(groupName)){
				this.groupPanelList.remove(i);
				}
		}
		
		// set GroupCnt
//		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().setGroupsCnt(this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getGroupsCnt()-1);
		
	}
	

	/**
	 * Checks whether the name of 
	 * the new rule is already used.
	 * @param basename
	 * @param newname
	 * @param index
	 * @return a new auto-generated name for the new rule
	 */
	public String checkName(String basename, String newname, int index) {
		boolean exists = false;

		for(String documentName : this.groups.keySet()) {
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
	 * @param ruleName
	 */
	public void showDocument(String ruleName){
		
		this.activeGroup = this.groups.get(ruleName);
//		this.activeDocument.updateDocument();
		
		this.visualRifEditor.setRightComponent(this.activeGroup);
		
	}
	


	@SuppressWarnings("unchecked")
	public void removeElement(String elem, TreeNode parentNode) {
		GroupPanel ret = this.groups.remove(elem);

			this.activeGroup = null;


		this.visualRifEditor.setRightComponent(new JPanel());
	}


	public boolean nameChanged(TypeEnum e, String oldName, String newName) {
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




	@Override
	public String getNameOfActiveElement() {
		// TODO Auto-generated method stub
		return this.activeGroup.toString();
	}

	private void updateNameInGroupPanelList(String oldName, String newName){
		
		for(int i = 0 ; i < this.groupPanelList.size() ; i++){
			if(this.groupPanelList.get(i).getGroupName().equals(oldName)){
				this.groupPanelList.get(i).setGroupName(newName);
			}
		}
		
	}

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
		for(String name : this.groups.keySet()) {
			System.out.println(name);
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}



	
	
	/* *************** **
	 * Getter + Setter **
	 * *************** */
	
	

	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}



	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}



	public GroupPanel getActiveGroup() {
		return activeGroup;
	}



	public void setActiveGroup(GroupPanel activeGroup) {
		this.activeGroup = activeGroup;
	}



	public HashMap<String, GroupPanel> getGroups() {
		return groups;
	}



	public void setGroups(HashMap<String, GroupPanel> groups) {
		this.groups = groups;
	}



	public LinkedList<GroupIdentifier> getGroupPanelList() {
		return groupPanelList;
	}



	public void setGroupPanelList(LinkedList<GroupIdentifier> groupPanelList) {
		this.groupPanelList = groupPanelList;
	}









}
