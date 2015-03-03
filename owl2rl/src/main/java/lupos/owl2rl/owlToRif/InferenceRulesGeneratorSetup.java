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
package lupos.owl2rl.owlToRif;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import lupos.owl2rl.parser.ParserResults;
import lupos.owl2rl.parser.TemplatesRuleParser;
import lupos.rif.BasicIndexRuleEvaluator;

public class InferenceRulesGeneratorSetup {


	/**Current Generator**/
	private static InferenceRulesGenerator gen;
	/** Whether Information should be printed or not **/
	private boolean printOn=true;

	/**Saves parsed rules for type of ruleset**/
	private final HashMap<String,ParserResults> parserResultsMap=new HashMap<String, ParserResults>();


	/**
	 * Init InferenceRulesGenerator with rule templates from file
	 * @param ontology
	 * @param file
	 */
	public void init(final String ontology, final String file){
		this.init(ontology, createInputStream(file), file);
	}

	/**
	 * Init InferenceRulesGenerator with rule templates from InputStream
	 * @param ontology
	 * @param rules
	 * @param key
	 */
	public  void init(final String ontology, final InputStream rules, final String key){
		// Reuse old parsed rule set if exists...

		//...if not, create new parser and save results
		if(!this.parserResultsMap.containsKey(key)){
			final TemplatesRuleParser parser= new TemplatesRuleParser();
			parser.start(rules);
			this.parserResultsMap.put(key, parser.getResults());
		}

		//Reuse old generator if exists...

		//..if not create new
		if(gen==null){
			gen=new InferenceRulesGenerator(this.printOn);

		}

		//set the results for the corresponding MethodType
		gen.setParserResults(this.parserResultsMap.get(key));
		gen.start(ontology);
	}

	/**Returns the Initialized Generator
	 *
	 * @return initialized Generator
	 */
	public InferenceRulesGenerator getGenerator() {
		return gen;
	}

	/**Set wether or not debug Information and emitted Rules should be printed on System.out
	 *
	 * @param printOn
	 */

	public void setPrintOnOrOff(final boolean printOn){
		this.printOn=printOn;
		if(gen!=null) {
			gen.setPrintOnOrOff(this.printOn);
		}
	}

	/**Create an InputStream from file Contents
	 *
	 * @param file
	 * @return generated InputStream
	 */
	private static InputStream createInputStream(final String file) {
		try {
			try {
				return InferenceRulesGeneratorSetup.class.getResource(file).openStream();
			} catch (final IOException e1) {
				return new FileInputStream(InferenceRulesGeneratorSetup.class.getResource(file).getFile());
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("File not found! Could not create  Input Stream in "
					+ "InferenceRulesGeneratorSetup" + " from file: \"" + file + "\"");
		}
		return null;
	}

	public void initWithEvaluator(final BasicIndexRuleEvaluator ruleEvaluator, final String rules, final String key) {
		// Reuse old parsed rule set if exists...

		//...if not, create new parser and save results
		if(!this.parserResultsMap.containsKey(key)){
			final TemplatesRuleParser parser= new TemplatesRuleParser();
			parser.start(createInputStream(rules));
			this.parserResultsMap.put(key, parser.getResults());
		}

		//Reuse old generator if exists...

		//..if not create new
		if(gen==null){
			gen=new InferenceRulesGenerator(this.printOn);
		}
		//set the results for the corresponding MethodType
		gen.setParserResults(this.parserResultsMap.get(key));
		gen.start(ruleEvaluator);
	}
}
