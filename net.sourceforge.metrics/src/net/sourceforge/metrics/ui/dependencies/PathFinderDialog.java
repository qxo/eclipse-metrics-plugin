/*
 * Created on May 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sourceforge.metrics.ui.dependencies;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.metrics.internal.xml.XMLPrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import classycle.graph.Vertex;

/**
 * Dialog to show from/to lists and a resulting shortest (reverse) path
 * 
 * @author Frank Sauer
 */
public class PathFinderDialog extends TitleAreaDialog {

	private Button exportButton;

	private static final int COLUMN_WIDTH = 40;

	private ListViewer revV;
	private boolean showReverse;
	private Vertex from;
	private Vertex to;
	private ListViewer pathV;
	private ListViewer toV;
	private ListViewer fromV;
	private Button findButton;
	private PathFinder finder;

	/**
	 * Open a dialog to determine the shortest path from one node to another and optionally the reverse path as well. The nodes are obtained from the PathFinder
	 * 
	 * @param parent
	 *            parent shell for this dialog
	 * @param pf
	 *            Pathfinder with knowledge of the graph
	 * @param showReverse
	 *            boolean indicating whether reverse must be shown
	 */
	public PathFinderDialog(Shell parent, PathFinder pf, boolean showReverse) {
		super(parent);
		this.finder = pf;
		this.showReverse = showReverse;
	}

