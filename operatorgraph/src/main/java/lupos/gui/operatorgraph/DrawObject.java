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
package lupos.gui.operatorgraph;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.LinkedList;
import java.util.List;
public class DrawObject {
	/**
	 * <p>drawSimpleBoxOuterLines.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 */
	public static void drawSimpleBoxOuterLines(final Graphics2D g, final int x,
			final int y, final int width, final int height, final Color color1,
			final Color color2) {
		drawSimpleBox(g, x, y, width, height, color1);

		// draw the outer lines of the object...
		g.setColor(color2);
		g.drawRect(x, y, width, height);
	}

	/**
	 * <p>drawSimpleBoxShade.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param shadewidth a int.
	 */
	public static void drawSimpleBoxShade(final Graphics2D g, final int x,
			final int y, final int width, final int height, final Color color1,
			final Color color2, final int shadewidth) {
		// draw the shade...
		drawSimpleBox(g, x + shadewidth, y + shadewidth, width, height, color2);

		// draw the box itself
		drawSimpleBox(g, x, y, width, height, color1);
	}

	/**
	 * <p>drawSimpleBox.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 */
	public static void drawSimpleBox(final Graphics2D g, final int x,
			final int y, final int width, final int height, final Color color1) {
		// fill the object...
		g.setColor(color1);
		g.fillRect(x, y, width, height);
	}

	/**
	 * <p>drawGradientPaintBoxOuterLines.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param color3 a {@link java.awt.Color} object.
	 */
	public static void drawGradientPaintBoxOuterLines(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2, final Color color3) {
		drawGradientPaintBox(g, x, y, width, height, color1, color2);

		// draw the outer lines of the object...
		g.setColor(color3);
		g.drawRect(x, y, width, height);
	}

	/**
	 * <p>drawGradientPaintBoxShade.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param color3 a {@link java.awt.Color} object.
	 * @param shadewidth a int.
	 */
	public static void drawGradientPaintBoxShade(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2, final Color color3,
			final int shadewidth) {
		// draw the shade
		drawSimpleBox(g, x + shadewidth, y + shadewidth, width, height, color3);

		// draw the box itself
		drawGradientPaintBox(g, x, y, width, height, color1, color2);
	}

	/**
	 * <p>drawGradientPaintBox.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 */
	public static void drawGradientPaintBox(final Graphics2D g, final int x,
			final int y, final int width, final int height, final Color color1,
			final Color color2) {
		// fill the object...
		g
				.setPaint(new GradientPaint(x, y, color1, x + width, y + width,
						color2));
		g.fillRect(x, y, width, height);
	}

	/**
	 * <p>drawSimpleRoundBoxOuterLines.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 */
	public static void drawSimpleRoundBoxOuterLines(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2) {
		drawSimpleRoundBox(g, x, y, width, height, color1);

		// draw the outer lines of the object...
		g.setColor(color2);
		g.drawRoundRect(x, y, width, height, 15, 15);
	}

	/**
	 * <p>drawSimpleRoundBoxShade.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param shadewidth a int.
	 */
	public static void drawSimpleRoundBoxShade(final Graphics2D g, final int x,
			final int y, final int width, final int height, final Color color1,
			final Color color2, final int shadewidth) {
		// draw the shade...
		drawSimpleRoundBox(g, x + shadewidth, y + shadewidth, width, height,
				color2);

		// draw the box itself
		drawSimpleRoundBox(g, x, y, width, height, color1);
	}

	/**
	 * <p>drawSimpleRoundBox.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 */
	public static void drawSimpleRoundBox(final Graphics2D g, final int x,
			final int y, final int width, final int height, final Color color1) {
		// fill the object...
		g.setColor(color1);
		g.fillRoundRect(x, y, width, height, 15, 15);
	}

	/**
	 * <p>drawGradientPaintRoundBoxOuterLines.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param color3 a {@link java.awt.Color} object.
	 */
	public static void drawGradientPaintRoundBoxOuterLines(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2, final Color color3) {
		drawGradientPaintRoundBox(g, x, y, width, height, color1, color2);

		// draw the outer lines of the object...
		g.setColor(color3);
		g.drawRoundRect(x, y, width, height, 15, 15);
	}

