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

/**
 * Only needed to distinguish a pure metric from a propagated one
 * 
 * @author Frank Sauer
 */
public class Sum extends Metric implements Serializable {

	private static final long serialVersionUID = -3763122480771575457L;
	boolean propagated = true;

	public Sum(String name, double value, boolean propagated) {
		this(name, value);
		this.propagated = propagated;
	}

	public Sum(String name, double value) {
		super(name, value);
	}

	@Override
	public boolean isPropagated() {
		return propagated;
	}

	/**
	 * @param name
	 * @param per
	 * @param value
	 */
	public Sum(String name, String per, double value) {
		super(name, per, value);
	}

	/**
	 * Sets the propagated.
	 * 
	 * @param propagated
	 *            The propagated to set
	 */
	public void setPropagated(boolean propagated) {
		this.propagated = propagated;
	}

}
