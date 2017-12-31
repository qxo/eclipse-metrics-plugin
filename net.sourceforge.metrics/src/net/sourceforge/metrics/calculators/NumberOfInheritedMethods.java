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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.TypeMetrics;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;
import org.eclipse.jdt.internal.corext.refactoring.Checks;

/**
 * Counts the number of methods in a class that are inherited from its ancestors in the its inheritance hierarchy.
 * 
 * @author Vassilis Zafeiris
 */
@SuppressWarnings("restriction")
public class NumberOfInheritedMethods extends Calculator implements Constants {

	/**
	 * Constructor for NumberOfInheritedMethods.
	 */
	public NumberOfInheritedMethods() {
		super(NUM_INHERITED_METHODS);
	}

	private void appendInheritedMethods(IType baseClass, IType superclass,
			List<IMethod> allmethods, List<IType> visitedHierarchy)
			throws JavaModelException {

		IMethod[] methods = superclass.getMethods();
		IField[] fields = superclass.getFields();
		ArrayList<IMethod> gettersAndSetters = new ArrayList<IMethod>();
		
		for (IField field : fields) {
			IMethod getter = GetterSetterUtil.getGetter(field);
			IMethod setter = GetterSetterUtil.getSetter(field);
			if (getter != null)
				gettersAndSetters.add(getter);
			if (setter != null)
				gettersAndSetters.add(setter);
		}

		for (IMethod method : methods) {
			// exclude static, private and abstract methods
			if ((method.getFlags() & Flags.AccStatic) != 0) {
				continue;
			}
			if ((method.getFlags() & Flags.AccPrivate) != 0) {
				continue;
			}

			if ((method.getFlags() & Flags.AccAbstract) != 0) {
				continue;
			}
			// exclude inherited methods with package visibility that belong to
			// different package
			if ((Flags.isPackageDefault(method.getFlags()))) {

				IPackageFragment basePackageFragment = baseClass
						.getPackageFragment();
				IPackageFragment parentPackageFragment = superclass
						.getPackageFragment();

				String basePackageName = basePackageFragment.getElementName();
				String parentPackageName = parentPackageFragment
						.getElementName();

				if (!basePackageName.startsWith(parentPackageName)) {
					continue;
				}

			}

			boolean bDeclaredInSubclass = false;
			for (IType clazz : visitedHierarchy) {
				if (Checks.findMethod(method, clazz) != null) {
					bDeclaredInSubclass = true;
					break;
				}
			}

			if (bDeclaredInSubclass) {
				continue;
			}

			if (gettersAndSetters.contains(method)) {
				continue;
			}

			allmethods.add(method);

		}

	}

	private IType[] getSuperClasses(AbstractMetricSource source) {
		TypeMetrics tm = (TypeMetrics) source;
		IType iType = (IType) source.getJavaElement();
		ITypeHierarchy hierarchy = tm.getHierarchy();
		IType[] supers = hierarchy.getAllSuperclasses(iType);
		return supers;
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source)
			throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException(
					"NumberOfMethods is only applicable to types");
		}
		try {
			IType analyzedType = ((IType) source.getJavaElement());
			
			IMethod[] methods = analyzedType.getMethods();
			IField[] fields = analyzedType.getFields();
			ArrayList<IMethod> gettersAndSetters = new ArrayList<IMethod>();

			int inst = 0;
			
			for (IField field : fields) {
				IMethod getter = GetterSetterUtil.getGetter(field);
				IMethod setter = GetterSetterUtil.getSetter(field);
				if (getter != null)
					gettersAndSetters.add(getter);
				if (setter != null)
					gettersAndSetters.add(setter);
			}

			List<IMethod> normalMethods = new ArrayList<IMethod>();

			// collect normal methods of the analyzed class
			for (IMethod method : methods) {
				// exclude static methods
				if ((method.getFlags() & Flags.AccStatic) != 0) {
					continue;
				}

				// exclude getters and setters
				if (!gettersAndSetters.contains(method)) {
					normalMethods.add(method);
				}
			}

			IType[] superclasses = getSuperClasses(source);

			// initialize 
			List<IMethod> inheritedMethods = new ArrayList<IMethod>(normalMethods);
			List<IType> visitedHierarchy = new ArrayList<IType>();
			visitedHierarchy.add(analyzedType);

			/*
			 * Traverse the hierarchy from the analyzed class up to the java.lang.Object and add to the
			 * inheritedMethods list all methods not currently encountered in subclasses
			 */
			for (IType superclass : superclasses) {
				String name = superclass.getFullyQualifiedName();
				// exclude methods of java.lang.Object
				if (name.equals("java.lang.Object")) {
					continue;
				}

				appendInheritedMethods(analyzedType, superclass, inheritedMethods,
						visitedHierarchy);
				visitedHierarchy.add(superclass);
			}

			inheritedMethods.removeAll(normalMethods);

			inst = inheritedMethods.size();

			source.setValue(new Metric(NUM_INHERITED_METHODS, inst));
		} catch (JavaModelException e) {
			source.setValue(new Metric(NUM_INHERITED_METHODS, 0));
		}
	}
	
}
