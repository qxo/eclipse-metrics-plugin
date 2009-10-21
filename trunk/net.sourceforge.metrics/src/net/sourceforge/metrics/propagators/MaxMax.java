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

import net.sourceforge.metrics.core.Max;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

/**
 * Propagate a maximum value
 * 
 * @author Frank Sauer
 */
public class MaxMax extends Propagator {

	/**
	 * Constructor for MaxMax.
	 * 
	 * @param name
	 */
	public MaxMax(String name, String per) {
		super(name, per, name);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) {
		List values = source.getMaximaFromChildren(name, per);
		Max max = Max.createFromMaxes(name, per, values);
		if (max != null) {
			source.setMaximum(max);
		}
	}

	/**
	 * @see net.sourceforge.metrics.propagators.Propagator#createNextLevel()
	 */
	@Override
	public Propagator createNextLevel() {
		return this;
	}

	@Override
	public String toString() {
		return "MaxMax(" + name + "," + per + ")";
	}
}
