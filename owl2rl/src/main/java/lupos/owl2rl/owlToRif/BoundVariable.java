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
package lupos.owl2rl.owlToRif;

/**
 * Contains the information that is bound to a variable (e.g. ?x) after a Query
 */
public class BoundVariable {
	private String variable; // name of variable e.g. ?p
	private String methodName=""; //the Name of the emitter Method that handles the type of Rule this BoundVariable belongs to
	private String name; // name of Template e.g. #prp-fp (for functional Property)
	private String partOfList=""; //For List Rules this field is the name of the list (e.g. ?partOfList rdf:first <x>. ?partOfList rdf:rest _:b0. ...)
	private String className; //class name of Emitter Class
	private String originalString;
	/**Constructor for Bound Variable
	 *
	 * @param variable (is empty string for RuleResults (as in rules concerning lists))
	 * @param prefix (is empty string for GraphResults (as in simple property rules))
	 * @param value
	 * @param partOfList
	 * @param currentTemplateRule
	 * @param originalString
	 */
	BoundVariable(String variable,String partOfList,TemplateRule currentTemplateRule, String originalString) {
		this.setVariable(variable);//is empty String for ListRules
		this.setName(currentTemplateRule.getName());
		this.setPartOfList(partOfList);//is empty String for Property Rules
		this.methodName=currentTemplateRule.getMethodName();
		this.className=currentTemplateRule.getClassName();
		this.setOriginalString(originalString);
	}

	/**
	 *
	 * @return class name of emitter class
	 */
	public String getClassName() {
		return this.className;

	}

	/**
	 *
	 * @return name of emitter method
	 */
	public String getMethodName() {
		return this.methodName;
	}

	public String getVariable() {
		return this.variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPartOfList() {
		return this.partOfList;
	}

	public void setPartOfList(String partOfList) {
		this.partOfList = partOfList;
	}

	public String getOriginalString() {
		return this.originalString;
	}

	public void setOriginalString(String originalString) {
		this.originalString = originalString;
	}
}
