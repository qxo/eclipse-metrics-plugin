package net.sourceforge.metrics.ui.layeredpackagegraph;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.lang.reflect.Method;
import java.util.List;

import net.sourceforge.metrics.core.Log;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.touchgraph.graphlayout.TGException;

public class LayeredPackageGraphView extends ViewPart implements ArmListener {
	private static final String MESSAGE = "Please use the Layered Package Table View toolbar or menu to show a package graph.";
	private LayeredPackageGraphPanel glPanel;

	/**
     * 
     */
	public LayeredPackageGraphView() {
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
			glPanel = new LayeredPackageGraphPanel();
			glPanel.setSize(800, 600);
			f.add("Center", glPanel);
			glPanel.showMessage(MESSAGE);
			LayeredPackageTableView.setArmListener(this);
		} else {
			Label l = new Label(c, SWT.NONE);
			l.setText(MESSAGE);
			l.setAlignment(SWT.CENTER);
		}
	}

	private java.awt.Frame createAWTFrame(Composite parent) {
		// try the final 3.0M7+ API
		try {
			Class clSWT_AWT = Class.forName("org.eclipse.swt.awt.SWT_AWT");
			Method m = clSWT_AWT.getMethod("new_Frame", new Class[] { Composite.class });
			java.awt.Frame f = (Frame) m.invoke(null, new Object[] { parent });
			f.setLayout(new BorderLayout());
			return f;
		} catch (Throwable e) {
			Log.logError("Could not embed awt panel using reflection", e);
			return null;
		}
	}

	public void setDependencies(final List layers) {
		try {
			glPanel.createDependencies(layers);
		} catch (TGException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setFocus() {
	}

	public void widgetArmed(ArmEvent e) {
		setDependencies(LayeredPackageTableView.getLayers());
	}

	@Override
	public void dispose() {
		super.dispose();
		if (glPanel != null) {
			glPanel.getTGPanel().clearAll();
			glPanel = null;
		}
	}

}
