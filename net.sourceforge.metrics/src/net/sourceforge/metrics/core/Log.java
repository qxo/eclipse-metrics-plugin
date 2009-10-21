/*
 * Created on May 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sourceforge.metrics.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Separating this from the MetricsPlugin class breaks numerous cyclic dependencies
 * 
 * @author Frank Sauer
 */
public class Log {
	
	public final static String pluginId = "net.sourceforge.metrics";
	
	public static void logError(String message, Throwable t) {
		MetricsPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, pluginId,
				IStatus.ERROR, message, t));
	}
	
	public static void logMessage(String message) {
		MetricsPlugin.getDefault().getLog().log(
			new Status(IStatus.INFO, pluginId, IStatus.INFO, message, null));
	}

}
