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
package lupos.endpoint.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import xpref.XPref;

import com.sun.net.httpserver.HttpExchange;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.server.Endpoint.OutputStreamLogger;
import lupos.endpoint.server.Endpoint.SPARQLExecution;
import lupos.endpoint.server.Endpoint.SPARQLHandler;
import lupos.endpoint.server.format.Formatter;
import lupos.endpoint.server.format.OperatorgraphFormatter;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.gui.Demo_Applet;
import lupos.gui.GUI;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperator;
import lupos.gui.operatorgraph.viewer.Viewer;

/**
 * This class provides an endpoint for debugging purposes:
 * The operatorgraph can be returned (instead of the query result).
 * Furthermore, in the context /sparqldebug (instead of the normal /sparql context),
 * queries, their result, the sent message and the operator graph is stored in log files...  
 */
public class DebugEndpoint {

	private static int queryNumber = 0;
	// directory in which the logs are written
	public static String logDirectory = "d:/luposdate/log/";

	public static void main(String[] args) throws Exception {
		File file = new File(logDirectory);
		file.mkdirs();
		
		try {
			XPref.getInstance(Demo_Applet.class.getResource("/preferencesMenu.xml"));
		} catch(Exception e){
			XPref.getInstance(new URL("file:"+GUI.class.getResource("/preferencesMenu.xml").getFile()));
		}
		
		Endpoint.registerFormatter(new OperatorgraphFormatter("png"));
		Endpoint.registerFormatter(new OperatorgraphFormatter("jpeg"));
		Endpoint.registerFormatter(new OperatorgraphFormatter("gif"));
		Endpoint.registerHandler("/sparqldebug", new SPARQLHandler(new SPARQLExecutionDebugImplementation()));
		Endpoint.main(args);
	}
	
	public static class SPARQLExecutionDebugImplementation implements SPARQLExecution {
		@Override
		public void execute(final String queryParameter, final Formatter formatter, final HttpExchange t) throws IOException {
			try {
				synchronized(Endpoint.evaluator){ // avoid any inference of several queries in parallel!
					DebugEndpoint.queryNumber++;
					File file = new File(logDirectory+DebugEndpoint.queryNumber+".txt");
					FileWriter writer = new FileWriter(file);
					writer.write("Evaluating query of size "+queryParameter.length()+":\n"+queryParameter);					
					System.out.println("Evaluating query:\n"+queryParameter);
					if((Endpoint.evaluator instanceof CommonCoreQueryEvaluator) && formatter.isWriteQueryTriples()){
						// log query-triples by using BindingsArrayReadTriples as class for storing the query solutions!
						Bindings.instanceClass = BindingsArrayReadTriples.class;
					} else {
						Bindings.instanceClass = Endpoint.getDefaultBindingsClass();
					}
					
					QueryResult queryResult = (Endpoint.evaluator instanceof CommonCoreQueryEvaluator)?((CommonCoreQueryEvaluator)Endpoint.evaluator).getResult(queryParameter, false):Endpoint.evaluator.getResult(queryParameter);
					
					writer.write("\nResult (of size "+queryResult.size()+"):\n"+queryResult.toString());
					
					final String mimeType = formatter.getMIMEType(queryResult);
					System.out.println("Done, sending response using MIME type "+mimeType+"\n");
					writer.write("\nSending response using MIME type "+mimeType+"\n");
					t.getResponseHeaders().add("Content-type", mimeType);
					t.getResponseHeaders().add("Transfer-encoding", "chunked");
					t.sendResponseHeaders(200, 0);
					OutputStream os = t.getResponseBody();
					if(Endpoint.log){
						os = new OutputStreamLogger(os);
					}
					os = new PipeOutputStream(os, writer);
					formatter.writeResult(os, Endpoint.evaluator.getVariablesOfQuery(), queryResult);
					os.close();
					writer.close();
					Endpoint.evaluator.writeOutIndexFileAndModifiedPages(Endpoint.dir);
					new Viewer(new GraphWrapperBasicOperator(Endpoint.evaluator.getRootNode()), logDirectory+DebugEndpoint.queryNumber+".jpg");
				}
				return;
			} catch (Error e) {
				System.err.println(e);
				e.printStackTrace();
				t.getResponseHeaders().add("Content-type", "text/plain");
				final String answer = "Error:\n"+e.getMessage();
				System.out.println(answer);
				Endpoint.sendString(t, answer);
				return;
			} catch (Exception e){
				System.err.println(e);
				e.printStackTrace();
				t.getResponseHeaders().add("Content-type", "text/plain");
				final String answer = "Error:\n"+e.getMessage();
				System.out.println(answer);
				Endpoint.sendString(t, answer);
				return;
			}			
		}
	}
	
	public static class PipeOutputStream extends OutputStream {
		
		private final OutputStream piped;
		private final OutputStreamWriter logger;
		private int numberOfBytes = 0;
		
		public PipeOutputStream(final OutputStream piped, final OutputStreamWriter logger){
			this.piped = piped;
			this.logger = logger;
		}
		
		@Override
		public void write(int b) throws IOException {
			this.numberOfBytes++;
			this.logger.write(b);
			this.piped.write(b);
		}
		
		@Override
		public void close() throws IOException{
			this.logger.write("\n\nNumber of sent bytes:"+this.numberOfBytes);
			this.piped.close();
			this.logger.close();
		}
	}
}
