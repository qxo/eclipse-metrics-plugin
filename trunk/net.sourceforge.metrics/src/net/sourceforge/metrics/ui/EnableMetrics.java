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
 * $Id: EnableMetrics.java,v 1.12 2004/05/01 17:21:35 sauerf Exp $
 */

package net.sourceforge.metrics.ui;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.metrics.builder.MetricsBuilder;
import net.sourceforge.metrics.builder.MetricsNature;
import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Log;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action for the checked menu item in the IJavaproject popup. Checks/Unchecks based on the presence of the metrics nature and selection adds/removes the nature.
 * 
 * @author Frank Sauer
 */
public class EnableMetrics implements IObjectActionDelegate, Constants {

	private IProject project;

	/**
	 * Constructor for Action1.
	 */
	public EnableMetrics() {
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
		Shell shell = new Shell();
		if (project != null) {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public String error = "";

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						if (!project.hasNature(PLUGIN_ID + ".nature")) {
							monitor.beginTask("Enabling metrics", 2);
							monitor.worked(1);
							error = "enable";
							MetricsNature.addNatureToProject(project, monitor);
							monitor.worked(1);
						} else {
							monitor.beginTask("Disabling metrics", 2);
							monitor.worked(1);
							error = "disable";
							MetricsNature.removeNatureFromProject(project, monitor);
							IJavaProject p = JavaCore.create(project);
							// abort any ongoing or pending calculations for
							// this project
							if (p != null) {
								MetricsBuilder.abort(p.getHandleIdentifier());
							}
							monitor.worked(1);
						}
					} catch (CoreException e) {
						throw new MetricsNatureException(e, error);
					} finally {
						monitor.done();
					}
				}
			};
			try {
				new ProgressMonitorDialog(shell).run(true, false, op);
			} catch (MetricsNatureException e) {
				Log.logError("Could not " + e.getTask() + " metrics", e);
				MessageDialog.openInformation(shell, "Metrics", "Could not enable metrics.");
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
				Log.logError("Could not change metrics enablement", e);
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if ((!selection.isEmpty()) && (selection instanceof IStructuredSelection)) {
			try {
				// Openable op =
				// (Openable)((IStructuredSelection)selection).getFirstElement();
				IJavaElement elem = (IJavaElement) ((IStructuredSelection) selection).getFirstElement();
				if (elem != null) {
					project = (IProject) elem.getUnderlyingResource();
					action.setChecked(project.hasNature(PLUGIN_ID + ".nature"));
				}
			} catch (Throwable e) {
				Log.logError("EnableMetrics: error getting project.", e);
				project = null;
			}
		}
	}

	static class MetricsNatureException extends InvocationTargetException {

		private static final long serialVersionUID = -8316949498780911023L;
		private String task = "";

		MetricsNatureException(Throwable t, String task) {
			super(t);
			this.task = task;
		}

		String getTask() {
			return task;
		}
	}
}