	/**
	 * <p>drawGradientPaintRoundBoxShade.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param color3 a {@link java.awt.Color} object.
	 * @param shadewidth a int.
	 */
	public static void drawGradientPaintRoundBoxShade(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2, final Color color3,
			final int shadewidth) {
		// draw the shade
		drawSimpleRoundBox(g, x + shadewidth, y + shadewidth, width, height,
				color3);

		// draw the box itself
		drawGradientPaintRoundBox(g, x, y, width, height, color1, color2);
	}

	/**
	 * <p>drawGradientPaintRoundBox.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 */
	public static void drawGradientPaintRoundBox(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2) {
		// fill the object...
		g
				.setPaint(new GradientPaint(x, y, color1, x + width, y + width,
						color2));
		g.fillRoundRect(x, y, width, height, 15, 15);
	}

	private static Polygon getPolygonOfOctagon(final int x, final int y,
			final int width, final int height) {
		int xstep = 5;
		if (2 * xstep > width)
			xstep = width / 2;
		int ystep = height / 3;
		if (2 * ystep > height)
			ystep = height / 2;
		final Polygon p = new Polygon(new int[] { x + xstep, x + width - xstep,
				x + width, x + width, x + width - xstep, x + xstep, x, x },
				new int[] { 0, 0, y + ystep, y + height - ystep, y + height,
						y + height, y + height - ystep, y + ystep }, 8);
		return p;
	}

	/**
	 * <p>drawGradientPaintOctagonOuterLines.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param color3 a {@link java.awt.Color} object.
	 */
	public static void drawGradientPaintOctagonOuterLines(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2, final Color color3) {
		final Polygon p = getPolygonOfOctagon(x, y, width, height);
		g
				.setPaint(new GradientPaint(x, y, color1, x + width, y + width,
						color2));
		g.fillPolygon(p);
		// draw the outer lines of the object...
		g.setColor(color3);
		g.drawPolygon(p);
	}

	/**
	 * <p>drawGradientPaintOctagonShade.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param color3 a {@link java.awt.Color} object.
	 * @param shadewidth a int.
	 */
	public static void drawGradientPaintOctagonShade(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2, final Color color3,
			final int shadewidth) {
		// draw the shade
		drawGradientPaintOctagon(g, x + shadewidth, y + shadewidth, width,
				height, color3, color3);

		// draw the box itself
		drawGradientPaintOctagon(g, x, y, width, height, color1, color2);
	}

	/**
	 * <p>drawGradientPaintOctagon.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 */
	public static void drawGradientPaintOctagon(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2) {
		final Polygon p = getPolygonOfOctagon(x, y, width, height);
		// fill the object...
		g
				.setPaint(new GradientPaint(x, y, color1, x + width, y + width,
						color2));
		g.fillPolygon(p);
	}

	private static Polygon getPolygonOfHexagon(final int x, final int y,
			final int width, final int height) {
		int xstep = 5;
		if (2 * xstep > width)
			xstep = width / 2;
		final int ystep = height / 2;
		final Polygon p = new Polygon(new int[] { x + xstep, x + width - xstep,
				x + width, x + width, x + width - xstep, x + xstep, x, x },
				new int[] { 0, 0, y + ystep, y + height - ystep, y + height,
						y + height, y + height - ystep, y + ystep }, 8);
		return p;
	}

	/**
	 * <p>drawGradientPaintHexagonOuterLines.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param color3 a {@link java.awt.Color} object.
	 */
	public static void drawGradientPaintHexagonOuterLines(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2, final Color color3) {
		final Polygon p = getPolygonOfHexagon(x, y, width, height);
		g
				.setPaint(new GradientPaint(x, y, color1, x + width, y + width,
						color2));
		g.fillPolygon(p);
		// draw the outer lines of the object...
		g.setColor(color3);
		g.drawPolygon(p);
	}

