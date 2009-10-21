/*
 * Created on Jul 6, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package net.sourceforge.metrics.ui;

import net.sourceforge.metrics.core.Log;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Used solely from within the welcome page
 * 
 * @author Frank Sauer
 */
public class OpenMetricsViewAction extends Action {

	/**
	 * 
	 */
	public OpenMetricsViewAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IWorkbenchWindow dw = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
		IWorkbenchPage page = dw.getActivePage();
		if (page != null) {
			MetricsView v = (MetricsView) page.findView("net.sourceforge.metrics.ui.MetricsView");
			if (v == null) {
				try {
					page.showView("net.sourceforge.metrics.ui.MetricsView");
				} catch (PartInitException e) {
					Log.logError("Could not create metrics view", e);
				}
			} 
		} 
	}

}
