/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.event.actions;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.*;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.Action;
import lupos.event.actions.send.Send;
import lupos.event.actions.send.SlidingWindow;
import lupos.event.htmlclient.Encode;

/**
 * Class for handling with query results and interpretating template file code
 * for generating HTML pages.
 */
public class PageAction extends Action {

	private String name;
	private String template;
	private Send send;

	/**
	 * Constructor of this action.
	 * 
	 * @param name
	 *            the name of the action
	 * @param template
	 *            the name of the template
	 * @param send
	 *            the send option
	 */
	public PageAction(String name, String template, Send send) {
		super("PageAction");
		this.name = name;
		this.template = template;
		this.send = send;
	}

	/**
	 * Receives query results and interpretates the incoming triples. Also
	 * replaces the syntax of template HTML's to HTML files with data.
	 * 
	 * @param queryResult
	 *            the incoming data
	 */
	@Override
	public void execute(QueryResult queryResult) {

		String result = new String(this.template);
		String nextForTemplate;
		String tmp;
		StringBuilder lines;
		Set<Variable> vars = queryResult.getVariableSet();
		// Set<String> variableNames = getVariables(template);
		Collection<Bindings> bindings = queryResult.getCollection();

		result = result.replaceAll("\\r\\n|\\r|\\n", " ");

		// FOR
		while (true) {
			nextForTemplate = getNextForTemplate(result);

			if (nextForTemplate != null) {
				lines = new StringBuilder();

				for (Bindings b : bindings) {
					tmp = nextForTemplate;
					for (Variable v : vars) {

						tmp = replaceEscape(b, v, tmp);
						tmp = replaceContent(b, v, tmp);
						tmp = replacePredicate(b, v, tmp);
					}

					lines.append(tmp);
				}
				result = result.replaceFirst(Encode.FORSTART + "(.*?)"
						+ Encode.FOREND, lines.toString());
			} else {
				break;
			}

		}

		// set refresh time if exists
		result = setRefresh(result);

		// set everything else
		for (Bindings b : bindings) {
			for (Variable v : vars) {
				result = replaceEscape(b, v, result);
				result = replaceContent(b, v, result);
				result = replacePredicate(b, v, result);
			}
		}

		// delete Ecode for the final HTML Code
		result = result.replaceAll(Encode.START + "(.*?)" + Encode.END, "");

		this.send.sendContent(result);
	}

	/**
	 * Interprets the refresh syntax and replaces it with content. This makes
	 * a page self refreshing.
	 * 
	 * @param htmlCode
	 *            the given htmlCode of template
	 * @return result as content for HTML page
	 */
	private String setRefresh(String htmlCode) {
		String result = htmlCode;
		String time = null;
		Pattern pattern = Pattern.compile(Encode.START + "REFRESH\\(" + "(.*?)"
				+ "\\)" + Encode.END);
		Matcher matcher = pattern.matcher(result);

		while (matcher.find()) {
			time = matcher.group(1);
			return result.replaceFirst(Encode.START + "REFRESH\\(" + "(.*?)"
					+ "\\)" + Encode.END,
					"<meta http-equiv=\"refresh\" content=" + time + "; url="
							+ this.name + ".html\"/>");
		}

		return result;

	}

	/**
	 * Checking incoming bindings and creates out of syntax a readable content.
	 * 
	 * @param b
	 *            the bindings with information
	 * @param v
	 *            the variables of bindings
	 * @param template_param
	 *            with row data
	 * 
	 * @return the new HTML content
	 */
	public String replaceContent(Bindings b, Variable v, String template_param) {
		String result = template_param;
		String newInfo = null;

		if (b.get(v).isTypedLiteral()) {
			newInfo = ((TypedLiteral) b.get(v)).getContent();
			newInfo = newInfo.substring(1, newInfo.length() - 1);
		} else if (b.get(v).isURI()) {
			newInfo = ((URILiteral) b.get(v)).getString();
		} else if (b.get(v).isSimpleLiteral()) {
			newInfo = b.get(v).originalString();
			newInfo = newInfo.substring(1, newInfo.length() - 1);
		} else if (b.get(v).isLanguageTaggedLiteral()) {
			newInfo = ((LanguageTaggedLiteral) b.get(v)).getContent();
			newInfo = newInfo.substring(1, newInfo.length() - 1);
		}

		if (b.get(v).isBlank()) {
			// broker information ...
			newInfo = "";
		}

		result = result.replaceAll(Encode.REG_CONTENT_OF(v.getName()), newInfo);

		result = result.replaceAll(Encode.REG_VAR(v.getName()), b.get(v)
				.originalString());

		return result;
	}

	/**
	 * Checking incoming bindings and creates out of the template HTML content a
	 * HTML content with readable data. At this case the escape syntax is
	 * replaced.
	 * 
	 * @param b
	 *            the bindings with information
	 * @param v
	 *            the variables of bindings
	 * @param template_param
	 *            with row data
	 * 
	 * @return the new HTML content
	 */
	public String replaceEscape(Bindings b, Variable v, String template_param) {
		String result = template_param;
		String var;

		if (b.get(v).isTypedLiteral()) {
			var = ((TypedLiteral) b.get(v)).originalString();
			var = StringEscapeUtils.escapeHtml(var);
			result = result.replaceAll(Encode.REG_ESCAPE_OF(v.getName()), var);

		} else if (b.get(v).isURI()) {
			var = ((URILiteral) b.get(v)).originalString();
			var = StringEscapeUtils.escapeHtml(var);
			result = result.replaceAll(Encode.REG_ESCAPE_OF(v.getName()), var);
		}

		var = b.get(v).originalString();
		var = StringEscapeUtils.escapeHtml(var);
		result = result.replaceAll(Encode.REG_ESCAPE_OF(v.getName()), var);

		return result;
	}

	/**
	 * For getting the next content of for syntax.
	 * 
	 * @param forCode
	 *            the syntax code
	 * @return the content
	 */
	public String getNextForTemplate(String forCode) {
		Pattern pattern = Pattern.compile(Encode.FORSTART + " (.*?)"
				+ Encode.FOREND);
		Matcher matcher = pattern.matcher(forCode);

		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	/**
	 * Replaces the ECode with the last word of the predicate of v.
	 * 
	 * @param b
	 *            the bindings with information
	 * @param v
	 *            the variables of bindings
	 * @param template_param
	 *            with row data
	 * 
	 * @return the new HTML content
	 */
	public String replacePredicate(Bindings b, Variable v, String template_param) {
		String result = template_param;
		String newInfo = "";

		if (b.get(v).isURI()) {
			newInfo = ((URILiteral) b.get(v)).getString();
			String[] splitNewInfo = newInfo.split("/");
			newInfo = splitNewInfo[splitNewInfo.length-1];
		}
		
		result = result.replaceAll(Encode.REG_PREDICATE_OF(v.getName()), newInfo);
		
		return result;
	}
}