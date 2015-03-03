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
package lupos.event.consumer.app.charts;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JTextField;

import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.util.TimedWrapper;

import org.jfree.chart.ChartPanel;


/**
 * Class for displaying input elements for XY charts and display
 * of the chart itself.
 * @author heidemey
 *
 */
public class XYChart extends ChartHandler{

	protected JTextField value;
	protected JTextField category;
	protected JTextField value2;

	/**
	 * Constructor. Creates an appropriate DataModel and initializes all required GUI-Elements.
	 */
	public XYChart(final ChartTyp type)
	{
		super(new GridBagLayout(), type);

		super.add(new JLabel("Y Variable"),createGBC(0,0, GridBagConstraints.HORIZONTAL));
	    super.add(new JLabel("Z Variable"),createGBC(0,1, GridBagConstraints.HORIZONTAL));
	    super.add(new JLabel("X Variable"),createGBC(0,2, GridBagConstraints.HORIZONTAL));

	    this.value = new JTextField(25);
	    this.value.setEditable(true);
	    this.value.setMinimumSize(new Dimension(80,25));
	    super.add(this.value, createGBC(1,0, GridBagConstraints.HORIZONTAL));

	    this.value2 = new JTextField(25);
	    this.value2.setEditable(true);
	    this.value2.setMinimumSize(new Dimension(80,25));
	    super.add(this.value2, createGBC(1,1, GridBagConstraints.HORIZONTAL));

	    this.category = new JTextField(25);
	    this.category.setEditable(true);
	    this.category.setMinimumSize(new Dimension(80,25));
	    super.add(this.category, createGBC(1,2, GridBagConstraints.HORIZONTAL));

		this.chartPanel = new ChartPanel(null);
		this.add(this.chartPanel,new GridBagConstraints(0, 3, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 1, 1));

		this.model = new XYChartModel(type);
	}

	@Override
	public void fillDataset(final TimedWrapper<QueryResult> l)
	{

		this.getModel().setXVar(new Variable(this.category.getText()));
		this.getModel().setZ(new Variable(this.value2.getText()));
		this.getModel().setY(new Variable(this.value.getText()));

		this.getModel().fillDataset(l.getWrappedObject());
	}

	@Override
	protected XYChartModel getModel(){
		return (XYChartModel) super.getModel();
	}

	@Override
	public void clearFields(){

		this.value.setText(null);
		this.category.setText(null);
		this.value2.setText(null);

	}

}
