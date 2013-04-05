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
package lupos.owl2rl.emitter;

import java.util.HashMap;
import java.util.LinkedList;

import lupos.owl2rl.owlToRif.InferenceRulesGenerator;
import lupos.owl2rl.owlToRif.BoundVariable;
import lupos.owl2rl.owlToRif.TemplateRule;

public class RuleEmitter {

	public static void emitPropertyRules(LinkedList<BoundVariable> boundV, StringBuilder outputString, HashMap<String, TemplateRule> templateRulemap){
		boolean block =false;
		String ret="";

		//for each variable: replace variables with value from query result
		for(int j=0;j<boundV.size();j++){
			BoundVariable v=boundV.get(j);

			if(j==0){//first time
				ret=("(* "+v.getName()+" *) "+templateRulemap.get(v.getName()).getTemplate()).replace(v.getVariable(), v.getOriginalString());
			} else {
				ret=ret.replace(v.getVariable(),v.getOriginalString());
			}
			//makes sure rules like (x, rdfs:subClassOf, x) are not emitted
			if(ret.contains(v.getOriginalString()+"[rdfs:subClassOf->"+v.getOriginalString()+"]"))
				block=true;
		}

		if(!ret.equals("") && !block && !outputString.toString().contains(ret)){
			outputString.append("\n"+ret);
			InferenceRulesGenerator.rulesEmitted++;
		} else {
			block=false;
		}
	}

	public static void emitSubClassOfRule(LinkedList<BoundVariable> variablesInList, StringBuilder outputString, HashMap<String, TemplateRule> templateRulemap) {

		String c1=variablesInList.getLast().getPartOfList();

		String rule="";
		for(int xi=0;xi<variablesInList.size();xi++){

			BoundVariable c3=variablesInList.get(xi);

			rule=("(* "+c3.getName()+" *) "+templateRulemap.get(c3.getName()).getTemplate()).replace("?c1", c1);
			rule=rule.replace("?c3",c3.getOriginalString());

			if(!outputString.toString().contains(rule)){
				outputString.append(rule+"\n");
				InferenceRulesGenerator.rulesEmitted++;
			}
		}
	}

	public static void emitSubPropertyOfRule(LinkedList<BoundVariable> variablesInList, StringBuilder outputString, HashMap<String, TemplateRule> templateRulemap) {

		String p1=variablesInList.getLast().getPartOfList();

		String rule="\n";
		for(int xi=0;xi<variablesInList.size();xi++){
			BoundVariable p3=variablesInList.get(xi);
			rule=("(* "+p3.getName()+" *) "+templateRulemap.get(p3.getName()).getTemplate()).replace("?p1", p1);
			rule=rule.replace("?p3",p3.getOriginalString());
			if(!outputString.toString().contains(rule)){
				outputString.append(rule+"\n");
				InferenceRulesGenerator.rulesEmitted++;
			}
		}
	}

	/**
	 * emit Rules for #eq-diff2/diff3 #prp-adp #cax-adc 
	 */
	public static String emitForEachRules(LinkedList<BoundVariable> variablesInList, StringBuilder output, HashMap<String, TemplateRule> templateRulemap) {
		String output2="";
		/*
		 * for all pairs of x,y element of list, x!=y do:
		 * append rule to outputString
		 */
		String rule;
		for(int xi=0;xi<variablesInList.size();xi++){
			for(int yi=0;yi<variablesInList.size();yi++){
				BoundVariable x=variablesInList.get(xi);
				BoundVariable y=variablesInList.get(yi);

				if(!x.getOriginalString().equals(y.getOriginalString())){

					rule=("(* "+x.getName()+" *) "+templateRulemap.get(x.getName()).getTemplate()).replace("?x", x.getOriginalString());
					rule=rule.replace("?y",y.getOriginalString());

					//avoid duplicate rules
					if(!output2.contains(rule)){
						/*
						 * avoid rules that are already implied through symmetry.
						 * e.g.: 
						 * (x sameAs y) and (y sameAs x) in diff2 and diff3.
						 * And(factA factB) and And(factB factA) in prp-adp and cax adc.
						 */

						rule=("(* "+x.getName()+" *) "+templateRulemap.get(x.getName()).getTemplate()).replace("?y", x.getOriginalString());
						rule=rule.replace("?x",y.getOriginalString());

						if(!output2.contains(rule)){
							output2+=("\n"+rule);
							output.append("\n"+rule);
							InferenceRulesGenerator.rulesEmitted++;
						}
					}
				}
			}
		}

		output.append("\n");
		return output2;
	}
	
	/**
	 * make template for #prp-key
	 * 
	 * @param variablesInList
	 * @param returnString
	 */
	@SuppressWarnings("unused")
	public static void emitHasKeyRule(LinkedList<BoundVariable> variablesInList, StringBuilder returnString, HashMap<String, TemplateRule> templateRulemap) {
		String rule=("\n(* #prp-key *)");

		String listName /*= type */ = variablesInList.get(0).getPartOfList();

		rule+=("Forall ?x ?y ?v ( \n"+
				"?x[owl:sameAs->?y]:-And(\n"+
				"?x[rdf:type->"+listName +"]\n"+
				"?y[rdf:type->"+listName +"]\n");

		for(BoundVariable b: variablesInList){
			rule+=("?x["+b.getOriginalString()+"->?v] ?y["+b.getOriginalString()+"->?v]\n");
		}
		
		if(!returnString.toString().contains(rule)){
			returnString.append(rule+"))");
			InferenceRulesGenerator.rulesEmitted++;
		}
	} 

