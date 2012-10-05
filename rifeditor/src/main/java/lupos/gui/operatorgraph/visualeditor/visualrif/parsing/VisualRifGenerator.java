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
package lupos.gui.operatorgraph.visualeditor.visualrif.parsing;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTabbedPane;

import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.Console;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.DocumentEditorPane;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AndContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ExistsContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.OrContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.misc.util.OperatorIDTuple;
import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.generated.parser.ParseException;
import lupos.rif.generated.parser.RIFParser;
import lupos.rif.generated.syntaxtree.CompilationUnit;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RuleList;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.RuleVariable;
import lupos.rif.visitor.NormalizeRuleVisitor;
import lupos.rif.visitor.ParseSyntaxTreeVisitor;
import lupos.rif.visitor.ResolveListsRuleVisitor;
import lupos.rif.visitor.RuleDependencyGraphVisitor;
import lupos.rif.visitor.RuleFilteringVisitor;
import lupos.rif.visitor.SubstituteFunctionCallsVisitor;
import lupos.rif.visitor.ValidateRuleVisitor;
import lupos.sparql1_1.Node;

public class VisualRifGenerator implements IRuleVisitor<Object, Object>{

	String query;
	
	private VisualRifEditor visualRifEditor;
	protected final CommonCoreQueryEvaluator<Node> evaluator = null;
	private CompilationUnit compilationUnit;
	private Document rifDocument;
	private DocumentEditorPane documentEditorPane;
	private PrefixOperator po = null;
	private String[] comboBoxEntries;
	private Scout scout = new Scout();

	
	// Constructor	
	public VisualRifGenerator(String query, DocumentEditorPane that, Console console, JTabbedPane bottomPane, VisualRifEditor visualRifEditor, IRuleNode arg){
		this.setVisualRifEditor(visualRifEditor);
		this.setQuery(query);
		this.setDocumentEditorPane(that);
		
		final RIFParser parser = new RIFParser(new StringReader(query));
		
		try {
			
			this.compilationUnit = parser.CompilationUnit();
			
		} catch (ParseException e) {
			
			console.setText(e.getLocalizedMessage());
			bottomPane.setSelectedIndex(1);

			

//			e.printStackTrace();
			
		}
		
		this.rifDocument = (Document) this.compilationUnit.accept(new ParseSyntaxTreeVisitor(), arg);
		

		final ValidateRuleVisitor valVisitor = new ValidateRuleVisitor();
		final NormalizeRuleVisitor normVisitor = new NormalizeRuleVisitor();
		final SubstituteFunctionCallsVisitor subVisitor = new SubstituteFunctionCallsVisitor();
		final ResolveListsRuleVisitor listVisitor = new ResolveListsRuleVisitor();
		final RuleDependencyGraphVisitor dependencyVisitor = new RuleDependencyGraphVisitor();
		final RuleFilteringVisitor filteringVisitor = new RuleFilteringVisitor();

		this.rifDocument = (Document) this.rifDocument.accept(subVisitor, arg);
		this.rifDocument = (Document) this.rifDocument.accept(listVisitor, arg);
		this.rifDocument = (Document) this.rifDocument.accept(normVisitor, arg);
		this.rifDocument.accept(valVisitor, arg);
		this.rifDocument.accept(dependencyVisitor, arg);
		this.rifDocument.accept(filteringVisitor, arg);
		

		

	}
	


	// Visit 
	@Override
	public Object visit(Document obj, Object arg) throws RIFException {
		
		
		
		// Prefix + Base
		if (!obj.getPrefixMap().isEmpty() || obj.getBaseNamespace() != null) {

			po = new PrefixOperator();
			int prefixCnt = 0;
			
			// Base
			if (obj.getBaseNamespace() != null) {
				po.addEntry("BASE", obj.getBaseNamespace());
				prefixCnt++;
			}
			
			// Prefix
			for (Entry<String, String> e : obj.getPrefixMap().entrySet()) {
				po.addEntry(e.getKey(), e.getValue());
				prefixCnt++;
			}

			this.comboBoxEntries = new String[prefixCnt+3];
			int cnt = prefixCnt;

			if (obj.getBaseNamespace() != null) {
				this.comboBoxEntries[prefixCnt-cnt] = "BASE";
				cnt--;
			}
			
			for (Entry<String, String> e : obj.getPrefixMap().entrySet()) {
				if ( e.getKey().equals("xsd")){;}else
				if ( e.getKey().equals("xs") ){
					this.comboBoxEntries[prefixCnt-cnt] = e.getKey();cnt--;
					this.comboBoxEntries[prefixCnt-cnt] = "xs#integer"; cnt--;
					this.comboBoxEntries[prefixCnt-cnt] = "xs#string";cnt--;
					this.comboBoxEntries[prefixCnt-cnt] = "integer";
					cnt--;
				}else{
					this.comboBoxEntries[prefixCnt-cnt] = e.getKey();
					cnt--;
				}
			}
			
			

		} // end prefix
		
		if (!obj.getFacts().isEmpty()) {


			for (Iterator iterator = obj.getFacts().iterator(); iterator
					.hasNext();) {
				IExpression type = (IExpression) iterator.next();
//				type.accept(this, null);
//				System.out.println(type.get);

			}

		}
		
		
		return  po;
	
	}

