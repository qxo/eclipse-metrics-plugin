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

import junit.framework.TestCase;
import net.sourceforge.metrics.core.Max;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

/**
 * @author Frank Sauer
 */
public class MaxTests extends TestCase {
	
	private static String name1 = "NAME1";
	private static String name2 = "NAME2";
	private ArrayList metrics;
	private ArrayList maxes;
	private AbstractMetricSource source;

	/**
	 * Constructor for MaxTests.
	 * @param arg0
	 */
	public MaxTests(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(MaxTests.class);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		metrics = new ArrayList();
		metrics.add(new Metric(name1, 1));
		metrics.add(new Metric(name1, 3));
		metrics.add(new Metric(name1, 2));
		metrics.add(new Metric(name1, 5));
		maxes = new ArrayList();
		maxes.add(new Max(name1, "per", 1));
		maxes.add(new Max(name1, "per", 4));
		maxes.add(new Max(name1, "per", 10));
		maxes.add(new Max(name1, "per", 6));

	}

	public void testMaxFromMetrics() {
		Max max = Max.createFromMetrics(name1, "per", metrics);
		assertEquals("wrong expected max value", 5, max.intValue());
	}
	
	public void testMaxFromMaxes() {
		Max max = Max.createFromMaxes(name1, "per", maxes);
		assertEquals("wrong expected max value", maxes.get(2), max);
	}

}
