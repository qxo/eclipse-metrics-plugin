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
 * $Id: IMetricsProgressListener.java,v 1.4 2003/07/03 04:05:10 sauerf Exp $
 */

package net.sourceforge.metrics.builder;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

public interface IMetricsProgressListener {
	/**
	 * announces that an element has moved
	 * 
	 * @param handle
	 *            new handle
	 * @param fromPath
	 *            old location
	 */
	public void moved(IJavaElement element, IPath fromPath);

	/**
	 * announces that count new elements are queued for calculation
	 * 
	 * @param count
	 */
	public void queued(int count);

	/**
	 * announces the currently calculating element
	 * 
	 * @param current
	 */
	public void pending(IJavaElement element);

	/**
	 * announces that the calculation for the element has completed
	 * 
	 * @param handle
	 * @param data
	 */
	public void completed(IJavaElement element, Object data);

	/**
	 * announces that the currently calculating project is complete
	 */
	public void projectComplete(IJavaProject project, boolean aborted);

	/**
	 * announces that the calculations are temporarily paused
	 */
	public void paused();

}