	@Override
	public Object visit(Rule obj, Object arg) throws RIFException {
		System.out.println("visit(Rule obj, Object arg)");
		
		RuleOperator ro = new RuleOperator();
		ro.setVisualRifEditor(this.visualRifEditor);
		ro.initRule();

		ro.setUnVisitedObject(obj);

		

		

		
		return ro;
	}

	@Override
	public Object visit(ExistExpression obj, Object arg) throws RIFException {
		System.out.println("visit(ExistExpression obj, Object arg)");

		ExistsContainer existsContainer = new ExistsContainer();
		existsContainer.setVisualRifEditor(visualRifEditor);

		existsContainer.draw(new GraphWrapperOperator(existsContainer),
				(RuleGraph) arg);

		LinkedList<String> existsVar = new LinkedList<String>();


		
		
			
			Operator op = (Operator) obj
						.getChildren()
						.get(obj.getChildren().size()-1)
						.accept(this, existsContainer.getRecursiveOperatorGraph());
			
			for (int i = 0 ; i < obj.getChildren().size()-1; i++){
				

				
				existsVar.add((String)obj.getChildren().get(i).getLabel().substring(1));
				
			
			
			existsContainer.addOperator(op);
		}
		
//		for (int i = 0; i < obj.getChildren().size(); i++) {
//
//			if (obj.getChildren().get(i)
//					.accept(this, existsContainer.getRecursiveOperatorGraph()) instanceof Operator) {
//
//				existsContainer.addOperator((Operator) obj
//						.getChildren()
//						.get(i)
//						.accept(this,
//								existsContainer.getRecursiveOperatorGraph()));
//
//			} else
//
//				existsVar.add((String) obj
//						.getChildren()
//						.get(i)
//						.accept(this,
//								existsContainer.getRecursiveOperatorGraph()));
//
//		}

		return existsContainer;
	}

	@Override
	public Object visit(Conjunction obj, Object arg) throws RIFException {
		System.out.println("visit(Conjunction obj, Object arg)");
	
	
		
			AndContainer andContainer = new AndContainer();
			andContainer.setVisualRifEditor(visualRifEditor);
			
			
			
			AbstractGuiComponent<Operator> recrusiveOperatorGraph = andContainer.draw(new GraphWrapperOperator(andContainer),
					(RuleGraph) arg);
			
//			andContainer.initRecursiveOperatorGraph((RuleGraph)arg);
			
			// TODO
			System.out.println("NullTest andContainer.getRecursiveOperatorGraph(): "+(andContainer.getRecursiveOperatorGraph()==null));
			
			for (int i = 0; i < obj.getChildren().size(); i++) {
				Operator operator = (Operator) obj.getChildren().get(i).accept(this, andContainer.getRecursiveOperatorGraph());
				andContainer.addOperator(operator);
			}

		return andContainer;
	}

	@Override
	public Object visit(Disjunction obj, Object arg) throws RIFException {
		System.out.println("visit(Disjunction obj, Object arg)");
		

		
		OrContainer orContainer = new OrContainer();
		orContainer.setVisualRifEditor(visualRifEditor);
		
		orContainer.draw(new GraphWrapperOperator(orContainer),
				(RuleGraph) arg);
//		orContainer.setRecursiveOperatorGraph(recursiveOperatorGraph)
//		orContainer.initRecursiveOperatorGraph((RuleGraph)arg);
		
		for (int i = 0; i < obj.getChildren().size(); i++) {
			
			orContainer.addOperator((Operator) obj.getChildren().get(i).accept(this, orContainer.getRecursiveOperatorGraph()));
		
		}

	return orContainer;
	}

