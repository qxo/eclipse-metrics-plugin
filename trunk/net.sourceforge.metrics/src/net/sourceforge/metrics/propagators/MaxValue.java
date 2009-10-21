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
package net.sourceforge.metrics.propagators;

import java.util.List;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Max;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

/**
 * Calculate a maximum value
 * @author Frank Sauer
 */
public class MaxValue extends Propagator implements Constants {

	/**
	 * Constructor for MaxValue.
	 * @param name	 name of the maximum
	 * @param per	 name of the scope
	 * @param x	 name of the metric to max
	 */
	public MaxValue(String name, String per, String x) {
		super(name, per, x);
	}

	/**
	 * Constructor for MaxValue.
	 * @param name	 name of the maximum as well as the metric to maximize
	 * @param per	 name of the scope
	 */
	public MaxValue(String name, String per) {
		super(name, per, name);
	}
	
	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	public void calculate(AbstractMetricSource source) {
		List values = source.getMetricsFromChildren(x);
		Max m = Max.createFromChildMetrics(name, per, x, source);
		if (m != null) source.setMaximum(m);
	}
	
	/**
	 * @see net.sourceforge.metrics.propagators.Propagator#createNextLevel()
	 */
	public Propagator createNextLevel() {
		return new MaxMax(name, per);
	}

	public String toString() {
		return "MaxValue(" + name + "," + per + ")";
	}
}
