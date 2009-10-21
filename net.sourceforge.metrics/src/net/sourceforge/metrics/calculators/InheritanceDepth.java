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
package net.sourceforge.metrics.calculators;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.TypeMetrics;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;

/**
 * Calculates Depth of Inheritance Tree, Number of Children
 * and sets superclasses to either 0 or 1 (if the source has subclasses)
 * 
 * @author Frank Sauer
 */
public class InheritanceDepth extends Calculator implements Constants {

	/**
	 * Constructor for Dit. 
	 */
	public InheritanceDepth() {
		super(INHERITANCE_DEPTH);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) throw new InvalidSourceException("InheritanceDepth only applicable to types");
		TypeMetrics tm = (TypeMetrics)source;
		IType iType = (IType)source.getJavaElement();
		ITypeHierarchy hierarchy = tm.getHierarchy();
		IType[] supers = hierarchy.getAllSuperclasses(iType);
		IType[] subs = hierarchy.getSubtypes(iType); // BUG #933209 
		source.setValue(new Metric(INHERITANCE_DEPTH, supers.length));
		source.setValue(new Metric(SUBCLASSES,subs.length)); 
	}

}