	@Override
	public Object visit(RulePredicate obj, Object arg) throws RIFException {
		System.out.println("visit(RulePredicate obj, Object arg)");
		LinkedList<Term> terms = new LinkedList<Term>();
		
		
		// FrameOperator
		if(obj.getLabel().contains("[") && obj.getLabel().contains("]")){
			
			FrameOperator frameOperator = new FrameOperator();
			frameOperator.setVisualRifEditor(visualRifEditor);
			frameOperator.setConstantComboBoxEntries(comboBoxEntries);
			

			// create Terms
				
				switch((Integer) obj.termParams.get(0).accept(scout, arg)){

				
				case Scout.RULEVARIABLE :   if(((String)obj.termParams.get(0).accept(this, arg)).startsWith("ALIASVAR")){
												
											}
											Term termVar1 = frameOperator.prepareToCreateVariableTerm((String)obj.termParams.get(0).accept(this, arg)); 
											terms.add(termVar1);
											break;
				
				case Scout.CONSTANT: String[] constArray = new String[2];
									 constArray = (String[]) obj.termParams.get(0).accept(this, arg);
									 Term termConst1 = frameOperator.prepareToCreateConstantTerm(constArray[0],constArray[1],comboBoxEntries);
									 terms.add(termConst1);
									 break;
				
										
							case Scout.RULELIST:   
													
															ListOperator listOperator = (ListOperator) obj.termParams.get(0).accept(this, arg);
															Term listTerm = frameOperator.prepareToCreateListTerm(listOperator, comboBoxEntries);
															terms.add(listTerm);
															break;
													
													
							case Scout.RULEPREDICATE:  
													
															UnitermOperator unitermOperator = (UnitermOperator) obj.termParams.get(0).accept(this, arg);
												
															Term unitermTerm = frameOperator.prepareToCreateUnitermTerm(unitermOperator, comboBoxEntries);
															terms.add(unitermTerm);
				
														break;
														
							case Scout.EXTERNAL:  
								
															UnitermOperator external = (UnitermOperator) obj.termParams.get(0).accept(this, arg);
															
															Term externalTerm = frameOperator.prepareToCreateUnitermTerm(external, comboBoxEntries);
															terms.add(externalTerm);

														
														break;
														
										
							default: 				System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
													break;
			
			}
				

				switch((Integer) obj.termName.accept(scout, arg)){
				
				
				
				case Scout.RULEVARIABLE :   Term termNameVar = frameOperator.prepareToCreateVariableTerm((String)obj.termName.accept(this, arg)); 
											terms.add(termNameVar);
											break;
				
				case Scout.CONSTANT:
									 String[] constArray = new String[2];
									 constArray = (String[]) obj.termName.accept(this, arg);
									 Term termNameConst = frameOperator.prepareToCreateConstantTerm(constArray[0],constArray[1],comboBoxEntries);
									 terms.add(termNameConst);
									 break;
				
										
							case Scout.RULELIST:   
													
															ListOperator listOperator = (ListOperator) obj.termName.accept(this, arg);
															Term listTerm = frameOperator.prepareToCreateListTerm(listOperator, comboBoxEntries);
															terms.add(listTerm);
															break;
													
													
							case Scout.RULEPREDICATE:  
													
															UnitermOperator unitermOperator = (UnitermOperator) obj.termName.accept(this, arg);
												
															Term unitermTerm = frameOperator.prepareToCreateUnitermTerm(unitermOperator, comboBoxEntries);
															terms.add(unitermTerm);
				
														break;
														
							case Scout.EXTERNAL:  
								
															UnitermOperator external = (UnitermOperator) obj.termName.accept(this, arg);
															
															Term externalTerm = frameOperator.prepareToCreateUnitermTerm(external, comboBoxEntries);
															terms.add(externalTerm);

														
														break;
														
										
							default: 				System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
													break;
			
			}
				
				
				switch((Integer) obj.termParams.get(1).accept(scout, arg)){
				
					
				
					case Scout.RULEVARIABLE :  Term termVar2 = frameOperator.prepareToCreateVariableTerm((String)obj.termParams.get(1).accept(this, arg)); 
												terms.add(termVar2);
												break;
					
					case Scout.CONSTANT: String[] constArray = new String[2];
										 constArray = (String[]) obj.termParams.get(1).accept(this, arg);
										 Term termConst2 = frameOperator.prepareToCreateConstantTerm(constArray[0],constArray[1],comboBoxEntries);
										 terms.add(termConst2);
										 break;
					
											
					case Scout.RULELIST:   
											
													ListOperator listOperator = (ListOperator) obj.termParams.get(1).accept(this, arg);
													Term listTerm = frameOperator.prepareToCreateListTerm(listOperator, comboBoxEntries);
													terms.add(listTerm);
													break;
											
											
					case Scout.RULEPREDICATE:  
											
													UnitermOperator unitermOperator = (UnitermOperator) obj.termParams.get(1).accept(this, arg);
										
													Term unitermTerm = frameOperator.prepareToCreateUnitermTerm(unitermOperator, comboBoxEntries);
													terms.add(unitermTerm);
		
												break;
												
					case Scout.EXTERNAL:  
						
													UnitermOperator external = (UnitermOperator) obj.termParams.get(1).accept(this, arg);
													
													Term externalTerm = frameOperator.prepareToCreateUnitermTerm(external, comboBoxEntries);
													terms.add(externalTerm);

												
												break;
												
								
					default: 				System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
											break;
				
				}
				

			frameOperator.setTerms(terms);

			
			if( (arg instanceof RuleGraph) &&((RuleGraph) arg).isRecursiveOperatorGraph()  ){
				System.out.println("!Ich wart hier");
				RuleGraph rg = ((RuleGraph) arg);
				rg.getOperatorContainer().addOperator(frameOperator);
			}
			
			return frameOperator;
			
		}else{
			
		// Uniterm
		UnitermOperator uniTerm = new UnitermOperator();
		uniTerm.setVisualRifEditor(visualRifEditor);
		uniTerm.setConstantComboBoxEntries(comboBoxEntries);
		
		// Prefix + Name
		String[] termPref = (String[]) obj.termName.accept(this, arg);
	
		// set prefix
		uniTerm.getUniTermComboBox().setSelectedItem(termPref[0]);
		uniTerm.setSelectedPrefix(termPref[0]);
		
		// set name
		uniTerm.setTermName(termPref[1]);

		// create Terms
		int tmp = 0;
		
		for (int i = 0; i < obj.termParams.size(); i++) {
			
			tmp =  (Integer) obj.termParams.get(i).accept(scout, arg);
			
			switch(tmp){
			
			
				case Scout.RULEVARIABLE: 				
										
										Term termVar = uniTerm.prepareToCreateVariableTerm((String)obj.termParams.get(i).accept(this, arg));
										terms.add(termVar);
										break;
										
				case Scout.CONSTANT:
				
										String[] constArray = new String[2];
										constArray = (String[]) obj.termParams.get(i).accept(this, arg);
										Term termConst = uniTerm.prepareToCreateConstantTerm(constArray[0],constArray[1],comboBoxEntries);
										terms.add(termConst);
										break;
										
				case Scout.RULELIST:   
										
												ListOperator listOperator = (ListOperator) obj.termParams.get(i).accept(this, arg);
												Term listTerm = uniTerm.prepareToCreateListTerm(listOperator, comboBoxEntries);
												terms.add(listTerm);
												break;
										
										
				case Scout.RULEPREDICATE:  
										
												UnitermOperator unitermOperator = (UnitermOperator) obj.termParams.get(i).accept(this, arg);
									
												Term unitermTerm = uniTerm.prepareToCreateUnitermTerm(unitermOperator, comboBoxEntries);
												terms.add(unitermTerm);
	
											break;
											
				case Scout.EXTERNAL:  
					
												UnitermOperator external = (UnitermOperator) obj.termParams.get(i).accept(this, arg);
												
												Term externalTerm = uniTerm.prepareToCreateUnitermTerm(external, comboBoxEntries);
												terms.add(externalTerm);

											
											break;
											
							
				default: 				System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
										break;
			
			}

			
		} // end for
		
		uniTerm.setTerms(terms);
		
		return uniTerm;
		}
		
		
	
		
	}
	
