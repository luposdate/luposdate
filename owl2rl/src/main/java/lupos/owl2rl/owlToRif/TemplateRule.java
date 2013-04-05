/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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

import lupos.owl2rl.parser.PatternQueryType;

public class TemplateRule {
	
	private String pattern="";
	private String template_Name="";
	private String patternQuery="";
	private String template ="";
	public PatternQueryType queryType=PatternQueryType.FROMXMLDATA; //specifies whether the patternQuery is read from the XML file(default) or generated from a type
	private String[] vars;
	String prefixes;
	private String methodName="";
	private String className="";
	
	
	public TemplateRule setPattern(String pattern){
		this.pattern=pattern;
		return this;
	}
	
	public void setMethodName(String methodName){
		this.methodName=methodName;
	}
	
	public TemplateRule setName(String name){
		this.template_Name=name;
		return this;
	}

	
	
	private void makePatternQueryFromType() {
		String ret="";
		switch(this.queryType){
		case FROMXMLDATA: ret=this.patternQuery;
		ret=lupos.owl2rl.tools.Tools.document(this.prefixes+
				lupos.owl2rl.tools.Tools.group(lupos.owl2rl.tools.Tools.forall(getVariablesArray(),ret)));
		break;
		case RDFTYPE: ret="?p[rdf:type->"+this.patternQuery+"]:-?p[rdf:type->"+this.patternQuery+"] ";
		ret=lupos.owl2rl.tools.Tools.document(this.prefixes+
				lupos.owl2rl.tools.Tools.group(lupos.owl2rl.tools.Tools.forall(getVariablesArray(),ret)));
		break;
		case P1HASPROPERTYP2: ret="?p1["+this.patternQuery+"->?p2]:-?p1["+this.patternQuery+" ->?p2]" ;
		ret=lupos.owl2rl.tools.Tools.document(this.prefixes+
				lupos.owl2rl.tools.Tools.group(lupos.owl2rl.tools.Tools.forall(getVariablesArray(),ret)));
		break;
		case WITHFORALL:
			ret=this.patternQuery;
			ret=lupos.owl2rl.tools.Tools.document(this.prefixes+
					lupos.owl2rl.tools.Tools.group(ret));
	
		break;
		
		}
		this.patternQuery=ret;
	}

	public TemplateRule setTemplate(String template){
		this.template = (template);
		return this;
	}
		
	
	public String getPattern() {
		return this.pattern;
	}

	public String getName() {
		return this.template_Name;
	}

	public String getTemplate() {
		return this.template;
	}
	
	public String getPatternQuery() {
		return this.patternQuery;
	}

	
	public void setPatternQuery(String pattern_query,String prefixes) {
		this.patternQuery =  pattern_query;
		this.prefixes=prefixes;
	}
	
	
	
	public String[] getVariablesArray(){
		return this.vars;	
	}

	public void setVariableArray(String[] vars){
		this.vars=vars;
	}
	
	public void create() {
		makePatternQueryFromType();		
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setClassName(String string) {
		this.className=string;
		
	}
	
	public String getClassName(){
		return this.className;
	}
	
}
