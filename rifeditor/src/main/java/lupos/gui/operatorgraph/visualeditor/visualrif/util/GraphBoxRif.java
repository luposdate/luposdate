
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.operatorgraph.visualeditor.visualrif.util;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.FrameOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ListOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.UnitermOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
public class GraphBoxRif extends GraphBox {


	public static class RifGraphBoxCreator implements GraphBoxCreator {

		@Override
		public GraphBox createGraphBox(final OperatorGraph parent, final GraphWrapper op) {
			return new GraphBoxRif(parent, op);
		}

	}


	/**
	 * Constructor for the box.
	 *
	 * @param parent
	 *            QueryGraph where the box is in
	 * @param op
	 *            element to put in the box
	 */
	protected GraphBoxRif(final OperatorGraph parent, final GraphWrapper op) {
		super(parent, op);
	}

	/**
	 * <p>getSucceedingsElementsToDraw.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@Override
	public List<GraphWrapperIDTuple> getSucceedingsElementsToDraw(){
		final LinkedList<GraphWrapperIDTuple> list = new LinkedList<GraphWrapperIDTuple>(this.op.getSucceedingElements());

		if(this.op.getElement() instanceof AbstractTermOperator ) {
			final AbstractTermOperator ato = (AbstractTermOperator) this.op.getElement();

			for( final Term term : ato.getTerms()){

				if( (term.isList() || term.isUniterm()) ){

					GraphWrapper childGW = term.getSucceedingOperator();

					if(childGW==null){
						final Operator dummyOperator = term.getDummyOperator();

						if(dummyOperator==null){
							continue;
						}

						childGW = new GraphWrapperOperator(dummyOperator);

					}

					for(final GraphWrapperIDTuple gwidTuple: new LinkedList<GraphWrapperIDTuple>(list)){
						if(gwidTuple.getOperator().equals(childGW)){
							list.remove(gwidTuple);
						}
					}
				}
			}


		}

		return list;
	}

	/** {@inheritDoc} */
	@Override
	public void draw(final Graphics2D g) {

		if(this.op.getElement() instanceof AbstractTermOperator ) {
			final AbstractTermOperator ato = (AbstractTermOperator) this.op.getElement();

			for( final Term term : ato.getTerms()){

				if( (term.isList() || term.isUniterm()) ){

					GraphWrapper childGW = term.getSucceedingOperator();

					if(childGW==null){
						final Operator dummyOperator = term.getDummyOperator();

						if(dummyOperator==null){
							continue;
						}

						childGW = new GraphWrapperOperator(dummyOperator);
					}

					this.drawTermOperator(g, term, childGW,ato);

				}
			}


		}
		super.draw(g);
	}



	private void drawTermOperator(final Graphics2D g, final Term term, final GraphWrapper childGW, final AbstractTermOperator ato){
		final GraphBox childBox = this.parent.getBoxes().get(childGW);



		final Point startPoint = new Point((this.x + term.getConnectionButton().getX() + term.getConnectionButton().getWidth()),
				(this.y + term.getConnectionButton().getY() + (term.getConnectionButton().getHeight()/2)));;

				final Point endPoint = GraphBox.determineEdgePoint(childBox.getX(),
						childBox.getY(), childBox.width, childBox.height, this.x,
						this.y, this.width);

				g.setColor(GraphBox.lineColors[0]);

				drawTermConnection(g, startPoint.x, startPoint.y, endPoint.x, endPoint.y, true,ato);

				final JPanel annotationPanel = this.drawTermLineAnnotation(startPoint.x, startPoint.y,childGW, false, ato,term);

				if(annotationPanel != null){
				annotationPanel.invalidate();
				annotationPanel.repaint();
				}
				ato.getGUIComponent().invalidate();
				ato.getGUIComponent().repaint();

	}