	@Override
	public Object visit(Equality obj, Object arg) throws RIFException {
		System.out.println("visit(Equality obj, Object arg)");
		
		Object left = obj.leftExpr.accept(this, arg);
		Object right = obj.rightExpr.accept(this, arg);
	   
		// Constant
		if (left instanceof String[]){
			String[] constArray = (String[])left;
			ConstantOperator constOpL = new ConstantOperator();
			constOpL.setConstant(constArray[1]);
			constOpL.setComboBoxEntries(comboBoxEntries);
			constOpL.setVisualRifEditor(visualRifEditor);
			constOpL.getConstantComboBox().setSelectedItem((String)constArray[0]);
			constOpL.setSelectedPrefix((String)constArray[0]);
			
			switch((Integer) obj.rightExpr.accept(scout, arg)){
			
			
			case Scout.RULEVARIABLE: 			
										VariableOperator varOpR = new VariableOperator();
										varOpR.setVariable((String)right);
										OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
										constOpL.addSucceedingOperator(oidtVar);
										break;
										
										
			case Scout.CONSTANT: 		ConstantOperator constOpR = new ConstantOperator();
										constOpR.setVisualRifEditor(visualRifEditor);
										constOpR.setComboBoxEntries(comboBoxEntries);
										String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
										constOpR.setConstant(prefConst[1]);
										constOpR.getConstantComboBox().setSelectedItem((String)prefConst[0]);
										constOpR.setSelectedPrefix((String)prefConst[0]);
										OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
										constOpL.addSucceedingOperator(oidtConst);
										break;
										
			case Scout.RULEPREDICATE:
										
										if(right instanceof UnitermOperator){
											UnitermOperator factOpR = (UnitermOperator) right;
									
											OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
											constOpL.addSucceedingOperator(oidtRulePred);
										}
										
										break;
										
			case Scout.EXTERNAL:		
										if(right instanceof UnitermOperator){
											UnitermOperator factOpR = (UnitermOperator) right;
				
											OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
											constOpL.addSucceedingOperator(oidtRulePred);
										}
			
										break;
				
				
				
			
										
										
			case Scout.RULELIST: 		
				
										
										ListOperator listOpR = (ListOperator) right;
										
										OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
										constOpL.addSucceedingOperator(oidt);
										
										break;
										
										
			default:					break;
										
										
			
			}
		
			
			return constOpL;
		} // End Constant
		
		
		// Variable
		if (left instanceof String){

			VariableOperator varOpL = new VariableOperator();
			varOpL.setVariable((String)left);
			
			switch((Integer) obj.rightExpr.accept(scout, arg)){
			
			
			case Scout.RULEVARIABLE: 			
										VariableOperator varOpR = new VariableOperator();
										varOpR.setVariable((String)right);
										OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
										varOpL.addSucceedingOperator(oidtVar);
										break;
										
										
			case Scout.CONSTANT: 		ConstantOperator constOpR = new ConstantOperator();
										constOpR.setVisualRifEditor(visualRifEditor);
										constOpR.setComboBoxEntries(comboBoxEntries);
										String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
										constOpR.setConstant(prefConst[1]);
										constOpR.getConstantComboBox().setSelectedItem((String)prefConst[0]);
										constOpR.setSelectedPrefix((String)prefConst[0]);
										OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
										varOpL.addSucceedingOperator(oidtConst);
										break;
										
			case Scout.RULEPREDICATE:
										
										if(right instanceof UnitermOperator){
											UnitermOperator factOpR = (UnitermOperator) right;
									
											OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
											varOpL.addSucceedingOperator(oidtRulePred);
										}
										
										break;
										
			case Scout.EXTERNAL:		
										if(right instanceof UnitermOperator){
											UnitermOperator factOpR = (UnitermOperator) right;
				
											OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
											varOpL.addSucceedingOperator(oidtRulePred);
										}
			
										break;
				
				
				
			
										
										
			case Scout.RULELIST: 		
				
										
										ListOperator listOpR = (ListOperator) right;
										
										OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
										varOpL.addSucceedingOperator(oidt);
										
										break;
										
										
			default:					break;
										
										
			
			}
		
			
			return varOpL;
		}// End Variable
		
		// Uniterm
				if (left instanceof UnitermOperator){

					UnitermOperator unitermOperator = (UnitermOperator) left;
					
					switch((Integer) obj.rightExpr.accept(scout, arg)){
					
					
					case Scout.RULEVARIABLE: 			
												VariableOperator varOpR = new VariableOperator();
												varOpR.setVariable((String)right);
												OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
												unitermOperator.addSucceedingOperator(oidtVar);
												break;
												
												
					case Scout.CONSTANT: 	
												
												ConstantOperator constOpR = new ConstantOperator();
												constOpR.setVisualRifEditor(visualRifEditor);
												constOpR.setComboBoxEntries(comboBoxEntries);
												String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
												constOpR.setConstant(prefConst[1]);
												constOpR.getConstantComboBox().setSelectedItem((String)prefConst[0]);
												constOpR.setSelectedPrefix((String)prefConst[0]);
												OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
												unitermOperator.addSucceedingOperator(oidtConst);
												break;
												
												
												
					case Scout.RULEPREDICATE:
												
												if(right instanceof UnitermOperator){
													UnitermOperator factOpR = (UnitermOperator) right;
											
													OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
													unitermOperator.addSucceedingOperator(oidtRulePred);
												}
												
												break;
												
					case Scout.EXTERNAL:		
												if(right instanceof UnitermOperator){
													UnitermOperator factOpR = (UnitermOperator) right;
						
													OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
													unitermOperator.addSucceedingOperator(oidtRulePred);
												}
					
												break;
						
						
						
					
												
												
					case Scout.RULELIST: 		
						
												
												ListOperator listOpR = (ListOperator) right;
												
												OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
												unitermOperator.addSucceedingOperator(oidt);
												
												break;
												
												
					default:					break;
												
												
					
					}
				
					
					return unitermOperator;
				}// End Uniterm
		
				// List
				if (left instanceof ListOperator){

					ListOperator listOperator = (ListOperator) left;
					
					switch((Integer) obj.rightExpr.accept(scout, arg)){
					
					
					case Scout.RULEVARIABLE: 			
												VariableOperator varOpR = new VariableOperator();
												varOpR.setVariable((String)right);
												OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
												listOperator.addSucceedingOperator(oidtVar);
												break;
												
												
					case Scout.CONSTANT: 		
												
												
												ConstantOperator constOpR = new ConstantOperator();
												constOpR.setVisualRifEditor(visualRifEditor);
												constOpR.setComboBoxEntries(comboBoxEntries);
												String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
												constOpR.setConstant(prefConst[1]);
												constOpR.getConstantComboBox().setSelectedItem((String)prefConst[0]);
												constOpR.setSelectedPrefix((String)prefConst[0]);
												OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
												listOperator.addSucceedingOperator(oidtConst);
												break;
												
												
												
												
					case Scout.RULEPREDICATE:
												
												if(right instanceof UnitermOperator){
													UnitermOperator factOpR = (UnitermOperator) right;
											
													OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
													listOperator.addSucceedingOperator(oidtRulePred);
												}
												
												break;
												
					case Scout.EXTERNAL:		
												if(right instanceof UnitermOperator){
													UnitermOperator factOpR = (UnitermOperator) right;
						
													OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
													listOperator.addSucceedingOperator(oidtRulePred);
												}
					
												break;
						
						
						
					
												
												
					case Scout.RULELIST: 		
						
												
												ListOperator listOpR = (ListOperator) right;
												
												OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
												listOperator.addSucceedingOperator(oidt);
												
												break;
												
												
					default:					break;
												
												
					
					}
				
					
					return listOperator;
				}// End List
				
				// Frame
				if (left instanceof FrameOperator){

					FrameOperator frameOperator = (FrameOperator) left;
					
					switch((Integer) obj.rightExpr.accept(scout, arg)){
					
					
					case Scout.RULEVARIABLE: 			
												VariableOperator varOpR = new VariableOperator();
												varOpR.setVariable((String)right);
												OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
												frameOperator.addSucceedingOperator(oidtVar);
												break;
												
												
					case Scout.CONSTANT: 		
												
												
												ConstantOperator constOpR = new ConstantOperator();
												constOpR.setVisualRifEditor(visualRifEditor);
												constOpR.setComboBoxEntries(comboBoxEntries);
												String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
												constOpR.setConstant(prefConst[1]);
												constOpR.getConstantComboBox().setSelectedItem((String)prefConst[0]);
												constOpR.setSelectedPrefix((String)prefConst[0]);
												OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
												frameOperator.addSucceedingOperator(oidtConst);
												break;
												
					case Scout.RULEPREDICATE:
												
												if(right instanceof UnitermOperator){
													UnitermOperator factOpR = (UnitermOperator) right;
											
													OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
													frameOperator.addSucceedingOperator(oidtRulePred);
												}
												
												break;
												
					case Scout.EXTERNAL:		
												if(right instanceof UnitermOperator){
													UnitermOperator factOpR = (UnitermOperator) right;
						
													OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
													frameOperator.addSucceedingOperator(oidtRulePred);
												}
					
												break;
						
						
						
					
												
												
					case Scout.RULELIST: 		
						
												
												ListOperator listOpR = (ListOperator) right;
												
												OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
												frameOperator.addSucceedingOperator(oidt);
												
												break;
												
												
					default:					break;
												
												
					
					}
				
					
					return frameOperator;
				}// End Frame
				
		
		return null;
	}
	
