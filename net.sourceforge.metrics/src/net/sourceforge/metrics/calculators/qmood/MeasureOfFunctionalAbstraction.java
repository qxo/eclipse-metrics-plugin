package net.sourceforge.metrics.calculators.qmood;

import java.util.ArrayList;

import net.sourceforge.metrics.calculators.Calculator;
import net.sourceforge.metrics.calculators.InvalidSourceException;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.TypeMetrics;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jdt.internal.corext.util.JdtFlags;

/**
 * The ratio of the number of methods inherited by a class to the number of methods accessible by member methods of the class. Interpreted as 
the average across all classes in a design of the ratio of the number of methods inherited by a class to the total number of methods available 
to that class, i.e. inherited and defined methods. In this implementation i don't consider static methods because they're not methods of that class 'per se'
and don't contribute to the abstraction of the project.
 
 * @author Leonardo Jr.
 *
 */
@SuppressWarnings("restriction")
public class MeasureOfFunctionalAbstraction extends Calculator implements Constants {
	
	public MeasureOfFunctionalAbstraction() {
		super(MFA);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("MFA is only applicable to types");
		}
		try {
			ITypeHierarchy hierarchy = ((TypeMetrics) source).getHierarchy();
			int declared = 0;
			int inherited = 0;
			declared = filterNonConstructors(hierarchy.getType().getMethods()).size();
			for (IType superType : hierarchy.getAllSuperclasses(hierarchy.getType()))
				if (superType instanceof ResolvedSourceType) {
					inherited += filterPublicAndProtected(superType.getMethods()).size();
				}
			if (declared + inherited == 0) {
				source.setValue(new Metric(MFA, 0));
			} else {
				double mfa = (inherited / (declared+inherited)); 
				source.setValue(new Metric(MFA, mfa));
			}
		} catch (Exception e) {
			source.setValue(new Metric(MFA, 0));
		}
	}
	
	/**
	 * Get all the NonConstructors, NonStatic methods from an array of methods
	 * @param methods
	 * @return
	 * @throws JavaModelException
	 */
	private ArrayList<IMethod> filterNonConstructors(IMethod[] methods) throws JavaModelException{
		ArrayList<IMethod> filteredMethods = new ArrayList<IMethod>(methods.length);
		for(IMethod metodo:methods){
			if(!metodo.isConstructor() && !JdtFlags.isStatic(metodo)){
				filteredMethods.add(metodo);
			}
		}
		return filteredMethods;
	}
	
	/**
	 * Get all the non-constructors, non-statics, public and protected methods from an array of methods 
	 * 
	 * @param methods An array of IMethods\
	 * @return
	 * @throws JavaModelException
	 */
	private ArrayList<IMethod> filterPublicAndProtected(IMethod[] methods) throws JavaModelException{
		ArrayList<IMethod> filteredMethods = new ArrayList<IMethod>(methods.length);
		for(IMethod metodo:methods){
			if(!metodo.isConstructor() && !JdtFlags.isStatic(metodo) && (JdtFlags.isPublic(metodo) || JdtFlags.isProtected(metodo))){
					filteredMethods.add(metodo);
			}
		}
		return filteredMethods;
	}
}