	/**
	 * <p>drawTermLineAnnotation.</p>
	 *
	 * @param x a int.
	 * @param y a int.
	 * @param childGW a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param align a boolean.
	 * @param ato a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator} object.
	 * @param term a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Term} object.
	 * @return a {@link javax.swing.JPanel} object.
	 */
	protected synchronized JPanel drawTermLineAnnotation(final int x, final int y, final GraphWrapper childGW, final boolean align, final AbstractTermOperator ato, final Term term) {
		// get current child box...
		final GraphBox childBox = this.parent.getBoxes().get(childGW);

		if(childBox == null) {
			this.parent.remove(this.lineAnnotations.get(childGW));
			this.parent.getBoxes().remove(childGW);
			this.lineAnnotations.remove(childGW);

			return null;
		}




		// get panel for predicate to this arrow...
		final JPanel annotationPanel = this.lineAnnotations.get(childGW);

		if(annotationPanel == null) { // DummyOpetator...
			return annotationPanel;
		}

		int centerX = -1;
		int centerY = -1;

		if(childBox.equals(this)) { // connection to itself...
			centerX = this.x + (this.width / 2);
			centerY = this.y + this.height + 10 + (annotationPanel.getPreferredSize().height / 2);
		}
		else {
			// determine start point of the connection...
			final Point startPoint = new Point(x,y);
//			GraphBox.determineEdgePoint(this.x,
//					this.y, this.width, this.height, childBox.getX(), childBox.getY(),
//					childBox.width);

			final Dimension buttonDimension = new Dimension();
			buttonDimension.setSize(30d, 24d);
			final JIconButton deleteButton = new JIconButton("icons/001_02.png");
			deleteButton.setPreferredSize(buttonDimension);
			deleteButton.setMaximumSize(buttonDimension);
			deleteButton.setMinimumSize(buttonDimension);





			int distance = 0;

			if (ato instanceof UnitermOperator) {
				distance = 130;
//				final UnitermOperator uo = (UnitermOperator) ato;
//				final UnitermOperatorPanel uop = uo.getFactOperatorPanel();
//				deleteButton.addActionListener(new ActionListener(){
//					 public void actionPerformed(ActionEvent e) {
//
//						 uop.removeRow(term);
//						 uo.getTerms().remove(term);
//
//					 }});
//				annotationPanel.add(deleteButton);
			}
			if (ato instanceof FrameOperator){

				distance = 50;
			}
			if (ato instanceof ListOperator) {
				distance = 130;
//				final ListOperator lo = (ListOperator) ato;
//				final ListOperatorPanel lop = lo.getListOperatorPanel();
//				deleteButton.addActionListener(new ActionListener(){
//					 public void actionPerformed(ActionEvent e) {
//
//						 lop.removeRow(term);
//						lo.getTerms().remove(term);
//
//					 }});

//				annotationPanel.add(deleteButton);
			}


			// determine end point of the connection...
			final Point endPoint = new Point(x+distance,y);
			// determine center of the connection...
			centerX = (startPoint.x + endPoint.x) / 2;
			centerY = (startPoint.y + endPoint.y) / 2;
		}

		// position panel for predicate so, that center of predicate panel is
		// equal to center of the arrow...
		int x_ = centerX - (annotationPanel.getPreferredSize().width / 2);
		final int y_ = centerY - (annotationPanel.getPreferredSize().height / 2);

		if(align == true && x_ < this.parent.PADDING) {
			final int diff = Math.abs((int) this.parent.PADDING - x_);

			childBox.updateX(childBox.getX() + diff, y_, new HashSet<GraphBox>());

			this.x += diff;

			x_ += diff;
		}



		// connection in opposite direction does exist and was already drawn...
		if(this.op.getPrecedingElements().contains(childGW) && this.parent.annotationWasProcessed(childGW, this.op)) {
			x_ += this.lineAnnotations.get(childGW).getPreferredSize().width + 10;
		}

		annotationPanel.setBounds(x_, y_, annotationPanel.getPreferredSize().width, annotationPanel.getPreferredSize().height);

		this.parent.add(annotationPanel); // add predicate panel to the parent object...

		this.parent.addProcessedAnnotation(this.op, childGW);

		return annotationPanel;
	}


//	protected synchronized JPanel drawTermLineAnnotation(int x, int y, final GraphWrapper childGW, final boolean align) {
//		// get current child box...
//		final GraphBox childBox = this.parent.getBoxes().get(childGW);
//
//		if(childBox == null) {
//			this.parent.remove(this.lineAnnotations.get(childGW));
//			this.parent.getBoxes().remove(childGW);
//			this.lineAnnotations.remove(childGW);
//
//			return null;
//		}
//
//		// get panel for predicate to this arrow...
//		final JPanel annotationPanel = this.lineAnnotations.get(childGW);
//
//		if(annotationPanel == null) { // DummyOpetator...
//			return annotationPanel;
//		}
//
//		int centerX = -1;
//		int centerY = -1;
//
//		if(childBox.equals(this)) { // connection to itself...
//			centerX = this.x + (this.width / 2);
//			centerY = this.y + this.height + 10 + (annotationPanel.getPreferredSize().height / 2);
//		}
//		else {
//			// determine start point of the connection...
//			final Point startPoint = new Point(x,y);
////			GraphBox.determineEdgePoint(this.x,
////					this.y, this.width, this.height, childBox.getX(), childBox.getY(),
////					childBox.width);
//
//			// determine end point of the connection...
//			final Point endPoint = GraphBox.determineEdgePoint(childBox.getX(),
//					childBox.getY(), childBox.width, childBox.height, this.x,
//					this.y, this.width);
//
//			// determine center of the connection...
//			centerX = (startPoint.x + endPoint.x) / 2;
//			centerY = (startPoint.y + endPoint.y) / 2;
//		}
//
//		// position panel for predicate so, that center of predicate panel is
//		// equal to center of the arrow...
//		int x_ = centerX - (annotationPanel.getPreferredSize().width / 2);
//		final int y_ = centerY - (annotationPanel.getPreferredSize().height / 2);
//
//		if(align == true && x_ < this.parent.PADDING) {
//			final int diff = Math.abs((int) this.parent.PADDING - x_);
//
//			childBox.updateX(childBox.getX() + diff, y_, new HashSet<GraphBox>());
//
//			this.x += diff;
//
//			x_ += diff;
//		}
//
//		// connection in opposite direction does exist and was already drawn...
//		if(this.op.getPrecedingElements().contains(childGW) && this.parent.annotationWasProcessed(childGW, this.op)) {
//			x_ += this.lineAnnotations.get(childGW).getPreferredSize().width + 10;
//		}
//
//		annotationPanel.setBounds(x_, y_, annotationPanel.getPreferredSize().width, annotationPanel.getPreferredSize().height);
//
//		this.parent.add(annotationPanel); // add predicate panel to the parent object...
//
//		this.parent.addProcessedAnnotation(this.op, childGW);
//
//		return annotationPanel;
//	}