	@Override
	public Object visit(External obj, Object arg) throws RIFException {
		System.out.println("visit(External obj, Object arg)");

		LinkedList<Term> terms = new LinkedList<Term>();
		UnitermOperator uniTerm = new UnitermOperator();
		uniTerm.setVisualRifEditor(visualRifEditor);
		uniTerm.setConstantComboBoxEntries(comboBoxEntries);
		uniTerm.setExternal(true);
		
		// Prefix + Name
		String[] termPref = (String[]) obj.termName.accept(this, arg);
	
		// set prefix
		uniTerm.getUniTermComboBox().setSelectedItem(termPref[0]);
		uniTerm.setSelectedPrefix(termPref[0]);
		
		// set name
		uniTerm.setTermName(termPref[1]);

		// create Terms
		int tmp = 0;
		
		for (int i = 0; i < obj.termParams.size(); i++) {
			
			tmp =  (Integer) obj.termParams.get(i).accept(scout, arg);
			
			switch(tmp){
			
			
				case Scout.RULEVARIABLE: 				
										
										Term termVar = uniTerm.prepareToCreateVariableTerm((String)obj.termParams.get(i).accept(this, arg));
										terms.add(termVar);
										break;
										
				case Scout.CONSTANT:
				
										String[] constArray = new String[2];
										constArray = (String[]) obj.termParams.get(i).accept(this, arg);
										Term termConst = uniTerm.prepareToCreateConstantTerm(constArray[0],constArray[1],comboBoxEntries);
										terms.add(termConst);
										break;
										
				case Scout.RULELIST:   
										
										ListOperator listOperator = (ListOperator) obj.termParams.get(i).accept(this, arg);
										Term listTerm = uniTerm.prepareToCreateListTerm(listOperator, comboBoxEntries);
										terms.add(listTerm);
										break;
			
			
				case Scout.RULEPREDICATE:  
			
										UnitermOperator unitermOperator = (UnitermOperator) obj.termParams.get(i).accept(this, arg);
							
										Term unitermTerm = uniTerm.prepareToCreateUnitermTerm(unitermOperator, comboBoxEntries);
										terms.add(unitermTerm);
					
									break;
				
				case Scout.EXTERNAL:  

											UnitermOperator external = (UnitermOperator) obj.termParams.get(i).accept(this, arg);
											
											Term externalTerm = uniTerm.prepareToCreateUnitermTerm(external, comboBoxEntries);
											terms.add(externalTerm);
						
										
										break;
											
							
				default: 				System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
										break;
			
			}

			
		} // end for
		
		uniTerm.setTerms(terms);
		
		return uniTerm;
	}

