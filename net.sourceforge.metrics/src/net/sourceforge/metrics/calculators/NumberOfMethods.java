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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Counts the number of methods in a class.
 * Distinguishes between statics and instance methods, 
 * sets NUM_METHODS and NUM_STAT_METHODS
 * 
 * @author Frank Sauer
 */
public class NumberOfMethods extends Calculator implements Constants {

	/**
	 * Constructor for NumberOfMethods.
	 */
	public NumberOfMethods() {
		super(NUM_METHODS);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) throw new InvalidSourceException("NumberOfMethods is only applicable to types");
		try {
			IMethod[] methods = ((IType)source.getJavaElement()).getMethods();
			int stats = 0;
			int inst  = 0;
			for (int i = 0; i < methods.length; i++) {
				if ((methods[i].getFlags() & Flags.AccStatic) != 0) {
					stats++;
				} else {
					inst++;
				}
			}
			source.setValue(new Metric(NUM_METHODS,inst));
			source.setValue(new Metric(NUM_STAT_METHODS,stats));
		} catch (JavaModelException e) {
			source.setValue(new Metric(NUM_METHODS,0));
			source.setValue(new Metric(NUM_STAT_METHODS,0));
		}
	}

}
