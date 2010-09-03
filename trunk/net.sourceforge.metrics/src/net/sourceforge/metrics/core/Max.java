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
import java.util.Iterator;
import java.util.List;

import net.sourceforge.metrics.core.sources.AbstractMetricSource;

import org.eclipse.jdt.core.IJavaElement;

/**
 * A Max is a number with an associated compilation unit and (optional) selection range.
 * 
 * @author Frank Sauer
 */
public class Max extends Metric implements Serializable {

	private static final long serialVersionUID = -6675399898791171393L;
	/**
	 * handle identifying the element causing this maximum
	 */
	private String handle;

	public static Max createFromMetrics(String name, String per, List<Metric> values) {
		double max = 0; // will be initialized with first max (if any)
		boolean found = false;
		for (Iterator<Metric> i = values.iterator(); i.hasNext();) {
			Metric next = i.next();
			if (!found || (next.doubleValue() > max)) {
				max = next.doubleValue();
				found = true;
			}

		}
		return (found) ? new Max(name, per, max) : null;
	}

	public static Max createFromChildMetrics(String name, String per, String childName, AbstractMetricSource source) {
		double max = 0; // will be initialized with first max (if any)
		IJavaElement element = null;
		for (Object element2 : source.getChildren()) {
			AbstractMetricSource next = (AbstractMetricSource) element2;
			Metric nm = next.getValue(childName);
			// BUG #716717 temporary fix until reason for null can be determined
			if ((nm != null) && ((element == null) || (nm.doubleValue() > max))) {
				max = nm.doubleValue();
				element = next.getJavaElement();
			} else {
				if (nm == null) {
					Log.logError("Max.createFromChildMetrics: metric " + childName + " not found in " + next.getJavaElement().getHandleIdentifier(), null);
				}
			}

		}
		return (element == null) ? null : new Max(name, per, max, element);
	}

	public static Max createFromMaxes(String name, String per, List<Max> values) {
		Max max = null;
		for (Iterator<Max> i = values.iterator(); i.hasNext();) {
			Max next = i.next();
			if ((max == null) || (next.doubleValue() > max.doubleValue())) {
				max = next;
			}

		}
		return max; // may be null
	}

	/**
	 * Constructor for Max.
	 */
	public Max(String name, String per, double value) {
		super(name, per, value);
	}

	private Max(String name, String per, double value, IJavaElement element) {
		this(name, per, value);
		if (element != null) {
			handle = element.getHandleIdentifier();
		}
	}

	/**
	 * return a string representation of a Max for debugging purposes only
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Max[name = ").append(getName()).append(", ");
		b.append("per = ").append(getPer()).append(", ");
		b.append("value = ").append(getValue()).append(", ");
		b.append("handle = ").append(handle).append(", ");
		return b.toString();
	}

	/**
	 * @return String
	 */
	public String getHandle() {
		return handle;
	}

	/**
	 * Sets the handle.
	 * 
	 * @param handle
	 *            The handle to set
	 */
	public void setHandle(String handle) {
		this.handle = handle;
	}

}