	/**
	 * Draws an arrow from one box to an other.
	 *
	 * @param g
	 *            Graphics2D object
	 * @param x
	 *            x coordinate of box
	 * @param xChild
	 *            x coordinate of child box
	 * @param y
	 *            y coordinate of box
	 * @param yChild
	 *            y coordinate of child box
	 * @param ato a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator} object.
	 * @param arrowHead a boolean.
	 */
	public static void drawTermConnection(final Graphics2D g, final int x, final int y, final int xChild, final int yChild, final boolean arrowHead, final AbstractTermOperator ato) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


		int width = 0;
		int height = 0;
		int opX = 0;
		int opY = 0;
		int distance = 0;

		if (ato instanceof UnitermOperator) {
			final UnitermOperator uo = (UnitermOperator) ato;
			final UnitermOperatorPanel uop = uo.getFactOperatorPanel();
			width = uop.getWidth();
			height = uop.getHeight();
			opX = uop.getX();
			opY = uop.getY();
			distance = 90;
		}
		if (ato instanceof FrameOperator){
			final FrameOperator fo = (FrameOperator) ato;
			final FrameOperatorPanel fop = fo.getFrameOperatorPanel();
			width = fop.getWidth();
			height = fop.getHeight();
			opX = fop.getX();
			opY = fop.getY();
			distance = 50;
		}
		if (ato instanceof ListOperator) {
			final ListOperator lo = (ListOperator) ato;
			final ListOperatorPanel lop = lo.getListOperatorPanel();
			width = lop.getWidth();
			height = lop.getHeight();
			opX = lop.getX();
			opY = lop.getY();
			distance = 90;
		}

