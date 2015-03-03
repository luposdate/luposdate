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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import lupos.datastructures.queryresult.QueryResult;
import lupos.event.util.TimedWrapper;

import org.jfree.chart.ChartPanel;

/**
 * Class for displaying input elements for chart generation
 * and displaying the chart itself.
 *
 * @author heidemey
 * @version $Id: $Id
 */
abstract public class ChartHandler extends JPanel {

	// Panel for displaying the chart
	protected ChartPanel chartPanel;
	protected DataModel model;

	/**
	 *Constructor using a GridBagLayout-Manager
	 *
	 * @param chartType a {@link lupos.event.consumer.app.charts.ChartTyp} object.
	 */
	public ChartHandler(final ChartTyp chartType){
		this(new GridBagLayout(), chartType);
	}

	/**
	 * Constructor
	 *
	 * @param mgr a {@link java.awt.LayoutManager} object.
	 * @param chartType a {@link lupos.event.consumer.app.charts.ChartTyp} object.
	 */
	public ChartHandler(final LayoutManager mgr, final ChartTyp chartType){
		super(mgr);
	}

	/**
	 * Clears all input fields of the chart handler
	 */
	abstract public void clearFields();

	/**
	 * Reads the QueryResult and fills the dataset according to
	 * chart type
	 *
	 * @param l a {@link lupos.event.util.TimedWrapper} object.
	 */
	abstract public void fillDataset(TimedWrapper<QueryResult> l);

	/**
	 * Delivers the JPanel displaying the chart. This needs to
	 * contain all input fields for this chart handler, too.
	 *
	 * @return a {@link javax.swing.JPanel} object.
	 */
	public JPanel getChart(){
		this.makeChart();
		return this;
	}

	/**
	 * <p>Getter for the field <code>model</code>.</p>
	 *
	 * @return a {@link lupos.event.consumer.app.charts.DataModel} object.
	 */
	protected DataModel getModel(){
		return this.model;
	}

	/**
	 * Creates the JPanel for displaying the chart. This also needs
	 * to prepare all input fields for display.
	 */
	protected void makeChart(){
		this.chartPanel.setChart(this.model.makeChart());
	}


	/**
	 * <p>createGBC.</p>
	 *
	 * @param gridx a int.
	 * @param gridy a int.
	 * @param fill a int.
	 * @return a {@link java.awt.GridBagConstraints} object.
	 */
	protected static GridBagConstraints createGBC(final int gridx, final int gridy, final int fill) {
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = gridx;
		c.gridy = gridy;
		c.fill= fill;
		return c;
	}


}
