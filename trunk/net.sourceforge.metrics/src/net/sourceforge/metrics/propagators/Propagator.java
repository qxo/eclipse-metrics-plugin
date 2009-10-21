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
package net.sourceforge.metrics.propagators;

import net.sourceforge.metrics.calculators.Calculator;

/**
 * A Propagator propagates values up the source tree. Current implementations are Max, Avg and Sum.
 * 
 * @author Frank Sauer
 */
public abstract class Propagator extends Calculator {

	protected String x = "";
	protected String per = "";

	/**
	 * Constructor for Propagator.
	 * 
	 * @param name
	 */
	public Propagator(String name, String per) {
		super(name);
		this.per = per;
	}

	/**
	 * Constructor for Propagator.
	 * 
	 * @param name
	 *            name of result
	 * @param x
	 *            name of metric being propagated
	 */
	public Propagator(String name, String per, String x) {
		this(name, per);
		this.x = x;
	}

	/**
	 * Returns the name of the metric being propagated.
	 * 
	 * @return String
	 */
	public String getX() {
		return x;
	}

	/**
	 * Sets the name of the metric being propagated.
	 * 
	 * @param x
	 *            The x to set
	 */
	public void setX(String x) {
		this.x = x;
	}

	/**
	 * Returns the scope of the value, e.g. per method or per class.
	 * 
	 * @return String
	 */
	public String getPer() {
		return per;
	}

	/**
	 * Sets the scope of the value, e.g. per method or per class.
	 * 
	 * @param per
	 *            The per to set
	 */
	public void setPer(String per) {
		this.per = per;
	}

	public abstract Propagator createNextLevel();
}
