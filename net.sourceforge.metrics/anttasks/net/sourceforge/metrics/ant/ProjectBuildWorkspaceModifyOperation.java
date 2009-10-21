/**
 * (C) Copyright IBM Corp. 2002 - All Rights Reserved.
 *
 * DISCLAIMER:
 * The following code is sample code created by IBM Corporation.
 * This sample code is not part of any standard IBM product and is
 * provided to you solely for the purpose of assisting you in the
 * development of your applications.  The code is provided 'AS IS',
 * without warranty or condition of any kind.  IBM shall not be liable 
 * for any damages arising out of your use of the sample code, even 
 * if it has been advised of the possibility of such damages.
 *
 * Modified by Frank Sauer (package name)
 */
package net.sourceforge.metrics.ant;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class ProjectBuildWorkspaceModifyOperation extends WorkspaceModifyOperation {
	private IProject project;
	private int buildTypeInt;

	public ProjectBuildWorkspaceModifyOperation(IProject proj, int buildType) {
		project = proj;
		buildTypeInt = buildType;
	}

	/**
	 * Performs the steps that are to be treated as a single logical workspace change.
	 * 
	 * @param monitor
	 *            to display progress and handle cancel requests
	 * @exception CoreException
	 *                if the operation fails due to a CoreException
	 */
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException {
		String projectName = "unknown";
		try {
			projectName = project.getDescription().getName();
			monitor.setTaskName("Building: " + projectName);
			project.build(buildTypeInt, monitor);
		} catch (CoreException e) {
			throw e;
		} finally {
			monitor.done();
		}
	}
}
