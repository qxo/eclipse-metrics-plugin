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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import net.sourceforge.metrics.builder.IMetricsProgressListener;
import net.sourceforge.metrics.builder.MetricsBuilder;
import net.sourceforge.metrics.core.IExporter;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import net.sourceforge.metrics.core.sources.IGraphContributor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * View that renders the metrica in a table and reacts to selection events
 * and resource change events from the environment
 * 
 * @author Frank Sauer
 */
public class MetricsView extends ViewPart implements ISelectionListener, IMetricsProgressListener, Preferences.IPropertyChangeListener {
	
//	 FIXME GB 04/15/2005 move that const to the approriate place
	private static String pluginId = MetricsPlugin.getDefault().getBundle().getSymbolicName();

	private final static String[] EXPLANATION = {
		"No metrics available for selection. To calculate and display metrics:",
		"",
		"    1) ensure you are in a java perspective using the package explorer,",
		"    2) select a project and enable the metrics from its context menu,",
		"    3) perform a full rebuild on the project.",
		"",
		"After the above steps, selecting any java element in the project will result in metrics being shown here.",
		"Automatic builds will keep the metrics up-to-date by re-calculating the metrics for changed elements only.",
		"",
		"To temporarily pause calculations, click the pause button. Click the resume button to resume.",
		"To abort all current and pending calculations, click the stop button."
	};
	

	private Composite tablePage;
	private Composite explanationPage;
	private Composite cards;
	private StackLayout pageSelector;
	private int queued;
	private ProgressBar progressBar;
	private Label progressText;
	private static ArmListener armListener;
	private static Map currentDependencies;
	private IMemento memento;
	private MetricsActionGroup mActions;
	private MetricsTable table;
	private Cursor wait;
	private Cursor normal;
	private IJavaElement selection;
		