	/**
	 * Open a dialog to determine the shortest path from one node to another and no reverse path. The nodes are obtained from the PathFinder
	 * 
	 * @param parent
	 *            parent shell for this dialog
	 * @param pf
	 *            Pathfinder with knowledge of the graph
	 */
	public PathFinderDialog(Shell parent, PathFinder pf) {
		this(parent, pf, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite c = new Composite(composite, SWT.NONE);
		Font font = parent.getFont();
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		gl.makeColumnsEqualWidth = true;
		c.setLayout(gl);
		Label l = new Label(c, SWT.NONE);
		l.setText("From");
		l = new Label(c, SWT.NONE);
		l.setText("To");

		fromV = createListViewer(parent, c);
		toV = createListViewer(parent, c);

		l = new Label(c, SWT.NONE);
		l.setText("Shortest Path");
		if (showReverse) {
			l = new Label(c, SWT.NONE);
			l.setText("Reverse Shortest Path");
		}
		pathV = new ListViewer(c, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		pathV.getList().setFont(font);
		GridData d = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		if (!showReverse) {
			d.heightHint = convertHeightInCharsToPixels(6);
			d.widthHint = convertWidthInCharsToPixels(2 * COLUMN_WIDTH);
			d.horizontalSpan = 2;
			pathV.getList().setLayoutData(d);
		} else {
			d.heightHint = convertHeightInCharsToPixels(6);
			d.widthHint = convertWidthInCharsToPixels(COLUMN_WIDTH);
			pathV.getList().setLayoutData(d);
			revV = new ListViewer(c, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			d = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
			d.heightHint = convertHeightInCharsToPixels(6);
			d.widthHint = convertWidthInCharsToPixels(COLUMN_WIDTH);
			revV.getList().setFont(font);
			revV.getList().setLayoutData(d);

		}
		setupViewers();
		setTitle("Find the shortest path between two nodes.");
		setMessage("Click the find button to determine shortest paths");
		setErrorMessage("Please select a source and target node");
		return composite;
	}

	private ListViewer createListViewer(Composite parent, Composite c) {
		ListViewer v = new ListViewer(c, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		v.getList().setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = convertHeightInCharsToPixels(10);
		data.widthHint = convertWidthInCharsToPixels(COLUMN_WIDTH);
		v.getList().setLayoutData(data);
		v.setSorter(new VertexSorter());
		return v;
	}

	/**
	 * @param fromList
	 * @param toList
	 */
	private void setupViewers() {
		VertexLabelProvider vlp = new VertexLabelProvider();
		fromV.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				fromSelected(event.getSelection());
			}
		});
		toV.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				toSelected(event.getSelection());
			}
		});
		fromV.setLabelProvider(vlp);
		toV.setLabelProvider(vlp);
		pathV.setLabelProvider(vlp);
		Vertex[] graph = finder.getVertices();
		fromV.add(graph);
		toV.add(graph);
		if (revV != null) {
			revV.setLabelProvider(vlp);
		}
	}

	/**
	 * @param selection
	 */
	protected void toSelected(ISelection selection) {
		if ((selection != null) && !(selection.isEmpty())) {
			to = (Vertex) ((IStructuredSelection) selection).getFirstElement();
		} else {
			to = null;
		}
		updateFindButton();
	}

	/**
	 * 
	 */
	private void updateFindButton() {
		if ((to != null) && (from != null)) {
			setErrorMessage(null);
			findButton.setEnabled(true);
		} else {
			findButton.setEnabled(false);
		}
	}

	/**
	 * @param selection
	 */
	protected void fromSelected(ISelection selection) {
		if ((selection != null) && !(selection.isEmpty())) {
			from = (Vertex) ((IStructuredSelection) selection).getFirstElement();
		} else {
			from = null;
		}
		updateFindButton();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets .Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Shortest Paths");
	}

	private void findButtonClicked() {
		Vertex[] path = finder.findShortestPath(from, to);
		pathV.getList().removeAll();
		if (path != null) {
			setErrorMessage(null);
			pathV.add(path);
			if (showReverse) {
				Vertex[] rev = finder.findShortestPath(to, from);
				revV.getList().removeAll();
				revV.add(rev);
			}
		} else {
			setErrorMessage("No path found.");
		}
	}

	private void exportButtonClicked() {
		FileDialog d = new FileDialog(getShell(), SWT.SAVE);
		String fileName = d.open();
		if (fileName != null) {
			File f = new File(fileName);
			try {
				FileOutputStream out = new FileOutputStream(f);
				final XMLPrintStream x = new XMLPrintStream(out);
				IRunnableWithProgress op = new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						exportCycles(x, monitor);
					}
				};
				new ProgressMonitorDialog(getShell()).run(true, false, op);
				x.close();
			} catch (Throwable e) {

			}

		}
	}

	/**
	 * export all cycles consisting of shortest paths to the given xml stream
	 * 
	 * @param x
	 */
	private void exportCycles(XMLPrintStream x, IProgressMonitor monitor) {
		x.printXMLHeader();
		x.println("<cycles>");
		Vertex[] from = finder.getVertices();
		Vertex[] to = finder.getVertices();
		monitor.beginTask("Exporting cycles to XML", from.length);
		for (int f = 0; f < from.length; f++) {
			for (int t = 0; t < to.length; t++) {
				if (f != t) {
					Vertex[] path = findCycle(from[f], to[t]);
					x.indent(1);
					x.print("<cycle length=\"");
					x.print(path.length);
					x.println("\">");
					for (Vertex element : path) {
						PackageAttributes a = (PackageAttributes) element.getAttributes();
						IJavaElement elm = a.getJavaElement();
						x.indent(2);
						x.print("<element");
						if (elm != null) {
							x.print(" kind=\"type");
							x.print("\">");
							x.print(((IType) elm).getFullyQualifiedName());
						} else {
							x.print(" kind=\"package");
							x.print("\">");
							x.print(a.getLabel());
						}
						x.println("</element>");
					}
					x.indent(1);
					x.println("</cycle>");
				}
			}
			monitor.worked(1);
		}
		monitor.done();
		x.indent(0);
		x.println("</cycles>");
	}

	/**
	 * Find a cycle by calculating the path from from to to and the reverse path from to to from and appending them.
	 * 
	 * @param vertex
	 * @param vertex2
	 * @return String[] containing a full walk from from to to and back to from
	 */
	private Vertex[] findCycle(Vertex from, Vertex to) {
		Vertex[] forward = finder.findShortestPath(from, to);
		Vertex[] reverse = finder.findShortestPath(to, from);
		List<Vertex> result = new ArrayList<Vertex>();
		for (Vertex element : forward) {
			result.add(element);
		}
		Vertex pivot = forward[forward.length - 1];
		int index = -1;
		for (int i = 0; i < reverse.length; i++) {
			if (reverse[i] == pivot) {
				index = i;
				break;
			}
		}
		for (int i = index + 1; i < reverse.length; i++) {
			result.add(reverse[i]);
		}
		return result.toArray(new Vertex[] {});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse .swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		findButton = createButton(parent, IDialogConstants.DETAILS_ID, "Find", false);
		findButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				findButtonClicked();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		findButton.setEnabled(false);
		if (showReverse) {
			exportButton = createButton(parent, IDialogConstants.INTERNAL_ID, "Export All", false);
			exportButton.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					exportButtonClicked();
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
	}

	static class VertexSorter extends ViewerSorter {
		VertexSorter() {
			super();
		}
	}

	static class VertexLabelProvider extends LabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			if (element instanceof Vertex) {
				Vertex v = (Vertex) element;
				PackageAttributes a = (PackageAttributes) v.getAttributes();
				return a.getLabel();
			} /* else { */
			return super.getText(element);
			/* } */
		}
	}
}
