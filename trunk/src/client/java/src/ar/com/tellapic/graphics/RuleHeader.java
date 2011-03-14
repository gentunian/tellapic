/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.GeneralPath;

import javax.swing.JComponent;

public class RuleHeader extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int INCH = Toolkit.getDefaultToolkit().getScreenResolution();
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	public static final int SIZE = 24;

	public int orientation;
	public boolean isMetric;
	private int increment;
	private int units;
	private int x;
	private int y;
	private GeneralPath mark;
	
	
	public RuleHeader(int o, boolean m) {
		orientation = o;
		isMetric = m;
		setIncrementAndUnits();
		x = 0;
		y = 0;
	}

	public void setIsMetric(boolean isMetric) {
		this.isMetric = isMetric;
		setIncrementAndUnits();
		repaint();
	}

	private void setIncrementAndUnits() {
		if (isMetric) {
			units = (int)((double)INCH / (double)2.546); // dots per centimeter
			increment = units;
		} else {
			units = INCH;
			increment = units / 2;
		}
	}

	public boolean isMetric() {
		return this.isMetric;
	}

	public int getIncrement() {
		return increment;
	}

	public void setPreferredHeight(int ph) {
		setPreferredSize(new Dimension(SIZE, ph));
	}

	public void setPreferredWidth(int pw) {
		setPreferredSize(new Dimension(pw, SIZE));
	}

	protected void paintComponent(Graphics g2) {
		java.awt.Rectangle drawHere = g2.getClipBounds();
		
		// Fill clipping area with dirty brown/orange.
		g2.setColor(Color.gray);
		g2.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);
		
		// Do the ruler labels in a small font that's black.
		g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
		g2.setColor(Color.black);

		// Some vars we need.
		int end = 0;
		int start = 0;
		int tickLength = 0;
		String text = null;

		// Use clipping bounds to calculate first and last tick locations.
		if (orientation == HORIZONTAL) {
			start = (drawHere.x / increment) * increment;
			end = (((drawHere.x + drawHere.width) / increment) + 1) * increment;
		} else {
			start = (drawHere.y / increment) * increment;
			end = (((drawHere.y + drawHere.height) / increment) + 1) * increment;
		}

		// Make a special case of 0 to display the number
		// within the rule and draw a units label.
		if (start == 0) {
			text = Integer.toString(0) + (isMetric ? " cm" : " in");
			tickLength = (int) (SIZE * 0.2);
			if (orientation == HORIZONTAL) {
				g2.drawLine(0, SIZE-1, 0, SIZE-tickLength-1);
				g2.drawString(text, 2, 10);
			} else {
				g2.drawLine(SIZE-1, 0, SIZE-tickLength-1, 0);
				g2.drawString(text, 1, 10);
			}
			text = null;
			start = increment;
		}

		// ticks and labels
		for (int i = start; i < end; i += increment) {
			if (i % units == 0)  {
				tickLength = (int) (SIZE * 0.2);
				text = Integer.toString(i/units);
			} else {
				tickLength = (int) (SIZE * 0.2);
				text = null;
			}

			if (tickLength != 0) {
				if (orientation == HORIZONTAL) {
					g2.drawLine(i, SIZE-1, i, SIZE-tickLength-1);
					if (text != null)
						g2.drawString(text, i-3, 10);
				} else {
					g2.drawLine(SIZE-1, i, SIZE-tickLength-1, i);
					if (text != null)
						g2.drawString(text, 1, i+3);
				}
			}
		}
		
		//g.drawImage(headerBackground, 0, 0, null);
	}
	
	
	public void paint(Graphics g) {
		paintComponent(g);
		
		mark = new GeneralPath();
		if (orientation == HORIZONTAL) {
			mark.moveTo(x,SIZE);
			mark.lineTo(x-5, SIZE-5);
			mark.lineTo(x+5,SIZE-5);
			mark.lineTo(x,SIZE);
			mark.closePath();
		} else {
			mark.moveTo(SIZE, y);
			mark.lineTo(SIZE-5, y-5);
			mark.lineTo(SIZE-5, y+5);
			mark.lineTo(SIZE, y);
			mark.closePath();
		}
		((Graphics2D)g).fill(mark);
	}
	
	
	public void update(int x, int y) {
		this.x = x;
		this.y = y;
		repaint();
	}
}
