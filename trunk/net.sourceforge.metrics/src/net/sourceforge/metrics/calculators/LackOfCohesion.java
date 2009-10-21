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
 * $Id: LackOfCohesion.java,v 1.15 2005/01/16 21:32:04 sauerf Exp $
 */
package net.sourceforge.metrics.calculators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

/**
 * Calculates the Lack of Cohesion of Methods (LCOM*) metric
 * using the Henderson-Sellers method (See book page 147):
 * (avg(m(a)) - m)/(1 - m) where m(a) is the number of methods that access a.
 * Note that whether static attributes and static methods are considered is
 * configurable from preferences.
 * 
 * @author Frank Sauer
 */
public class LackOfCohesion extends Calculator implements Constants {

	private HashMap buckets = new HashMap();
	private static Preferences prefs;
	
	/**
	 * Constructor for LackOfCohesion.
	 * @param name
	 */
	public LackOfCohesion() {
		super(LCOM);
	}

	private void add(String field, String method) {
		if (buckets.containsKey(field)) {
			Set methods = (Set) buckets.get(field);
			methods.add(method);
		} 
	}
	
	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) 
			throw new InvalidSourceException("LCOM only applicable to types");
		try {
			IType type = (IType)source.getJavaElement();
			IMethod[] methods = type.getMethods();
			IField[] fields = type.getFields();
			double value = 0;
			if ((fields.length > 1)&&(methods.length > 1)) {
				initBuckets(fields);
				if (buckets.size() > 0) {
					visitMethods(methods);
					value = calculateResult();
				}
			}
			source.setValue(new Metric(LCOM, value));
		} catch (JavaModelException e) {
		}
	}

	/**
	 * @return double (avg(m(a)) - m)/(1 - m) where m(a) is the number of methods that access a
	 */
	private double calculateResult() {
		int sum = 0;
		int a = 0;
		Set allMethods = new HashSet();
		for (Iterator i = buckets.values().iterator(); i.hasNext();a++) {
			Set methods = (Set)i.next();
			allMethods.addAll(methods);
			sum += methods.size();
		}
		int m = allMethods.size();
		if (m == 1) return 0;
		double avg = (double)sum / (double)a;
		return Math.abs((avg - m) / (1 - m));
	}

	private void visitMethods(IMethod[] methods) {
		boolean countStatics = getPrefs().countStaticMethods();
		for (int i = 0; i < methods.length; i++) {
			String methodName = methods[i].getElementName();
			try {
				if ((countStatics)||((methods[i].getFlags() & Flags.AccStatic) == 0)) {
					IScanner s = ToolFactory.createScanner(false, false, false, false);
					s.setSource(methods[i].getSource().toCharArray());
					while (true) {
						 int token = s.getNextToken();
						 if (token == ITerminalSymbols.TokenNameEOF) break;
						 if (token == ITerminalSymbols.TokenNameIdentifier) {
						 	add(new String(s.getCurrentTokenSource()), methodName);
						 }
					}
				}
			} catch (JavaModelException e) {
				System.err.println("LCOM:Can't get source for method " + methodName);
			} catch (InvalidInputException e) {
				System.err.println("LCOM:Invalid scanner input for method" + methodName);
			}
		}
	}

	/**
	 * create a map of HashSets to store methods for each attribute. Ask 
	 * preferences whether static attributes have to be considered
	 * @param fields
	 */
	private void initBuckets(IField[] fields) {
		buckets.clear(); // BUG #867594 
		try {
			boolean countStatics = getPrefs().countStaticAttributes();
			for (int i = 0; i < fields.length; i++) {
				if (countStatics||((fields[i].getFlags() & Flags.AccStatic) == 0)) {
					buckets.put(fields[i].getElementName(), new HashSet());
				}
			}
		} catch (JavaModelException e) {
		}
	}
	
	/**
	 * Statically cache preference values, yet register for change events so they
	 * get updated when they change.
	 */
	public static class Preferences implements org.eclipse.core.runtime.Preferences.IPropertyChangeListener {

		private boolean countStaticMethods;
		private boolean countStaticAttributes;

		
		public Preferences() {
			init();
			getPreferences().addPropertyChangeListener(this);
		}
		
		protected void init() {
			countStaticMethods = getPreferences().getBoolean("LCOM.StaticMethods");
			countStaticAttributes = getPreferences().getBoolean("LCOM.StaticAttributes");
		}
		
		public boolean countStaticMethods() {
			return countStaticMethods;
		}
		
		public boolean countStaticAttributes() {
			return countStaticAttributes;
		}
		
		/**
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
			if (event.getProperty().startsWith("LCOM")) {
				init();
			}
		}
	}
	
	/**
	 * Returns the preferences.
	 * @return Preferences
	 */
	public static Preferences getPrefs() {
		if (prefs == null) {
			prefs = new Preferences();
		}
		return prefs;
	}
	
}
