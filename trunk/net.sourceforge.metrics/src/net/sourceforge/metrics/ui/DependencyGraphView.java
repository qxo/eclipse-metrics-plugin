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
 * $id$
 */
package net.sourceforge.metrics.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.ui.dependencies.DependencyGraphPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.touchgraph.graphlayout.TGException;

/**
 * @author Administrator
 */
public class DependencyGraphView extends ViewPart implements ArmListener {

	private static final String MESSAGE = "Please use the Metrics View toolbar or menu to show a dependency graph.";
	private DependencyGraphPanel glPanel;

	/**
	 * 
	 */
	public DependencyGraphView() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// Note: in 3.0M8 using SWT.EMBEDDED is critical.
		// without it the embedded component does not resize!
		Composite c = new Composite(parent, SWT.EMBEDDED);
		java.awt.Frame f = createAWTFrame(c);
		if (f != null) { // solution for bug #757046 ?
			glPanel = new DependencyGraphPanel();
			glPanel.setSize(800, 600);
			f.add("Center", glPanel);
			glPanel.showMessage(MESSAGE);
			MetricsView.setArmListener(this);
		} else {
			Label l = new Label(c, SWT.NONE);
			l.setText(MESSAGE);
			l.setAlignment(SWT.CENTER);
		}
	}

	private java.awt.Frame createAWTFrame(Composite parent) {
		// try the final 3.0M7+ API
		try {
			Class<?> clSWT_AWT = Class.forName("org.eclipse.swt.awt.SWT_AWT");
			Method m = clSWT_AWT.getMethod("new_Frame", new Class[] { Composite.class });
			java.awt.Frame f = (Frame) m.invoke(null, new Object[] { parent });
			f.setLayout(new BorderLayout());
			return f;
		} catch (Throwable e) {
			Log.logError("Could not embed awt panel using reflection", e);
			return null;
		}
	}

	public void setDependencies(final Map<String, Set<String>> dependencies) {
		try {
			glPanel.createDependencies(dependencies);
		} catch (TGException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.ArmListener#widgetArmed(org.eclipse.swt.events .ArmEvent)
	 */
	public void widgetArmed(ArmEvent e) {
		Map<String, Set<String>> deps = MetricsView.getDependencies();
		setDependencies(deps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (glPanel != null) {
			glPanel.getTGPanel().clearAll();
			glPanel = null;
		}
	}

}
