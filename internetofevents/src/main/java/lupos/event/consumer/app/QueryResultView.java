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
package lupos.event.consumer.app;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTable;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;

/**
 * Display the contents of a QueryResult in a table.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class QueryResultView extends JPanel {

	/**
	 * <p>Constructor for QueryResultView.</p>
	 *
	 * @param qr a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public QueryResultView(QueryResult qr) {
		Set<Variable> vars = qr.getVariableSet();

		Map<Variable, Integer> varColMap = new HashMap<Variable, Integer>();
		int i = 0;
		for(Variable v : vars)
			varColMap.put(v, i);
		
		
		Collection<Bindings> bindings = qr.getCollection();
		
		int rows = bindings.size();
		int cols = vars.size();
		
		String[][] rowData = new String[rows][cols];
		
		int currRow = 0;
		int currCol = 0;
		System.out.println("-----BINDINGS----");
		for(Bindings b : bindings) {
			System.out.println("-----new row----");
			for(Variable v : vars) {
				rowData[currRow][currCol] = b.get(v).originalString();
				System.out.println("v: "+v + " = " + b.get(v).originalString());
				++currCol;
			}
			currCol = 0;
			++currRow;
		}
		
		JTable table = new JTable(rowData, vars.toArray());
		
		super.setLayout(new BorderLayout());
		super.add(table.getTableHeader(), BorderLayout.NORTH);
		super.add(table, BorderLayout.CENTER);
	}
}
