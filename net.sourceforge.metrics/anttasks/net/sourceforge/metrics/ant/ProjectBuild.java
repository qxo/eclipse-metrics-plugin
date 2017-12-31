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
 *
 * Modified by Frank Sauer to export compile errors to text or xml file
 * combining the ProjectBuild and GetJavaErrorCount tasks into one
 */

package net.sourceforge.metrics.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

import net.sourceforge.metrics.internal.xml.XMLPrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaCore;

public class ProjectBuild extends Task {

	private static final String TASKNAME = "eclipse.build: ";
	private static final int UNKNOWN_ERRORS = -1;
	private static final String FAIL_MSG = "Compile failed; see the compiler error output file or this log for details.";

	private IProject project = null;
	private String projectName;
	private String buildTypeString = "INCREMENTAL";
	private String failonerror = "true";
	private boolean failOnError = true;
	private String debugcompilation = "";
	private int buildTypeInt = IncrementalProjectBuilder.INCREMENTAL_BUILD;
	private File compileErrorsOut;
	private String compileErrorsFormat;

	/**
	 * Execute this Ant task. Builds the given project according to the given parameters
	 */
	@Override
	public void execute() throws BuildException {
		validateAttributes();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
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
			AntConsoleProgressMonitor monitor = new AntConsoleProgressMonitor(this);
			monitor.beginTask(projectName, 1);
			setDebugOptions(debugcompilation);
			ProjectBuildWorkspaceModifyOperation op = new ProjectBuildWorkspaceModifyOperation(project, buildTypeInt);
			op.execute(monitor);
			// get/export errors and fail if needed
			int errors = getJavacErrorCount(project, monitor);
			if (errors > 0) {
				displayError(FAIL_MSG);
			}
			monitor.done();
		} catch (BuildException x) {
			throw x;
		} catch (Exception e) {
			displayError(TASKNAME + projectName + " Exception=" + e.getMessage());
		}
	}

	/**
	 * return the number of compilation errors and as a side effect, write the errors to the specified output file or to the build log if none specified.
	 * 
	 * @param project
	 * @param monitor
	 * @return
	 */
	private int getJavacErrorCount(IProject project, AntConsoleProgressMonitor monitor) {
		try {
			IMarker[] markerList = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if (markerList == null || markerList.length == 0) {
				return 0;
			}
			IMarker marker = null;
			int numErrors = 0;
			XMLPrintStream out = getErrorOutputStream(project.getName());
			for (IMarker element : markerList) {
				marker = element;
				int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				// default severity = ERROR
				if (severity == IMarker.SEVERITY_ERROR) {
					numErrors++;
					Integer lineNum = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
					String resourceName = marker.getResource().getName();
					String message = (String) marker.getAttribute(IMarker.MESSAGE);
					if (out != null) {
						appendError(out, marker.getResource(), message, lineNum);
					} else {
						monitor.displayMsg(resourceName + ":" + lineNum + ": " + message);
					}
				}
			}
			if (out != null) {
				close(out);
			}
			return numErrors;
		} catch (CoreException e) {
			displayError("CoreException: " + e.getMessage());
		}
		return UNKNOWN_ERRORS;
	}

	/**
	 * @param out
	 */
	private void close(XMLPrintStream p) {
		if ("XML".equalsIgnoreCase(compileErrorsFormat)) {
			p.println("</buildErrors>");
		}
		p.close();
	}

	/**
	 * 
	 */
	private XMLPrintStream getErrorOutputStream(String projectName) {
		if (compileErrorsOut != null) {
			try {
				FileOutputStream out = new FileOutputStream(compileErrorsOut);
				XMLPrintStream p = new XMLPrintStream(out);
				if ("XML".equalsIgnoreCase(compileErrorsFormat)) {
					p.printXMLHeader();
					p.print("<buildErrors project=\"");
					p.print(projectName);
					p.print("\" date=\"");
					p.print(new Date());
					p.println("\">");
				}
				return p;
			} catch (FileNotFoundException e) {
				displayError("Could not open error outputfile " + compileErrorsOut.getAbsolutePath());
				return null;
			}

		} /* else { */
		return null;
		/* } */
	}

	/**
	 * Append a compilation error to the correct output file if it was specified
	 * 
	 * @param resource
	 * @param message
	 * @param lineNum
	 */
	private void appendError(XMLPrintStream out, IResource resource, String message, Integer lineNum) {
		if ("XML".equalsIgnoreCase(compileErrorsFormat)) {
			out.indent(1);
			out.print("<Error resource=\"");
			out.print(resource.getFullPath());
			out.print("\" shortName=\"");
			out.print(resource.getName());
			out.print("\" line=\"");
			out.print(lineNum);
			out.print("\">");
			out.print(message);
			out.println("</Error>");
		} else {
			out.println(resource.getFullPath() + ":" + lineNum + ": " + message);
		}
	}

	public void setDebugOptions(String str) {
		if (str == null || str.equals("")) {
			return;
		}

		Hashtable<String, String> options = JavaCore.getOptions();
		if (str.equalsIgnoreCase("true")) {
			options.put("org.eclipse.jdt.core.compiler.debug.localVariable", "generate");
			options.put("org.eclipse.jdt.core.compiler.debug.lineNumber", "generate");
			options.put("org.eclipse.jdt.core.compiler.debug.sourceFile", "generate");
		} else if (str.equalsIgnoreCase("false")) {
			options.put("org.eclipse.jdt.core.compiler.debug.localVariable", "do not generate");
			options.put("org.eclipse.jdt.core.compiler.debug.lineNumber", "do not generate");
			options.put("org.eclipse.jdt.core.compiler.debug.sourceFile", "do not generate");
		}
		JavaCore.setOptions(options);
	}

	protected void displayError(String msg) throws BuildException {
		System.out.println(msg);
		if (failOnError) {
			throw new BuildException(msg, getLocation());
		}
	}

	public void setProjectName(String name) {
		projectName = name;
	}

	public void setBuildType(String type) {
		buildTypeString = type;
	}

	public void setFailonerror(String str) {
		failonerror = str;
	}

	public void setDebugCompilation(String str) {
		debugcompilation = str;
	}

	public void setCompileErrorsOut(File outFile) {
		if (outFile != null) {
			compileErrorsOut = outFile;
		}
	}

	public void setErrorOut(File outFile) {
		if (outFile != null) {
			compileErrorsOut = outFile;
		}
	}

	public void setCompileErrorsFormat(String outFormat) {
		compileErrorsFormat = outFormat;
	}

	public void setErrorFormat(String outFormat) {
		compileErrorsFormat = outFormat;
	}

	protected void validateAttributes() throws BuildException {
		if (failonerror.equals("true")) {
			failOnError = true;
		} else if (failonerror.equals("false")) {
			failOnError = false;
		} else {
			displayError("Invalid failonerror=" + failonerror + ", must be \"true\" or \"false\" ");
			return;
		}
		if (projectName == null) {
			displayError("Must supply ProjectName");
			return;
		}
		if (buildTypeString.equalsIgnoreCase("INCREMENTAL")) {
			buildTypeInt = IncrementalProjectBuilder.INCREMENTAL_BUILD;
		} else if (buildTypeString.equalsIgnoreCase("FULL")) {
			buildTypeInt = IncrementalProjectBuilder.FULL_BUILD;
		} else if (buildTypeString.equalsIgnoreCase("AUTO")) {
			buildTypeInt = IncrementalProjectBuilder.AUTO_BUILD;
		} else {
			displayError("Invalid BuildType=" + buildTypeString + ", must be INCREMENTAL or FULL or AUTO");
			return;
		}
		if (compileErrorsOut != null) {
			try {
				if (compileErrorsOut.exists()) {
					compileErrorsOut.delete();
				}
				compileErrorsOut.createNewFile();
			} catch (IOException e) {
				displayError("Could not initialize compile error outputfile");
				return;
			}
		}
		if (compileErrorsFormat != null) {
			if (!compileErrorsFormat.equalsIgnoreCase("XML") && !compileErrorsFormat.equalsIgnoreCase("TXT")) {
				displayError("error output format must be txt or xml");
				return;
			}
		}
	}
}
