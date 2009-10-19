/*
 * Copyright (c) 2003 Frank Sauer. All rights reserved.
 *
 * Licenced under CPL 1.0 (Common Public License Version 1.0).
 * The licence is available at http://www.eclipse.org/legal/cpl-v10.html.
 *
 *
 * DISCLAIMER OF WARRANTIES AND LIABILITY:
 *
 * THE SOFTWARE IS PROVIDED "AS IS".  THE AUTHOR MAKES  NO REPRESENTATIONS OR WARRANTIES,
 * EITHER EXPRESS OR IMPLIED.  TO THE EXTENT NOT PROHIBITED BY LAW, IN NO EVENT WILL THE
 * AUTHOR  BE LIABLE FOR ANY DAMAGES, INCLUDING WITHOUT LIMITATION, LOST REVENUE,  PROFITS
 * OR DATA, OR FOR SPECIAL, INDIRECT, CONSEQUENTIAL, INCIDENTAL  OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF  LIABILITY, ARISING OUT OF OR RELATED TO
 * ANY FURNISHING, PRACTICING, MODIFYING OR ANY USE OF THE SOFTWARE, EVEN IF THE AUTHOR
 * HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *
 * $id$
 */
package net.sourceforge.metrics.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * An Avg is a number with an associated count of the datapoints it
 * was calculated with, as well as the associated variance.
 * 
 * @author Frank Sauer
 */
public class Avg extends Metric implements Serializable {

	protected int points ;
	protected double variance ;
	
	/**
	 * Create the Avg for the metrics in the given list
	 * @param name			name of Avg
	 * @param per			scope of Avg
	 * @param metrics		List of Metric objects
	 * @return Avg
	 */
	public static Avg createFromMetrics(String name, String per, List metrics) {
		int points = metrics.size();
		if (points == 0) return new Avg(name, per, 0,0,0);
		double sum = 0, sum2 = 0;
		for (Iterator i = metrics.iterator(); i.hasNext();) {
			Metric next = (Metric)i.next();
			sum += next.doubleValue();
			sum2 += next.doubleValue() * next.doubleValue();
		}
		double avg = sum / points;
		return new Avg(name, per, avg, sum2 / points - avg*avg, points);
	}
	
	/**
	 * Create the (weighted) avg of the averages in the given list
	 * @param name			name of Avg
	 * @param per			scope of Avg
	 * @param metrics		List of Avg objects
	 * @return Avg
	 */
	public static Avg createFromAverages(String name, String per, List averages) {
		double sum2 = 0, sum = 0;
		int points = 0;
		for (Iterator i = averages.iterator(); i.hasNext();) {
			Avg next = (Avg)i.next();
			points += next.getPoints();
			sum += next.doubleValue() * next.getPoints();
			sum2 += next.getSum2();
		}
		if (points == 0) return new Avg(name, per, 0,0,0);
		double avg = sum / points;
		return new Avg(name, per, avg, sum2 / points - avg * avg, points);
	}
	
	public Avg(String name, String per, double value, double variance, int points) {
		super(name, per, value);
		this.variance = variance; 
		this.points = points;
	}
	
	public double getVariance() {
		return variance;
	}
	
	public double getStandardDeviation() {
		return Math.sqrt(variance);
	}
	
	public int getPoints() {
		return points;
	}
	
	/**
	 * get the sum of the squares
	 * @return double
	 */
	public double getSum2() {
		return points * (variance + getValue() * getValue());
	}
	
}
