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
package net.sourceforge.metrics.calculators.qmood;

import java.util.ArrayList;

import net.sourceforge.metrics.calculators.Calculator;
import net.sourceforge.metrics.calculators.InvalidSourceException;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;
import org.eclipse.jdt.internal.corext.util.JdtFlags;

/**
 * Counts the number of instance class methods, excluding getters and setters. Those are identified using the Eclipse api
 * for getter and setter generation which respect the JavaBean nomenclature conventions. 
 * 
 * @author Leonardo Jr.
 */
@SuppressWarnings("restriction")
public class NumberOfNormalMethods extends Calculator implements Constants {

	/**
	 * Constructor for NumberOfMethods.
	 */
	public NumberOfNormalMethods() {
		super(NUM_NORMAL_METHODS);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("NumberOfNormalMethods is only applicable to types");
		}
		try {
			IType type = ((IType) source.getJavaElement());
			IMethod[] methods = type.getMethods();
			IField[] fields = type.getFields();
			ArrayList<IMethod> gettersAndSetters = new ArrayList<IMethod>();
			int numNormalMethods=0;
			
			for(IField field:fields){
				IMethod getter = GetterSetterUtil.getGetter(field);
				IMethod setter = GetterSetterUtil.getSetter(field);
				if(getter!=null)
					gettersAndSetters.add(getter);
				if(setter!=null)
					gettersAndSetters.add(setter);
			}	
			
			for (IMethod method : methods) {
				 if(!gettersAndSetters.contains(method) && !JdtFlags.isStatic(method))
						numNormalMethods++;
			}
			source.setValue(new Metric(NUM_NORMAL_METHODS, numNormalMethods));
		} catch (JavaModelException e) {
			source.setValue(new Metric(NUM_NORMAL_METHODS, 0));
		}
	}
}