	/**
	 * <p>drawGradientPaintHexagonShade.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 * @param color3 a {@link java.awt.Color} object.
	 * @param shadewidth a int.
	 */
	public static void drawGradientPaintHexagonShade(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2, final Color color3,
			final int shadewidth) {
		// draw the shade
		drawGradientPaintHexagon(g, x + shadewidth, y + shadewidth, width,
				height, color3, color3);

		// draw the box itself
		drawGradientPaintHexagon(g, x, y, width, height, color1, color2);
	}

	/**
	 * <p>drawGradientPaintHexagon.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 * @param color1 a {@link java.awt.Color} object.
	 * @param color2 a {@link java.awt.Color} object.
	 */
	public static void drawGradientPaintHexagon(final Graphics2D g,
			final int x, final int y, final int width, final int height,
			final Color color1, final Color color2) {
		final Polygon p = getPolygonOfHexagon(x, y, width, height);
		// fill the object...
		g
				.setPaint(new GradientPaint(x, y, color1, x + width, y + width,
						color2));
		g.fillPolygon(p);
	}

	public static enum Type {
		SIMPLEBOX, ROUNDBOX, OUTERLINEDSIMPLEBOX, OUTERLINEDROUNDBOX, OCTAGON, HEXAGON, OUTERLINEDOCTAGON, OUTERLINEDHEXAGON
	};

	public static enum OuterAttribute {
		NONE, OUTERLINED, SHADED
	};

	public static enum InnerAttribute {
		NONE, GRADIENTPAINT
	};

	private final Type type;
	private final OuterAttribute outerAttribute;
	private final InnerAttribute innerAttribute;
	private final List<Color> colorList;

	/**
	 * <p>Constructor for DrawObject.</p>
	 *
	 * @param type a {@link lupos.gui.operatorgraph.DrawObject.Type} object.
	 * @param innerAttribute a {@link lupos.gui.operatorgraph.DrawObject.InnerAttribute} object.
	 * @param outerAttribute a {@link lupos.gui.operatorgraph.DrawObject.OuterAttribute} object.
	 * @param colorlist a {@link java.awt.Color} object.
	 */
	public DrawObject(final Type type, final InnerAttribute innerAttribute,
			final OuterAttribute outerAttribute, final Color... colorlist) {
		this.type = type;
		this.outerAttribute = outerAttribute;
		this.innerAttribute = innerAttribute;
		this.colorList = new LinkedList<Color>();

		for (final Color color : colorlist) {
			this.colorList.add(color);
		}

		int numberOfColors = 1;

		if (outerAttribute == OuterAttribute.OUTERLINED
				|| outerAttribute == OuterAttribute.SHADED) {
			numberOfColors++;
		}

		if (innerAttribute == InnerAttribute.GRADIENTPAINT) {
			numberOfColors++;
		}

		if (this.colorList.size() != numberOfColors) {
			System.err
					.println("DrawObject with wrong number of colors initialized: Expected "
							+ numberOfColors
							+ ", but found "
							+ this.colorList.size() + " colors!");
		}
	}

