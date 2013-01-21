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
package lupos.endpoint.client.formatreader;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.formatreader.csv.ASTOneResult;
import lupos.endpoint.client.formatreader.csv.ASTValue;
import lupos.endpoint.client.formatreader.csv.ASTVar;
import lupos.endpoint.client.formatreader.csv.ASTVars;
import lupos.endpoint.client.formatreader.csv.CSVParser;
import lupos.endpoint.client.formatreader.csv.Node;
import lupos.endpoint.client.formatreader.csv.ParseException;
import lupos.endpoint.client.formatreader.csv.SimpleNode;

public class CSVFormatReader extends MIMEFormatReader {
	
	public final static String MIMETYPE = "text/csv";

	public CSVFormatReader() {
		super("CSV", CSVFormatReader.MIMETYPE);
	}

	@Override
	public String getMIMEType() {
		return CSVFormatReader.MIMETYPE;
	}

	@Override
	public QueryResult getQueryResult(InputStream inputStream) {
		QueryResult result = QueryResult.createInstance();
		
		try {
			SimpleNode root = CSVParser.parse(inputStream);
			if(root == null){
				return null;
			}
			LinkedList<Variable> vars = new LinkedList<Variable>();
			for(int i=0; i<root.jjtGetNumChildren(); i++){
				Node child = root.jjtGetChild(i);
				if(child instanceof ASTVars){
					for(int j=0; j<child.jjtGetNumChildren(); j++){
						Node childchild = child.jjtGetChild(j);
						if(childchild instanceof ASTVar){
							vars.add(new Variable(((ASTVar)childchild).getName()));
						}						
					}					
				}
			}
			for(int i=0; i<root.jjtGetNumChildren(); i++){
				Node child = root.jjtGetChild(i);
				if(child instanceof ASTOneResult){
					Bindings bindings = Bindings.createNewInstance();
					Iterator<Variable> varIt = vars.iterator();
					for(int j=0; j<child.jjtGetNumChildren() && varIt.hasNext(); j++){
						Variable var = varIt.next();
						Node childchild = child.jjtGetChild(j);
						if(childchild instanceof ASTValue && childchild.jjtGetNumChildren()>0){
							bindings.add(var, CSVParser.getLiteral(childchild.jjtGetChild(0)));
						}
					}
					result.add(bindings);
				}
			}			
		} catch (ParseException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		return result;
	}

}
