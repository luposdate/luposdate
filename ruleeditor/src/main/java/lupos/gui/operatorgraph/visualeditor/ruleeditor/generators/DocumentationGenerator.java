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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.generators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JTabbedPane;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.DocumentationPanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RulePackagePanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RulePanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.File;

public class DocumentationGenerator {
	private RuleEditor editor = null;

	private String targetDirectory = "";

	private StringBuffer ruleFrameNavigation = null;
	private StringBuffer allRules_content = null;

	private HashMap<String, String> templates = new HashMap<String, String>();
	private HashMap<String, JTabbedPane[]> rulePackageAssociations = null;

	public DocumentationGenerator(RuleEditor editor) {
		this.editor = editor;
	}

	public void generate(String targetDirectory, String docTitle, String imagePath) {
		this.ruleFrameNavigation = new StringBuffer("<table style=\"width:100&#37;; border:0;\"><tr><td><a href=\"packagesDescriptionFrame.html\">All Packages</a>");
		this.allRules_content = new StringBuffer();
		this.rulePackageAssociations = new HashMap<String, JTabbedPane[]>();
		this.targetDirectory = targetDirectory;

		if(!this.targetDirectory.endsWith("/")) {
			this.targetDirectory += "/";
		}

		System.out.println("Starting to generate documentation...");

		String logo_name = "";

		// --- copy images - begin ---
		HashMap<String, String> images_map = new HashMap<String, String>();
		images_map.put("ruleTransitionArrow", System.getProperty("user.dir") + "/src/lupos/gui/operatorgraph/visualeditor/ruleeditor/images/ruleTransitionArrow.png");
		images_map.put("logo", imagePath);

		for(String img_name_bit : images_map.keySet()) {
			String img_path = images_map.get(img_name_bit);

			if(img_path == null) {
				continue;
			}

			System.out.println(":: copying " + img_path + "...");

			File img = new File(img_path);
			img.copyFileTo(this.targetDirectory + img_name_bit + "." + img.getFileExtension());

			if(img_name_bit.equals("logo")) {
				logo_name = img_name_bit + "." + img.getFileExtension();
			}
		}
		// --- copy images - end ---

		// --- generate rule frame header - begin ---
		String ruleFrameHeader = "";

		if(imagePath != null) {
			ruleFrameHeader = "<table style=\"width:100%;\"><tr><td style=\"text-align:right;\"><img src=\"" + logo_name + "\"></td>" +
			"<td style=\"text-align:left;\"><h1>" + docTitle + "</h1></td></tr></table>";
		}
		else {
			ruleFrameHeader = "<h1 align=\"center\">" + docTitle + "</h1>";
		}
		// --- generate rule frame header - end ---

		// --- index file - begin ---
		System.out.println(":: generating index.html...");

		File.writeFile(this.targetDirectory + "index.html", String.format(this.getTemplate("index"), docTitle));
		// --- index file - end ---

		// --- package overview frame - begin ---
		System.out.println(":: generating packagesOverviewFrame.html...");

		StringBuffer rulePackageLinks = new StringBuffer();

		for(JTabbedPane rulePackagePane : this.sortElementsRulePackages(this.editor.getRulePackages())) {
			RulePackagePanel rulePackage = (RulePackagePanel) rulePackagePane;
			String rulePackageName = rulePackage.toString();
			String rulePackageLink = rulePackageName.replaceAll(" ", "").toLowerCase();
			DocumentationPanel rPDocumentationPanel = rulePackage.getDocumentationPanel();

			rulePackageLinks.append("<a href=\"" + rulePackageLink + "OverviewFrame.html\" target=\"rulesOverviewFrame\">" + rulePackageName + "</a><br>\n");

			// --- rules overview frames - begin ---
			JTabbedPane[] associatedRulePackages = this.sortElementsRules(rulePackage.getAssociatedRules());
			this.rulePackageAssociations.put(rulePackageName, associatedRulePackages);
			this.generateRulesOverviewFrame(rulePackageName, associatedRulePackages);
			// --- rules overview frames - end ---

			this.ruleFrameNavigation.append(" - <a href=\"" + rulePackageLink + "PackageDescription.html\">" + rulePackageName + "</a>");

			this.allRules_content.append("<h2>Rule Package <a href=\"" + rulePackageLink + "PackageDescription.html\">" + rulePackageName + "</a></h2>\n");
			this.allRules_content.append(rPDocumentationPanel.getShortDescription());
		}

		File.writeFile(this.targetDirectory + "packagesOverviewFrame.html", String.format(this.getTemplate("packagesOverviewFrame"), rulePackageLinks.toString()));

		this.ruleFrameNavigation.append("</td><td align=\"right\"><a href=\"index.html?%s\" target=\"_top\">FRAMES</a> - <a href=\"%s\" target=\"_top\">NO FRAMES</a></td></tr></table>");
		// --- package overview frame - end ---

		// --- all rules overview frame - begin ---
		this.generateRulesOverviewFrame("All Rules", this.sortElementsRules(this.editor.getRules()));
		// --- all rules overview frame - end ---

		// --- packages description frame - begin ---
		System.out.println(":: generating packagesDescriptionFrame.html...");

		String title = "All Rule Packages";

		String formattedNavi = String.format(this.ruleFrameNavigation.toString(), "packagesDescriptionFrame.html", "packagesDescriptionFrame.html");

		File.writeFile(this.targetDirectory + "packagesDescriptionFrame.html", String.format(this.getTemplate("ruleFrame"), title, title, ruleFrameHeader, formattedNavi, title, this.allRules_content.toString(), formattedNavi));
		// --- packages description frame - end ---

		// --- rule package description frames - begin ---
		for(JTabbedPane rulePackagePane : this.sortElementsRulePackages(this.editor.getRulePackages())) {
			RulePackagePanel rulePackage = (RulePackagePanel) rulePackagePane;
			String rulePackageName = rulePackage.toString();
			String rulePackageLink = rulePackageName.replaceAll(" ", "").toLowerCase() + "PackageDescription.html";
			title = "Rule Package " + rulePackageName;

			System.out.println(":: generating " + rulePackageLink + "...");

			formattedNavi = String.format(this.ruleFrameNavigation.toString(), rulePackageLink , rulePackageLink);

			String content = rulePackage.getDocumentationPanel().getContent(this.targetDirectory, this.rulePackageAssociations.get(rulePackageName));

			File.writeFile(this.targetDirectory + rulePackageLink, String.format(this.getTemplate("ruleFrame"), title, title, ruleFrameHeader, formattedNavi, title, content, formattedNavi));
		}
		// --- rule package description frames - end ---

		// --- rule description frames - begin ---
		for(JTabbedPane rulePane : this.sortElementsRules(this.editor.getRules())) {
			RulePanel rule = (RulePanel) rulePane;
			String ruleName = rule.toString();
			String ruleLink = ruleName.replaceAll(" ", "").toLowerCase() + "Rule.html";
			title = "Rule " + ruleName;

			System.out.println(":: generating " + ruleLink + "...");

			formattedNavi = String.format(this.ruleFrameNavigation.toString(), ruleLink , ruleLink);

			String content = rule.getDocumentationPanel().getContent(this.targetDirectory, null);

			File.writeFile(this.targetDirectory + ruleLink, String.format(this.getTemplate("ruleFrame"), title, title, ruleFrameHeader, formattedNavi, title, content, formattedNavi));
		}
		// --- rule description frames - end ---

		System.out.println("DONE");
	}

