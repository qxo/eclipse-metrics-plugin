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
package net.sourceforge.metrics.ui.preferences;

import java.util.StringTokenizer;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Base category for the settings of the individual metrics. Allows the display order of metrics to be modified.
 * 
 * @author Frank Sauer
 * @see PreferencePage
 */
public class MetricsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, Constants {

	public MetricsPreferencePage() {
		super(GRID);
		setPreferenceStore(MetricsPlugin.getDefault().getPreferenceStore());
		setDescription("General preferences for metrics");
	}

	@Override
	public void createFieldEditors() {
		addField(new IntegerFieldEditor("METRICS.decimals", "Number of decimal places for Average and Standard Deviation", getFieldEditorParent()));
		addField(new BooleanFieldEditor("METRICS.showProject", "Display project level metrics after a build completes", getFieldEditorParent()));
		addField(new BooleanFieldEditor("METRICS.enablewarnings", "Enable out-of-range warnings", getFieldEditorParent()));
		addField(new ListUpDownEditor("METRICS.displayOrder", "Display metrics in this order:", getFieldEditorParent()) {
			@Override
			protected String createList(String[] items) {
				StringBuffer b = new StringBuffer();
				for (String item : items) {
					b.append(item).append(",");
				}
				return b.substring(0, b.length() - 1);
			}

			@Override
			protected String getNewInputObject() {
				return null;
			}

			@Override
			protected String[] parseString(String stringList) {
				StringTokenizer t = new StringTokenizer(stringList, ",");
				int length = t.countTokens();
				String[] items = new String[length];
				for (int i = 0; i < length; i++) {
					items[i] = t.nextToken().trim();
				}
				return items;
			}
		});
	}

	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#contributeButtons(org.eclipse .swt.widgets.Composite)
	 */
	@Override
	protected void contributeButtons(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns += 1;
		super.contributeButtons(parent);
		/**
		 * Button clearCache = new Button(parent,SWT.PUSH); clearCache.setText("Clear Cache"); clearCache.addSelectionListener(new SelectionListener() {
		 * 
		 * public void widgetSelected(SelectionEvent e) { if (MessageDialog.openConfirm(getShell(), "Clear Cache", "This will remove all stored metrics and force recalculation.\nAre you sure?" )) Cache.singleton.clear(); }
		 * 
		 * public void widgetDefaultSelected(SelectionEvent e) { } });
		 */
		Button eraseMarkers = new Button(parent, SWT.PUSH);
		eraseMarkers.setText("Erase All Warnings");
		eraseMarkers.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (MessageDialog.openConfirm(getShell(), "Erase warnings", "This will remove all out of range warnings.\nRecalculation is needed to get them back.\nAre you sure?")) {
					try {
						ResourcesPlugin.getWorkspace().getRoot().deleteMarkers("net.sourceforge.metrics.outofrangemarker", true, IResource.DEPTH_INFINITE);
					} catch (CoreException x) {
						Log.logError("Could not delete markers", x);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

}