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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Counts the number of attributes in a class. Distinguishes between statics and instance fields, sets NUM_FIELDS and NUM_STAT_FIELDS
 * 
 * @author Frank Sauer
 */
public class NumberOfAttributes extends Calculator implements Constants {

	/**
	 * Constructor for NumberOfAttributes.
	 */
	public NumberOfAttributes() {
		super(NUM_FIELDS);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("NumberOfAttributes is only applicable to types");
		}
		try {
			IField[] fields = ((IType) source.getJavaElement()).getFields();
			int stats = 0;
			int inst = 0;
			for (IField field : fields) {
				if ((field.getFlags() & Flags.AccStatic) != 0) {
					stats++;
				} else {
					inst++;
				}
			}
			source.setValue(new Metric(NUM_FIELDS, inst));
			source.setValue(new Metric(NUM_STAT_FIELDS, stats));
		} catch (JavaModelException e) {
			source.setValue(new Metric(NUM_FIELDS, 0));
			source.setValue(new Metric(NUM_STAT_FIELDS, 0));
		}
	}

}