		// case: child behind operator
		if ( opX < xChild && xChild < (opX+width) && opY < yChild && yChild < (opY+height) ){

			distance = 0;
		}else

		// case child under operator
		if ( xChild < (opX+width) && yChild > (opY+height)  ){

			g.drawLine(opX+width, y, x+distance, y); // draw normal line
			g.drawLine(x+distance, y, x+distance, (opY+height) +30); // draw normal line
			g.drawLine(x+distance,(opY+height) +30, xChild, yChild); // draw normal line
		}else

		// case child above operator
		if ( xChild < (opX+width) && yChild < opY ){

			g.drawLine(opX+width, y, x+distance, y); // draw normal line
			g.drawLine(x+distance, y, x+distance, opY -30); // draw normal line
			g.drawLine(x+distance, opY -30, xChild, yChild); // draw normal line
		}else

		if(  xChild < opX && opY < yChild && yChild < (opY+(height/2))) {
			g.drawLine(opX+width, y, x+distance, y); // draw normal line
			g.drawLine(x+distance, y, x+distance, opY -30); // draw normal line
			g.drawLine(x+distance, opY -30, opX -30,  opY -30); // draw normal line
			g.drawLine(opX -30,  opY -30, xChild, yChild); // draw normal line
		}else

		if(  xChild < opX && (opY+(height/2)) < yChild && yChild < (opY+height)) {
			g.drawLine(opX+width, y, x+distance, y); // draw normal line
			g.drawLine(x+distance, y, x+distance, (opY+height) +30); // draw normal line
			g.drawLine(x+distance,(opY+height) +30, opX -30,(opY+height) +30); // draw normal line
			g.drawLine(opX -30,(opY+height) +30, xChild, yChild); // draw normal line
		}

		else{
		// default
		g.drawLine(opX+width, y, x+distance, y); // draw normal line
		g.drawLine(x+distance, y, xChild, yChild); // draw normal line

		}

		if(arrowHead) {
			g.setStroke(new BasicStroke(1f)); // solid arrow head

			final int sideLength = (int) (8 * 1.0);
			final int sideStrength = (int) (5 * 1.0);
			final double aDir = Math.atan2(x - xChild, y - yChild);

			// create new polygon for arrow head...
			final Polygon tmpPoly = new Polygon();
			// add arrow tip as point...
			tmpPoly.addPoint(xChild, yChild);
			// add one edge as point...
			tmpPoly.addPoint(xChild + xCor(sideLength, aDir + 0.5), yChild + yCor(sideLength, aDir + 0.5));
			// add between the edges as point...
			tmpPoly.addPoint(xChild + xCor(sideStrength, aDir), yChild + yCor(sideStrength, aDir));
			// add other edge as point...
			tmpPoly.addPoint(xChild + xCor(sideLength, aDir - 0.5), yChild + yCor(sideLength, aDir - 0.5));
			// add arrow tip as point...
			tmpPoly.addPoint(xChild, yChild);

			g.drawPolygon(tmpPoly); // draw the arrow head
			g.fillPolygon(tmpPoly); // fill the arrow head
		}
	}



}
