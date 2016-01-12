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
package net.sourceforge.metrics.calculators;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

/**
 * Calculates the SI (NORM * DIT) / NOM. Note that because NORM is configurable from preferences, the value of SI is indirectly influenced by preferences as well.
 * 
 * @author Frank Sauer
 */
public class SpecializationIndex2 extends Calculator implements Constants {

	/**
	 * Constructor for SpecializationIndex.
	 */
	public SpecializationIndex2() {
		super(SPECIALIZATION_IN2);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("SpecializationIndex only applicable to types");
		}
		double norm = source.getValue(NORM).doubleValue();
		double dit = source.getValue(INHERITANCE_DEPTH).doubleValue();
		double nom = source.getValue(NUM_METHODS).doubleValue();
		double nim = source.getValue(NUM_INHERITED_METHODS).doubleValue();
		source.setValue(new Metric(SPECIALIZATION_IN2, div(norm * dit, (nom + nim))));
	}

}