	@Override
	public Object visit(RuleList obj, Object arg) throws RIFException {
		System.out.println("visit(RuleList obj, Object arg)");
		
		LinkedList<Term> terms = new LinkedList<Term>();
		ListOperator listOperator = new ListOperator();
		listOperator.setVisualRifEditor(visualRifEditor);
		listOperator.setConstantComboBoxEntries(comboBoxEntries);
		listOperator.setOpen(obj.isOpen);



		// create Terms
				int tmp = 0;
				
				for (int i = 0; i < obj.getItems().size(); i++) {
					
					tmp =  (Integer) obj.getItems().get(i).accept(scout, arg);
					
					switch(tmp){
					
					
						case Scout.RULEVARIABLE: 				
												
												Term termVar = listOperator.prepareToCreateVariableTerm((String)obj.getItems().get(i).accept(this, arg));
												terms.add(termVar);
												break;
												
						case Scout.CONSTANT:
						
												String[] constArray = new String[2];
												constArray = (String[]) obj.getItems().get(i).accept(this, arg);
												Term termConst = listOperator.prepareToCreateConstantTerm(constArray[0],constArray[1],comboBoxEntries);
												terms.add(termConst);
												break;
												
						case Scout.RULELIST:   
												
														ListOperator listOperatorTerm = (ListOperator) obj.getItems().get(i).accept(this, arg);
														Term listTerm = listOperator.prepareToCreateListTerm(listOperatorTerm, comboBoxEntries);
														terms.add(listTerm);
														break;
												
												
						case Scout.RULEPREDICATE:  
												
														UnitermOperator unitermOperator = (UnitermOperator) obj.getItems().get(i).accept(this, arg);
											
														Term unitermTerm = listOperator.prepareToCreateUnitermTerm(unitermOperator, comboBoxEntries);
														terms.add(unitermTerm);
			
													break;
													
						case Scout.EXTERNAL:  
							
														UnitermOperator external = (UnitermOperator) obj.getItems().get(i).accept(this, arg);
														
														Term externalTerm = listOperator.prepareToCreateUnitermTerm(external, comboBoxEntries);
														terms.add(externalTerm);

													
													break;
													
									
						default: 				System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
												break;
					
					}

					
				} // end for
		listOperator.setTerms(terms);
		return listOperator;
	}

