/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.geonames;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

public class CountryCanvas extends JComponent {

	private static final int RATIO = 50000;
	private static final int RATIO_ = -RATIO;

	private static final int BORDER_VERTICES = 8;

	private int[] main8;
	private int[] centre;
	private Polygon poly;
	private Polygon border;
	private int tX, tY;
	private int[] specific;

	public CountryCanvas(CountryRegion region) {
		super();

		setPreferredSize(new Dimension(2500, 1500));

		centre = findCentre(region);
		centre[0] /= RATIO;
		centre[1] /= -RATIO;
		main8 = region.findMainVertices(BORDER_VERTICES);

		poly = new Polygon();
		for (int i = 0; i < region.npoints; i++) {
			poly.addPoint(region.xpoints[i] / RATIO, region.ypoints[i] / RATIO_);
		}
		border = new Polygon();
		for (int i : main8) {
			if (i >= 0)
				border.addPoint(region.xpoints[i] / RATIO, region.ypoints[i] / RATIO_);
		}
		tX = 0;
		tY = 0;
		if ("AF".equals(region.getCountryCode())) {
			tX = -1100;
			tY = 900;
		} else if ("BW".equals(region.getCountryCode())) {
			tX = -250;
			tY = -300;
			// Dikholola near Brits.
			specific = new int[] { 27746222, -25411172 };
		} else if ("IL".equals(region.getCountryCode())) {
			tX = -450;
			tY = 850;
		} else if ("US".equals(region.getCountryCode())) {
			tX = 3400;
			tY = 1500;
		} else if ("ZA".equals(region.getCountryCode())) {
			tX = -250;
			tY = -300;
			// Dikholola near Brits.
			specific = new int[] { 27746222, -25411172 };
		}
	}

	public void paint(Graphics g) {
		g.translate(tX, tY);

		g.setColor(Color.DARK_GRAY);
		if (g instanceof Graphics2D) {
			((Graphics2D) g).draw(poly);
		} else {
			g.drawPolygon(poly);
		}

		Rectangle rect = poly.getBounds();
		g.setColor(Color.YELLOW);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);

		int cx = centre[0];
		int cy = centre[1];
		g.setColor(Color.RED);
		g.drawOval(cx - 2, cy - 2, 5, 5);

		final double sweepAngle = (2f * Math.PI) / BORDER_VERTICES;
		double angleStart = -(sweepAngle / 2f);
		double angleEnd;
		int x2, y2;
		int r = 2500;

		for (int v = 0; v < BORDER_VERTICES; v++) {
			x2 = cx + (int) (r * Math.cos(angleStart));
			y2 = cy + (int) (r * Math.sin(angleStart));
			g.setColor(Color.CYAN);
			g.drawLine(cx, cy, x2, y2);

			angleEnd = angleStart + sweepAngle;
			x2 = cx + (int) (r * Math.cos(angleEnd));
			y2 = cy + (int) (r * Math.sin(angleEnd));
			g.setColor(Color.MAGENTA);
			g.drawLine(cx, cy, x2, y2);

			angleStart += sweepAngle;
		}

		g.setColor(Color.BLUE);
		for (int i : main8) {
			if (i >= 0)
				g.drawOval(poly.xpoints[i] - 2, poly.ypoints[i] - 2, 5, 5);
		}
		g.setColor(Color.ORANGE);
		g.drawPolygon(border);

		if (specific != null) {
			g.setColor(Color.RED);
			g.drawOval((specific[0] / RATIO) - 5, (specific[1] / RATIO_) - 5, 10, 10);
		}
	}

	/**
	 * Find the centre of gravity.
	 * 
	 * @return the middle-x at index {@code 0}, the middle-y at index {@code 1}.
	 */
	public int[] findCentre(CountryRegion region) {
		int[] centre = new int[2];

		int n = region.npoints;
		long tx = 0;
		long ty = 0;
		for (int i = 0; i < n; i++) {
			tx += region.xpoints[i];
			ty += region.ypoints[i];
		}
		centre[0] = (int) (tx / n);
		centre[1] = (int) (ty / n);

		return centre;
	}

	public static void main(String[] args) {
		String path = "res/cities1000.txt";
		String code = args[0];
		File res = new File(path);
		Countries countries = new Countries();
		Collection<GeoName> names;
		Collection<CountryRegion> regions;
		CountryRegion region = null;
		try {
			names = countries.loadNames(res);
			regions = countries.toRegions(names);

			for (CountryRegion r : regions) {
				if (code.equals(r.getCountryCode())) {
					region = r;
					break;
				}
			}
			CountryCanvas canvas = new CountryCanvas(region);
			JDialog window = new JDialog(null, ModalityType.APPLICATION_MODAL);
			window.setBounds(0, 0, 500, 500);
			window.getContentPane().add(new JScrollPane(canvas));
			window.setVisible(true);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
