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

import java.awt.Image;
import java.util.List;

import lupos.engine.evaluators.DebugContainerQuery;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperAST;
import lupos.gui.operatorgraph.viewer.ViewerPrefix;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.sparql1_1.Node;

public class SPARQLDebugViewerCreator extends DebugViewerCreator {

	private final DebugContainerQuery<BasicOperatorByteArray, Node> debugContainerQuery;

	public SPARQLDebugViewerCreator(final boolean fromJar, final ViewerPrefix viewerPrefix, final BooleanReference usePrefixes, final RulesGetter rulesGetter, final Image icon,
			final DebugContainerQuery<BasicOperatorByteArray, Node> debugContainerQuery) {
		super(fromJar, viewerPrefix, usePrefixes, rulesGetter, icon);
		this.debugContainerQuery = debugContainerQuery;
	}

	@Override
	public GraphWrapper getASTGraphWrapper() {
		return (this.debugContainerQuery == null) ? null : new GraphWrapperAST(this.debugContainerQuery.getAst());
	}
	
	public Node getAST(){
		return (this.debugContainerQuery == null) ? null : this.debugContainerQuery.getAst();
	}

	@Override
	public String queryOrRule() {
		return "SPARQL query";
	}

	@Override
	public GraphWrapper getASTCoreGraphWrapper() {
		return (this.debugContainerQuery == null) ? null : new GraphWrapperAST(this.debugContainerQuery.getAstCoreSPARQLQuery());
	}

	@Override
	public String getCore() {
		return (this.debugContainerQuery == null) ? null : this.debugContainerQuery.getCoreSPARQLQuery();
	}

	@Override
	public List<DebugContainer<BasicOperatorByteArray>> getCorrectOperatorGraphRules() {
		return (this.debugContainerQuery == null) ? null : this.debugContainerQuery.getCorrectOperatorGraphRules();
	}
}