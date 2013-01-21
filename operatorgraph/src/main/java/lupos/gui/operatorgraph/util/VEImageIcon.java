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
package lupos.gui.operatorgraph.util;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

public class VEImageIcon extends ImageIcon {
	private static final long serialVersionUID = 6170066219065271242L;

	public static ImageIcon getScaledIcon(final URL source, final int height) {
		return new ImageIcon(new ImageIcon(source).getImage()
				.getScaledInstance(height, -1, Image.SCALE_SMOOTH));
	}

	public static ImageIcon getScaledIcon(final String source, final int height) {
		return new ImageIcon(new ImageIcon(source).getImage()
				.getScaledInstance(height, -1, Image.SCALE_SMOOTH));
	}

	public static ImageIcon getMinusIcon(int height) {
		if (height % 2 == 1)
			height -= 1;
		if (height == 0)
			height = 2;
		final double step = height / 10;
		final BufferedImage dst = new BufferedImage(height, height,
				BufferedImage.TYPE_INT_ARGB_PRE);

		final Graphics2D g2 = dst.createGraphics();
		g2.setBackground(new Color(0, 0, 0, 255));
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 0, 0, 255), height,
				height, new Color(150, 0, 0, 255)));
		g2.fillOval(0, 0, height, height);
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 250, 250, 255),
				height, height, new Color(200, 180, 180, 255)));
		g2.fillOval((int) Math.round(step), (int) Math.round(step), (int) Math
				.round(height - 2 * step), (int) Math.round(height - 2 * step));
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 0, 0, 255), height,
				height, new Color(150, 0, 0, 255)));
		g2.fillOval((int) Math.round(2 * step), (int) Math.round(2 * step),
				(int) Math.round(height - 4 * step), (int) Math.round(height
						- 4 * step));
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 255),
				height, height, new Color(255, 255, 255, 255)));
		int heightMinus = (int) Math.round(2 * step);
		if (heightMinus == 0)
			heightMinus = 1;
		g2.fillRect((int) Math.round((double) height / 4), (int) Math
				.round(((double) height / 2) - step), (int) Math.round(height
				- ((double) height / 2)), heightMinus);
		return new ImageIcon(dst);
	}

	public static ImageIcon getPlusIcon(int height) {
		if (height % 2 == 1)
			height -= 1;
		if (height == 0)
			height = 2;
		final double step = height / 10;
		final BufferedImage dst = new BufferedImage(height, height,
				BufferedImage.TYPE_INT_ARGB_PRE);

		final Graphics2D g2 = dst.createGraphics();
		g2.setBackground(new Color(0, 0, 0, 255));
		g2.setPaint(new GradientPaint(0, 0, new Color(0, 255, 0, 255), height,
				height, new Color(0, 150, 0, 255)));
		g2.fillOval(0, 0, height, height);
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 250, 250, 255),
				height, height, new Color(180, 200, 180, 255)));
		g2.fillOval((int) Math.round(step), (int) Math.round(step), (int) Math
				.round(height - 2 * step), (int) Math.round(height - 2 * step));
		g2.setPaint(new GradientPaint(0, 0, new Color(0, 255, 0, 255), height,
				height, new Color(0, 150, 0, 255)));
		g2.fillOval((int) Math.round(2 * step), (int) Math.round(2 * step),
				(int) Math.round(height - 4 * step), (int) Math.round(height
						- 4 * step));
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 255),
				height, height, new Color(255, 255, 255, 255)));
		int heightPlus = (int) Math.round(2 * step);
		if (heightPlus == 0)
			heightPlus = 1;
		g2.fillRect(Math.round(height / 4), (int) Math
				.round(((double) height / 2) - step), Math.round(height
				- (height / 2)), heightPlus);
		g2.fillRect((int) Math.round(((double) height / 2) - step), (int) Math
				.round((double) height / 4), heightPlus, (int) Math
				.round(height - ((double) height / 2)));
		return new ImageIcon(dst);
	}

	public static ImageIcon getUncheckedIcon(int height) {
		if (height % 2 == 1)
			height -= 1;
		if (height == 0)
			height = 2;
		final double step = height / 10;
		final BufferedImage dst = new BufferedImage(height, height,
				BufferedImage.TYPE_INT_ARGB_PRE);

		final Graphics2D g2 = dst.createGraphics();
		drawCheckBox(g2, step, height);

		return new ImageIcon(dst);
	}

	public static ImageIcon getMouseOverUncheckedIcon(int height) {
		if (height % 2 == 1)
			height -= 1;
		if (height == 0)
			height = 2;
		final double step = height / 10;
		final BufferedImage dst = new BufferedImage(height, height,
				BufferedImage.TYPE_INT_ARGB_PRE);

		final Graphics2D g2 = dst.createGraphics();
		drawMouseOverCheckBox(g2, step, height);

		return new ImageIcon(dst);
	}

	public static ImageIcon getCheckedIcon(int height) {
		if (height % 2 == 1)
			height -= 1;
		if (height == 0)
			height = 2;
		final double step = height / 10;
		final BufferedImage dst = new BufferedImage(height, height,
				BufferedImage.TYPE_INT_ARGB_PRE);

		final Graphics2D g2 = dst.createGraphics();
		drawCheckBox(g2, step, height);
		drawCheck(g2, step, height);

		return new ImageIcon(dst);
	}

	public static ImageIcon getMouseOverCheckedIcon(int height) {
		if (height % 2 == 1)
			height -= 1;
		if (height == 0)
			height = 2;
		final double step = height / 10;
		final BufferedImage dst = new BufferedImage(height, height,
				BufferedImage.TYPE_INT_ARGB_PRE);

		final Graphics2D g2 = dst.createGraphics();
		drawMouseOverCheckBox(g2, step, height);
		drawCheck(g2, step, height);

		return new ImageIcon(dst);
	}

	private static void drawCheckBox(final Graphics2D g2, final double step,
			final int height) {
		g2.setBackground(new Color(0, 0, 0, 255));
		g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 255, 255), height,
				height, new Color(0, 0, 0, 255)));
		g2.fillRect(0, 0, height, height);
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 255),
				height, height, new Color(245, 245, 245, 155)));
		g2.fillRect((int) Math.round(step), (int) Math.round(step), (int) Math
				.round(height - 2 * step), (int) Math.round(height - 2 * step));
	}

	private static void drawMouseOverCheckBox(final Graphics2D g2,
			final double step, final int height) {
		g2.setBackground(new Color(0, 0, 0, 255));
		g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 255, 255), height,
				height, new Color(0, 0, 0, 255)));
		g2.fillRect(0, 0, height, height);
		g2.setPaint(new GradientPaint(0, 0, new Color(200, 200, 25, 255),
				height, height, new Color(60, 80, 60, 155)));
		g2.fillRect((int) Math.round(step), (int) Math.round(step), (int) Math
				.round(height - 2 * step), (int) Math.round(height - 2 * step));
		g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 255),
				height, height, new Color(245, 245, 245, 155)));
		g2.fillRect((int) Math.round(3 * step), (int) Math.round(3 * step),
				(int) Math.round(height - 6 * step), (int) Math.round(height
						- 6 * step));
	}

	private static void drawCheck(final Graphics2D g2, final double step,
			final int height) {
		g2.setPaint(new GradientPaint(0, 0, new Color(0, 200, 0, 255), height,
				height, new Color(0, 200, 0, 255)));
		g2.fillRect((int) Math.round(3 * step), (int) Math.round(3 * step),
				(int) Math.round(height - 6 * step), (int) Math.round(height
						- 6 * step));
	}

	public static ImageIcon getResizeIcon(int height) {
		if (height % 2 == 1)
			height -= 1;
		if (height == 0)
			height = 2;
		final double step = (double) height / 10;
		final BufferedImage dst = new BufferedImage(height, height,
				BufferedImage.TYPE_INT_ARGB_PRE);
		final Graphics2D g2 = dst.createGraphics();
		g2.setBackground(new Color(0, 0, 0, 255));
		g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 255), height,
				height, new Color(0, 0, 0, 255)));

		if (step < 1) {
			for (int y = 0; y < height / 2; y++) {
				for (int x = (height / 2) - 1 - y; x < 5; x++) {
					g2.fillRect(2 * x, 2 * y, 1, 1);
				}
			}
		} else {
			for (int y = 0; y < 5; y++) {
				for (int x = 4 - y; x < 5; x++) {
					g2.fillRect((int) Math.round((double) 2 * x * step),
							(int) Math.round((double) 2 * y * step),
							(int) step, (int) step);
				}
			}
		}
		return new ImageIcon(dst);
	}

	public static ImageIcon getPlayIcon(int height, final Color color) {
		if (height % 2 == 1)
			height -= 1;
		if (height == 0)
			height = 2;
		final int middle = height / 2;
		final BufferedImage dst = new BufferedImage(height, height,
				BufferedImage.TYPE_INT_ARGB_PRE);

		final Graphics2D g2 = dst.createGraphics();
		g2.setBackground(new Color(0, 0, 0, 255));
		g2.setColor(color);
		g2.fillPolygon(new int[] { 0, height, 0 }, new int[] { 0, middle,
				height }, 3);
		return new ImageIcon(dst);
	}

	public static ImageIcon getPauseIcon(int height, final Color color) {
		if (height % 2 == 1)
			height -= 1;
		if (height == 0)
			height = 2;
		final double step = height / 3;
		final BufferedImage dst = new BufferedImage(height, height,
				BufferedImage.TYPE_INT_ARGB_PRE);

		final Graphics2D g2 = dst.createGraphics();
		g2.setBackground(new Color(0, 0, 0, 255));
		g2.setColor(color);
		g2.fillPolygon(new int[] { 0, (int) step, (int) step, 0 }, new int[] {
				0, 0, height, height }, 4);
		g2.fillPolygon(new int[] { (int) (2 * step),
				(int) (2 * step) + (int) step, (int) (2 * step) + (int) step,
				(int) (2 * step) }, new int[] { 0, 0, height, height }, 4);
		return new ImageIcon(dst);
	}
}