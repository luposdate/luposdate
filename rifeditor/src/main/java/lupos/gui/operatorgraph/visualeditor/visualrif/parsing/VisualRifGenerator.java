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
	private final Scout scout = new Scout();


	// Constructor
	public VisualRifGenerator(final String query, final DocumentEditorPane that, final Console console, final JTabbedPane bottomPane, final VisualRifEditor visualRifEditor, final IRuleNode arg){
		this.setVisualRifEditor(visualRifEditor);
		this.setQuery(query);
		this.setDocumentEditorPane(that);

		final RIFParser parser = new RIFParser(new StringReader(query));

		try {

			this.compilationUnit = parser.CompilationUnit();

		} catch (final ParseException e) {
			console.setText(e.getLocalizedMessage());
			bottomPane.setSelectedIndex(1);
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
	public Object visit(final Document obj, final Object arg) throws RIFException {
		// Prefix + Base
		if (!obj.getPrefixMap().isEmpty() || obj.getBaseNamespace() != null) {
			this.po = new PrefixOperator();
			int prefixCnt = 0;

			// Base
			if (obj.getBaseNamespace() != null) {
				this.po.addEntry("BASE", obj.getBaseNamespace());
				prefixCnt++;
			}

			// Prefix
			for (final Entry<String, String> e : obj.getPrefixMap().entrySet()) {
				this.po.addEntry(e.getKey(), e.getValue());
				prefixCnt++;
			}

			this.comboBoxEntries = new String[prefixCnt+3];
			int cnt = prefixCnt;

			if (obj.getBaseNamespace() != null) {
				this.comboBoxEntries[prefixCnt-cnt] = "BASE";
				cnt--;
			}

			for (final Entry<String, String> e : obj.getPrefixMap().entrySet()) {
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

			for (final Iterator iterator = obj.getFacts().iterator(); iterator
					.hasNext();) {
				final IExpression type = (IExpression) iterator.next();
			}

		}
		return  this.po;

	}

	@Override
	public Object visit(final Rule obj, final Object arg) throws RIFException {
		System.out.println("visit(Rule obj, Object arg)");

		final RuleOperator ro = new RuleOperator();
		ro.setVisualRifEditor(this.visualRifEditor);
		ro.initRule();

		ro.setUnVisitedObject(obj);
		return ro;
	}

	@Override
	public Object visit(final ExistExpression obj, final Object arg) throws RIFException {
		System.out.println("visit(ExistExpression obj, Object arg)");

		final ExistsContainer existsContainer = new ExistsContainer();
		existsContainer.setVisualRifEditor(this.visualRifEditor);

		existsContainer.draw(new GraphWrapperOperator(existsContainer),
				(RuleGraph) arg);

		final LinkedList<String> existsVar = new LinkedList<String>();

		final Operator op = (Operator) obj
				.getChildren()
				.get(obj.getChildren().size()-1)
				.accept(this, existsContainer.getRecursiveOperatorGraph());

		for (int i = 0 ; i < obj.getChildren().size()-1; i++){
			existsVar.add(obj.getChildren().get(i).getLabel().substring(1));
			existsContainer.addOperator(op);
		}
		return existsContainer;
	}

	@Override
	public Object visit(final Conjunction obj, final Object arg) throws RIFException {
		System.out.println("visit(Conjunction obj, Object arg)");

		final AndContainer andContainer = new AndContainer();
		andContainer.setVisualRifEditor(this.visualRifEditor);

		final AbstractGuiComponent<Operator> recrusiveOperatorGraph = andContainer.draw(new GraphWrapperOperator(andContainer),
				(RuleGraph) arg);
		for (int i = 0; i < obj.getChildren().size(); i++) {
			final Operator operator = (Operator) obj.getChildren().get(i).accept(this, andContainer.getRecursiveOperatorGraph());
			andContainer.addOperator(operator);
		}
		return andContainer;
	}

	@Override
	public Object visit(final Disjunction obj, final Object arg) throws RIFException {
		System.out.println("visit(Disjunction obj, Object arg)");

		final OrContainer orContainer = new OrContainer();
		orContainer.setVisualRifEditor(this.visualRifEditor);

		orContainer.draw(new GraphWrapperOperator(orContainer),
				(RuleGraph) arg);
		for (int i = 0; i < obj.getChildren().size(); i++) {
			orContainer.addOperator((Operator) obj.getChildren().get(i).accept(this, orContainer.getRecursiveOperatorGraph()));
		}
		return orContainer;
	}

	@Override
	public Object visit(final RulePredicate obj, final Object arg) throws RIFException {
		System.out.println("visit(RulePredicate obj, Object arg)");
		final LinkedList<Term> terms = new LinkedList<Term>();

		// FrameOperator
		if(obj.getLabel().contains("[") && obj.getLabel().contains("]")){
			final FrameOperator frameOperator = new FrameOperator();
			frameOperator.setVisualRifEditor(this.visualRifEditor);
			frameOperator.setConstantComboBoxEntries(this.comboBoxEntries);
			// create Terms

			switch((Integer) obj.termParams.get(0).accept(this.scout, arg)){
			case Scout.RULEVARIABLE :   if(((String)obj.termParams.get(0).accept(this, arg)).startsWith("ALIASVAR")){
				// TODO: check!
			}
			final Term termVar1 = frameOperator.prepareToCreateVariableTerm((String)obj.termParams.get(0).accept(this, arg));
			terms.add(termVar1);
			break;

			case Scout.CONSTANT: String[] constArray = new String[2];
			constArray = (String[]) obj.termParams.get(0).accept(this, arg);
			final Term termConst1 = frameOperator.prepareToCreateConstantTerm(constArray[0],constArray[1],this.comboBoxEntries);
			terms.add(termConst1);
			break;
			case Scout.RULELIST:
				final ListOperator listOperator = (ListOperator) obj.termParams.get(0).accept(this, arg);
				final Term listTerm = frameOperator.prepareToCreateListTerm(listOperator, this.comboBoxEntries);
				terms.add(listTerm);
				break;
			case Scout.RULEPREDICATE:
				final UnitermOperator unitermOperator = (UnitermOperator) obj.termParams.get(0).accept(this, arg);
				final Term unitermTerm = frameOperator.prepareToCreateUnitermTerm(unitermOperator, this.comboBoxEntries);
				terms.add(unitermTerm);
				break;
			case Scout.EXTERNAL:
				final UnitermOperator external = (UnitermOperator) obj.termParams.get(0).accept(this, arg);
				final Term externalTerm = frameOperator.prepareToCreateUnitermTerm(external, this.comboBoxEntries);
				terms.add(externalTerm);
				break;
			default:
				System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
				break;
			}

			switch((Integer) obj.termName.accept(this.scout, arg)){
			case Scout.RULEVARIABLE :
				final Term termNameVar = frameOperator.prepareToCreateVariableTerm((String)obj.termName.accept(this, arg));
				terms.add(termNameVar);
				break;

			case Scout.CONSTANT:
				String[] constArray = new String[2];
				constArray = (String[]) obj.termName.accept(this, arg);
				final Term termNameConst = frameOperator.prepareToCreateConstantTerm(constArray[0],constArray[1],this.comboBoxEntries);
				terms.add(termNameConst);
				break;

			case Scout.RULELIST:
				final ListOperator listOperator = (ListOperator) obj.termName.accept(this, arg);
				final Term listTerm = frameOperator.prepareToCreateListTerm(listOperator, this.comboBoxEntries);
				terms.add(listTerm);
				break;

			case Scout.RULEPREDICATE:
				final UnitermOperator unitermOperator = (UnitermOperator) obj.termName.accept(this, arg);
				final Term unitermTerm = frameOperator.prepareToCreateUnitermTerm(unitermOperator, this.comboBoxEntries);
				terms.add(unitermTerm);
				break;

			case Scout.EXTERNAL:
				final UnitermOperator external = (UnitermOperator) obj.termName.accept(this, arg);
				final Term externalTerm = frameOperator.prepareToCreateUnitermTerm(external, this.comboBoxEntries);
				terms.add(externalTerm);
				break;

			default:
				System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
				break;
			}

			switch((Integer) obj.termParams.get(1).accept(this.scout, arg)){
			case Scout.RULEVARIABLE :
				final Term termVar2 = frameOperator.prepareToCreateVariableTerm((String)obj.termParams.get(1).accept(this, arg));
				terms.add(termVar2);
				break;

			case Scout.CONSTANT: String[] constArray = new String[2];
				constArray = (String[]) obj.termParams.get(1).accept(this, arg);
				final Term termConst2 = frameOperator.prepareToCreateConstantTerm(constArray[0],constArray[1],this.comboBoxEntries);
				terms.add(termConst2);
				break;

			case Scout.RULELIST:
				final ListOperator listOperator = (ListOperator) obj.termParams.get(1).accept(this, arg);
				final Term listTerm = frameOperator.prepareToCreateListTerm(listOperator, this.comboBoxEntries);
				terms.add(listTerm);
				break;

			case Scout.RULEPREDICATE:
				final UnitermOperator unitermOperator = (UnitermOperator) obj.termParams.get(1).accept(this, arg);
				final Term unitermTerm = frameOperator.prepareToCreateUnitermTerm(unitermOperator, this.comboBoxEntries);
				terms.add(unitermTerm);
				break;

			case Scout.EXTERNAL:
				final UnitermOperator external = (UnitermOperator) obj.termParams.get(1).accept(this, arg);
				final Term externalTerm = frameOperator.prepareToCreateUnitermTerm(external, this.comboBoxEntries);
				terms.add(externalTerm);
				break;

			default:
				System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
				break;
			}
			frameOperator.setTerms(terms);
			if( (arg instanceof RuleGraph) &&((RuleGraph) arg).isRecursiveOperatorGraph()  ){
				System.out.println("!Ich wart hier");
				final RuleGraph rg = ((RuleGraph) arg);
				rg.getOperatorContainer().addOperator(frameOperator);
			}
			return frameOperator;
		} else {

			// Uniterm
			final UnitermOperator uniTerm = new UnitermOperator();
			uniTerm.setVisualRifEditor(this.visualRifEditor);
			uniTerm.setConstantComboBoxEntries(this.comboBoxEntries);

			// Prefix + Name
			final String[] termPref = (String[]) obj.termName.accept(this, arg);

			// set prefix
			uniTerm.getUniTermComboBox().setSelectedItem(termPref[0]);
			uniTerm.setSelectedPrefix(termPref[0]);

			// set name
			uniTerm.setTermName(termPref[1]);

			// create Terms
			int tmp = 0;
			for (int i = 0; i < obj.termParams.size(); i++) {
				tmp =  (Integer) obj.termParams.get(i).accept(this.scout, arg);
				switch(tmp){
				case Scout.RULEVARIABLE:
					final Term termVar = uniTerm.prepareToCreateVariableTerm((String)obj.termParams.get(i).accept(this, arg));
					terms.add(termVar);
					break;

				case Scout.CONSTANT:
					String[] constArray = new String[2];
					constArray = (String[]) obj.termParams.get(i).accept(this, arg);
					final Term termConst = uniTerm.prepareToCreateConstantTerm(constArray[0],constArray[1],this.comboBoxEntries);
					terms.add(termConst);
					break;

				case Scout.RULELIST:
					final ListOperator listOperator = (ListOperator) obj.termParams.get(i).accept(this, arg);
					final Term listTerm = uniTerm.prepareToCreateListTerm(listOperator, this.comboBoxEntries);
					terms.add(listTerm);
					break;

				case Scout.RULEPREDICATE:
					final UnitermOperator unitermOperator = (UnitermOperator) obj.termParams.get(i).accept(this, arg);
					final Term unitermTerm = uniTerm.prepareToCreateUnitermTerm(unitermOperator, this.comboBoxEntries);
					terms.add(unitermTerm);
					break;

				case Scout.EXTERNAL:
					final UnitermOperator external = (UnitermOperator) obj.termParams.get(i).accept(this, arg);
					final Term externalTerm = uniTerm.prepareToCreateUnitermTerm(external, this.comboBoxEntries);
					terms.add(externalTerm);
					break;

					default:
						System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
						break;
				}
			} // end for
			uniTerm.setTerms(terms);
			return uniTerm;
		}
	}

	@Override
	public Object visit(final Equality obj, final Object arg) throws RIFException {
		System.out.println("visit(Equality obj, Object arg)");

		final Object left = obj.leftExpr.accept(this, arg);
		final Object right = obj.rightExpr.accept(this, arg);

		// Constant
		if (left instanceof String[]){
			final String[] constArray = (String[])left;
			final ConstantOperator constOpL = new ConstantOperator();
			constOpL.setConstant(constArray[1]);
			constOpL.setComboBoxEntries(this.comboBoxEntries);
			constOpL.setVisualRifEditor(this.visualRifEditor);
			constOpL.getConstantComboBox().setSelectedItem(constArray[0]);
			constOpL.setSelectedPrefix(constArray[0]);

			switch((Integer) obj.rightExpr.accept(this.scout, arg)){
				case Scout.RULEVARIABLE:
					final VariableOperator varOpR = new VariableOperator();
					varOpR.setVariable((String)right);
					final OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
					constOpL.addSucceedingOperator(oidtVar);
					break;

				case Scout.CONSTANT:
					final ConstantOperator constOpR = new ConstantOperator();
					constOpR.setVisualRifEditor(this.visualRifEditor);
					constOpR.setComboBoxEntries(this.comboBoxEntries);
					final String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
					constOpR.setConstant(prefConst[1]);
					constOpR.getConstantComboBox().setSelectedItem(prefConst[0]);
					constOpR.setSelectedPrefix(prefConst[0]);
					final OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
					constOpL.addSucceedingOperator(oidtConst);
					break;

				case Scout.RULEPREDICATE:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;

						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						constOpL.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.EXTERNAL:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;
						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						constOpL.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.RULELIST:
					final ListOperator listOpR = (ListOperator) right;
					final OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
					constOpL.addSucceedingOperator(oidt);
					break;

				default:
					break;
			}
			return constOpL;
		} // End Constant
		// Variable
		if (left instanceof String){

			final VariableOperator varOpL = new VariableOperator();
			varOpL.setVariable((String)left);

			switch((Integer) obj.rightExpr.accept(this.scout, arg)){

				case Scout.RULEVARIABLE:
					final VariableOperator varOpR = new VariableOperator();
					varOpR.setVariable((String)right);
					final OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
					varOpL.addSucceedingOperator(oidtVar);
					break;

				case Scout.CONSTANT:
					final ConstantOperator constOpR = new ConstantOperator();
					constOpR.setVisualRifEditor(this.visualRifEditor);
					constOpR.setComboBoxEntries(this.comboBoxEntries);
					final String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
					constOpR.setConstant(prefConst[1]);
					constOpR.getConstantComboBox().setSelectedItem(prefConst[0]);
					constOpR.setSelectedPrefix(prefConst[0]);
					final OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
					varOpL.addSucceedingOperator(oidtConst);
					break;

				case Scout.RULEPREDICATE:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;

						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						varOpL.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.EXTERNAL:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;

						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						varOpL.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.RULELIST:
					final ListOperator listOpR = (ListOperator) right;
					final OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
					varOpL.addSucceedingOperator(oidt);
					break;

				default:
					break;
			}
			return varOpL;
		}// End Variable

		// Uniterm
		if (left instanceof UnitermOperator){
			final UnitermOperator unitermOperator = (UnitermOperator) left;
			switch((Integer) obj.rightExpr.accept(this.scout, arg)){
				case Scout.RULEVARIABLE:
					final VariableOperator varOpR = new VariableOperator();
					varOpR.setVariable((String)right);
					final OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
					unitermOperator.addSucceedingOperator(oidtVar);
					break;

				case Scout.CONSTANT:
					final ConstantOperator constOpR = new ConstantOperator();
					constOpR.setVisualRifEditor(this.visualRifEditor);
					constOpR.setComboBoxEntries(this.comboBoxEntries);
					final String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
					constOpR.setConstant(prefConst[1]);
					constOpR.getConstantComboBox().setSelectedItem(prefConst[0]);
					constOpR.setSelectedPrefix(prefConst[0]);
					final OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
					unitermOperator.addSucceedingOperator(oidtConst);
					break;

				case Scout.RULEPREDICATE:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;
						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						unitermOperator.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.EXTERNAL:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;
						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						unitermOperator.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.RULELIST:
					final ListOperator listOpR = (ListOperator) right;
					final OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
					unitermOperator.addSucceedingOperator(oidt);
					break;

				default:
					break;
			}
			return unitermOperator;
		}// End Uniterm

		// List
		if (left instanceof ListOperator){
			final ListOperator listOperator = (ListOperator) left;
			switch((Integer) obj.rightExpr.accept(this.scout, arg)){

				case Scout.RULEVARIABLE:
					final VariableOperator varOpR = new VariableOperator();
					varOpR.setVariable((String)right);
					final OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
					listOperator.addSucceedingOperator(oidtVar);
					break;

				case Scout.CONSTANT:
					final ConstantOperator constOpR = new ConstantOperator();
					constOpR.setVisualRifEditor(this.visualRifEditor);
					constOpR.setComboBoxEntries(this.comboBoxEntries);
					final String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
					constOpR.setConstant(prefConst[1]);
					constOpR.getConstantComboBox().setSelectedItem(prefConst[0]);
					constOpR.setSelectedPrefix(prefConst[0]);
					final OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
					listOperator.addSucceedingOperator(oidtConst);
					break;

				case Scout.RULEPREDICATE:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;
						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						listOperator.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.EXTERNAL:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;
						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						listOperator.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.RULELIST:
					final ListOperator listOpR = (ListOperator) right;
					final OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
					listOperator.addSucceedingOperator(oidt);
					break;

				default:
					break;
			}

			return listOperator;
		}// End List

		// Frame
		if (left instanceof FrameOperator){
			final FrameOperator frameOperator = (FrameOperator) left;
			switch((Integer) obj.rightExpr.accept(this.scout, arg)){

				case Scout.RULEVARIABLE:
					final VariableOperator varOpR = new VariableOperator();
					varOpR.setVariable((String)right);
					final OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (varOpR, 0);
					frameOperator.addSucceedingOperator(oidtVar);
					break;

				case Scout.CONSTANT:
					final ConstantOperator constOpR = new ConstantOperator();
					constOpR.setVisualRifEditor(this.visualRifEditor);
					constOpR.setComboBoxEntries(this.comboBoxEntries);
					final String[] prefConst = (String[]) obj.rightExpr.accept(this, arg);
					constOpR.setConstant(prefConst[1]);
					constOpR.getConstantComboBox().setSelectedItem(prefConst[0]);
					constOpR.setSelectedPrefix(prefConst[0]);
					final OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (constOpR, 0);
					frameOperator.addSucceedingOperator(oidtConst);
					break;

				case Scout.RULEPREDICATE:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;
						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						frameOperator.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.EXTERNAL:
					if(right instanceof UnitermOperator){
						final UnitermOperator factOpR = (UnitermOperator) right;
						final OperatorIDTuple<Operator> oidtRulePred = new OperatorIDTuple<Operator> (factOpR, 0);
						frameOperator.addSucceedingOperator(oidtRulePred);
					}
					break;

				case Scout.RULELIST:
					final ListOperator listOpR = (ListOperator) right;
					final OperatorIDTuple<Operator> oidt = new OperatorIDTuple<Operator> (listOpR, 0);
					frameOperator.addSucceedingOperator(oidt);
					break;

				default:
					break;
			}
			return frameOperator;
		}// End Frame
		return null;
	}

	@Override
	public Object visit(final External obj, final Object arg) throws RIFException {
		System.out.println("visit(External obj, Object arg)");

		final LinkedList<Term> terms = new LinkedList<Term>();
		final UnitermOperator uniTerm = new UnitermOperator();
		uniTerm.setVisualRifEditor(this.visualRifEditor);
		uniTerm.setConstantComboBoxEntries(this.comboBoxEntries);
		uniTerm.setExternal(true);

		// Prefix + Name
		final String[] termPref = (String[]) obj.termName.accept(this, arg);

		// set prefix
		uniTerm.getUniTermComboBox().setSelectedItem(termPref[0]);
		uniTerm.setSelectedPrefix(termPref[0]);

		// set name
		uniTerm.setTermName(termPref[1]);

		// create Terms
		int tmp = 0;

		for (int i = 0; i < obj.termParams.size(); i++) {

			tmp =  (Integer) obj.termParams.get(i).accept(this.scout, arg);

			switch(tmp){

				case Scout.RULEVARIABLE:
					final Term termVar = uniTerm.prepareToCreateVariableTerm((String)obj.termParams.get(i).accept(this, arg));
					terms.add(termVar);
					break;

				case Scout.CONSTANT:
					String[] constArray = new String[2];
					constArray = (String[]) obj.termParams.get(i).accept(this, arg);
					final Term termConst = uniTerm.prepareToCreateConstantTerm(constArray[0],constArray[1],this.comboBoxEntries);
					terms.add(termConst);
					break;

				case Scout.RULELIST:
					final ListOperator listOperator = (ListOperator) obj.termParams.get(i).accept(this, arg);
					final Term listTerm = uniTerm.prepareToCreateListTerm(listOperator, this.comboBoxEntries);
					terms.add(listTerm);
					break;

				case Scout.RULEPREDICATE:
					final UnitermOperator unitermOperator = (UnitermOperator) obj.termParams.get(i).accept(this, arg);
					final Term unitermTerm = uniTerm.prepareToCreateUnitermTerm(unitermOperator, this.comboBoxEntries);
					terms.add(unitermTerm);
					break;

				case Scout.EXTERNAL:
					final UnitermOperator external = (UnitermOperator) obj.termParams.get(i).accept(this, arg);
					final Term externalTerm = uniTerm.prepareToCreateUnitermTerm(external, this.comboBoxEntries);
					terms.add(externalTerm);
					break;

				default:
					System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
					break;
			}
		} // end for
		uniTerm.setTerms(terms);
		return uniTerm;
	}

	@Override
	public Object visit(final RuleList obj, final Object arg) throws RIFException {
		System.out.println("visit(RuleList obj, Object arg)");

		final LinkedList<Term> terms = new LinkedList<Term>();
		final ListOperator listOperator = new ListOperator();
		listOperator.setVisualRifEditor(this.visualRifEditor);
		listOperator.setConstantComboBoxEntries(this.comboBoxEntries);
		listOperator.setOpen(obj.isOpen);

		// create Terms
		int tmp = 0;
		for (int i = 0; i < obj.getItems().size(); i++) {
			tmp =  (Integer) obj.getItems().get(i).accept(this.scout, arg);
			switch(tmp){

				case Scout.RULEVARIABLE:
					final Term termVar = listOperator.prepareToCreateVariableTerm((String)obj.getItems().get(i).accept(this, arg));
					terms.add(termVar);
					break;

				case Scout.CONSTANT:
					String[] constArray = new String[2];
					constArray = (String[]) obj.getItems().get(i).accept(this, arg);
					final Term termConst = listOperator.prepareToCreateConstantTerm(constArray[0],constArray[1],this.comboBoxEntries);
					terms.add(termConst);
					break;

				case Scout.RULELIST:
					final ListOperator listOperatorTerm = (ListOperator) obj.getItems().get(i).accept(this, arg);
					final Term listTerm = listOperator.prepareToCreateListTerm(listOperatorTerm, this.comboBoxEntries);
					terms.add(listTerm);
					break;

				case Scout.RULEPREDICATE:
					final UnitermOperator unitermOperator = (UnitermOperator) obj.getItems().get(i).accept(this, arg);
					final Term unitermTerm = listOperator.prepareToCreateUnitermTerm(unitermOperator, this.comboBoxEntries);
					terms.add(unitermTerm);
					break;

				case Scout.EXTERNAL:
					final UnitermOperator external = (UnitermOperator) obj.getItems().get(i).accept(this, arg);
					final Term externalTerm = listOperator.prepareToCreateUnitermTerm(external, this.comboBoxEntries);
					terms.add(externalTerm);
					break;

				default:
					System.err.println("VisualRifGenerator.visit(RulePredicate obj, Object arg)");//TODO
					break;
			}
		} // end for
		listOperator.setTerms(terms);
		return listOperator;
	}

	@Override
	public Object visit(final RuleVariable obj, final Object arg) throws RIFException {
		System.out.println("visit(RuleVariable obj, Object arg)");
		return obj.getLabel().substring(1);
	}

	@Override
	public Object visit(final Constant obj, final Object arg) throws RIFException {
		System.out.println("visit(Constant obj, Object arg)");

		String[] prefValueArray = new String[2];

		if ( Pattern.matches( ":.+", obj.getLabel() ) ){
			System.out.println("BASE:postfix");
		}

		if ( Pattern.matches( "<http://.+>", obj.getLabel() ) ){
			final String iri = obj.getLabel().substring(1, obj.getLabel().length()-1);
			for (final Entry<String, String> entry : this.po.getPrefixList().entrySet()) {
				if(iri.startsWith(entry.getKey())){
					prefValueArray[0] = entry.getValue();
					prefValueArray[1] = iri.substring(entry.getKey().length(), iri.length());
					return prefValueArray;
				}
			}
		} else	if ( Pattern.matches( "<.+>", obj.getLabel() ) ){
			final String iri = obj.getLabel().substring(1, obj.getLabel().length()-1);
			prefValueArray[0] = "BASE";
			prefValueArray[1] = iri;
			return prefValueArray;

		}

		if ( Pattern.matches( "\".*\"\\^\\^.+:.+",  obj.getLabel() ) ){
			String value, prefix, type; value = prefix = type = "";
			for ( final MatchResult r : findMatches( "\".*\"", obj.getLabel() ) ) {
				value = r.group().substring(1, r.group().length()-1);
			}
			for ( final MatchResult r : findMatches( "<.+#", obj.getLabel() ) ) {
				prefix = r.group().substring(1, r.group().length());
			}
			for ( final MatchResult r : findMatches( "#.+>", obj.getLabel() ) ) {
				type = r.group().substring(1, r.group().length()-1);
			}

			for (final Entry<String, String> entry : this.po.getPrefixList().entrySet()) {
				if((prefix).equals(entry.getKey())){
					prefValueArray[0] = entry.getValue()+"#"+type;
					prefValueArray[1] = value;
					return prefValueArray;
				}
			}
		}

		if ( Pattern.matches( "\".+\"", obj.getLabel() ) ){
			String value  = "";
			for ( final MatchResult r : findMatches( "\".*\"", obj.getLabel() ) ) {
				value = r.group().substring(1, r.group().length()-1);
			}
			prefValueArray[0] = "xs#string";
			prefValueArray[1] = value;
			return prefValueArray;
		}

		if ( Pattern.matches( "\\d*", obj.getLabel()) ){
			// TODO check!
		}

		if ( Pattern.matches( "\".*\"\\@.+" , obj.getLabel() ) ){
			System.out.println("languagetaggedLiteralXSPattern");
		}

		// TODO Append ConstantCombo for single constants
		// save prefValueArray
		final String[] tmp = prefValueArray;
		final int cnt = prefValueArray.length + this.comboBoxEntries.length;
		prefValueArray = new String[cnt];

		for (int j = 0; j < tmp.length; j++) {
			prefValueArray[j] = tmp[j];
		}

		for (int i = 0; i < this.comboBoxEntries.length; i++) {
			prefValueArray[i+tmp.length] = tmp[i];
		}
		return prefValueArray;
	}

	public static Iterable<MatchResult> findMatches( final String pattern, final CharSequence s ) {
		final List<MatchResult> results = new ArrayList<MatchResult>();
		for ( final Matcher m = Pattern.compile(pattern).matcher(s); m.find(); ) {
			results.add( m.toMatchResult() );
		}
		return results;
	}

	/* *************** **
	 * Getter + Setter **
	 * *************** */
	public String getQuery() {
		return this.query;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	public CompilationUnit getCompilationUnit() {
		return this.compilationUnit;
	}

	public void setCompilationUnit(final CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public Document getRifDocument() {
		return this.rifDocument;
	}

	public void setRifDocument(final Document rifDocument) {
		this.rifDocument = rifDocument;
	}

	public CommonCoreQueryEvaluator<Node> getEvaluator() {
		return this.evaluator;
	}

	public DocumentEditorPane getDocumentEditorPane() {
		return this.documentEditorPane;
	}

	public void setDocumentEditorPane(final DocumentEditorPane documentEditorPane) {
		this.documentEditorPane = documentEditorPane;
	}

	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}
}