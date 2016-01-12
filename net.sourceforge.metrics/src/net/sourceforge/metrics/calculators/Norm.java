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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Calculates number of overridden methods for a class. Note that if the inherited method is abstract or if the method under investigation calls the superclass' implementation, it is <EM>not</EM> counted. A better name for this metric would
 * be NumberOfReplaced methods. The reasons for not counting these is outlined on page 68 of the Lorenz and Kidd book "Object Oriented Software Metrics".
 * 
 * Note that this calculator is now configurable from a preference page.
 * 
 * @author Frank Sauer
 */
public class Norm extends Calculator implements Constants {

	private static Preferences prefs = null;

	/**
	 * Constructor for Norm.
	 */
	public Norm() {
		super(NORM);
	}

	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) {
		TypeMetrics tm = (TypeMetrics) source;
		IType iType = (IType) source.getJavaElement();
		ITypeHierarchy hierarchy = tm.getHierarchy();
		IType[] supers = hierarchy.getAllSuperclasses(iType);
		try {
			int overridden = 0;
			IMethod[] myMethods = iType.getMethods();
			List<IMethod> counted = new ArrayList<IMethod>();
			for (IMethod myMethod : myMethods) {
				// don't consider methods excluded by preferences
				if (getPrefs().countMethod(myMethod.getElementName())) {
					overridden = countMethods(supers, overridden, counted, myMethod);
				}
			}
			
			source.setValue(new Metric(NORM, overridden));
			source.setValue(new Metric(NOPM, overridden)); //NOPM is just the QMOOD name for it. I've dediced to duplicate just to make the metric readings easier.
		} catch (JavaModelException e) {
		}
	}

	private int countMethods(IType[] supers, int overridden, List<IMethod> counted, IMethod myMethod) throws JavaModelException {
		int totalOverridden = overridden;
		for (IType super1 : supers) {
			IMethod[] inheritedMethods = super1.getMethods();
			for (IMethod inheritedMethod : inheritedMethods) {
				if (counted.contains(myMethod)) {
					continue;
				}
				IMethod inherited = inheritedMethod;
				int inheritedFlags = inherited.getFlags();
				// don't have to consider static methods
				if ((inheritedFlags & Flags.AccStatic) != 0) {
					continue;
				}
				// don't have to consider private methods
				if ((inheritedFlags & Flags.AccPrivate) != 0) {
					continue;
				}
//				if (inherited.isConstructor()){
//					continue;
//				}
				// don't count abstract methods unless preferences dictate it
				if ((!getPrefs().countAbstract()) && ((inheritedFlags & Flags.AccAbstract) != 0)) {
					continue;
				}
				// methods must have same signature and return type
				if (!inherited.isSimilar(myMethod)) {
					continue;
				}
				// don't count methods invoking super unless preferences
				// override
				if ((getPrefs().countSuper()) || (!containsSuperCall(myMethod))) {
					totalOverridden++;
					counted.add(myMethod);
				}
			}
		}
		return totalOverridden;
	}

	private boolean containsSuperCall(IMethod myMethod) {
		try {
			String source = myMethod.getSource();
			int indexOfSuper = source.indexOf("super.");
			if (indexOfSuper == -1) {
				return false;
			}
			String rest = source.substring(indexOfSuper + 6);
			return rest.startsWith(myMethod.getElementName());
		} catch (JavaModelException e) {
			return false;
		}
	}

	/**
	 * Statically cache preference values, yet register for change events so they get updated when they change.
	 */
	public static class Preferences implements IPropertyChangeListener {

		private boolean countAbstract;
		private boolean supers;
		private String excludes;

		public Preferences() {
			init();
			getPreferences().addPropertyChangeListener(this);
		}

		protected void init() {
			countAbstract = getPreferences().getBoolean("NORM.Abstract");
			supers = getPreferences().getBoolean("NORM.Super");
			excludes = getPreferences().getString("NORM.ExludeList");
		}

		public boolean countAbstract() {
			return countAbstract;
		}

		public boolean countSuper() {
			return supers;
		}

		public String getExcludedMethods() {
			return excludes;
		}

		public boolean countMethod(String name) {
			return getExcludedMethods().indexOf(name) == -1;
		}

		/**
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			// System.err.println("NORM.prefs resetting!!!");
			if (event.getProperty().startsWith("NORM")) {
				init();
			}
		}
	}

	/**
	 * Returns the preferences.
	 * 
	 * @return Preferences
	 */
	public static Preferences getPrefs() {
		if (prefs == null) {
			prefs = new Preferences();
		}
		return prefs;
	}

}
