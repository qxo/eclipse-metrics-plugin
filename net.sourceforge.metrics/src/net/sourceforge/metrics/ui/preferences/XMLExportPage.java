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

import net.sourceforge.metrics.core.ExportDescriptor;
import net.sourceforge.metrics.core.MetricsPlugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Frank Sauer
 */
public class XMLExportPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private RadioGroupFieldEditor editor;

	public XMLExportPage() {
		super(GRID);
		setPreferenceStore(MetricsPlugin.getDefault().getPreferenceStore());
	}

	protected void createFieldEditors() {
		ExportDescriptor[] exporters = MetricsPlugin.getDefault().getExporters();
		if (exporters.length>0) {
			String[][] labels = new String[exporters.length][2];
			for (int i = 0; i < exporters.length;i++) {
				ExportDescriptor next = exporters[i];
				labels[i][0] = next.getName();
				labels[i][1] = next.getClassName();
			}
			editor= new RadioGroupFieldEditor(
						"METRICS.xmlformat", "XML Export format", 1,
						labels,
						getFieldEditorParent(),
					  	true);		
			addField(editor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		getPreferenceStore().setDefault("METRICS.xmlformat", "net.sourceforge.metrics.internal.xml.MetricsFirstExporter");
		String current = getPreferenceStore().getString("METRICS.xmlformat");
		ExportDescriptor xd = MetricsPlugin.getDefault().getExporter(current);
		if (xd != null) setDescription(xd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);		
		if (event.getSource() == editor) {
			String className = (String)event.getNewValue();
			ExportDescriptor xd = MetricsPlugin.getDefault().getExporter(className);
			if (xd != null) {
				setDescription(xd);
			}
		}
	}

	/**
	 * @param xd
	 */
	private void setDescription(ExportDescriptor xd) {
		setMessage(xd.getDescription());
	}

}
