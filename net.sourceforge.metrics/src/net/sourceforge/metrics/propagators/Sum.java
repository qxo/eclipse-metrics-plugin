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

import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

/**
 * Calculates the sum of the metric with name x in the source's children. The name of the sum could be different
 * 
 * @author Frank Sauer
 */
public class Sum extends Propagator {

	/**
	 * Calculates the sum of the metric with name x in the source's children. The name of the sum could be different
	 * 
	 * @param name
	 *            name of Sum
	 * @param x
	 *            name of metric to be summed
	 */
	public Sum(String name, String x) {
		super(name, "", x);
	}

	/**
	 * Calculates the sum of the metric with same name in the source's children.
	 * 
	 * @param name
	 *            name of Sum and of metric to be summed
	 */
	public Sum(String name) {
		this(name, name);
	}

	/**
	 * @see net.sourceforge.metrics.core.metrics.Metric#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) {
		double sum = 0;
		for (Object element : source.getChildren()) {
			AbstractMetricSource next = (AbstractMetricSource) element;
			Metric partial = next.getValue(x);
			if (partial != null) {
				sum += partial.doubleValue();
			}
		}
		source.setValue(new net.sourceforge.metrics.core.Sum(getName(), sum, isPropagator()));
	}

	private boolean isPropagator() {
		return getName().equals(x);
	}

	/**
	 * @see net.sourceforge.metrics.propagators.Propagator#createNextLevel()
	 */
	@Override
	public Propagator createNextLevel() {
		return new Sum(name, name);
	}

	@Override
	public String toString() {
		return "Sum(" + name + ")";
	}
}
