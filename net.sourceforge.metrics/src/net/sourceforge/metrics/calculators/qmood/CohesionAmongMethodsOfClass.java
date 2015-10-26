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
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.metrics.calculators.Calculator;
import net.sourceforge.metrics.calculators.InvalidSourceException;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JdtFlags;

/**
 * The relatedness among methods of a class, computed using the summation of the intersection of parameters of a method with 
 * the maximum independent set of all parameters types in the class. Cinnéide excluded constructors and implicit 'this' parameters
 * 
 * @author Leonardo Jr.
 */
@SuppressWarnings("restriction")
public class CohesionAmongMethodsOfClass extends Calculator implements Constants {

	/**
	 * Constructor for CAM.
	 */
	public CohesionAmongMethodsOfClass() {
		super(CAM);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("CohesionAmongMethodsOfClass is only applicable to types");
		}
		try {
			
			IType tipo = ((IType) source.getJavaElement());
			IMethod[] methods = tipo.getMethods();
			ArrayList<IMethod> filteredMethods = new ArrayList<IMethod>();
			Set<String> classParameterTypes = new HashSet<String>();
			double sumIntersection = 0.0;

			//Filtering the constructors and static methods
			for(IMethod method:methods){

				if(!method.isConstructor() && !JdtFlags.isStatic(method))
					filteredMethods.add(method);
			}
			
			
			/*if(filteredMethods.isEmpty()){//No CAM if no Method
				return;
			}*/	
			
			//Calculating all the parameters on the whole class
			for (IMethod method : filteredMethods) {
				for(String pType:method.getParameterTypes()){
					if(!pType.contains(tipo.getElementName()))
						classParameterTypes.add(pType);
				}
			}
			
			//If the class has methods without parameters, the metric is set to the default value of 0 (maybe needs to be 1).
			if(classParameterTypes.isEmpty()){
				source.setValue(new Metric(CAM,0));
				return;
			}
			
			//CAM is calculated as the mean of the proportional class parameter usage by the methods
			for (IMethod method : filteredMethods) {
				Set<String> parametros  = new HashSet<String>(); //Set is used to eliminate duplicates
				for(String pType:method.getParameterTypes()){
					if(!pType.contains(tipo.getElementName()))
						parametros.add(pType);
				}
				sumIntersection+=parametros.size(); //Somatório da interseção dos métodos com todos os métodos da classe
			}
			
			double cam = sumIntersection / (filteredMethods.size()*classParameterTypes.size());
			source.setValue(new Metric(CAM, cam));
			
		} catch (JavaModelException e) {
			source.setValue(new Metric(CAM, 0));
			System.out.println("Error calculating the CAM of "+source.getName());
		}
	}
}
