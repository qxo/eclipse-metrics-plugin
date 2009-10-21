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

import net.sourceforge.metrics.core.CalculatorDescriptor;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.ICalculator;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Abstract base class for all metrics calculators. Real calculators must implement the calculate method and a no argument default constructor.
 * 
 * @author Frank Sauer
 */
public abstract class Calculator implements Constants, ICalculator {

	protected String name;
	private CalculatorDescriptor descriptor = null;

	/**
	 * Constructor for Calculator.
	 */
	public Calculator(String name) {
		super();
		this.name = name;
	}

	public void setDescriptor(CalculatorDescriptor d) {
		descriptor = d;
	}

	public CalculatorDescriptor getDescriptor() {
		return descriptor;
	}

	protected Metric getZero() {
		return new Metric(getName(), 0);
	}

	/**
	 * Returns the name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param source
	 */
	public abstract void calculate(AbstractMetricSource source) throws InvalidSourceException;

	/**
	 * calculate fraction but return 0 if denominator is 0 instead of infinity or NaN
	 * 
	 * @param num
	 *            numerator
	 * @param den
	 *            denominator
	 * @return double
	 */
	protected double div(double num, double den) {
		if (den == 0) {
			return 0;
		}
		return num / den;
	}

	protected static IPreferenceStore getPreferences() {
		return MetricsPlugin.getDefault().getPreferenceStore();
	}
}
