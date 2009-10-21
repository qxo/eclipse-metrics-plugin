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
package net.sourceforge.metrics.internal.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sourceforge.metrics.core.Avg;
import net.sourceforge.metrics.core.Metric;

/**
 * @author Frank Sauer
 */
public class AvgTests extends TestCase {

	private static String name1 = "NAME1";
	private List<Metric> metrics1;
	private List<Metric> metrics2;

	/**
	 * Constructor for AvgTests.
	 * 
	 * @param arg0
	 */
	public AvgTests(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AvgTests.class);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		metrics1 = new ArrayList<Metric>();
		metrics2 = new ArrayList<Metric>();
		metrics1.add(new Metric(name1, 1));
		metrics1.add(new Metric(name1, 3));
		metrics1.add(new Metric(name1, 2));
		metrics1.add(new Metric(name1, 6));
		metrics2.add(new Metric(name1, 2));
		metrics2.add(new Metric(name1, 4));
		metrics2.add(new Metric(name1, 6));
		metrics2.add(new Metric(name1, 4));
	}

	public void testAvgFromMetrics() {
		Avg avg1 = Avg.createFromMetrics(name1, "per", metrics1);
		assertEquals("Wrong number of points", 4, avg1.getPoints());
		assertEquals("Wrong average", 3, avg1.intValue());
		assertEquals("Wrong variance", 3.5, avg1.getVariance(), 0);
		Avg avg2 = Avg.createFromMetrics(name1, "per", metrics2);
		assertEquals("Wrong number of points", 4, avg2.getPoints());
		assertEquals("Wrong average", 4, avg2.intValue());
		assertEquals("Wrong variance", 2.0, avg2.getVariance(), 0);
	}

	public void testAvgFromAverages() {
		Avg avg1 = Avg.createFromMetrics(name1, "per", metrics1);
		Avg avg2 = Avg.createFromMetrics(name1, "per", metrics2);
		List<Avg> averages = new ArrayList<Avg>();
		averages.add(avg1);
		averages.add(avg2);
		Avg result = Avg.createFromAverages(name1, "per", averages);
		assertEquals("Wrong number of points", 8, result.getPoints());
		assertEquals("Wrong average", 3.5, result.doubleValue(), 0);
		assertEquals("Wrong variance", 3.0, result.getVariance(), 0);
	}

}
