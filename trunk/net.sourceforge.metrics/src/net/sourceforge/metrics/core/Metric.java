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
package net.sourceforge.metrics.core;

import java.io.Serializable;
import java.text.NumberFormat;

import net.sourceforge.metrics.core.sources.AbstractMetricSource;

/**
 * Main class that holds a named calculated value and a (optional) scope, e.g. method, type, etc.
 * 
 * @author Frank Sauer
 */
public class Metric implements Constants, Serializable, Comparable {

	private static final long serialVersionUID = -1310980061419852562L;

	private static NumberFormat nf;

	private String name = "";
	private double value;
	private String per = "";

	/**
	 * Constructor for basic Metric.
	 */
	public Metric(String name, double value) {
		super();
		this.name = name;
		this.value = value;
	}

	/**
	 * Constructor for scoped metric
	 * 
	 * @param name
	 * @param per
	 * @param value
	 */
	public Metric(String name, String per, double value) {
		this(name, value);
		this.per = per;
	}

	public String getName() {
		return name;
	}

	public String getPer() {
		return per;
	}

	/**
	 * @see java.lang.Number#intValue()
	 */
	public int intValue() {
		return (int) value;
	}

	/**
	 * @see java.lang.Number#longValue()
	 */
	public long longValue() {
		return (long) value;
	}

	/**
	 * @see java.lang.Number#floatValue()
	 */
	public float floatValue() {
		return (float) value;
	}

	/**
	 * @see java.lang.Number#doubleValue()
	 */
	public double doubleValue() {
		return value;
	}

	@Override
	public String toString() {
		return format(value);
	}

	private static NumberFormat getNumberFormat() {
		if (nf == null) {
			nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(FRACTION_DIGITS);
			nf.setGroupingUsed(false);
		}
		return nf;
	}

	protected static String format(double d) {
		return getNumberFormat().format(d);
	}

	/**
	 * Calculate the value of this metric. This default implementation does nothing
	 * 
	 * @param ms
	 *            Source for the metric
	 */
	public void calculate(AbstractMetricSource ms) {
	}

	/**
	 * Returns the value.
	 * 
	 * @return double
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *            The value to set
	 */
	protected void setValue(double value) {
		this.value = value;
	}

	/**
	 * Only metrics calculated at the original level return false. All propagated metrics return true
	 * 
	 * @return boolean
	 */
	public boolean isPropagated() {
		return false;
	}

	/**
	 * Two metrics are considered the same if there name and per are the same. The values are not compared. This is used only for use of metrics as keys in a HashMap (for the reverse indexing)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (getClass().isInstance(o)) {
			Metric m = (Metric) o;
			return per.equals(m.getPer()) && name.equals(m.getName());
		} /* else { */
		return false;
		/* } */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (per + name).hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o == null) {
			return -1; // BUG #826997
		}
		Metric m = (Metric) o;
		if (doubleValue() == m.doubleValue()) {
			return 0;
		}
		if (doubleValue() < m.doubleValue()) {
			return -1;
		}
		return 1;
	}

}
