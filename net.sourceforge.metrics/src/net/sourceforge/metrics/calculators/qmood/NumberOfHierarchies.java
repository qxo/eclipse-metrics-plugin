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
 *
 * $Id: InheritanceDepth.java,v 1.11 2004/04/30 19:49:17 sauerf Exp $
 */
package net.sourceforge.metrics.calculators.qmood;

import net.sourceforge.metrics.calculators.Calculator;
import net.sourceforge.metrics.calculators.InvalidSourceException;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.TypeMetrics;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;

/**
 * Calculates if this type is the root of a hierarchic tree, and receives NOH = 1. Otherwise NOH = 0.
 * At an upper level, the propagated sum calculates the number of hiearchies on the software. 
 * 
 * @author Leonardo Jr.
 */
public class NumberOfHierarchies extends Calculator implements Constants {

	/**
	 * Constructor for NOH.
	 */
	public NumberOfHierarchies() {
		super(NOH);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("NumberOfHierarchies only applicable to types");
		}
		TypeMetrics tm = (TypeMetrics) source;
		IType iType = (IType) source.getJavaElement();
		ITypeHierarchy hierarchy = tm.getHierarchy();
		IType[] supers = hierarchy.getAllSuperclasses(iType);
		int numSourceSupers = 0;
		for (IType type : supers) {
			if(!type.isBinary())
				numSourceSupers++;
		}
		int numSubSources = 0;
		IType[] subs = hierarchy.getSubtypes(iType); // BUG #933209
		for (IType type : subs) {
			if(!type.isBinary())
				numSubSources++;
		}
		//If the type has no source supers and any subclasses, it's considered a root.
		if(numSourceSupers==0 && numSubSources>0)
			source.setValue(new Metric(NOH,1));
		else source.setValue(new Metric(NOH,0));
	}

}
