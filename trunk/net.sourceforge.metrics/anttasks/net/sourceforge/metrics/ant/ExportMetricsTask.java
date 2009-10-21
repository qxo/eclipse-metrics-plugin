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
 * $Id: ExportMetricsTask.java,v 1.1 2003/06/23 04:13:24 sauerf Exp $
 */
package net.sourceforge.metrics.ant;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import net.sourceforge.metrics.internal.xml.MetricsFirstExporter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Export the metrics to an xml file according to the task parameters
 * 
 * @author Frank Sauer
 */
public class ExportMetricsTask extends Task {

	private boolean failOnError;
	private static final String TASKNAME = "metrics.export";
	private String projectName;

	private File outFile;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {
		if (projectName == null) {
			displayError(TASKNAME + " projectName==null");
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null) {
			displayError(TASKNAME + projectName + " project==null");
			return;
		}
		if (!project.exists()) {
			displayError(TASKNAME + projectName + " not found in Workspace.");
			return;
		}
		if (!project.isOpen()) {
			displayError(TASKNAME + projectName + " is not open");
			return;
		}
		if (outFile != null) {
			MetricsFirstExporter exporter = new MetricsFirstExporter();
			IJavaProject p = JavaCore.create(project);
			if (p != null) {
				AntConsoleProgressMonitor monitor = new AntConsoleProgressMonitor(this);
				try {
					exporter.export(p, outFile, monitor);
				} catch (InvocationTargetException e) {
					displayError("Error exporting metrics" + e.getMessage());
				}
			} else {
				displayError("Project is not a Java project.");
			}
		} else {
			throw new BuildException("Must specify an output file using file=");
		}
	}

	protected void displayError(String msg) throws BuildException {
		System.out.println(msg);
		if (failOnError) {
			throw new BuildException(msg, getLocation());
		}
	}

	public void setFailonerror(String str) {
		if (str.equals("true")) {
			failOnError = true;
		} else if (str.equals("false")) {
			failOnError = false;
		} else {
			failOnError = true;
			displayError("Invalid failonerror=" + str + ", must be \"true\" or \"false\" ");
		}
	}

	public void setFile(File outFile) {
		this.outFile = outFile;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
}
