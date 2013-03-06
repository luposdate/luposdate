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
package lupos.datastructures.sort.helper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.misc.FileHelper;

/**
 * Helper class to read in and parse different types of data (strings or RDF terms of RDF data). 
 */
public class DataToBoundedBuffer {
	/**
	 * Read in and parse strings in a file (each line in the file is one string)
	 * @param dataFiles the data
	 * @param buffer the bounded buffer, in which the data is put
	 * @throws IOException in case of any failure
	 * @throws InterruptedException in case of any failure
	 */
	private static void stringsInFile(final InputStream dataFiles, final BoundedBuffer<String> buffer) throws IOException, InterruptedException{
		// read in the strings...
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(dataFiles)));
		String strLine;
		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
			buffer.put(strLine);
		}
		//Close the input stream
		br.close();
	}
	
	/**
	 * Read in and parse RDF terms of triples in an RDF file
	 * @param dataFiles the data
	 * @param format the format, e.g. N3, NQUADS, MULTIPLEN3, ...
	 * @param buffer the bounded buffer, in which the data is put
	 * @throws Exception in case of any failure
	 */
	private static void rdfTerms(final InputStream dataFiles, final String format, final BoundedBuffer<String> buffer) throws Exception{
		// now parse the data...
		CommonCoreQueryEvaluator.readTriples(format, dataFiles, 
				new TripleConsumer(){
					@Override
					public void consume(Triple triple) {
						for(Literal literal: triple){
							try {
								buffer.put(literal.originalString());
							} catch (InterruptedException e) {
								System.err.println(e);
								e.printStackTrace();
							}
						}
					}
		});
	}
	
	/**
	 * Read in and parse strings or RDF terms of RDF data, and put one after the other in the given bounded buffer.
	 * @param dataFiles the data
	 * @param format the format (STRING => strings in a file, each line one string, MULTIPLESTRING => file contains file list, all are read in line by line, otherwise RDF format)
	 * @param buffer the bounded buffer, in which the data is put
	 * @throws Exception in case of any failure
	 */
	public static void dataToBoundedBuffer(final InputStream dataFiles, final String format, final BoundedBuffer<String> buffer) throws Exception {
		if(format.toUpperCase().compareTo("STRING")==0){
			DataToBoundedBuffer.stringsInFile(dataFiles, buffer);
		} else if(format.toUpperCase().compareTo("MULTIPLESTRING")==0) {
			for (final String filename : FileHelper.readInputStreamToCollection(dataFiles)) {
				System.out.println("Reading data from file: " + filename);
				DataToBoundedBuffer.stringsInFile(new BufferedInputStream(new FileInputStream(filename)), buffer);
			}
		} else {
			DataToBoundedBuffer.rdfTerms(dataFiles, format, buffer);
		}		
	}
}