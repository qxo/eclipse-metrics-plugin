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

import net.sourceforge.metrics.core.Avg;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

/**
 * Calculate an average value
 * @author Frank Sauer
 */
public class AvgValue extends Propagator {

	/**
	 * Constructor for AvgValue.
	 * @param name
	 */
	public AvgValue(String name, String per) {
		super(name, per, name);
	}

	/**
	 * Constructor for AvgValue.
	 * @param name
	 */
	public AvgValue(String name, String per, String x) {
		super(name, per, x);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	public void calculate(AbstractMetricSource source) {
		if (source.getSize() == 0) 
			source.setAverage(new Avg(getName(), per, 0,0,0));
		List metrics = source.getMetricsFromChildren(x);
		source.setAverage(Avg.createFromMetrics(name, per, metrics));
	}
	
	/**
	 * @see net.sourceforge.metrics.propagators.Propagator#createNextLevel()
	 */
	public Propagator createNextLevel() {
		return new AvgAvg(name, per);
	}
	
	public String toString() {
		return "AvgValue(" + name + "," + per + ")";
	}	
}
