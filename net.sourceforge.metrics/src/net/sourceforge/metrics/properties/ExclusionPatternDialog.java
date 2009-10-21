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
 * $Id: ExclusionPatternDialog.java,v 1.2 2004/05/05 03:42:31 sauerf Exp $
 */
package net.sourceforge.metrics.properties;

import net.sourceforge.metrics.core.MetricDescriptor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * Allow the addition, editing or removal of exclusion patterns
 * 
 * @author Frank Sauer
 */
public class ExclusionPatternDialog extends TitleAreaDialog implements SelectionListener {

	private List patternList;
	private String[] patterns;
	private static int ADD_BUTTONID = 100;
	private static int EDIT_BUTTONID = 101;
	private static int REMOVE_BUTTONID = 102;

	private Button removeButton;

	private Button editButton;

	private Button addButton;

	private MetricDescriptor descriptor;

	/**
	 * @param parentShell
	 */
	public ExclusionPatternDialog(Shell parentShell, MetricDescriptor md, String[] patterns) {
		super(parentShell);
		this.descriptor = md;
		this.patterns = patterns;
	}

	public String[] getPatterns() {
		return patterns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Edit Exclusion Patterns");
		setMessage("Use this dialog to add, edit or remove exclusion patterns for " + descriptor.getName(), IMessageProvider.INFORMATION);
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout l = new GridLayout();
		l.marginWidth = 5;
		l.marginHeight = 5;
		c.setLayout(l);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		c.setLayoutData(data);
		patternList = new List(c, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
		patternList.setLayoutData(data);
		patternList.setItems(patterns);
		patternList.addSelectionListener(this);
		return super.createDialogArea(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse .swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		addButton = createButton(parent, ADD_BUTTONID, "Add", false);
		addButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				patternList.add("sample*.java");
				patterns = patternList.getItems();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		editButton = createButton(parent, EDIT_BUTTONID, "Edit", false);
		editButton.setEnabled(false);
		removeButton = createButton(parent, REMOVE_BUTTONID, "Remove", false);
		removeButton.setEnabled(false);
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		editButton.setEnabled(true);
		removeButton.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}

}