	/**
	 * <p>draw.</p>
	 *
	 * @param g a {@link java.awt.Graphics2D} object.
	 * @param x a int.
	 * @param y a int.
	 * @param width a int.
	 * @param height a int.
	 */
	public void draw(final Graphics2D g, final int x, final int y,
			final int width, final int height) {
		switch (this.type) {
		case HEXAGON:
			switch (this.outerAttribute) {
			case NONE:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintHexagon(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(0));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintHexagon(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1));
					break;
				}
				break;
			case OUTERLINED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintHexagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(1));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintHexagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2));
					break;
				}
				break;
			case SHADED:
				switch (innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintHexagonShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(1), 5);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintHexagonShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2), 5);
					break;
				}
				break;
			}
			break;
		case OCTAGON:
			switch (this.outerAttribute) {
			case NONE:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintOctagon(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(0));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintOctagon(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1));
					break;
				}
				break;
			case OUTERLINED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintOctagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(1));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintOctagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2));
					break;
				}
				break;
			case SHADED:
				switch (innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintOctagonShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(1), 5);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintOctagonShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2), 5);
					break;
				}
				break;
			}
			break;
		case SIMPLEBOX:
			switch (this.outerAttribute) {
			case NONE:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleBox(g, x, y, width - 1, height - 1,
							this.colorList.get(0));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintBox(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1));
					break;
				}
				break;
			case OUTERLINED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleBoxOuterLines(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintBoxOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2));
					break;
				}
				break;
			case SHADED:
				switch (innerAttribute) {
				case NONE:
					DrawObject.drawSimpleBoxShade(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1), 5);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintBoxShade(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1), this.colorList.get(2), 5);
					break;
				}
				break;
			}
			break;
		case ROUNDBOX:
			switch (this.outerAttribute) {
			case NONE:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleRoundBox(g, x, y, width - 1,
							height - 1, this.colorList.get(0));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintRoundBox(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1));
					break;
				}
				break;
			case OUTERLINED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleRoundBoxOuterLines(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintRoundBoxOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2));
					break;
				}
				break;
			case SHADED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleRoundBoxShade(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1), 5);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintRoundBoxShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2), 5);
					break;
				}
				break;
			}
			break;
		case OUTERLINEDSIMPLEBOX:
			switch (this.outerAttribute) {
			case NONE:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleBoxOuterLines(g, x, y, width - 1,
							height - 1, this.colorList.get(0), Color.BLACK);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintBoxOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), Color.BLACK);
					break;
				}
				break;
			case OUTERLINED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleBoxOuterLines(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintBoxOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2));
					break;
				}
				break;
			case SHADED:
				switch (innerAttribute) {
				case NONE:
					DrawObject.drawSimpleBoxShade(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1), 5);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintBoxShade(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1), this.colorList.get(2), 5);
					break;
				}
				break;
			}
			break;
		case OUTERLINEDROUNDBOX:
			switch (this.outerAttribute) {
			case NONE:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleRoundBoxOuterLines(g, x, y, width - 1,
							height - 1, this.colorList.get(0), Color.BLACK);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintRoundBoxOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), Color.BLACK);
					break;
				}
				break;
			case OUTERLINED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleRoundBoxOuterLines(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintRoundBoxOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2));
					break;
				}
				break;
			case SHADED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawSimpleRoundBoxShade(g, x, y, width - 1,
							height - 1, this.colorList.get(0), this.colorList
									.get(1), 5);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintRoundBoxShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2), 5);
					break;
				}
				break;
			}
			break;
		case OUTERLINEDHEXAGON:
			switch (this.outerAttribute) {
			case NONE:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintHexagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(0), Color.BLACK);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintHexagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), Color.BLACK);
					break;
				}
				break;
			case OUTERLINED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintHexagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(0), this.colorList.get(1));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintHexagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2));
					break;
				}
				break;
			case SHADED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintHexagonShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(0), this.colorList.get(1), 5);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintHexagonShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2), 5);
					break;
				}
				break;
			}
			break;
		case OUTERLINEDOCTAGON:
			switch (this.outerAttribute) {
			case NONE:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintOctagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(0), Color.BLACK);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintOctagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), Color.BLACK);
					break;
				}
				break;
			case OUTERLINED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintOctagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(0), this.colorList.get(1));
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintOctagonOuterLines(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2));
					break;
				}
				break;
			case SHADED:
				switch (this.innerAttribute) {
				case NONE:
					DrawObject.drawGradientPaintOctagonShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(0), this.colorList.get(1), 5);
					break;
				case GRADIENTPAINT:
					DrawObject.drawGradientPaintOctagonShade(g, x, y,
							width - 1, height - 1, this.colorList.get(0),
							this.colorList.get(1), this.colorList.get(2), 5);
					break;
				}
				break;
			}
			break;
		}
	}
}