	/**
	 * The constructor.
	 */
	public MetricsView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout(1, false));
		cards = new Composite(c, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		pageSelector = new StackLayout();
		cards.setLayoutData(data);
		cards.setLayout(pageSelector);
		explanationPage = createExplanation(cards);
		pageSelector.topControl = explanationPage;
		tablePage = new Composite(cards, SWT.NONE);
		tablePage.setLayout(new GridLayout(1, false));
		createTable(tablePage);
		createStatusBar(c);
		getViewSite().getPage().addSelectionListener(this);
		mActions = new MetricsActionGroup(this);
		IActionBars actionBars = getViewSite().getActionBars();
		mActions.fillActionBars(actionBars); 
		MetricsPlugin.getDefault().addPropertyChangeListener(this);
		MetricsBuilder.addMetricsProgressListener(this);
	}

	private void createStatusBar(Composite c) {
		Composite statusbar = new Composite(c, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		statusbar.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = false;
		statusbar.setLayoutData(data);
		progressText = new Label(statusbar, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		progressText.setLayoutData(data);
		progressBar = new ProgressBar(statusbar, SWT.NONE);
		data = new GridData();
		data.widthHint = 250;
		data.verticalAlignment = GridData.BEGINNING;
		progressBar.setLayoutData(data);
		progressBar.setMaximum(0);
	}

	private void createTable(Composite c) {
		table = new MetricsTable(c, SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH | SWT.H_SCROLL | SWT.V_SCROLL);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		table.setLayoutData(data);
		table.initWidths(memento);
		table.addSelectionListener(new SelectionListener() {
		
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = (TreeItem) e.item;
				if (item != null) {
					supplementTitle(item);
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	private Composite createExplanation(Composite stackC) {
		Composite exp = new Composite(stackC, SWT.NONE);
		exp.setLayout(new GridLayout(1, false));
		for (int i = 0; i < EXPLANATION.length; i++) {
			Label l = new Label(exp, SWT.NONE);
			l.setText(EXPLANATION[i]);
		}		
		return exp;
	}

	/**
	 * Add the metric desciption to the titlebar
	 * @param item
	 */
	private void supplementTitle(TreeItem item) {
		StringBuffer b = getTitlePrefix(selection);
		TreeItem root = item; // fix submitted by Jacob Eckel 5/27/03
		while (root.getParentItem() != null) root = root.getParentItem();
		b.append(" - ").append(root.getText(0));
		setPartName(b.toString());
	}
	
	private Cursor getWaitCursor(Display d) {
		if (wait==null) {
			wait = new Cursor(d, SWT.CURSOR_WAIT);
		}
		return wait;
	}
	
	private Cursor getNormalCursor(Display d) {
		if (normal==null) {
			normal = new Cursor(d, SWT.CURSOR_ARROW);
		}
		return normal;
	}

	private void setJavaElement(final IJavaElement elm, boolean force) {		
		if (elm == null) return; // BUG 676496
		if (force || (elm != getSelection())) {
			setSelection(elm);
		}		
	}
		
	private void setStatus(final String title, final boolean busy) {
		final Display display = Display.getDefault();
		display.asyncExec(new Runnable () {
			public void run() {
				if (table.isDisposed()) return;
				progressText.setText(title);
				progressText.update();
				if (busy) {
					table.setCursor(getWaitCursor(display));
					mActions.disable();
				} else { 
					table.setCursor(getNormalCursor(display));
					mActions.enable();
				}
			}
		});
	}
	
	private void refreshTable(final AbstractMetricSource ms, final IJavaElement selection) {
		final Display display = Display.getDefault();
		display.asyncExec(new Runnable () {
			public void run() {
				if (!table.isDisposed()) {
					table.setMetrics(ms);
					table.setCursor(getNormalCursor(table.getDisplay()));
					setPartName(getTitlePrefix(selection).toString());
				}
			}
		});
	}

	private StringBuffer getTitlePrefix(IJavaElement element) {
		StringBuffer b = new StringBuffer("Metrics - ");
		if (element == null) return b;
		String name = element.getElementName();
		if ("".equals(name)) name = "(default package)";
		b.append(name);
		return b;		
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		table.setFocus();
	}
	
	/**
	 * react to selections elsewhere in the workbench
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(!selection.isEmpty()){
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection l_structSelection = (IStructuredSelection) selection;
				if(l_structSelection.size() == 1){ //only one element allowed
					Object l_first = ((IStructuredSelection)l_structSelection).getFirstElement();
					IJavaElement l_jElem = null;
					if (l_first instanceof IJavaElement) {
						l_jElem = (IJavaElement)l_first;
					}else if(l_first instanceof IResource){
						l_jElem = (IJavaElement) ((IResource) l_first).getAdapter(IJavaElement.class);
					}
					if (l_jElem != null && canDoMetrics(l_jElem)){
						try{
							if(l_jElem.getJavaProject().getProject().hasNature(pluginId + ".nature")){
								setJavaElement(l_jElem, false);
							}
						}catch(CoreException l_ce){
//							 TODO GB 04/15/2005 ? what to do in such case
							Log.logError("project nature does not exist",l_ce);
						}
					}
				}
			}
		}
	}
	
	/**
	 * @param type
	 * @return true if acceptable type for metrics calculation
	 */
	private boolean canDoMetrics(IJavaElement element) {
		int type = element.getElementType();
		if (type == IJavaElement.CLASS_FILE) return false;
		if (type == IJavaElement.FIELD) return false;
		if (type == IJavaElement.IMPORT_CONTAINER) return false;
		if (type == IJavaElement.IMPORT_DECLARATION) return false;
		if (type == IJavaElement.INITIALIZER) return false;
		if (type == IJavaElement.PACKAGE_DECLARATION) return false;
		return true;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getViewSite().getPage().removeSelectionListener(this);
		MetricsBuilder.removeMetricsProgressListener(this);
		if (wait != null) wait.dispose();
		if (normal != null) normal.dispose();	
		super.dispose();
		// not sure if super does this, so check it
		if (!table.isDisposed()) table.dispose();
	}
	
	/**
	 * Returns the selection.
	 * @return IJavaElement
	 */
	public IJavaElement getSelection() {
		return selection;
	}

	protected void setSelection(IJavaElement elm) {
		if (elm != null) selection = elm;
		AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(selection);
		if (ms != null) {
			refreshTable(ms, selection);
			showTablePage();
		} else showExplanationPage();
	}

	/**
	 * 
	 */
	private void showExplanationPage() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				pageSelector.topControl = explanationPage;
				cards.layout();
				mActions.disable();
			}
		});
	}

	/**
	 * 
	 */
	private void showTablePage() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				pageSelector.topControl = tablePage;
				cards.layout();
				mActions.enable();
			}
		});
	}

	/**
	 * display dependency graph as embedded workbench view on Windows,
	 * in a separate AWT frame on all other platforms.
	 * As of 4/30/04, only 3.0M8+ and embedded style on all platforms
	 */
	public void displayDependencyGraph() {
		/*
		if (selection.getElementType() <= IJavaElement.PACKAGE_FRAGMENT_ROOT) {
			IGraphContributor source = (IGraphContributor) Dispatcher.getAbstractMetricSource(selection);
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				displayDependencyGraphSWT(source.getEfferent());
			} else {
				displayDependencyGraphAWT(source.getEfferent());
			}
		}
		*/
		// now works the same on all platforms
		IGraphContributor source = (IGraphContributor) Dispatcher.getAbstractMetricSource(selection);
		displayDependencyGraphSWT(source.getEfferent());
	}

	private void displayDependencyGraphSWT(final Map graph) {
		IWorkbenchWindow dw = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
		IWorkbenchPage page = dw.getActivePage();
		if (page != null) {
			DependencyGraphView v = (DependencyGraphView) page.findView("net.sourceforge.metrics.ui.DependencyGraphView");
			if (v == null) {
				try {
					page.showView("net.sourceforge.metrics.ui.DependencyGraphView");
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			} 
			currentDependencies = graph;
			fireArmEvent();
		} 
	}

	/*
	private void displayDependencyGraphAWT(final Map graph) {
		Display.getCurrent().asyncExec(new Runnable() {
		//Thread awt = new Thread(new Runnable() {
			public void run() {
				final Frame frame = new Frame("Dependencies");
				final DependencyGraphPanel glPanel = new DependencyGraphPanel();
				try {
					glPanel.createDependencies(graph);
				} catch (TGException e1) {
					Log.logError("Could not create DependencyGraphPanel", e1);
				}
				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
					  frame.remove(glPanel);
					  frame.dispose();
					}
				});
				frame.add("Center", glPanel);
				frame.setSize(800,600);
				frame.setVisible(true);
			}
		});
		//awt.start();
	}	
	*/
	
	/**
	 * 
	 */
	private static void fireArmEvent() {
		if (armListener != null) {
			armListener.widgetArmed(null);
		}
	}

	public static Map getDependencies() {
		return currentDependencies;
	}
	
	/**
	 * export the selected metrics to an XML report
	 */
	public void exportXML() {
		if (selection != null) {
			Shell activeShell = new Shell();
			FileDialog d = new FileDialog(activeShell, SWT.SAVE);
			String fileName = d.open();
			if (fileName != null) {
				File outputFile = new File(fileName);
				IExporter exporter = MetricsPlugin.getDefault().getCurrentExporter();
				if (exporter != null) 
					doExport(activeShell, outputFile, exporter);	
				else {
					MessageDialog.openWarning(activeShell, "Warning", "Sorry, exporter is not available");
				}			
			}
		}
	}

	private void doExport(Shell activeShell, final File outputFile, final IExporter exporter) {
		try {
			
			IRunnableWithProgress op = new IRunnableWithProgress() {
		
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					exporter.export(selection, outputFile, monitor);							
				}
			};
			new ProgressMonitorDialog(activeShell).run(true, true, op);
		 } catch (InvocationTargetException e) {
			Log.logError("MetricsView::doExport", e);
		 } catch (InterruptedException e) {
			outputFile.delete();
		 }
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			this.memento = memento;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		if (table != null) table.updateWidths(memento);
		super.saveState(memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(Preferences.PropertyChangeEvent event) {
		if (selection != null) setJavaElement(selection, true);
	}

	/**
	 * @param view
	 */
	public static void setArmListener(ArmListener l) {
		armListener = l;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.core.MetricsBuilder.MetricsProgressListener#pending(java.lang.String)
	 */
	public void pending(IJavaElement current) {
		setStatus(
				"Queued: " + queued + "\tCalculating now: " + current.getElementName(), 
				shouldBeBusy(current));		
	}
	
	private boolean shouldBeBusy(IJavaElement current) {
		boolean busy = (selection != null)&&(selection.equals(current));
		busy = busy || (selection != null)&&(current.getHandleIdentifier().startsWith(selection.getHandleIdentifier()));
		return busy;
	}
	
	private void resetProgressBar() {
		Display d = Display.getDefault();
		d.asyncExec(new Runnable() {

			public void run() {
					progressBar.setMaximum(0);
					progressBar.setSelection(0);
					mActions.enable();
			}
		});
	}
	
	private void incProgressBar() {
		Display d = Display.getDefault();
		d.asyncExec(new Runnable() {

			public void run() {
				if (!progressBar.isDisposed())
					progressBar.setSelection(progressBar.getSelection()+1);
			}
		});
	}
	
	private void addWorkToProgressBar() {
		Display d = Display.getDefault();
		d.asyncExec(new Runnable() {

			public void run() {
				progressBar.setMaximum(queued);
				mActions.disable();
			}
		});
	}
		
	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.core.MetricsBuilder.MetricsProgressListener#completed(java.lang.String)
	 */
	public void completed(IJavaElement element, Object data) {
		setStatus("completed " + element.getElementName(), shouldBeBusy(element));
		if ((selection != null)&&(selection.equals(element))) {
			AbstractMetricSource ms = (AbstractMetricSource)data;
			refreshTable(ms, selection);
		}
		queued--;
		incProgressBar();
	}

	public void queued(int count) {
		queued += count;
		addWorkToProgressBar();
	}
	
	public void projectComplete(IJavaProject project, boolean aborted) {
		//Log.logMessage("Got projectComplete event.");
		queued = 0;
		setStatus("", false);
		resetProgressBar();
		// force rendering of completed project
		boolean showProject = MetricsPlugin.getDefault().showProjectOnCompletion();

		if (!aborted) setSelection(showProject?project:selection);
	}
		
	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.core.MetricsBuilder.MetricsProgressListener#moved(java.lang.String, org.eclipse.core.runtime.IPath)
	 */
	public void moved(IJavaElement element, IPath fromPath) {
		if ((selection != null) && (selection.getPath().equals(fromPath))) {
			// this causes the view to refresh with new element when it completes
			selection = element;
		}
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.metrics.builder.IMetricsProgressListener#paused()
	 */
	public void paused() {
		setStatus("Paused. " + queued + " items in the queue.", false);
	}

}