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
 * $Id: AbortMetrics.java,v 1.2 2004/04/30 20:21:17 sauerf Exp $
 */
 
package net.sourceforge.metrics.ui;

import net.sourceforge.metrics.builder.MetricsBuilder;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Log;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action for the checked menu item in the IJavaproject popup.
 * Checks/Unchecks based on the presence of the metrics nature
 * and selection adds/removes the nature.
 * 
 * @author Frank Sauer
 */
public class AbortMetrics implements IObjectActionDelegate, Constants {

	private IProject project;

	/**
	 * Constructor for Action1.
	 */
	public AbortMetrics() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IJavaProject p = JavaCore.create(project);
		// abort any ongoing or pending calculations for this project
		if (p != null) {
			MetricsBuilder.abort(p.getHandleIdentifier());
		} 
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if ((!selection.isEmpty()) && (selection instanceof IStructuredSelection)) {
			try {
				Openable op = (Openable)((IStructuredSelection)selection).getFirstElement();
				if (op != null) {
					project = (IProject)op.getUnderlyingResource();
					if (project.hasNature(pluginId + ".nature")) {
						IJavaProject p = JavaCore.create(project);
						if (p != null) {
							action.setEnabled(MetricsBuilder.isBuilding(p.getHandleIdentifier()));
						} else action.setEnabled(false);
					} else action.setEnabled(false);
				} 
			} catch (Throwable e) {
				Log.logError("AbortMetrics: error getting project.", e);				
				project = null;
			}
		}
	}

}
