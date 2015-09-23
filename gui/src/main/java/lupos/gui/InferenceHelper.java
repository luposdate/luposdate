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
package lupos.gui;

import lupos.owl2rl.owlToRif.InferenceRulesGeneratorSetup;
public class InferenceHelper {

	/** Constant <code>genSet</code> */
	protected static InferenceRulesGeneratorSetup genSet;

	/**
	 * <p>getRIFInferenceRulesForRDFSOntology.</p>
	 *
	 * @param ontology a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getRIFInferenceRulesForRDFSOntology(final String ontology){
		return returnValue(ontology, "/owl2rl/RDFSRules.xml");
	}

	/**
	 * <p>getRIFInferenceRulesForRDFSOntologyAlternative.</p>
	 *
	 * @param ontology a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getRIFInferenceRulesForRDFSOntologyAlternative(final String ontology){
		return returnValue(ontology, "/owl2rl/RDFSRulesAlternative.xml");
	}

	/**
	 * <p>getRIFInferenceRulesForOWL2Ontology.</p>
	 *
	 * @param ontology a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getRIFInferenceRulesForOWL2Ontology(final String ontology){
		return returnValue(ontology, "/owl2rl/OWL2RLRules.xml");
	}

	/**
	 * <p>getRIFInferenceRulesForOWL2OntologyAlternative.</p>
	 *
	 * @param ontology a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getRIFInferenceRulesForOWL2OntologyAlternative(final String ontology){
		return returnValue(ontology, "/owl2rl/OWL2RLRulesAlternative.xml");
	}

	/**
	 * <p>getRIFInferenceRulesForOWL2OntologyWithoutCheckingInconsistencies.</p>
	 *
	 * @param ontology a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getRIFInferenceRulesForOWL2OntologyWithoutCheckingInconsistencies(final String ontology){
		return returnValue(ontology, "/owl2rl/OWL2RLRulesNoInconsistencyRules.xml");
	}

	/**
	 * <p>getRIFInferenceRulesForOWL2OntologyAlternativeWithoutCheckingInconsistencies.</p>
	 *
	 * @param ontology a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getRIFInferenceRulesForOWL2OntologyAlternativeWithoutCheckingInconsistencies(final String ontology){
		return returnValue(ontology, "/owl2rl/OWL2RLRulesAlternativeNoInconsistencyRules.xml");
	}

	/**
	 * <p>returnValue.</p>
	 *
	 * @param ontology a {@link java.lang.String} object.
	 * @param filepath a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String returnValue(final String ontology, final String filepath){
		if(genSet==null){
			genSet=new InferenceRulesGeneratorSetup();
		}
		genSet.init(ontology, filepath);
		return genSet.getGenerator().getOutputRules();		
	}
}
