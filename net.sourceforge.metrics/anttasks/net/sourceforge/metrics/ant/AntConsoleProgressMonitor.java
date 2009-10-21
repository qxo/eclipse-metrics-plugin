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
 * Modified by Frank Sauer (package name and some output formatting details)
 */
package net.sourceforge.metrics.ant;

import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.IProgressMonitor;

public class AntConsoleProgressMonitor implements IProgressMonitor {
	private String taskInfo = "";
	private Task task;
	private String taskname = "unknown";
	public static final int ERROR = org.apache.tools.ant.Project.MSG_ERR;
	public static final int WARNING = org.apache.tools.ant.Project.MSG_WARN;
	public static final int MESSAGE = org.apache.tools.ant.Project.MSG_INFO;
	private boolean quiet = false;

	public AntConsoleProgressMonitor(Task t) {
		task = t;
		if (task == null) {
			task = new nullTask();
		}
		taskname = task.getTaskName();
	}

	public void beginTask(String name, int totalTime) {
		if (name == null) {
			return;
		}
		taskname = name;
		if (!quiet) {
			task.log(name + " " + taskInfo + "...");
		}
	}

	public void done() {
		if (!quiet) {
			task.log(taskname + "... Done.");
		}
		taskname = "unknown";

		taskInfo = "";
	}

	public void internalWorked(double arg0) {
		;
	}

	public boolean isCanceled() {
		return false;
	}

	public void setCanceled(boolean arg0) {
		;
	}

	public void setTaskName(String name) {
		taskname = name;
	}

	public void subTask(String name) {
		if (name == null || name.length() == 0) {
			return;
		}
		if (!quiet) {
			task.log(name + "...");
		}
	}

	public void worked(int timework) {
		;
	}

	public void displayMsg(String msg) {
		if (!quiet) {
			task.log(msg);
		}
	}

	public void displayMsg(String msg, int level) {
		if (level == ERROR || level == WARNING) {
			task.log(msg, level);
		} else if (!quiet) {
			task.log(msg, level);
		}
	}

	public void setCurrentTaskInfo(String info) {
		taskInfo = info;
		if (taskInfo == null) {
			taskInfo = "";
		}
		if (!quiet) {
			task.log("TaskInfo=" + info);
		}
	}

	/**
	 * Run Monitor in Quiet Mode
	 * 
	 * @param q
	 *            true, to display no messages unless they are error/warning bounded, false otherwise.
	 */
	public void runQuiet(boolean q) {
		quiet = q;
	}

	private class nullTask extends Task {
		@Override
		public void log(String msg) {
		}

		@Override
		public void log(String msg, int level) {
			if (level == ERROR || level == WARNING) {
				System.out.println("nullTask: " + msg);
			}
		}
	}

}
