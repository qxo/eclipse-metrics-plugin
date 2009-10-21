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
package net.sourceforge.metrics.ui.dependencies;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import classycle.graph.Vertex;

/**
 * Dialog to show topological sort result
 * 
 * @author Frank Sauer
 */
public class TopoSortDialog extends TitleAreaDialog {

	private Vertex[] sorted;

	private static final int COLUMN_WIDTH = 60;
	
	private ListViewer sortedV;
	private Vertex[] graph;

	public static void showUI(final Vertex[] graph) {
		final Display d = Display.getDefault();
		d.syncExec(new Runnable() {
				
			public void run() {
				Shell shell = new Shell(d);
				TopoSortDialog td = new TopoSortDialog(shell, graph);
				td.open();
			}
		});
	}


	public TopoSortDialog(Shell parent, Vertex[] graph) {
		super(parent);
		this.graph = graph;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		Composite c = new Composite(composite, SWT.NONE);
		c.setLayout(new GridLayout());
		VertexLabelProvider vlp = new VertexLabelProvider();
		sortedV = createListViewer(parent, c);
		sortedV.setLabelProvider(vlp);
		sorted = new TopoSort().sort(graph);
		setTitle("The topologically sorted dependency graph gives a possible build order.");
		if (sorted != null) {
			sortedV.add(sorted);
		} else {
			setErrorMessage("Graph has cycles...");
		}
		return composite;
	}

	private ListViewer createListViewer(Composite parent, Composite c) {
		ListViewer v = new ListViewer(c, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		v.getList().setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = convertHeightInCharsToPixels(20);
		data.widthHint = convertWidthInCharsToPixels(COLUMN_WIDTH);
		data.grabExcessHorizontalSpace = true;
		v.getList().setLayoutData(data);
		return v;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Topological Sort");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		Button copyButton = createButton(parent, IDialogConstants.DETAILS_ID, "Copy to Clipboard", false);
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
		copyButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				copyToClipboard();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		copyButton.setEnabled(sorted != null);
	}
	
	protected void copyToClipboard() {
		Clipboard clipboard = new Clipboard(getShell().getDisplay());
		StringBuffer b = new StringBuffer();
		VertexLabelProvider vlp = new VertexLabelProvider();
		for (int i = 0; i < sorted.length; i++) {
			String line = vlp.getText(sorted[i]);
			b.append(line).append("\n");
		}
		TextTransfer textTransfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[]{b.toString()}, new Transfer[]{textTransfer});
		clipboard.dispose();
	}
	
	static class VertexSorter extends ViewerSorter {
		VertexSorter() {
			super();
		}
	}
	

	static class VertexLabelProvider extends LabelProvider {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof Vertex) {
				Vertex v = (Vertex)element;
				PackageAttributes a = (PackageAttributes) v.getAttributes();
				return a.getLabel();
			} else {
				return super.getText(element);
			}
		}
	}
}
