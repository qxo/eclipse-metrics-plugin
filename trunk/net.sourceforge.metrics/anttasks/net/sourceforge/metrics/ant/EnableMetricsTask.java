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
 * $Id: EnableMetricsTask.java,v 1.3 2004/05/01 17:24:30 sauerf Exp $
 */
package net.sourceforge.metrics.ant;

import net.sourceforge.metrics.builder.MetricsNature;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * Add metrics nature and build command to the project specified in the task parameters
 * 
 * @author Frank Sauer
 */
public class EnableMetricsTask extends Task {

	private static final String TASKNAME = "metrics.enable";
	public final static String pluginId = "net.sourceforge.metrics";

	private String projectName;
	private boolean failOnError;

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
		try {
			if (!project.hasNature(pluginId + ".nature")) {
				log("Enabling metrics", 1);
				MetricsNature.addNatureToProject(project, null);
				log("metrics enabled.");
			} else {
				log("Metrics already enabled");
			}
		} catch (CoreException e) {
			displayError(TASKNAME + " Could not enable the metrics for the given project.");
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

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
}