	private void generateRulesOverviewFrame(String title, JTabbedPane[] rules) {
		StringBuffer ruleLinks = new StringBuffer();
		String filename = title.replaceAll(" ", "").toLowerCase() + "OverviewFrame.html";

		System.out.println(":: generating " + filename + "...");

		for(JTabbedPane rulePane : rules) {
			String ruleName = rulePane.toString();
			String ruleLink = ruleName.replaceAll(" ", "").toLowerCase();

			ruleLinks.append("<a href=\"" + ruleLink + "Rule.html\" target=\"rulesFrame\">" + ruleName + "</a><br>\n");
		}

		if(ruleLinks.length() == 0) {
			ruleLinks.append("<i>No rules found</i>");
		}

		File.writeFile(this.targetDirectory + filename, String.format(this.getTemplate("rulesOverviewFrame"), title, title, ruleLinks.toString()));
	}

	
	private JTabbedPane[] sortElementsRules(Collection<RulePanel> elements) {
		return sortElements(elements.toArray(new JTabbedPane[]{}));
	}

	private JTabbedPane[] sortElementsRulePackages(Collection<RulePackagePanel> elements) {
		return sortElements(elements.toArray(new JTabbedPane[]{}));
	}
	
	private JTabbedPane[] sortElements(JTabbedPane[] tmpArray) {

		Arrays.sort(tmpArray, new Comparator<JTabbedPane>() {
			public int compare(JTabbedPane o1, JTabbedPane o2) {
				String o1_str = o1.toString();
				String o2_str = o2.toString();

				return o1_str.compareTo(o2_str);
			}
		});

		return tmpArray;
	}

	private String getTemplate(String templateName) {
		if(!this.templates.containsKey(templateName)) {
			this.templates.put(templateName, File.readFile("src/lupos/gui/operatorgraph/visualeditor/ruleeditor/templates/doc_" + templateName + ".tpl"));
		}

		return this.templates.get(templateName);
	}
}