	@Override
	public Object visit(RuleVariable obj, Object arg) throws RIFException {
		System.out.println("visit(RuleVariable obj, Object arg)");
		
		return obj.getLabel().substring(1);
	}

	@Override
	public Object visit(Constant obj, Object arg) throws RIFException {
		System.out.println("visit(Constant obj, Object arg)");

		// [0] = Prefix ; [1] = name
		String[] prefValueArray = new String[2];
		
		
		
				// prefix:postfix
//				Pattern prefixPostfixPattern = Pattern.compile( ".+:.+" ); 
//				if ( Pattern.matches(".+:.+", obj.getLabel()) ){
//					System.out.println("prefix:postfix");
//				}
				
				// BASE:postfix
//				Pattern basePostfixPattern = Pattern.compile( ":.+" ); 
				if ( Pattern.matches( ":.+", obj.getLabel() ) ){
					System.out.println("BASE:postfix");
				}
				
				// simpleLiteral
//				Pattern simpleLiteralPattern = Pattern.compile( "<.+>" ); 
				if ( Pattern.matches( "<http://.+>", obj.getLabel() ) ){

					String iri = obj.getLabel().substring(1, obj.getLabel().length()-1);
					for (Entry<String, String> entry : po.getPrefixList().entrySet()) {
						if(iri.startsWith(entry.getKey())){
							prefValueArray[0] = entry.getValue();
							prefValueArray[1] = iri.substring(entry.getKey().length(), iri.length());
							return prefValueArray;
						}
					}
				}else	if ( Pattern.matches( "<.+>", obj.getLabel() ) ){
		
					String iri = obj.getLabel().substring(1, obj.getLabel().length()-1);
					
							prefValueArray[0] = "BASE";
							prefValueArray[1] = iri;
							return prefValueArray;
					
				}
				

//				// typedLiteralPatternINTEGER
//				if ( Pattern.matches( "\"\\d+\"\\^\\^.+:.+",  obj.getLabel() ) ){
//					String value, prefix, type; value = prefix = type = "";
//					
//					System.out.println("typedLiteralPatternINTEGER");
//					for ( MatchResult r : findMatches( "\"\\d+\"", obj.getLabel() ) ) 
//						  value = r.group().substring(1, r.group().length()-1);
//
//	
//							prefValueArray[0] = "integer";
//							prefValueArray[1] = value;
//							return prefValueArray;
//				
//					
//				}else			
				// typedLiteralPattern
				if ( Pattern.matches( "\".*\"\\^\\^.+:.+",  obj.getLabel() ) ){
					String value, prefix, type; value = prefix = type = "";
					
		
					for ( MatchResult r : findMatches( "\".*\"", obj.getLabel() ) ) 
						  value = r.group().substring(1, r.group().length()-1);
					for ( MatchResult r : findMatches( "<.+#", obj.getLabel() ) ) 
						 prefix = r.group().substring(1, r.group().length());
					for ( MatchResult r : findMatches( "#.+>", obj.getLabel() ) ) 
						 type = r.group().substring(1, r.group().length()-1);
					
		
					for (Entry<String, String> entry : po.getPrefixList().entrySet()) {
		
						if((prefix).equals(entry.getKey())){
							prefValueArray[0] = entry.getValue()+"#"+type;
							prefValueArray[1] = value;
							return prefValueArray;
						}
					}
					
				}
				
			
				
				
				// typedLiteralXSPattern
//				Pattern typedLiteralXSPattern = Pattern.compile( "\".+\"" ); 
				if ( Pattern.matches( "\".+\"", obj.getLabel() ) ){
					String value  = "";
					
		
					for ( MatchResult r : findMatches( "\".*\"", obj.getLabel() ) ) 
						  value = r.group().substring(1, r.group().length()-1);
				
					prefValueArray[0] = "xs#string";
					prefValueArray[1] = value;
					return prefValueArray;
					
				}
				
				// integerLiteralXSPattern
//				Pattern integerLiteralXSPattern = Pattern.compile( "\\d+" ); 
				if ( Pattern.matches( "\\d*", obj.getLabel()) ){
					
				}
		
				// languagetaggedLiteralXSPattern
//				Pattern languagetaggedLiteralXSPattern = Pattern.compile( "\".*\"\\@.+" ); 
				if ( Pattern.matches( "\".*\"\\@.+" , obj.getLabel() ) ){
					System.out.println("languagetaggedLiteralXSPattern");
				}
		
		
		
		
		
		
		
		// TODO Für einzelne Konstanten die ConstantCombo anhägenen
		// prefValueArray sichern
		String[] tmp = prefValueArray;
		int cnt = prefValueArray.length + this.comboBoxEntries.length;
		prefValueArray = new String[cnt];
		

			for (int j = 0; j < tmp.length; j++) {
				prefValueArray[j] = tmp[j];
			}
			
			for (int i = 0; i < this.comboBoxEntries.length; i++) {
				prefValueArray[i+tmp.length] = tmp[i];
			}
		
		
		
		
//		String iri = obj.getLabel().substring(1, obj.getLabel().length()-1);
//		
//		System.out.println("IRI: "+iri);
//		
//		// "constant"
//		if ( obj.getLabel().startsWith("\"") &&  obj.getLabel().endsWith("\"") ){
//			
//			prefValueArray[0] = "";
//			prefValueArray[1] = obj.getLabel();
//			
//			return prefValueArray;
//		
//		}
//
//		// pref:const
//		for (Entry<String, String> entry : po.getPrefixList().entrySet()) {
//			if(iri.startsWith(entry.getKey())){
//				prefValueArray[0] = entry.getValue();
//				prefValueArray[1] = iri.substring(entry.getKey().length(), iri.length());
//				return prefValueArray;
//			}
//		}
//		
//
//		
//		if(iri.endsWith("\"^^<http://www.w3.org/2001/XMLSchema#integer")){
//			
//			prefValueArray[0] = "Integer";
//			prefValueArray[1] = iri.substring(0, iri.indexOf("\"^^<http://www.w3.org/2001/XMLSchema#integer")); 
//			
//		}else{
//		
//		prefValueArray[0] = "BASE";
//		prefValueArray[1] = iri; 
//		}
		
		return prefValueArray;
	}
	
	
	public static Iterable<MatchResult> findMatches( String pattern, CharSequence s ) 
	{ 
	  List<MatchResult> results = new ArrayList<MatchResult>(); 
	 
	  for ( Matcher m = Pattern.compile(pattern).matcher(s); m.find(); ) 
	    results.add( m.toMatchResult() ); 
	 
	  return results; 
	}
	
	
	/* *************** **
	 * Getter + Setter **
	 * *************** */
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public Document getRifDocument() {
		return rifDocument;
	}

	public void setRifDocument(Document rifDocument) {
		this.rifDocument = rifDocument;
	}

	public CommonCoreQueryEvaluator<Node> getEvaluator() {
		return evaluator;
	}

	public DocumentEditorPane getDocumentEditorPane() {
		return documentEditorPane;
	}
	
	public void setDocumentEditorPane(DocumentEditorPane documentEditorPane) {
		this.documentEditorPane = documentEditorPane;
	}



	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}



	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}




	
	
	

}