	/**
	 * make template for #cls-int1
	 * 
	 * @param variablesInList
	 * @param returnString
	 */
	@SuppressWarnings("unused")
	public static void emitIntersectionOfRule1(LinkedList<BoundVariable> variablesInList, StringBuilder returnString, HashMap<String, TemplateRule> templateRulemap) {

		String rule=("\n(* #cls-int1 *)");

		String listName /*= type */ = variablesInList.get(0).getPartOfList();

		rule+=("Forall ?y ( \n"+	
				"?y[rdf:type->"+listName +"] :- And(\n");

		for(BoundVariable b: variablesInList){
			rule+=("?y[rdf:type->"+b.getOriginalString()+"]\n");
		}

		if(!returnString.toString().contains(rule)){
			returnString.append(rule+"))");
			InferenceRulesGenerator.rulesEmitted++;
		}
	}

	/**
	 * make template for #scm-int
	 * 
	 * @param variablesInList
	 * @param returnString
	 */
	@SuppressWarnings("unused")
	public static void emitIntersectionOfRule2(LinkedList<BoundVariable> variablesInList, StringBuilder returnString, HashMap<String, TemplateRule> templateRulemap) {

		returnString.append( "\n(* #scm-int *)");

		String listName /*= type */ = variablesInList.get(0).getPartOfList();
		String rule="";

		for(BoundVariable b: variablesInList){
			rule+=("Forall ?y ("+	
					"?y[rdf:type->"+b.getOriginalString()+"]:- And( \n"+
					"?y[rdf:type->"+listName +"]))\n"+

					""+listName+""+"[rdfs:subClassOf->"+b.getOriginalString()+"]\n");

			if(!returnString.toString().contains(rule)){
				returnString.append(rule);
				InferenceRulesGenerator.rulesEmitted++;
			}
		}
	}

	/**
	 * make template for #cls-oo
	 * 
	 * @param variablesInList
	 * @param returnString
	 */
	@SuppressWarnings("unused")
	public static void emitOneOfRule(LinkedList<BoundVariable> variablesInList, StringBuilder returnString, HashMap<String, TemplateRule> templateRulemap) {

		returnString.append("\n(* #cls-oo *)");

		String listName /*= type */ = variablesInList.get(0).getPartOfList();

		String rule="";
		for(BoundVariable b: variablesInList){
			rule+=(b.getOriginalString()+"[rdf:type->"+listName +"]\n");

			if(!returnString.toString().contains(rule)){
				returnString.append(rule);
				InferenceRulesGenerator.rulesEmitted++;
			}
		}
	}

	/**
	 * make template for #prp-spo2
	 * 
	 * @param variablesInList
	 * @param returnString
	 */
	@SuppressWarnings("unused")
	public static void emitPropertyChainRule(LinkedList<BoundVariable> variablesInList, StringBuilder rule, HashMap<String, TemplateRule> templateRulemap) {
		rule.append("\n(*#prp-spo2*) ");
		/*
		 * this rule needs list members in proper order. 
		 */
		int index=0;
		int size=variablesInList.size();
		BoundVariable[] properties = new BoundVariable[size/2];
		/*Construct Array with correct sorting through indices*/
		for(int i=1; i<=size;i+=2){
			index=(size)/2-Integer.parseInt(""+ variablesInList.get(i).getOriginalString().charAt(1))-1;		
			properties[index]=variablesInList.get(i-1);//Every second Listmember is the index
		}

		/* Construct Rule #prp-spo2 */
		rule.append("Forall ");

		for(int k=0; k<=properties.length;k++){
			rule.append(" ?u"+k);
		}
		
		rule.append(" ( \n");	
		rule.append(/*erstes Element*/"?u0[" +/*Der Name der Liste*/properties[0].getPartOfList()+ /*letztes Element*/"->?u"+(properties.length) +"] :- And ( \n");
		
		/*The Property Chain*/
		for(int j=0; j<properties.length;j++){		
			rule.append("?u"+j+"["+properties[j].getOriginalString()+"->"+"?u"+(j+1)+"]\n");
		}
		rule.append("))");
		InferenceRulesGenerator.rulesEmitted++;
	}
	
	/**
	 * make template for #cls-uni
	 * 
	 * @param variablesInList
	 * @param returnString
	 */
	@SuppressWarnings("unused")
	public static void emitUnionOfRule1(LinkedList<BoundVariable> variablesInList, StringBuilder returnString, HashMap<String, TemplateRule> templateRulemap) {
		returnString.append("\n(* #cls-uni *)");
		String listName /*= type */ = variablesInList.get(0).getPartOfList();
		for(BoundVariable b: variablesInList){
			returnString.append("Forall ?y ( \n"+	
					"?y[rdf:type->"+listName +"] :- And(\n");		
			returnString.append("?y[rdf:type->"+b.getOriginalString()+"]\n))\n");
		}
		InferenceRulesGenerator.rulesEmitted++;
	}
	/**
	 * make template for #scm-uni
	 * 
	 * @param variablesInList
	 * @param returnString
	 */

	@SuppressWarnings("unused")
	public static void emitUnionOfRule2(LinkedList<BoundVariable> variablesInList, StringBuilder returnString, HashMap<String, TemplateRule> templateRulemap) {
		returnString.append("\n(* #scm-uni *)");
		String listName /*= type */ = variablesInList.get(0).getPartOfList();
		for(BoundVariable b: variablesInList){
			returnString.append(b.getOriginalString()+"[rdf:subClassOf->"+listName +"]\n");
		}
		InferenceRulesGenerator.rulesEmitted++;
	}

}
